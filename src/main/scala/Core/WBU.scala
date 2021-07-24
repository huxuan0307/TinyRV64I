package Core

import Core.Bundles.RegfileWritePortIO
import chisel3._

class WriteBackOutPort extends Bundle with CoreConfig {

}

class WriteBackUnitIO extends Bundle with CoreConfig {
  val in : ExecuteOutPort = Flipped(new ExecuteOutPort)
  val out = new RegfileWritePortIO
}

class WBU extends Module {
  val io = new WriteBackUnitIO
  io.out <> io.in.w
}
