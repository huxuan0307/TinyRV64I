package Core.ISA.RV32I

import chisel3.util.BitPat

trait InstB{
  // use rs2 and rs1
  // not use rd
  //                          |--imm--|-rs2-|-rs1-|func3|-imm-|-opcode-|
  val BEQ : BitPat =  BitPat("b???????_?????_?????__000__?????_1100011")
  val BNE : BitPat =  BitPat("b???????_?????_?????__001__?????_1100011")
  val BLT : BitPat =  BitPat("b???????_?????_?????__100__?????_1100011")
  val BGE : BitPat =  BitPat("b???????_?????_?????__101__?????_1100011")
  val BLTU: BitPat =  BitPat("b???????_?????_?????__110__?????_1100011")
  val BGEU: BitPat =  BitPat("b???????_?????_?????__111__?????_1100011")
}
