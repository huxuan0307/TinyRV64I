package Core

import chisel3._

class InstFetchUnitIO extends Bundle {
  val to_idu = new PcInstPathIO
  val imem : MemReadPort = Flipped(new MemReadPort)
}

trait PcInit {
  val pc_init = 0x80000000L
}

class InstFetchUnit extends Module with PcInit with CoreConfig {
  val io : InstFetchUnitIO = IO(new InstFetchUnitIO)
  // private 不能被实例化，需要使用Reg类型显示定义寄存器
  val pc : UInt = RegInit(pc_init.U(ADDR_WIDTH))
  pc := pc + 4.U
  io.imem.addr := pc
  io.to_idu.inst := io.imem.data
  io.to_idu.pc := pc
}
