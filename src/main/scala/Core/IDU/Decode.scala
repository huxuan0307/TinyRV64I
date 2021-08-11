package Core.IDU

import Core.Config.{HasFuncType, HasInstType, HasRs1Type, HasRs2Type}
import Core.EXU.CSR.CsrOp
import Core.EXU.{AluOp, BruOp, LsuOp}
import Core.ISA.CSR._
import Core.ISA.RV64I._
import Core.ISA.Trap.Trap
import chisel3.{UInt, fromStringToLiteral}
import chisel3.util.BitPat

object Decode
  extends HasFuncType
    with HasInstType
    with HasRs1Type
    with HasRs2Type
{
  import Core.Config.BasicDefine._
  private val NoneOp : UInt = "b00000".U
  // 多路选择的sel尽量在这里完成，不够就加列
  //
  //                                                                                    rd_enable
  //                                                        Inst    Func    OP          |  rs1Type
  val DecodeDefault :                               //      |       |       |           |  |        rs2Type
          List[UInt] =                              List( InstN, FuncALU, NoneOp      , Y, Rs1None, Rs2None)
  object RV32I {
    val DecodeTable : Array[(BitPat, List[UInt])] =
      Array(
      LUI           ->                              List( InstU, FuncALU, AluOp.LUI   , Y, Rs1PC  , Rs2Imm  ),
      AUIPC         ->                              List( InstU, FuncALU, AluOp.ADD   , Y, Rs1PC  , Rs2Imm  ),
      JAL           ->                              List( InstJ, FuncBRU, BruOp.JAL   , Y, Rs1PC  , Rs2Imm  ),
      JALR          ->                              List( InstI, FuncBRU, BruOp.JALR  , Y, Rs1Reg , Rs2Imm  ),
      BEQ           ->                              List( InstB, FuncBRU, BruOp.BEQ   , N, Rs1Reg , Rs2Imm  ),
      BNE           ->                              List( InstB, FuncBRU, BruOp.BNE   , N, Rs1Reg , Rs2Imm  ),
      BLT           ->                              List( InstB, FuncBRU, BruOp.BLT   , N, Rs1Reg , Rs2Imm  ),
      BGE           ->                              List( InstB, FuncBRU, BruOp.BGE   , N, Rs1Reg , Rs2Imm  ),
      BLTU          ->                              List( InstB, FuncBRU, BruOp.BLTU  , N, Rs1Reg , Rs2Imm  ),
      BGEU          ->                              List( InstB, FuncBRU, BruOp.BGEU  , N, Rs1Reg , Rs2Imm  ),
      LB            ->                              List( InstI, FuncLSU, LsuOp.LB    , Y, Rs1Reg , Rs2Imm  ),
      LH            ->                              List( InstI, FuncLSU, LsuOp.LH    , Y, Rs1Reg , Rs2Imm  ),
      LW            ->                              List( InstI, FuncLSU, LsuOp.LW    , Y, Rs1Reg , Rs2Imm  ),
      LBU           ->                              List( InstI, FuncLSU, LsuOp.LBU   , Y, Rs1Reg , Rs2Imm  ),
      LHU           ->                              List( InstI, FuncLSU, LsuOp.LHU   , Y, Rs1Reg , Rs2Imm  ),
      SB            ->                              List( InstS, FuncLSU, LsuOp.SB    , N, Rs1Reg , Rs2Imm  ),
      SH            ->                              List( InstS, FuncLSU, LsuOp.SH    , N, Rs1Reg , Rs2Imm  ),
      SW            ->                              List( InstS, FuncLSU, LsuOp.SW    , N, Rs1Reg , Rs2Imm  ),
      ADDI          ->                              List( InstI, FuncALU, AluOp.ADD   , Y, Rs1Reg , Rs2Imm  ),
      SLTI          ->                              List( InstI, FuncALU, AluOp.SLT   , Y, Rs1Reg , Rs2Imm  ),
      SLTIU         ->                              List( InstI, FuncALU, AluOp.SLTU  , Y, Rs1Reg , Rs2Imm  ),
      XORI          ->                              List( InstI, FuncALU, AluOp.XOR   , Y, Rs1Reg , Rs2Imm  ),
      ORI           ->                              List( InstI, FuncALU, AluOp.OR    , Y, Rs1Reg , Rs2Imm  ),
      ANDI          ->                              List( InstI, FuncALU, AluOp.AND   , Y, Rs1Reg , Rs2Imm  ),
      SLLI          ->                              List( InstI, FuncALU, AluOp.SLL   , Y, Rs1Reg , Rs2Imm  ),
      SRLI          ->                              List( InstI, FuncALU, AluOp.SRL   , Y, Rs1Reg , Rs2Imm  ),
      SRAI          ->                              List( InstI, FuncALU, AluOp.SRA   , Y, Rs1Reg , Rs2Imm  ),
      ADD           ->                              List( InstR, FuncALU, AluOp.ADD   , Y, Rs1Reg , Rs2Reg  ),
      SUB           ->                              List( InstR, FuncALU, AluOp.SUB   , Y, Rs1Reg , Rs2Reg  ),
      SLL           ->                              List( InstR, FuncALU, AluOp.SLL   , Y, Rs1Reg , Rs2Reg  ),
      SLT           ->                              List( InstR, FuncALU, AluOp.SLT   , Y, Rs1Reg , Rs2Reg  ),
      SLTU          ->                              List( InstR, FuncALU, AluOp.SLTU  , Y, Rs1Reg , Rs2Reg  ),
      XOR           ->                              List( InstR, FuncALU, AluOp.XOR   , Y, Rs1Reg , Rs2Reg  ),
      SRL           ->                              List( InstR, FuncALU, AluOp.SRL   , Y, Rs1Reg , Rs2Reg  ),
      SRA           ->                              List( InstR, FuncALU, AluOp.SRA   , Y, Rs1Reg , Rs2Reg  ),
      OR            ->                              List( InstR, FuncALU, AluOp.OR    , Y, Rs1Reg , Rs2Reg  ),
      AND           ->                              List( InstR, FuncALU, AluOp.AND   , Y, Rs1Reg , Rs2Reg  ),
      FENCE         ->                              List( InstI, FuncNONE,NoneOp      , N, Rs1None, Rs2None ),
      FENCE_I       ->                              List( InstI, FuncNONE,NoneOp      , N, Rs1None, Rs2None ),
      ECALL         ->                              List( InstI, FuncCSRU,CsrOp.ECALL , N, Rs1None, Rs2None ),
      EBREAK        ->                              List( InstI, FuncCSRU,CsrOp.EBREAK, N, Rs1None, Rs2None ),
      CSRRW         ->                              List( InstI, FuncCSRU,CsrOp.RW    , Y, Rs1Reg , Rs2Imm  ),
      CSRRS         ->                              List( InstI, FuncCSRU,CsrOp.RS    , Y, Rs1Reg , Rs2Imm  ),
      CSRRC         ->                              List( InstI, FuncCSRU,CsrOp.RC    , Y, Rs1Reg , Rs2Imm  ),
      CSRRWI        ->                              List( InstI, FuncCSRU,CsrOp.RWI   , Y, Rs1UImm, Rs2Imm  ),
      CSRRSI        ->                              List( InstI, FuncCSRU,CsrOp.RSI   , Y, Rs1UImm, Rs2Imm  ),
      CSRRCI        ->                              List( InstI, FuncCSRU,CsrOp.RCI   , Y, Rs1UImm, Rs2Imm  ),
      MRET          ->                              List( InstI, FuncCSRU,CsrOp.MRET  , N, Rs1Reg , Rs2Imm  ),

      // todo: add decode for fence, ECall ... CSRop
    )
  }

  object RV64I {
    val DecodeTable : Array[(BitPat, List[UInt])] = Array(
      LWU           ->                              List( InstI, FuncLSU, LsuOp.LWU   , Y, Rs1Reg , Rs2Imm  ),
      LD            ->                              List( InstI, FuncLSU, LsuOp.LD    , Y, Rs1Reg , Rs2Imm  ),
      ADDIW         ->                              List( InstI, FuncALU, AluOp.ADD   , Y, Rs1Reg , Rs2Imm  ),
      SLLIW         ->                              List( InstI, FuncALU, AluOp.SLL   , Y, Rs1Reg , Rs2Imm  ),
      SRLIW         ->                              List( InstI, FuncALU, AluOp.SRL   , Y, Rs1Reg , Rs2Imm  ),
      SRAIW         ->                              List( InstI, FuncALU, AluOp.SRA   , Y, Rs1Reg , Rs2Imm  ),
      SD            ->                              List( InstS, FuncLSU, LsuOp.SD    , N, Rs1Reg , Rs2Imm  ),
      ADDW          ->                              List( InstR, FuncALU, AluOp.ADD   , Y, Rs1Reg , Rs2Reg  ),
      SUBW          ->                              List( InstR, FuncALU, AluOp.SUB   , Y, Rs1Reg , Rs2Reg  ),
      SLLW          ->                              List( InstR, FuncALU, AluOp.SLL   , Y, Rs1Reg , Rs2Reg  ),
      SRLW          ->                              List( InstR, FuncALU, AluOp.SRL   , Y, Rs1Reg , Rs2Reg  ),
      SRAW          ->                              List( InstR, FuncALU, AluOp.SRA   , Y, Rs1Reg , Rs2Reg  ),
    )
  }

  object TrapI {
    val DecodeTable : Array[(BitPat, List[UInt])] = Array(
      // 构造TRAP指令时，一般rd会是x0，保险起见，还是把regfile写使能置为N
      Trap.TRAP     ->                              List( InstT, FuncTrapU, NoneOp    , N, Rs1None, Rs2None )
    )
  }

  val DecodeTable : Array[(BitPat, List[UInt])] =
    RV32I.DecodeTable ++ RV64I.DecodeTable ++ TrapI.DecodeTable

  val RsTypeTable = Seq (
    InstN -> Tuple2(Rs1None, Rs2None),
    InstU -> Tuple2(Rs1PC,  Rs2Imm),
    InstJ -> Tuple2(Rs1PC,  Rs2Imm),
    InstB -> Tuple2(Rs1Reg, Rs2Imm),  // B指令的寄存器数x[rs2]在wdata中传递
    InstI -> Tuple2(Rs1Reg, Rs2Imm),
    InstS -> Tuple2(Rs1Reg, Rs2Imm),
    InstR -> Tuple2(Rs1Reg, Rs2Reg),
    InstT -> Tuple2(Rs1Reg, Rs2Imm)   // 自定义的Trap指令，传递x[0]和立即数
  )
}
