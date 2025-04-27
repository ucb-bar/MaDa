`timescale 1ns / 1ps


interface axi_interface #(
  parameter DATA_WIDTH = 64,
  parameter ADDR_WIDTH = 12,
  parameter ID_WIDTH = 4
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

module MemorySubsystemTestbench();
  parameter DATA_WIDTH = 32;
  parameter ADDR_WIDTH = 32;
  parameter ID_WIDTH = 4;
  parameter MEM_ALIGNMENT = $clog2(DATA_WIDTH / 8);
  
  parameter FLASH_BASE    = 32'h2000_0000;
  parameter UART_BASE     = 32'h1002_0000;
  parameter GPIO_BASE     = 32'h1001_0000;
  parameter SCRATCH_BASE  = 32'h0800_0000;

  logic clock, reset;
  initial clock = 'b0;
  initial reset = 'b1;
  always #5 clock = ~clock;

  axi_interface #(32, ADDR_WIDTH, ID_WIDTH) axi32();
  axi_interface #(64, ADDR_WIDTH, ID_WIDTH) axi64();

  wire SPI_MOSI;
  wire SPI_MISO;
  wire SPI_SCLK;
  wire SPI_CS;

  SimSpiFlashModel #(
    .PLUSARG("firmware.8.hex"),
    .READONLY(0),
    .CAPACITY_BYTES(1024)
  ) sim_spi (
    .sck(SPI_SCLK),
    .cs_0(SPI_CS),
    .reset(reset),
    .dq_0(SPI_MOSI),
    .dq_1(SPI_MISO),
    .dq_2(),
    .dq_3()
  );


  MemorySubsystem sys(
    .clock(clock),
    .reset(reset),
    .io_m_axi_32_aw_valid(axi32.awvalid),
    .io_m_axi_32_aw_ready(axi32.awready),
    .io_m_axi_32_aw_bits_id(axi32.awid),
    .io_m_axi_32_aw_bits_addr(axi32.awaddr),
    .io_m_axi_32_aw_bits_len(axi32.awlen),
    .io_m_axi_32_aw_bits_size(axi32.awsize),
    .io_m_axi_32_aw_bits_burst(axi32.awburst),
    .io_m_axi_32_w_valid(axi32.wvalid),
    .io_m_axi_32_w_ready(axi32.wready),
    .io_m_axi_32_w_bits_data(axi32.wdata),
    .io_m_axi_32_w_bits_strb(axi32.wstrb),
    .io_m_axi_32_w_bits_last(axi32.wlast),
    .io_m_axi_32_b_valid(axi32.bvalid),
    .io_m_axi_32_b_ready(axi32.bready),
    .io_m_axi_32_b_bits_id(axi32.bid),
    .io_m_axi_32_b_bits_resp(axi32.bresp),
    .io_m_axi_32_ar_valid(axi32.arvalid),
    .io_m_axi_32_ar_ready(axi32.arready),
    .io_m_axi_32_ar_bits_id(axi32.arid),
    .io_m_axi_32_ar_bits_addr(axi32.araddr),
    .io_m_axi_32_ar_bits_len(axi32.arlen),
    .io_m_axi_32_ar_bits_size(axi32.arsize),
    .io_m_axi_32_ar_bits_burst(axi32.arburst),
    .io_m_axi_32_r_ready(axi32.rready),
    .io_m_axi_32_r_valid(axi32.rvalid),
    .io_m_axi_32_r_bits_id(axi32.rid),
    .io_m_axi_32_r_bits_data(axi32.rdata),
    .io_m_axi_32_r_bits_resp(axi32.rresp),
    .io_m_axi_32_r_bits_last(axi32.rlast),

    .io_m_axi_64_aw_valid(axi64.awvalid),
    .io_m_axi_64_aw_ready(axi64.awready),
    .io_m_axi_64_aw_bits_id(axi64.awid),
    .io_m_axi_64_aw_bits_addr(axi64.awaddr),
    .io_m_axi_64_aw_bits_len(axi64.awlen),
    .io_m_axi_64_aw_bits_size(axi64.awsize),
    .io_m_axi_64_aw_bits_burst(axi64.awburst),
    .io_m_axi_64_w_valid(axi64.wvalid),
    .io_m_axi_64_w_ready(axi64.wready),
    .io_m_axi_64_w_bits_data(axi64.wdata),
    .io_m_axi_64_w_bits_strb(axi64.wstrb),
    .io_m_axi_64_w_bits_last(axi64.wlast),
    .io_m_axi_64_b_valid(axi64.bvalid),
    .io_m_axi_64_b_ready(axi64.bready),
    .io_m_axi_64_b_bits_id(axi64.bid),
    .io_m_axi_64_b_bits_resp(axi64.bresp),
    .io_m_axi_64_ar_valid(axi64.arvalid),
    .io_m_axi_64_ar_ready(axi64.arready),
    .io_m_axi_64_ar_bits_id(axi64.arid),
    .io_m_axi_64_ar_bits_addr(axi64.araddr),
    .io_m_axi_64_ar_bits_len(axi64.arlen),
    .io_m_axi_64_ar_bits_size(axi64.arsize),
    .io_m_axi_64_ar_bits_burst(axi64.arburst),
    .io_m_axi_64_r_ready(axi64.rready),
    .io_m_axi_64_r_valid(axi64.rvalid),
    .io_m_axi_64_r_bits_id(axi64.rid),
    .io_m_axi_64_r_bits_data(axi64.rdata),
    .io_m_axi_64_r_bits_resp(axi64.rresp),
    .io_m_axi_64_r_bits_last(axi64.rlast),

    .io_qspi_cs(SPI_CS),
    .io_qspi_sck(SPI_SCLK),
    .io_qspi_dq_0(SPI_MOSI),
    .io_qspi_dq_1(SPI_MISO),
    .io_qspi_dq_2(),
    .io_qspi_dq_3()
  );


  logic [31:0] tb_addr;
  logic [63:0] tb_data;
  

  initial begin
    tb_addr = 32'h0;
    tb_data = 64'h0;

    axi32.awvalid = 1'b0;
    axi32.awid = 4'h0;
    axi32.awlen = 8'h0;
    axi32.awsize = 3'h0;
    axi32.awburst = 2'h0;
    axi32.awaddr = 32'h0;

    axi32.wvalid = 1'b0;
    axi32.wdata = 32'h0;
    axi32.wstrb = 4'h0;
    axi32.wlast = 1'b1;

    axi32.bready = 1'b1;

    axi32.arvalid = 1'b0;
    axi32.arid = 4'h0;
    axi32.arlen = 8'h0;
    axi32.arsize = 3'h0;
    axi32.arburst = 2'h0;
    axi32.araddr = 32'h0;

    axi32.rready = 1'b1;

    axi64.awvalid = 1'b0;
    axi64.awid = 4'h1;
    axi64.awlen = 8'h0;
    axi64.awsize = 3'h0;
    axi64.awburst = 2'h0;
    axi64.awaddr = 32'h0;

    axi64.wvalid = 1'b0;
    axi64.wdata = 64'h0;
    axi64.wstrb = 8'h0;
    axi64.wlast = 1'b1;

    axi64.bready = 1'b1;

    axi64.arvalid = 1'b0;
    axi64.arid = 4'h1;
    axi64.arlen = 8'h0;
    axi64.arsize = 3'h0;
    axi64.arburst = 2'h0;
    axi64.araddr = 32'h0;

    axi64.rready = 1'b1;

    reset = 1;
    repeat (10) @(posedge clock);
    reset = 0;
    repeat (10) @(posedge clock);


    @(posedge clock); #0;



    // write word
    tb_addr = SCRATCH_BASE + 'h00;
    tb_data = 64'hDEADBEEF;
    axi32.awvalid = 1;
    axi32.wvalid = 1;
    axi32.awaddr = tb_addr;
    axi32.wdata = tb_data[31:0];
    axi32.wstrb = 'hFF;
    fork
      begin
        wait (axi32.awvalid && axi32.awready); #0;
        @(posedge clock); #0;
        axi32.awvalid = 0;
        axi32.awaddr = 'h0;
      end
      begin
        wait (axi32.wvalid && axi32.wready); #0;
        @(posedge clock); #0;
        axi32.wvalid = 0;
        axi32.wdata = 'h0;
        axi32.wstrb = 'h0;
      end
    join
    wait (axi32.bvalid && axi32.bready); #0;
    @(posedge clock); #0;
    $display("sw (0x%08x): 0x%016lx ", tb_addr, tb_data);


    // write word
    tb_addr = SCRATCH_BASE + 'h04;
    tb_data = 64'h01020304;
    axi32.awvalid = 1;
    axi32.wvalid = 1;
    axi32.awaddr = tb_addr;
    axi32.wdata = tb_data[31:0];
    axi32.wstrb = 'h0F;
    fork
      begin
        wait (axi32.awvalid && axi32.awready); #0;
        @(posedge clock); #0;
        axi32.awvalid = 0;
        axi32.awaddr = 'h0;
      end
      begin
        wait (axi32.wvalid && axi32.wready); #0;
        @(posedge clock); #0;
        axi32.wvalid = 0;
        axi32.wdata = 'h0;
        axi32.wstrb = 'h0;
      end
    join
    wait (axi32.bvalid && axi32.bready); #0;
    @(posedge clock); #0;
    $display("sw (0x%08x): 0x%016lx ", tb_addr, tb_data);


    // read word
    tb_addr = SCRATCH_BASE + 'h00;
    axi32.arvalid = 1;
    axi32.araddr = tb_addr;
    wait (axi32.arvalid && axi32.arready); #0;
    @(posedge clock); #0;
    axi32.arvalid = 0;
    wait (axi32.rvalid && axi32.rready); #0;
    @(posedge clock); #0;
    tb_data = axi32.rdata;
    $display("lw (0x%08x): 0x%016lx ", tb_addr, tb_data);


    // read word
    tb_addr = SCRATCH_BASE + 'h04;
    axi32.arvalid = 1;
    axi32.araddr = tb_addr;
    wait (axi32.arvalid && axi32.arready); #0;
    @(posedge clock); #0;
    axi32.arvalid = 0;
    wait (axi32.rvalid && axi32.rready); #0;
    @(posedge clock); #0;
    tb_data = axi32.rdata;
    $display("lw (0x%08x): 0x%016lx ", tb_addr, tb_data);


    // read word
    tb_addr = SCRATCH_BASE + 'h00;
    axi64.arvalid = 1;
    axi64.araddr = tb_addr;
    wait (axi64.arvalid && axi64.arready); #0;
    @(posedge clock); #0;
    axi64.arvalid = 0;
    wait (axi64.rvalid && axi64.rready); #0;
    @(posedge clock); #0;
    tb_data = axi64.rdata;
    $display("ld (0x%08x): 0x%016lx ", tb_addr, tb_data);




    // read word from Flash
    tb_addr = FLASH_BASE + 'h00;
    axi32.arvalid = 1;
    axi32.araddr = tb_addr;
    wait (axi32.arvalid && axi32.arready); #0;
    @(posedge clock); #0;
    axi32.arvalid = 0;
    wait (axi32.rvalid && axi32.rready); #0;
    @(posedge clock); #0;
    tb_data = axi32.rdata;
    $display("lw (0x%08x): 0x%016lx ", tb_addr, tb_data);


    // read word from Flash
    tb_addr = FLASH_BASE + 'h04;
    axi32.arvalid = 1;
    axi32.araddr = tb_addr;
    wait (axi32.arvalid && axi32.arready); #0;
    @(posedge clock); #0;
    axi32.arvalid = 0;
    wait (axi32.rvalid && axi32.rready); #0;
    @(posedge clock); #0;
    tb_data = axi32.rdata;
    $display("lw (0x%08x): 0x%016lx ", tb_addr, tb_data);


    // read word from Flash
    tb_addr = FLASH_BASE + 'h00;
    axi64.arvalid = 1;
    axi64.araddr = tb_addr;
    wait (axi64.arvalid && axi64.arready); #0;
    @(posedge clock); #0;
    axi64.arvalid = 0;
    wait (axi64.rvalid && axi64.rready); #0;
    @(posedge clock); #0;
    tb_data = axi64.rdata;
    $display("ld (0x%08x): 0x%016lx ", tb_addr, tb_data);



    
    repeat (100) @(posedge clock);
    $finish;
  end
endmodule
