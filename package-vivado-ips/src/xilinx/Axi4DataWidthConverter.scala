import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4Bundle}


class Axi4DataWidthConverter(
  s_params: Axi4Params,
  m_params: Axi4Params
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle(s_params))
    val m_axi = new Axi4Bundle(m_params)
  })

  val blackbox = Module(new Axi4DataWidthConverterBlackbox(s_params, m_params))

  blackbox.io.s_axi_aclk := clock
  blackbox.io.s_axi_aresetn := ~reset.asBool
  blackbox.io.s_axi.connectFrom(io.s_axi)
  blackbox.io.m_axi.connectTo(io.m_axi)
}

class Axi4DataWidthConverterBlackbox(
  s_params: Axi4Params,
  m_params: Axi4Params
) extends BlackBox {
  val io = IO(new Bundle {
    val s_axi_aclk = Input(Clock())
    val s_axi_aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4BlackboxBundle(s_params))
    val m_axi = new Axi4BlackboxBundle(m_params)
  })

  val ipName = "Axi4DataWidthConverterBlackbox"
  addVivadoIp(
    name="axi_dwidth_converter",
    vendor="xilinx.com",
    library="ip",
    version="2.1",
    moduleName=ipName,
    extra = s"""
set_property -dict [list \\
  CONFIG.MI_DATA_WIDTH {${m_params.dataWidth}} \\
  CONFIG.SI_DATA_WIDTH {${s_params.dataWidth}} \\
  CONFIG.SI_ID_WIDTH {${s_params.idWidth}} \\
] [get_ips ${ipName}]
"""
  )
}
