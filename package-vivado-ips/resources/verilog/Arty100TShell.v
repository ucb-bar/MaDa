`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 11/26/2024 10:54:24 AM
// Design Name: 
// Module Name: Arty100TShell
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

module Arty100TShell(
  input CLK100MHZ,
  input ck_rst,
  output uart_rxd_out,
  input uart_txd_in,

  output jd_0,
  input jd_1,
  input jd_2,
  output jd_3,
  input jd_4,
  input jd_5,
  input jd_6,
  input jd_7,

  input [3:0] btn,
  input [3:0] sw,

  output led0_b,
  output led1_b,
  output led2_b

  //   // Inouts
  // inout [15:0]       ddr3_dq,
  // inout [1:0]        ddr3_dqs_n,
  // inout [1:0]        ddr3_dqs_p,
  // // Outputs
  // output [13:0]     ddr3_addr,
  // output [2:0]        ddr3_ba,
  // output            ddr3_ras_n,
  // output            ddr3_cas_n,
  // output            ddr3_we_n,
  // output            ddr3_reset_n,
  // output [0:0]       ddr3_ck_p,
  // output [0:0]       ddr3_ck_n,
  // output [0:0]       ddr3_cke,
  // output [0:0]        ddr3_cs_n,
  // output [1:0]     ddr3_dm,
  // output [0:0]       ddr3_odt
);


  wire clock;
  wire pll_locked;
  wire reset;
    
  clk_wiz_0 u_clk_wiz_0 (
    .clk_in1(CLK100MHZ),
    .reset(~ck_rst),
    .locked(pll_locked),
    .clk_out1(clock)
  );
  
  
  sync_reset #(
    .N(2)
  )
  u_sync_reset (
    .clk(clock),
    .rst(~pll_locked),
    .out(reset)
  );
 


  assign led1_b = 1'b1;
  assign led2_b = 1'b0;

  wire cbus_reset;
  wire jtag_reset;
  
  sync_reset #(
    .N(2)
  )
  u_sync_debug_reset (
    .clk(jd_2),
    .rst(cbus_reset),
    .out(jtag_reset)
  );



  wire axi_aclk;
  wire axi_aresetn;

  wire [31:0] tile_axi_awaddr;
  wire tile_axi_awvalid;
  wire tile_axi_awready;
  wire [31:0] tile_axi_wdata;
  wire [3:0] tile_axi_wstrb;
  wire tile_axi_wvalid;
  wire tile_axi_wready;
  wire [1:0] tile_axi_bresp;
  wire tile_axi_bvalid;
  wire tile_axi_bready;
  wire [31:0] tile_axi_araddr;
  wire tile_axi_arvalid;
  wire tile_axi_arready;
  wire [31:0] tile_axi_rdata;
  wire [1:0] tile_axi_rresp;
  wire tile_axi_rvalid;
  wire tile_axi_rready;

  wire ddr3_axi_aresetn;


  wire        mem_axi4_0_aw_ready;
  wire        mem_axi4_0_aw_valid;
  wire [3:0]  mem_axi4_0_aw_bits_id;
  wire [31:0] mem_axi4_0_aw_bits_addr;
  wire [7:0]  mem_axi4_0_aw_bits_len;
  wire [2:0]  mem_axi4_0_aw_bits_size;
  wire [1:0]  mem_axi4_0_aw_bits_burst;
  wire        mem_axi4_0_aw_bits_lock;
  wire [3:0]  mem_axi4_0_aw_bits_cache;
  wire [2:0]  mem_axi4_0_aw_bits_prot;
  wire [3:0]  mem_axi4_0_aw_bits_qos;
  wire        mem_axi4_0_w_ready;
  wire        mem_axi4_0_w_valid;
  wire [63:0] mem_axi4_0_w_bits_data;
  wire [7:0]  mem_axi4_0_w_bits_strb;
  wire        mem_axi4_0_w_bits_last;
  wire        mem_axi4_0_b_ready;
  wire        mem_axi4_0_b_valid;
  wire  [3:0] mem_axi4_0_b_bits_id;
  wire  [1:0] mem_axi4_0_b_bits_resp;
  wire        mem_axi4_0_ar_ready;
  wire        mem_axi4_0_ar_valid;
  wire [3:0]  mem_axi4_0_ar_bits_id;
  wire [31:0] mem_axi4_0_ar_bits_addr;
  wire [7:0]  mem_axi4_0_ar_bits_len;
  wire [2:0]  mem_axi4_0_ar_bits_size;
  wire [1:0]  mem_axi4_0_ar_bits_burst;
  wire        mem_axi4_0_ar_bits_lock;
  wire [3:0]  mem_axi4_0_ar_bits_cache;
  wire [2:0]  mem_axi4_0_ar_bits_prot;
  wire [3:0]  mem_axi4_0_ar_bits_qos;
  wire        mem_axi4_0_r_ready;
  wire        mem_axi4_0_r_valid;
  wire  [3:0] mem_axi4_0_r_bits_id;
  wire  [63:0] mem_axi4_0_r_bits_data;
  wire  [1:0] mem_axi4_0_r_bits_resp;
  wire        mem_axi4_0_r_bits_last;


  DigitalTop system (
    .auto_chipyard_prcictrl_domain_reset_setter_clock_in_member_allClocks_uncore_clock (clock),
    .auto_chipyard_prcictrl_domain_reset_setter_clock_in_member_allClocks_uncore_reset (reset),
    .auto_mbus_fixedClockNode_anon_out_clock                                           (),
    .auto_cbus_fixedClockNode_anon_out_clock                                           (),
    .auto_cbus_fixedClockNode_anon_out_reset                                           (cbus_reset),
    .resetctrl_hartIsInReset_0                                                         (cbus_reset),
    .debug_clock                                                                       (clock),
    .debug_reset                                                                       (reset),
    .debug_systemjtag_jtag_TCK                                                         (jd_2),
    .debug_systemjtag_jtag_TMS                                                         (jd_5),
    .debug_systemjtag_jtag_TDI                                                         (jd_4),
    .debug_systemjtag_jtag_TDO_data                                                    (jd_0),
    .debug_systemjtag_reset                                                            (jtag_reset),
    .debug_dmactive                                                                    (),
    .debug_dmactiveAck                                                                 ('b1),
    .mem_axi4_0_aw_ready                                                               (mem_axi4_0_aw_ready),
    .mem_axi4_0_aw_valid                                                               (mem_axi4_0_aw_valid),
    .mem_axi4_0_aw_bits_id                                                             (mem_axi4_0_aw_bits_id),
    .mem_axi4_0_aw_bits_addr                                                           (mem_axi4_0_aw_bits_addr),
    .mem_axi4_0_aw_bits_len                                                            (mem_axi4_0_aw_bits_len),
    .mem_axi4_0_aw_bits_size                                                           (mem_axi4_0_aw_bits_size),
    .mem_axi4_0_aw_bits_burst                                                          (mem_axi4_0_aw_bits_burst),
    .mem_axi4_0_aw_bits_lock                                                           (mem_axi4_0_aw_bits_lock),
    .mem_axi4_0_aw_bits_cache                                                          (mem_axi4_0_aw_bits_cache),
    .mem_axi4_0_aw_bits_prot                                                           (mem_axi4_0_aw_bits_prot),
    .mem_axi4_0_aw_bits_qos                                                            (mem_axi4_0_aw_bits_qos),
    .mem_axi4_0_w_ready                                                                (mem_axi4_0_w_ready),
    .mem_axi4_0_w_valid                                                                (mem_axi4_0_w_valid),
    .mem_axi4_0_w_bits_data                                                            (mem_axi4_0_w_bits_data),
    .mem_axi4_0_w_bits_strb                                                            (mem_axi4_0_w_bits_strb),
    .mem_axi4_0_w_bits_last                                                            (mem_axi4_0_w_bits_last),
    .mem_axi4_0_b_ready                                                                (mem_axi4_0_b_ready),
    .mem_axi4_0_b_valid                                                                (mem_axi4_0_b_valid),
    .mem_axi4_0_b_bits_id                                                              (mem_axi4_0_b_bits_id),
    .mem_axi4_0_b_bits_resp                                                            (mem_axi4_0_b_bits_resp),
    .mem_axi4_0_ar_ready                                                               (mem_axi4_0_ar_ready),
    .mem_axi4_0_ar_valid                                                               (mem_axi4_0_ar_valid),
    .mem_axi4_0_ar_bits_id                                                             (mem_axi4_0_ar_bits_id),
    .mem_axi4_0_ar_bits_addr                                                           (mem_axi4_0_ar_bits_addr),
    .mem_axi4_0_ar_bits_len                                                            (mem_axi4_0_ar_bits_len),
    .mem_axi4_0_ar_bits_size                                                           (mem_axi4_0_ar_bits_size),
    .mem_axi4_0_ar_bits_burst                                                          (mem_axi4_0_ar_bits_burst),
    .mem_axi4_0_ar_bits_lock                                                           (mem_axi4_0_ar_bits_lock),
    .mem_axi4_0_ar_bits_cache                                                          (mem_axi4_0_ar_bits_cache),
    .mem_axi4_0_ar_bits_prot                                                           (mem_axi4_0_ar_bits_prot),
    .mem_axi4_0_ar_bits_qos                                                            (mem_axi4_0_ar_bits_qos),
    .mem_axi4_0_r_ready                                                                (mem_axi4_0_r_ready),
    .mem_axi4_0_r_valid                                                                (mem_axi4_0_r_valid),
    .mem_axi4_0_r_bits_id                                                              (mem_axi4_0_r_bits_id),
    .mem_axi4_0_r_bits_data                                                            (mem_axi4_0_r_bits_data),
    .mem_axi4_0_r_bits_resp                                                            (mem_axi4_0_r_bits_resp),
    .mem_axi4_0_r_bits_last                                                            (mem_axi4_0_r_bits_last),
    .custom_boot                                                                       ('b1),
    .serial_tl_0_in_ready                                                              (),
    .serial_tl_0_in_valid                                                              ('b0),
    .serial_tl_0_in_bits_phit                                                          ('b0),
    .serial_tl_0_out_ready                                                             ('b0),
    .serial_tl_0_out_valid                                                             (),
    .serial_tl_0_out_bits_phit                                                         (),
    .serial_tl_0_clock_in                                                              ('b0),
    .uart_0_txd                                                                        (uart_rxd_out),
    .uart_0_rxd                                                                        (uart_txd_in),
    .clock_tap                                                                         (),
    .periph_axi4_s_axi_aclk                                                        (axi_aclk),
    .periph_axi4_s_axi_aresetn                                                     (axi_aresetn),
    .periph_axi4_s_axi_awaddr                                                      (tile_axi_awaddr),
    .periph_axi4_s_axi_awvalid                                                     (tile_axi_awvalid),
    .periph_axi4_s_axi_awready                                                     (tile_axi_awready),
    .periph_axi4_s_axi_wdata                                                       (tile_axi_wdata),
    .periph_axi4_s_axi_wstrb                                                       (tile_axi_wstrb),
    .periph_axi4_s_axi_wvalid                                                      (tile_axi_wvalid),
    .periph_axi4_s_axi_wready                                                      (tile_axi_wready),
    .periph_axi4_s_axi_bresp                                                       (tile_axi_bresp),
    .periph_axi4_s_axi_bvalid                                                      (tile_axi_bvalid),
    .periph_axi4_s_axi_bready                                                      (tile_axi_bready),
    .periph_axi4_s_axi_araddr                                                      (tile_axi_araddr),
    .periph_axi4_s_axi_arvalid                                                     (tile_axi_arvalid),
    .periph_axi4_s_axi_arready                                                     (tile_axi_arready),
    .periph_axi4_s_axi_rdata                                                       (tile_axi_rdata),
    .periph_axi4_s_axi_rresp                                                       (tile_axi_rresp),
    .periph_axi4_s_axi_rvalid                                                      (tile_axi_rvalid),
    .periph_axi4_s_axi_rready                                                      (tile_axi_rready)	
  );


  wire [63:0] m_axi_awaddr;
  wire [5:0] m_axi_awprot;
  wire [1:0] m_axi_awvalid;
  wire [1:0] m_axi_awready;
  wire [63:0] m_axi_wdata;
  wire [7:0] m_axi_wstrb;
  wire [1:0] m_axi_wvalid;
  wire [1:0] m_axi_wready;
  wire [3:0] m_axi_bresp;
  wire [1:0] m_axi_bvalid;
  wire [1:0] m_axi_bready;
  wire [63:0] m_axi_araddr;
  wire [1:0] m_axi_arvalid;
  wire [1:0] m_axi_arready;
  wire [63:0] m_axi_rdata;
  wire [3:0] m_axi_rresp;
  wire [1:0] m_axi_rvalid;
  wire [1:0] m_axi_rready;

  axi_crossbar_0 u_axi_crossbar_0 (
    .aclk(axi_aclk),
    .aresetn(axi_aresetn),
    .s_axi_awaddr(tile_axi_awaddr),
    .s_axi_awprot('b000_000),
    .s_axi_awvalid(tile_axi_awvalid),
    .s_axi_awready(tile_axi_awready),
    .s_axi_wdata(tile_axi_wdata),
    .s_axi_wstrb(tile_axi_wstrb),
    .s_axi_wvalid(tile_axi_wvalid),
    .s_axi_wready(tile_axi_wready),
    .s_axi_bresp(tile_axi_bresp),
    .s_axi_bvalid(tile_axi_bvalid),
    .s_axi_bready(tile_axi_bready),
    .s_axi_araddr(tile_axi_araddr),
    .s_axi_arprot('b000_000),
    .s_axi_arvalid(tile_axi_arvalid),
    .s_axi_arready(tile_axi_arready),
    .s_axi_rdata(tile_axi_rdata),
    .s_axi_rresp(tile_axi_rresp),
    .s_axi_rvalid(tile_axi_rvalid),
    .s_axi_rready(tile_axi_rready),
    .m_axi_awaddr(m_axi_awaddr),
    .m_axi_awprot(m_axi_awprot),
    .m_axi_awvalid(m_axi_awvalid),
    .m_axi_awready(m_axi_awready),
    .m_axi_wdata(m_axi_wdata),
    .m_axi_wstrb(m_axi_wstrb),
    .m_axi_wvalid(m_axi_wvalid),
    .m_axi_wready(m_axi_wready),
    .m_axi_bresp(m_axi_bresp),
    .m_axi_bvalid(m_axi_bvalid),
    .m_axi_bready(m_axi_bready),
    .m_axi_araddr(m_axi_araddr),
    .m_axi_arprot(m_axi_arprot),
    .m_axi_arvalid(m_axi_arvalid),
    .m_axi_arready(m_axi_arready),
    .m_axi_rdata(m_axi_rdata),
    .m_axi_rresp(m_axi_rresp),
    .m_axi_rvalid(m_axi_rvalid),
    .m_axi_rready(m_axi_rready)
  );


  wire [31:0] gpio_axi_awaddr;
  wire gpio_axi_awvalid;
  wire gpio_axi_awready;
  wire [31:0] gpio_axi_wdata;
  wire [3:0] gpio_axi_wstrb;
  wire gpio_axi_wvalid;
  wire gpio_axi_wready;
  wire [1:0] gpio_axi_bresp;
  wire gpio_axi_bvalid;
  wire gpio_axi_bready;
  wire [31:0] gpio_axi_araddr;
  wire gpio_axi_arvalid;
  wire gpio_axi_arready;
  wire [31:0] gpio_axi_rdata;
  wire [1:0] gpio_axi_rresp;
  wire gpio_axi_rvalid;
  wire gpio_axi_rready;

  wire [31:0] uart_axi_awaddr;
  wire uart_axi_awvalid;
  wire uart_axi_awready;
  wire [31:0] uart_axi_wdata;
  wire [3:0] uart_axi_wstrb;
  wire uart_axi_wvalid;
  wire uart_axi_wready;
  wire [1:0] uart_axi_bresp;
  wire uart_axi_bvalid;
  wire uart_axi_bready;
  wire [31:0] uart_axi_araddr;
  wire uart_axi_arvalid;
  wire uart_axi_arready;
  wire [31:0] uart_axi_rdata;
  wire [1:0] uart_axi_rresp;
  wire uart_axi_rvalid;
  wire uart_axi_rready;


  assign gpio_axi_awaddr = m_axi_awaddr[31:0];
  assign gpio_axi_awvalid = m_axi_awvalid[0];
  assign m_axi_awready[0] = gpio_axi_awready;
  
  assign gpio_axi_wdata = m_axi_wdata[31:0];
  assign gpio_axi_wstrb = m_axi_wstrb[3:0];
  assign gpio_axi_wvalid = m_axi_wvalid[0];
  assign m_axi_wready[0] = gpio_axi_wready;

  assign m_axi_bresp[1:0] = gpio_axi_bresp;
  assign m_axi_bvalid[0] = gpio_axi_bvalid;
  assign gpio_axi_bready = m_axi_bready[0];

  assign gpio_axi_araddr = m_axi_araddr[31:0];
  assign gpio_axi_arvalid = m_axi_arvalid[0];
  assign m_axi_arready[0] = gpio_axi_arready;
  
  assign m_axi_rdata[31:0] = gpio_axi_rdata;
  assign m_axi_rresp[1:0] = gpio_axi_rresp;
  assign m_axi_rvalid[0] = gpio_axi_rvalid;
  assign gpio_axi_rready = m_axi_rready[0];


  assign uart_axi_awaddr = m_axi_awaddr[63:32];
  assign uart_axi_awvalid = m_axi_awvalid[1];
  assign m_axi_awready[1] = uart_axi_awready;

  assign uart_axi_wdata = m_axi_wdata[63:32];
  assign uart_axi_wstrb = m_axi_wstrb[7:4];
  assign uart_axi_wvalid = m_axi_wvalid[1];
  assign m_axi_wready[1] = uart_axi_wready;

  assign m_axi_bresp[3:2] = uart_axi_bresp;
  assign m_axi_bvalid[1] = uart_axi_bvalid;
  assign uart_axi_bready = m_axi_bready[1];

  assign uart_axi_araddr = m_axi_araddr[63:32];
  assign uart_axi_arvalid = m_axi_arvalid[1];
  assign m_axi_arready[1] = uart_axi_arready;
  
  assign m_axi_rdata[63:32] = uart_axi_rdata;
  assign m_axi_rresp[3:2] = uart_axi_rresp;
  assign m_axi_rvalid[1] = uart_axi_rvalid;
  assign uart_axi_rready = m_axi_rready[1];

  wire [31:0] gpio_input;
  wire [31:0] gpio_output;
  assign gpio_input = {'h0, btn[3], btn[2], btn[1], btn[0], sw[3], sw[2], sw[1], sw[0]};
  assign led0_b = gpio_output[0];
  axi_gpio_0 u_axi_gpio_0 (
    .s_axi_aclk(axi_aclk),
    .s_axi_aresetn(axi_aresetn),
    .s_axi_awaddr(gpio_axi_awaddr),
    .s_axi_awvalid(gpio_axi_awvalid),
    .s_axi_awready(gpio_axi_awready),
    .s_axi_wdata(gpio_axi_wdata),
    .s_axi_wstrb(gpio_axi_wstrb),
    .s_axi_wvalid(gpio_axi_wvalid),
    .s_axi_wready(gpio_axi_wready),
    .s_axi_bresp(gpio_axi_bresp),
    .s_axi_bvalid(gpio_axi_bvalid),
    .s_axi_bready(gpio_axi_bready),
    .s_axi_araddr(gpio_axi_araddr),
    .s_axi_arvalid(gpio_axi_arvalid),
    .s_axi_arready(gpio_axi_arready),
    .s_axi_rdata(gpio_axi_rdata),
    .s_axi_rresp(gpio_axi_rresp),
    .s_axi_rvalid(gpio_axi_rvalid),
    .s_axi_rready(gpio_axi_rready),
    .gpio_io_i(gpio_input),
    .gpio_io_o(gpio_output),
    .gpio_io_t()
  );


  axi_uartlite_0 u_axi_uartlite_0 (
    .s_axi_aclk(axi_aclk),
    .s_axi_aresetn(axi_aresetn),
    .interrupt(),
    .s_axi_awaddr(uart_axi_awaddr),
    .s_axi_awvalid(uart_axi_awvalid),
    .s_axi_awready(uart_axi_awready),
    .s_axi_wdata(uart_axi_wdata),
    .s_axi_wstrb(uart_axi_wstrb),
    .s_axi_wvalid(uart_axi_wvalid),
    .s_axi_wready(uart_axi_wready),
    .s_axi_bresp(uart_axi_bresp),
    .s_axi_bvalid(uart_axi_bvalid),
    .s_axi_bready(uart_axi_bready),
    .s_axi_araddr(uart_axi_araddr),
    .s_axi_arvalid(uart_axi_arvalid),
    .s_axi_arready(uart_axi_arready),
    .s_axi_rdata(uart_axi_rdata),
    .s_axi_rresp(uart_axi_rresp),
    .s_axi_rvalid(uart_axi_rvalid),
    .s_axi_rready(uart_axi_rready),
    .rx(jd_7),
    .tx(jd_3)
  );


  // mig_7series_0 u_mig_7series_0 (
  //   .ddr3_dq(ddr3_dq),
  //   .ddr3_dqs_n(ddr3_dqs_n),
  //   .ddr3_dqs_p(ddr3_dqs_p),
  //   .ddr3_addr(ddr3_addr),
  //   .ddr3_ba(ddr3_ba),
  //   .ddr3_ras_n(ddr3_ras_n),
  //   .ddr3_cas_n(ddr3_cas_n),
  //   .ddr3_we_n(ddr3_we_n),
  //   .ddr3_reset_n(ddr3_reset_n),
  //   .ddr3_ck_p(ddr3_ck_p),
  //   .ddr3_ck_n(ddr3_ck_n),
  //   .ddr3_cke(ddr3_cke),
  //   .ddr3_cs_n(ddr3_cs_n),
  //   .ddr3_dm(ddr3_dm),
  //   .ddr3_odt(ddr3_odt),


  //   .sys_clk_i(axi_aclk),
  //   .sys_rst(reset),
  //   .clk_ref_i(clock),
  //   .ui_clk(),
  //   .ui_clk_sync_rst(),
  //   .mmcm_locked(),

  //   .aresetn(axi_aresetn),
  //   .app_sr_req('b0),
  //   .app_ref_req('b0),
  //   .app_zq_req('b0),
  //   .app_sr_active(),
  //   .app_ref_ack(),
  //   .app_zq_ack(),

  //   // Slave Interface Write Address Ports
  //   .s_axi_awid(mem_axi4_0_aw_bits_id),
  //   .s_axi_awaddr(mem_axi4_0_aw_bits_addr),
  //   .s_axi_awlen(mem_axi4_0_aw_bits_len),
  //   .s_axi_awsize(mem_axi4_0_aw_bits_size),
  //   .s_axi_awburst(mem_axi4_0_aw_bits_burst),
  //   .s_axi_awlock(mem_axi4_0_aw_bits_lock),
  //   .s_axi_awcache(mem_axi4_0_aw_bits_cache),
  //   .s_axi_awprot(),
  //   .s_axi_awqos(),
  //   .s_axi_awvalid(mem_axi4_0_aw_valid),
  //   .s_axi_awready(mem_axi4_0_aw_ready),

  //   // Slave Interface Write Data Ports
  //   .s_axi_wdata(mem_axi4_0_w_bits_data),
  //   .s_axi_wstrb(mem_axi4_0_w_bits_strb),
  //   .s_axi_wlast(mem_axi4_0_w_bits_last),
  //   .s_axi_wvalid(mem_axi4_0_w_valid),
  //   .s_axi_wready(mem_axi4_0_w_ready),

  //   // Slave Interface Write Response Ports
  //   .s_axi_bid(mem_axi4_0_b_bits_id),
  //   .s_axi_bresp(mem_axi4_0_b_bits_resp),
  //   .s_axi_bvalid(mem_axi4_0_b_valid),
  //   .s_axi_bready(mem_axi4_0_b_ready),

  //   // Slave Interface Read Address Ports
  //   .s_axi_arid(mem_axi4_0_ar_bits_id),
  //   .s_axi_araddr(mem_axi4_0_ar_bits_addr),
  //   .s_axi_arlen(mem_axi4_0_ar_bits_len),
  //   .s_axi_arsize(mem_axi4_0_ar_bits_size),
  //   .s_axi_arburst(mem_axi4_0_ar_bits_burst),
  //   .s_axi_arlock(mem_axi4_0_ar_bits_lock),
  //   .s_axi_arcache(mem_axi4_0_ar_bits_cache),
  //   .s_axi_arprot(mem_axi4_0_ar_bits_prot),
  //   .s_axi_arqos(mem_axi4_0_ar_bits_qos),
  //   .s_axi_arvalid(mem_axi4_0_ar_valid),
  //   .s_axi_arready(mem_axi4_0_ar_ready),

  //   // Slave Interface Read Data Ports
  //   .s_axi_rdata(mem_axi4_0_r_bits_data),
  //   .s_axi_rresp(mem_axi4_0_r_bits_resp),
  //   .s_axi_rlast(mem_axi4_0_r_bits_last),
  //   .s_axi_rvalid(mem_axi4_0_r_valid),
  //   .s_axi_rready(mem_axi4_0_r_ready),
  //   .s_axi_rid(mem_axi4_0_r_bits_id),

  //   .init_calib_complete(),
  //   .device_temp()
  // );


endmodule
