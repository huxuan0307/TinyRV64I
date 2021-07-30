package Core.ISA.RV64I

import chisel3.util.BitPat

trait InstR extends Core.ISA.RV32I.InstR {
  val ADDW : BitPat =  BitPat("b0000000_?????_?????__000__?????_0111011")
  val SUBW : BitPat =  BitPat("b0100000_?????_?????__000__?????_0111011")
  val SLLW : BitPat =  BitPat("b0000000_?????_?????__001__?????_0111011")
  val SRLW : BitPat =  BitPat("b0000000_?????_?????__101__?????_0111011")
  val SRAW : BitPat =  BitPat("b0100000_?????_?????__101__?????_0111011")
}
