package Core

import chisel3._

class TopIO extends Bundle {
  val imem : MemReadPort = Flipped(new MemReadPort)
  val dmem : MemIO = Flipped(new MemIO)
}

class Top extends Module {
  val io = new TopIO
  val ifu = new InstFetchUnit
  val idu = new InstDecodeUnit
  val exu = new ExecuteUnit
  val wbu = new WriteBackUnit
  val data_path = new DataPathUnit

  ifu.io.imem           <> io.imem
  ifu.io.to_idu         <> idu.io.in
  idu.io.out            <> data_path.io.from_idu
  data_path.io.to_exu   <> exu.io.in
  exu.io.out            <> wbu.io.in
  wbu.io.out            <> data_path.io.from_wbu
  exu.io.dmem           <> io.dmem
}
