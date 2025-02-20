import chisel3._
import chisel3.util._
import chisel3.experimental.Analog


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
  // val jd = Analog(8.W)
  val jd_0 = Output(Bool())
  val jd_1 = Input(Bool())
  val jd_2 = Input(Bool())
  val jd_3 = Output(Bool())
  val jd_4 = Input(Bool())
  val jd_5 = Input(Bool())
  val jd_6 = Input(Bool())
  val jd_7 = Input(Bool())

  val uart_rxd_out = Output(Bool())
  val uart_txd_in = Input(Bool())

  val ck_ioa = Input(Bool())
  val ck_rst = Input(Bool())

  val eth_col = Input(Bool())
  val eth_crs = Input(Bool())
  // val eth_mdc = Output(Bool())
  // val eth_mdio = Input(Bool())
  val eth_ref_clk = Output(Clock())
  val eth_rstn = Output(Bool())
  val eth_rx_clk = Input(Bool())
  val eth_rx_dv = Input(Bool())
  val eth_rxd = Input(UInt(4.W))
  val eth_rxerr = Input(Bool())
  val eth_tx_clk = Input(Bool())
  val eth_tx_en = Output(Bool())
  val eth_txd = Output(UInt(4.W))
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

    io.jd_0 := false.B
    io.jd_3 := false.B

    io.uart_rxd_out := io.uart_txd_in

    io.eth_ref_clk := clock
    io.eth_rstn := false.B
    io.eth_tx_en := false.B
    io.eth_txd := 0.U
  }

  def instantiate_clk_reset(clk_freqs: Seq[Int]) = {
    val clk_wiz = Module(new ClockingWizard(clk_freqs))
    clk_wiz.io.clk_in := io.CLK100MHZ
    clk_wiz.io.reset := ~io.ck_rst
    clock := clk_wiz.io.clk_outs(0)

    val sync_reset = Module(new SyncReset())
    sync_reset.io.clock := clock
    sync_reset.io.reset := ~clk_wiz.io.locked
    reset := sync_reset.io.out
  }
}
