package Sim

import chisel3._
import chisel3.util._
import Core._
import difftest._
import Core.Config.BasicDefine._
import Chisel.unless
import Util.Counter
import Common.Const.ULONG_MAX
import Core.Config.{CoreConfig, HasMemDataType}

class SimTopIO extends Bundle {
  val logCtrl = new LogCtrlIO
  val perfInfo = new PerfInfoIO
  val uart = new UARTIO
}

class SimTop extends Module with CoreConfig with HasMemDataType {
  val io : SimTopIO = IO(new SimTopIO())

  val rvcore : Top = Module(new Top)

  private val mem = Module(new MemHelper)
  mem.io.dmem <> rvcore.io.dmem
  mem.io.imem <> rvcore.io.imem
  io.uart     <> mem.io.uart
  rvcore.io.debug <> DontCare
  private val pc = mem.io.pc
  private val inst = mem.io.inst
  private val inst_valid = mem.io.inst_valid

  private val commit_pc         = RegNext(pc)
  private val commit_inst_valid = RegNext(inst_valid)
  private val commit_inst       = RegNext(inst)
  private val commit_wen        = RegNext(rvcore.io.diffTest.wreg.ena)
  private val commit_wdata      = RegNext(rvcore.io.diffTest.wreg.data)
  private val commit_wdest      = RegNext(rvcore.io.diffTest.wreg.addr)
  private val commit_trap_valid = rvcore.io.diffTest.trap.valid
  private val commit_trap_code  = rvcore.io.diffTest.trap.code

  private val instrCommit = Module(new DifftestInstrCommit)
  instrCommit.io.clock := clock
  instrCommit.io.coreid := 0.U
  instrCommit.io.index := 0.U
  instrCommit.io.skip := mem.io.skip
  instrCommit.io.isRVC := false.B
  instrCommit.io.scFailed := false.B

  instrCommit.io.valid := commit_inst_valid
  instrCommit.io.pc := commit_pc
  instrCommit.io.instr := commit_inst
  instrCommit.io.wen := commit_wen
  instrCommit.io.wdata := commit_wdata
  instrCommit.io.wdest := commit_wdest

  private val commit_cycle_cnt = Counter.counter(0xfffffff, clock.asBool())
  private val commit_inst_cnt = Counter.counter(0xfffffff, inst_valid)
  private val trapEvent = Module(new DifftestTrapEvent)
  trapEvent.io.clock  := clock
  trapEvent.io.coreid := 0.U
  trapEvent.io.valid  := commit_trap_valid
  trapEvent.io.code   := commit_trap_code
  trapEvent.io.pc     := commit_pc
  trapEvent.io.cycleCnt := commit_cycle_cnt
  trapEvent.io.instrCnt := commit_inst_cnt
}
