package Core.Bundles

import Core.Config.CoreConfig
import chisel3.{Bundle, Output, UInt}

class PcInstPathIO extends Bundle with CoreConfig {
  val pc : UInt = Output(UInt(ADDR_WIDTH))
  val inst : UInt = Output(UInt(INST_WIDTH))
}
