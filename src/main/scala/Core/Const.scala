package Core

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._

trait HasInstType {
  val InstTypeSize = 7
  val InstN : UInt  = 0.U
  val InstU : UInt  = 1.U
  val InstJ : UInt  = 2.U
  val InstB : UInt  = 3.U
  val InstI : UInt  = 4.U
  val InstS : UInt  = 5.U
  val InstR : UInt  = 6.U
}

trait HasFuncType {
  val FuncTypeSize = 8
  val FuncALU       : UInt  = 0.U
  val FuncBRU       : UInt  = 1.U
  val FuncLSU       : UInt  = 2.U
  val FuncMLTU      : UInt  = 3.U
  val FuncDIVU      : UInt  = 4.U
  val FuncSYSU      : UInt  = 5.U
  val FuncCSRU      : UInt  = 6.U
  val FuncTypeWidth : Width = log2Up(FuncTypeSize).W
}

trait HasFullOpType {
  val FullOpTypeSize = 32
  val FullOpTypeWidth: Width = log2Up(FullOpTypeSize).W
}

trait HasRs1Type {
  val Rs1TypeSize = 2
  def Rs1Reg: UInt = 0.U
  def Rs1PC: UInt = 1.U
  val Rs1TypeWidth: Width = log2Up(Rs1TypeSize).W
}

trait HasRs2Type {
  val Rs2TypeSize = 2
  def Rs2Reg: UInt = 0.U
  def Rs2Imm: UInt = 1.U
  val Rs2TypeWidth: Width = log2Up(Rs2TypeSize).W
}
