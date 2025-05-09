package vivadoips

import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4LiteBundle}
import builder.addVivadoIp


case class Axi4LiteUartLiteConfig(
  axiClockFrequency: Int = 100,
  baudRate: Int = 115200,
)

class Axi4LiteUartLite(
  val config: Axi4LiteUartLiteConfig = Axi4LiteUartLiteConfig()
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBundle())
    val rx = Input(Bool())
    val tx = Output(Bool())
  })

  val blackbox = Module(new Axi4LiteUartLiteBlackbox(config))

  blackbox.io.s_axi_aclk := clock
  blackbox.io.s_axi_aresetn := ~reset.asBool
  blackbox.io.s_axi.connectFrom(io.s_axi)
  blackbox.io.rx := io.rx
  io.tx := blackbox.io.tx
}

class Axi4LiteUartLiteBlackbox(
  val config: Axi4LiteUartLiteConfig = Axi4LiteUartLiteConfig()
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
  CONFIG.C_BAUDRATE {${config.baudRate}} \\
  CONFIG.C_S_AXI_ACLK_FREQ_HZ {${config.axiClockFrequency}} \\
] [get_ips ${ipName}]
"""
  )
}

