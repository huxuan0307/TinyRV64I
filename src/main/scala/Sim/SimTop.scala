package Sim

import chisel3._
import Core._
import difftest._

class SimTopIO extends Bundle {
  val logCtrl = new LogCtrlIO
  val perfInfo = new PerfInfoIO
  val uart = new UARTIO
}

class SimTop extends Module {
  val io : SimTopIO = IO(new SimTopIO())

  val rvcore : Top = Module(new Top)

  rvcore.io.dmem.rdata := 0.U
  rvcore.io.dmem.debug.data := 0.U
  rvcore.io.imem.rdata := 0.U
  rvcore.io.debug.addr := 0.U

  io.uart.in.valid := false.B
  io.uart.out.valid := false.B
  io.uart.out.ch  := 0.U

}
