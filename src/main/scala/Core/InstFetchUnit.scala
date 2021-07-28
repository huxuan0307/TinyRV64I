package Core

import chisel3._

class InstFetchUnitIO extends Bundle {
  val to_idu = new PcInstPathIO
  val imem : IMemIO = Flipped(new IMemIO)
  val branch : BranchPathIO = Flipped(new BranchPathIO)
}

trait PcInit {
  val pc_init   = 0x80000000L
  val pc_shift  = 0x7f000000L
//  val pc_init   = 0x01000000L
//  val pc_shift  = 0x00000000L
  def addrMap(src: Long) : Long = src - pc_shift
  def addrMap(src: BigInt): BigInt = src - BigInt(pc_shift)
}

class InstFetchUnit extends Module with PcInit with CoreConfig with HasMemDataType {
  val io : InstFetchUnitIO = IO(new InstFetchUnitIO)
  // private 不能被实例化，需要使用Reg类型显示定义寄存器
  val pc : UInt = RegInit(pc_init.U(ADDR_WIDTH))
  pc := Mux(io.branch.valid, io.branch.new_pc, pc + 4.U)
  io.imem.addr := pc
  io.imem.data_type := type_w
  io.imem.valid := true.B
  io.to_idu.inst := io.imem.rdata
  io.to_idu.pc := pc
}
