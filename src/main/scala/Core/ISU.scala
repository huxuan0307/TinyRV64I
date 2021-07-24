package Core

import chisel3._

class ISU_IO extends Bundle{

}

class ISU extends Module {
  val io: ISU_IO = IO(new ISU_IO)
}
