package Core

import chisel3.iotesters.PeekPokeTester
import chisel3.iotesters

case class AdderUnitTester(adder: Adder) extends PeekPokeTester(adder) {
  def asUnsigned(a: Long) : Long = if (a < 0) a + 0x100000000L else a

  def scalaSum(a: Long, b: Long): Long = {
    a + b
  }
  val input = List(0, 1, 2, 0x7ffffffeL, 0x7fffffffL, 0x80000000L, 0x80000001L, 0xfffffffeL, 0xffffffffL)

  for (a <- input) {
    for (b <- input) {
      poke(adder.io.a, a)
      poke(adder.io.b, b)

// 非常重要！！将溢出的部分舍去
      val ref:BigInt = scalaSum(a, b) & 0xffffffffL
      val dut = peek(adder.io.sum)
      if (dut != ref) {
        println(s"a = $a, b = $b, right = $ref, wrong = $dut")
      }
      expect(adder.io.sum, ref)
    }
  }
}

object TestAdder extends App{
  iotesters.Driver.execute(args, () => new Adder) {
    adder => AdderUnitTester(adder)
  }
}


