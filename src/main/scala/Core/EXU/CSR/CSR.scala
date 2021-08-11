package Core.EXU.CSR

import Core.Bundles.BranchPathIO
import Core.Config.CoreConfig._
import Core.EXU.CSR.Define.Privilege.{supportSupervisor, supportUser}
import Core.Config.HasFullOpType
import Core.EXU.CSR.Define.{Privilege, Traps}
import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._
import difftest.DifftestCSRState

object CsrOp {
  def RW    : UInt = "b00001".U
  def RS    : UInt = "b00010".U
  def RC    : UInt = "b00011".U
  def RWI   : UInt = "b00101".U
  def RSI   : UInt = "b00110".U
  def RCI   : UInt = "b00111".U
  def ECALL : UInt = "b10000".U
  def EBREAK: UInt = "b10001".U
  def MRET  : UInt = "b11000".U
  def SRET  : UInt = "b11001".U
  def URET  : UInt = "b11011".U
  def is_jmp(op: UInt) : Bool = op(4).asBool()
  def is_ret(op: UInt) : Bool = op(3).asBool()
  def is_csri(op: UInt) : Bool = op(2).asBool()
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


trait CSRDefine {
  class InterruptEnable extends Bundle {
    val M : Bool = Output(Bool())
    val H : Bool = Output(Bool())
    val S : Bool = Output(Bool())
    val U : Bool = Output(Bool())
  }

  // 参考XiangShan的实现
  class Status extends Bundle {
    val SD    : UInt = Output(UInt(1.W))
    val PAD0  : UInt = if (MXLEN == 64) Output(UInt((MXLEN - 37).W)) else null
    val SXL   : UInt = if (MXLEN == 64) Output(UInt(2.W)) else null
    val UXL   : UInt = if (MXLEN == 64) Output(UInt(2.W)) else null
    val PAD1  : UInt = if (MXLEN == 64) Output(UInt(9.W)) else Output(UInt(8.W))
    val TSR   : UInt = Output(UInt(1.W))
    val TW    : UInt = Output(UInt(1.W))
    val TVM   : UInt = Output(UInt(1.W))
    val MXR   : UInt = Output(UInt(1.W))
    val SUM   : UInt = Output(UInt(1.W))
    val MPRV  : UInt = Output(UInt(1.W))
    val XS    : UInt = Output(UInt(2.W))
    val FS    : UInt = Output(UInt(2.W))
    val MPP   : UInt = Output(UInt(2.W))
    val HPP   : UInt = Output(UInt(2.W))
    val SPP   : UInt = Output(UInt(1.W))
    val PIE   = new InterruptEnable
    val IE    = new InterruptEnable
  }

  protected val CSR_DATA_W : Width = MXLEN.W
  // Machine Information Registers
  // 0xF11~0xF14
  val mvendorid     : UInt = VendorID         .U(CSR_DATA_W)
  val marchid       : UInt = ArchitectureID   .U(CSR_DATA_W)
  val mimpid        : UInt = ImplementationID .U(CSR_DATA_W)
  val mhartid       : UInt = HardwareThreadID .U(CSR_DATA_W)

  // Machine Trap Setup
  // 0x300~0x306
  val status        : Status = RegInit(0.U.asTypeOf(new Status))
  val misa          : UInt = RegInit(MISA.U(CSR_DATA_W))
  val medeleg       : UInt = RegInit(0.U(CSR_DATA_W))
  val mideleg       : UInt = RegInit(0.U(CSR_DATA_W))
  val mie           : UInt = RegInit(0.U(CSR_DATA_W))
  val mtvec         : UInt = RegInit(0.U(CSR_DATA_W))
  val mcounteren    : UInt = RegInit(0.U(CSR_DATA_W))

  // Machine Trap Handling
  // 0x340~0x344
  val mscratch      : UInt = RegInit(0.U(CSR_DATA_W))
  val mepc          : UInt = RegInit(0.U(CSR_DATA_W))
  val mcause        : UInt = RegInit(0.U(CSR_DATA_W))
  val mtval         : UInt = RegInit(0.U(CSR_DATA_W))
  val mip           : UInt = RegInit(0.U(CSR_DATA_W))

