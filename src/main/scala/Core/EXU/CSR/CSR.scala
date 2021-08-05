package Core.EXU.CSR

import Core.CoreConfig._
import Core.HasFullOpType
import chisel3._
import chisel3.internal.firrtl.Width

object CsrOp {
  def jmp : UInt = "b0000".U
  def rw  : UInt = "b0001".U
  def rs  : UInt = "b0010".U
  def rc  : UInt = "b0011".U
  def rwi : UInt = "b0101".U
  def rsi : UInt = "b0110".U
  def rci : UInt = "b0111".U
}

private object CsrAddr {
  def ADDR_W    : Width = 12.W
  // Machine Information Registers
  // 0xF11~0xF14
  def mvendorid : UInt  = 0xF11.U(ADDR_W)
  def marchid   : UInt  = 0xF12.U(ADDR_W)
  def mimpid    : UInt  = 0xF13.U(ADDR_W)
  def mhartid   : UInt  = 0xF14.U(ADDR_W)

  // Machine Trap Setup
  // 0x300~0x306
  def mstatus   : UInt  = 0x300.U(ADDR_W)
  def misa      : UInt  = 0x301.U(ADDR_W)
  def medeleg   : UInt  = 0x302.U(ADDR_W)
  def mideleg   : UInt  = 0x303.U(ADDR_W)
  def mie       : UInt  = 0x304.U(ADDR_W)
  def mtvec     : UInt  = 0x305.U(ADDR_W)
  def mcounteren: UInt  = 0x306.U(ADDR_W)

  // Machine Trap Handling
  // 0x340~0x344
  def mscratch  : UInt  = 0x340.U(ADDR_W)
  def mepc      : UInt  = 0x341.U(ADDR_W)
  def mcause    : UInt  = 0x342.U(ADDR_W)
  def mtval     : UInt  = 0x343.U(ADDR_W)
  def mip       : UInt  = 0x344.U(ADDR_W)

  // Machine Memory Protection
  // 0x3A0~0x3A3, 0x3B0~0x3BF
  def pmpcfg(idx: Int) : UInt = {
    require(idx >= 0 && idx < 4)
    (0x3A0 + idx).U(ADDR_W)
  }
  def pmpaddr(idx: Int) : UInt = {
    require(idx >= 0 && idx < 16)
    (0x3B0 + idx).U(ADDR_W)
  }

  // Machine Counters and Timers
  // 0xB00~0xB1F
  def mcycle    : UInt  = 0xB00.U(ADDR_W)
  def mtime     : UInt  = 0xB01.U(ADDR_W)
  def minstret  : UInt  = 0xB02.U(ADDR_W)
  def mhpmcounter(idx : Int) : UInt = {
    require(idx >= 3 && idx < 32)
    (0xB00 + idx).U(ADDR_W)
  }

  // Machine Counter Setup
  // 0x320, 0x323~0x33F
  def mhpmevent(idx: Int) : UInt = {
    require(idx >= 3 && idx < 32)
    (0x320 + idx).U(ADDR_W)
  }
  def mcountinhibit   : UInt = 0x320.U(ADDR_W)

  // Debug/Trace Registers(shared with Debug Mode)
  // 0x7A0~0x7A3
  val tselect   : UInt  = 0x7A0.U(ADDR_W)
  val tdata1    : UInt  = 0x7A1.U(ADDR_W)
  val tdata2    : UInt  = 0x7A2.U(ADDR_W)
  val tdata3    : UInt  = 0x7A3.U(ADDR_W)

  // Debug Mode Registers
  // 0x7B0~0x7B3
  val dcsr      : UInt  = 0x7B0.U(ADDR_W)
  val dpc       : UInt  = 0x7B1.U(ADDR_W)
  val dscratch0 : UInt  = 0x7B2.U(ADDR_W)
  val dscratch1 : UInt  = 0x7B3.U(ADDR_W)
}

class CSRIO extends Bundle with HasFullOpType {
  val ena   : Bool = Input(Bool())
  val addr  : UInt = Input(UInt(CsrAddr.ADDR_W))
  val src   : UInt = Input(UInt(DATA_WIDTH))
  val op    : UInt = Input(UInt(FullOpTypeWidth))
  val rdata : UInt = Output(UInt(DATA_WIDTH))
}

trait CSRDefine {
  private val CSR_DATA_W = MXLEN.W
  // Machine Information Registers
  // 0xF11~0xF14
  val mvendorid     : UInt = VendorID         .U(CSR_DATA_W)
  val marchid       : UInt = ArchitectureID   .U(CSR_DATA_W)
  val mimpid        : UInt = ImplementationID .U(CSR_DATA_W)
  val mhartid       : UInt = HardwareThreadID .U(CSR_DATA_W)

  // Machine Trap Setup
  // 0x300~0x306
  val mstatus       : UInt = Reg(UInt(CSR_DATA_W))
  val misa          : UInt = Reg(UInt(CSR_DATA_W))
  val medeleg       : UInt = Reg(UInt(CSR_DATA_W))
  val mideleg       : UInt = Reg(UInt(CSR_DATA_W))
  val mie           : UInt = Reg(UInt(CSR_DATA_W))
  val mtvec         : UInt = Reg(UInt(CSR_DATA_W))
  val mcounteren    : UInt = Reg(UInt(CSR_DATA_W))

  // Machine Trap Handling
  // 0x340~0x344
  val mscratch      : UInt = Reg(UInt(CSR_DATA_W))
  val mepc          : UInt = Reg(UInt(CSR_DATA_W))
  val mcause        : UInt = Reg(UInt(CSR_DATA_W))
  val mtval         : UInt = Reg(UInt(CSR_DATA_W))
  val mip           : UInt = Reg(UInt(CSR_DATA_W))

  // Machine Memory Protection
  // 0x3A0~0x3A3, 0x3B0~0x3BF
  val pmpcfg        : Vec[UInt] = Vec(4 , Reg(UInt(CSR_DATA_W)))
  val pmpaddr       : Vec[UInt] = Vec(16, Reg(UInt(CSR_DATA_W)))

  // Machine Counters and Timers
  // 0xB00~0xB1F
  val mhpmcounter   : Vec[UInt] = Vec(32, Reg(UInt(CSR_DATA_W)))
  val mcycle        : UInt = mhpmcounter(0)
  val mtime         : UInt = mhpmcounter(1)
  val minstret      : UInt = mhpmcounter(2)

  // Machine Counter Setup
  // 0x320, 0x323~0x33F
  val mhpmevent     : Vec[UInt] = Vec(32, Reg(UInt(CSR_DATA_W)))
  val mcountinhibit : UInt = mhpmevent(0)

  // Debug/Trace Registers(shared with Debug Mode)
  // 0x7A0~0x7A3
  val tselect       : UInt = Reg(UInt(CSR_DATA_W))
  val tdata1        : UInt = Reg(UInt(CSR_DATA_W))
  val tdata2        : UInt = Reg(UInt(CSR_DATA_W))
  val tdata3        : UInt = Reg(UInt(CSR_DATA_W))

  // Debug Mode Registers
  // 0x7B0~0x7B3
  val dcsr          : UInt = Reg(UInt(CSR_DATA_W))
  val dpc           : UInt = Reg(UInt(CSR_DATA_W))
  val dscratch0     : UInt = Reg(UInt(CSR_DATA_W))
  val dscratch1     : UInt = Reg(UInt(CSR_DATA_W))
}

class CSR extends Module with CSRDefine {
  val io : CSRIO = IO(new CSRIO)

}
