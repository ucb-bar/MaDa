import chisel3._
import chisel3.util._





/**
  * 
  * 
  * Memory Organization:
  * SBUS
  * 0x2000_0000 - 0x200F_FFFF: QSPI Memory (1 MB)
  * PBUS
  * 0x1004_0000 - 0x1004_0FFF: TIMER
  * 0x1003_0000 - 0x1003_0FFF: QSPI Control
  * 0x1002_0000 - 0x1002_0FFF: UART
  * 0x1001_0000 - 0x1001_0FFF: GPIO
  * MBUS
  * 0x0800_0000 - 0x0800_3FFF: scratchpad (16 kB)
  * 
  */
class MlpPolicyRunner extends RawModule {
  val io = IO(new Arty100TIO())

  val systemClockFrequency = 50

  io := DontCare

  val clock = Wire(Clock())
  val reset = Wire(Bool())
  
  val pll_locked = Wire(Bool())

  val clk_wiz = Module(new ClockingWizard(Seq(systemClockFrequency)))
  // clocking wizard connection
  clk_wiz.io.clk_in := io.CLK100MHZ
  clk_wiz.io.reset := ~io.ck_rst
  pll_locked := clk_wiz.io.locked
  clock := clk_wiz.io.clk_outs(0)


  val sync_reset = Module(new SyncReset())
  // sync reset connection
  sync_reset.io.clock := clock
  sync_reset.io.reset := ~pll_locked
  reset := sync_reset.io.out


  withClockAndReset(clock, reset) {
    val reset_vector = RegInit(0x0800_0000.U(32.W))

    val tile = Module(new Tile(sbusFrequency=systemClockFrequency))

    tile.io.reset_vector := reset_vector

    val pbus_crossbar = Module(new Axi4LiteCrossbar(
      numSlave=1,
      numMaster=3,
      // params=Axi4Params(
      //   dataWidth=32,
      //   idWidth=4,
      // ),
      deviceSizes=Array(
        0x0000_0400,  // UART
        0x0000_0400,  // GPIO
        0x0000_0400,  // TIMER
      ),
      deviceAddresses=Array(
        0x1001_0000,   // GPIO
        0x1002_0000,   // UART
        0x1004_0000,   // TIMER
      ),
    ))

    val spi = Module(new Axi4QuadSpiFlash())
    // val spi = Module(new Axi4SpiFlash())

    val gpio = Module(new Axi4LiteGpio())
    val uart = Module(new Axi4LiteUartLite(axiClockFrequency=systemClockFrequency))
    val timer = Module(new Axi4LiteTimer())

    pbus_crossbar.io.s_axi(0).connectFromAxi4(tile.io.pbus)

    gpio.io.s_axi <> pbus_crossbar.io.m_axi(0)
    uart.io.s_axi <> pbus_crossbar.io.m_axi(1)
    timer.io.s_axi <> pbus_crossbar.io.m_axi(2)

    timer.io.capturetrig0 := 0.B
    timer.io.capturetrig1 := 0.B
    timer.io.freeze := 0.B

    io.ck_io(8) := timer.io.pwm0
    io.ck_io(9) := timer.io.pwm0



    spi.io.ext_spi_clk := clock

    // tie off control ports    
    spi.io.s_axi := DontCare
    
    spi.io.s_axi4 <> tile.io.sbus

    // make sure the write port is disabled
    spi.io.s_axi4.aw.valid := false.B
    spi.io.s_axi4.w.valid := false.B
    spi.io.s_axi4.b.ready := false.B

    dontTouch(tile.io.sbus.ar.bits.burst)


    // io.qspi_sck := spi.io.sck_o.asClock
    io.qspi_cs := spi.io.ss_o
    // spi.io.sck_i := 0.B
    spi.io.ss_i := 0.B

    val qspi_io0_buf = Module(new IOBUF())
    spi.io.io0_i := qspi_io0_buf.io.O
    qspi_io0_buf.io.IO <> io.qspi_dq(0)
    qspi_io0_buf.io.I := spi.io.io0_o
    qspi_io0_buf.io.T := spi.io.io0_t

    val qspi_io1_buf = Module(new IOBUF())
    spi.io.io1_i := qspi_io1_buf.io.O
    qspi_io1_buf.io.IO <> io.qspi_dq(1)
    qspi_io1_buf.io.I := spi.io.io1_o
    qspi_io1_buf.io.T := spi.io.io1_t

    val qspi_io2_buf = Module(new IOBUF())
    spi.io.io2_i := qspi_io2_buf.io.O
    qspi_io2_buf.io.IO <> io.qspi_dq(2)
    qspi_io2_buf.io.I := spi.io.io2_o
    qspi_io2_buf.io.T := spi.io.io2_t

    val qspi_io3_buf = Module(new IOBUF())
    spi.io.io3_i := qspi_io3_buf.io.O
    qspi_io3_buf.io.IO <> io.qspi_dq(3)
    qspi_io3_buf.io.I := spi.io.io3_o
    qspi_io3_buf.io.T := spi.io.io3_t

    

    
    gpio.io.gpio_io_i := 0x05050505.U
    io.led := gpio.io.gpio_io_o

    io.ck_io(0) := gpio.io.gpio_io_o(0)
    io.ck_io(1) := gpio.io.gpio_io_o(1)
    io.ck_io(2) := gpio.io.gpio_io_o(2)
    io.ck_io(3) := gpio.io.gpio_io_o(3)
    io.ck_io(4) := gpio.io.gpio_io_o(4)
    io.ck_io(5) := gpio.io.gpio_io_o(5)
    io.ck_io(6) := gpio.io.gpio_io_o(6)
    io.ck_io(7) := gpio.io.gpio_io_o(7)

    io.uart_rxd_out := uart.io.tx
    uart.io.rx := io.uart_txd_in


    for (i <- 0 until 8) {
      val iobuf = Module(new IOBUF())
      iobuf.io.I := tile.io.debug.syscall0(i)
      iobuf.io.T := false.B
      iobuf.io.IO <> io.ja(i)
    }

    tile.io.debug.sysresp0 := 0.U(32.W)
    tile.io.debug.sysresp1 := 0.U(32.W)
    tile.io.debug.sysresp2 := 0.U(32.W)
    tile.io.debug.sysresp3 := 0.U(32.W)

  }



  addConstraintResource("ChiselProject/resources/constraints/Arty-A7-100-Master.xdc")

  addSimulationResource("ChiselProject/test/resources/verilog/examples/MlpPolicyRunnerTestbench.sv")
}
