package Core.Bundles

import chisel3._

class AluIOs extends Bundle {
  val op: UInt = Input(UInt(5.W))
  val a: UInt = Input(UInt(32.W))
  val b: UInt = Input(UInt(32.W))
  val out: UInt = Output(UInt(32.W))
}
