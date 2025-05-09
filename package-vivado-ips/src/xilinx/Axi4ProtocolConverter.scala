import chisel3._
import chisel3.util._
import java.io.PrintWriter


class Axi4ProtocolConverter(
  s_params: Axi4Params,
  m_params: Axi4Params
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle(s_params))
    val m_axi = new Axi4LiteBundle()
  })

  val blackbox = Module(new Axi4ProtocolConverterBlackbox(s_params, m_params))

  blackbox.io.aclk := clock
  blackbox.io.aresetn := ~reset.asBool
  blackbox.io.s_axi.connectFrom(io.s_axi)
  blackbox.io.m_axi.connectTo(io.m_axi)
}

class Axi4ProtocolConverterBlackbox(
  s_params: Axi4Params,
  m_params: Axi4Params
) extends BlackBox {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    val aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4BlackboxBundle(s_params))
    val m_axi = new Axi4LiteBlackboxBundle(m_params)
  })

  val ipName = "Axi4ProtocolConverterBlackbox"
  addVivadoIp(
    name="axi_protocol_converter",
    vendor="xilinx.com",
    library="ip",
    version="2.1",
    moduleName=ipName,
    extra = s"""
set_property -dict [list \\
  CONFIG.ID_WIDTH {${s_params.idWidth}} \\
  CONFIG.MI_PROTOCOL {AXI4LITE} \\
  CONFIG.SI_PROTOCOL {AXI4} \\
  CONFIG.DATA_WIDTH {${s_params.dataWidth}} \\
] [get_ips ${ipName}]
"""
  )
}