import chisel3._
import chisel3.util._


class BiliArty100T extends ExampleArty100TShell {
  
  instantiate_clk_reset(clk_freqs = Seq(50, 25))

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
