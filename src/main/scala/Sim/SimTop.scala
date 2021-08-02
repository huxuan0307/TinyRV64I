package Sim

import chisel3._
import chisel3.util._
import Core._
import difftest._
import BasicDefine._

object Counter {
  def wrapAround(n: UInt, max: UInt) : UInt =
    Mux(n > max, 0.U, n)

  def counter(max: UInt, en: Bool, amt: UInt): UInt = {
    val x = RegInit(0.U(max.getWidth.W))
    when (en) { x := wrapAround(x + amt, max) }
    x
  }
}

class SimTopIO extends Bundle {
  val logCtrl = new LogCtrlIO
  val perfInfo = new PerfInfoIO
  val uart = new UARTIO
}

class SimTop extends Module with CoreConfig with HasMemDataType {
  def ULONG_MAX : BigInt = BigInt(Long.MaxValue) * 2 + 1


  val io : SimTopIO = IO(new SimTopIO())
  io.uart.in.valid := false.B
  io.uart.out.valid := false.B
  io.uart.out.ch  := 0.U
  val rvcore : Top = Module(new Top)

  val data_ram : RAMHelper = Module(new RAMHelper)
  val inst_rom : ROMHelper = Module(new ROMHelper)

  // imem
  private val pc = rvcore.io.imem.addr
  private val inst = Mux(pc(2), inst_rom.io.rdata(63,32), inst_rom.io.rdata(31,0))
  private val inst_valid = inst =/= ZERO32
  inst_rom.io.clk := clock
  inst_rom.io.en  := (!reset.asBool()) & true.B
  inst_rom.io.rIdx := (pc - 0x80000000L.U(64.W))(63,3)
  rvcore.io.imem.rdata := inst
  // dmem
  private val wdata = rvcore.io.dmem.wdata
  private val offset = rvcore.io.dmem.addr(2,0)
  private val mask = MuxLookup(rvcore.io.dmem.data_type, BigInt("ffffffffffffffff", 16).U, Array(
    type_b -> BigInt(0xff).U(DATA_WIDTH),
    type_h -> BigInt(0xffff).U(DATA_WIDTH),
    type_w -> BigInt(0xffffffffL).U(DATA_WIDTH),
    type_d -> ULONG_MAX.U
  ))
  private val wdata_align = wdata << (offset * 8.U)
  private val mask_align = mask << (offset * 8.U)
  private val rdata_align = data_ram.io.rdata >> (offset * 8.U)
  data_ram.io.clk  := clock
  data_ram.io.en   := rvcore.io.dmem.valid
  data_ram.io.rIdx := (rvcore.io.dmem.addr - 0x80000000L.U) >> 3.U
  rvcore.io.dmem.rdata := rdata_align

  //  data_ram.io.wIdx := (rvcore.io.dmem.addr - BigInt("80000000", 16).U) >> 3.U
  data_ram.io.wIdx := (rvcore.io.dmem.addr - 0x80000000L.U) >> 3.U
  data_ram.io.wdata := wdata_align
  // todo: check it
  data_ram.io.wmask := mask_align
  data_ram.io.wen   := rvcore.io.dmem.wena

  rvcore.io.dmem.debug.data := 0.U

  rvcore.io.debug.addr := 0.U
  val traped = WireInit(false.B)
  when(rvcore.io.diffTest.trap.valid) {
    traped := true.B
  }

  private val commit_pc         = RegNext(pc)
  private val commit_inst_valid = RegNext(inst_valid)
  private val commit_inst       = RegNext(inst)
  private val commit_wen        = RegNext(rvcore.io.diffTest.wreg.ena)
  private val commit_wdata      = RegNext(rvcore.io.diffTest.wreg.data)
  private val commit_wdest      = RegNext(rvcore.io.diffTest.wreg.addr)
  private val commit_gpr        = rvcore.io.diffTest.reg
  private val commit_trap_valid = rvcore.io.diffTest.trap.valid
  private val commit_trap_code  = rvcore.io.diffTest.reg(10)(2,0)

  private val instrCommit = Module(new DifftestInstrCommit)
  instrCommit.io.clock := clock
  instrCommit.io.coreid := 0.U
  instrCommit.io.index := 0.U
  instrCommit.io.skip := false.B
  instrCommit.io.isRVC := false.B
  instrCommit.io.scFailed := false.B

