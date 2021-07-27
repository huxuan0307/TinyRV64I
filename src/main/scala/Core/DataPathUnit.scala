package Core

import Core.Bundles.{RegfileDebugIO, RegfileWritePortIO}
import Core.EXU.ExecuteInPort
import chisel3._
import chisel3.util._

class DataPathUnitIO extends Bundle {
  val from_idu : InstDecodeUnitOutPort = Flipped(new InstDecodeUnitOutPort)
  val from_wbu = new RegfileWritePortIO
  val to_exu : ExecuteInPort = Flipped(new ExecuteInPort)
  val debug = new RegfileDebugIO
}

class DataPathUnit extends Module with HasRs1Type with HasRs2Type {
  val io : DataPathUnitIO = IO(new DataPathUnitIO)
  val rf : Regfile = Module(new Regfile)
  rf.io.r1.addr   := io.from_idu.rs1Addr
  rf.io.r2.addr   := io.from_idu.rs2Addr
  rf.io.w <> io.from_wbu

  io.to_exu.op_num1   := MuxLookup(io.from_idu.rs1Type, 0.U, List(
    Rs1Reg -> rf.io.r1.data,
    Rs1PC  -> io.from_idu.pc
  ))
  io.to_exu.op_num2   := MuxLookup(io.from_idu.rs2Type, 0.U, List(
    Rs2Reg -> rf.io.r2.data,
    Rs2Imm -> io.from_idu.rs2Data
  ))
  io.to_exu.op_type   := io.from_idu.opType
  io.to_exu.func_type := io.from_idu.funcType
  io.to_exu.w.ena     := io.from_idu.rdEna
  io.to_exu.w.addr    := io.from_idu.rdAddr
  // 借用wdata放S指令的写内存数据
  io.to_exu.w.data    := rf.io.r2.data
  io.debug            <> rf.io.debug

}
