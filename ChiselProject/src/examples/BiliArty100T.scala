import chisel3._
import chisel3.util._


class BiliArty100T extends ExampleArty100TShell {
  
  val pll_locked = Wire(Bool())

  val clk_wiz = Module(new ClockingWizard(20))
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
    val reset_vector = RegInit(0x08000000.U(32.W))

    val tile = Module(new Tile())

    val gpio = Module(new Axi4LiteGpio())
    val uart = Module(new Axi4LiteUartLite())

    val sbus_crossbar = Module(new Axi4LiteCrossbar(
      numSlave = 1,
      numMaster = 2,
      device0Size = 0x1000,
      device0Address = 0x10000000,
      device1Size = 0x1000,
      device1Address = 0x10001000,
    ))

    tile.io.reset_vector := reset_vector
    
    sbus_crossbar.io.s_axi(0) <> Axi4ToAxi4Lite(tile.io.sbus)
    gpio.attach(sbus_crossbar.io.m_axi(0))
    uart.attach(sbus_crossbar.io.m_axi(1))
    
    gpio.io.gpio_io_i := 0x05050505.U
    io.led := gpio.io.gpio_io_o

    io.uart_rxd_out := uart.io.tx
    uart.io.rx := io.uart_txd_in

    io.ja := tile.io.debug.tohost(7, 0)
  }
}
