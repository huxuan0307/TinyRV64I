package Core.WBU

import Core.Bundles.RegfileWritePortIO
import Core.Config.CoreConfig
import Core.EXU.ExecuteOutPort
import chisel3._

class WriteBackUnitIO extends Bundle with CoreConfig {
  val in : ExecuteOutPort = Flipped(new ExecuteOutPort)
  val out : RegfileWritePortIO = Flipped(new RegfileWritePortIO)
}

class WriteBackUnit extends Module {
  val io : WriteBackUnitIO = IO(new WriteBackUnitIO)
  io.out <> io.in.w
}
