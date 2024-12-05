import chisel3.{BlackBox, _}
import chisel3.util._


class clk_wiz_0 extends BlackBox {
  val io = IO(new Bundle {
    val clk_in1 = Input(Clock())
    val reset = Input(Bool())
    val locked = Output(Bool())
    val clk_out1 = Output(Clock())
  })
}
