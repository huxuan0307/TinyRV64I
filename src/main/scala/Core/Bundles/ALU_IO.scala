package Core.Bundles

import Core.{CoreConfig, HasFullOpType}
import chisel3._

class ALU_InputPortIO extends Bundle with CoreConfig with HasFullOpType{
  val op: UInt = Input(UInt(FullOpTypeWidth))
  val a: UInt = Input(UInt(DATA_WIDTH))
  val b: UInt = Input(UInt(DATA_WIDTH))
}

class ALU_OutputPortIO extends Bundle {
  val data: UInt = Output(UInt(32.W))
}

class ALU_IO extends Bundle {
  val in = new ALU_InputPortIO
  val out = new ALU_OutputPortIO
}