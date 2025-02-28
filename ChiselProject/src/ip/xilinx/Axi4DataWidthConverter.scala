import chisel3._
import chisel3.util._
import java.io.PrintWriter


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
  blackbox.io.s_axi.connect(io.s_axi)
  blackbox.io.m_axi.flipConnect(io.m_axi)
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

  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/VivadoProject"
    val ip_name = "Axi4DataWidthConverterBlackbox"
    val ip_name_lower = ip_name.toLowerCase()

    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")
    
    tcl_script.println(s"create_ip -name axi_dwidth_converter -vendor xilinx.com -library ip -version 2.1 -module_name ${ip_name}")

    tcl_script.println(s"""
set_property -dict [list \\
  CONFIG.MI_DATA_WIDTH {${m_params.dataWidth}} \\
  CONFIG.SI_DATA_WIDTH {${s_params.dataWidth}} \\
  CONFIG.SI_ID_WIDTH {${s_params.idWidth}} \\
] [get_ips ${ip_name}]
""")

    tcl_script.println(s"generate_target {instantiation_template} [get_ips ${ip_name}]")
    tcl_script.println("update_compile_order -fileset sources_1")
    tcl_script.println(s"generate_target all [get_ips ${ip_name}]")
    tcl_script.println(s"catch { config_ip_cache -export [get_ips -all ${ip_name}] }")
    tcl_script.println(s"export_ip_user_files -of_objects [get_ips ${ip_name}] -no_script -sync -force -quiet")
    tcl_script.println(s"create_ip_run [get_ips ${ip_name}]")

    tcl_script.close()
  }
  generate_tcl_script()
}