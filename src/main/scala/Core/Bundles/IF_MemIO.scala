package Core.Bundles

import chisel3._

class IF_MemIO extends Bundle {
  val pc: UInt = Output(UInt(32.W))
  val inst: UInt = Input(UInt(32.W))
}
