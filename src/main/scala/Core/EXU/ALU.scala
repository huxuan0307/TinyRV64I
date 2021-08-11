package Core.EXU

import Core.Config.{CoreConfig, HasFullOpType}
import Util.sext
import chisel3._
import chisel3.util.{Cat, MuxLookup}

class ALU extends Module with CoreConfig {
  val io: ALU_IO = IO(new ALU_IO)
  private val op_num1 = Wire(UInt(DATA_WIDTH))
  private val op_num2 = Wire(UInt(DATA_WIDTH))
  private val shamt = Wire(UInt(6.W))
  op_num1 := io.in.a
  op_num2 := io.in.b
  shamt := Mux(io.in.is_word_type, op_num2(4, 0), op_num2(5, 0))
  private val OpList = List(
    (AluOp.ADD, op_num1 + op_num2),
    (AluOp.SLL, op_num1 << shamt),
    (AluOp.SLT, Cat(0.U((XLEN - 1).W), op_num1.asSInt < op_num2.asSInt)),
    (AluOp.SLTU, op_num1 < op_num2),
    (AluOp.XOR, op_num1 ^ op_num2),
    (AluOp.SRL, Mux(io.in.is_word_type, op_num1(31,0), op_num1) >> shamt),
    (AluOp.OR, op_num1 | op_num2),
    (AluOp.AND, op_num1 & op_num2),
    (AluOp.SUB, op_num1 - op_num2),
    (AluOp.SRA, Mux(io.in.is_word_type,
      (op_num1(31,0).asSInt() >> shamt).asUInt(),
      (op_num1.asSInt >> shamt).asUInt)),
    // 不需要再位移12位，在DataPathUnit里已经位移了
    (AluOp.LUI, op_num2)
  )
  val tmp_res : UInt = Wire(UInt(DATA_WIDTH))
  tmp_res := MuxLookup(io.in.op, 0.U, OpList)

  io.out.data := Mux(
    io.in.is_word_type,
    sext(XLEN, tmp_res(31, 0)),
    tmp_res
  )
}

// {funct7[5], funct3[2:0]}
object AluOp {
  def ADD : UInt  = "b0000".U
  def SLL : UInt  = "b0001".U
  def SLT : UInt  = "b0010".U
  def SLTU: UInt  = "b0011".U
  def XOR : UInt  = "b0100".U
  def SRL : UInt  = "b0101".U
  def OR  : UInt  = "b0110".U
  def AND : UInt  = "b0111".U
  def SUB : UInt  = "b1000".U | ADD
  def SRA : UInt  = "b1000".U | SRL
  def LUI : UInt  = "b1111".U
}

class ALU_InputPortIO extends Bundle with CoreConfig with HasFullOpType{
  val ena : Bool = Input(Bool())
  val op: UInt = Input(UInt(FullOpTypeWidth))
  val a: UInt = Input(UInt(DATA_WIDTH))
  val b: UInt = Input(UInt(DATA_WIDTH))
  val is_word_type : Bool = Input(Bool())
}

class ALU_OutputPortIO extends Bundle with CoreConfig {
  val data: UInt = Output(UInt(DATA_WIDTH))
}

class ALU_IO extends Bundle {
  val in = new ALU_InputPortIO
  val out = new ALU_OutputPortIO
}