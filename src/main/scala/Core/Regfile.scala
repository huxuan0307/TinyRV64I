package Core

import Core.Bundles.RegfileIO
import chisel3._

class RegfileImpl extends CoreConfig {
  val regfile: chisel3.Mem[UInt] = Mem(REG_NUM, UInt(DATA_WIDTH))

  def write(addr: UInt, data: UInt): Unit = {
    when(addr =/= 0.U) {
      regfile(addr) := data
    }
    ()
  }

  def read(addr: UInt): UInt = {
    regfile(addr)
  }
}

class Regfile extends Module with CoreConfig {
  val io: RegfileIO = IO(new RegfileIO)
  val regfile = new RegfileImpl

  when(io.w.ena === 1.U(1.W)) {
    regfile.write(io.w.addr, io.w.data)
  }
  io.r1.data := regfile.read(io.r1.addr)
  io.r2.data := regfile.read(io.r2.addr)
  io.debug.data := regfile.read(io.debug.addr)
}
