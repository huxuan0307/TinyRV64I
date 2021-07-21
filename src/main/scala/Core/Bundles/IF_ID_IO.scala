package Core.Bundles

import chisel3._

class IF_ID_IO extends Bundle {
  val inst: UInt = Output(UInt(32.W))
}
