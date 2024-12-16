import chisel3._
import chisel3.util._


class TinyRocketArty100T extends Arty100TShell {

  withClockAndReset(io.CLK100MHZ, io.ck_rst) {

    io.led0.r := io.btn(0)
    io.led0.g := false.B
    io.led0.b := io.sw(0)

    io.led1.r := io.btn(1)
    io.led1.g := false.B
    io.led1.b := io.sw(1)

    io.led2.r := io.btn(2)
    io.led2.g := false.B
    io.led2.b := io.sw(2)

    io.led3.r := io.btn(3)
    io.led3.g := false.B
    io.led3.b := io.sw(3)

    io.uart_rxd_out := io.uart_txd_in
  }
}
