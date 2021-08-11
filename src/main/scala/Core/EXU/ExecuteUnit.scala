package Core.EXU

import Core.Bundles.{BranchPathIO, RegfileWritePortIO}
import Core.Config.{CoreConfig, HasFullOpType, HasFuncType}
import Core.EXU.CSR.CSR
import Core._
import chisel3._
import chisel3.util._

class ExecuteUnit extends Module with HasFuncType with CoreConfig {
  val io: EXU_IO = IO(new EXU_IO)

  protected val alu : ALU           = Module(new ALU)
  protected val lsu : LoadStoreUnit = Module(new LoadStoreUnit)
  protected val bru : BranchUnit    = Module(new BranchUnit)
  protected val csr : CSR           = Module(new CSR)
  alu.io.in.a     := io.in.op_num1
  alu.io.in.b     := io.in.op_num2
  alu.io.in.op    := io.in.op_type
  alu.io.in.ena   := io.in.func_type === FuncALU
  alu.io.in.is_word_type := io.in.is_word_type
  lsu.io.in.op_num1 := io.in.op_num1
  lsu.io.in.op_num2 := io.in.op_num2
  lsu.io.in.wdata    := io.in.w.data
  lsu.io.in.op_type := io.in.op_type
  lsu.io.in.ena     := io.in.func_type === FuncLSU
  bru.io.in.op_num1 := io.in.op_num1// jalr:   x[rs1]
  bru.io.in.op_num2 := io.in.w.data // B-Type: x[rs2]
  bru.io.in.op_type := io.in.op_type
  bru.io.in.pc      := io.in.pc
  bru.io.in.offset  := io.in.op_num2// imm
  bru.io.in.ena     := io.in.func_type === FuncBRU
  csr.io.in.src     := io.in.op_num1
  csr.io.in.pc      := io.in.pc
  csr.io.in.op_type := io.in.op_type
  csr.io.in.addr    := io.in.op_num2
  csr.io.in.ena     := io.in.func_type === FuncCSRU

  io.out.w.data := MuxLookup(io.in.func_type, 0.U(XLEN.W), Array(
    FuncALU -> alu.io.out.data,
    FuncLSU -> lsu.io.out.data,
    FuncBRU -> (io.in.pc + 4.U),
    FuncCSRU-> csr.io.out.rdata,
  ))

  io.out.w.ena      := io.in.w.ena
  io.out.w.addr     := io.in.w.addr
  io.dmem           <> lsu.io.dmem
  io.branch   <> Mux(io.in.func_type === FuncBRU,
    bru.io.out,
    csr.io.out.jmp
  )
}

class ExecuteInPort extends Bundle with CoreConfig with HasFuncType with HasFullOpType{
  val func_type : UInt = Input(UInt(FuncTypeWidth))
  val op_type : UInt = Input(UInt(FullOpTypeWidth))
  val op_num1 : UInt = Input(UInt(DATA_WIDTH))
  val op_num2 : UInt = Input(UInt(DATA_WIDTH))
  val pc      : UInt = Input(UInt(ADDR_WIDTH))
  val w : RegfileWritePortIO = new RegfileWritePortIO
  val is_word_type : Bool = Input(Bool())
}

class ExecuteOutPort extends Bundle with CoreConfig {
  val w : RegfileWritePortIO = Flipped(new RegfileWritePortIO)
}

class EXU_IO extends Bundle{
  val in = new ExecuteInPort
  val out = new ExecuteOutPort
  val dmem : DMemIO = Flipped(new DMemIO)
  val branch = new BranchPathIO
}