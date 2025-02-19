import chisel3._
import chisel3.util._


class BiliArty100T extends RawModule {
  val io = IO(new Arty100TIO())

  io := DontCare

  val clock = Wire(Clock())
  val reset = Wire(Bool())
  
  val pll_locked = Wire(Bool())

  val clock_25 = Wire(Clock())


  val clk_wiz = Module(new ClockingWizard(50, 25))
  // clocking wizard connection
  clk_wiz.io.clk_in1 := io.CLK100MHZ
  clk_wiz.io.reset := ~io.ck_rst
  pll_locked := clk_wiz.io.locked
  clock := clk_wiz.io.clk_out1
  clock_25 := clk_wiz.io.clk_out2


  val sync_reset = Module(new SyncReset())
  // sync reset connection
  sync_reset.io.clock := clock
  sync_reset.io.reset := ~pll_locked
  reset := sync_reset.io.out
}
