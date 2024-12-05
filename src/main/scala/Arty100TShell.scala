import chisel3._
import chisel3.util._


class Arty100TShell extends RawModule {
  val CLK100MHZ = IO(Input(Clock()))
  val ck_rst = IO(Input(Bool()))

  val uart_rxd_out = IO(Output(Bool()))
  val uart_txd_in = IO(Input(Bool()))

  val jd_0 = IO(Output(Bool()))
  val jd_1 = IO(Input(Bool()))
  val jd_2 = IO(Input(Bool()))
  val jd_3 = IO(Output(Bool()))
  val jd_4 = IO(Input(Bool()))
  val jd_5 = IO(Input(Bool()))
  val jd_6 = IO(Input(Bool()))
  val jd_7 = IO(Input(Bool()))

  val btn = IO(Input(UInt(4.W)))
  val sw = IO(Input(UInt(4.W)))

  val led0_b = IO(Output(Bool()))
  val led1_b = IO(Output(Bool()))
  val led2_b = IO(Output(Bool()))
  // val led3_b = IO(Output(Bool()))


  val clock = Wire(Clock())
  val pll_locked = Wire(Bool())
  val reset = Wire(Bool())

  val cbus_reset = Wire(Bool())
  val jtag_reset = Wire(Bool())


  val clk_wiz = Module(new clk_wiz_0())

  val sync_reset = Module(new SyncReset())
  val debug_sync_reset = Module(new SyncReset())

  val system = Module(new DigitalTop())

  val axi_crossbar = Module(new AXICrossBar())
  val axi_gpio = Module(new axi_gpio_0())
  val axi_uartlite = Module(new axi_uartlite_0())

  // clocking wizard connection
  clk_wiz.io.clk_in1 := CLK100MHZ
  clk_wiz.io.reset := ~ck_rst
  pll_locked := clk_wiz.io.locked
  clock := clk_wiz.io.clk_out1

  // sync reset connection
  sync_reset.io.clk := clock
  sync_reset.io.reset := ~pll_locked
  reset := sync_reset.io.out

  // jtag reset connection
  debug_sync_reset.io.clk := jd_2.asClock
  debug_sync_reset.io.reset := cbus_reset
  jtag_reset := debug_sync_reset.io.out


  // digital top connection
  system.io.auto_chipyard_prcictrl_domain_reset_setter_clock_in_member_allClocks_uncore_clock := clock
  system.io.auto_chipyard_prcictrl_domain_reset_setter_clock_in_member_allClocks_uncore_reset := reset
  
  cbus_reset := system.io.auto_cbus_fixedClockNode_anon_out_reset
  system.io.resetctrl_hartIsInReset_0 := cbus_reset
  
  system.io.debug_clock := clock
  system.io.debug_reset := reset

  system.io.debug_systemjtag.jtag.TCK := jd_2
  system.io.debug_systemjtag.jtag.TMS := jd_5
  system.io.debug_systemjtag.jtag.TDI := jd_4
  jd_0 := system.io.debug_systemjtag.jtag.TDO.data
  system.io.debug_systemjtag.reset := jtag_reset

  system.io.debug_dmactiveAck := true.B

  system.io.custom_boot := true.B

  system.io.serial_tl_0.in.valid := false.B
  system.io.serial_tl_0.in.bits.phit := 0.U(1.W)
  system.io.serial_tl_0.out.ready := true.B
  system.io.serial_tl_0.clock_in := clock

  system.io.uart_0_rxd := uart_txd_in
  uart_rxd_out := system.io.uart_0_txd

  system.io.periph_axi4_s_axi <> axi_crossbar.io.s_axi
  
  axi_crossbar.io.m_axi(0) <> axi_gpio.io.s_axi
  axi_gpio.io.gpio_io_i := 5.U(32.W)
  led0_b := axi_gpio.io.gpio_io_o(0)
  
  axi_crossbar.io.m_axi(1) <> axi_uartlite.io.s_axi
  axi_uartlite.io.rx := jd_7
  jd_3 := axi_uartlite.io.tx

  

  led1_b := false.B
  led2_b := true.B



}
