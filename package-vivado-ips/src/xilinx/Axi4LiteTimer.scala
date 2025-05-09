import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4LiteBundle}


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

  val blackbox = Module(new Axi4LiteTimerBlackbox())

  blackbox.io.s_axi_aclk := clock
  blackbox.io.s_axi_aresetn := ~reset.asBool
  blackbox.io.s_axi.connectFrom(io.s_axi)
  blackbox.io.capturetrig0 := io.capturetrig0
  blackbox.io.capturetrig1 := io.capturetrig1
  blackbox.io.freeze := io.freeze
  io.generateout0 := blackbox.io.generateout0
  io.generateout1 := blackbox.io.generateout1
  io.pwm0 := blackbox.io.pwm0
  io.interrupt := blackbox.io.interrupt
}

class Axi4LiteTimerBlackbox extends BlackBox {
  val io = IO(new Bundle {
    val s_axi_aclk = Input(Clock())
    val s_axi_aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4LiteBlackboxBundle())
    val capturetrig0 = Input(Bool())
    val capturetrig1 = Input(Bool())
    val generateout0 = Output(Bool())
    val generateout1 = Output(Bool())
    val pwm0 = Output(Bool())
    val interrupt = Output(Bool())
    val freeze = Input(Bool())
  })

  val ipName = "Axi4LiteTimerBlackbox"
  addVivadoIp(
    name="axi_timer",
    vendor="xilinx.com",
    library="ip",
    version="2.0",
    moduleName=ipName,
    extra = s"""
set_property -dict [list \\
  CONFIG.COUNT_WIDTH {32} \\
] [get_ips ${ipName}]
"""
  )
}
