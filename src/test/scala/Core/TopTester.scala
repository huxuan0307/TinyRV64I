package Core

import chisel3.iotesters._

import java.nio.{IntBuffer, ByteOrder}
import java.io.FileInputStream
import java.nio.channels.FileChannel

case class TopTester(top: Top, imgPath: String) extends PeekPokeTester(top) with PcInit {
  val memSize = 4*1024*1024
  val mem = {
    if (imgPath=="") {
      Array.fill(pc_init / 4)(0) ++ Array(
        0x07b08093,   // addi x1,x1,123
        0xf8508093,   // addi x1,x1,-123
//        0x0000806b,   // trap x1
        0, 0, 0, 0
      )
    } else {
      val fc = new FileInputStream(imgPath).getChannel()
      println(f"bin size = 0x${fc.size()}%08x")
      var mem = Array.fill(memSize / 4)(0)
      fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).order(ByteOrder.LITTLE_ENDIAN)
        .asIntBuffer().get(mem, pc_init / 4, fc.size() / 4)
      mem
    }
  }
  var pc = 0
  var ill_inst = 0
  var instr = 0

  do {
    pc = peek(top.io.imem.addr).toInt
    assert((pc & 0x3) == 0)
    instr = mem(pc >> 2)
    poke(top.io.imem.data, instr)

    step(1)
    ill_inst = peek(top.io.ill_inst).toInt

  } while (ill_inst == 0)
  println(f"ill_inst: $ill_inst as pc: $pc%8x")
  for (i <- 0 to 31) {
    poke(top.io.debug.addr, i)
    val reg = peek(top.io.debug.data)
    println(f"rf[$i%2d] : $reg%8x")
  }
//  expect(noop.io.trap, 0)

}

object TopMain extends App {
  Driver.execute(args, () => new Top) {
    top => TopTester(top, "")
  }
}