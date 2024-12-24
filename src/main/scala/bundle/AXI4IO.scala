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

class AXI4LiteAW extends Bundle {
  val id = Output(UInt(4.W))
  val addr = Output(UInt(32.W))
  val len = Output(UInt(8.W))
  val size = Output(UInt(3.W))
  val burst = Output(UInt(2.W))
  val lock = Output(Bool())
  val cache = Output(UInt(4.W))
  val prot = Output(UInt(3.W))
  val qos = Output(UInt(4.W))
}

class AXI4LiteW extends Bundle {
  val data = Output(UInt(64.W))
  val strb = Output(UInt(8.W))
  val last = Output(Bool())
}

class AXI4LiteB extends Bundle {
  val id = Input(UInt(4.W))
  val resp = Input(UInt(2.W))
}

class AXI4LiteAR extends Bundle {
  val id = Output(UInt(4.W))
  val addr = Output(UInt(32.W))
  val len = Output(UInt(8.W))
  val size = Output(UInt(3.W))
  val burst = Output(UInt(2.W))
  val lock = Output(Bool())
  val cache = Output(UInt(4.W))
  val prot = Output(UInt(3.W))
  val qos = Output(UInt(4.W))
}

class AXI4LiteR extends Bundle {
  val id = Input(UInt(4.W))
  val data = Input(UInt(64.W))
  val resp = Input(UInt(2.W))
  val last = Input(Bool())
}

class AXI4 extends Bundle {
  val aw = new DecoupledIO(new AXI4LiteAW)
  val w = new DecoupledIO(new AXI4LiteW)
  val b = Flipped(new DecoupledIO(new AXI4LiteB))
  val ar = new DecoupledIO(new AXI4LiteAR)
  val r = Flipped(new DecoupledIO(new AXI4LiteR))
}
