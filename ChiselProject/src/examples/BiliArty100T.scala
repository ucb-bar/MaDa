import chisel3._
import chisel3.util._





/**
  * 
  * 
  * Memory Organization:
  * 0x2000_0000 - 0x200F_FFFF: QSPI Memory (1 MB)
  * 0x1003_0000 - 0x1003_0FFF: QSPI Control
  * 0x1002_0000 - 0x1002_0FFF: UART
  * 0x1001_0000 - 0x1001_0FFF: GPIO
  * 0x0800_0000 - 0x0800_3FFF: scratchpad (16 kB)
  * 
  */
class BiliArty100T extends RawModule {
  val io = IO(new Arty100TIO())

  io := DontCare

  val clock = Wire(Clock())
  val reset = Wire(Reset())
  
  val pll_locked = Wire(Bool())


  val clk_wiz = Module(new ClockingWizard(20))
  // clocking wizard connection
  clk_wiz.io.clk_in1 := io.CLK100MHZ
  clk_wiz.io.reset := ~io.ck_rst
  pll_locked := clk_wiz.io.locked
  clock := clk_wiz.io.clk_out1


  val sync_reset = Module(new SyncReset())
  // sync reset connection
  sync_reset.io.clock := clock
  sync_reset.io.reset := ~pll_locked
  reset := sync_reset.io.out


  withClockAndReset(clock, reset) {
    val reset_vector = RegInit(0x0800_0000.U(32.W))

    val tile = Module(new Tile())

    tile.io.reset_vector := reset_vector

    val pbus_crossbar = Module(new Axi4LiteCrossbar(
      numSlave = 1,
      numMaster = 2,
      deviceSizes = Array(0x0400, 0x0400),
      deviceAddresses = Array(0x1001_0000, 0x1002_0000),
    ))

    val spi = Module(new Axi4QuadSpi())
    val gpio = Module(new Axi4LiteGpio())
    val uart = Module(new Axi4LiteUartLite())

    pbus_crossbar.io.s_axi(0) <> Axi4ToAxi4Lite(tile.io.pbus)

    gpio.attach(pbus_crossbar.io.m_axi(0))
    uart.attach(pbus_crossbar.io.m_axi(1))

    spi.io.ext_spi_clk := clock
    spi.io.s_axi.aw.bits.addr := 0x00000000.U
    spi.io.s_axi.aw.valid := false.B
    spi.io.s_axi.w.bits.data := 0x00000000.U
    spi.io.s_axi.w.bits.strb := 0x00000000.U
    spi.io.s_axi.w.valid := false.B
    spi.io.s_axi.b.ready := false.B
    spi.io.s_axi.ar.bits.addr := 0x00000000.U
    spi.io.s_axi.ar.valid := false.B
    spi.io.s_axi.r.ready := false.B
    
    spi.io.s_axi4 := DontCare

    // make sure the write port is not used
    spi.io.s_axi4.aw.bits.addr := 0x00000000.U
    spi.io.s_axi4.aw.valid := false.B
    spi.io.s_axi4.w.bits.data := 0x00000000.U
    spi.io.s_axi4.w.bits.strb := 0x00000000.U
    spi.io.s_axi4.w.bits.last := false.B
    spi.io.s_axi4.w.valid := false.B
    spi.io.s_axi4.b.ready := false.B

    tile.io.sbus := DontCare
    spi.io.s_axi4.ar.bits.addr := tile.io.sbus.ar.bits.addr
    spi.io.s_axi4.ar.valid := tile.io.sbus.ar.valid
    tile.io.sbus.ar.ready := spi.io.s_axi4.ar.ready
    tile.io.sbus.r.bits.data := spi.io.s_axi4.r.bits.data
    tile.io.sbus.r.valid := spi.io.s_axi4.r.valid
    tile.io.sbus.r.bits.last := 1.B
    spi.io.s_axi4.r.ready := tile.io.sbus.r.ready

    spi.io.s_axi4.ar.bits.size := 2.U.asTypeOf(spi.io.s_axi4.ar.bits.size)
    spi.io.s_axi4.ar.bits.burst := 1.U.asTypeOf(spi.io.s_axi4.ar.bits.burst)


    io.qspi_sck := spi.io.sck_o.asClock
    io.qspi_cs := spi.io.ss_o
    spi.io.sck_i := 0.B
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

    

    
    gpio.io.gpio_io_i := 0x05050505.U
    io.led := gpio.io.gpio_io_o

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
}
