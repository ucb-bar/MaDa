`timescale 1ns / 1ps


interface axi_interface #(
  parameter DATA_WIDTH = 32,
  parameter ADDR_WIDTH = 32,
  parameter ID_WIDTH = 0
) ();
    // Write Address Channel
  logic                     awvalid;
  logic                     awready;
  logic [ID_WIDTH-1:0]      awid;
  logic [ADDR_WIDTH-1:0]    awaddr;
  logic [7:0]               awlen;
  logic [2:0]               awsize;
  logic [1:0]               awburst;
  // Write Data Channel
  logic                     wvalid;
  logic                     wready;
  logic [DATA_WIDTH-1:0]    wdata;
  logic [DATA_WIDTH/8-1:0]  wstrb;
  logic                     wlast;
  // Write Response Channel
  logic                     bvalid;
  logic                     bready;
  logic [ID_WIDTH-1:0]      bid;
  logic [1:0]               bresp;
  // Read Address Channel
  logic                     arvalid;
  logic                     arready;
  logic [ID_WIDTH-1:0]      arid;
  logic [ADDR_WIDTH-1:0]    araddr;
  logic [7:0]               arlen;
  logic [2:0]               arsize;
  logic [1:0]               arburst;
  // Read Data Channel
  logic                     rvalid;
  logic                     rready;
  logic [ID_WIDTH-1:0]      rid;
  logic [DATA_WIDTH-1:0]    rdata;
  logic [1:0]               rresp;
  logic                     rlast;

endinterface

module Axi4MemoryForTestTestbench();
  logic clock, reset;
  initial clock = 'b0;
  initial reset = 'b1;
  always #5 clock = ~clock;

  axi_interface axi();

  Axi4MemoryForTest mem (
    .clock(clock),
    .reset(reset),
    .io_s_axi_aw_ready(axi.awready),
    .io_s_axi_aw_valid(axi.awvalid),
    .io_s_axi_aw_bits_addr(axi.awaddr),
    .io_s_axi_aw_bits_len(axi.awlen),
    .io_s_axi_aw_bits_size(axi.awsize),
    .io_s_axi_aw_bits_burst(axi.awburst),
    .io_s_axi_w_ready(axi.wready),
    .io_s_axi_w_valid(axi.wvalid),
    .io_s_axi_w_bits_data(axi.wdata),
    .io_s_axi_w_bits_strb(axi.wstrb),
    .io_s_axi_w_bits_last(axi.wlast),
    .io_s_axi_b_ready(axi.bready),
    .io_s_axi_b_valid(axi.bvalid),
    .io_s_axi_b_bits_resp(axi.bresp),
    .io_s_axi_ar_ready(axi.arready),
    .io_s_axi_ar_valid(axi.arvalid),
    .io_s_axi_ar_bits_addr(axi.araddr),
    .io_s_axi_ar_bits_len(axi.arlen),
    .io_s_axi_ar_bits_size(axi.arsize),
    .io_s_axi_ar_bits_burst(axi.arburst),
    .io_s_axi_r_ready(axi.rready),
    .io_s_axi_r_valid(axi.rvalid),
    .io_s_axi_r_bits_data(axi.rdata),
    .io_s_axi_r_bits_resp(axi.rresp),
    .io_s_axi_r_bits_last(axi.rlast)
  );

  initial begin
    axi.awvalid = 'b0;
    axi.awid = 'h0;
    axi.awlen = 'h0;
    axi.awsize = 'h0;
    axi.awburst = 'h0;
    axi.awaddr = 'h0;

    axi.wvalid = 'b0;
    axi.wdata = 'h0;
    axi.wstrb = 'h0;
    axi.wlast = 'b1;

    axi.bready = 'b1;

    axi.arvalid = 'b0;
    axi.arid = 'h0;
    axi.arlen = 'h0;
    axi.arsize = 'h0;
    axi.arburst = 'h0;
    axi.araddr = 'h0;

    axi.rready = 'b1;

    reset = 1;
    repeat (10) @(posedge clock);
    reset = 0;
    repeat (10) @(posedge clock);


    // === read many consecutive words ===
    
    axi.arvalid = 1;
    axi.araddr = 'h00000000;
    wait (axi.arvalid && axi.arready); #0;
    @(posedge clock); #0;
    axi.araddr = 'h00000004;
    wait (axi.arvalid && axi.arready); #0;
    @(posedge clock); #0;
    axi.araddr = 'h00000008;
    wait (axi.arvalid && axi.arready); #0;
    @(posedge clock); #0;
    axi.araddr = 'h0000000C;
    wait (axi.arvalid && axi.arready); #0;
    @(posedge clock); #0;
    axi.araddr = 'h00000010;
    wait (axi.arvalid && axi.arready); #0;
    @(posedge clock); #0;
    axi.araddr = 'h00000014;
    wait (axi.arvalid && axi.arready); #0;
    @(posedge clock); #0;
    axi.araddr = 'h00000018;
    wait (axi.arvalid && axi.arready); #0;
    @(posedge clock); #0;
    axi.araddr = 'h0000001C;
    wait (axi.arvalid && axi.arready); #0;
    @(posedge clock); #0;
    axi.arvalid = 0;

    wait (axi.rvalid && axi.rready); #0;
    @(posedge clock); #0;



    // write word
    axi.awvalid = 1;
    axi.wvalid = 1;
    axi.awaddr = 'h00000010;
    axi.wdata = 'hDEADBEEF;
    axi.wstrb = 'h0F;
    fork
      begin
        wait (axi.awvalid && axi.awready); #0;
        @(posedge clock); #0;
        axi.awvalid = 0;
        axi.awaddr = 'h0;
      end
      begin
        wait (axi.wvalid && axi.wready); #0;
        @(posedge clock); #0;
        axi.wvalid = 0;
        axi.wdata = 'h0;
        axi.wstrb = 'h0;
      end
    join
    wait (axi.bvalid && axi.bready); #0;
    @(posedge clock); #0;
    $display("sw (0x%08x): 0x%016lx ", axi.awaddr, axi.wdata);

    // write word
    axi.awvalid = 1;
    axi.wvalid = 1;
    axi.awaddr = 'h00000014;
    axi.wdata = 'hBAADCAFE;
    axi.wstrb = 'h0F;
    fork
      begin
        wait (axi.awvalid && axi.awready); #0;
        @(posedge clock); #0;
        axi.awvalid = 0;
        axi.awaddr = 'h0;
      end
      begin
        wait (axi.wvalid && axi.wready); #0;
        @(posedge clock); #0;
        axi.wvalid = 0;
        axi.wdata = 'h0;
        axi.wstrb = 'h0;
      end
    join
    wait (axi.bvalid && axi.bready); #0;
    @(posedge clock); #0;
    $display("sw (0x%08x): 0x%016lx ", axi.awaddr, axi.wdata);


    // read word
    axi.arvalid = 1;
    axi.araddr = 'h00000010;
    wait (axi.arvalid && axi.arready); #0;
    @(posedge clock); #0;
    axi.arvalid = 0;
    wait (axi.rvalid && axi.rready); #0;
    @(posedge clock); #0;
    $display("lw (0x%08x): 0x%016lx ", axi.araddr, axi.rdata);


    // read word
    axi.arvalid = 1;
    axi.araddr = 'h00000014;
    wait (axi.arvalid && axi.arready); #0;
    @(posedge clock); #0;
    axi.arvalid = 0;
    wait (axi.rvalid && axi.rready); #0;
    @(posedge clock); #0;
    $display("lw (0x%08x): 0x%016lx ", axi.araddr, axi.rdata);


    // read word
    axi.arvalid = 1;
    axi.araddr = 'h00000018;
    wait (axi.arvalid && axi.arready); #0;
    @(posedge clock); #0;
    axi.arvalid = 0;
    wait (axi.rvalid && axi.rready); #0;
    @(posedge clock); #0;
    $display("lw (0x%08x): 0x%016lx ", axi.araddr, axi.rdata);


    

    repeat (10) @(posedge clock);
    $finish;
  end
endmodule
