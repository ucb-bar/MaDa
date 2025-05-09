import chisel3._
import chisel3.util._

import java.io.PrintWriter


class Axi4LiteStreamDataFifo(width: Int) extends Module {
  val io = IO(new Bundle {
    val s_axis = Flipped(new Axi4StreamBundle())
    val m_axis = new Axi4StreamBundle()
  })

  val blackbox = Module(new Axi4LiteStreamDataFifoBlackbox(width))

  blackbox.io.s_axis_aclk := clock
  blackbox.io.s_axis_aresetn := ~reset.asBool
  blackbox.io.s_axis.connect(io.s_axis)
  blackbox.io.m_axis.flipConnect(io.m_axis)
}

class Axi4LiteStreamDataFifoBlackbox(width: Int) extends BlackBox {
  val io = IO(new Bundle {
    val s_axis_aclk = Input(Clock())
    val s_axis_aresetn = Input(Reset())
    val s_axis = Flipped(new Axi4StreamBlackboxBundle())
    val m_axis = new Axi4StreamBlackboxBundle()
  })

  val ipName = "AXIStreamDataFifo"
  addVivadoIp(
    name="axis_data_fifo",
    vendor="xilinx.com",
    library="ip",
    version="2.0",
    moduleName=ipName,
    extra = s"""
set_property -dict [list \\
  CONFIG.HAS_TLAST {1} \\
  CONFIG.TUSER_WIDTH {1} \\
] [get_ips ${ipName}]
"""
  )
}