  instrCommit.io.valid := commit_inst_valid
  instrCommit.io.pc := commit_pc
  instrCommit.io.instr := commit_inst
  instrCommit.io.wen := commit_wen
  instrCommit.io.wdata := commit_wdata
  instrCommit.io.wdest := commit_wdest

  private val regfileCommit = Module(new DifftestArchIntRegState)
  regfileCommit.io.clock := clock
  regfileCommit.io.coreid := 0.U
  regfileCommit.io.gpr := commit_gpr

  private val csrCommit = Module(new DifftestCSRState)
  csrCommit.io.clock          := clock
  csrCommit.io.priviledgeMode := 0.U
  csrCommit.io.mstatus        := 0.U
  csrCommit.io.sstatus        := 0.U
  csrCommit.io.mepc           := 0.U
  csrCommit.io.sepc           := 0.U
  csrCommit.io.mtval          := 0.U
  csrCommit.io.stval          := 0.U
  csrCommit.io.mtvec          := 0.U
  csrCommit.io.stvec          := 0.U
  csrCommit.io.mcause         := 0.U
  csrCommit.io.scause         := 0.U
  csrCommit.io.satp           := 0.U
  csrCommit.io.mip            := 0.U
  csrCommit.io.mie            := 0.U
  csrCommit.io.mscratch       := 0.U
  csrCommit.io.sscratch       := 0.U
  csrCommit.io.mideleg        := 0.U
  csrCommit.io.medeleg        := 0.U

  private val cnt_en = Wire(Bool())
  cnt_en := withClock(clock){~reset.asBool()}
  val commit_cycle_cnt : UInt = Counter.counter(Int.MaxValue.U, cnt_en, 1.U)

  private val commit_inst_cnt = Counter.counter(Int.MaxValue.U, cnt_en, inst_valid)
  private val trapEvent = Module(new DifftestTrapEvent)
  trapEvent.io.clock  := clock
  trapEvent.io.coreid := 0.U
  trapEvent.io.valid  := commit_trap_valid
  trapEvent.io.code   := commit_trap_code
  trapEvent.io.pc     := commit_pc
  trapEvent.io.cycleCnt := 0.U
  trapEvent.io.instrCnt := 0.U

//  private val storeEvent = Module(new DifftestStoreEvent)
//  storeEvent.io.clock := clock
//  storeEvent.io.coreid := 0.U
//  storeEvent.io.valid := rvcore.io.dmem.valid & ~rvcore.io.dmem.wena
//  storeEvent.io.storeAddr := rvcore.io.dmem.addr
//  storeEvent.io.storeData := wdata_align
//  storeEvent.io.storeMask := mask_align
//
//  private val loadEvent = Module(new DifftestLoadEvent)
//  loadEvent.io.clock      :=  clock
//  loadEvent.io.coreid     :=  0.U
//  loadEvent.io.valid      :=  commit_inst_valid & rvcore.io.dmem.wena
//  loadEvent.io.paddr      :=  rvcore.io.dmem.addr
//  loadEvent.io.opType     :=  commit_inst(14, 12)
//  loadEvent.io.fuType     :=  0x0.U

}

class ROMHelperIO extends Bundle with CoreConfig {
  val clk : Clock = Input(Clock())
  val en : Bool = Input(Bool())
  val rIdx : UInt = Input(UInt(ADDR_WIDTH))
  val rdata : UInt = Output(UInt(DATA_WIDTH))
}

class RAMHelperIO extends Bundle with CoreConfig {
  val clk : Clock = Input(Clock())
  val en : Bool = Input(Bool())
  val rIdx : UInt = Input(UInt(ADDR_WIDTH))
  val rdata : UInt = Output(UInt(DATA_WIDTH))
  val wIdx : UInt = Input(UInt(ADDR_WIDTH))
  val wdata : UInt = Input(UInt(DATA_WIDTH))
  val wmask : UInt = Input(UInt(ADDR_WIDTH))
  val wen : Bool = Input(Bool())
}

class RAMHelper extends BlackBox with CoreConfig {
  val io : RAMHelperIO = IO(new RAMHelperIO)
}

class ROMHelper extends BlackBox with CoreConfig {
  val io : ROMHelperIO = IO(new ROMHelperIO)
}

