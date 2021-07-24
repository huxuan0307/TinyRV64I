package Core.Bundles

import chisel3._

class ALU_InputPortIO extends Bundle {
  val opType: UInt = Input(UInt(5.W))
  val op: UInt = Input(UInt(5.W))
  val a: UInt = Input(UInt(32.W))
  val b: UInt = Input(UInt(32.W))
}

class ALU_OutputPortIO extends Bundle {
  val data: UInt = Output(UInt(32.W))
}

class ALU_IO extends Bundle {
  val in = new ALU_InputPortIO
  val out = new ALU_OutputPortIO
}
