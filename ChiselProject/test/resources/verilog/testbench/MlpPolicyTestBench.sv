`timescale 1ns / 1ps

import SimUart::*;

module MlpPolicyTestBench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;

  // parameter TIMEOUT_CYCLES = 2_000_000;
  parameter TIMEOUT_CYCLES = 8_000;
  
  // setup clock and reset
  reg clock, reset;
  initial clock = 'b0;
  always #(CLOCK_PERIOD/2) clock = ~clock;

  logic led;

  logic uart_txd;
  logic uart_rxd;

  logic [7:0] tohost;

  BiliArty100T dut(
    .io_CLK100MHZ(clock),
    .io_sw(4'b0),
    .io_btn(4'b0),
    .io_ja(tohost),
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

  SimUartConsole #(
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
        sim_uart.listen(LOGGING_INFO);
      end
      begin
        sim_uart.write(8'hca);
        sim_uart.write(8'hca);
      end
      begin
        repeat (TIMEOUT_CYCLES) @(posedge clock);
        $display("Timeout! called at %t.", $time);
        $finish;
      end
      begin
        forever begin
          @(posedge dut.tile.core.clock);
          // if (tohost == 8'h02) begin
          if (dut.tile.core.valu.io_func == 'h1) begin
            $display("R0: %.4f (0x%x)   R1: %.4f (0x%x)",
              $bitstoshortreal(dut.tile.core.valu.io_out_0),
              dut.tile.core.valu.io_out_0,
              $bitstoshortreal(dut.tile.core.valu.io_out_1),
              dut.tile.core.valu.io_out_1
            );
          end
          if (tohost == 8'h01) begin
            $display("exit() called from DUT at %t.", $time);
            $finish;
          end
        end
      end
    join

  end
endmodule
