package Core

import chisel3._

class InstFetchUnitIO extends Bundle {
  val to_idu = new PcInstPathIO
  val imem : MemReadPort = Flipped(new MemReadPort)
}

trait PcInit {
  val pc_init = 0x80000000
}

class InstFetchUnit extends Module with PcInit with CoreConfig {
  val io = new InstFetchUnitIO
  private val pc = RegInit(pc_init.U(ADDR_WIDTH))
  pc := pc + 4.U(ADDR_WIDTH)
  io.imem.addr := pc
  io.to_idu.inst := io.imem.data
  io.to_idu.pc := pc
}
