`timescale 1ns / 1ps


module GenericTestBench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;
  
  // setup clock and reset
  reg clock, reset;
  initial clock = 'b0;
  always #(CLOCK_PERIOD/2) clock = ~clock;

  logic led;


  wire IO0_IO;
  wire IO1_IO;
  wire SCK_IO;
  wire SS_IO;


  
  SimSpiFlashModel #(
    .PLUSARG("firmware.8.hex"),
    .READONLY(0),
    .CAPACITY_BYTES(1024)
  ) sim_spi (
    .sck(SCK_IO),
    .cs_0(SS_IO),
    .reset(reset),
    .dq_0(IO0_IO),
    .dq_1(IO1_IO),
    .dq_2(),
    .dq_3()
  );

  BiliArty100T dut(
    .io_CLK100MHZ(clock),
    .io_sw(4'b0),
    .io_btn(4'b0),
    .io_ja_0(),
    .io_ja_1(),
    .io_ja_2(),
    .io_ja_3(),
    .io_ja_4(),
    .io_ja_5(),
    .io_ja_6(),
    .io_ja_7(),
    .io_uart_txd_in(1'b0),
    .io_uart_rxd_out(),
    .io_ck_ioa(1'b0),
    .io_ck_rst(~reset),
    .io_eth_col(1'b0),
    .io_eth_crs(1'b0),
    .io_eth_rx_clk(1'b0),
    .io_eth_rx_dv(1'b0),
    .io_eth_rxd(4'b0),
    .io_eth_rxerr(1'b0),
    .io_eth_tx_clk(1'b0),
    .io_qspi_cs(SS_IO),
    .io_qspi_sck(SCK_IO),
    .io_qspi_dq_0(IO0_IO),
    .io_qspi_dq_1(IO1_IO),
    .io_qspi_dq_2(),
    .io_qspi_dq_3(),
    .io_led(led)
  );

  initial begin
    reset = 1'b1;
    repeat (10) @(posedge clock);
    reset = 1'b0;

    repeat (10000) @(posedge clock);
    $finish;
  end
endmodule
