package Sim

import chisel3._
import chisel3.util._
import Core._
import difftest._

class SimTopIO extends Bundle {
  val logCtrl = new LogCtrlIO
  val perfInfo = new PerfInfoIO
  val uart = new UARTIO
}

class SimTop extends Module with CoreConfig with HasMemDataType {
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
    type_w -> BigInt(0xffffffffL).U(DATA_WIDTH)
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

  private val instrCommit = Module(new DifftestInstrCommit)
  instrCommit.io.clock := clock
  instrCommit.io.coreid := 0.U
  instrCommit.io.index := 0.U
  instrCommit.io.skip := false.B
  instrCommit.io.isRVC := false.B
  instrCommit.io.scFailed := false.B

  instrCommit.io.valid := RegNext(RegNext(rvcore.io.diffTest.commit))
  instrCommit.io.pc := RegNext(RegNext(pc))

  instrCommit.io.instr := RegNext(RegNext(inst))

  instrCommit.io.wen := RegNext(RegNext(rvcore.io.diffTest.wreg.ena))
  instrCommit.io.wdata := RegNext(RegNext(rvcore.io.diffTest.wreg.data))
  instrCommit.io.wdest := RegNext(RegNext(rvcore.io.diffTest.wreg.addr))

  private val regfileCommit = Module(new DifftestArchIntRegState)
  regfileCommit.io.clock := clock
  regfileCommit.io.coreid := 0.U
  regfileCommit.io.gpr := RegNext(rvcore.io.diffTest.reg)

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

