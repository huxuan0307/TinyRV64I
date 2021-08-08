package Core

import Core.EXU.CSR.CsrOp
import Core.EXU.{AluOp, BruOp, LsuOp}
import chisel3._
import ISA.RV64I._
import ISA.Trap._
import ISA.CSR._
import chisel3.util.BitPat

object Decode
  extends HasFuncType
    with HasInstType
    with HasRs1Type
    with HasRs2Type
{
  import Core.BasicDefine._
  private val NoneOp : UInt = "b0000".U

  //                                                    rd_enable
  //                      | Inst | Func   |     Op    |
  val DecodeDefault : List[UInt] = List( InstN, FuncALU, NoneOp , Y)
  object RV32I {
    val DecodeTable : Array[(BitPat, List[UInt])] = Array(
      LUI           ->  List( InstU, FuncALU, AluOp.LUI , Y),
      AUIPC         ->  List( InstU, FuncALU, AluOp.ADD , Y),
      JAL           ->  List( InstJ, FuncBRU, BruOp.JAL , Y),
      JALR          ->  List( InstI, FuncBRU, BruOp.JALR, Y),
      BEQ           ->  List( InstB, FuncBRU, BruOp.BEQ , N),
      BNE           ->  List( InstB, FuncBRU, BruOp.BNE , N),
      BLT           ->  List( InstB, FuncBRU, BruOp.BLT , N),
      BGE           ->  List( InstB, FuncBRU, BruOp.BGE , N),
      BLTU          ->  List( InstB, FuncBRU, BruOp.BLTU, N),
      BGEU          ->  List( InstB, FuncBRU, BruOp.BGEU, N),
      LB            ->  List( InstI, FuncLSU, LsuOp.LB  , Y),
      LH            ->  List( InstI, FuncLSU, LsuOp.LH  , Y),
      LW            ->  List( InstI, FuncLSU, LsuOp.LW  , Y),
      LBU           ->  List( InstI, FuncLSU, LsuOp.LBU , Y),
      LHU           ->  List( InstI, FuncLSU, LsuOp.LHU , Y),
      SB            ->  List( InstS, FuncLSU, LsuOp.SB  , N),
      SH            ->  List( InstS, FuncLSU, LsuOp.SH  , N),
      SW            ->  List( InstS, FuncLSU, LsuOp.SW  , N),
      ADDI          ->  List( InstI, FuncALU, AluOp.ADD , Y),
      SLTI          ->  List( InstI, FuncALU, AluOp.SLT , Y),
      SLTIU         ->  List( InstI, FuncALU, AluOp.SLTU, Y),
      XORI          ->  List( InstI, FuncALU, AluOp.XOR , Y),
      ORI           ->  List( InstI, FuncALU, AluOp.OR  , Y),
      ANDI          ->  List( InstI, FuncALU, AluOp.AND , Y),
      SLLI          ->  List( InstI, FuncALU, AluOp.SLL , Y),
      SRLI          ->  List( InstI, FuncALU, AluOp.SRL , Y),
      SRAI          ->  List( InstI, FuncALU, AluOp.SRA , Y),
      ADD           ->  List( InstR, FuncALU, AluOp.ADD , Y),
      SUB           ->  List( InstR, FuncALU, AluOp.SUB , Y),
      SLL           ->  List( InstR, FuncALU, AluOp.SLL , Y),
      SLT           ->  List( InstR, FuncALU, AluOp.SLT , Y),
      SLTU          ->  List( InstR, FuncALU, AluOp.SLTU, Y),
      XOR           ->  List( InstR, FuncALU, AluOp.XOR , Y),
      SRL           ->  List( InstR, FuncALU, AluOp.SRL , Y),
      SRA           ->  List( InstR, FuncALU, AluOp.SRA , Y),
      OR            ->  List( InstR, FuncALU, AluOp.OR  , Y),
      AND           ->  List( InstR, FuncALU, AluOp.AND , Y),
      FENCE         ->  List( InstI, FuncNONE,NoneOp    , N),
      FENCE_I       ->  List( InstI, FuncNONE,NoneOp    , N),
      ECALL         ->  List( InstI, FuncSYSU,NoneOp    , N),
      EBREAK        ->  List( InstI, FuncSYSU,NoneOp    , N),
      CSRRW         ->  List( InstI, FuncCSRU,CsrOp.RW  , Y),
      CSRRS         ->  List( InstI, FuncCSRU,CsrOp.RS  , Y),
      CSRRC         ->  List( InstI, FuncCSRU,CsrOp.RC  , Y),
      CSRRWI        ->  List( InstI, FuncCSRU,CsrOp.RW  , Y),
      CSRRSI        ->  List( InstI, FuncCSRU,CsrOp.RS  , Y),
      CSRRCI        ->  List( InstI, FuncCSRU,CsrOp.RC  , Y),
      MRET          ->  List( InstI, FuncCSRU,CsrOp.MRET, N),

      // todo: add decode for fence, ECall ... CSRop
    )
  }

  object RV64I {
    val DecodeTable : Array[(BitPat, List[UInt])] = Array(
      LWU           ->  List( InstI, FuncLSU, LsuOp.LWU , Y),
      LD            ->  List( InstI, FuncLSU, LsuOp.LD  , Y),

      ADDIW         ->  List( InstI, FuncALU, AluOp.ADD , Y),

      SLLIW         ->  List( InstI, FuncALU, AluOp.SLL , Y),
      SRLIW         ->  List( InstI, FuncALU, AluOp.SRL , Y),
      SRAIW         ->  List( InstI, FuncALU, AluOp.SRA , Y),

      SD            ->  List( InstS, FuncLSU, LsuOp.SD  , N),

      ADDW          ->  List( InstR, FuncALU, AluOp.ADD , Y),
      SUBW          ->  List( InstR, FuncALU, AluOp.SUB , Y),
      SLLW          ->  List( InstR, FuncALU, AluOp.SLL , Y),
      SRLW          ->  List( InstR, FuncALU, AluOp.SRL , Y),
      SRAW          ->  List( InstR, FuncALU, AluOp.SRA , Y),
    )
  }

  object TrapI {
    val DecodeTable : Array[(BitPat, List[UInt])] = Array(
      // 构造TRAP指令时，一般rd会是x0，保险起见，还是把regfile写使能置为N
      Trap.TRAP     -> List( InstT, FuncTrapU, AluOp.ADD, N)
    )
  }

  val DecodeTable : Array[(BitPat, List[UInt])] =
    RV32I.DecodeTable ++ RV64I.DecodeTable ++ TrapI.DecodeTable

  val RsTypeTable = Seq (
    InstN -> Tuple2(Rs1Reg, Rs2Reg),
    InstU -> Tuple2(Rs1PC,  Rs2Imm),
    InstJ -> Tuple2(Rs1PC,  Rs2Imm),
    InstB -> Tuple2(Rs1Reg, Rs2Imm),  // B指令的寄存器数x[rs2]在wdata中传递
    InstI -> Tuple2(Rs1Reg, Rs2Imm),
    InstS -> Tuple2(Rs1Reg, Rs2Imm),
    InstR -> Tuple2(Rs1Reg, Rs2Reg),
    InstT -> Tuple2(Rs1Reg, Rs2Imm)   // 自定义的Trap指令，传递x[0]和立即数
  )
}
