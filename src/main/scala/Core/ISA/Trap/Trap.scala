package Core.ISA.Trap

import chisel3._
import chisel3.util.BitPat

object Trap {
  def GoodTrap : UInt = 0.U
  def BadTrap : UInt = 1.U
  //                          |-----imm----|-rs1-|func3|-imm-|-opcode-|
  val TRAP : BitPat = BitPat("b????????????_?????__000__?????_1101011")
}
