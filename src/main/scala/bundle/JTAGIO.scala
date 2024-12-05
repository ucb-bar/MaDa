import chisel3._
import chisel3.util._


class JTAGIO extends Bundle {
  val TCK = Output(Bool())
  val TMS = Output(Bool())
  val TDI = Output(Bool())
  val TDO = Input(new Tristate())
}

