`timescale 1ns / 1ps


module Axi4SpiFlashTestBench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;
  
  // setup clock and reset
  reg clock, reset;
  initial clock = 'b0;
  always #(CLOCK_PERIOD/2) clock = ~clock;

  wire axi_clk;
  wire axi_aresetn;

  wire qspi_axi_arid;
  wire qspi_axi_araddr;
  wire qspi_axi_arlen;
  wire qspi_axi_arsize;
  wire qspi_axi_arburst;
  wire qspi_axi_arlock;
  wire qspi_axi_arcache;
  wire qspi_axi_arprot;
  wire qspi_axi_arvalid;
  wire qspi_axi_arready;
  wire qspi_axi_rid;
  wire qspi_axi_rdata;
  wire qspi_axi_rresp;
  wire qspi_axi_rlast;
  wire qspi_axi_rvalid;
  wire qspi_axi_rready;

  wire io0_i;
  wire io0_o;
  wire io0_t;
  wire io1_i;
  wire io1_o;
  wire io1_t;
  wire sck_i;
  wire sck_o;
  wire sck_t;
  wire ss_i;
  wire ss_o;
  wire ss_t;


  wire IO0_IO;
  wire IO1_IO;
  wire SCK_IO;
  wire SS_IO;



  assign axi_clk = clock;
  assign axi_aresetn = ~reset;


  wire IO0_IO_mem;
  wire IO1_IO_mem;

  wire sio0_i;
  wire sio0_o;
  wire sio0_t;
  wire sio1_i;
  wire sio1_o;
  wire sio1_t;
  wire sio2_i;
  wire sio2_o;
  wire sio2_t;
  wire sio3_i;
  wire sio3_o;
  wire sio3_t;


  IOBUF qspi_io0_mem (
    .O(sio0_i),
    .IO(IO0_IO_mem),
    .I(sio0_o),
    .T(sio_t_red)
   );
           
  IOBUF qspi_io1_mem (
    .O(sio1_i),
    .IO(IO1_IO_mem),
    .I(sio1_o),
    .T(sio1_t)
  );

  memory #(
    .C_FIFO_DEPTH(256),
    .C_ADDR_WIDTH(256),
    .C_SPI_MODE(0),
    .C_DATA_WIDTH(8)
  ) sim_memory (
    .MODEL_CLK(axi_clk),
    .MODEL_RESET(axi_aresetn),
    .Core_Clk(SCK_IO),
    .Chip_Selectn(SS_IO),
	
    // -------------------------------
    // --*SPI port interface      * --
    // -------------------------------
    .io0_i(sio0_i),
    .io0_o(sio0_o),
    .io0_t(sio0_t),
    // -------------------------------
    .io1_i(sio1_i),
    .io1_o(sio1_o),
    .io1_t(sio1_t),
    // -----------------
    // -- quad mode pins
    // -----------------
    .io2_i(sio2_i),
    .io2_o(sio2_o),
    .io2_t(sio2_t),
    // ---------------
    .io3_i(sio3_i),
    .io3_o(sio3_o),
    .io3_t(sio3_t)
  );




  IOBUF qspi_io0_0 (
    .O(io0_i),
    .IO(IO0_IO),
    .I(io0_o),
    .T(io0_t)
  );

  IOBUF qspi_io1_0 (
    .O(io1_i),
    .IO(IO1_IO),
    .I(io1_o),
    .T(io1_t)
  );

  IOBUF QSPI_SCK_0 (
    .O(sck_i),
    .IO(SCK_IO),
    .I(sck_o),
    .T(sck_t)
  );

  IOBUF QSPI_SS_mem (
    .O(ss_i),
    .IO(SS_IO),
    .I(ss_o),
    .T(ss_t)
  );

  axi_quad_spi_0 dut (
    .ext_spi_clk(axi_clk),
    .s_axi_aclk(axi_clk),
    .s_axi_aresetn(axi_aresetn),
    .s_axi4_aclk(axi_clk),
    .s_axi4_aresetn(axi_aresetn),
    .s_axi_awaddr('h0),
    .s_axi_awvalid('b0),
    .s_axi_awready(),
    .s_axi_wdata('h0),
    .s_axi_wstrb('h0),
    .s_axi_wvalid('b0),
    .s_axi_wready(),
    .s_axi_bresp(),
    .s_axi_bvalid(),
    .s_axi_bready('b0),
    .s_axi_araddr('h0),
    .s_axi_arvalid('b0),
    .s_axi_arready(),
    .s_axi_rdata(),
    .s_axi_rresp(),
    .s_axi_rvalid(),
    .s_axi_rready('b0),
    .s_axi4_awid('h0),
    .s_axi4_awaddr('h0),
    .s_axi4_awlen('h0),
    .s_axi4_awsize('h0),
    .s_axi4_awburst('h0),
    .s_axi4_awlock('b0),
    .s_axi4_awcache('h0),
    .s_axi4_awprot('h0),
    .s_axi4_awvalid('b0),
    .s_axi4_awready(),
    .s_axi4_wdata('h0),
    .s_axi4_wstrb('h0),
    .s_axi4_wlast('b0),
    .s_axi4_wvalid('b0),
    .s_axi4_wready(),
    .s_axi4_bid(),
    .s_axi4_bresp(),
    .s_axi4_bvalid(),
    .s_axi4_bready('b0),
    .s_axi4_arid(qspi_axi_arid),
    .s_axi4_araddr(qspi_axi_araddr),
    .s_axi4_arlen(qspi_axi_arlen),
    .s_axi4_arsize(qspi_axi_arsize),
    .s_axi4_arburst(qspi_axi_arburst),
    .s_axi4_arlock(qspi_axi_arlock),
    .s_axi4_arcache(qspi_axi_arcache),
    .s_axi4_arprot(qspi_axi_arprot),
    .s_axi4_arvalid(qspi_axi_arvalid),
    .s_axi4_arready(qspi_axi_arready),
    .s_axi4_rid(qspi_axi_rid),
    .s_axi4_rdata(qspi_axi_rdata),
    .s_axi4_rresp(qspi_axi_rresp),
    .s_axi4_rlast(qspi_axi_rlast),
    .s_axi4_rvalid(qspi_axi_rvalid),
    .s_axi4_rready(qspi_axi_rready),
    .io0_i(io0_i),
    .io0_o(io0_o),
    .io0_t(io0_t),
    .io1_i(io1_i),
    .io1_o(io1_o),
    .io1_t(io1_t),
    .sck_i(sck_i),
    .sck_o(sck_o),
    .sck_t(sck_t),
    .ss_i(ss_i),
    .ss_o(ss_o),
    .ss_t(ss_t),
    .ip2intc_irpt(ip2intc_irpt)
  );


  initial begin
    reset = 1'b1;
    repeat (10) @(posedge clock);
    reset = 1'b0;
    repeat (10) @(posedge clock);


    repeat (10) @(posedge clock);
    $finish;
  end
endmodule
