import chisel3.{BlackBox, _}
import chisel3.util._


class ClockingWizard(
  clk_freqs: Seq[Int]
) extends RawModule {
  val io = IO(new Bundle {
    val clk_in = Input(Clock())
    val reset = Input(Bool())
    val locked = Output(Bool())
    val clk_outs = Output(Vec(clk_freqs.length, Clock()))
  })
  require(clk_freqs.length > 0, "clk_freqs must be non-empty")
  require(clk_freqs.length <= 7, "clk_freqs must be at most 7")
  val blackbox = Module(new ClockingWizardBlackbox(clk_freqs))
  blackbox.io.clk_in1 := io.clk_in
  blackbox.io.reset := io.reset
  io.locked := blackbox.io.locked
  io.clk_outs(0) := blackbox.io.clk_out1
  if (clk_freqs.length > 1) { io.clk_outs(1) := blackbox.io.clk_out2 }
  if (clk_freqs.length > 2) { io.clk_outs(2) := blackbox.io.clk_out3 }
  if (clk_freqs.length > 3) { io.clk_outs(3) := blackbox.io.clk_out4 }
  if (clk_freqs.length > 4) { io.clk_outs(4) := blackbox.io.clk_out5 }
  if (clk_freqs.length > 5) { io.clk_outs(5) := blackbox.io.clk_out6 }
  if (clk_freqs.length > 6) { io.clk_outs(6) := blackbox.io.clk_out7 }
}

class ClockingWizardBlackbox(
  clk_freqs: Seq[Int]
) extends BlackBox {

  def get_used(i: Int) = i < clk_freqs.length
  def get_freq(i: Int) = if (get_used(i)) clk_freqs(i) else 0

  val io = IO(new Bundle {
    val clk_in1 = Input(Clock())
    val reset = Input(Bool())
    val locked = Output(Bool())
    val clk_out1 = Output(Clock())
    val clk_out2 = if (get_used(1)) Output(Clock()) else null
    val clk_out3 = if (get_used(2)) Output(Clock()) else null
    val clk_out4 = if (get_used(3)) Output(Clock()) else null
    val clk_out5 = if (get_used(4)) Output(Clock()) else null
    val clk_out6 = if (get_used(5)) Output(Clock()) else null
    val clk_out7 = if (get_used(6)) Output(Clock()) else null
  })


  val ipName = "ClockingWizardBlackbox"
  addVivadoIp(
    name="clk_wiz",
    vendor="xilinx.com",
    library="ip",
    version="6.0",
    moduleName=ipName,
    extra = s"""
set_property -dict [list \\
  CONFIG.NUM_OUT_CLKS {${clk_freqs.length}} \\
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
] [get_ips ${ipName}]
"""
  )
}
