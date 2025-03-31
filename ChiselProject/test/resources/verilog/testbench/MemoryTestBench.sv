`timescale 1ns / 1ps

module MemoryTestBench();
  logic clock, reset;  
  initial clock = 'b0;
  initial reset = 'b1;
  always #5 clock = ~clock;

  logic         axi_32_arvalid;
  logic         axi_32_arready;
  logic [3:0]   axi_32_arid;
  logic [31:0]  axi_32_araddr;
  logic [3:0]   axi_32_arlen;
  logic [2:0]   axi_32_arsize;
  logic [1:0]   axi_32_arburst;
  logic         axi_32_arlock;
  logic [3:0]   axi_32_arcache;
  logic [2:0]   axi_32_arprot;
  logic [3:0]   axi_32_arqos;
  logic         axi_32_rready;
  logic         axi_32_rvalid;
  logic [3:0]   axi_32_rid;
  logic [31:0]  axi_32_rdata;
  logic [1:0]   axi_32_rresp;
  logic         axi_32_rlast;


  logic         axi_64_arvalid;
  logic [3:0]   axi_64_arid;
  logic [31:0]  axi_64_araddr;
  logic [3:0]   axi_64_arlen;
  logic         axi_64_rready;
  logic         axi_64_rvalid;
  logic [63:0]  axi_64_rdata;


  assign axi_64_arready = 'b1;
  assign axi_64_arlen = 'b0;
  assign axi_64_arid = axi_32_arid;
  assign axi_64_rid = 'h0;
  assign axi_64_rresp = 'b0;
  assign axi_64_rlast = 'b1;

  axi_traffic_gen_0 traffic_gen (
    .s_axi_aclk(clock),
    .s_axi_aresetn(~reset),
    .core_ext_start('b1),
    .core_ext_stop('b0),
    .s_axi_awaddr('h0),
    .s_axi_awlen('h0),
    .s_axi_awsize('h0),
    .s_axi_awburst('b0),
    .s_axi_awlock('b0),
    .s_axi_awcache('b0),
    .s_axi_awprot('b0),
    .s_axi_awqos('b0),
    .s_axi_awvalid('b0),
    .s_axi_awready(),
    .s_axi_wlast('b1),
    .s_axi_wdata('h0),
    .s_axi_wstrb('h0),
    .s_axi_wvalid('b0),
    .s_axi_wready(),
    .s_axi_bresp(),
    .s_axi_bvalid(),
    .s_axi_bready('b1),
    .s_axi_araddr(),
    .s_axi_arlen('h0),
    .s_axi_arsize('h0),
    .s_axi_arburst('b0),
    .s_axi_arlock('b0),
    .s_axi_arcache('b0),
    .s_axi_arprot('b0),
    .s_axi_arqos('b0),
    .s_axi_arvalid('b0),
    .s_axi_arready(),
    .s_axi_rlast(),
    .s_axi_rdata(),
    .s_axi_rresp(),
    .s_axi_rvalid(),
    .s_axi_rready('b1),
    .m_axi_arid(axi_32_arid),
    .m_axi_araddr(axi_32_araddr),
    .m_axi_arlen(axi_32_arlen),
    .m_axi_arsize(axi_32_arsize),
    .m_axi_arburst(axi_32_arburst),
    .m_axi_arlock(axi_32_arlock),
    .m_axi_arcache(axi_32_arcache),
    .m_axi_arprot(axi_32_arprot),
    .m_axi_arqos(axi_32_arqos),
    .m_axi_arvalid(axi_32_arvalid),
    .m_axi_arready(axi_32_arready),
    .m_axi_rid(axi_32_rid),
    .m_axi_rlast(axi_32_rlast),
    .m_axi_rdata(axi_32_rdata),
    .m_axi_rresp(axi_32_rresp),
    .m_axi_rvalid(axi_32_rvalid),
    .m_axi_rready(axi_32_rready)
  );
  Axi4WidthUpsizer width_converter (
    .clock(clock),
    .reset(reset),
    .io_s_axi_ar_valid(axi_32_arvalid),
    .io_s_axi_ar_ready(axi_32_arready),
    .io_s_axi_ar_bits_id(axi_32_arid),
    .io_s_axi_ar_bits_addr(axi_32_araddr),
    .io_s_axi_ar_bits_len(axi_32_arlen),
    .io_s_axi_ar_bits_size(axi_32_arsize),
    .io_s_axi_ar_bits_burst(axi_32_arburst),
    
    .io_s_axi_r_valid(axi_32_rvalid),
    .io_s_axi_r_ready(axi_32_rready),
    .io_s_axi_r_bits_id(axi_32_rid),
    .io_s_axi_r_bits_data(axi_32_rdata),
    .io_s_axi_r_bits_resp(axi_32_rresp),
    .io_s_axi_r_bits_last(axi_32_rlast),

    .io_m_axi_ar_valid(axi_64_arvalid),
    .io_m_axi_ar_ready(axi_64_arready),
    .io_m_axi_ar_bits_id(axi_64_arid),
    .io_m_axi_ar_bits_addr(axi_64_araddr),
    .io_m_axi_ar_bits_len(axi_64_arlen),
    .io_m_axi_ar_bits_size(axi_64_arsize),
    .io_m_axi_ar_bits_burst(axi_64_arburst),
    
    .io_m_axi_r_valid(axi_64_rvalid),
    .io_m_axi_r_ready(axi_64_rready),
    .io_m_axi_r_bits_id(axi_64_rid),
    .io_m_axi_r_bits_data(axi_64_rdata),
    .io_m_axi_r_bits_resp(axi_64_rresp),
    .io_m_axi_r_bits_last(axi_64_rlast)
  );

  Axi4Memory memory(
    .clock(clock),
    .reset(reset),
    .io_s_axi_aw_valid('b0),
    .io_s_axi_aw_bits_id('b0),
    .io_s_axi_aw_bits_addr('h0),
    .io_s_axi_w_valid('b0),
    .io_s_axi_w_bits_data('h0),
    .io_s_axi_w_bits_strb('h0),
    .io_s_axi_b_ready('b1),
    .io_s_axi_b_valid(),
    .io_s_axi_b_bits_id(),
    .io_s_axi_ar_valid(axi_64_arvalid),
    .io_s_axi_ar_bits_id(axi_64_arid),
    .io_s_axi_ar_bits_addr(axi_64_araddr),
    .io_s_axi_r_ready(axi_64_rready),
    .io_s_axi_r_valid(axi_64_rvalid),
    .io_s_axi_r_bits_id(),
    .io_s_axi_r_bits_data(axi_64_rdata)
  );

  initial begin
    repeat (20) @(posedge clock);
    reset = 1'b0;

    repeat (100) @(posedge clock);

    $finish;


  end
endmodule
