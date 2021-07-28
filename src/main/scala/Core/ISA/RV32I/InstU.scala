package Core.ISA.RV32I

import Core.ISA.Inst
import chisel3.util.BitPat

case object InstU extends Inst {
  // use rd
  //                            |-----------imm----------|--rd-|-opcode-|
  val LUI   : BitPat  = BitPat("b????_????_????_????_????_?????_0110111")
  val AUIPC : BitPat  = BitPat("b????_????_????_????_????_?????_0010111")
}
