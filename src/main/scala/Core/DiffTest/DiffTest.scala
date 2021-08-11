package Core.DiffTest

import Core.Bundles.RegfileWritePortIO
import Core.Config.CoreConfig
import chisel3._

class TrapIO extends Bundle with CoreConfig {
  val valid : Bool = Output(Bool())
  val code : UInt = Output(UInt(3.W))
}

class DiffTestIO extends Bundle with CoreConfig {
  val commit : Bool = Output(Bool())
  val wreg : RegfileWritePortIO = Flipped(new RegfileWritePortIO)
  val trap = new TrapIO
}
