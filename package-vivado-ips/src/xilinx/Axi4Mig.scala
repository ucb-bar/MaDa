package vivadoips

import os.Path
import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, attach}
import chisel3.SpecifiedDirection.Flip

import builder.{addConstraintResource, addVivadoIp}
import amba.{Axi4Bundle, AxResponse}


class Axi4Mig extends Module {
  val io = IO(new Bundle {
    // Inouts
    val ddr3_dq = Analog(16.W)
    val ddr3_dqs_n = Analog(2.W)
    val ddr3_dqs_p = Analog(2.W)
    // Outputs
    val ddr3_addr = Output(UInt(15.W))
    val ddr3_ba = Output(UInt(3.W))
    val ddr3_ras_n = Output(Bool())
    val ddr3_cas_n = Output(Bool())
    val ddr3_we_n = Output(Bool())
    val ddr3_reset_n = Output(Bool())
    val ddr3_ck_p = Output(UInt(1.W))
    val ddr3_ck_n = Output(UInt(1.W))
    val ddr3_cke = Output(UInt(1.W))
    val ddr3_dm = Output(UInt(2.W))
    val ddr3_odt = Output(UInt(1.W))

    val s_axi = Flipped(new Axi4Bundle())

    val sys_clk_i = Input(Clock())
    val clk_ref_i = Input(Clock())
  })

  val blackbox = Module(new Axi4MigBlackbox())

  //inouts
  io.ddr3_dq <> blackbox.io.ddr3_dq
  io.ddr3_dqs_n <> blackbox.io.ddr3_dqs_n
  io.ddr3_dqs_p <> blackbox.io.ddr3_dqs_p

  //outputs
  io.ddr3_addr     := blackbox.io.ddr3_addr
  io.ddr3_ba       := blackbox.io.ddr3_ba
  io.ddr3_ras_n    := blackbox.io.ddr3_ras_n
  io.ddr3_cas_n    := blackbox.io.ddr3_cas_n
  io.ddr3_we_n     := blackbox.io.ddr3_we_n
  io.ddr3_reset_n  := blackbox.io.ddr3_reset_n
  io.ddr3_ck_p     := blackbox.io.ddr3_ck_p
  io.ddr3_ck_n     := blackbox.io.ddr3_ck_n
  io.ddr3_cke      := blackbox.io.ddr3_cke
  io.ddr3_dm       := blackbox.io.ddr3_dm
  io.ddr3_odt      := blackbox.io.ddr3_odt

  //inputs
  //NO_BUFFER clock
  blackbox.io.sys_clk_i     := io.sys_clk_i
  blackbox.io.clk_ref_i     := io.clk_ref_i

  // user interface signals
  // io.ui_clk            := blackbox.io.ui_clk
  // io.port.ui_clk_sync_rst   := blackbox.io.ui_clk_sync_rst
  // io.port.mmcm_locked       := blackbox.io.mmcm_locked
  blackbox.io.aresetn       := !reset.asBool
  blackbox.io.app_sr_req    := DontCare
  blackbox.io.app_ref_req   := DontCare
  blackbox.io.app_zq_req    := DontCare
  //app_sr_active           := unconnected
  //app_ref_ack             := unconnected
  //app_zq_ack              := unconnected

  val awaddr = io.s_axi.aw.bits.addr - 0x80000000L.U
  val araddr = io.s_axi.ar.bits.addr - 0x80000000L.U

  //slave AXI interface write address ports
  blackbox.io.s_axi_awid    := io.s_axi.aw.bits.id
  blackbox.io.s_axi_awaddr  := awaddr //truncated
  blackbox.io.s_axi_awlen   := io.s_axi.aw.bits.len
  blackbox.io.s_axi_awsize  := io.s_axi.aw.bits.size.asUInt
  blackbox.io.s_axi_awburst := io.s_axi.aw.bits.burst.asUInt
  blackbox.io.s_axi_awlock  := false.B
  blackbox.io.s_axi_awcache := "b0011".U
  blackbox.io.s_axi_awprot  := "b000".U
  blackbox.io.s_axi_awqos   := "b0000".U
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
  io.s_axi.b.bits.resp      := AxResponse(blackbox.io.s_axi_bresp)
  io.s_axi.b.valid          := blackbox.io.s_axi_bvalid