  // Machine Memory Protection
  // 0x3A0~0x3A3, 0x3B0~0x3BF
  val pmpcfg        : Vec[UInt] = RegInit(VecInit(Seq.fill( 4)(0.U(CSR_DATA_W))))
  val pmpaddr       : Vec[UInt] = RegInit(VecInit(Seq.fill(16)(0.U(CSR_DATA_W))))

  // Machine Counters and Timers
  // 0xB00~0xB1F
  val mhpmcounter   : Vec[UInt] = RegInit(VecInit(Seq.fill(32)(0.U(CSR_DATA_W))))
  val mcycle        : UInt = mhpmcounter(0)
  val mtime         : UInt = WireInit(mcycle)
  val minstret      : UInt = mhpmcounter(2)

  // Machine Counter Setup
  // 0x320, 0x323~0x33F
  val mhpmevent     : Vec[UInt] = RegInit(VecInit(Seq.fill(32)(0.U(CSR_DATA_W))))
  val mcountinhibit : UInt = mhpmevent(0)

  // Debug/Trace Registers(shared with Debug Mode)
  // 0x7A0~0x7A3
  val tselect       : UInt = RegInit(0.U(CSR_DATA_W))
  val tdata1        : UInt = RegInit(0.U(CSR_DATA_W))
  val tdata2        : UInt = RegInit(0.U(CSR_DATA_W))
  val tdata3        : UInt = RegInit(0.U(CSR_DATA_W))

  // Debug Mode Registers
  // 0x7B0~0x7B3
  val dcsr          : UInt = RegInit(0.U(CSR_DATA_W))
  val dpc           : UInt = RegInit(0.U(CSR_DATA_W))
  val dscratch0     : UInt = RegInit(0.U(CSR_DATA_W))
  val dscratch1     : UInt = RegInit(0.U(CSR_DATA_W))

  /** sub field in CSRs */

  // mtvec
  val mtvec_base    : UInt = mtvec(MXLEN-1, 2)
  val mtvec_mode    : UInt = mtvec(1, 0)
  object MtvecMode {
    def Direct : UInt = 0.U(2.W)
    def Vectored : UInt = 1.U(2.W)
  }

  val readOnlyMap = List (
    CsrAddr.mvendorid   ->  mvendorid   ,
    CsrAddr.marchid     ->  marchid     ,
    CsrAddr.mimpid      ->  mimpid      ,
    CsrAddr.mhartid     ->  mhartid     ,
  )

  val mstatus = WireInit(status)
  mstatus.UXL := (if(supportUser)  (log2Ceil(UXLEN)-4).U else 0.U)
  mstatus.SXL := (if(supportSupervisor) (log2Ceil(SXLEN)-4).U else 0.U)
  mstatus.SPP := (if(!supportSupervisor) 0.U else status.SPP)
  mstatus.MPP := (if(!supportUser) Privilege.Level.M else status.MPP)
  mstatus.IE.U := (if(!supportUser) 0.U else status.IE.U)
  mstatus.IE.S := (if(!supportSupervisor) 0.U else status.IE.S)
  mstatus.PIE.U := (if(!supportUser) 0.U else status.PIE.U)
  mstatus.PIE.S := (if(!supportSupervisor) 0.U else status.PIE.S)

