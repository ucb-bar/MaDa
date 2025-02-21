import chisel3._
import chisel3.util._


class BiliArty100T extends RawModule {
  val io = IO(new Arty100TIO())

  io := DontCare

  val clock = Wire(Clock())
  val reset = Wire(Reset())
  
  val pll_locked = Wire(Bool())


  val clk_wiz = Module(new ClockingWizard(25))
  // clocking wizard connection
  clk_wiz.io.clk_in1 := io.CLK100MHZ
  clk_wiz.io.reset := ~io.ck_rst
  pll_locked := clk_wiz.io.locked
  clock := clk_wiz.io.clk_out1


  val sync_reset = Module(new SyncReset())
  // sync reset connection
  sync_reset.io.clock := clock
  sync_reset.io.reset := ~pll_locked
  reset := sync_reset.io.out

  withClockAndReset(clock, reset) {
    val reset_vector = RegInit(0x00000000.U(32.W))

    val tile = Module(new Tile())

    val axi_gpio = Module(new Axi4LiteGpio())
    
    tile.io.reset_vector := reset_vector
    
    tile.io.sbus <> axi_gpio.io.s_axi
    
    axi_gpio.io.gpio_io_i := 0x05050505.U
    io.led := axi_gpio.io.gpio_io_o
    
    // io.debug := tile.io.debug
  }
}
