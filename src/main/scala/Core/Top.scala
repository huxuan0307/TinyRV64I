package Core

import Core.Bundles.RegfileDebugIO
import chisel3._

class TopIO extends Bundle {
  val imem : MemReadPort = Flipped(new MemReadPort)
  val dmem : MemIO = Flipped(new MemIO)
  val ill_inst : Bool = Output(Bool())
  val debug = new RegfileDebugIO
}

class Top extends Module {
  val io : TopIO            = IO(new TopIO)
  val data_path : DataPathUnit = Module(new DataPathUnit)
  val ifu : InstFetchUnit = Module(new InstFetchUnit)
  val wbu : WriteBackUnit = Module(new WriteBackUnit)
  val idu : InstDecodeUnit = Module(new InstDecodeUnit)
  val exu : ExecuteUnit = Module(new ExecuteUnit)

  ifu.io.imem           <> io.imem
  ifu.io.to_idu         <> idu.io.in
  idu.io.out            <> data_path.io.from_idu
  data_path.io.to_exu   <> exu.io.in
  exu.io.out            <> wbu.io.in
  wbu.io.out            <> data_path.io.from_wbu
  exu.io.dmem           <> io.dmem
  io.ill_inst           := idu.io.illegal
  io.debug              <> data_path.io.debug

}
