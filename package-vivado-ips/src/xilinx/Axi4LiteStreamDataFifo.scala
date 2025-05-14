package vivadoips

import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4StreamBundle}
import builder.addVivadoIp


class Axi4LiteStreamDataFifo(params: Axi4Params = Axi4Params()) extends Module {
  val io = IO(new Bundle {
    val s_axis = Flipped(new Axi4StreamBundle())
    val m_axis = new Axi4StreamBundle()
  })

  val blackbox = Module(new Axi4LiteStreamDataFifoBlackbox(params))

  blackbox.io.s_axis_aclk := clock
  blackbox.io.s_axis_aresetn := ~reset.asBool
  blackbox.io.s_axis.connect(io.s_axis)
  blackbox.io.m_axis.flipConnect(io.m_axis)
}

class Axi4LiteStreamDataFifoBlackbox(params: Axi4Params = Axi4Params()) extends BlackBox {
  val io = IO(new Bundle {
    val s_axis_aclk = Input(Clock())
    val s_axis_aresetn = Input(Reset())
    val s_axis = Flipped(new Axi4StreamBlackboxBundle(params))
    val m_axis = new Axi4StreamBlackboxBundle(params)
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
