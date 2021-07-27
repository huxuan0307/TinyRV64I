package Core.EXU

import Core.Bundles.ALU_IO
import Core.CoreConfig
import chisel3._
import chisel3.util.{Cat, MuxLookup}

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

class ALU extends Module with CoreConfig {
  val io: ALU_IO = IO(new ALU_IO)

  private val OpList = List(
    (AluOp.ADD, io.in.a + io.in.b),
    (AluOp.SLL, io.in.a << io.in.b(4, 0)), // just b[4:0]
    (AluOp.SLT, Cat(0.U((XLEN - 1).W), io.in.a.asSInt < io.in.b.asSInt)),
    (AluOp.SLTU, io.in.a < io.in.b),
    (AluOp.XOR, io.in.a ^ io.in.b),
    (AluOp.SRL, io.in.a >> io.in.b(4, 0)), // just b[4:0]
    (AluOp.OR, io.in.a | io.in.b),
    (AluOp.AND, io.in.a & io.in.b),
    (AluOp.SUB, io.in.a - io.in.b),
    (AluOp.SRA, (io.in.a.asSInt >> io.in.b(4,0)).asUInt),
    // 不需要再位移12位，在DataPathUnit里已经位移了
    (AluOp.LUI, io.in.b)
  )

  when(io.in.ena){
    io.out.data := MuxLookup(io.in.op, 0.U, OpList)
  } otherwise {
    io.out.data := 0.U(DATA_WIDTH)
  }
}
