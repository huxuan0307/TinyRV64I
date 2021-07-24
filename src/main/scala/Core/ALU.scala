package Core
import Core.Bundles.ALU_IO


import chisel3._
import chisel3.util.{Cat, MuxLookup}

// {1'b0, funct7[5], funct3[2:0]}
private object OP {
  def ADD: UInt = "b00000".U(5.W)
  def SLL: UInt = "b00001".U(5.W)
  def SLT: UInt = "b00010".U(5.W)
  def SLTU: UInt = "b00011".U(5.W)
  def XOR: UInt = "b00100".U(5.W)
  def SRL: UInt = "b00101".U(5.W)
  def OR: UInt = "b00110".U(5.W)
  def AND: UInt = "b00111".U(5.W)
  def SUB: UInt = ADD | "b01000".U(5.W)
  def SRA: UInt = SRL | "b01000".U(5.W)
}

class ALU extends Module {
  val io: ALU_IO = IO(new ALU_IO)

  val OpList = List(
    (OP.ADD, io.in.a + io.in.b),
    (OP.SLL, io.in.a << io.in.b(4, 0)), // just b[4:0]
    (OP.SLT, Cat(0.U(63.W), io.in.a.asSInt < io.in.b.asSInt)),
    (OP.SLTU, io.in.a < io.in.b),
    (OP.XOR, io.in.a ^ io.in.b),
    (OP.SRL, io.in.a >> io.in.b(4, 0)), // just b[4:0]
    (OP.OR, io.in.a | io.in.b),
    (OP.AND, io.in.a & io.in.b),
    (OP.SUB, io.in.a - io.in.b),
    (OP.SRA, (io.in.a.asSInt >> io.in.b(4,0)).asUInt)
  )

  io.out.data := MuxLookup(io.in.op, 0.U, OpList)
}
