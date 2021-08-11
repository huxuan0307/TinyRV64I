package Core.EXU.CSR.Define

import Core.Config.CoreConfig.ISAEXT
import chisel3.{UInt, fromStringToLiteral, fromIntToWidth}

object Privilege {
  object Level {
    def U : UInt = "b00".U(2.W)
    def S : UInt = "b01".U(2.W)
    def H : UInt = "b10".U(2.W)
    def M : UInt = "b11".U(2.W)
  }
  object Access {
    def RW : UInt = "b00".U(2.W)
    def RO : UInt = "b11".U(2.W)
  }
  object FieldSpec {
    /** Reserved Writed Preserve Values, Reads Ingore Values */
    def WPRI : UInt = "b00".U(2.W)
    /** Write/Read Only Legal Values */
    def WLRL : UInt = "b01".U(2.W)
    /** Write Any Values, Reads Legal Values */
    def WARL : UInt = "b11".U(2.W)
  }
  def supportUser : Boolean = ISAEXT.support('U')
  def supportSupervisor : Boolean = ISAEXT.support('S')
}
