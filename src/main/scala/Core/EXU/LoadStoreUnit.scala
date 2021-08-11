package Core.EXU

import Core._
import chisel3._
import chisel3.util._

object LsuOp {
  def LB  :UInt  = "b0000".U
  def LH  :UInt  = "b0001".U
  def LW  :UInt  = "b0010".U
  def LD  :UInt  = "b0011".U
  def LBU :UInt  = "b0100".U
  def LHU :UInt  = "b0101".U
  def LWU :UInt  = "b0110".U
  def SB  :UInt  = "b1000".U
  def SH  :UInt  = "b1001".U
  def SW  :UInt  = "b1010".U
  def SD  :UInt  = "b1011".U
}

class LoadStoreUnitInPort extends Bundle with CoreConfig with HasFullOpType {
  val ena : Bool = Input(Bool())
  val op_type : UInt = Input(UInt(FullOpTypeWidth))
  val op_num1 : UInt = Input(UInt(ADDR_WIDTH))
  val op_num2 : UInt = Input(UInt(ADDR_WIDTH))
  val wdata : UInt = Input(UInt(DATA_WIDTH))
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
    LsuOp.LD -> sext(XLEN, io.dmem.rdata(63,0)),
    LsuOp.LBU-> zext(XLEN, io.dmem.rdata(7, 0)),
    LsuOp.LHU-> zext(XLEN, io.dmem.rdata(15,0)),
    LsuOp.LWU-> zext(XLEN, io.dmem.rdata(31,0))
  ))
  io.dmem.wdata := Mux(io.in.ena, MuxLookup(io.in.op_type, 0.U(DATA_WIDTH), Array(
    // How to save byte or half word? It's none of my business...
    LsuOp.SB -> zext(XLEN, io.in.wdata(7, 0)),
    LsuOp.SH -> zext(XLEN, io.in.wdata(15,0)),
    LsuOp.SW -> zext(XLEN, io.in.wdata(31,0)),
    LsuOp.SD -> zext(XLEN, io.in.wdata(63,0))
  )),0.U(DATA_WIDTH))
  io.dmem.data_type := io.in.op_type(1,0)
  io.dmem.valid := io.in.ena & true.B
  io.dmem.debug.addr := DontCare
}
