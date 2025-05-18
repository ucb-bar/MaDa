package vivadoips

import chisel3._
import chisel3.util._
import chisel3.experimental.Analog



class NexysVideoIO extends Bundle {
  val clk = Input(Clock())

  val led = Output(UInt(8.W))

  val btnc = Input(Bool())
  val btnd = Input(Bool())
  val btnl = Input(Bool())
  val btnr = Input(Bool())
  val btnu = Input(Bool())
  val cpu_resetn = Input(Bool())

  val sw = Input(UInt(8.W))

  val ja = Vec(8, Analog(1.W))
  val jb = Vec(8, Analog(1.W))
  val jc = Vec(8, Analog(1.W))

  val uart_rxd_out = Output(Bool())
  val uart_txd_in = Input(Bool())
  
  // Inouts
  val ddr3_dq = Analog(16.W)
  val ddr3_dqs_n = Analog(2.W)
  val ddr3_dqs_p = Analog(2.W)
  // Outputs
  val ddr3_addr = Output(UInt(15.W))
  val ddr3_ba = Output(UInt(3.W))
  val ddr3_ras_n = Output(Bool())
  val ddr3_cas_n = Output(Bool())
  val ddr3_we_n = Output(Bool())
  val ddr3_reset_n = Output(Bool())
  val ddr3_ck_p = Output(UInt(1.W))
  val ddr3_ck_n = Output(UInt(1.W))
  val ddr3_cke = Output(UInt(1.W))
  val ddr3_dm = Output(UInt(2.W))
  val ddr3_odt = Output(UInt(1.W))
}


class NexysVideoTop extends RawModule {
  val io = IO(new NexysVideoIO())
}
