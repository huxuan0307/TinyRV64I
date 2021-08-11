package Util

import chisel3._

object Counter {
  def counter(max: Int, ena: Bool): UInt = {
    val x = RegInit(0.U(64.W))
    when(ena) {
      x := Mux(x === max.U, 0.U, x + 1.U)
    }
    x
  }
}
