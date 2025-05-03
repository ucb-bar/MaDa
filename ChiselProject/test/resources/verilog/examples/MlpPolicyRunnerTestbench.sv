`timescale 1ns / 1ps

import SimUart::*;


// The QSPI IP will use the clock from the hardened startup module
// in simulation, we cannot access the clock from that, so we need to
// drive the clock from this clock gen
module SimQspiClockGen #(
  parameter PRESCALER = 4
) (
  input clock,
  input qspi_cs,
  output qspi_sclk
);

  reg [3:0] reg_counter;
  reg reg_sclk;

  always @(posedge clock) begin
    if (qspi_cs) begin
      reg_counter <= 4'b0000;
      reg_sclk <= 1'b0;
    end
    else begin
      if (reg_counter >= PRESCALER) begin
        reg_counter <= 4'b0000;
        reg_sclk <= ~reg_sclk;
      end
      else begin
        reg_counter <= reg_counter + 4'd1;
      end
    end
  end

  assign qspi_sclk = reg_sclk;
endmodule


module MlpPolicyRunnerTestbench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;

  // parameter TIMEOUT_CYCLES = 2_000_000;
  parameter TIMEOUT_CYCLES = 500_000;
  
  // setup clock and reset
  reg clock, reset;
  initial clock = 'b0;
  always #(CLOCK_PERIOD/2) clock = ~clock;

  logic [3:0] led;

  logic uart_txd;
  logic uart_rxd;

  wire qspi_io0;
  wire qspi_io1;
  wire qspi_io2;
  wire qspi_io3;
  wire qspi_sclk;
  wire qspi_cs;

  SimQspiClockGen #(
    .PRESCALER(4)
  ) qspi_clock_gen (
    .clock(clock),
    .qspi_sclk(qspi_sclk),
    .qspi_cs(qspi_cs)
  );

  
  // We simulate the Spansion S25FL128S flash memory on the Arty board
  // it has 8 dummy cycles for single-mode read commands
  // and 6 dummy cycles for quad-mode read commands
  // localparam DUMMY_CYCLES = 8;
  localparam DUMMY_CYCLES = 6;
  SimSpiFlashModel #(
    .PLUSARG("firmware.flash.8.hex"),
    .READONLY(0),
    .CAPACITY_BYTES(1024),
    .DUMMY_CYCLES(DUMMY_CYCLES)
  ) sim_spi (
    .sck(qspi_sclk),
    .cs_0(qspi_cs),
    .reset(reset),
    .dq_0(qspi_io0),
    .dq_1(qspi_io1),
    .dq_2(qspi_io2),
    .dq_3(qspi_io3)
  );


  wire [7:0] tohost;

  MlpPolicyRunner dut(
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
    .io_qspi_sck(), // in simulation, we drive this from the clock gen
    .io_qspi_dq_0(qspi_io0),
    .io_qspi_dq_1(qspi_io1),
    .io_qspi_dq_2(qspi_io2),
    .io_qspi_dq_3(qspi_io3),
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
    repeat (10) @(posedge clock); #0;
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
        repeat (TIMEOUT_CYCLES) @(posedge clock); #0;
        $display("TB: Timeout! called at %t.", $time);
        $finish;
      end
      begin
        forever begin
          @(posedge dut.tile.core.clock);
          // if (dut.tile.core.valu.io_func == 'h1) begin
          //   $display("R0: %.4f (0x%x)   R1: %.4f (0x%x)",
          //     $bitstoshortreal(dut.tile.core.valu.io_out_0),
          //     dut.tile.core.valu.io_out_0,
          //     $bitstoshortreal(dut.tile.core.valu.io_out_1),
          //     dut.tile.core.valu.io_out_1
          //   );
          // end

          // handle syscall
          if (tohost == 8'h01) begin  // exit()
            $write("FASVR: exit() called from DUT at %t.", $time);
            $display("FASVR: error code: %d", dut.tile.core.io_debug_syscall1);
            $finish;
          end 
          if (tohost == 8'h03) begin  // print char
            $write("%c",
              dut.tile.core.io_debug_syscall1
            );
          end
          if (tohost == 8'h04) begin  // print float
            $write("%.4f (0x%x) ",
              $bitstoshortreal(dut.tile.core.io_debug_syscall1),
              dut.tile.core.io_debug_syscall1
            );
          end
        end
      end
    join

  end
endmodule
