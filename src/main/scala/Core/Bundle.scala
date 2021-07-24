package Core

import chisel3._
import chisel3.util._

class DataPathIO extends Bundle with CoreConfig {
  val rs1: UInt = Output(UInt(DATA_WIDTH))
  val rs2: UInt = Output(UInt(DATA_WIDTH))
  val rd : UInt = Output(UInt(DATA_WIDTH))
}

trait DecodeConst extends HasRs1Type with HasRs2Type with HasFuncType with HasFullOpType

class DecoderCtrlPathIO extends Bundle with DecodeConst with CoreConfig {
  val rs1Type : UInt  = Output(UInt(Rs1TypeWidth))
  val rs2Type : UInt  = Output(UInt(Rs2TypeWidth))
  val funcType: UInt  = Output(UInt(FuncTypeWidth))
  val opType  : UInt  = Output(UInt(FullOpTypeWidth))
  val rs1Ena  : UInt  = Output(UInt(1.W))
  val rs1Reg  : UInt  = Output(UInt(REG_ADDR_WIDTH))
  val rs2Ena  : UInt  = Output(UInt(1.W))
  val rs2Reg  : UInt  = Output(UInt(REG_ADDR_WIDTH))
  val rdEna   : UInt  = Output(UInt(1.W))
  val rdReg   : UInt  = Output(UInt(REG_ADDR_WIDTH))
}

class PcInstPathIO extends Bundle with CoreConfig {
  val pc:   UInt = Output(UInt(32.W))
  val inst: UInt = Output(UInt(32.W))
}


