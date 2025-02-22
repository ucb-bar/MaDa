import chisel3._
import chisel3.util._
import java.io.PrintWriter


class Axi4ProtocolConverter extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle())
    val m_axi = new Axi4LiteBundle()
  })

  val blackbox = Module(new Axi4ProtocolConverterBlackbox())

  blackbox.io.aclk := clock
  blackbox.io.aresetn := ~reset.asBool
  blackbox.io.s_axi.connect(io.s_axi)
  blackbox.io.m_axi.flipConnect(io.m_axi)
}

class Axi4ProtocolConverterBlackbox extends BlackBox {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    val aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4BlackboxBundle())
    val m_axi = new Axi4LiteBlackboxBundle()
  })

  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/VivadoProject"
    val ip_name = "Axi4ProtocolConverterBlackbox"
    val ip_name_lower = ip_name.toLowerCase()

    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")
    
    tcl_script.println(s"create_ip -name axi_protocol_converter -vendor xilinx.com -library ip -version 2.1 -module_name ${ip_name}")
    
    tcl_script.println(s"""
set_property -dict [list \\
  CONFIG.ID_WIDTH {4} \\
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