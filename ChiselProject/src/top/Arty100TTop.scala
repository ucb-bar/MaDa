import chisel3._
import chisel3.util._
import chisel3.experimental.Analog


class RgbLedIO extends Bundle {
  val r = Bool()
  val g = Bool()
  val b = Bool()
}

class Arty100TIO extends Bundle {
  val CLK100MHZ = Input(Clock())

  val sw = Input(UInt(4.W))
  
  val led0 = Output(new RgbLedIO())
  val led1 = Output(new RgbLedIO())
  val led2 = Output(new RgbLedIO())
  val led3 = Output(new RgbLedIO())

  val led = Output(UInt(4.W))

  val btn = Input(UInt(4.W))

  val ja = Vec(8, Analog(1.W))
  val jb = Vec(8, Analog(1.W))
  val jc = Vec(8, Analog(1.W))
  val jd = Vec(8, Analog(1.W))

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


class Arty100TTop extends RawModule {
  val io = IO(new Arty100TIO())
}
