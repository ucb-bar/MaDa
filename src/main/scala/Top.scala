import chisel3._
import chisel3.util._


class Top extends RawModule {
  val CLK100MHZ = IO(Input(Clock()))
  val ck_rst = IO(Input(Bool()))
  val led = IO(Output(UInt(1.W)))

  val clock = CLK100MHZ
  val reset = ~ck_rst

  // add implicit reset and clock to the module
  withClockAndReset(clock, reset) {
    val clk_divider = Module(new ClockDivider)

    clk_divider.io.clk_in := clock
    clk_divider.io.reset := reset
    clk_divider.io.divider := 2.U

    led := clk_divider.io.clk_out.asUInt
  }
}
