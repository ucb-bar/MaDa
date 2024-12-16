import chisel3._
import chisel3.util._


class RGBLEDIO extends Bundle {
  val r = Bool()
  val g = Bool()
  val b = Bool()
}

class Arty100TIO extends Bundle {
  val CLK100MHZ = Input(Clock())

  val sw = Input(UInt(4.W))
  
  val led0 = Output(new RGBLEDIO())
  val led1 = Output(new RGBLEDIO())
  val led2 = Output(new RGBLEDIO())
  val led3 = Output(new RGBLEDIO())

  val led = Output(UInt(4.W))

  val btn = Input(UInt(4.W))

  val ja = Input(UInt(8.W))
  val jb = Input(UInt(8.W))
  val jc = Input(UInt(8.W))
  val jd = Input(UInt(8.W))

  val uart_rxd_out = Output(Bool())
  val uart_txd_in = Input(Bool())

  val ck_ioa = Input(Bool())
  val ck_rst = Input(Bool())
}


class Arty100TShell extends RawModule {
  val io = IO(new Arty100TIO())
}

class ExampleArty100TShell extends Arty100TShell {

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
