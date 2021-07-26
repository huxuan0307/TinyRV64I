package Core

import Core.Bundles.RegfileWritePortIO
import chisel3._

class WriteBackOutPort extends Bundle with CoreConfig {

}

class WriteBackUnitIO extends Bundle with CoreConfig {
  val in : ExecuteOutPort = Flipped(new ExecuteOutPort)
  val out = Flipped(new RegfileWritePortIO)
}

class WriteBackUnit extends Module {
  val io = IO(new WriteBackUnitIO)
  io.out <> io.in.w
}
