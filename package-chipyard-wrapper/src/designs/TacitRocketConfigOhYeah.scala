package chipyard_wrapper

import chisel3._
import chisel3.util._
import chisel3.experimental.Analog
import prci.SyncReset
import vivadoips.{ClockingWizard, NexysVideoIO, IOBUF, Axi4LiteGpio, IBUF, OBUF, Axi4Mig}
import builder.{addConstraintResource, addSimulationResource, addResourceByFilelist}


// object analogAsInput {
//   def apply(io: Analog): Bool = {
//     val buf = Module(new IOBUF())
//     buf.io.IO <> io
//     buf.io.T := true.B
//     buf.io.I := DontCare
//     buf.io.O
//   }
// }
object analogAsInput {
  def apply(io: Analog): Bool = {
    val buf = Module(new IBUF(io.cloneType))
    buf.io.I <> io
    buf.io.O
  }
}

object outputAsAnalog {
  def apply(bool: Bool): Analog = {
    val buf = Module(new OBUF())
    buf.io.I := bool
    buf.io.O
  }
}

/**
 *  ```bash
 *  make bitstream PACKAGE=chipyard-wrapper DESIGN=chipyard_wrapper.TacitRocketConfigOhYeah
 *  ```
 */
class TacitRocketConfigOhYeah extends RawModule {
  val io = IO(new NexysVideoIO())

  io := DontCare

  val clock = Wire(Clock())
  val reset = Wire(Bool())

  // clock domain generation
  val clk_wiz = Module(new ClockingWizard(
    clk_freqs=Seq(20, 100, 200),
  ))
  clk_wiz.io.clk_in := io.clk
  clk_wiz.io.reset := !io.cpu_resetn
  reset := !clk_wiz.io.locked
  clock := clk_wiz.io.clk_outs(0)

  val clock_100 = clk_wiz.io.clk_outs(1)
  val clock_200 = clk_wiz.io.clk_outs(2)

  // simpler connection
  // clock := io.CLK100MHZ.asClock
  // reset := !io.ck_rst

  
  val jtag_tck = analogAsInput(io.jc(2))
  // sync reset connection

  val chiptop = Module(new ChipTop())

  withClockAndReset(clock, reset) {
    val mig = Module(new Axi4Mig())

    mig.io.sys_clk_i := clock_100
    mig.io.clk_ref_i := clock_200
    
    // Inouts
    io.ddr3_dq <> mig.io.ddr3_dq
    io.ddr3_dqs_n <> mig.io.ddr3_dqs_n
    io.ddr3_dqs_p <> mig.io.ddr3_dqs_p
    // Outputs
    io.ddr3_addr := mig.io.ddr3_addr
    io.ddr3_ba := mig.io.ddr3_ba
    io.ddr3_ras_n := mig.io.ddr3_ras_n
    io.ddr3_cas_n := mig.io.ddr3_cas_n
    io.ddr3_we_n := mig.io.ddr3_we_n
    io.ddr3_reset_n := mig.io.ddr3_reset_n
    io.ddr3_ck_p := mig.io.ddr3_ck_p
    io.ddr3_ck_n := mig.io.ddr3_ck_n
    io.ddr3_cke := mig.io.ddr3_cke
    io.ddr3_dm := mig.io.ddr3_dm
    io.ddr3_odt := mig.io.ddr3_odt
  
    mig.io.s_axi <> chiptop.io.axi4_mem_0_bits
  }

  // main system clock and reset
  chiptop.io.clock_uncore := clock
  chiptop.io.reset_io := reset

  
  // jtag connection
  chiptop.io.jtag_TCK := jtag_tck.asClock
  chiptop.io.jtag_TMS := analogAsInput(io.jc(5))
  chiptop.io.jtag_TDI := analogAsInput(io.jc(4))
  io.jc(0) <> outputAsAnalog(chiptop.io.jtag_TDO)
  chiptop.io.jtag_reset := false.B
  
  // memory interface
  // withClockAndReset(chiptop.io.axi4_mem_0_clock, reset) {
  withClockAndReset(chiptop.io.periph_axi4_aclk, !chiptop.io.periph_axi4_aresetn) {
    val gpio = Module(new Axi4LiteGpio())
    gpio.io.s_axi <> chiptop.io.periph_axi4
    
    gpio.io.gpio_io_i := 0x05050505.U
    io.led := gpio.io.gpio_io_o
  }

  
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
  
  addConstraintResource("package-vivado-ips/resources/constraints/Nexys-Video-Master.xdc")

  addResourceByFilelist("/home/tk/Desktop/chipyard-for-mada/sims/verilator/generated-src/chipyard.harness.TestHarness.TacitRocketConfigOhYeah/gen-collateral/filelist.f")
  addSimulationResource("package-chipyard-wrapper/test/TacitRocketConfigOhYeahTestbench.sv")
}
