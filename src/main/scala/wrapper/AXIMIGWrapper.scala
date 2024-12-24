import chisel3.{BlackBox, withClockAndReset, _}
import chisel3.experimental.{Analog, attach}
import chisel3.util._
import chisel3.SpecifiedDirection.Flip


class Arty100TMIGIODDR extends Bundle {
  val ddr3_addr             = Output(Bits(14.W))
  val ddr3_ba               = Output(Bits(3.W))
  val ddr3_ras_n            = Output(Bool())
  val ddr3_cas_n            = Output(Bool())
  val ddr3_we_n             = Output(Bool())
  val ddr3_reset_n          = Output(Bool())
  val ddr3_ck_p             = Output(Bits(1.W))
  val ddr3_ck_n             = Output(Bits(1.W))
  val ddr3_cke              = Output(Bits(1.W))
  val ddr3_cs_n             = Output(Bits(1.W))
  val ddr3_dm               = Output(Bits(2.W))
  val ddr3_odt              = Output(Bits(1.W))
  
  val ddr3_dq               = Analog(16.W)
  val ddr3_dqs_n            = Analog(2.W)
  val ddr3_dqs_p            = Analog(2.W)
}

trait Arty100TMIGIOClocksReset extends Bundle {
  //inputs
  //"NO_BUFFER" clock source (must be connected to IBUF outside of IP)
  val sys_clk_i             = Input(Bool())
  val clk_ref_i             = Input(Bool())
  //user interface signals
  val ui_clk                = Output(Clock())
  val ui_clk_sync_rst       = Output(Bool())
  val mmcm_locked           = Output(Bool())
  val aresetn               = Input(Bool())
  //misc
  val init_calib_complete   = Output(Bool())
  val sys_rst               = Input(Bool())
}

class XilinxArty100TMIGIO extends Arty100TMIGIODDR with Arty100TMIGIOClocksReset

class arty100tmig extends BlackBox
{
  val io = IO(new Arty100TMIGIODDR with Arty100TMIGIOClocksReset {
    // User interface signals
    val app_sr_req            = Input(Bool())
    val app_ref_req           = Input(Bool())
    val app_zq_req            = Input(Bool())
    val app_sr_active         = Output(Bool())
    val app_ref_ack           = Output(Bool())
    val app_zq_ack            = Output(Bool())
    //axi_s
    //slave interface write address ports
    val s_axi_awid            = Input(Bits(4.W))
    val s_axi_awaddr          = Input(Bits(32.W))
    val s_axi_awlen           = Input(Bits(8.W))
    val s_axi_awsize          = Input(Bits(3.W))
    val s_axi_awburst         = Input(Bits(2.W))
    val s_axi_awlock          = Input(Bits(1.W))
    val s_axi_awcache         = Input(Bits(4.W))
    val s_axi_awprot          = Input(Bits(3.W))
    val s_axi_awqos           = Input(Bits(4.W))
    val s_axi_awvalid         = Input(Bool())
    val s_axi_awready         = Output(Bool())
    //slave interface write data ports
    val s_axi_wdata           = Input(Bits(64.W))
    val s_axi_wstrb           = Input(Bits(8.W))
    val s_axi_wlast           = Input(Bool())
    val s_axi_wvalid          = Input(Bool())
    val s_axi_wready          = Output(Bool())
    //slave interface write response ports
    val s_axi_bready          = Input(Bool())
    val s_axi_bid             = Output(Bits(4.W))
    val s_axi_bresp           = Output(Bits(2.W))
    val s_axi_bvalid          = Output(Bool())
    //slave interface read address ports
    val s_axi_arid            = Input(Bits(4.W))
    val s_axi_araddr          = Input(Bits(32.W))
    val s_axi_arlen           = Input(Bits(8.W))
    val s_axi_arsize          = Input(Bits(3.W))
    val s_axi_arburst         = Input(Bits(2.W))
    val s_axi_arlock          = Input(Bits(1.W))
    val s_axi_arcache         = Input(Bits(4.W))
    val s_axi_arprot          = Input(Bits(3.W))
    val s_axi_arqos           = Input(Bits(4.W))
    val s_axi_arvalid         = Input(Bool())
    val s_axi_arready         = Output(Bool())
    //slave interface read data ports
    val s_axi_rready          = Input(Bool())
    val s_axi_rid             = Output(Bits(4.W))
    val s_axi_rdata           = Output(Bits(64.W))
    val s_axi_rresp           = Output(Bits(2.W))
    val s_axi_rlast           = Output(Bool())
    val s_axi_rvalid          = Output(Bool())
    //misc
    val device_temp           = Output(Bits(12.W))
  })
}

class XilinxArty100TMIG extends RawModule {
  val io = IO(new Bundle {
    val port = new Arty100TMIGIODDR with Arty100TMIGIOClocksReset
    val s_axi = Flipped(new AXI4())
  })

  val blackbox = withClockAndReset(io.port.ui_clk, io.port.ui_clk_sync_rst) {Module(new arty100tmig)}
  
  val offset = 0x80000000L

