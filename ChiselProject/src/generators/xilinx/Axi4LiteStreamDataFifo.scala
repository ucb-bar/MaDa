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

  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/VivadoProject"
    val ip_name = "AXIStreamDataFifo"
    val ip_name_lower = ip_name.toLowerCase()

    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")
    
    tcl_script.println(s"create_ip -name axis_data_fifo -vendor xilinx.com -library ip -version 2.0 -module_name ${ip_name}")

    tcl_script.println(s"""
set_property -dict [list \\
  CONFIG.HAS_TLAST {1} \\
  CONFIG.TUSER_WIDTH {1} \\
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
