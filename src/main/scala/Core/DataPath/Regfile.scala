package Core.DataPath

import Core.Config.CoreConfig
import chisel3._

class Regfile extends CoreConfig {
  val regfile : Vec[UInt] = RegInit(VecInit(Seq.fill(REG_NUM)(0.U(XLEN.W))))

  def write(addr: UInt, data: UInt): Unit = {
    when(addr =/= 0.U) {
      regfile(addr) := data
    }
    ()
  }

  def read(addr: UInt): UInt = {
    regfile(addr)
  }
}
