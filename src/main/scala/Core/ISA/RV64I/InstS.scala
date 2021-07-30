package Core.ISA.RV64I

import chisel3.util.BitPat

trait InstS extends Core.ISA.RV32I.InstS {
  // use rd
  //                            |--imm--|-rs2-|-rs1-|func3|--rd-|-opcode-|
  val SD    : BitPat  = BitPat("b???????_?????_?????__011__?????_0100011")
}
