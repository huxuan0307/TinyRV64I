package Core

import chisel3._
import ISA.RV32I.InstI._
import ISA.RV32I.InstJ._
import ISA.RV32I.InstU._
import ISA.RV32I.InstB._
import ISA.RV32I.InstR._
import ISA.RV32I.InstS._

trait Inst

object Decode extends HasFuncType with HasInstType {

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

  //                          Inst | Func   | Op        |
  val DecodeDefault: (UInt, UInt, UInt) = Tuple3( InstN, FuncALU, AluOp.ADD )

  val DecodeTable = Map(
    LUI           ->  Tuple3( InstU, FuncALU, AluOp.LUI ),
    AUIPC         ->  Tuple3( InstU, FuncALU, AluOp.ADD ),
    JAL           ->  Tuple3( InstJ, FuncBRU, BruOp.JAL ),
    JALR          ->  Tuple3( InstI, FuncBRU, BruOp.JALR),
    BEQ           ->  Tuple3( InstB, FuncBRU, BruOp.BEQ ),
    BNE           ->  Tuple3( InstB, FuncBRU, BruOp.BNE ),
    BLT           ->  Tuple3( InstB, FuncBRU, BruOp.BLT ),
    BGE           ->  Tuple3( InstB, FuncBRU, BruOp.BGE ),
    BLTU          ->  Tuple3( InstB, FuncBRU, BruOp.BLTU),
    BGEU          ->  Tuple3( InstB, FuncBRU, BruOp.BGEU),
    LB            ->  Tuple3( InstI, FuncLSU, LsuOp.LB  ),
    LH            ->  Tuple3( InstI, FuncLSU, LsuOp.LH  ),
    LW            ->  Tuple3( InstI, FuncLSU, LsuOp.LW  ),
    LBU           ->  Tuple3( InstI, FuncLSU, LsuOp.LBU ),
    LHU           ->  Tuple3( InstI, FuncLSU, LsuOp.LHU ),
    SB            ->  Tuple3( InstS, FuncLSU, LsuOp.SB  ),
    SH            ->  Tuple3( InstS, FuncLSU, LsuOp.SH  ),
    SW            ->  Tuple3( InstS, FuncLSU, LsuOp.SW  ),
    ADDI          ->  Tuple3( InstI, FuncALU, AluOp.ADD ),
    SLTI          ->  Tuple3( InstI, FuncALU, AluOp.SLT ),
    SLTIU         ->  Tuple3( InstI, FuncALU, AluOp.SLTU),
    XORI          ->  Tuple3( InstI, FuncALU, AluOp.XOR ),
    ORI           ->  Tuple3( InstI, FuncALU, AluOp.OR  ),
    ANDI          ->  Tuple3( InstI, FuncALU, AluOp.AND ),
    SLLI          ->  Tuple3( InstI, FuncALU, AluOp.SLL ),
    SRLI          ->  Tuple3( InstI, FuncALU, AluOp.SRL ),
    SRAI          ->  Tuple3( InstI, FuncALU, AluOp.SRA ),
    ADD           ->  Tuple3( InstR, FuncALU, AluOp.ADD ),
    SUB           ->  Tuple3( InstR, FuncALU, AluOp.SUB ),
    SLL           ->  Tuple3( InstR, FuncALU, AluOp.SLL ),
    SLT           ->  Tuple3( InstR, FuncALU, AluOp.SLT ),
    SLTU          ->  Tuple3( InstR, FuncALU, AluOp.SLTU),
    XOR           ->  Tuple3( InstR, FuncALU, AluOp.XOR ),
    SRL           ->  Tuple3( InstR, FuncALU, AluOp.SRL ),
    SRA           ->  Tuple3( InstR, FuncALU, AluOp.SRA ),
    OR            ->  Tuple3( InstR, FuncALU, AluOp.OR  ),
    AND           ->  Tuple3( InstR, FuncALU, AluOp.AND )
    // todo: add decode for fence, ECall ... CSRop
  )
}
