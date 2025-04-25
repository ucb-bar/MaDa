import chisel3._
import chisel3.util._

import java.io.PrintWriter


class Axi4LiteGpio extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBundle())
    val gpio_io_i = Input(UInt(32.W))
    val gpio_io_o = Output(UInt(32.W))
    val gpio_io_t = Output(UInt(32.W))
  })

  val blackbox = Module(new Axi4LiteGpioBlackbox())

  blackbox.io.s_axi_aclk := clock
  blackbox.io.s_axi_aresetn := ~reset.asBool
  blackbox.io.s_axi.connectFrom(io.s_axi)

  blackbox.io.gpio_io_i := io.gpio_io_i
  io.gpio_io_o := blackbox.io.gpio_io_o
  io.gpio_io_t := blackbox.io.gpio_io_t

  def attach(axi: Axi4LiteBundle): Unit = {
    io.s_axi <> axi
  }

  def attach(axi: Axi4Bundle): Unit = {
    io.s_axi.connectFromAxi4(axi)
  }
}

class Axi4LiteGpioBlackbox extends BlackBox {
  val io = IO(new Bundle {
    val s_axi_aclk = Input(Clock())
    val s_axi_aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4LiteBlackboxBundle())
    val gpio_io_i = Input(UInt(32.W))
    val gpio_io_o = Output(UInt(32.W))
    val gpio_io_t = Output(UInt(32.W))
  })
  
  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/VivadoProject"
    val ip_name = "Axi4LiteGpioBlackbox"
    val ip_name_lower = ip_name.toLowerCase()

    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")
    
    tcl_script.println(s"create_ip -name axi_gpio -vendor xilinx.com -library ip -version 2.0 -module_name ${ip_name}")

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
