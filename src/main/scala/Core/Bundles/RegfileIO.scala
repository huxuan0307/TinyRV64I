package Core.Bundles

import chisel3._

class RegfileIO extends Bundle{
  val r1 = new ReadEnaIO
  val r2 = new ReadEnaIO
  val w = new WriteEnaIO
}
