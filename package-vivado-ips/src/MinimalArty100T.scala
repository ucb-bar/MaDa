package vivadoips

import chisel3._
import chisel3.util._
import chisel3.experimental.Analog


class MinimalArty100T extends RawModule {
  val io = IO(new Arty100TIO())

  io := DontCare

  val clock = Wire(Clock())
  val reset = Wire(Bool())

  clock := io.CLK100MHZ
  reset := ~io.ck_rst


  withClockAndReset(clock, reset) {
    val counter = RegInit(0.U(32.W))
    counter := counter + 1.U

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

    io.led := counter(28, 25)

    io.uart_rxd_out := io.uart_txd_in
  }
}
