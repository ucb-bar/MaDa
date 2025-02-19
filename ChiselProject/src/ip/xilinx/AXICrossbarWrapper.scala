import chisel3.{BlackBox, _}
import chisel3.util._


class axi_crossbar_0(n: Int = 2) extends BlackBox {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    val aresetn = Input(Reset())
    val s_axi_awaddr = Input(UInt(32.W))
    val s_axi_awprot = Input(UInt(3.W))
    val s_axi_awvalid = Input(Bool())
    val s_axi_awready = Output(Bool())
    val s_axi_wdata = Input(UInt(32.W))
    val s_axi_wstrb = Input(UInt(4.W))
    val s_axi_wvalid = Input(Bool())
    val s_axi_wready = Output(Bool())
    val s_axi_bresp = Output(UInt(2.W))
    val s_axi_bvalid = Output(Bool())
    val s_axi_bready = Input(Bool())
    val s_axi_araddr = Input(UInt(32.W))
    val s_axi_arprot = Input(UInt(3.W))
    val s_axi_arvalid = Input(Bool())
    val s_axi_arready = Output(Bool())
    val s_axi_rdata = Output(UInt(32.W))
    val s_axi_rresp = Output(UInt(2.W))
    val s_axi_rvalid = Output(Bool())
    val s_axi_rready = Input(Bool())

    val m_axi_awaddr = Output(UInt((32*n).W))
    val m_axi_awprot = Output(UInt((3*n).W))
    val m_axi_awvalid = Output(UInt(n.W))
    val m_axi_awready = Input(UInt(n.W))
    val m_axi_wdata = Output(UInt((32*n).W))
    val m_axi_wstrb = Output(UInt((4*n).W))
    val m_axi_wvalid = Output(UInt(n.W))
    val m_axi_wready = Input(UInt(n.W))
    val m_axi_bresp = Input(UInt((2*n).W))
    val m_axi_bvalid = Input(UInt(n.W))
    val m_axi_bready = Output(UInt(n.W))
    val m_axi_araddr = Output(UInt((32*n).W))
    val m_axi_arprot = Output(UInt((3*n).W))
    val m_axi_arvalid = Output(UInt(n.W))
    val m_axi_arready = Input(UInt(n.W))
    val m_axi_rdata = Input(UInt((32*n).W))
    val m_axi_rresp = Input(UInt((2*n).W))
    val m_axi_rvalid = Input(UInt(n.W))
    val m_axi_rready = Output(UInt(n.W))
  })
}


class AXICrossBar extends RawModule {
  val io = IO(new Bundle() {
    val s_axi = Flipped(new RawAXI4Lite())
    val m_axi = Vec(2, new RawAXI4Lite())
  })

  val axi_crossbar_0 = Module(new axi_crossbar_0(2))

  io.m_axi(0).aclk := io.s_axi.aclk
  io.m_axi(1).aclk := io.s_axi.aclk
  io.m_axi(0).aresetn := io.s_axi.aresetn
  io.m_axi(1).aresetn := io.s_axi.aresetn

  axi_crossbar_0.io.aclk := io.s_axi.aclk
  axi_crossbar_0.io.aresetn := io.s_axi.aresetn

  axi_crossbar_0.io.s_axi_awaddr := io.s_axi.awaddr
  axi_crossbar_0.io.s_axi_awprot := 0.U(3.W)
  axi_crossbar_0.io.s_axi_awvalid := io.s_axi.awvalid
  io.s_axi.awready := axi_crossbar_0.io.s_axi_awready

  axi_crossbar_0.io.s_axi_wdata := io.s_axi.wdata
  axi_crossbar_0.io.s_axi_wstrb := io.s_axi.wstrb
  axi_crossbar_0.io.s_axi_wvalid := io.s_axi.wvalid
  io.s_axi.wready := axi_crossbar_0.io.s_axi_wready

  io.s_axi.bresp := axi_crossbar_0.io.s_axi_bresp
  io.s_axi.bvalid := axi_crossbar_0.io.s_axi_bvalid
  axi_crossbar_0.io.s_axi_bready := io.s_axi.bready

  axi_crossbar_0.io.s_axi_araddr := io.s_axi.araddr
  axi_crossbar_0.io.s_axi_arprot := 0.U(3.W)
  axi_crossbar_0.io.s_axi_arvalid := io.s_axi.arvalid
  io.s_axi.arready := axi_crossbar_0.io.s_axi_arready

  io.s_axi.rdata := axi_crossbar_0.io.s_axi_rdata
  io.s_axi.rresp := axi_crossbar_0.io.s_axi_rresp
  io.s_axi.rvalid := axi_crossbar_0.io.s_axi_rvalid
  axi_crossbar_0.io.s_axi_rready := io.s_axi.rready


