package Core

import chisel3._
import chisel3.util._
import Core.Decode.{InstJ, _}

class IDU_IO extends Bundle {
  val in: PcInstPathIO = Flipped(new PcInstPathIO)
  val out = new DecoderOutPort
}

class IDU extends Module with HasRs1Type with HasRs2Type with CoreConfig with HasInstType {
  val io: IDU_IO = IO(new IDU_IO)
  private val inst = io.in.inst
  private val rs1Reg = inst(19, 15)
  private val rs2Reg = inst(24, 20)
  private val rdReg  = inst(11, 7)
  private val instType :: funcType :: opType :: rdEna :: Nil
    = ListLookup(io.in.inst, DecodeDefault, DecodeTable)
//  io.out.rs1Type := MuxLookup(funcType, N, RsTypeTable.map(p => (p._1, p._2._1)).toSeq)
//  io.out.rs2Type := MuxLookup(funcType, N, RsTypeTable.map(p => (p._1, p._2._2)).toSeq)
  Cat(io.out.rs1Type, io.out.rs2Type) := MuxLookup(instType, Cat(Rs1None, Rs2None), RsTypeTable.map(p=>(p._1, Cat(p._2._1, p._2._1))))
  io.out.funcType := funcType
  io.out.opType   := opType
  io.out.rs1Addr   := rs1Reg
  io.out.rs2Addr   := rs2Reg
  io.out.rdEna    := rdEna
  io.out.rdAddr    := rdReg

  def msb (src: UInt) : Bool = src(src.getWidth - 1).asBool()
  def sext(width: Int, src: UInt) : UInt = Cat(Fill(width - src.getWidth, msb(src)), src)
  def zext(width: Int, src: UInt) : UInt = Cat(Fill(width - src.getWidth, 0.U(1.W)), src)

  // data
  io.out.rs1Data      := DontCare
  io.out.rs2Data      := MuxLookup(instType, 0.U, List(
      InstU -> sext(XLEN, Cat(inst(31, 12), 0.U(12.W))),
      InstJ -> sext(XLEN, Cat(inst(31), inst(19,12), inst(20), inst(30,21), 0.U(1.W))),
      InstB -> sext(XLEN, Cat(inst(31), inst(7), inst(30,25), inst(11,8), 0.U(1.W))),
      InstI -> sext(XLEN, inst(31,20)),
      InstS -> sext(XLEN, Cat(inst(31,25), inst(11,7)))
  ))
  io.out.rd           := DontCare

}
