package Core

import chisel3._
import chisel3.util._

trait DataPathIO extends Bundle with CoreConfig {
  val rs1_data: UInt = Output(UInt(DATA_WIDTH))
  val rs2_data: UInt = Output(UInt(DATA_WIDTH))
  val rd_data : UInt = Output(UInt(DATA_WIDTH))
}

trait DecodeConst extends HasRs1Type with HasRs2Type with HasFuncType with HasFullOpType

trait CtrlPathIO extends Bundle with DecodeConst with CoreConfig {
  val rs1Type : UInt  = Output(UInt(Rs1TypeWidth))
  val rs2Type : UInt  = Output(UInt(Rs2TypeWidth))
  val funcType: UInt  = Output(UInt(FuncTypeWidth))
  val opType  : UInt  = Output(UInt(FullOpTypeWidth))
  val rs1Addr  : UInt  = Output(UInt(REG_ADDR_WIDTH))
  val rs2Addr  : UInt  = Output(UInt(REG_ADDR_WIDTH))
  val rdEna   : UInt  = Output(UInt(1.W))
  val rdAddr   : UInt  = Output(UInt(REG_ADDR_WIDTH))
}

trait DecoderCtrlPathIO extends Bundle with CtrlPathIO

trait DecoderDataPathIO extends Bundle with DataPathIO with DecodeConst with CoreConfig

class InstDecodeUnitOutPort extends DecoderCtrlPathIO with DecoderDataPathIO {
  val pc : UInt = Output(UInt(ADDR_WIDTH))
}

class PcInstPathIO extends Bundle with CoreConfig {
  val pc:   UInt = Output(UInt(ADDR_WIDTH))
  val inst: UInt = Output(UInt(INST_WIDTH))
}

class BranchPathIO extends Bundle with CoreConfig {
  val valid : Bool = Output(Bool())
  val new_pc : UInt = Output(UInt(ADDR_WIDTH))
}