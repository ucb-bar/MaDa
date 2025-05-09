package chipyardwrapper

// import chisel3._
// import chisel3.util._


// class MotorArty100T extends Arty100TShell {

//   val clock = Wire(Clock())
//   val pll_locked = Wire(Bool())
//   val reset = Wire(Bool())

//   val cbus_reset = Wire(Bool())
//   val jtag_reset = Wire(Bool())


//   val clk_wiz = Module(new clk_wiz_0())

//   val sync_reset = Module(new SyncReset())
//   val debug_sync_reset = Module(new SyncReset())

//   val system = Module(new DigitalTop())

//   val axi_crossbar = Module(new AXICrossBar())
//   val axi_gpio = Module(new axi_gpio_0())
//   // val axi_uartlite = Module(new axi_uartlite_0())
//   // val axi_timer = Module(new axi_timer_0())
//   val axi_madatimer = Module(new AXIMadaTimer())

//   // clocking wizard connection
//   clk_wiz.io.clk_in1 := io.CLK100MHZ
//   clk_wiz.io.reset := ~io.ck_rst
//   pll_locked := clk_wiz.io.locked
//   clock := clk_wiz.io.clk_out1

//   // sync reset connection
//   sync_reset.io.clk := clock
//   sync_reset.io.reset := ~pll_locked
//   reset := sync_reset.io.out

//   // jtag reset connection
//   debug_sync_reset.io.clk := io.jd(2).asClock
//   debug_sync_reset.io.reset := cbus_reset
//   jtag_reset := debug_sync_reset.io.out


//   // digital top connection
//   system.io.auto_chipyard_prcictrl_domain_reset_setter_clock_in_member_allClocks_uncore_clock := clock
//   system.io.auto_chipyard_prcictrl_domain_reset_setter_clock_in_member_allClocks_uncore_reset := reset
  
//   cbus_reset := system.io.auto_cbus_fixedClockNode_anon_out_reset
//   system.io.resetctrl_hartIsInReset_0 := cbus_reset
  
//   system.io.debug_clock := clock
//   system.io.debug_reset := reset

//   system.io.debug_systemjtag.jtag.TCK := io.jd(2)
//   system.io.debug_systemjtag.jtag.TMS := io.jd(5)
//   system.io.debug_systemjtag.jtag.TDI := io.jd(4)
//   io.jd(0) := system.io.debug_systemjtag.jtag.TDO.data
//   system.io.debug_systemjtag.reset := jtag_reset

//   system.io.debug_dmactiveAck := true.B

//   system.io.custom_boot := true.B

//   system.io.serial_tl_0.in.valid := false.B
//   system.io.serial_tl_0.in.bits.phit := 0.U(1.W)
//   system.io.serial_tl_0.out.ready := true.B
//   system.io.serial_tl_0.clock_in := clock

//   system.io.uart_0_rxd := io.uart_txd_in
//   io.uart_rxd_out := system.io.uart_0_txd

//   // axi connection
//   system.io.periph_axi4_s_axi <> axi_crossbar.io.s_axi
  
//   axi_crossbar.io.m_axi(0) <> axi_gpio.io.s_axi
//   axi_gpio.io.gpio_io_i := 5.U(32.W)
//   io.led0.b := axi_gpio.io.gpio_io_o(0)

//   axi_crossbar.io.m_axi(1) <> axi_madatimer.io.s_axi
  
//   // axi_crossbar.io.m_axi(1) <> axi_uartlite.io.s_axi
//   // axi_uartlite.io.rx := jd_7
//   // jd_3 := axi_uartlite.io.tx
//   io.jd(3) := false.B

//   // axi_crossbar.io.m_axi(1) <> axi_timer.io.s_axi
//   // axi_timer.io.capturetrig0 := false.B
//   // axi_timer.io.capturetrig1 := false.B
//   // axi_timer.io.freeze := false.B
//   // ck_io8 := axi_timer.io.pwm0

//   io.ck_io2 := axi_madatimer.io.pwms(0)
//   io.ck_io3 := ~axi_madatimer.io.pwms(0)
//   io.ck_io4 := axi_madatimer.io.pwms(1)
//   io.ck_io5 := ~axi_madatimer.io.pwms(1)
//   io.ck_io6 := axi_madatimer.io.pwms(2)
//   io.ck_io7 := ~axi_madatimer.io.pwms(2)

//   io.ck_io26 := io.sw(0)


//   io.led1.b := false.B //axi_timer.io.pwm0
//   io.led2.b := true.B
// }
