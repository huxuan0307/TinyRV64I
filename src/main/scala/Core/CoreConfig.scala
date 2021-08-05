package Core

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util.log2Up

trait CoreConfig {
  def XLEN : Int = 64
  def INST_WIDTH: Width = 32.W
  def ADDR_WIDTH: Width = XLEN.W
  def DATA_WIDTH: Width = XLEN.W
  // 按Byte编址
  def MEM_DATA_WIDTH : Width = 8.W
  def IMEM_SIZE = 256
  def DMEM_SIZE : Int = 256*1024

  def REG_NUM: Int = 32
  def REG_ADDR_WIDTH: Width = log2Up(REG_NUM).W

  // CSR
  // Machine
  def MXL = 2
  def MXLEN : Int = 2^(MXL+4)
  def ISAEXT : UInt = ISAExt('L')
  def CSR_ADDR_WIDTH: Width = 12.W
  def VendorID = 0
  def ArchitectureID = 0
  def ImplementationID = 0
  def HardwareThreadID = 0
}

object CoreConfig extends CoreConfig