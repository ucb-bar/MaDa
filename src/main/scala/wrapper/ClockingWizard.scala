import chisel3.{BlackBox, _}
import chisel3.util._

import java.io.PrintWriter


class ClockingWizard(
  clk_out1_freq: Int,
  clk_out2_freq: Int = 0,
  clk_out3_freq: Int = 0,
  clk_out4_freq: Int = 0,
  clk_out5_freq: Int = 0,
  clk_out6_freq: Int = 0,
  clk_out7_freq: Int = 0
) extends BlackBox {
  val io = IO(new Bundle {
    val clk_in1 = Input(Clock())
    val reset = Input(Bool())
    val locked = Output(Bool())
    val clk_out1 = Output(Clock())
    val clk_out2 = Output(Clock())
    val clk_out3 = Output(Clock())
    val clk_out4 = Output(Clock())
    val clk_out5 = Output(Clock())
    val clk_out6 = Output(Clock())
    val clk_out7 = Output(Clock())
  })

  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/VivadoProject"
    val ip_name = "ClockingWizard"
    val ip_name_lower = ip_name.toLowerCase()

    var num_out_clks = 1
    if (clk_out2_freq > 0) {
      num_out_clks += 1
    }
    if (clk_out3_freq > 0) {
      num_out_clks += 1
    }
    if (clk_out4_freq > 0) {
      num_out_clks += 1
    }
    if (clk_out5_freq > 0) {
      num_out_clks += 1
    }
    if (clk_out6_freq > 0) {
      num_out_clks += 1
    }
    if (clk_out7_freq > 0) {
      num_out_clks += 1
    }
    
    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")
    
    tcl_script.println(s"create_ip -name clk_wiz -vendor xilinx.com -library ip -version 6.0 -module_name ${ip_name}")

    tcl_script.println(s"""
set_property -dict [list \\
  CONFIG.NUM_OUT_CLKS {${num_out_clks}} \\
  CONFIG.MMCM_CLKFBOUT_MULT_F {10.000} \\
  CONFIG.MMCM_CLKOUT0_DIVIDE_F {8.000} \\
  CONFIG.MMCM_CLKOUT1_DIVIDE {40} \\
  CONFIG.CLKOUT1_USED {${clk_out1_freq > 0}} \\
  CONFIG.CLKOUT1_REQUESTED_OUT_FREQ {${clk_out1_freq}} \\
  CONFIG.CLKOUT1_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT1_REQUESTED_DUTY_CYCLE {50.0} \\
  CONFIG.CLKOUT2_USED {${clk_out2_freq > 0}} \\
  CONFIG.CLKOUT2_REQUESTED_OUT_FREQ {${clk_out2_freq}} \\
  CONFIG.CLKOUT2_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT2_REQUESTED_DUTY_CYCLE {50.0} \\
  CONFIG.CLKOUT3_USED {${clk_out3_freq > 0}} \\
  CONFIG.CLKOUT3_REQUESTED_OUT_FREQ {${clk_out3_freq}} \\
  CONFIG.CLKOUT3_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT3_REQUESTED_DUTY_CYCLE {50.0} \\
  CONFIG.CLKOUT4_USED {${clk_out4_freq > 0}} \\
  CONFIG.CLKOUT4_REQUESTED_OUT_FREQ {${clk_out4_freq}} \\
  CONFIG.CLKOUT4_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT4_REQUESTED_DUTY_CYCLE {50.0} \\
  CONFIG.CLKOUT5_USED {${clk_out5_freq > 0}} \\
  CONFIG.CLKOUT5_REQUESTED_OUT_FREQ {${clk_out5_freq}} \\
  CONFIG.CLKOUT5_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT5_REQUESTED_DUTY_CYCLE {50.0} \\
  CONFIG.CLKOUT6_USED {${clk_out6_freq > 0}} \\
  CONFIG.CLKOUT6_REQUESTED_OUT_FREQ {${clk_out6_freq}} \\
  CONFIG.CLKOUT6_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT6_REQUESTED_DUTY_CYCLE {50.0} \\
  CONFIG.CLKOUT7_USED {${clk_out7_freq > 0}} \\
  CONFIG.CLKOUT7_REQUESTED_OUT_FREQ {${clk_out7_freq}} \\
  CONFIG.CLKOUT7_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT7_REQUESTED_DUTY_CYCLE {50.0} \\
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
