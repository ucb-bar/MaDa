import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4LiteBundle}


class Axi4LiteUartLite(
  val axiClockFrequency: Int = 100,
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBundle())
    val rx = Input(Bool())
    val tx = Output(Bool())
  })

  val blackbox = Module(new Axi4LiteUartLiteBlackbox(axiClockFrequency=axiClockFrequency))

  blackbox.io.s_axi_aclk := clock
  blackbox.io.s_axi_aresetn := ~reset.asBool
  blackbox.io.s_axi.connectFrom(io.s_axi)
  blackbox.io.rx := io.rx
  io.tx := blackbox.io.tx
}

class Axi4LiteUartLiteBlackbox(
  val axiClockFrequency: Int = 100,
) extends BlackBox {
  val io = IO(new Bundle {
    val s_axi_aclk = Input(Clock())
    val s_axi_aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4LiteBlackboxBundle())
    val rx = Input(Bool())
    val tx = Output(Bool())
  })

  val ipName = "Axi4LiteUartLiteBlackbox"
  addVivadoIp(
    name="axi_uartlite",
    vendor="xilinx.com",
    library="ip",
    version="2.0",
    moduleName=ipName,
    extra = s"""
set_property -dict [list \\
  CONFIG.C_BAUDRATE {115200} \\
  CONFIG.C_S_AXI_ACLK_FREQ_HZ_d {${axiClockFrequency}} \\
] [get_ips ${ipName}]
"""
  )
}

