`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 11/26/2024 11:45:52 AM
// Design Name: 
// Module Name: tb
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module TB_Chip();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;

  // setup clock and reset
  reg clk, rst;
  initial clk = 'b0;
  always #(CLOCK_PERIOD/2) clk = ~clk;

  logic uart_txd_in, uart_rxd_out;
  logic led0_b, led1_b, led2_b;

  reg jtag_clk;
  initial jtag_clk = 'b0;
  always #(CLOCK_PERIOD*2) jtag_clk = ~jtag_clk;

  logic ft_link_txd, ft_link_rxd;
  initial ft_link_txd = 'b0;

  DDRArty100T u_chip(
    .io_CLK100MHZ(clk),
    .io_ck_rst(rst),
    .io_uart_txd_in(uart_txd_in),
    .io_uart_rxd_out(uart_rxd_out),
    .io_led0_b(led0_b),
    .io_led1_b(led1_b),
    .io_led2_b(led2_b),
    
    .io_jd_0(),
    .io_jd_1('b0),
    .io_jd_2(jtag_clk),
    .io_jd_3(ft_link_rxd),
    .io_jd_4('b0),
    .io_jd_5('b0),
    .io_jd_6('b0),
    .io_jd_7(ft_link_txd)
  );
  
  initial begin
    rst = 1'b0;

    
    repeat (2) @(posedge clk);
    rst = 1'b1;

    repeat (10000) @(posedge clk);
    $finish;
  end
endmodule
