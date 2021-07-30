package Core

import Core.Bundles.RegfileWritePortIO
import Core.EXU.ExecuteOutPort
import chisel3._

class WriteBackOutPort extends Bundle with CoreConfig {

}

class WriteBackUnitIO extends Bundle with CoreConfig {
  val in : ExecuteOutPort = Flipped(new ExecuteOutPort)
  val out : RegfileWritePortIO = Flipped(new RegfileWritePortIO)
}

class WriteBackUnit extends Module {
  val io : WriteBackUnitIO = IO(new WriteBackUnitIO)
  io.out <> io.in.w
}
