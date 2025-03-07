import chisel3._
import chisel3.util._

import java.io.PrintWriter

class Axi4LiteTimer extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBundle())
    val capturetrig0 = Input(Bool())
    val capturetrig1 = Input(Bool())
    val generateout0 = Output(Bool())
    val generateout1 = Output(Bool())
    val pwm0 = Output(Bool())
    val interrupt = Output(Bool())
    val freeze = Input(Bool())
  })
}

class Axi4LiteTimerBlackbox extends BlackBox {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBlackboxBundle())
    val capturetrig0 = Input(Bool())
    val capturetrig1 = Input(Bool())
    val generateout0 = Output(Bool())
    val generateout1 = Output(Bool())
    val pwm0 = Output(Bool())
    val interrupt = Output(Bool())
    val freeze = Input(Bool())
  })

  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/VivadoProject"
    val ip_name = "Axi4LiteTimerBlackbox"
    val ip_name_lower = ip_name.toLowerCase()

    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")

    tcl_script.println(s"create_ip -name axi_timer -vendor xilinx.com -library ip -version 2.0 -module_name ${ip_name}")
    
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
