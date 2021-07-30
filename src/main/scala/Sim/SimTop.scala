package Sim

import chisel3._
import chisel3.util._
import Core._
import chisel3.experimental.ExtModule
import difftest._

class SimTopIO extends Bundle {
  val logCtrl = new LogCtrlIO
  val perfInfo = new PerfInfoIO
  val uart = new UARTIO
  val in_valid = Input(Bool())

}

class SimTop extends Module with CoreConfig with HasMemDataType {
  val io : SimTopIO = IO(new SimTopIO())
  io.uart.in.valid := false.B
  io.uart.out.valid := false.B
  io.uart.out.ch  := 0.U
  val rvcore : Top = Module(new Top)

  val data_ram = Module(new RAMHelper)
  val inst_rom = Module(new ROMHelper)


  // imem
  inst_rom.io.clk := clock
  inst_rom.io.en  := true.B
  inst_rom.io.rIdx := rvcore.io.imem.addr - (BigInt("80000000", 16)>>3).U
  rvcore.io.imem.rdata := Mux(inst_rom.io.rIdx(2), inst_rom.io.rdata(63,32), inst_rom.io.rdata(31,0))
  // dmem
  val wdata = rvcore.io.dmem.wdata
  val offset = rvcore.io.dmem.addr(2,0)
  val mask = MuxLookup(rvcore.io.dmem.data_type, BigInt("ffffffffffffffff", 16).U, Array(
    type_b -> BigInt(0xff).U(DATA_WIDTH),
    type_h -> BigInt(0xffff).U(DATA_WIDTH),
    type_w -> BigInt(0xffffffffL).U(DATA_WIDTH)
  ))
  val wdata_align = wdata << (offset * 8.U) // offset*8
  val mask_align = mask << (offset * 8.U)
  data_ram.io.clk  := clock
  data_ram.io.en   := rvcore.io.dmem.valid
  data_ram.io.rIdx := rvcore.io.dmem.addr - (BigInt("80000000", 16)>>3).U
  rvcore.io.dmem.rdata := data_ram.io.rdata
  data_ram.io.wIdx := rvcore.io.dmem.addr - (BigInt("80000000", 16)>>3).U
  data_ram.io.wdata := wdata_align
  // todo: check it
  data_ram.io.wmask := mask_align
  data_ram.io.wen   := rvcore.io.dmem.wena

  rvcore.io.dmem.debug.data := 0.U

  rvcore.io.debug.addr := 0.U

  val instrCommit = Module(new DifftestInstrCommit)
  instrCommit.io.clock := rvcore.clock
  instrCommit.io.coreid := 0.U
  instrCommit.io.index := 0.U
  instrCommit.io.skip := false.B
  instrCommit.io.isRVC := false.B
  instrCommit.io.scFailed := false.B

  instrCommit.io.valid := RegNext(rvcore.io.diffTest.commit)
  instrCommit.io.pc := RegNext(rvcore.io.imem.addr)
  instrCommit.io.instr := RegNext(rvcore.io.imem.rdata)

  instrCommit.io.wen := RegNext(rvcore.io.diffTest.wreg.ena)
  instrCommit.io.wdata := RegNext(rvcore.io.diffTest.wreg.data)
  instrCommit.io.wdest := RegNext(rvcore.io.diffTest.wreg.addr)

//  val regfileCommit = Module(new DifftestArchIntRegState)
//  regfileCommit.io.clock := clock
//  regfileCommit.io.coreid := 0.U
//  regfileCommit.io.gpr := RegNext(rvcore.io.diffTest.reg)

}

class ROMHelperIO extends Bundle with CoreConfig {
  val clk = Input(Clock())
  val en  = Input(Bool())
  val rIdx = Input(UInt(ADDR_WIDTH))
  val rdata = Output(UInt(DATA_WIDTH))
}

class RAMHelperIO extends Bundle with CoreConfig {
  val clk = Input(Clock())
  val en = Input(Bool())
  val rIdx = Input(UInt(ADDR_WIDTH))
  val rdata = Output(UInt(DATA_WIDTH))
  val wIdx = Input(UInt(ADDR_WIDTH))
  val wdata = Input(UInt(DATA_WIDTH))
  val wmask = Input(UInt(ADDR_WIDTH))
  val wen   = Input(Bool())
}

class RAMHelper extends BlackBox with CoreConfig {
  val io = IO(new RAMHelperIO)
}

class ROMHelper extends BlackBox with CoreConfig {
  val io = IO(new ROMHelperIO)
}

