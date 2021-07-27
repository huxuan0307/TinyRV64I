package Core

import chisel3._
import chisel3.util._

trait MemReadPort extends CoreConfig {
  val rdata : UInt = Output(UInt(DATA_WIDTH))
}

class MemDebugPort extends Bundle with CoreConfig {
  val addr : UInt = Input(UInt(ADDR_WIDTH))
  val data : UInt = Output(UInt(DATA_WIDTH))
}

trait MemWritePortForReadOnly extends CoreConfig {
  val addr : UInt = Input(UInt(ADDR_WIDTH))
  val data_type : UInt = Input(UInt(2.W))
}

trait MemWritePortForReadWrite extends MemWritePortForReadOnly {
  val wena : Bool = Input(Bool())
  val wdata : UInt = Input(UInt(DATA_WIDTH))
}

class IMemIO extends Bundle with MemWritePortForReadOnly with MemReadPort

class DMemIO extends Bundle with MemWritePortForReadWrite with MemReadPort {
  val debug = new MemDebugPort
}

abstract class Mem extends Module with CoreConfig {
  val io: Bundle
  val data: chisel3.Mem[UInt]
}

class InstMem extends Mem with HasMemDataType {
  val io : IMemIO = IO(new IMemIO)
  val data : chisel3.Mem[UInt] = Mem(IMEM_SIZE, UInt(MEM_DATA_WIDTH))
  protected val addr : UInt = Wire(UInt(8.W))
  addr:=io.addr(7,0)
  io.rdata := Cat(
    data(Cat(addr(7,2), 0.U(2.W))),
    data(Cat(addr(7,2), 1.U(2.W))),
    data(Cat(addr(7,2), 2.U(2.W))),
    data(Cat(addr(7,2), 3.U(2.W)))
  )
}

class DataMem extends Mem {
  val io : DMemIO = IO(new DMemIO)
  val data : chisel3.Mem[UInt] = Mem(DMEM_SIZE, UInt(MEM_DATA_WIDTH))
  private val addr = Wire(UInt(8.W))
  addr := io.addr(7,0)
//  io.rdata := 0.U
  io.rdata := Cat(
    data(addr + 0.U(2.W)),
    data(addr + 1.U(2.W)),
    data(addr + 2.U(2.W)),
    data(addr + 3.U(2.W))
  )
  when(io.wena) {
    data(addr + 0.U(2.W)) := io.wdata(31, 24)
    data(addr + 1.U(2.W)) := io.wdata(23, 16)
    data(addr + 2.U(2.W)) := io.wdata(15,  8)
    data(addr + 3.U(2.W)) := io.wdata( 7,  0)
  }
  private val debug_addr = Wire(UInt(8.W))
  debug_addr := io.debug.addr(7,0)
  io.debug.data := Cat(
    data(debug_addr),
    data(debug_addr + 1.U(2.W)),
    data(debug_addr + 2.U(2.W)),
    data(debug_addr + 3.U(2.W))
  )

}
