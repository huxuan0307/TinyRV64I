package Core

import chisel3._

class Adder extends Module {
  val io = IO(new Bundle{
    val a: UInt = Input(UInt(32.W))
    val b: UInt = Input(UInt(32.W))
    val sum: UInt = Output(UInt(32.W))
  })
  io.sum := io.a + io.b
}
