import chisel3._
import chisel3.util._


class ClockDivider extends Module {
  val io = IO(new Bundle {
    val clk_in = Input(Clock())
    val reset = Input(Bool())
    val divider = Input(UInt(32.W))
    val clk_out = Output(Clock())
  })

  val counter = RegInit(0.U(32.W))
  val clk_reg = RegInit(false.B)

  withClockAndReset(io.clk_in, io.reset) {
    when(counter === (io.divider - 1.U)) {
      counter := 0.U
      clk_reg := ~clk_reg
    }.otherwise {
      counter := counter + 1.U
    }
  }

  io.clk_out := clk_reg.asClock
}
