`timescale 1ns / 1ps


module TestDriver();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;
  
  // setup clock and reset
  reg clock, reset;
  initial clock = 'b0;
  always #(CLOCK_PERIOD/2) clock = ~clock;

  logic success;

  SimRamTestbench dut(
    .clock(clock),
    .reset(reset),
    .success(success)
  );

  initial begin
    reset = 1'b1;
    repeat (10) @(posedge clock);
    reset = 1'b0;

    repeat (10000) @(posedge clock);
    $finish;
  end

endmodule
