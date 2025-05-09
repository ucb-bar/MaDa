import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4Bundle, Axi4LiteBundle}


class Axi4SpiFlash extends Module {
  val io = IO(new Bundle {
    val ext_spi_clk = Input(Clock())
    val s_axi = Flipped(new Axi4LiteBundle())
    val s_axi4 = Flipped(new Axi4Bundle())
    val io0_i = Input(Bool())
    val io0_o = Output(Bool())
    val io0_t = Output(Bool())
    val io1_i = Input(Bool())
    val io1_o = Output(Bool())
    val io1_t = Output(Bool())
    // val sck_i = Input(Bool())
    // val sck_o = Output(Bool())
    // val sck_t = Output(Bool())
    val ss_i = Input(Bool())
    val ss_o = Output(Bool())
    val ss_t = Output(Bool())
    val ip2intc_irpt = Output(Bool())
  })

  val blackbox = Module(new Axi4SpiFlashBlackbox())

  blackbox.io.ext_spi_clk := io.ext_spi_clk
  blackbox.io.s_axi_aclk := clock
  blackbox.io.s_axi_aresetn := ~reset.asBool
  blackbox.io.s_axi.connectFrom(io.s_axi)
  blackbox.io.s_axi4_aclk := clock
  blackbox.io.s_axi4_aresetn := ~reset.asBool
  blackbox.io.s_axi4.connectFrom(io.s_axi4)
  io.s_axi4.aw.ready := false.B
  io.s_axi4.w.ready := false.B
  io.s_axi4.b.valid := false.B
  blackbox.io.s_axi4.awaddr := 0x00000000.U
  blackbox.io.s_axi4.awvalid := false.B
  blackbox.io.s_axi4_awlock := 0.B
  blackbox.io.s_axi4_awcache := 0.U
  blackbox.io.s_axi4_awprot := 0.U
  blackbox.io.s_axi4.wdata := 0x00000000.U
  blackbox.io.s_axi4.wstrb := 0x00.U
  blackbox.io.s_axi4.wlast := false.B
  blackbox.io.s_axi4.wvalid := false.B
  blackbox.io.s_axi4.bready := false.B
  blackbox.io.s_axi4_arlock := 0.B
  blackbox.io.s_axi4_arcache := 0.U
  blackbox.io.s_axi4_arprot := 0.U

  blackbox.io.io0_i := io.io0_i
  io.io0_o := blackbox.io.io0_o
  io.io0_t := blackbox.io.io0_t
  blackbox.io.io1_i := io.io1_i
  io.io1_o := blackbox.io.io1_o
  io.io1_t := blackbox.io.io1_t
  // blackbox.io.sck_i := io.sck_i
  // io.sck_o := blackbox.io.sck_o
  // io.sck_t := blackbox.io.sck_t
  blackbox.io.ss_i := io.ss_i
  io.ss_o := blackbox.io.ss_o
  io.ss_t := blackbox.io.ss_t
  io.ip2intc_irpt := blackbox.io.ip2intc_irpt

  def connectFrom(axi: Axi4LiteBundle): Unit = {
    io.s_axi <> axi
  }

  def connectFrom(axi: Axi4Bundle): Unit = {
    io.s_axi.connectFromAxi4(axi)
  }
}

class Axi4SpiFlashBlackbox extends BlackBox {
  val io = IO(new Bundle {
    val ext_spi_clk = Input(Clock())
    val s_axi_aclk = Input(Clock())
    val s_axi_aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4LiteBlackboxBundle())
    val s_axi4_aclk = Input(Clock())
    val s_axi4_aresetn = Input(Bool())
    val s_axi4 = Flipped(new Axi4BlackboxBundle())
    val s_axi4_awlock = Input(Bool())
    val s_axi4_awcache = Input(UInt(4.W))
    val s_axi4_awprot = Input(UInt(3.W))
    val s_axi4_arlock = Input(Bool())
    val s_axi4_arcache = Input(UInt(4.W))
    val s_axi4_arprot = Input(UInt(3.W))
    val io0_i = Input(Bool())
    val io0_o = Output(Bool())
    val io0_t = Output(Bool())
    val io1_i = Input(Bool())
    val io1_o = Output(Bool())
    val io1_t = Output(Bool())
    // val sck_i = Input(Bool())
    // val sck_o = Output(Bool())
    // val sck_t = Output(Bool())
    val ss_i = Input(Bool())
    val ss_o = Output(Bool())
    val ss_t = Output(Bool())
    val ip2intc_irpt = Output(Bool())
  })

  // the Flash memory used on the Arty is Spansion S25FL128S
  // it has 8 dummy cycles for single-mode read commands
  // and 6 dummy cycles for quad-mode read commands
  val ipName = "Axi4SpiFlashBlackbox"
  addVivadoIp(
    name="axi_quad_spi",
    vendor="xilinx.com",
    library="ip",
    version="3.2",
    moduleName=ipName,
    extra = s"""
set_property -dict [list \\
  CONFIG.C_SPI_MEMORY {3} \\
  CONFIG.C_USE_STARTUP {0} \\
  CONFIG.C_XIP_MODE {1} \\
  CONFIG.C_XIP_PERF_MODE {0} \\
  CONFIG.C_USE_STARTUP {1} \\
] [get_ips ${ipName}]
"""
  )
}
