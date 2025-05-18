package vivadoips

import chisel3._
import chisel3.util._
import builder.addVivadoIp


/**
  * Floating Point Multiplier-Adder
  *
  * @param pipelineStages
  */
case class FloatingPointConfig(
  /** Number of pipeline stages */
  pipelineStages: Int = 1,
  /** Floating point precision */
  width: Int = 32,
)

/**
  * Floating Point Multiplier-Adder
  *
  * @param config
  */
class FloatingPoint(
  val config: FloatingPointConfig = FloatingPointConfig()
) extends Module {
  val io = IO(new Bundle {
    val a = Flipped(Valid(UInt(config.width.W)))
    val b = Flipped(Valid(UInt(config.width.W)))
    val c = Flipped(Valid(UInt(config.width.W)))
    val result = Valid(UInt(config.width.W))
  })

  val blackbox = Module(new FloatingPointBlackbox(config))

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

class FloatingPointBlackbox(
  val config: FloatingPointConfig = FloatingPointConfig()
) extends BlackBox {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    val s_axis_a_tvalid = Input(Bool())
    val s_axis_a_tdata = Input(UInt(config.width.W))
    val s_axis_b_tvalid = Input(Bool())
    val s_axis_b_tdata = Input(UInt(config.width.W))
    val s_axis_c_tvalid = Input(Bool())
    val s_axis_c_tdata = Input(UInt(config.width.W))
    val m_axis_result_tvalid = Output(Bool())
    val m_axis_result_tdata = Output(UInt(config.width.W))
  })

  val exponentWidth = if (config.width == 16) 5 else 8
  val fractionWidth = if (config.width == 16) 11 else 24
  val precisionType = if (config.width == 16) "Half" else "Single"

  val ipName = "FloatingPointBlackbox"
  addVivadoIp(
    name="floating_point",
    vendor="xilinx.com",
    library="ip",
    version="7.1",
    moduleName=ipName,
    extra = s"""
set_property -dict [list \\
  CONFIG.Add_Sub_Value {Add} \\
  CONFIG.A_Precision_Type {${precisionType}} \\
  CONFIG.C_A_Exponent_Width {${exponentWidth}} \\
  CONFIG.C_A_Fraction_Width {${fractionWidth}} \\
  CONFIG.C_Accum_Input_Msb {15} \\
  CONFIG.C_Accum_Lsb {-24} \\
  CONFIG.C_Accum_Msb {32} \\
  CONFIG.C_Latency {${config.pipelineStages}} \\
  CONFIG.C_Mult_Usage {Full_Usage} \\
  CONFIG.C_Optimization {Speed_Optimized} \\
  CONFIG.C_Rate {1} \\
  CONFIG.Result_Precision_Type {${precisionType}} \\
  CONFIG.C_Result_Exponent_Width {${exponentWidth}} \\
  CONFIG.C_Result_Fraction_Width {${fractionWidth}} \\
  CONFIG.Flow_Control {NonBlocking} \\
  CONFIG.Has_RESULT_TREADY {false} \\
  CONFIG.Maximum_Latency {false} \\
  CONFIG.Operation_Type {FMA} \\
] [get_ips ${ipName}]
"""
  )
}
