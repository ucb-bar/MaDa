package delta

import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4Bundle, Axi4LiteBundle}
import prci.SyncReset
import vivadoips.{
  Arty100TIO,
  NexysVideoIO,
  Axi4Crossbar,
  Axi4DataWidthConverter,
  Axi4LiteCrossbar,
  Axi4LiteTimer,
  Axi4LiteTimerConfig,
  Axi4LiteUartLite,
  Axi4LiteUartLiteConfig,
  Axi4LiteGpio,
  Axi4QuadSpiFlash,
  Axi4SpiFlash,
  ClockingWizard,
  IOBUF,
  Axi4Mig,
}
import builder.{addConstraintResource, addSimulationResource}


class DeltaWithDram extends RawModule {
  val io = IO(new NexysVideoIO())
  val config = new SoCConfig(
    tile = new TileConfig(
      sbusFrequency = 50,
    )
  )

  io := DontCare

  val clock = Wire(Clock())
  val reset = Wire(Bool())
  
  val pll_locked = Wire(Bool())

  val clk_wiz = Module(new ClockingWizard(Seq(config.tile.sbusFrequency, 100, 200)))
  // clocking wizard connection
  clk_wiz.io.clk_in := io.clk
  clk_wiz.io.reset := ~io.cpu_resetn
  pll_locked := clk_wiz.io.locked
  clock := clk_wiz.io.clk_outs(0)
  val clock_100 = clk_wiz.io.clk_outs(1)
  val clock_200 = clk_wiz.io.clk_outs(2)


  val sync_reset = Module(new SyncReset())
  // sync reset connection
  sync_reset.io.clock := clock
  sync_reset.io.reset := ~pll_locked
  reset := sync_reset.io.out


  withClockAndReset(clock, reset) {
    val reset_vector = RegInit(0x0800_0000.U(32.W))

    val tile = Module(new Tile(config.tile))

    tile.io.reset_vector := reset_vector

    tile.io.debug := DontCare

    // val pbus_crossbar = Module(new Axi4LiteCrossbar(
    //   numSlave=1,
    //   numMaster=3,
    //   // params=Axi4Params(
    //   //   dataWidth=32,
    //   //   idWidth=4,
    //   // ),
    //   deviceSizes=Array(
    //     0x0000_0400,  // UART
    //     0x0000_0400,  // GPIO
    //     0x0000_0400,  // TIMER
    //   ),
    //   deviceAddresses=Array(
    //     0x1001_0000,   // GPIO
    //     0x1002_0000,   // UART
    //     0x1004_0000,   // TIMER
    //   ),
    // ))

    val gpio = Module(new Axi4LiteGpio())
    // val uart = Module(new Axi4LiteUartLite(Axi4LiteUartLiteConfig(axiClockFrequency=config.tile.sbusFrequency)))

    // pbus_crossbar.io.s_axi(0).connectFromAxi4(tile.io.pbus)

    // gpio.io.s_axi <> pbus_crossbar.io.m_axi(0)
    // uart.io.s_axi <> pbus_crossbar.io.m_axi(1)
    gpio.io.s_axi.connectFromAxi4(tile.io.pbus)

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
    
      mig.io.s_axi <> tile.io.sbus
    }
    
    gpio.io.gpio_io_i := 0x05050505.U
    io.led := gpio.io.gpio_io_o

    addConstraintResource("package-vivado-ips/resources/constraints/Nexys-Video-Master.xdc")
  }
}
