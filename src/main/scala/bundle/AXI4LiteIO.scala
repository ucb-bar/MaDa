import chisel3._
import chisel3.util._


class RawAXI4Lite extends Bundle {
  val aclk = Output(Clock())
  val aresetn = Output(Bool())
  val awaddr = Output(UInt(32.W))
  val awvalid = Output(Bool())
  val awready = Input(Bool())
  val wdata = Output(UInt(32.W))
  val wstrb = Output(UInt(4.W))
  val wvalid = Output(Bool())
  val wready = Input(Bool())
  val bresp = Input(UInt(2.W))
  val bvalid = Input(Bool())
  val bready = Output(Bool())
  val araddr = Output(UInt(32.W))
  val arvalid = Output(Bool())
  val arready = Input(Bool())
  val rdata = Input(UInt(32.W))
  val rresp = Input(UInt(2.W))
  val rvalid = Input(Bool())
  val rready = Output(Bool())
}
