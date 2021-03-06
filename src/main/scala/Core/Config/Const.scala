package Core.Config

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._

object BasicDefine {
  val Y : UInt = true.B.asUInt()
  val N : UInt = false.B.asUInt()
  val ZERO64 : UInt = 0.U(64.W)
  val ZERO32 : UInt = 0.U(32.W)
}

trait HasInstType {
  protected def InstTypeSize = 8
  protected def InstN : UInt  = 0.U
  protected def InstU : UInt  = 1.U
  protected def InstJ : UInt  = 2.U
  protected def InstB : UInt  = 3.U
  protected def InstI : UInt  = 4.U
  protected def InstS : UInt  = 5.U
  protected def InstR : UInt  = 6.U
  protected def InstT : UInt  = 7.U // Only for debug trap
}

trait HasFuncType {
  protected def FuncTypeSize = 16
  protected def FuncNONE      : UInt  = 0.U
  protected def FuncALU       : UInt  = 1.U
  protected def FuncBRU       : UInt  = 2.U
  protected def FuncLSU       : UInt  = 3.U
  protected def FuncMLTU      : UInt  = 4.U
  protected def FuncDIVU      : UInt  = 5.U
  protected def FuncSYSU      : UInt  = 6.U
  protected def FuncCSRU      : UInt  = 7.U
  protected def FuncTrapU     : UInt  = 8.U
  protected def FuncTypeWidth : Width = log2Up(FuncTypeSize).W
}

trait HasFullOpType {
  protected def FullOpTypeSize = 32
  protected def FullOpTypeWidth: Width = log2Up(FullOpTypeSize).W
}

trait HasRs1Type {
  protected val Rs1TypeSize = 4
  protected def Rs1None : UInt = 0.U
  protected def Rs1Reg  : UInt = 1.U
  protected def Rs1PC   : UInt = 2.U
  protected def Rs1UImm : UInt = 3.U
  protected val Rs1TypeWidth: Width = log2Up(Rs1TypeSize).W
}

trait HasRs2Type {
  protected val Rs2TypeSize = 4
  protected def Rs2None : UInt = 0.U
  protected def Rs2Reg  : UInt = 1.U
  protected def Rs2Imm  : UInt = 2.U
  protected val Rs2TypeWidth: Width = log2Up(Rs2TypeSize).W
}

trait HasMemDataType {
  protected val MemDataTypeSize = 4
  val type_b :: type_h :: type_w :: type_d :: Nil = Enum(MemDataTypeSize)
  protected val MemDataTypeWidth: Width = log2Up(MemDataTypeSize).W
}

trait PcInit {
  val pc_init = 0x80000000L
  val pc_shift = 0x7f000000L

  def addrMap(src : Long) : Long = src - pc_shift

  def addrMap(src : BigInt) : BigInt = src - BigInt(pc_shift)
}
