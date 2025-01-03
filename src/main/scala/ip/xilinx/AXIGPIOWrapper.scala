import chisel3.{BlackBox, _}
import chisel3.util._

import java.io.PrintWriter


class axi_gpio_0 extends BlackBox {
  val io = IO(new Bundle {
    val s_axi = Flipped(new RawAXI4Lite())
    val gpio_io_i = Input(UInt(32.W))
    val gpio_io_o = Output(UInt(32.W))
    val gpio_io_t = Output(UInt(32.W))
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