  //inouts
  attach(io.port.ddr3_dq,blackbox.io.ddr3_dq)
  attach(io.port.ddr3_dqs_n,blackbox.io.ddr3_dqs_n)
  attach(io.port.ddr3_dqs_p,blackbox.io.ddr3_dqs_p)

  //outputs
  io.port.ddr3_addr         := blackbox.io.ddr3_addr
  io.port.ddr3_ba           := blackbox.io.ddr3_ba
  io.port.ddr3_ras_n        := blackbox.io.ddr3_ras_n
  io.port.ddr3_cas_n        := blackbox.io.ddr3_cas_n
  io.port.ddr3_we_n         := blackbox.io.ddr3_we_n
  io.port.ddr3_reset_n      := blackbox.io.ddr3_reset_n
  io.port.ddr3_ck_p         := blackbox.io.ddr3_ck_p
  io.port.ddr3_ck_n         := blackbox.io.ddr3_ck_n
  io.port.ddr3_cke          := blackbox.io.ddr3_cke
  io.port.ddr3_cs_n         := blackbox.io.ddr3_cs_n
  io.port.ddr3_dm           := blackbox.io.ddr3_dm
  io.port.ddr3_odt          := blackbox.io.ddr3_odt

  io.port.init_calib_complete := blackbox.io.init_calib_complete

 //inputs
  //NO_BUFFER clock
  blackbox.io.sys_clk_i     := io.port.sys_clk_i
  blackbox.io.clk_ref_i     := io.port.clk_ref_i

  io.port.ui_clk            := blackbox.io.ui_clk
  io.port.ui_clk_sync_rst   := blackbox.io.ui_clk_sync_rst
  io.port.mmcm_locked       := blackbox.io.mmcm_locked
  blackbox.io.aresetn       := io.port.aresetn
  blackbox.io.app_sr_req    := false.B
  blackbox.io.app_ref_req   := false.B
  blackbox.io.app_zq_req    := false.B
  //app_sr_active           := unconnected
  //app_ref_ack             := unconnected
  //app_zq_ack              := unconnected

  val awaddr = io.s_axi.aw.bits.addr - offset.U
  val araddr = io.s_axi.ar.bits.addr - offset.U

  //slave AXI interface write address ports
  blackbox.io.s_axi_awid    := io.s_axi.aw.bits.id
  blackbox.io.s_axi_awaddr  := awaddr //truncated
  blackbox.io.s_axi_awlen   := io.s_axi.aw.bits.len
  blackbox.io.s_axi_awsize  := io.s_axi.aw.bits.size
  blackbox.io.s_axi_awburst := io.s_axi.aw.bits.burst
  blackbox.io.s_axi_awlock  := io.s_axi.aw.bits.lock
  blackbox.io.s_axi_awcache := "b0011".U
  blackbox.io.s_axi_awprot  := io.s_axi.aw.bits.prot
  blackbox.io.s_axi_awqos   := io.s_axi.aw.bits.qos
  blackbox.io.s_axi_awvalid := io.s_axi.aw.valid
  io.s_axi.aw.ready          := blackbox.io.s_axi_awready

  //slave interface write data ports
  blackbox.io.s_axi_wdata   := io.s_axi.w.bits.data
  blackbox.io.s_axi_wstrb   := io.s_axi.w.bits.strb
  blackbox.io.s_axi_wlast   := io.s_axi.w.bits.last
  blackbox.io.s_axi_wvalid  := io.s_axi.w.valid
  io.s_axi.w.ready          := blackbox.io.s_axi_wready

  //slave interface write response
  blackbox.io.s_axi_bready  := io.s_axi.b.ready
  io.s_axi.b.bits.id        := blackbox.io.s_axi_bid
  io.s_axi.b.bits.resp      := blackbox.io.s_axi_bresp
  io.s_axi.b.valid          := blackbox.io.s_axi_bvalid

  //slave AXI interface read address ports
  blackbox.io.s_axi_arid    := io.s_axi.ar.bits.id
  blackbox.io.s_axi_araddr  := araddr // truncated
  blackbox.io.s_axi_arlen   := io.s_axi.ar.bits.len
  blackbox.io.s_axi_arsize  := io.s_axi.ar.bits.size
  blackbox.io.s_axi_arburst := io.s_axi.ar.bits.burst
  blackbox.io.s_axi_arlock  := io.s_axi.ar.bits.lock
  blackbox.io.s_axi_arcache := "b0011".U
  blackbox.io.s_axi_arprot  := io.s_axi.ar.bits.prot
  blackbox.io.s_axi_arqos   := io.s_axi.ar.bits.qos
  blackbox.io.s_axi_arvalid := io.s_axi.ar.valid
  io.s_axi.ar.ready          := blackbox.io.s_axi_arready

  //slace AXI interface read data ports
  blackbox.io.s_axi_rready  := io.s_axi.r.ready
  io.s_axi.r.bits.id       := blackbox.io.s_axi_rid
  io.s_axi.r.bits.data     := blackbox.io.s_axi_rdata
  io.s_axi.r.bits.resp     := blackbox.io.s_axi_rresp
  io.s_axi.r.bits.last     := blackbox.io.s_axi_rlast
  io.s_axi.r.valid          := blackbox.io.s_axi_rvalid 
}
