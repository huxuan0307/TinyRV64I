package Core

import chisel3.iotesters
import chisel3.iotesters.{Driver, PeekPokeTester}
import chisel3._

case class RegfileUnitTester(regfile: Regfile) extends PeekPokeTester(regfile) {

  for (i <- (0 to 31).toList) {
    poke(regfile.io.w.addr, i)
    poke(regfile.io.w.ena, 1)
    poke(regfile.io.w.data, i + 0xffff)
    step(1)
  }

  for (i <- 0 to 31) {
    poke(regfile.io.r1.addr, i)
    poke(regfile.io.r1.ena, 1)
    val dut = peek(regfile.io.r1.data)
    if (dut != i + 0xffff && i != 0) {
      println(f"right = ${i + 0xffff}%x, wrong = $dut%x")
    }
    step(1)
  }

}

object TestRegfile extends App{
  Driver.execute(args, () => new Regfile) {
    regfile => RegfileUnitTester(regfile)
  }
}
