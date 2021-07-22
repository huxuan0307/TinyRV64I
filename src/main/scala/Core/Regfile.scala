package Core

import Core.Bundles.RegfileIO
import chisel3._

trait HasRegfileParameter {
  val resetVector = 0
  val regWidth = 32
  val regNum = 32
}

class RegfileImpl extends HasRegfileParameter {
  val regfile: Mem[UInt] = Mem(regNum, UInt(regWidth.W))

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

class Regfile extends Module with HasRegfileParameter {
  val io: RegfileIO = IO(new RegfileIO)
  val regfile = new RegfileImpl

  when(io.w.ena === 1.U(1.W)) {
    regfile.write(io.w.addr, io.w.data)
  }
  io.r1.data := Mux(io.r1.ena.asBool, regfile.read(io.r1.addr), 0.U)
  io.r2.data := Mux(io.r2.ena.asBool, regfile.read(io.r2.addr), 0.U)
}