  io.m_axi(0).awaddr := axi_crossbar_0.io.m_axi_awaddr(31, 0)
  io.m_axi(1).awaddr := axi_crossbar_0.io.m_axi_awaddr(63, 32)
  // io.m_axi(0).awprot := axi_crossbar_0.io.m_axi_awprot(2, 0)
  // io.m_axi(1).awprot := axi_crossbar_0.io.m_axi_awprot(4, 2)
  io.m_axi(0).awvalid := axi_crossbar_0.io.m_axi_awvalid(0)
  io.m_axi(1).awvalid := axi_crossbar_0.io.m_axi_awvalid(1)
  axi_crossbar_0.io.m_axi_awready := Cat(VecInit(io.m_axi.reverse.map(_.awready)))

  // w bundle
  io.m_axi.zipWithIndex.foreach { case (m_axi, i) =>
    m_axi.wdata := axi_crossbar_0.io.m_axi_wdata(32*i + 31, 32*i)
    m_axi.wstrb := axi_crossbar_0.io.m_axi_wstrb(2*i + 1, 2*i)
    m_axi.wvalid := axi_crossbar_0.io.m_axi_wvalid(i)
  }

  axi_crossbar_0.io.m_axi_wready := Cat(VecInit(io.m_axi.reverse.map(_.wready)))

  axi_crossbar_0.io.m_axi_bresp := Cat(VecInit(io.m_axi.reverse.map(_.bresp)))
  axi_crossbar_0.io.m_axi_bvalid := Cat(VecInit(io.m_axi.reverse.map(_.bvalid)))
  io.m_axi(0).bready := axi_crossbar_0.io.m_axi_bready(0)
  io.m_axi(1).bready := axi_crossbar_0.io.m_axi_bready(1)
  
  io.m_axi(0).araddr := axi_crossbar_0.io.m_axi_araddr(31, 0)
  io.m_axi(1).araddr := axi_crossbar_0.io.m_axi_araddr(63, 32)
  // io.m_axi(0).arprot := axi_crossbar_0.io.m_axi_arprot(2, 0)
  // io.m_axi(1).arprot := axi_crossbar_0.io.m_axi_arprot(4, 2)
  io.m_axi(0).arvalid := axi_crossbar_0.io.m_axi_arvalid(0)
  io.m_axi(1).arvalid := axi_crossbar_0.io.m_axi_arvalid(1)
  axi_crossbar_0.io.m_axi_arready := Cat(VecInit(io.m_axi.reverse.map(_.arready)))

  axi_crossbar_0.io.m_axi_rdata := Cat(VecInit(io.m_axi.reverse.map(_.rdata)))
  axi_crossbar_0.io.m_axi_rresp := Cat(VecInit(io.m_axi.reverse.map(_.rresp)))
  axi_crossbar_0.io.m_axi_rvalid := Cat(VecInit(io.m_axi.reverse.map(_.rvalid)))
  io.m_axi(0).rready := axi_crossbar_0.io.m_axi_rready(0)
  io.m_axi(1).rready := axi_crossbar_0.io.m_axi_rready(1)

}