  //slave AXI interface read address ports
  blackbox.io.s_axi_arid    := io.s_axi.ar.bits.id
  blackbox.io.s_axi_araddr  := araddr // truncated
  blackbox.io.s_axi_arlen   := io.s_axi.ar.bits.len
  blackbox.io.s_axi_arsize  := io.s_axi.ar.bits.size.asUInt
  blackbox.io.s_axi_arburst := io.s_axi.ar.bits.burst.asUInt
  blackbox.io.s_axi_arlock  := false.B
  blackbox.io.s_axi_arcache := "b0011".U
  blackbox.io.s_axi_arprot  := "b000".U
  blackbox.io.s_axi_arqos   := "b0000".U
  blackbox.io.s_axi_arvalid := io.s_axi.ar.valid
  io.s_axi.ar.ready          := blackbox.io.s_axi_arready

  //slace AXI interface read data ports
  blackbox.io.s_axi_rready  := io.s_axi.r.ready
  io.s_axi.r.bits.id       := blackbox.io.s_axi_rid
  io.s_axi.r.bits.data     := blackbox.io.s_axi_rdata
  io.s_axi.r.bits.resp     := AxResponse(blackbox.io.s_axi_rresp)
  io.s_axi.r.bits.last     := blackbox.io.s_axi_rlast
  io.s_axi.r.valid          := blackbox.io.s_axi_rvalid 

  blackbox.io.sys_rst       := reset.asBool
}


class Axi4MigBlackbox extends BlackBox {
  val io = IO(new Bundle {
    val sys_rst = Input(Reset())

    // Inouts
    val ddr3_dq = Analog(16.W)
    val ddr3_dqs_n = Analog(2.W)
    val ddr3_dqs_p = Analog(2.W)
    // Outputs
    val ddr3_addr = Output(UInt(15.W))
    val ddr3_ba = Output(UInt(3.W))
    val ddr3_ras_n = Output(Bool())
    val ddr3_cas_n = Output(Bool())
    val ddr3_we_n = Output(Bool())
    val ddr3_reset_n = Output(Bool())
    val ddr3_ck_p = Output(UInt(1.W))
    val ddr3_ck_n = Output(UInt(1.W))
    val ddr3_cke = Output(UInt(1.W))
    val ddr3_dm = Output(UInt(2.W))
    val ddr3_odt = Output(UInt(1.W))
    // Inputs
    // Single-ended system clock
    val sys_clk_i = Input(Clock())
    // Single-ended iodelayctrl clk (reference clock)
    val clk_ref_i = Input(Clock())
    // user interface signals
    val ui_clk                = Output(Clock())
    val ui_clk_sync_rst       = Output(Bool())
    val mmcm_locked           = Output(Bool())
    val aresetn               = Input(Reset())
    val app_sr_req            = Input(Bool())
    val app_ref_req           = Input(Bool())
    val app_zq_req            = Input(Bool())
    val app_sr_active         = Output(Bool())
    val app_ref_ack           = Output(Bool())
    val app_zq_ack            = Output(Bool())
    // Slave Interface Write Address Ports
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
    // Slave Interface Write Data Ports
    val s_axi_wdata           = Input(Bits(64.W))
    val s_axi_wstrb           = Input(Bits(8.W))
    val s_axi_wlast           = Input(Bool())
    val s_axi_wvalid          = Input(Bool())
    val s_axi_wready          = Output(Bool())
    // Slave Interface Write Response Ports
    val s_axi_bready          = Input(Bool())
    val s_axi_bid             = Output(Bits(4.W))
    val s_axi_bresp           = Output(Bits(2.W))
    val s_axi_bvalid          = Output(Bool())
    // Slave Interface Read Address Ports
    val s_axi_arid            = Input(Bits(4.W))
    val s_axi_araddr          = Input(Bits(32.W))
    val s_axi_arlen           = Input(UInt(8.W))
    val s_axi_arsize          = Input(Bits(3.W))
    val s_axi_arburst         = Input(Bits(2.W))
    val s_axi_arlock          = Input(Bits(1.W))
    val s_axi_arcache         = Input(Bits(4.W))
    val s_axi_arprot          = Input(Bits(3.W))
    val s_axi_arqos           = Input(Bits(4.W))
    val s_axi_arvalid         = Input(Bool())
    val s_axi_arready         = Output(Bool())
    // Slave Interface Read Data Ports
    val s_axi_rready          = Input(Bool())
    val s_axi_rid             = Output(Bits(4.W))
    val s_axi_rdata           = Output(Bits(64.W))
    val s_axi_rresp           = Output(Bits(2.W))
    val s_axi_rlast           = Output(Bool())
    val s_axi_rvalid          = Output(Bool())
    val device_temp           = Output(Bits(12.W))
  })

