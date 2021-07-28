package Core.ISA.RV64I

import Core.ISA.Inst
import chisel3.util.BitPat

object InstS extends Inst {
  // use rd
  //                            |--imm--|-rs2-|-rs1-|func3|--rd-|-opcode-|
  val SD    : BitPat  = BitPat("b???????_?????_?????__011__?????_0100011")
}
