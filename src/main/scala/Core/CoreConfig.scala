package Core

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util.{log2Up, Cat}
import scala.math._

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
  def MXLEN : Int = pow(2, MXL+4).toInt
  def SXLEN : Int = MXLEN
  def UXLEN : Int = SXLEN
  class ISAExt(string: String) {
    val value : Int = toInt(string)
    def support(ch: Char) : Boolean = {
      (value & ch.toInt) != 0
    }
    private def toInt(string: String) : Int = {
      var value = 0
      for (ch <- string) {
        require(ch.isLetter)
        value |= (ch.toUpper - 'A')
      }
      value
    }
    def toInt : Int = value
  }
  def ISAEXT = new ISAExt("I")
  def CSR_ADDR_LEN = 12
  def CSR_ADDR_W: Width = CSR_ADDR_LEN.W
  def VendorID = 0
  def ArchitectureID = 0
  def ImplementationID = 0
  def HardwareThreadID = 0
  def MISA : BigInt = {
    BigInt(MXL) << (MXLEN - 2) | BigInt(ISAEXT.toInt)
  }
}

object CoreConfig extends CoreConfig