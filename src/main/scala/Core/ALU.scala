package Core
import chisel3._
import chisel3.util.{MuxLookup, Cat}

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
  val io = IO(new Bundle() {
    val op: UInt = Input(UInt(5.W))
    val a: UInt = Input(UInt(32.W))
    val b: UInt = Input(UInt(32.W))
    val out: UInt = Output(UInt(32.W))
  })

  val OpList = List(
    (OP.ADD, io.a + io.b),
    (OP.SLL, io.a << io.b(4, 0)), // just b[4:0]
    (OP.SLT, Cat(0.U(63.W), io.a.asSInt < io.b.asSInt)),
    (OP.SLTU, io.a < io.b),
    (OP.XOR, io.a ^ io.b),
    (OP.SRL, io.a >> io.b(4, 0)), // just b[4:0]
    (OP.OR, io.a | io.b),
    (OP.AND, io.a & io.b),
    (OP.SUB, io.a - io.b),
    (OP.SRA, (io.a.asSInt >> io.b(4,0)).asUInt)
  )

  io.out := MuxLookup(io.op, 0.U, OpList)
}
