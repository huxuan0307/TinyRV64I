package Core.DiffTest

import Core.Bundles.RegfileWritePortIO
import chisel3._

class TrapIO extends Bundle {
  val valid : Bool = Output(Bool())
}

class DiffTestIO extends Bundle with Core.CoreConfig {
  val reg : Vec[UInt] = Output(Vec(REG_NUM, UInt(DATA_WIDTH)))
  val commit : Bool = Output(Bool())
  val wreg : RegfileWritePortIO = Flipped(new RegfileWritePortIO)
  val trap = new TrapIO
}
