package Sim

import Common.Const.ULONG_MAX
import Core.Config.BasicDefine.ZERO32
import Core.Config.{CoreConfig, HasMemDataType}
import Core.{DMemIO, IMemIO}
import chisel3.util.MuxLookup
import chisel3._
import difftest.UARTIO

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
    uart_out_valid := io.dmem.valid
    ram_valid := false.B
  }.otherwise {
    uart_out_valid := false.B
    ram_valid := io.dmem.valid
  }
  io.uart.out.valid := RegNext(RegNext(uart_out_valid))
  io.uart.out.ch := RegNext(RegNext(io.dmem.wdata(7, 0)))
  io.uart.in.valid := 0.U
  io.skip := uart_out_valid

  val data_ram : RAMHelper = Module(new RAMHelper)
  val inst_rom : ROMHelper = Module(new ROMHelper)
  private val pc = io.imem.addr
  private val inst = Mux(pc(2), inst_rom.io.rdata(63, 32), inst_rom.io.rdata(31, 0))
  private val inst_valid = inst =/= ZERO32
  inst_rom.io.clk := clock
  inst_rom.io.en := (!reset.asBool()) & true.B
  inst_rom.io.rIdx := (pc - 0x80000000L.U(64.W)) (63, 3)
  io.imem.rdata := inst
  private val wdata = io.dmem.wdata
  private val offset = io.dmem.addr(2, 0)


  private val mask = MuxLookup(io.dmem.data_type, ULONG_MAX.U, Array(
    type_b -> BigInt(0xff).U(DATA_WIDTH),
    type_h -> BigInt(0xffff).U(DATA_WIDTH),
    type_w -> BigInt(0xffffffffL).U(DATA_WIDTH),
    type_d -> ULONG_MAX.U
  ))
  private val wdata_align = wdata << (offset * 8.U)
  private val mask_align = mask << (offset * 8.U)
  private val rdata_align = data_ram.io.rdata >> (offset * 8.U)
  data_ram.io.clk := clock
  data_ram.io.en := ram_valid
  data_ram.io.rIdx := (io.dmem.addr - 0x80000000L.U) >> 3.U
  io.dmem.rdata := rdata_align

  data_ram.io.wIdx := (io.dmem.addr - 0x80000000L.U) >> 3.U
  data_ram.io.wdata := wdata_align
  data_ram.io.wmask := mask_align
  data_ram.io.wen := io.dmem.wena

  io.dmem.debug.data := 0.U
  io.pc := pc
  io.inst := inst
  io.inst_valid := inst_valid
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