  val readWriteMap = List (
    CsrAddr.mstatus     ->  mstatus.asUInt(),
    CsrAddr.misa        ->  misa        ,
    CsrAddr.medeleg     ->  medeleg     , // 异常委托寄存器，将m处理的异常委托给更低的特权级
    CsrAddr.mideleg     ->  mideleg     , // 中断委托寄存器，将m处理的中断委托给更低的特权级
    CsrAddr.mie         ->  mie         ,
    CsrAddr.mtvec       ->  mtvec       ,
    CsrAddr.mcounteren  ->  mcounteren  ,
    CsrAddr.mscratch    ->  mscratch    ,
    CsrAddr.mepc        ->  mepc        ,
    CsrAddr.mcause      ->  mcause      ,
    CsrAddr.mtval       ->  mtval       ,
    CsrAddr.mip         ->  mip         ,
    // todo map pmpcfg[0~15]
    CsrAddr.mcycle      ->  mcycle      ,
    CsrAddr.minstret    ->  minstret    ,
    // todo map mhpmcounter[3~31]
    // todo map Machine Counter Setup, Debug/Trace Registers, Debug Mode Registers
  )
}

class CSRIO extends Bundle with HasFullOpType {
  class CSRInPort extends Bundle {
    val ena   : Bool = Input(Bool())
    val addr  : UInt = Input(UInt(CsrAddr.ADDR_W))
    val src   : UInt = Input(UInt(DATA_WIDTH))
    val pc    : UInt = Input(UInt(ADDR_WIDTH))
    val op_type    : UInt = Input(UInt(FullOpTypeWidth))
  }
  class CSROutPort extends Bundle {
    val rdata : UInt          = Output(UInt(DATA_WIDTH))
    val jmp  : BranchPathIO  = new BranchPathIO
  }
  val in = new CSRInPort
  val out = new CSROutPort
}

class CSR extends Module with CSRDefine {
  val io : CSRIO = IO(new CSRIO)
  private val addr = io.in.addr(CSR_ADDR_LEN - 1, 0)
  private val op = io.in.op_type
  private val pc = io.in.pc
  private val ena = io.in.ena
  // 为了用Enum，被迫下划线命名枚举。。。bullshxt
  private val mode_u::mode_s::mode_h::mode_m::Nil = Enum(4)
  private val currentPriv = RegInit(UInt(2.W), mode_m)

