package Core.ISA.RV32I

import chisel3.util.BitPat

trait InstJ {
  //                        |-----------imm----------|--rd-|-opcode-|
  val JAL: BitPat = BitPat("b????_????_????_????_????_?????_1101111")
}
