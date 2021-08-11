package Core.DataPath

import Core.Bundles.{RegfileDebugIO, RegfileWritePortIO}
import Core.Config.{CoreConfig, HasRs1Type, HasRs2Type}
import Core.DiffTest.DiffTestIO
import Core.EXU.ExecuteInPort
import Core.IDU.InstDecodeUnitOutPort
import chisel3._
import chisel3.util._
import difftest.DifftestArchIntRegState

class DataPathUnitIO extends Bundle {
  val from_idu : InstDecodeUnitOutPort = Flipped(new InstDecodeUnitOutPort)
  val from_wbu = new RegfileWritePortIO
  val to_exu : ExecuteInPort = Flipped(new ExecuteInPort)
  val debug = new RegfileDebugIO
  val diffTest = new DiffTestIO
}

class DataPathUnit extends Module with HasRs1Type with HasRs2Type with CoreConfig {
  val io : DataPathUnitIO = IO(new DataPathUnitIO)
  val rf = new Regfile
  private val rs1Data = rf.read(io.from_idu.rs1Addr)
  private val rs2Data = rf.read(io.from_idu.rs2Addr)

  when(io.from_wbu.ena){
    rf.write(io.from_wbu.addr, io.from_wbu.data)
  }

  io.to_exu.op_num1   := MuxLookup(io.from_idu.rs1Type, 0.U, List(
    Rs1Reg -> rs1Data,
    Rs1PC  -> io.from_idu.pc,
    Rs1UImm-> io.from_idu.uimm_ext
  ))
  io.to_exu.op_num2   := MuxLookup(io.from_idu.rs2Type, 0.U, List(
    Rs2Reg -> rs2Data,
    Rs2Imm -> io.from_idu.imm_ext
  ))
  io.to_exu.op_type   := io.from_idu.opType
  io.to_exu.func_type := io.from_idu.funcType
  io.to_exu.w.ena     := io.from_idu.rdEna
  io.to_exu.w.addr    := io.from_idu.rdAddr
  io.to_exu.pc        := io.from_idu.pc
  // 借用wdata放S指令的写内存数据，立即数在from_idu.rs2Data中，
  // B指令也需要传递立即数和一个寄存器的值，于是使用wdata传递寄存器的值，
  // 用op_num2传递立即数，与S指令路径相同，可以减少一个Mux
  io.to_exu.w.data    := rs2Data
  io.to_exu.is_word_type := io.from_idu.is_word_type
  io.debug            <> DontCare
  io.diffTest.commit  := DontCare
  io.diffTest.trap.valid:= DontCare
  io.diffTest.trap.code := rf.read(10.U)(2,0)
  io.diffTest.wreg    <> io.from_wbu
  // zipWithIndex 按序号压缩，连续的情况下，使用foreach遍历，不连续使用map

  private val regfileCommit = Module(new DifftestArchIntRegState)
  regfileCommit.io.clock := clock
  regfileCommit.io.coreid := 0.U
  regfileCommit.io.gpr.zipWithIndex.foreach{ case (r, i) => r:= rf.read(i.U)}

}
