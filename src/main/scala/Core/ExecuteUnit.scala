package Core

import Core.Bundles.RegfileWritePortIO
import chisel3._

class ExecuteInPort extends Bundle with CoreConfig with HasFuncType with HasFullOpType{
  val func_type : UInt = Input(UInt(FuncTypeWidth))
  val op_type : UInt = Input(UInt(FullOpTypeWidth))
  val op_num1 : UInt = Input(UInt(DATA_WIDTH))
  val op_num2 : UInt = Input(UInt(DATA_WIDTH))
  val w : RegfileWritePortIO = new RegfileWritePortIO
}

class ExecuteOutPort extends Bundle with CoreConfig {
  val w : RegfileWritePortIO = Flipped(new RegfileWritePortIO)
}

class EXU_IO extends Bundle{
  val in = new ExecuteInPort
  val out = new ExecuteOutPort
  val dmem : MemIO = Flipped(new MemIO)
}

class ExecuteUnit extends Module {
  val io: EXU_IO = IO(new EXU_IO)

  val alu = new ALU
  alu.io.in.a   := io.in.op_num1
  alu.io.in.b   := io.in.op_num2
  alu.io.in.op  := io.in.op_type
  io.out.w.data := alu.io.out.data
  io.out.w.ena  := io.in.w.ena
  io.out.w.addr := io.in.w.addr
}
