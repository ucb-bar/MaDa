package chipyard_wrapper

import chisel3._
import chisel3.util._
import chisel3.experimental.Analog
import prci.SyncReset
import vivadoips.{ClockingWizard, Arty100TIO, IOBUF, Axi4LiteGpio}
import builder.{addConstraintResource, addSimulationResource, addResourceByFilelist}


object analogAsInput {
  def apply(io: Analog): Bool = {
    val buf = Module(new IOBUF())
    buf.io.IO <> io
    buf.io.T := true.B
    buf.io.I := DontCare
    buf.io.O
  }
}

object outputAsAnalog {
  def apply(bool: Bool): Analog = {
    val buf = Module(new IOBUF())
    buf.io.I := bool
    buf.io.T := false.B
    buf.io.IO
  }
}


class TacitRocketConfigOhYeah extends RawModule {
  val io = IO(new Arty100TIO())

  io := DontCare

  val clock = Wire(Clock())
  val reset = Wire(Bool())

  // clock domain generation
  val clk_wiz = Module(new ClockingWizard(
    clk_freqs=Seq(50),
  ))
  clk_wiz.io.clk_in := io.CLK100MHZ
  clk_wiz.io.reset := !io.ck_rst
  reset := !clk_wiz.io.locked
  clock := clk_wiz.io.clk_outs(0)

  // simpler connection
  // clock := io.CLK100MHZ.asClock
  // reset := !io.ck_rst

  
  val jtag_tck = analogAsInput(io.jd(2))
  // sync reset connection

  val chiptop = Module(new ChipTop())


  // main system clock and reset
  chiptop.io.clock_uncore := clock
  chiptop.io.reset_io := reset

  
  // jtag connection
  chiptop.io.jtag_TCK := jtag_tck.asClock
  chiptop.io.jtag_TMS := analogAsInput(io.jd(5))
  chiptop.io.jtag_TDI := analogAsInput(io.jd(4))
  io.jd(0) <> outputAsAnalog(chiptop.io.jtag_TDO)
  chiptop.io.jtag_reset := !reset
  
  // memory interface
  // withClockAndReset(chiptop.io.axi4_mem_0_clock, reset) {
  withClockAndReset(clock, reset) {
    val gpio = Module(new Axi4LiteGpio())
    gpio.io.s_axi.connectFromAxi4(chiptop.io.axi4_mem_0_bits)
    
    gpio.io.gpio_io_i := 0x05050505.U
    io.led := gpio.io.gpio_io_o
  }

  // chiptop.io.axi4_mem_0_bits := DontCare
  // chiptop.io.axi4_mem_0_bits.aw.ready := true.B
  // chiptop.io.axi4_mem_0_bits.w.ready := true.B
  // chiptop.io.axi4_mem_0_bits.ar.ready := true.B

  // custom boot
  chiptop.io.custom_boot := false.B

  // serial TileLink interface
  chiptop.io.serial_tl_0.clock_in := clock
  chiptop.io.serial_tl_0.in.valid := false.B
  chiptop.io.serial_tl_0.in.bits.phit := 0.U(32.W)
  chiptop.io.serial_tl_0.out.ready := true.B

  io.uart_rxd_out := chiptop.io.uart_0_txd
  chiptop.io.uart_0_rxd := io.uart_txd_in

  // debug clock output
  // io.clock_tap
  
  addConstraintResource("package-vivado-ips/resources/constraints/Arty-A7-100-Master.xdc")

  addResourceByFilelist("/home/tk/Desktop/chipyard-for-mada/sims/verilator/generated-src/chipyard.harness.TestHarness.TacitRocketConfigOhYeah/gen-collateral/filelist.f")
  addSimulationResource("package-chipyard-wrapper/test/TacitRocketConfigOhYeahTestbench.sv")
}
