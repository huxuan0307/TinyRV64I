package Core.EXU

import Core._
import chisel3._
import chisel3.util._

object LsuOp {
  val LB  :UInt  = "b0000".U
  val LH  :UInt  = "b0001".U
  val LW  :UInt  = "b0010".U
  val LBU :UInt  = "b0100".U
  val LHU :UInt  = "b0101".U
  val SB  :UInt  = "b1000".U
  val SH  :UInt  = "b1001".U
  val SW  :UInt  = "b1010".U
}

class LoadStoreUnitInPort extends Bundle with CoreConfig with HasFullOpType {
  val ena : Bool = Input(Bool())
  val op_type : UInt = Input(UInt(FullOpTypeWidth))
  val op_num1 : UInt = Input(UInt(ADDR_WIDTH))
  val op_num2 : UInt = Input(UInt(ADDR_WIDTH))
  val data : UInt = Input(UInt(DATA_WIDTH))
}

class LoadStoreUnitOutPort extends Bundle with CoreConfig with HasFullOpType {
  val data : UInt = Output(UInt(DATA_WIDTH))

}

class LoadStoreUnitIO extends Bundle with CoreConfig with HasFullOpType {
  val in = new LoadStoreUnitInPort
  val out = new LoadStoreUnitOutPort
  val dmem : DMemIO = Flipped(new DMemIO)
}

class LoadStoreUnit extends Module with CoreConfig with HasMemDataType {
  val io : LoadStoreUnitIO = IO(new LoadStoreUnitIO)
  io.dmem.addr := Mux(io.in.ena, io.in.op_num1 + io.in.op_num2, 0.U(ADDR_WIDTH))
  // 所有store指令的op_type[3]是1
  io.dmem.wena  := io.in.op_type(3) & io.in.ena
  io.out.data := MuxLookup(io.in.op_type, 0.U(DATA_WIDTH), Array(
    LsuOp.LB -> sext(XLEN, io.dmem.rdata(7, 0)),
    LsuOp.LH -> sext(XLEN, io.dmem.rdata(15,0)),
    LsuOp.LW -> sext(XLEN, io.dmem.rdata(31,0)),
    LsuOp.LBU-> zext(XLEN, io.dmem.rdata(7, 0)),
    LsuOp.LHU-> zext(XLEN, io.dmem.rdata(15,0))
  ))
  io.dmem.wdata := Mux(io.in.ena, MuxLookup(io.in.op_type, 0.U(DATA_WIDTH), Array(
    // How to save byte or half word? It's none of my business...
    LsuOp.SB -> zext(XLEN, io.in.data(7, 0)),
    LsuOp.SH -> zext(XLEN, io.in.data(15,0)),
    LsuOp.SW -> zext(XLEN, io.in.data(31,0))
  )),0.U(DATA_WIDTH))
  io.dmem.data_type := MuxLookup(io.in.op_type, type_w, Array(
    LsuOp.SB -> type_b,
    LsuOp.SH -> type_h,
    LsuOp.SW -> type_w
  ))
  io.dmem.debug.addr := DontCare
}
