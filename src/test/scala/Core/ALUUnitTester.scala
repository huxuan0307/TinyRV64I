package Core

import chisel3.iotesters
import chisel3.iotesters.{Driver, PeekPokeTester}
import chisel3._
import scala.math.Ordering.BigInt

private object TestOP {
  val ADD = 0
  val SLL = 1
  val SLT = 2
  val SLTU = 3
  val XOR = 4
  val SRL = 5
  val OR  = 6
  val AND = 7
  val SUB: Int = ADD | 0x8
  val SRA: Int = SRL | 0x8
}

case class ALUUnitTester(alu:ALU) extends PeekPokeTester (alu){
  def asUnsigned(a: Long) : Long = a & 0xffffffffL
  def scalaALU(a: Long, b: Long, op: Int) : Long = {
//    println(s"op:$op, TestOP.ADD:${TestOP.ADD}")
    val res = op match {
      case TestOP.ADD => a + b
      case TestOP.SLL => a.toInt << (b & 0x1f)
      case TestOP.SLT => (a < b).toLong
      case TestOP.SLTU => (asUnsigned(a) < asUnsigned(b)).toLong
      case TestOP.XOR => a ^ b
      case TestOP.SRL => asUnsigned(a) >> (b & 0x1f)
      case TestOP.OR  => a | b
      case TestOP.AND => a & b
      case TestOP.SUB => a - b
      case TestOP.SRA => a.toInt >>> (b & 0x1f)
      case _ => 0
    }
    val res2 = asUnsigned(res.toInt)
    res2
  }

  val input = List(0,1,2,0x7ffffffe, 0x7fffffff,
    0x80000000, 0x80000001, 0xfffffffe, 0xffffffff)

  for (op <- (0 to 15).toList) {
    for (a <- input) {
      for (b <- input){
        poke(alu.io.in.a, a)
        poke(alu.io.in.b, b)
        poke(alu.io.in.op, op)
        val ref:BigInt = asUnsigned(scalaALU(a, b, op))
        val dut = peek(alu.io.out.data)
        if (dut != ref) {
          println(f"a = $a%x, b = $b%x, op = $op%x, right = $ref%x, wrong = $dut%x")
        }
        expect(alu.io.out.data, ref)
      }
    }
  }
}

object TestALU extends App {
  iotesters.Driver.execute(args, () => new ALU) {
    alu => ALUUnitTester(alu)
  }
}
