package Core

import chisel3._
import chiseltest._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.experimental.UncheckedClockPoke._
import chiseltest.internal.VerilatorBackendAnnotation
import treadle.executable.ClockInfo
import treadle.{ClockInfoAnnotation, WriteVcdAnnotation}

import org.scalatest.FreeSpec


import java.nio.{ByteOrder, IntBuffer}
import java.io.FileInputStream
import java.nio.channels.FileChannel

class TopTester extends FreeSpec with ChiselScalatestTester with PcInit {
  def show_regfile(top: Top) : Unit = {
    for (i <- 0 to 7) {
      top.io.debug.addr.poke(i.U)
      val reg = top.io.debug.data.peek().litValue()
      println(f"rf[$i%2d] : $reg%08x")
    }
  }
  "Top test" in {
    test(new Top).withAnnotations(Seq(WriteVcdAnnotation)) {
      top =>
        val imgPath = "z:/home/huxuan/repo/am-kernels/tests/cpu-tests/single_tests/asm/shift.bin"
        val memSize = 4*1024*1024
        val mem = {
          if (imgPath=="") {
            val mem = Array.fill((pc_init / 4).toInt)(0) ++ Array(
              0xf8508093,   // addi x1,x1,-123
              0x07b08093,   // addi x1,x1,123
              //        0x0000806b,   // trap x1
              2, 1, 0
            )
            mem
          } else {
            val fc = new FileInputStream(imgPath).getChannel
            println(f"bin size = 0x${fc.size()}%08x")
            var mem = Array.fill(memSize / 4)(0)
            fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).order(ByteOrder.LITTLE_ENDIAN)
              .asIntBuffer().get(mem, (pc_init / 4).toInt, fc.size().toInt / 4)
            mem
          }
        }
        var pc = 0
        var ill_inst = 0
        var instr = 0L
        do {
          pc = top.io.imem.addr.peek().litValue().toInt
          println(f"pc: $pc%08x")
          assert((pc & 0x3) == 0)
          instr = mem(pc >> 2) & 0xffffffffL
          println(f"inst: $instr%08x")
          top.io.imem.rdata.poke(instr.U(32.W))
          top.clock.step()
          show_regfile(top)
          ill_inst = top.io.ill_inst.peek().litValue().toInt
          // 不能peek内部的模块
//          val alu_a = top.data_path.io.to_exu.op_num1.peek().litValue().toInt
//          val alu_b = top.data_path.io.to_exu.op_num2.peek().litValue().toInt
//          println(f"alu a: $alu_a%08x, b: $alu_b%08x")
        } while (ill_inst == 0)
        println(f"ill_inst: $instr%08x as pc: $pc%08x")

        fork {

        }.join()
    }
  }
}

