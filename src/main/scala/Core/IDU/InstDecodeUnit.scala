package Core.IDU

import Core.Bundles.PcInstPathIO
import Core.Config._
import Core.EXU.CSR.CsrOp
import Core.IDU.Decode._
import Util.{sext, zext}
import chisel3._
import chisel3.util._

class InstDecodeUnit extends Module with HasRs1Type with HasRs2Type with CoreConfig with HasInstType with HasFuncType {
  val io: InstDecodeUnitIO = IO(new InstDecodeUnitIO)
  protected val inst : UInt = io.in.inst
  protected val rs1Reg : UInt = inst(19, 15)
  protected val rs2Reg : UInt = inst(24, 20)
  protected val rdReg : UInt = inst(11, 7)
  protected val uimm : UInt = inst(19, 15)
  protected val instType :: funcType :: opType :: rdEna :: rs1Type :: rs2Type :: Nil
    = ListLookup(inst, DecodeDefault, DecodeTable)
  io.out.rs1Type := rs1Type
  io.out.rs2Type := rs2Type
  io.out.funcType := funcType
  io.out.opType   := opType
  io.out.rs1Addr   := rs1Reg
  io.out.rs2Addr   := rs2Reg
  io.out.rdEna    := rdEna
  io.out.rdAddr    := rdReg

  io.out.uimm_ext     := Mux(CsrOp.is_csri(opType), zext(XLEN, uimm), 0.U)
  io.out.imm_ext      := MuxLookup(instType, 0.U, List(
    InstU -> sext(XLEN, Cat(inst(31, 12), 0.U(12.W))),
    InstJ -> sext(XLEN, Cat(inst(31), inst(19,12), inst(20), inst(30,21), 0.U(1.W))),
    InstB -> sext(XLEN, Cat(inst(31), inst(7), inst(30,25), inst(11,8), 0.U(1.W))),
    InstI -> sext(XLEN, inst(31,20)),   // csr addr here
    InstS -> sext(XLEN, Cat(inst(31,25), inst(11,7))),
    InstT -> sext(XLEN, inst(31,20))
  ))
  io.out.is_word_type   := inst(3)
  io.out.rd_data        := DontCare
  io.out.pc             := io.in.pc
  io.illegal            := instType === InstN
  io.is_trap            := instType === InstT // 自定义的Trap指令，单独的指令类型
}


trait DecodeConst extends HasRs1Type with HasRs2Type with HasFuncType with HasFullOpType

trait CtrlPathIO extends Bundle with DecodeConst with CoreConfig {
  val rs1Type   : UInt  = Output(UInt(Rs1TypeWidth))
  val rs2Type   : UInt  = Output(UInt(Rs2TypeWidth))
  val funcType  : UInt  = Output(UInt(FuncTypeWidth))
  val opType    : UInt  = Output(UInt(FullOpTypeWidth))
  val rs1Addr   : UInt  = Output(UInt(REG_ADDR_WIDTH))
  val rs2Addr   : UInt  = Output(UInt(REG_ADDR_WIDTH))
  val rdEna     : UInt  = Output(UInt(1.W))
  val rdAddr    : UInt  = Output(UInt(REG_ADDR_WIDTH))

  val is_word_type : Bool = Output(Bool())
}

class InstDecodeUnitOutPort extends CtrlPathIO {
  val pc : UInt = Output(UInt(ADDR_WIDTH))
  val uimm_ext: UInt = Output(UInt(DATA_WIDTH))
  val imm_ext: UInt = Output(UInt(DATA_WIDTH))
  val rd_data : UInt = Output(UInt(DATA_WIDTH))
}

class InstDecodeUnitIO extends Bundle {
  val in: PcInstPathIO = Flipped(new PcInstPathIO)
  val out = new InstDecodeUnitOutPort
  val illegal : Bool = Output(Bool())
  val is_trap : Bool = Output(Bool())
}
