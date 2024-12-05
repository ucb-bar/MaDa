import chisel3._
import chisel3.util._


class SystemJTAGIO extends Bundle {
  val jtag = new JTAGIO()
  val reset = Output(Bool())
}

