package Core

import Core.Bundles.RegfileIO
import chisel3._

class Regfile extends Module {
  val io: RegfileIO = IO(new RegfileIO)
  val regfile: Mem[UInt] = Mem(32, UInt(32.W))
  //  def read(addr: UInt): UInt = Mux(addr === 0.U, 0.U, regfile(addr))
  //  def write(addr: UInt, data: UInt) = {
  //    when(addr =/= 0.U) {
  //      regfile(addr) := data
  //    }
  //    ()
  //  }
  when(io.w.ena === 1.U(1.W)) {
    when(io.w.addr =/= 0.U(1.W)){
      regfile(io.w.addr) := io.w.data
    }
    io.r1.data := 0.U
    io.r2.data := 0.U
  } otherwise {
    io.r1.data := Mux(io.r1.ena.asBool, regfile(io.r1.addr), 0.U)
    io.r2.data := Mux(io.r2.ena.asBool, regfile(io.r2.addr), 0.U)
  }
}
