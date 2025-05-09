import chisel3._
import chisel3.util._

import java.io.PrintWriter

class Axi4LiteUartLite(
  val axiClockFrequency: Int = 100,
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBundle())
    val rx = Input(Bool())
    val tx = Output(Bool())
  })

  val blackbox = Module(new Axi4LiteUartLiteBlackbox(axiClockFrequency=axiClockFrequency))

  blackbox.io.s_axi_aclk := clock
  blackbox.io.s_axi_aresetn := ~reset.asBool
  blackbox.io.s_axi.connectFrom(io.s_axi)
  blackbox.io.rx := io.rx
  io.tx := blackbox.io.tx
}

class Axi4LiteUartLiteBlackbox(
  val axiClockFrequency: Int = 100,
) extends BlackBox {
  val io = IO(new Bundle {
    val s_axi_aclk = Input(Clock())
    val s_axi_aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4LiteBlackboxBundle())
    val rx = Input(Bool())
    val tx = Output(Bool())
  })

  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/vivado-project"
    val ip_name = "Axi4LiteUartLiteBlackbox"
    val ip_name_lower = ip_name.toLowerCase()

    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")
    
    tcl_script.println(s"create_ip -name axi_uartlite -vendor xilinx.com -library ip -version 2.0 -module_name ${ip_name}")

    tcl_script.println(s"""
set_property -dict [list \\
  CONFIG.C_BAUDRATE {115200} \\
  CONFIG.C_S_AXI_ACLK_FREQ_HZ_d {${axiClockFrequency}} \\
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

