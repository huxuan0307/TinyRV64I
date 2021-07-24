package Core

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util.log2Up

trait CoreConfig {
  def INST_WIDTH: Width = 32.W
  def ADDR_WIDTH: Width = 32.W
  def DATA_WIDTH: Width = 32.W
  def REG_NUM: Int = 32
  def REG_ADDR_WIDTH: Width = log2Up(REG_NUM).W
}
