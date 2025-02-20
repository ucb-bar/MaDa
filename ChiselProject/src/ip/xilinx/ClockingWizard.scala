import chisel3.{BlackBox, _}
import chisel3.util._

import java.io.PrintWriter


class ClockingWizard(
  clk_freqs: Seq[Int]
) extends BlackBox {
  require(clk_freqs.length > 0, "clk_freqs must be non-empty")
  require(clk_freqs.length <= 8, "clk_freqs must be at most 8")
  val io = IO(new Bundle {
    val clk_in = Input(Clock())
    val reset = Input(Bool())
    val locked = Output(Bool())
    val clk_outs = Output(Vec(clk_freqs.length, Clock()))
  })

  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/VivadoProject"
    val ip_name = "ClockingWizard"
    val ip_name_lower = ip_name.toLowerCase()

    val num_out_clks = clk_freqs.length
    def get_used(i: Int) = i < clk_freqs.length
    def get_freq(i: Int) = if (get_used(i)) clk_freqs(i) else 0
    
    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")
    
    tcl_script.println(s"create_ip -name clk_wiz -vendor xilinx.com -library ip -version 6.0 -module_name ${ip_name}")

    tcl_script.println(s"""
set_property -dict [list \\
  CONFIG.NUM_OUT_CLKS {${num_out_clks}} \\
  CONFIG.MMCM_CLKFBOUT_MULT_F {10.000} \\
  CONFIG.MMCM_CLKOUT0_DIVIDE_F {8.000} \\
  CONFIG.MMCM_CLKOUT1_DIVIDE {40} \\
  CONFIG.CLKOUT1_USED {${get_used(0)}} \\
  CONFIG.CLKOUT1_REQUESTED_OUT_FREQ {${get_freq(0)}} \\
  CONFIG.CLKOUT1_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT1_REQUESTED_DUTY_CYCLE {50.0} \\
  CONFIG.CLKOUT2_USED {${get_used(1)}} \\
  CONFIG.CLKOUT2_REQUESTED_OUT_FREQ {${get_freq(1)}} \\
  CONFIG.CLKOUT2_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT2_REQUESTED_DUTY_CYCLE {50.0} \\
  CONFIG.CLKOUT3_USED {${get_used(2)}} \\
  CONFIG.CLKOUT3_REQUESTED_OUT_FREQ {${get_freq(2)}} \\
  CONFIG.CLKOUT3_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT3_REQUESTED_DUTY_CYCLE {50.0} \\
  CONFIG.CLKOUT4_USED {${get_used(3)}} \\
  CONFIG.CLKOUT4_REQUESTED_OUT_FREQ {${get_freq(3)}} \\
  CONFIG.CLKOUT4_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT4_REQUESTED_DUTY_CYCLE {50.0} \\
  CONFIG.CLKOUT5_USED {${get_used(4)}} \\
  CONFIG.CLKOUT5_REQUESTED_OUT_FREQ {${get_freq(4)}} \\
  CONFIG.CLKOUT5_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT5_REQUESTED_DUTY_CYCLE {50.0} \\
  CONFIG.CLKOUT6_USED {${get_used(5)}} \\
  CONFIG.CLKOUT6_REQUESTED_OUT_FREQ {${get_freq(5)}} \\
  CONFIG.CLKOUT6_REQUESTED_PHASE {0.0} \\
  CONFIG.CLKOUT6_REQUESTED_DUTY_CYCLE {50.0} \\
  CONFIG.CLKOUT7_USED {${get_used(6)}} \\
  CONFIG.CLKOUT7_REQUESTED_OUT_FREQ {${get_freq(6)}} \\
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
