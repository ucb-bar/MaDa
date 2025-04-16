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

  wire qspi_mosi;
  wire qspi_miso;
  wire qspi_sclk;
  wire qspi_cs;

  
  SimSpiFlashModel #(
    .PLUSARG("firmware.8.hex"),
    .READONLY(0),
    .CAPACITY_BYTES(1024)
  ) sim_spi (
    .sck(qspi_sclk),
    .cs_0(qspi_cs),
    .reset(reset),
    .dq_0(qspi_mosi),
    .dq_1(qspi_miso),
    .dq_2(),
    .dq_3()
  );



  wire [7:0] tohost;

  BiliArty100T dut(
    .io_CLK100MHZ(clock),
    .io_sw(4'b0),
    .io_btn(4'b0),
    .io_ja_0(tohost[0]),
    .io_ja_1(tohost[1]),
    .io_ja_2(tohost[2]),
    .io_ja_3(tohost[3]),
    .io_ja_4(tohost[4]),
    .io_ja_5(tohost[5]),
    .io_ja_6(tohost[6]),
    .io_ja_7(tohost[7]),
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
    .io_qspi_cs(qspi_cs),
    .io_qspi_sck(qspi_sclk),
    .io_qspi_dq_0(qspi_mosi),
    .io_qspi_dq_1(qspi_miso),
    .io_qspi_dq_2(),
    .io_qspi_dq_3(),
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
