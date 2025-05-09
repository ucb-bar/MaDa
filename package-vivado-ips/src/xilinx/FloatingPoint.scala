import chisel3._
import chisel3.util._

import java.io.PrintWriter


class FloatingPoint(
  val pipelineStages: Int = 1
) extends Module {
  val io = IO(new Bundle {
    val a = Flipped(Valid(UInt(32.W)))
    val b = Flipped(Valid(UInt(32.W)))
    val c = Flipped(Valid(UInt(32.W)))
    val result = Valid(UInt(32.W))
  })

  val blackbox = Module(new FloatingPointBlackbox(pipelineStages=pipelineStages))

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
  val pipelineStages: Int = 1
) extends BlackBox {
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

  val ipName = "FloatingPointBlackbox"
  addVivadoIp(
    name="floating_point",
    vendor="xilinx.com",
    library="ip",
    version="7.1",
    moduleName=ipName,
    extra = s"""
set_property -dict [list \\
  CONFIG.A_Precision_Type {Single} \\
  CONFIG.Add_Sub_Value {Add} \\
  CONFIG.C_A_Exponent_Width {8} \\
  CONFIG.C_A_Fraction_Width {24} \\
  CONFIG.C_Latency {${pipelineStages}} \\
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
] [get_ips ${ipName}]
"""
  )
}
