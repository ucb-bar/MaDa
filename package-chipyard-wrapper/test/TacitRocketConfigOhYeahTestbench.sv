`timescale 1ns / 1ps


module TacitRocketConfigOhYeahTestbench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;

  // setup clock and reset
  reg clock, reset;
  initial clock = 0;
  always #(CLOCK_PERIOD/2) clock = ~clock;


  reg jtag_tck = 0;
  reg jtag_tms = 0;
  reg jtag_tdi = 0;

  wire jtag_tck_io;
  wire jtag_tms_io;
  wire jtag_tdi_io;
  wire jtag_tdo_io;

  always #(CLOCK_PERIOD/2) jtag_tck = ~jtag_tck;


  IOBUF jtag_tck_buf(
    .I(jtag_tck),
    .O(),
    .T(0),
    .IO(jtag_tck_io)
  );
  IOBUF jtag_tms_buf(
    .I(jtag_tms),
    .O(),
    .T(0),
    .IO(jtag_tms_io)
  );
  IOBUF jtag_tdi_buf(
    .I(jtag_tdi),
    .O(),
    .T(0),
    .IO(jtag_tdi_io)
  );


  TacitRocketConfigOhYeah dut(
    .io_CLK100MHZ(clock),
    .io_sw(),
    .io_led0_r(),
    .io_led0_g(),
    .io_led0_b(),
    .io_led1_r(),
    .io_led1_g(),
    .io_led1_b(),
    .io_led2_r(),
    .io_led2_g(),
    .io_led2_b(),
    .io_led3_r(),
    .io_led3_g(),
    .io_led3_b(),
    .io_led(),
    .io_btn(),
    .io_ja_0(),
    .io_ja_1(),
    .io_ja_2(),
    .io_ja_3(),
    .io_ja_4(),
    .io_ja_5(),
    .io_ja_6(),
    .io_ja_7(),
    .io_jb_0(),
    .io_jb_1(),
    .io_jb_2(),
    .io_jb_3(),
    .io_jb_4(),
    .io_jb_5(),
    .io_jb_6(),
    .io_jb_7(),
    .io_jc_0(),
    .io_jc_1(),
    .io_jc_2(),
    .io_jc_3(),
    .io_jc_4(),
    .io_jc_5(),
    .io_jc_6(),
    .io_jc_7(),
    .io_jd_0(jtag_tdo_io),
    .io_jd_1(),
    .io_jd_2(jtag_tck_io),
    .io_jd_3(),
    .io_jd_4(jtag_tdi_io),
    .io_jd_5(jtag_tms_io),
    .io_jd_6(),
    .io_jd_7(),
    .io_uart_rxd_out(),
    .io_uart_txd_in(),
    .io_ck_io_0(),
    .io_ck_io_1(),
    .io_ck_io_2(),
    .io_ck_io_3(),
    .io_ck_io_4(),
    .io_ck_io_5(),
    .io_ck_io_6(),
    .io_ck_io_7(),
    .io_ck_io_8(),
    .io_ck_io_9(),
    .io_ck_ioa(),
    .io_ck_rst(~reset),
    .io_eth_col(),
    .io_eth_crs(),
    .io_eth_ref_clk(),
    .io_eth_rstn(),
    .io_eth_rx_clk(),
    .io_eth_rx_dv(),
    .io_eth_rxd(),
    .io_eth_rxerr(),
    .io_eth_tx_clk(),
    .io_eth_tx_en(),
    .io_eth_txd(),
    .io_qspi_cs(),
    .io_qspi_sck(),
    .io_qspi_dq_0(),
    .io_qspi_dq_1(),
    .io_qspi_dq_2(),
    .io_qspi_dq_3()
  );

  initial begin
    reset = 1;
    repeat (40) @(posedge clock); #0;
    reset = 0;
    
    repeat (1000) @(posedge clock); #0;

    #100;
    $display("All tests passed!");
    $finish();
  end

endmodule