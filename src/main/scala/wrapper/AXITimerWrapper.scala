import chisel3.{BlackBox, _}
import chisel3.util._


class axi_timer_0 extends BlackBox {
  val io = IO(new Bundle {
    val s_axi = Flipped(new RawAXI4Lite())
    val capturetrig0 = Input(Bool())
    val capturetrig1 = Input(Bool())
    val generateout0 = Output(Bool())
    val generateout1 = Output(Bool())
    val pwm0 = Output(Bool())
    val interrupt = Output(Bool())
    val freeze = Input(Bool())
  })
}