  val ipName = "Axi4MigBlackbox"
  // addConstraintResource("package-vivado-ips/resources/constraints/NexysVideoMIG.prj")
  addConstraintResource("package-vivado-ips/resources/constraints/NexysVideoMIG.ucf")
  addVivadoIp(
    name="mig_7series",
    vendor="xilinx.com",
    library="ip",
    version="4.2",
    moduleName=ipName,
    extra=s"""
set_property -dict [list \\
  CONFIG.ARESETN.INSERT_VIP {0} \\
  CONFIG.BOARD_MIG_PARAM {Custom} \\
  CONFIG.C0_ARESETN.INSERT_VIP {0} \\
  CONFIG.C0_CLOCK.INSERT_VIP {0} \\
  CONFIG.C0_DDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C0_DDR3_RESET.INSERT_VIP {0} \\
  CONFIG.C0_LPDDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C0_MMCM_CLKOUT0.INSERT_VIP {0} \\
  CONFIG.C0_MMCM_CLKOUT1.INSERT_VIP {0} \\
  CONFIG.C0_MMCM_CLKOUT2.INSERT_VIP {0} \\
  CONFIG.C0_MMCM_CLKOUT3.INSERT_VIP {0} \\
  CONFIG.C0_MMCM_CLKOUT4.INSERT_VIP {0} \\
  CONFIG.C0_QDRIIP_RESET.INSERT_VIP {0} \\
  CONFIG.C0_RESET.INSERT_VIP {0} \\
  CONFIG.C0_RLDIII_RESET.INSERT_VIP {0} \\
  CONFIG.C0_RLDII_RESET.INSERT_VIP {0} \\
  CONFIG.C0_SYS_CLK_I.INSERT_VIP {0} \\
  CONFIG.C1_ARESETN.INSERT_VIP {0} \\
  CONFIG.C1_CLOCK.INSERT_VIP {0} \\
  CONFIG.C1_DDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C1_DDR3_RESET.INSERT_VIP {0} \\
  CONFIG.C1_LPDDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C1_MMCM_CLKOUT0.INSERT_VIP {0} \\
  CONFIG.C1_MMCM_CLKOUT1.INSERT_VIP {0} \\
  CONFIG.C1_MMCM_CLKOUT2.INSERT_VIP {0} \\
  CONFIG.C1_MMCM_CLKOUT3.INSERT_VIP {0} \\
  CONFIG.C1_MMCM_CLKOUT4.INSERT_VIP {0} \\
  CONFIG.C1_QDRIIP_RESET.INSERT_VIP {0} \\
  CONFIG.C1_RESET.INSERT_VIP {0} \\
  CONFIG.C1_RLDIII_RESET.INSERT_VIP {0} \\
  CONFIG.C1_RLDII_RESET.INSERT_VIP {0} \\
  CONFIG.C1_SYS_CLK_I.INSERT_VIP {0} \\
  CONFIG.C2_ARESETN.INSERT_VIP {0} \\
  CONFIG.C2_CLOCK.INSERT_VIP {0} \\
  CONFIG.C2_DDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C2_DDR3_RESET.INSERT_VIP {0} \\
  CONFIG.C2_LPDDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C2_MMCM_CLKOUT0.INSERT_VIP {0} \\
  CONFIG.C2_MMCM_CLKOUT1.INSERT_VIP {0} \\
  CONFIG.C2_MMCM_CLKOUT2.INSERT_VIP {0} \\
  CONFIG.C2_MMCM_CLKOUT3.INSERT_VIP {0} \\
  CONFIG.C2_MMCM_CLKOUT4.INSERT_VIP {0} \\
  CONFIG.C2_QDRIIP_RESET.INSERT_VIP {0} \\
  CONFIG.C2_RESET.INSERT_VIP {0} \\
  CONFIG.C2_RLDIII_RESET.INSERT_VIP {0} \\
  CONFIG.C2_RLDII_RESET.INSERT_VIP {0} \\
  CONFIG.C2_SYS_CLK_I.INSERT_VIP {0} \\
  CONFIG.C3_ARESETN.INSERT_VIP {0} \\
  CONFIG.C3_CLOCK.INSERT_VIP {0} \\
  CONFIG.C3_DDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C3_DDR3_RESET.INSERT_VIP {0} \\
  CONFIG.C3_LPDDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C3_MMCM_CLKOUT0.INSERT_VIP {0} \\
  CONFIG.C3_MMCM_CLKOUT1.INSERT_VIP {0} \\
  CONFIG.C3_MMCM_CLKOUT2.INSERT_VIP {0} \\
  CONFIG.C3_MMCM_CLKOUT3.INSERT_VIP {0} \\
  CONFIG.C3_MMCM_CLKOUT4.INSERT_VIP {0} \\
  CONFIG.C3_QDRIIP_RESET.INSERT_VIP {0} \\
  CONFIG.C3_RESET.INSERT_VIP {0} \\
  CONFIG.C3_RLDIII_RESET.INSERT_VIP {0} \\
  CONFIG.C3_RLDII_RESET.INSERT_VIP {0} \\
  CONFIG.C3_SYS_CLK_I.INSERT_VIP {0} \\
  CONFIG.C4_ARESETN.INSERT_VIP {0} \\
  CONFIG.C4_CLOCK.INSERT_VIP {0} \\
  CONFIG.C4_DDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C4_DDR3_RESET.INSERT_VIP {0} \\
  CONFIG.C4_LPDDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C4_MMCM_CLKOUT0.INSERT_VIP {0} \\
  CONFIG.C4_MMCM_CLKOUT1.INSERT_VIP {0} \\
  CONFIG.C4_MMCM_CLKOUT2.INSERT_VIP {0} \\
  CONFIG.C4_MMCM_CLKOUT3.INSERT_VIP {0} \\
  CONFIG.C4_MMCM_CLKOUT4.INSERT_VIP {0} \\
  CONFIG.C4_QDRIIP_RESET.INSERT_VIP {0} \\
  CONFIG.C4_RESET.INSERT_VIP {0} \\
  CONFIG.C4_RLDIII_RESET.INSERT_VIP {0} \\
  CONFIG.C4_RLDII_RESET.INSERT_VIP {0} \\
  CONFIG.C4_SYS_CLK_I.INSERT_VIP {0} \\
  CONFIG.C5_ARESETN.INSERT_VIP {0} \\
  CONFIG.C5_CLOCK.INSERT_VIP {0} \\
  CONFIG.C5_DDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C5_DDR3_RESET.INSERT_VIP {0} \\
  CONFIG.C5_LPDDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C5_MMCM_CLKOUT0.INSERT_VIP {0} \\
  CONFIG.C5_MMCM_CLKOUT1.INSERT_VIP {0} \\
  CONFIG.C5_MMCM_CLKOUT2.INSERT_VIP {0} \\
  CONFIG.C5_MMCM_CLKOUT3.INSERT_VIP {0} \\
  CONFIG.C5_MMCM_CLKOUT4.INSERT_VIP {0} \\
  CONFIG.C5_QDRIIP_RESET.INSERT_VIP {0} \\
  CONFIG.C5_RESET.INSERT_VIP {0} \\
  CONFIG.C5_RLDIII_RESET.INSERT_VIP {0} \\
  CONFIG.C5_RLDII_RESET.INSERT_VIP {0} \\
  CONFIG.C5_SYS_CLK_I.INSERT_VIP {0} \\
  CONFIG.C6_ARESETN.INSERT_VIP {0} \\
  CONFIG.C6_CLOCK.INSERT_VIP {0} \\
  CONFIG.C6_DDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C6_DDR3_RESET.INSERT_VIP {0} \\
  CONFIG.C6_LPDDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C6_MMCM_CLKOUT0.INSERT_VIP {0} \\
  CONFIG.C6_MMCM_CLKOUT1.INSERT_VIP {0} \\
  CONFIG.C6_MMCM_CLKOUT2.INSERT_VIP {0} \\
  CONFIG.C6_MMCM_CLKOUT3.INSERT_VIP {0} \\
  CONFIG.C6_MMCM_CLKOUT4.INSERT_VIP {0} \\
  CONFIG.C6_QDRIIP_RESET.INSERT_VIP {0} \\
  CONFIG.C6_RESET.INSERT_VIP {0} \\
  CONFIG.C6_RLDIII_RESET.INSERT_VIP {0} \\
  CONFIG.C6_RLDII_RESET.INSERT_VIP {0} \\
  CONFIG.C6_SYS_CLK_I.INSERT_VIP {0} \\
  CONFIG.C7_ARESETN.INSERT_VIP {0} \\
  CONFIG.C7_CLOCK.INSERT_VIP {0} \\
  CONFIG.C7_DDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C7_DDR3_RESET.INSERT_VIP {0} \\
  CONFIG.C7_LPDDR2_RESET.INSERT_VIP {0} \\
  CONFIG.C7_MMCM_CLKOUT0.INSERT_VIP {0} \\
  CONFIG.C7_MMCM_CLKOUT1.INSERT_VIP {0} \\
  CONFIG.C7_MMCM_CLKOUT2.INSERT_VIP {0} \\
  CONFIG.C7_MMCM_CLKOUT3.INSERT_VIP {0} \\
  CONFIG.C7_MMCM_CLKOUT4.INSERT_VIP {0} \\
  CONFIG.C7_QDRIIP_RESET.INSERT_VIP {0} \\
  CONFIG.C7_RESET.INSERT_VIP {0} \\
  CONFIG.C7_RLDIII_RESET.INSERT_VIP {0} \\
  CONFIG.C7_RLDII_RESET.INSERT_VIP {0} \\
  CONFIG.C7_SYS_CLK_I.INSERT_VIP {0} \\
  CONFIG.CLK_REF_I.INSERT_VIP {0} \\
  CONFIG.CLOCK.INSERT_VIP {0} \\
  CONFIG.DDR2_RESET.INSERT_VIP {0} \\
  CONFIG.DDR3_RESET.INSERT_VIP {0} \\
  CONFIG.LPDDR2_RESET.INSERT_VIP {0} \\
  CONFIG.MIG_DONT_TOUCH_PARAM {Custom} \\
  CONFIG.MMCM_CLKOUT0.INSERT_VIP {0} \\
  CONFIG.MMCM_CLKOUT1.INSERT_VIP {0} \\
  CONFIG.MMCM_CLKOUT2.INSERT_VIP {0} \\
  CONFIG.MMCM_CLKOUT3.INSERT_VIP {0} \\
  CONFIG.MMCM_CLKOUT4.INSERT_VIP {0} \\
  CONFIG.QDRIIP_RESET.INSERT_VIP {0} \\
  CONFIG.RESET.INSERT_VIP {0} \\
  CONFIG.RESET_BOARD_INTERFACE {Custom} \\
  CONFIG.RLDIII_RESET.INSERT_VIP {0} \\
  CONFIG.RLDII_RESET.INSERT_VIP {0} \\
  CONFIG.S0_AXI.INSERT_VIP {0} \\
  CONFIG.S0_AXI_CTRL.INSERT_VIP {0} \\
  CONFIG.S1_AXI.INSERT_VIP {0} \\
  CONFIG.S1_AXI_CTRL.INSERT_VIP {0} \\
  CONFIG.S2_AXI.INSERT_VIP {0} \\
  CONFIG.S2_AXI_CTRL.INSERT_VIP {0} \\
  CONFIG.S3_AXI.INSERT_VIP {0} \\
  CONFIG.S3_AXI_CTRL.INSERT_VIP {0} \\
  CONFIG.S4_AXI.INSERT_VIP {0} \\
  CONFIG.S4_AXI_CTRL.INSERT_VIP {0} \\
  CONFIG.S5_AXI.INSERT_VIP {0} \\
  CONFIG.S5_AXI_CTRL.INSERT_VIP {0} \\
  CONFIG.S6_AXI.INSERT_VIP {0} \\
  CONFIG.S6_AXI_CTRL.INSERT_VIP {0} \\
  CONFIG.S7_AXI.INSERT_VIP {0} \\
  CONFIG.S7_AXI_CTRL.INSERT_VIP {0} \\
  CONFIG.SYSTEM_RESET.INSERT_VIP {0} \\
  CONFIG.SYS_CLK_I.INSERT_VIP {0} \\
  CONFIG.S_AXI.INSERT_VIP {0} \\
  CONFIG.S_AXI_CTRL.INSERT_VIP {0} \\
  CONFIG.XML_INPUT_FILE {${os.pwd}/package-vivado-ips/resources/constraints/NexysVideoMIG.prj} \\
] [get_ips ${ipName}]
"""
  )
}