  private val rdata = MuxLookup(addr, 0.U(MXLEN.W), readOnlyMap++readWriteMap)
  private val wdata = MuxLookup(op, 0.U, Array(
    CsrOp.RW  ->  io.in.src,
    CsrOp.RWI ->  io.in.src,
    CsrOp.RS  ->  (rdata | io.in.src),
    CsrOp.RSI ->  (rdata | io.in.src),
    CsrOp.RC  ->  (rdata & (~io.in.src).asUInt()),
    CsrOp.RCI ->  (rdata & (~io.in.src).asUInt())
  ))
  mcycle := mcycle + 1.U
  // todo: add inst_valid in io, minstret increase only when an instruction return.
  private val inst_valid = true.B
  when (inst_valid) {
    minstret := minstret + 1.U
  }
  private val is_mret = CsrOp.MRET === op
  private val is_sret = CsrOp.SRET === op
  private val is_uret = CsrOp.URET === op
  private val is_jmp : Bool = CsrOp.is_jmp(op)
  private val is_ret = CsrOp.is_ret(op) & is_jmp
  private val new_pc = WireInit(0.U(ADDR_WIDTH))
  dontTouch(new_pc)
  private val trap_valid = WireInit(false.B)
  when(ena && !is_jmp) {
    new_pc := 0.U
    trap_valid := false.B
    switch(addr) {
      is(CsrAddr.mstatus)    {
        val mstatus_new = WireInit(wdata.asTypeOf(new Status))
        // todo 分别把各特权级允许写的字段一一连线
        if (supportUser) {
          status.MPRV  :=  mstatus_new.MPRV
          status.MPP   :=  legalizePrivilege(mstatus_new.MPP)
        }
        status.IE.M := mstatus_new.IE.M
        status.PIE.M := mstatus_new.PIE.M
      }
      is(CsrAddr.medeleg)   { medeleg   := wdata  }
      is(CsrAddr.mideleg)   { mideleg   := wdata  }
      is(CsrAddr.mie)       { mie       := wdata  }
      is(CsrAddr.mtvec)     { mtvec     := wdata  }
      is(CsrAddr.mcounteren){ mcounteren:= wdata  }
      is(CsrAddr.mscratch)  { mscratch  := wdata  }
      is(CsrAddr.mepc)      { mepc      := wdata  }
      is(CsrAddr.mcause)    { mcause    := wdata  }
      is(CsrAddr.mtval)     { mtval     := wdata  }
      is(CsrAddr.mip)       { mip       := wdata  }
      // todo map pmpcfg[0~15]
      is(CsrAddr.mcycle)    { mcycle    := wdata  }
      is(CsrAddr.minstret)  { minstret  := wdata  }
      // todo map mhpmcounter[3~31]
      // todo map Machine Counter Setup, Debug/Trace Registers, Debug Mode Registers
    }
  }.elsewhen(ena && is_jmp){
    // handle output
    trap_valid := true.B
    new_pc := Mux(is_ret,
      MuxLookup(currentPriv, 0.U, Array(
        mode_m -> mepc,
        // todo: add mode s&u
      )),
      // is except
      MuxLookup(mtvec_mode, 0.U, Array(
        MtvecMode.Direct -> Cat(mtvec_base(61,0), 0.U(2.W)),
        MtvecMode.Vectored -> Cat(mtvec_base + mcause, 0.U(2.W))
      ))
    )
    // handle internal
    when (op === CsrOp.ECALL) {
      when (currentPriv === mode_m) {
        mepc := pc
        mcause := Traps.MECall.U
      }
      status.IE.M := false.B
      status.PIE.M := status.IE.M
      status.MPP := currentPriv
//      val mstatus_old = WireInit(mstatus.asTypeOf(new Status))
//      val mstatus_new = WireInit(mstatus.asTypeOf(new Status))
//      mstatus_new.IE.M := false.B           // xIE设为0
//      mstatus_new.PIE.M := mstatus_old.IE.M // xPIE设为xIE的值
//      mstatus_new.MPP := currentPriv        // xPPi设为之前的特权级
//      status := mstatus_new.asUInt         // 写回mstatus

    }.elsewhen(is_mret) {
//      val mstatus_old = WireInit(mstatus.asTypeOf(new Status))
//      val mstatus_new = WireInit(mstatus.asTypeOf(new Status))
//      currentPriv := mstatus_old.MPP        // 特权模式修改为y模式
//      mstatus_new.PIE.M := true.B           // xPIE设为1
//      mstatus_new.IE.M := mstatus_old.PIE.M // xIE设为xPIE
//      mstatus_new.MPP := (if (supportUser) mode_u else mode_u)   // xPP设置为U模式（不支持U模式，则是M模式）
//      mstatus := mstatus_new.asUInt
      currentPriv := mstatus.MPP
      status.PIE.M := true.B
      status.IE.M := mstatus.PIE.M
      // todo: 给CSR加上U模式，这里为了和NEMU的行为同步，即使不支持U模式，MPP也设定为mode_u
      status.MPP := (if (supportUser) mode_u else mode_m)
    }
  }
  io.out.jmp.new_pc := new_pc
  io.out.jmp.valid := trap_valid
  io.out.rdata := rdata

  private val csrCommit = Module(new DifftestCSRState)
  csrCommit.io.clock          := clock
  csrCommit.io.coreid         := 0.U
  csrCommit.io.priviledgeMode := currentPriv
  csrCommit.io.mstatus        := mstatus.asUInt()
  csrCommit.io.sstatus        := 0.U
  csrCommit.io.mepc           := mepc
  csrCommit.io.sepc           := 0.U
  csrCommit.io.mtval          := mtval
  csrCommit.io.stval          := 0.U
  csrCommit.io.mtvec          := mtvec
  csrCommit.io.stvec          := 0.U
  csrCommit.io.mcause         := mcause
  csrCommit.io.scause         := 0.U
  csrCommit.io.satp           := 0.U
  csrCommit.io.mip            := mip
  csrCommit.io.mie            := mie
  csrCommit.io.mscratch       := mscratch
  csrCommit.io.sscratch       := 0.U
  csrCommit.io.mideleg        := mideleg
  csrCommit.io.medeleg        := medeleg

  def legalizePrivilege(priv: UInt): UInt =
    if (supportUser)
      Fill(2, priv(0))
    else
      Privilege.Level.M
}
