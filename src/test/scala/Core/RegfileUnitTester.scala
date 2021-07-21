package Core

import chisel3.iotesters
import chisel3.iotesters.{Driver, PeekPokeTester}

case class RegfileUnitTester(regfile: Regfile) extends PeekPokeTester(regfile) {
  for (i <- (0 to 31).toList) {
    poke(regfile.io.w.addr, i)
    poke(regfile.io.w.ena, 1)
    poke(regfile.io.w.data, i + 0xffff)
    poke(regfile.io.r1.addr, 0)
    poke(regfile.io.r1.ena, 0)
    poke(regfile.io.r2.addr, 0)
    poke(regfile.io.r2.ena, 0)

    step(1)
  }
  for (i <- 0 to 31) {

    val dut = peek(regfile.regfile(i))
    if (dut != i + 0xffff) {
      println(f"right = ${i + 0xffff}%x, wrong = $dut%x")
    }
    expect(regfile.regfile(i), i + 0xffff)
  }
  for (i <- 0 to 31) {
    poke(regfile.io.w.addr, i)
    poke(regfile.io.w.ena, 0)
    poke(regfile.io.w.data, i + 0xfffff)
    poke(regfile.io.r1.addr, i)
    poke(regfile.io.r1.ena, 1)
    poke(regfile.io.r2.addr, i)
    poke(regfile.io.r2.ena, 1)
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
