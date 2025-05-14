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


module DDR_TB_Chip();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;

  // setup clock and reset
  reg clk, rst;
  initial clk = 'b0;
  always #(CLOCK_PERIOD/2) clk = ~clk;

  wire uart_txd_in, uart_rxd_out;
  wire led0_b, led1_b, led2_b;

  reg jtag_clk;
  initial jtag_clk = 'b0;
  always #(CLOCK_PERIOD*2) jtag_clk = ~jtag_clk;

  reg ft_link_txd;
  initial ft_link_txd = 'b0;
  wire ft_link_rxd;

  wire [13:0] ddr_ddr3_addr;
  wire [2:0] ddr_ddr3_ba;
  wire ddr_ddr3_ras_n, ddr_ddr3_cas_n, ddr_ddr3_we_n, ddr_ddr3_reset_n;
  wire [1:0] ddr_ddr3_dm;
  wire ddr_ddr3_odt;
  wire [15:0] ddr_ddr3_dq;
  wire [1:0] ddr_ddr3_dqs_n;
  wire [1:0] ddr_ddr3_dqs_p;

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
    .io_jd_7(ft_link_txd),

    .io_ddr_ddr3_addr(ddr_ddr3_addr),
    .io_ddr_ddr3_ba(ddr_ddr3_ba),
    .io_ddr_ddr3_ras_n(ddr_ddr3_ras_n),
    .io_ddr_ddr3_cas_n(ddr_ddr3_cas_n),
    .io_ddr_ddr3_we_n(ddr_ddr3_we_n),
    .io_ddr_ddr3_reset_n(ddr_ddr3_reset_n),
    .io_ddr_ddr3_ck_p(ddr_ddr3_ck_p),
    .io_ddr_ddr3_ck_n(ddr_ddr3_ck_n),
    .io_ddr_ddr3_cke(ddr_ddr3_cke),
    .io_ddr_ddr3_cs_n(ddr_ddr3_cs_n),
    .io_ddr_ddr3_dm(ddr_ddr3_dm),
    .io_ddr_ddr3_odt(ddr_ddr3_odt),
    .io_ddr_ddr3_dq(ddr_ddr3_dq),
    .io_ddr_ddr3_dqs_n(ddr_ddr3_dqs_n),
    .io_ddr_ddr3_dqs_p(ddr_ddr3_dqs_p)
  );

  ddr3_model ddr3_model_inst(
    .ck(ddr_ddr3_ck_p),
    .ck_n(ddr_ddr3_ck_n),
    .rst_n(~rst),
    .cke(ddr_ddr3_cke),
    .cs_n(ddr_ddr3_cs_n),
    .ras_n(ddr_ddr3_ras_n),
    .cas_n(ddr_ddr3_cas_n),
    .we_n(ddr_ddr3_we_n),
    .ba(ddr_ddr3_ba),
    .addr(ddr_ddr3_addr),
    .dm_tdqs(ddr_ddr3_dm),
    .dqs(ddr_ddr3_dqs_p),
    .dqs_n(ddr_ddr3_dqs_n),
    .dq(ddr_ddr3_dq),
    .odt(ddr_ddr3_odt)
  );
  
  initial begin
    rst = 1'b0;

    
    repeat (2) @(posedge clk);
    rst = 1'b1;

    repeat (200000) @(posedge clk);
    $finish;
  end
endmodule
