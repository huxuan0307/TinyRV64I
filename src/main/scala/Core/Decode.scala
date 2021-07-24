package Core

import chisel3._
import ISA.RV32I.InstI._
import ISA.RV32I.InstJ._
import ISA.RV32I.InstU._
import ISA.RV32I.InstB._
import ISA.RV32I.InstR._
import ISA.RV32I.InstS._
import chisel3.util.BitPat

trait Inst

object Decode
  extends HasFuncType
    with HasInstType
    with HasRs1Type
    with HasRs2Type
    with BasicDefine
{
  private val NoneOp : UInt = "b0000".U
  object AluOp {
    val ADD : UInt  = "b0000".U
    val SLL : UInt  = "b0001".U
    val SLT : UInt  = "b0010".U
    val SLTU: UInt  = "b0011".U
    val XOR : UInt  = "b0100".U
    val SRL : UInt  = "b0101".U
    val OR  : UInt  = "b0110".U
    val AND : UInt  = "b0111".U
    val SUB : UInt  = "b1000".U | ADD
    val SRA : UInt  = "b1000".U | SRL
    val LUI : UInt  = "b1111".U
  }

  object LsuOp {
    val LB  :UInt  = "b0000".U
    val LH  :UInt  = "b0001".U
    val LW  :UInt  = "b0010".U
    val LBU :UInt  = "b0100".U
    val LHU :UInt  = "b0101".U
    val SB  :UInt  = "b1000".U
    val SH  :UInt  = "b1001".U
    val SW  :UInt  = "b1010".U
  }

  object BruOp {
    val BEQ  :UInt = "b0000".U
    val BNE  :UInt = "b0001".U
    val BLT  :UInt = "b0100".U
    val BGE  :UInt = "b0101".U
    val BLTU :UInt = "b0110".U
    val BGEU :UInt = "b0111".U
    val JAL  :UInt = "b1000".U
    val JALR :UInt = "b1001".U
  }

  //                                                    rd_enable
  //                      | Inst | Func   |     Op    |
  val DecodeDefault : List[UInt] = List( InstN, FuncALU, AluOp.ADD , Y)
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
    CSRRW         ->  List( InstI, FuncCSRU,NoneOp    , Y),
    CSRRS         ->  List( InstI, FuncCSRU,NoneOp    , Y),
    CSRRC         ->  List( InstI, FuncCSRU,NoneOp    , Y),
    CSRRWI        ->  List( InstI, FuncCSRU,NoneOp    , Y),
    CSRRSI        ->  List( InstI, FuncCSRU,NoneOp    , Y),
    CSRRCI        ->  List( InstI, FuncCSRU,NoneOp    , Y)
    // todo: add decode for fence, ECall ... CSRop
  )

  val RsTypeTable = Seq (
    InstN -> Tuple2(Rs1Reg, Rs2Reg),
    InstU -> Tuple2(Rs1PC,  Rs2Imm),
    InstJ -> Tuple2(Rs1PC,  Rs2Imm),
    InstB -> Tuple2(Rs1Reg, Rs2Imm),
    InstI -> Tuple2(Rs1Reg, Rs2Imm),
    InstS -> Tuple2(Rs1Reg, Rs2Imm),
    InstR -> Tuple2(Rs1Reg, Rs2Reg)
  )
}
