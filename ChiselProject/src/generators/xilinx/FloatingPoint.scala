import chisel3._
import chisel3.util._

import java.io.PrintWriter


class FloatingPoint extends Module {
  val io = IO(new Bundle {
    val a = Flipped(Valid(UInt(32.W)))
    val b = Flipped(Valid(UInt(32.W)))
    val c = Flipped(Valid(UInt(32.W)))
    val result = Valid(UInt(32.W))
  })

  val blackbox = Module(new FloatingPointBlackbox())

  blackbox.io.aclk := clock
  blackbox.io.s_axis_a_tvalid := io.a.valid
  blackbox.io.s_axis_a_tdata := io.a.bits
  blackbox.io.s_axis_b_tvalid := io.b.valid
  blackbox.io.s_axis_b_tdata := io.b.bits
  blackbox.io.s_axis_c_tvalid := io.c.valid
  blackbox.io.s_axis_c_tdata := io.c.bits
  io.result.valid := blackbox.io.m_axis_result_tvalid
  io.result.bits := blackbox.io.m_axis_result_tdata
}

class FloatingPointBlackbox extends BlackBox {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    val s_axis_a_tvalid = Input(Bool())
    val s_axis_a_tdata = Input(UInt(32.W))
    val s_axis_b_tvalid = Input(Bool())
    val s_axis_b_tdata = Input(UInt(32.W))
    val s_axis_c_tvalid = Input(Bool())
    val s_axis_c_tdata = Input(UInt(32.W))
    val m_axis_result_tvalid = Output(Bool())
    val m_axis_result_tdata = Output(UInt(32.W))
  })
  
  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/VivadoProject"
    val ip_name = "FloatingPointBlackbox"
    val ip_name_lower = ip_name.toLowerCase()

    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")

    tcl_script.println(s"create_ip -name floating_point -vendor xilinx.com -library ip -version 7.1 -module_name ${ip_name}")

    tcl_script.println(s"""
set_property -dict [list \\
  CONFIG.A_Precision_Type {Single} \\
  CONFIG.Add_Sub_Value {Add} \\
  CONFIG.C_A_Exponent_Width {8} \\
  CONFIG.C_A_Fraction_Width {24} \\
  CONFIG.C_Latency {1} \\
  CONFIG.C_Mult_Usage {Full_Usage} \\
  CONFIG.C_Optimization {Speed_Optimized} \\
  CONFIG.C_Rate {1} \\
  CONFIG.C_Result_Exponent_Width {8} \\
  CONFIG.C_Result_Fraction_Width {24} \\
  CONFIG.Flow_Control {NonBlocking} \\
  CONFIG.Has_RESULT_TREADY {false} \\
  CONFIG.Maximum_Latency {false} \\
  CONFIG.Operation_Type {FMA} \\
  CONFIG.Result_Precision_Type {Single} \\
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
