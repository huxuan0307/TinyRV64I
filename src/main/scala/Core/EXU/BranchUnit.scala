package Core.EXU

import Core.{BranchPathIO, CoreConfig, HasFullOpType}
import chisel3._
import chisel3.util._

object BruOp {
  val BEQ  :UInt = "b0000".U
  val BNE  :UInt = "b0001".U
  val BLT  :UInt = "b0100".U
  val BGE  :UInt = "b0101".U
  val BLTU :UInt = "b0110".U
  val BGEU :UInt = "b0111".U
  val JAL  :UInt = "b1000".U
  val JALR :UInt = "b1001".U
}

class BranchUnitInPort extends Bundle with CoreConfig with HasFullOpType {
  val ena : Bool = Input(Bool())
  val op_type : UInt = Input(UInt(FullOpTypeWidth))
  val op_num1 : UInt = Input(UInt(DATA_WIDTH))
  val op_num2 : UInt = Input(UInt(DATA_WIDTH))
  val offset : UInt = Input(UInt(ADDR_WIDTH))
  val pc : UInt = Input(UInt(ADDR_WIDTH))
}

class BranchUnitIO extends Bundle with CoreConfig with HasFullOpType {
  val in = new BranchUnitInPort
  val out : BranchPathIO = new BranchPathIO
}

class BranchUnit extends Module {
  val io : BranchUnitIO = IO(new BranchUnitIO)
  io.out.valid := io.in.ena & MuxLookup(io.in.op_type, false.B, Array(
    BruOp.BEQ -> (io.in.op_num1 === io.in.op_num2),
    BruOp.BNE -> (io.in.op_num1 =/= io.in.op_num2),
    BruOp.BLT -> (io.in.op_num1 < io.in.op_num2),
    BruOp.BGE -> (io.in.op_num1 >= io.in.op_num2),
    BruOp.BLTU -> (io.in.op_num1.asSInt() < io.in.op_num2.asSInt()),
    BruOp.BGEU -> (io.in.op_num1.asSInt() >= io.in.op_num2.asSInt()),
    BruOp.JAL -> true.B,
    BruOp.JALR -> true.B
  ))
  // B-Type and jal: pc + offset
  // jalr: x[rs1] + offset
  io.out.new_pc := Mux(
    io.in.op_type =/= BruOp.JALR,
    io.in.pc + io.in.offset,
    io.in.op_num1 + io.in.offset
  )
}