// class AXICrossBar extends BlackBox with HasBlackBoxInline {
//   val io = IO(new Bundle() {
//     val s_axi = Flipped(new RawAXI4Lite())
//     val m_axi = Vec(2, new RawAXI4Lite())
//   })
//   setInline("AXICrossBarWrapper.v",
//     s"""
//       |module AXICrossBar(
//       |    input          s_axi_aclk,
//       |    input          s_axi_aresetn,
//       |    input  [31:0]  s_axi_awaddr,
//       |    input          s_axi_awvalid,
//       |    output         s_axi_awready,
//       |    input  [31:0]  s_axi_wdata,
//       |    input  [3:0]   s_axi_wstrb,
//       |    input          s_axi_wvalid,
//       |    output         s_axi_wready,
//       |    output [1:0]   s_axi_bresp,
//       |    output         s_axi_bvalid,
//       |    input          s_axi_bready,
//       |    input  [31:0]  s_axi_araddr,
//       |    input          s_axi_arvalid,
//       |    output         s_axi_arready,
//       |    output [31:0]  s_axi_rdata,
//       |    output [1:0]   s_axi_rresp,
//       |    output         s_axi_rvalid,
//       |    input          s_axi_rready,
//       |
//       |    output         m_axi_0_aclk,
//       |    output         m_axi_0_aresetn,
//       |    output [31:0]  m_axi_0_awaddr,
//       |    output         m_axi_0_awvalid,
//       |    input          m_axi_0_awready,
//       |    output [31:0]  m_axi_0_wdata,
//       |    output [3:0]   m_axi_0_wstrb,
//       |    output         m_axi_0_wvalid,
//       |    input          m_axi_0_wready,
//       |    output [1:0]   m_axi_0_bresp,
//       |    output         m_axi_0_bvalid,
//       |    input          m_axi_0_bready,
//       |    output [31:0]  m_axi_0_araddr,
//       |    output         m_axi_0_arvalid,
//       |    input          m_axi_0_arready,
//       |    input  [31:0]  m_axi_0_rdata,
//       |    input  [1:0]   m_axi_0_rresp,
//       |    input          m_axi_0_rvalid,
//       |    output         m_axi_0_rready,
//       |
//       |    output         m_axi_1_aclk,
//       |    output         m_axi_1_aresetn,
//       |    output [31:0]  m_axi_1_awaddr,
//       |    output         m_axi_1_awvalid,
//       |    input          m_axi_1_awready,
//       |    output [31:0]  m_axi_1_wdata,
//       |    output [3:0]   m_axi_1_wstrb,
//       |    output         m_axi_1_wvalid,
//       |    input          m_axi_1_wready,
//       |    output [1:0]   m_axi_1_bresp,
//       |    output         m_axi_1_bvalid,
//       |    input          m_axi_1_bready,
//       |    output [31:0]  m_axi_1_araddr,
//       |    output         m_axi_1_arvalid,
//       |    input          m_axi_1_arready,
//       |    input  [31:0]  m_axi_1_rdata,
//       |    input  [1:0]   m_axi_1_rresp,
//       |    input          m_axi_1_rvalid,
//       |    output         m_axi_1_rready
//       |);
//       |
//       |  assign m_axi_0_aclk = s_axi_aclk;
//       |  assign m_axi_0_aresetn = s_axi_aresetn;
//       |  assign m_axi_1_aclk = s_axi_aclk;
//       |  assign m_axi_1_aresetn = s_axi_aresetn;
//       |
//       |  axi_crossbar_0 u_axi_crossbar_0 (
//       |    .aclk(s_axi_aclk),
//       |    .aresetn(s_axi_aresetn),
//       |    .s_axi_awaddr(s_axi_awaddr),
//       |    .s_axi_awprot('b000_000),
//       |    .s_axi_awvalid(s_axi_awvalid),
//       |    .s_axi_awready(s_axi_awready),
//       |    .s_axi_wdata(s_axi_wdata),
//       |    .s_axi_wstrb(s_axi_wstrb),
//       |    .s_axi_wvalid(s_axi_wvalid),
//       |    .s_axi_wready(s_axi_wready),
//       |    .s_axi_bresp(s_axi_bresp),
//       |    .s_axi_bvalid(s_axi_bvalid),
//       |    .s_axi_bready(s_axi_bready),
//       |    .s_axi_araddr(s_axi_araddr),
//       |    .s_axi_arprot('b000_000),
//       |    .s_axi_arvalid(s_axi_arvalid),
//       |    .s_axi_arready(s_axi_arready),
//       |    .s_axi_rdata(s_axi_rdata),
//       |    .s_axi_rresp(s_axi_rresp),
//       |    .s_axi_rvalid(s_axi_rvalid),
//       |    .s_axi_rready(s_axi_rready),
//       |
//       |    .m_axi_awaddr({m_axi_1_awaddr, m_axi_0_awaddr}),
//       |    .m_axi_awprot({m_axi_1_awprot, m_axi_0_awprot}),
//       |    .m_axi_awvalid({m_axi_1_awvalid, m_axi_0_awvalid}),
//       |    .m_axi_awready({m_axi_1_awready, m_axi_0_awready}),
//       |    .m_axi_wdata({m_axi_1_wdata, m_axi_0_wdata}),
//       |    .m_axi_wstrb({m_axi_1_wstrb, m_axi_0_wstrb}),
//       |    .m_axi_wvalid({m_axi_1_wvalid, m_axi_0_wvalid}),
//       |    .m_axi_wready({m_axi_1_wready, m_axi_0_wready}),
//       |    .m_axi_bresp({m_axi_1_bresp, m_axi_0_bresp}),
//       |    .m_axi_bvalid({m_axi_1_bvalid, m_axi_0_bvalid}),
//       |    .m_axi_bready({m_axi_1_bready, m_axi_0_bready}),
//       |    .m_axi_araddr({m_axi_1_araddr, m_axi_0_araddr}),
//       |    .m_axi_arprot({m_axi_1_arprot, m_axi_0_arprot}),
//       |    .m_axi_arvalid({m_axi_1_arvalid, m_axi_0_arvalid}),
//       |    .m_axi_arready({m_axi_1_arready, m_axi_0_arready}),
//       |    .m_axi_rdata({m_axi_1_rdata, m_axi_0_rdata}),
//       |    .m_axi_rresp({m_axi_1_rresp, m_axi_0_rresp}),
//       |    .m_axi_rvalid({m_axi_1_rvalid, m_axi_0_rvalid}),
//       |    .m_axi_rready({m_axi_1_rready, m_axi_0_rready})
//       |  );
//       |
//       |endmodule
//     """.stripMargin)
// }