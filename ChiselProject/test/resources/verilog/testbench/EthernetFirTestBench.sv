`timescale 1ns / 1ps


module EthernetFirTestBench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;
  
  // setup clock and reset
  reg clock, reset;
  initial clock = 'b0;
  always #(CLOCK_PERIOD/2) clock = ~clock;


  logic [7:0] input_sequence[10] = {+8'd127, +8'd64, -8'd0, -8'd0, -8'd0, -8'd0, -8'd0, -8'd0, -8'd0, -8'd0};
  logic [17:0] expected_output[10] = {+18'd127, +18'd445, +18'd2097, +18'd10358, -18'd4662, -18'd6641, -18'd1341, -18'd319, -18'd64, -18'd0};

  logic passed = 1'b1;


  logic [7:0] in;
  logic [17:0] out;

  logic [7:0] w0;
  logic [7:0] w1;
  logic [7:0] w2;
  logic [7:0] w3;
  logic [7:0] w4;
  logic [7:0] w5;
  logic [7:0] w6;
  logic [7:0] w7;

  
  EthernetFir dut (
    .clock(clock),
    .io_in(in),
    .io_weights_0(w0),
    .io_weights_1(w1),
    .io_weights_2(w2),
    .io_weights_3(w3),
    .io_weights_4(w4),
    .io_weights_5(w5),
    .io_weights_6(w6),
    .io_weights_7(w7),
    .io_out(out)
  );

  initial begin
    reset = 1'b1;

    w0 = +8'd1;
    w1 = +8'd3;
    w2 = +8'd15;
    w3 = +8'd74;
    w4 = -8'd74;
    w5 = -8'd15;
    w6 = -8'd3;
    w7 = -8'd1;

    in = +18'd0;

    repeat (10) @(posedge clock);
    
    reset = 1'b0;
    @(posedge clock);

    for (int i = 0; i < 10; i++) begin
      in = input_sequence[i];
      @(posedge clock);
      if (out != expected_output[i]) begin
        $display("ERROR at time %t: out = %d, expected = %d", $time, out, expected_output[i]);
        passed = 1'b0;
      end
    end


    repeat (20) @(posedge clock);

    if (passed) begin
      $display("TEST PASSED");
    end else begin
      $display("TEST FAILED");
    end

    $finish;
  end
endmodule

