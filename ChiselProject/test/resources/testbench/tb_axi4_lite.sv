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


module TB_AXI4_Lite();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;

  // setup clock and reset
  reg clk, rst;
  initial clk = 'b0;
  always #(CLOCK_PERIOD/2) clk = ~clk;


  logic [8:0] awaddr;
  logic awvalid;
  logic awready;
  logic [31:0] wdata;
  logic [3:0] wstrb;
  logic wvalid;
  logic wready;
  logic [1:0] bresp;
  logic bvalid;
  logic bready;
  logic [8:0] araddr;
  logic arvalid;
  logic arready;
  logic [31:0] rdata;
  logic [1:0] rresp;
  logic rvalid;
  logic rready;
  
  logic [31:0] gpio_i;
  
 
  axi_gpio_0 u_axi_gpio_0 (
    .s_axi_aclk(clk),
    .s_axi_aresetn(~rst),
    .s_axi_awaddr(awaddr),
    .s_axi_awvalid(awvalid),
    .s_axi_awready(awready),
    .s_axi_wdata(wdata),
    .s_axi_wstrb(wstrb),
    .s_axi_wvalid(wvalid),
    .s_axi_wready(wready),
    .s_axi_bresp(bresp),
    .s_axi_bvalid(bvalid),
    .s_axi_bready(bready),
    .s_axi_araddr(araddr),
    .s_axi_arvalid(arvalid),
    .s_axi_arready(arready),
    .s_axi_rdata(rdata),
    .s_axi_rresp(rresp),
    .s_axi_rvalid(rvalid),
    .s_axi_rready(rready),
    .gpio_io_i(gpio_i),
    .gpio_io_o(),
    .gpio_io_t()
  );
  
  logic done;
  logic status;
  
  axi_traffic_gen_0 u_axi_traffic_gen_0 (
      .s_axi_aclk(clk),
      .s_axi_aresetn(~rst),
      .m_axi_lite_ch1_awaddr(awaddr),
      .m_axi_lite_ch1_awprot(),
      .m_axi_lite_ch1_awvalid(awvalid),
      .m_axi_lite_ch1_awready(awready),
      .m_axi_lite_ch1_wdata(wdata),
      .m_axi_lite_ch1_wstrb(wstrb),
      .m_axi_lite_ch1_wvalid(wvalid),
      .m_axi_lite_ch1_wready(wready),
      .m_axi_lite_ch1_bresp(bresp),
      .m_axi_lite_ch1_bvalid(bvalid),
      .m_axi_lite_ch1_bready(bready),
      .done(done),
      .status(status)
    );


  initial begin
    rst = 1'b1;
    
    gpio_i = 'b0101;
    
//    awaddr = 'h00;
//    awvalid = 'b0;
//    wdata = 'h00;
//    wstrb = 'b1111;
//    wvalid = 'b0;
//    bready = 'b1;
//    araddr = 'h00;
//    arvalid = 'b0;
//    rready = 'b1;
    
    repeat (5) @(posedge clk);
    rst = 1'b0;
    
//    araddr = 'h00;
//    arvalid = 'b1;
    
//    wait (arready == 1);
    repeat (1) @(posedge clk);
//    arvalid = 'b0;
    repeat (1) @(posedge clk);
//    rready = 'b0;
    
    

    repeat (100) @(posedge clk);
    $finish;
  end
endmodule
