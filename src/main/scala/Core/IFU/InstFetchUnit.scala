package Core.IFU

import Core.Bundles.{BranchPathIO, PcInstPathIO}
import Core.Config.{CoreConfig, HasMemDataType, PcInit}
import Core.IMemIO
import chisel3._

class InstFetchUnitIO extends Bundle {
  val to_idu = new PcInstPathIO
  val imem : IMemIO = Flipped(new IMemIO)
  val branch : BranchPathIO = Flipped(new BranchPathIO)
}

class InstFetchUnit extends Module with PcInit with CoreConfig with HasMemDataType {
  val io : InstFetchUnitIO = IO(new InstFetchUnitIO)
  private val pc : UInt = RegInit(pc_init.U(ADDR_WIDTH))
  pc := Mux(io.branch.valid, io.branch.new_pc, pc + 4.U)
  io.imem.addr := pc
  io.imem.data_type := type_w
  io.imem.valid := true.B
  io.to_idu.inst := io.imem.rdata
  io.to_idu.pc := pc
}
