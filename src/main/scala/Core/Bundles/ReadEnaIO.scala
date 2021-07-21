package Core.Bundles

import chisel3._

class ReadEnaIO extends Bundle {
  val ena: UInt = Input(UInt(1.W))
  val addr: UInt = Input(UInt(32.W))
  val data: UInt = Output(UInt(32.W))
}
