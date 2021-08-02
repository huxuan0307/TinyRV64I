package Core.EXU

import chisel3._
import chisel3.util._
import Core.BasicDefine._
import Core.{CoreConfig, HasFullOpType}

object TrapOp {
  def NONE : UInt = "b000".U  // 啥事不做
}

class TrapUnitInputPort extends Bundle with CoreConfig with HasFullOpType{
  val ena : Bool = Input(Bool())
  val op : UInt = Input(UInt(FullOpTypeWidth))
  val op_num1 : UInt = Input(UInt(DATA_WIDTH))
  val op_num2 : UInt = Input(UInt(DATA_WIDTH))
}

class TrapUnitOutputPort extends Bundle with CoreConfig {
  val data : UInt = Output(UInt(DATA_WIDTH))
}

class TrapUnitIO extends Bundle with CoreConfig {
  val in = new TrapUnitInputPort
  val out = new TrapUnitOutputPort
}

class TrapUnit extends Module with CoreConfig {
  import TrapOp._
  val io : TrapUnitIO = IO(new TrapUnitIO)
  io.out.data := MuxLookup(io.in.op, ZERO64, Array(
    NONE -> ZERO64
  ))
}
