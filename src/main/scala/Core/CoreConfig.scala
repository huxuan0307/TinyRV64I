package Core

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util.log2Up

trait CoreConfig {
  protected def XLEN  = 32
  protected def INST_WIDTH: Width = 32.W
  protected def ADDR_WIDTH: Width = 32.W
  protected def DATA_WIDTH: Width = XLEN.W
  // 按Byte编址
  protected def MEM_DATA_WIDTH : Width = 8.W
  protected def IMEM_SIZE = 256
  protected def DMEM_SIZE = 256

  protected def REG_NUM: Int = 32
  protected def REG_ADDR_WIDTH: Width = log2Up(REG_NUM).W
}
