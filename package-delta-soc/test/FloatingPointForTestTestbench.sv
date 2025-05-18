`timescale 1ns / 1ps

module FloatingPointForTestTestbench();
  logic clock, reset;
  initial clock = 'b0;
  initial reset = 'b1;
  always #5 clock = ~clock;

  logic io_a_valid;
  logic [31:0] io_a_bits;
  logic io_b_valid;
  logic [31:0] io_b_bits;
  logic io_c_valid;
  logic [31:0] io_c_bits;
  logic io_result_valid;
  logic [31:0] io_result_bits;

  FloatingPointForTest dut(
    .clock(clock),
    .reset(reset),
    .io_a_valid(io_a_valid),
    .io_a_bits(io_a_bits),
    .io_b_valid(io_b_valid),
    .io_b_bits(io_b_bits),
    .io_c_valid(io_c_valid),
    .io_c_bits(io_c_bits),
    .io_result_valid(io_result_valid),
    .io_result_bits(io_result_bits)
  );
  
  initial begin
    io_a_valid = 0;
    io_a_bits = 0;
    io_b_valid = 0;
    io_b_bits = 0;
    io_c_valid = 0;
    io_c_bits = 0;

    #10 reset = 0;

    #10 io_a_valid = 1;
    io_a_bits = 32'h3f800000;

    repeat (10) @(posedge clock); #0;

    io_a_valid = 0;
    io_b_valid = 1;
    io_b_bits = 32'h3f800000;
    
    
  end
endmodule
