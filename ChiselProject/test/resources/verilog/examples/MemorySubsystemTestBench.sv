`timescale 1ns / 1ps


interface axi_interface #(
  parameter DATA_WIDTH = 64,
  parameter ADDR_WIDTH = 12,
  parameter ID_WIDTH = 4
) (input logic clock);
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

  // Add these tasks to your interface
  task automatic write_word(input logic [ADDR_WIDTH-1:0] addr, input logic [DATA_WIDTH-1:0] data, input logic [DATA_WIDTH/8-1:0] mask);
    awvalid = 1;
    wvalid = 1;
    awaddr = addr;
    wdata = data;
    wstrb = mask;
    wlast = 'b1;

    fork
      begin
        wait (awvalid && awready);
        @(posedge clock);
        awvalid = 0;
        awaddr = 'h0;
      end
      begin
        wait (wvalid && wready);
        @(posedge clock);
        wvalid = 0;
        wdata = 'h0;
        wstrb = 'h0;
        wlast = 'b0;
      end
    join

    wait (bvalid && bready);
    @(posedge clock);
  endtask

  task automatic read_word(input logic [ADDR_WIDTH-1:0] addr, output logic [DATA_WIDTH-1:0] data);
    arvalid = 1;
    araddr = addr;

    wait (arvalid && arready);
    @(posedge clock);
    arvalid = 0;
    
    wait (rvalid && rready);
    @(posedge clock);
    data = rdata;
  endtask

  // Optional: Combined task to write and verify read
  task automatic write_and_verify(input logic [ADDR_WIDTH-1:0] addr, input logic [DATA_WIDTH-1:0] data, input logic [DATA_WIDTH/8-1:0] mask, input logic [DATA_WIDTH-1:0] expected_data);
    logic [DATA_WIDTH-1:0] read_data;
    
    write_word(addr, data, mask);
    read_word(addr, read_data);
    
    assert (read_data == expected_data) else 
      $error($time, "\tword read failed, expected 0x%x, got 0x%x", expected_data, read_data);
  endtask
endinterface

module MemorySubsystemTestbench();
  parameter DATA_WIDTH = 64;
  parameter ADDR_WIDTH = 32;
  parameter MEM_ALIGNMENT = $clog2(DATA_WIDTH / 8);
  parameter SCRATCH_BASE = 32'h00000000;

  logic clock, reset;
  initial clock = 'b0;
  initial reset = 'b1;
  always #5 clock = ~clock;

  axi_interface #(DATA_WIDTH, ADDR_WIDTH) axi(clock);

  MemorySubsystem sys(
    .clock(clock),
    .reset(reset),
    .io_m_axi_aw_valid(axi.awvalid),
    .io_m_axi_aw_ready(axi.awready),
    .io_m_axi_aw_bits_id(axi.awid),
    .io_m_axi_aw_bits_addr(axi.awaddr),
    .io_m_axi_aw_bits_len(axi.awlen),
    .io_m_axi_aw_bits_size(axi.awsize),
    .io_m_axi_aw_bits_burst(axi.awburst),
    .io_m_axi_w_valid(axi.wvalid),
    .io_m_axi_w_ready(axi.wready),
    .io_m_axi_w_bits_data(axi.wdata),
    .io_m_axi_w_bits_strb(axi.wstrb),
    .io_m_axi_w_bits_last(axi.wlast),
    .io_m_axi_b_valid(axi.bvalid),
    .io_m_axi_b_ready(axi.bready),
    .io_m_axi_b_bits_id(axi.bid),
    .io_m_axi_b_bits_resp(axi.bresp),
    .io_m_axi_ar_valid(axi.arvalid),
    .io_m_axi_ar_ready(axi.arready),
    .io_m_axi_ar_bits_id(axi.arid),
    .io_m_axi_ar_bits_addr(axi.araddr),
    .io_m_axi_ar_bits_len(axi.arlen),
    .io_m_axi_ar_bits_size(axi.arsize),
    .io_m_axi_ar_bits_burst(axi.arburst),
    .io_m_axi_r_ready(axi.rready),
    .io_m_axi_r_valid(axi.rvalid),
    .io_m_axi_r_bits_id(axi.rid),
    .io_m_axi_r_bits_data(axi.rdata),
    .io_m_axi_r_bits_resp(axi.rresp),
    .io_m_axi_r_bits_last(axi.rlast)
  );

  logic [DATA_WIDTH-1:0] read_data;

  initial begin
    axi.awvalid = 1'b0;
    axi.awid = 'h0;
    axi.awlen = 'h0;
    axi.awsize = 'b010;
    axi.awburst = 'h0;
    axi.wvalid = 1'b0;
    axi.wlast = 'b0;
    axi.bready = 1'b1;

    axi.arvalid = 1'b0;
    axi.arid = 'h0;
    axi.arlen = 'h0;
    axi.arsize = 'b010;
    axi.arburst = 'h0;
    axi.rready = 1'b1;

    reset = 1;
    repeat (10) @(posedge clock);
    reset = 0;
    repeat (10) @(posedge clock);

    
    axi.read_word(SCRATCH_BASE + 'h4 + 0*(1<<MEM_ALIGNMENT), read_data);

    axi.write_and_verify(SCRATCH_BASE + 0*(1<<MEM_ALIGNMENT), 'hDEADBEEF, 'hF, 'hDEADBEEF);

    axi.write_and_verify(SCRATCH_BASE + 1*(1<<MEM_ALIGNMENT), 'hDEADBEEF, 'h3, 'h0000BEEF);
    axi.write_and_verify(SCRATCH_BASE + 2*(1<<MEM_ALIGNMENT), 'hDEADBEEF, 'h1, 'h000000EF);

    axi.write_and_verify(SCRATCH_BASE + 3*(1<<MEM_ALIGNMENT), 'hDEADBEEF, 'hC, 'hDEAD0000);
    axi.write_and_verify(SCRATCH_BASE + 4*(1<<MEM_ALIGNMENT), 'hDEADBEEF, 'h2, 'h0000BE00);

    repeat (100) @(posedge clock);
    $finish;
  end
endmodule
