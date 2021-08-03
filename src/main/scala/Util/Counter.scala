package Util

import chisel3.util.log2Up
import chisel3._

object Counter {
  def counter(max: Int, ena: Bool): UInt = {
    val x = RegInit(log2Up(max).U)
    when(ena) {
      x := Mux(x === max.U, 0.U, x + 1.U)
    }
    x
  }
}