// # generate_target {instantiation_template} [get_files ${ipName}.xci]

// create_ip -name mig_7series -vendor xilinx.com -library ip -version 4.2 -module_name mig_7series_0
// set_property -dict [list \
//   CONFIG.ARESETN.INSERT_VIP {0} \
//   CONFIG.BOARD_MIG_PARAM {Custom} \
//   CONFIG.C0_ARESETN.INSERT_VIP {0} \
//   CONFIG.C0_CLOCK.INSERT_VIP {0} \
//   CONFIG.C0_DDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C0_DDR3_RESET.INSERT_VIP {0} \
//   CONFIG.C0_LPDDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C0_MMCM_CLKOUT0.INSERT_VIP {0} \
//   CONFIG.C0_MMCM_CLKOUT1.INSERT_VIP {0} \
//   CONFIG.C0_MMCM_CLKOUT2.INSERT_VIP {0} \
//   CONFIG.C0_MMCM_CLKOUT3.INSERT_VIP {0} \
//   CONFIG.C0_MMCM_CLKOUT4.INSERT_VIP {0} \
//   CONFIG.C0_QDRIIP_RESET.INSERT_VIP {0} \
//   CONFIG.C0_RESET.INSERT_VIP {0} \
//   CONFIG.C0_RLDIII_RESET.INSERT_VIP {0} \
//   CONFIG.C0_RLDII_RESET.INSERT_VIP {0} \
//   CONFIG.C0_SYS_CLK_I.INSERT_VIP {0} \
//   CONFIG.C1_ARESETN.INSERT_VIP {0} \
//   CONFIG.C1_CLOCK.INSERT_VIP {0} \
//   CONFIG.C1_DDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C1_DDR3_RESET.INSERT_VIP {0} \
//   CONFIG.C1_LPDDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C1_MMCM_CLKOUT0.INSERT_VIP {0} \
//   CONFIG.C1_MMCM_CLKOUT1.INSERT_VIP {0} \
//   CONFIG.C1_MMCM_CLKOUT2.INSERT_VIP {0} \
//   CONFIG.C1_MMCM_CLKOUT3.INSERT_VIP {0} \
//   CONFIG.C1_MMCM_CLKOUT4.INSERT_VIP {0} \
//   CONFIG.C1_QDRIIP_RESET.INSERT_VIP {0} \
//   CONFIG.C1_RESET.INSERT_VIP {0} \
//   CONFIG.C1_RLDIII_RESET.INSERT_VIP {0} \
//   CONFIG.C1_RLDII_RESET.INSERT_VIP {0} \
//   CONFIG.C1_SYS_CLK_I.INSERT_VIP {0} \
//   CONFIG.C2_ARESETN.INSERT_VIP {0} \
//   CONFIG.C2_CLOCK.INSERT_VIP {0} \
//   CONFIG.C2_DDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C2_DDR3_RESET.INSERT_VIP {0} \
//   CONFIG.C2_LPDDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C2_MMCM_CLKOUT0.INSERT_VIP {0} \
//   CONFIG.C2_MMCM_CLKOUT1.INSERT_VIP {0} \
//   CONFIG.C2_MMCM_CLKOUT2.INSERT_VIP {0} \
//   CONFIG.C2_MMCM_CLKOUT3.INSERT_VIP {0} \
//   CONFIG.C2_MMCM_CLKOUT4.INSERT_VIP {0} \
//   CONFIG.C2_QDRIIP_RESET.INSERT_VIP {0} \
//   CONFIG.C2_RESET.INSERT_VIP {0} \
//   CONFIG.C2_RLDIII_RESET.INSERT_VIP {0} \
//   CONFIG.C2_RLDII_RESET.INSERT_VIP {0} \
//   CONFIG.C2_SYS_CLK_I.INSERT_VIP {0} \
//   CONFIG.C3_ARESETN.INSERT_VIP {0} \
//   CONFIG.C3_CLOCK.INSERT_VIP {0} \
//   CONFIG.C3_DDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C3_DDR3_RESET.INSERT_VIP {0} \
//   CONFIG.C3_LPDDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C3_MMCM_CLKOUT0.INSERT_VIP {0} \
//   CONFIG.C3_MMCM_CLKOUT1.INSERT_VIP {0} \
//   CONFIG.C3_MMCM_CLKOUT2.INSERT_VIP {0} \
//   CONFIG.C3_MMCM_CLKOUT3.INSERT_VIP {0} \
//   CONFIG.C3_MMCM_CLKOUT4.INSERT_VIP {0} \
//   CONFIG.C3_QDRIIP_RESET.INSERT_VIP {0} \
//   CONFIG.C3_RESET.INSERT_VIP {0} \
//   CONFIG.C3_RLDIII_RESET.INSERT_VIP {0} \
//   CONFIG.C3_RLDII_RESET.INSERT_VIP {0} \
//   CONFIG.C3_SYS_CLK_I.INSERT_VIP {0} \
//   CONFIG.C4_ARESETN.INSERT_VIP {0} \
//   CONFIG.C4_CLOCK.INSERT_VIP {0} \
//   CONFIG.C4_DDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C4_DDR3_RESET.INSERT_VIP {0} \
//   CONFIG.C4_LPDDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C4_MMCM_CLKOUT0.INSERT_VIP {0} \
//   CONFIG.C4_MMCM_CLKOUT1.INSERT_VIP {0} \
//   CONFIG.C4_MMCM_CLKOUT2.INSERT_VIP {0} \
//   CONFIG.C4_MMCM_CLKOUT3.INSERT_VIP {0} \
//   CONFIG.C4_MMCM_CLKOUT4.INSERT_VIP {0} \
//   CONFIG.C4_QDRIIP_RESET.INSERT_VIP {0} \
//   CONFIG.C4_RESET.INSERT_VIP {0} \
//   CONFIG.C4_RLDIII_RESET.INSERT_VIP {0} \
//   CONFIG.C4_RLDII_RESET.INSERT_VIP {0} \
//   CONFIG.C4_SYS_CLK_I.INSERT_VIP {0} \
//   CONFIG.C5_ARESETN.INSERT_VIP {0} \
//   CONFIG.C5_CLOCK.INSERT_VIP {0} \
//   CONFIG.C5_DDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C5_DDR3_RESET.INSERT_VIP {0} \
//   CONFIG.C5_LPDDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C5_MMCM_CLKOUT0.INSERT_VIP {0} \
//   CONFIG.C5_MMCM_CLKOUT1.INSERT_VIP {0} \
//   CONFIG.C5_MMCM_CLKOUT2.INSERT_VIP {0} \
//   CONFIG.C5_MMCM_CLKOUT3.INSERT_VIP {0} \
//   CONFIG.C5_MMCM_CLKOUT4.INSERT_VIP {0} \
//   CONFIG.C5_QDRIIP_RESET.INSERT_VIP {0} \
//   CONFIG.C5_RESET.INSERT_VIP {0} \
//   CONFIG.C5_RLDIII_RESET.INSERT_VIP {0} \
//   CONFIG.C5_RLDII_RESET.INSERT_VIP {0} \
//   CONFIG.C5_SYS_CLK_I.INSERT_VIP {0} \
//   CONFIG.C6_ARESETN.INSERT_VIP {0} \
//   CONFIG.C6_CLOCK.INSERT_VIP {0} \
//   CONFIG.C6_DDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C6_DDR3_RESET.INSERT_VIP {0} \
//   CONFIG.C6_LPDDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C6_MMCM_CLKOUT0.INSERT_VIP {0} \
//   CONFIG.C6_MMCM_CLKOUT1.INSERT_VIP {0} \
//   CONFIG.C6_MMCM_CLKOUT2.INSERT_VIP {0} \
//   CONFIG.C6_MMCM_CLKOUT3.INSERT_VIP {0} \
//   CONFIG.C6_MMCM_CLKOUT4.INSERT_VIP {0} \
//   CONFIG.C6_QDRIIP_RESET.INSERT_VIP {0} \
//   CONFIG.C6_RESET.INSERT_VIP {0} \
//   CONFIG.C6_RLDIII_RESET.INSERT_VIP {0} \
//   CONFIG.C6_RLDII_RESET.INSERT_VIP {0} \
//   CONFIG.C6_SYS_CLK_I.INSERT_VIP {0} \
//   CONFIG.C7_ARESETN.INSERT_VIP {0} \
//   CONFIG.C7_CLOCK.INSERT_VIP {0} \
//   CONFIG.C7_DDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C7_DDR3_RESET.INSERT_VIP {0} \
//   CONFIG.C7_LPDDR2_RESET.INSERT_VIP {0} \
//   CONFIG.C7_MMCM_CLKOUT0.INSERT_VIP {0} \
//   CONFIG.C7_MMCM_CLKOUT1.INSERT_VIP {0} \
//   CONFIG.C7_MMCM_CLKOUT2.INSERT_VIP {0} \
//   CONFIG.C7_MMCM_CLKOUT3.INSERT_VIP {0} \
//   CONFIG.C7_MMCM_CLKOUT4.INSERT_VIP {0} \
//   CONFIG.C7_QDRIIP_RESET.INSERT_VIP {0} \
//   CONFIG.C7_RESET.INSERT_VIP {0} \
//   CONFIG.C7_RLDIII_RESET.INSERT_VIP {0} \
//   CONFIG.C7_RLDII_RESET.INSERT_VIP {0} \
//   CONFIG.C7_SYS_CLK_I.INSERT_VIP {0} \
//   CONFIG.CLK_REF_I.INSERT_VIP {0} \
//   CONFIG.CLOCK.INSERT_VIP {0} \
//   CONFIG.DDR2_RESET.INSERT_VIP {0} \
//   CONFIG.DDR3_RESET.INSERT_VIP {0} \
//   CONFIG.LPDDR2_RESET.INSERT_VIP {0} \
//   CONFIG.MIG_DONT_TOUCH_PARAM {Custom} \
//   CONFIG.MMCM_CLKOUT0.INSERT_VIP {0} \
//   CONFIG.MMCM_CLKOUT1.INSERT_VIP {0} \
//   CONFIG.MMCM_CLKOUT2.INSERT_VIP {0} \
//   CONFIG.MMCM_CLKOUT3.INSERT_VIP {0} \
//   CONFIG.MMCM_CLKOUT4.INSERT_VIP {0} \
//   CONFIG.QDRIIP_RESET.INSERT_VIP {0} \
//   CONFIG.RESET.INSERT_VIP {0} \
//   CONFIG.RESET_BOARD_INTERFACE {Custom} \
//   CONFIG.RLDIII_RESET.INSERT_VIP {0} \
//   CONFIG.RLDII_RESET.INSERT_VIP {0} \
//   CONFIG.S0_AXI.INSERT_VIP {0} \
//   CONFIG.S0_AXI_CTRL.INSERT_VIP {0} \
//   CONFIG.S1_AXI.INSERT_VIP {0} \
//   CONFIG.S1_AXI_CTRL.INSERT_VIP {0} \
//   CONFIG.S2_AXI.INSERT_VIP {0} \
//   CONFIG.S2_AXI_CTRL.INSERT_VIP {0} \
//   CONFIG.S3_AXI.INSERT_VIP {0} \
//   CONFIG.S3_AXI_CTRL.INSERT_VIP {0} \
//   CONFIG.S4_AXI.INSERT_VIP {0} \
//   CONFIG.S4_AXI_CTRL.INSERT_VIP {0} \
//   CONFIG.S5_AXI.INSERT_VIP {0} \
//   CONFIG.S5_AXI_CTRL.INSERT_VIP {0} \
//   CONFIG.S6_AXI.INSERT_VIP {0} \
//   CONFIG.S6_AXI_CTRL.INSERT_VIP {0} \
//   CONFIG.S7_AXI.INSERT_VIP {0} \
//   CONFIG.S7_AXI_CTRL.INSERT_VIP {0} \
//   CONFIG.SYSTEM_RESET.INSERT_VIP {0} \
//   CONFIG.SYS_CLK_I.INSERT_VIP {0} \
//   CONFIG.S_AXI.INSERT_VIP {0} \
//   CONFIG.S_AXI_CTRL.INSERT_VIP {0} \
//   CONFIG.XML_INPUT_FILE {mig_a.prj} \
// ] [get_ips mig_7series_0]
// generate_target {instantiation_template} [get_files /home/tk/Desktop/MaDa/out/vivado-project/VivadoProject.srcs/sources_1/ip/mig_7series_0/mig_7series_0.xci]
// INFO: [IP_Flow 19-1686] Generating 'Instantiation Template' target for IP 'mig_7series_0'...
// update_compile_order -fileset sources_1
// generate_target all [get_files  /home/tk/Desktop/MaDa/out/vivado-project/VivadoProject.srcs/sources_1/ip/mig_7series_0/mig_7series_0.xci]
