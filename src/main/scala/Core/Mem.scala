package Core

import chisel3._

class MemWritePort extends Bundle with CoreConfig {
  val ena : Bool = Input(Bool())
  val addr : UInt = Input(UInt(ADDR_WIDTH))
  val data : UInt = Input(UInt(DATA_WIDTH))
}

class MemReadPort extends Bundle with CoreConfig {
  val addr : UInt = Input(UInt(ADDR_WIDTH))
  val data : UInt = Output(UInt(DATA_WIDTH))
}

class MemIO extends Bundle {
  val w = new MemWritePort
  val r = new MemReadPort
}

abstract class Mem extends Module with CoreConfig {
  val io = new MemIO
  val data: chisel3.Mem[UInt]
}

class InstMem extends Mem {
  val data : chisel3.Mem[UInt] = Mem(IMEM_SIZE, UInt(MEM_DATA_WIDTH))
}

class DataMem extends Mem {
  val data : chisel3.Mem[UInt] = Mem(DMEM_SIZE, UInt(MEM_DATA_WIDTH))
}
