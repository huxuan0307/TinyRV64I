package Core.ISA

import chisel3.util._

object CSR {
  // use imm12 and rs1
  // use rd
  //                              |------------|-----|func3|--rd-|-opcode-|
  val ECALL   : BitPat  = BitPat("b000000000000_00000__000__00000_1110011")
  val EBREAK  : BitPat  = BitPat("b000000000001_00000__000__00000_1110011")
  val MRET    : BitPat  = BitPat("b001100000010_00000__000__00000_1110011")
  val SRET    : BitPat  = BitPat("b000100000010_00000__000__00000_1110011")
  val WFI     : BitPat  = BitPat("b000100000101_00000__000__00000_1110011")
  //                              |----CSR-----|-rs1-|func3|--rd-|-opcode-|
  val CSRRW   : BitPat  = BitPat("b????????????_?????__001__?????_1110011")
  val CSRRS   : BitPat  = BitPat("b????????????_?????__010__?????_1110011")
  val CSRRC   : BitPat  = BitPat("b????????????_?????__011__?????_1110011")
  //                              |----CSR-----|-zimm|func3|--rd-|-opcode-|
  val CSRRWI  : BitPat  = BitPat("b????????????_?????__101__?????_1110011")
  val CSRRSI  : BitPat  = BitPat("b????????????_?????__110__?????_1110011")
  val CSRRCI  : BitPat  = BitPat("b????????????_?????__111__?????_1110011")
  //                            |-func7-|shamt|-rs1-|func3|--rd-|-opcode-|

}
