package Core.Bundles

import Core.Config.CoreConfig
import chisel3._

class BranchPathIO extends Bundle with CoreConfig {
  val valid : Bool = Output(Bool())
  val new_pc : UInt = Output(UInt(ADDR_WIDTH))
}