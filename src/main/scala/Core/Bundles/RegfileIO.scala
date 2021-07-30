package Core.Bundles

import chisel3._

class RegfileIO extends Bundle{
  val r1 = new RegfileReadPortIO
  val r2 = new RegfileReadPortIO
  val w = new RegfileWritePortIO
  val debug = new RegfileDebugIO
}

class RegfileReadPortIO extends Bundle with Core.CoreConfig {
  val addr: UInt = Input(UInt(REG_ADDR_WIDTH))
  val data: UInt = Output(UInt(DATA_WIDTH))
}

class RegfileDebugIO extends RegfileReadPortIO

class RegfileWritePortIO extends Bundle with Core.CoreConfig {
  val ena: UInt = Input(UInt(1.W))
  val addr: UInt = Input(UInt(REG_ADDR_WIDTH))
  val data: UInt = Input(UInt(DATA_WIDTH))
}