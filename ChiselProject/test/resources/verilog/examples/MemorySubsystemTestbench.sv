`timescale 1ns / 1ps


interface axi_interface #(
  parameter DATA_WIDTH = 64,
  parameter ADDR_WIDTH = 12
) ();
    // Write Address Channel
  logic                     awvalid;
  logic                     awready;
  logic [ADDR_WIDTH-1:0]    awaddr;

  // Write Data Channel
  logic                     wvalid;
  logic                     wready;
  logic [DATA_WIDTH-1:0]    wdata;
  logic [DATA_WIDTH/8-1:0]  wstrb;

  // Write Response Channel
  logic                     bvalid;
  logic                     bready;
  logic [1:0]               bresp;

  // Read Address Channel
  logic                     arvalid;
  logic                     arready;
  logic [ADDR_WIDTH-1:0]    araddr;

  // Read Data Channel
  logic                     rvalid;
  logic                     rready;
  logic [DATA_WIDTH-1:0]    rdata;
  logic [1:0]               rresp;

endinterface

module MemorySubsystemTestbench();
  parameter DATA_WIDTH = 32;
  parameter ADDR_WIDTH = 32;
  parameter MEM_ALIGNMENT = $clog2(DATA_WIDTH / 8);
  parameter SCRATCH_BASE = 32'h00000000;

  logic clock, reset;
  initial clock = 'b0;
  initial reset = 'b1;
  always #5 clock = ~clock;

  axi_interface #(DATA_WIDTH, ADDR_WIDTH) axi();

  MemorySubsystem sys(
    .clock(clock),
    .reset(reset),
    .io_m_axi_aw_valid(axi.awvalid),
    .io_m_axi_aw_ready(axi.awready),
    .io_m_axi_aw_bits_addr(axi.awaddr),
    .io_m_axi_w_valid(axi.wvalid),
    .io_m_axi_w_ready(axi.wready),
    .io_m_axi_w_bits_data(axi.wdata),
    .io_m_axi_w_bits_strb(axi.wstrb),
    .io_m_axi_b_valid(axi.bvalid),
    .io_m_axi_b_ready(axi.bready),
    .io_m_axi_b_bits_resp(axi.bresp),
    .io_m_axi_ar_valid(axi.arvalid),
    .io_m_axi_ar_ready(axi.arready),
    .io_m_axi_ar_bits_addr(axi.araddr),
    .io_m_axi_r_ready(axi.rready),
    .io_m_axi_r_valid(axi.rvalid),
    .io_m_axi_r_bits_data(axi.rdata),
    .io_m_axi_r_bits_resp(axi.rresp)
  );

  logic [DATA_WIDTH-1:0] read_data;

  initial begin
    $dumpfile("wave.fst");  // or "wave.vcd" if using --trace
    $dumpvars(0, MemorySubsystemTestbench);
  end

  initial begin
    axi.awvalid = 1'b0;
    axi.wvalid = 1'b0;
    axi.bready = 1'b1;

    axi.arvalid = 1'b0;
    axi.rready = 1'b1;

    reset = 1;
    repeat (10) @(posedge clock);
    reset = 0;
    repeat (10) @(posedge clock);


    @(posedge clock); #0;

    // read word
    axi.arvalid = 1;
    axi.araddr = SCRATCH_BASE + 'h4 + 0*(1<<MEM_ALIGNMENT);

    @(posedge clock); #0;
    wait (axi.arvalid && axi.arready); #0;
    axi.arvalid = 0;
    
    @(posedge clock); #0;
    wait (axi.rvalid && axi.rready); #0;
    
    
    // write word
    axi.awvalid = 1;
    axi.wvalid = 1;
    axi.awaddr = SCRATCH_BASE + 'h4 + 0*(1<<MEM_ALIGNMENT);
    axi.wdata = 'hDEADBEEF;
    axi.wstrb = 'hFF;

    fork
      begin
        @(posedge clock); #0;
        wait (axi.awvalid && axi.awready); #0;
        axi.awvalid = 0;
        axi.awaddr = 'h0;
      end
      begin
        @(posedge clock); #0;
        wait (axi.wvalid && axi.wready); #0;
        axi.wvalid = 0;
        axi.wdata = 'h0;
        axi.wstrb = 'h0;
      end
    join

    @(posedge clock); #0;
    wait (axi.bvalid && axi.bready); #0;

    // read word
    axi.arvalid = 1;
    axi.araddr = SCRATCH_BASE + 'h4 + 0*(1<<MEM_ALIGNMENT);

    @(posedge clock); #0;
    wait (axi.arvalid && axi.arready); #0;
    axi.arvalid = 0;
    
    @(posedge clock); #0;
    wait (axi.rvalid && axi.rready); #0;
    
    repeat (100) @(posedge clock);
    $finish;
  end
endmodule
