import chisel3._
import chisel3.util._


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
}
