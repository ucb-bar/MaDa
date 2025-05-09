package chipyardwrapper

// import chisel3._
// import chisel3.util._


// class UdpTinyRocketArty100T extends RawModule {
//   val io = IO(new Arty100TIO())

//   val clock = Wire(Clock())
//   val reset = Wire(Bool())
  
//   val pll_locked = Wire(Bool())
//   val cbus_reset = Wire(Bool())
//   val jtag_reset = Wire(Bool())

//   val clock_25 = Wire(Clock())


//   val clk_wiz = Module(new ClockingWizard(50, 25))
//   // clocking wizard connection
//   clk_wiz.io.clk_in1 := io.CLK100MHZ
//   clk_wiz.io.reset := ~io.ck_rst
//   pll_locked := clk_wiz.io.locked
//   clock := clk_wiz.io.clk_out1
//   clock_25 := clk_wiz.io.clk_out2


//   val sync_reset = Module(new SyncReset())
//   // sync reset connection
//   sync_reset.io.clock := clock
//   sync_reset.io.reset := ~pll_locked
//   reset := sync_reset.io.out


//   val debug_sync_reset = Module(new SyncReset())
//   // jtag reset connection
//   debug_sync_reset.io.clock := io.jd_2.asClock
//   debug_sync_reset.io.reset := cbus_reset
//   jtag_reset := debug_sync_reset.io.out

  
//   val digital_top = Module(new DigitalTop)
//   val udp_core = Module(new udp_core(
//     mac_address = 0x02_00_00_00_00_00L,
//     ip_address = 0x0a_00_00_80,
//     gateway_ip = 0x0a_00_00_01,
//     subnet_mask = 0xff_ff_ff_00,
//     udp_port = 1234
//   ))


//   digital_top.io.auto_chipyard_prcictrl_domain_reset_setter_clock_in_member_allClocks_uncore_clock := clock
//   digital_top.io.auto_chipyard_prcictrl_domain_reset_setter_clock_in_member_allClocks_uncore_reset := reset

//   cbus_reset := digital_top.io.auto_cbus_fixedClockNode_anon_out_reset
//   digital_top.io.resetctrl_hartIsInReset_0 := cbus_reset

//   digital_top.io.debug_clock := clock
//   digital_top.io.debug_reset := reset

//   digital_top.io.debug_systemjtag.jtag.TCK := io.jd_2
//   digital_top.io.debug_systemjtag.jtag.TMS := io.jd_5
//   digital_top.io.debug_systemjtag.jtag.TDI := io.jd_4
//   io.jd_0 := digital_top.io.debug_systemjtag.jtag.TDO.data
//   digital_top.io.debug_systemjtag.reset := jtag_reset

//   digital_top.io.debug_dmactiveAck := true.B

//   digital_top.io.custom_boot := true.B

//   digital_top.io.serial_tl_0.in.valid := false.B
//   digital_top.io.serial_tl_0.in.bits.phit := 0.U(32.W)
//   digital_top.io.serial_tl_0.out.ready := true.B
//   digital_top.io.serial_tl_0.clock_in := clock

//   digital_top.io.uart_0_rxd := false.B
//   // io.uart_rxd_out := digital_top.io.uart_0_txd




//   udp_core.io.clk := clock
//   udp_core.io.rst := reset

//   udp_core.io.btn := io.btn
//   udp_core.io.sw := io.sw

//   io.led0.r := udp_core.io.led0_r
//   io.led0.g := udp_core.io.led0_g
//   io.led0.b := udp_core.io.led0_b

//   io.led1.r := udp_core.io.led1_r
//   io.led1.g := udp_core.io.led1_g
//   io.led1.b := udp_core.io.led1_b

//   io.led2.r := udp_core.io.led2_r
//   io.led2.g := udp_core.io.led2_g
//   io.led2.b := udp_core.io.led2_b

//   io.led3.r := udp_core.io.led3_r
//   io.led3.g := udp_core.io.led3_g
//   io.led3.b := udp_core.io.led3_b

//   // io.led := Cat(udp_core.io.led7, udp_core.io.led6, udp_core.io.led5, udp_core.io.led4)
  
//   io.jd_3 := false.B

//   io.uart_rxd_out := udp_core.io.uart_txd
//   udp_core.io.uart_rxd := io.uart_txd_in

//   io.eth_ref_clk := clock_25
//   udp_core.io.phy_col := io.eth_col
//   udp_core.io.phy_crs := io.eth_crs
//   io.eth_rstn := udp_core.io.phy_reset_n
//   udp_core.io.phy_rx_clk := io.eth_rx_clk
//   udp_core.io.phy_rx_dv := io.eth_rx_dv
//   udp_core.io.phy_rxd := io.eth_rxd
//   udp_core.io.phy_rx_er := io.eth_rxerr
//   udp_core.io.phy_tx_clk := io.eth_tx_clk
//   io.eth_tx_en := udp_core.io.phy_tx_en
//   io.eth_txd := udp_core.io.phy_txd

//   withClockAndReset(clock, reset) {
//     val udp_payload_axis_fifo = Module(new Axi4LiteStreamDataFifo(8))
//     udp_payload_axis_fifo.io.s_axis.t.valid := udp_core.io.rx_fifo_udp_payload_axis_tvalid
//     udp_core.io.rx_fifo_udp_payload_axis_tready := udp_payload_axis_fifo.io.s_axis.t.ready
//     udp_payload_axis_fifo.io.s_axis.t.bits.data := udp_core.io.rx_fifo_udp_payload_axis_tdata
//     udp_payload_axis_fifo.io.s_axis.t.bits.last := udp_core.io.rx_fifo_udp_payload_axis_tlast
//     udp_payload_axis_fifo.io.s_axis.t.bits.user := udp_core.io.rx_fifo_udp_payload_axis_tuser
    
//     udp_core.io.tx_fifo_udp_payload_axis_tvalid := udp_payload_axis_fifo.io.m_axis.t.valid
//     udp_payload_axis_fifo.io.m_axis.t.ready := udp_core.io.tx_fifo_udp_payload_axis_tready
//     udp_core.io.tx_fifo_udp_payload_axis_tdata := udp_payload_axis_fifo.io.m_axis.t.bits.data
//     udp_core.io.tx_fifo_udp_payload_axis_tlast := udp_payload_axis_fifo.io.m_axis.t.bits.last
//     udp_core.io.tx_fifo_udp_payload_axis_tuser := udp_payload_axis_fifo.io.m_axis.t.bits.user

//     val gpio_0 = Module(new Axi4LiteGpio)
//     gpio_0.io.s_axi <> digital_top.io.periph_axi4_s_axi
//     gpio_0.io.gpio_io_i := io.btn
//     io.led := gpio_0.io.gpio_io_o
//   }
// }
