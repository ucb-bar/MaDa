`timescale 1ns / 1ps


module Axi4LiteTestBench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;
  
  // setup clock and reset
  reg clock, reset;
  initial clock = 'b0;
  always #(CLOCK_PERIOD/2) clock = ~clock;


  logic axi_aw_valid;
  logic axi_aw_ready;
  logic [31:0] axi_aw_addr;
  logic axi_w_valid;
  logic axi_w_ready;
  logic [31:0] axi_w_data;
  logic [3:0] axi_w_strb;
  logic axi_b_valid;
  logic axi_b_ready;
  logic [1:0] axi_b_resp;

  logic axi_ar_valid;
  logic axi_ar_ready;
  logic [31:0] axi_ar_addr;
  logic axi_r_valid;
  logic axi_r_ready;
  logic [31:0] axi_r_data;
  logic [1:0] axi_r_resp;

  
  blk_mem_gen_1 mem (
    .s_aclk(clock),
    .s_aresetn(~reset),
    .s_axi_awaddr(axi_aw_addr),
    .s_axi_awvalid(axi_aw_valid),
    .s_axi_awready(axi_aw_ready),
    .s_axi_wdata(axi_w_data),
    .s_axi_wstrb(axi_w_strb),
    .s_axi_wvalid(axi_w_valid),
    .s_axi_wready(axi_w_ready),
    .s_axi_bresp(axi_b_resp),
    .s_axi_bvalid(axi_b_valid),
    .s_axi_bready(axi_b_ready),
    .s_axi_araddr(axi_ar_addr),
    .s_axi_arvalid(axi_ar_valid),
    .s_axi_arready(axi_ar_ready),
    .s_axi_rdata(axi_r_data),
    .s_axi_rresp(axi_r_resp),
    .s_axi_rvalid(axi_r_valid),
    .s_axi_rready(axi_r_ready)
  );

  initial begin

    axi_aw_valid = 'b0;
    axi_aw_addr = 'h00000000;
    axi_w_valid = 'b0;
    axi_w_data = 'h00000000;
    axi_w_strb = 'h0;
    axi_b_ready = 'b1;
    axi_ar_valid = 'b0;
    axi_ar_addr = 'h00000000;
    axi_r_ready = 'b1;

    reset = 1'b1;
    repeat (4) @(posedge clock);
    reset = 1'b0;
    repeat (4) @(posedge clock);

    axi_aw_valid = 'b1;
    axi_aw_addr = 'h00000000;
    repeat (1) @(posedge clock);
    axi_aw_valid = 'b0;
    axi_aw_addr = 'h00000000;
    axi_w_valid = 'b1;
    axi_w_data = 'h11111111;
    axi_w_strb = 'b1111;
    repeat (1) @(posedge clock);
    axi_w_valid = 'b0;
    axi_w_data = 'h00000000;
    axi_w_strb = 'h00;

    
    axi_aw_valid = 'b1;
    axi_aw_addr = 'h00000004;
    repeat (1) @(posedge clock);
    axi_aw_valid = 'b0;
    axi_aw_addr = 'h00000000;
    axi_w_valid = 'b1;
    axi_w_data = 'h22222222;
    axi_w_strb = 'b1111;
    repeat (1) @(posedge clock);
    axi_w_valid = 'b0;
    axi_w_data = 'h00000000;
    axi_w_strb = 'h00;

    
    axi_aw_valid = 'b1;
    axi_aw_addr = 'h00000008;
    repeat (1) @(posedge clock);
    axi_aw_valid = 'b0;
    axi_aw_addr = 'h00000000;
    axi_w_valid = 'b1;
    axi_w_data = 'h33333333;
    axi_w_strb = 'b1111;
    repeat (1) @(posedge clock);
    axi_w_valid = 'b0;
    axi_w_data = 'h00000000;
    axi_w_strb = 'h00;
    

    axi_aw_valid = 'b1;
    axi_aw_addr = 'h0000000C;
    repeat (1) @(posedge clock);
    axi_aw_valid = 'b0;
    axi_aw_addr = 'h00000000;
    axi_w_valid = 'b1;
    axi_w_data = 'h44444444;
    axi_w_strb = 'b1111;
    repeat (1) @(posedge clock);
    axi_w_valid = 'b0;
    axi_w_data = 'h00000000;
    axi_w_strb = 'h00;


    repeat (10) @(posedge clock);

    axi_ar_valid = 'b1;
    axi_ar_addr = 'h00000000;
    repeat (1) @(posedge clock);
    axi_ar_valid = 'b1;
    axi_ar_addr = 'h00000004;
    repeat (1) @(posedge clock);
    axi_ar_valid = 'b1;
    axi_ar_addr = 'h00000008;
    repeat (1) @(posedge clock);
    axi_ar_valid = 'b1;
    axi_ar_addr = 'h0000000C;
    repeat (1) @(posedge clock);
    axi_ar_valid = 'b0;
    axi_ar_addr = 'h00000000;

    repeat (10) @(posedge clock);
    $finish;
  end
endmodule
