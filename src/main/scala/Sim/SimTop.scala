package Sim

import chisel3._
import chisel3.util._
import Core._
import difftest._
import BasicDefine._
import Chisel.unless
import Util.Counter
import Common.Const.ULONG_MAX

class SimTopIO extends Bundle {
  val logCtrl = new LogCtrlIO
  val perfInfo = new PerfInfoIO
  val uart = new UARTIO
}

class MemHelper extends Module with CoreConfig with HasMemDataType {
  class MemHelperIO extends Bundle {
    val imem = new IMemIO
    val dmem = new DMemIO
    val uart = new UARTIO
    val pc : UInt = Output(UInt(ADDR_WIDTH))
    val inst : UInt = Output(UInt(INST_WIDTH))
    val inst_valid : Bool = Output(Bool())
    val skip : Bool = Output(Bool())
  }
  val io : MemHelperIO = IO(new MemHelperIO)

  def SERIAL_MMIO_ADDR : UInt = 0xa10003f8L.U(ADDR_WIDTH)
  private val io_addr = io.dmem.addr
  private val ram_valid = Wire(Bool())
  private val uart_out_valid = Wire(Bool())
  when(io_addr === SERIAL_MMIO_ADDR) {
    // 访问串口
//    uart_out_valid := true.B & io.dmem.valid & io.dmem.wena
    uart_out_valid := io.dmem.valid
    ram_valid := false.B
  }.otherwise {
    uart_out_valid := false.B
    ram_valid := io.dmem.valid
  }
  io.uart.out.valid := RegNext(RegNext(uart_out_valid))
  io.uart.out.ch := RegNext(RegNext(io.dmem.wdata(7,0)))
//  io.uart.out.ch := 0.U
  io.uart.in.valid := 0.U
  io.skip := uart_out_valid

  val data_ram : RAMHelper = Module(new RAMHelper)
  val inst_rom : ROMHelper = Module(new ROMHelper)
  private val pc = io.imem.addr
  private val inst = Mux(pc(2), inst_rom.io.rdata(63,32), inst_rom.io.rdata(31,0))
  private val inst_valid = inst =/= ZERO32
  inst_rom.io.clk := clock
  inst_rom.io.en  := (!reset.asBool()) & true.B
  inst_rom.io.rIdx := (pc - 0x80000000L.U(64.W))(63,3)
  io.imem.rdata := inst
  private val wdata = io.dmem.wdata
  private val offset = io.dmem.addr(2,0)


  private val mask = MuxLookup(io.dmem.data_type, ULONG_MAX.U, Array(
    type_b -> BigInt(0xff).U(DATA_WIDTH),
    type_h -> BigInt(0xffff).U(DATA_WIDTH),
    type_w -> BigInt(0xffffffffL).U(DATA_WIDTH),
    type_d -> ULONG_MAX.U
  ))
  private val wdata_align = wdata << (offset * 8.U)
  private val mask_align = mask << (offset * 8.U)
  private val rdata_align = data_ram.io.rdata >> (offset * 8.U)
  data_ram.io.clk  := clock
  data_ram.io.en   := ram_valid
  data_ram.io.rIdx := (io.dmem.addr - 0x80000000L.U) >> 3.U
  io.dmem.rdata := rdata_align

  data_ram.io.wIdx := (io.dmem.addr - 0x80000000L.U) >> 3.U
  data_ram.io.wdata := wdata_align
  data_ram.io.wmask := mask_align
  data_ram.io.wen   := io.dmem.wena

  io.dmem.debug.data := 0.U
  io.pc := pc
  io.inst := inst
  io.inst_valid := inst_valid
}

class SimTop extends Module with CoreConfig with HasMemDataType {
  val io : SimTopIO = IO(new SimTopIO())
//  io.uart.in.valid := false.B
//  io.uart.out.valid := false.B
//  io.uart.out.ch  := 0.U
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
  private val commit_gpr        = rvcore.io.diffTest.reg
  private val commit_trap_valid = rvcore.io.diffTest.trap.valid
  private val commit_trap_code  = rvcore.io.diffTest.reg(10)(2,0)

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

  private val regfileCommit = Module(new DifftestArchIntRegState)
  regfileCommit.io.clock := clock
  regfileCommit.io.coreid := 0.U
  regfileCommit.io.gpr := commit_gpr

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

