package Core.DiffTest

import Core.Bundles.RegfileWritePortIO
import chisel3._

class DiffTestIO extends Bundle with Core.CoreConfig {
  val reg = Output(Vec(REG_NUM, UInt(DATA_WIDTH)))
  val commit = Output(Bool())
  val wreg = Flipped(new RegfileWritePortIO)
}
