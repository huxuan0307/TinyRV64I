package Core

import Core.Decode._
import chisel3._
import chisel3.util._

class InstDecodeUnitIO extends Bundle {
  val in: PcInstPathIO = Flipped(new PcInstPathIO)
  val out = new InstDecodeUnitOutPort
  val illegal : Bool = Output(Bool())
}

class InstDecodeUnit extends Module with HasRs1Type with HasRs2Type with CoreConfig with HasInstType {
  val io: InstDecodeUnitIO = IO(new InstDecodeUnitIO)
  protected val inst : UInt = io.in.inst
  protected val rs1Reg : UInt = inst(19, 15)
  protected val rs2Reg : UInt = inst(24, 20)
  protected val rdReg : UInt = inst(11, 7)
  protected val instType :: funcType :: opType :: rdEna :: Nil
    = ListLookup(inst, DecodeDefault, DecodeTable)
  io.out.rs1Type := MuxLookup(instType, Rs1None, RsTypeTable.map(p => (p._1, p._2._1)))
  io.out.rs2Type := MuxLookup(instType, Rs2None, RsTypeTable.map(p => (p._1, p._2._2)))
//  Cat(io.out.rs1Type, io.out.rs2Type) := MuxLookup(instType, Cat(Rs1None, Rs2None), RsTypeTable.map(p=>(p._1, Cat(p._2._1, p._2._1))))
  io.out.funcType := funcType
  io.out.opType   := opType
  io.out.rs1Addr   := rs1Reg
  io.out.rs2Addr   := rs2Reg
  io.out.rdEna    := rdEna
  io.out.rdAddr    := rdReg



//  private val tmp = Cat(inst(31, 12), 0.U(12.W))

  // data
  io.out.rs1_data      := DontCare
  io.out.rs2_data      := MuxLookup(instType, 0.U, List(
      InstU -> sext(XLEN, Cat(inst(31, 12), 0.U(12.W))),
      InstJ -> sext(XLEN, Cat(inst(31), inst(19,12), inst(20), inst(30,21), 0.U(1.W))),
      InstB -> sext(XLEN, Cat(inst(31), inst(7), inst(30,25), inst(11,8), 0.U(1.W))),
      InstI -> sext(XLEN, inst(31,20)),
      InstS -> sext(XLEN, Cat(inst(31,25), inst(11,7)))
  ))
  io.out.is_word_type   := inst(3)
  io.out.rd_data        := DontCare
  io.out.pc           := io.in.pc
  io.illegal          := instType === InstN
}
