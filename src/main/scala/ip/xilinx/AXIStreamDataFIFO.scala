import chisel3.{BlackBox, _}
import chisel3.util._

import java.io.PrintWriter


class AXIStreamDataFifo(width: Int) extends BlackBox {
  val io = IO(new Bundle {
    val s_axis_aresetn = Input(Reset())
    val s_axis_aclk = Input(Clock())
    val s_axis_tvalid = Input(Bool())
    val s_axis_tready = Output(Bool())
    val s_axis_tdata = Input(UInt(width.W))
    val s_axis_tlast = Input(Bool())
    val s_axis_tuser = Input(Bool())
    val m_axis_tvalid = Output(Bool())
    val m_axis_tready = Input(Bool())
    val m_axis_tdata = Output(UInt(width.W))
    val m_axis_tlast = Output(Bool())
    val m_axis_tuser = Output(Bool())
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
