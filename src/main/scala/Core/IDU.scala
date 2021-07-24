package Core

import chisel3._

class IDU_IO extends Bundle {
  val in: PcInstPathIO = Flipped(new PcInstPathIO)
  val out = new DecoderCtrlPathIO
}

class IDU extends Module {
  val io: IDU_IO = IO(new IDU_IO)
}
