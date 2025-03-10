`timescale 1ns / 1ps


module MlpPolicyTestBench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;
  
  // setup clock and reset
  reg clock, reset;
  initial clock = 'b0;
  always #(CLOCK_PERIOD/2) clock = ~clock;

  logic led;

  logic uart_txd;

  BiliArty100T dut(
    .io_CLK100MHZ(clock),
    .io_sw(4'b0),
    .io_btn(4'b0),
    .io_ja(8'b0),
    .io_jb(1'b0),
    .io_jc(1'b0),
    .io_jd_0(),
    .io_jd_1(1'b0),
    .io_jd_2(1'b0),
    .io_jd_3(),
    .io_jd_4(1'b0),
    .io_jd_5(1'b0),
    .io_jd_6(1'b0),
    .io_jd_7(1'b0),
    .io_uart_txd_in(uart_rxd),
    .io_uart_rxd_out(uart_txd),
    .io_ck_ioa(1'b0),
    .io_ck_rst(~reset),
    .io_eth_col(1'b0),
    .io_eth_crs(1'b0),
    .io_eth_rx_clk(1'b0),
    .io_eth_rx_dv(1'b0),
    .io_eth_rxd(4'b0),
    .io_eth_rxerr(1'b0),
    .io_eth_tx_clk(1'b0),
    .io_led(led)
  );

  SimUart #(
    .BAUD_RATE(115200)
  ) sim_uart (
    .io_out(uart_rxd),
    .io_in(uart_txd)
  );

  initial begin
    reset = 1'b1;
    repeat (10) @(posedge clock);
    reset = 1'b0;
    
    fork
      begin
        sim_uart.listen();
      end
    join

    repeat (10000) @(posedge clock);
    $finish;
  end
endmodule
