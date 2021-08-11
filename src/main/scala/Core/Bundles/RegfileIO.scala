package Core.Bundles

import Core.Config.CoreConfig
import chisel3._

class RegfileReadPortIO extends Bundle with CoreConfig {
  val addr: UInt = Input(UInt(REG_ADDR_WIDTH))
  val data: UInt = Output(UInt(DATA_WIDTH))
}

class RegfileDebugIO extends RegfileReadPortIO

class RegfileWritePortIO extends Bundle with CoreConfig {
  val ena: Bool = Input(Bool())
  val addr: UInt = Input(UInt(REG_ADDR_WIDTH))
  val data: UInt = Input(UInt(DATA_WIDTH))
}