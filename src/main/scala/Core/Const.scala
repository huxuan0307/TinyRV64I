package Core

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._

trait BasicDefine {
  protected val Y : UInt = true.B.asUInt()
  protected val N : UInt = false.B.asUInt()
}

trait HasInstType {
  protected val InstTypeSize = 7
  protected val InstN : UInt  = 0.U
  protected val InstU : UInt  = 1.U
  protected val InstJ : UInt  = 2.U
  protected val InstB : UInt  = 3.U
  protected val InstI : UInt  = 4.U
  protected val InstS : UInt  = 5.U
  protected val InstR : UInt  = 6.U

}

trait HasFuncType {
  protected def FuncTypeSize = 8
  protected def FuncNONE      : UInt  = 0.U
  protected def FuncALU       : UInt  = 1.U
  protected def FuncBRU       : UInt  = 2.U
  protected def FuncLSU       : UInt  = 3.U
  protected def FuncMLTU      : UInt  = 4.U
  protected def FuncDIVU      : UInt  = 5.U
  protected def FuncSYSU      : UInt  = 6.U
  protected def FuncCSRU      : UInt  = 7.U
  protected def FuncTypeWidth : Width = log2Up(FuncTypeSize).W
}

trait HasFullOpType {
  protected val FullOpTypeSize = 32
  protected val FullOpTypeWidth: Width = log2Up(FullOpTypeSize).W
}

trait HasRs1Type {
  protected val Rs1TypeSize = 4
  protected def Rs1None : UInt = 0.U
  protected def Rs1Reg  : UInt = 1.U
  protected def Rs1PC   : UInt = 2.U
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