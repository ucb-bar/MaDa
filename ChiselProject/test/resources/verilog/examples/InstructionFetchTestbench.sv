`timescale 1ns / 1ps

`include "AxiInterface.vh"


module InstructionFetchTestbench();
  logic clock, reset;
  initial clock = 'b0;
  initial reset = 'b1;
  always #5 clock = ~clock;

  axi_interface #(32, 32, 4) axi();

  reg [31:0] reset_vector = 32'h0800_0000;

  wire [31:0] ex_pc;
  wire [31:0] ex_inst;
  wire ex_valid;
  reg ex_ready;

  reg ex_redirected = 0;
  reg [31:0] ex_redirected_pc = 'h0800_0100;


  Axi4MemoryForTest mem(
    .clock(clock),
    .reset(reset),
    .io_s_axi_aw_ready(axi.awready),
    .io_s_axi_aw_valid(axi.awvalid),
    .io_s_axi_aw_bits_addr(axi.awaddr),
    .io_s_axi_aw_bits_len('b0),
    .io_s_axi_aw_bits_size('b0),
    .io_s_axi_aw_bits_burst('b0),
    .io_s_axi_w_ready(axi.wready),
    .io_s_axi_w_valid(axi.wvalid),
    .io_s_axi_w_bits_data(axi.wdata),
    .io_s_axi_w_bits_strb(axi.wstrb),
    .io_s_axi_w_bits_last('b1),
    .io_s_axi_b_ready(axi.bready),
    .io_s_axi_b_valid(axi.bvalid),
    .io_s_axi_b_bits_resp(axi.bresp),
    .io_s_axi_ar_ready(axi.arready),
    .io_s_axi_ar_valid(axi.arvalid),
    .io_s_axi_ar_bits_addr(axi.araddr),
    .io_s_axi_ar_bits_len('b0),
    .io_s_axi_ar_bits_size('b0),
    .io_s_axi_ar_bits_burst('b0),
    .io_s_axi_r_ready(axi.rready),
    .io_s_axi_r_valid(axi.rvalid),
    .io_s_axi_r_bits_data(axi.rdata),
    .io_s_axi_r_bits_resp(axi.rresp),
    .io_s_axi_r_bits_last()
  );

  InstructionFetch frontend(
    .clock(clock),
    .reset(reset),
    .io_reset_vector(reset_vector),
    .io_ex_ready(ex_ready),
    .io_ex_valid(ex_valid),
    .io_ex_bits_pc(ex_pc),
    .io_ex_bits_inst(ex_inst),
    .io_redirected(ex_redirected),
    .io_redirected_pc(ex_redirected_pc),
    .io_imem_aw_ready(axi.awready),
    .io_imem_aw_valid(axi.awvalid),
    .io_imem_aw_bits_addr(axi.awaddr),
    .io_imem_w_ready(axi.wready),
    .io_imem_w_valid(axi.wvalid),
    .io_imem_w_bits_data(axi.wdata),
    .io_imem_w_bits_strb(axi.wstrb),
    .io_imem_b_ready(axi.bready),
    .io_imem_b_valid(axi.bvalid),
    .io_imem_b_bits_resp(axi.bresp),
    .io_imem_ar_ready(axi.arready),
    .io_imem_ar_valid(axi.arvalid),
    .io_imem_ar_bits_addr(axi.araddr),
    .io_imem_r_ready(axi.rready),
    .io_imem_r_valid(axi.rvalid),
    .io_imem_r_bits_data(axi.rvalid ? axi.rdata : 'hZZ),
    .io_imem_r_bits_resp(axi.rresp)
  );

  // always @(posedge clock) begin
  //   if (reset) begin
  //     axi.rvalid <= 'b0;
  //     pipe_valid <= 'b0;
  //   end
  //   else begin
  //     // 0 delay
  //     axi.rvalid <= axi.arvalid && axi.arready;

  //     // 1 cycle delay
  //     // pipe_valid <= axi.arvalid && axi.arready;
  //     // axi.rvalid <= pipe_valid;

  //   end
  // end
  
  initial begin
    // axi.awready = 'b1;
    // axi.wready  = 'b1;
    // axi.bvalid  = 'b0;
    // axi.bresp   = 'h0;
    // axi.bid     = 'h0;
    // axi.arready = 'b0;
    // axi.rvalid  = 'b0;
    // axi.rresp   = 'h0;
    // axi.rlast   = 'b1;
    // axi.rid     = 'h0;

    ex_ready = 'b1;

    reset = 1;
    repeat (10) @(posedge clock);
    reset = 0;

    // axi.arready = 'b1;
    // @(posedge clock); #0;
    // @(posedge clock); #0;
    // @(posedge clock); #0;

    // // memory halt for 2 cycles
    // axi.arready = 'b0;
    // @(posedge clock); #0;
    // @(posedge clock); #0;

    // axi.arready = 'b1;
    // @(posedge clock); #0;
    // @(posedge clock); #0;
    // @(posedge clock); #0;

    // backend not ready for 3 cycles
    ex_ready = 'b0;
    @(posedge clock); #0;
    @(posedge clock); #0;
    @(posedge clock); #0;

    ex_ready = 'b1;
    @(posedge clock); #0;
    @(posedge clock); #0;
    @(posedge clock); #0;

    // redirect to 0x0800_0100
    ex_redirected = 'b1;
    ex_redirected_pc = 'h0800_0100;
    @(posedge clock); #0;

    ex_redirected = 'b0;
    @(posedge clock); #0;
    @(posedge clock); #0;
    @(posedge clock); #0;




    
    repeat (100) @(posedge clock);
    $finish;
  end
endmodule
