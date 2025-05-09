`timescale 1ns / 1ns


`define RF_PATH dut.core.regfile_ext.Memory
`define RF_DEPTH 32

`define IMEM_PATH dut.itim.mem.mem.mem

`define DMEM_PATH dut.dtim.mem.mem
`define DMEM_DEPTH 4096

`define TIMEOUT_CYCLES 100




module EECS151TileISATestbench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;

  // setup clock and reset
  reg clock, reset;
  initial clock = 0;
  always #(CLOCK_PERIOD/2) clock = ~clock;

  // Init PC with 32'h1000_0000 -- address space of IMem
  wire [31:0] reset_vector = 32'h1000_0000;
  wire [31:0] csr_tohost;

  EECS151Tile dut (
    .clock(clock),
    .reset(reset),
    .io_reset_vector(reset_vector),
    .io_debug_syscall0(csr_tohost)
  );

  reg [31:0] cycle;
  always @(posedge clk) begin
    if (rst === 1)
      cycle <= 0;
    else
      cycle <= cycle + 1;
  end



  string hex_file;
  string test_name;
  initial begin
    if (!$value$plusargs("hex_file=%s", hex_file)) begin
      $display("Must supply hex_file!");
      $fatal();
    end

    if (!$value$plusargs("test_name=%s", test_name)) begin
      $display("Must supply test_name!");
      $fatal();
    end

    $readmemh(hex_file, `IMEM_PATH.mem, 0, 16384-1);
    $readmemh(hex_file, `DMEM_PATH.mem, 0, 16384-1);

    $dumpfile({test_name, ".fst"});
    $dumpvars(0, isa_tb);


    // Reset the CPU
    reset = 1;
    repeat (30) @(posedge clock); // Hold reset for 30 cycles

    @(negedge clock);
    reset = 0;

    // Wait until csr[0] is asserted
    while (csr_tohost[0] !== 1'b1)
      @(posedge clock);

    if (csr_tohost[0] === 1'b1 && csr_tohost[31:1] === 31'b0) begin
      $display("[passed] - %s in %d simulation cycles", test_name, cycle);
    end else begin
      $display("[failed] - %s. Failed test: %d", test_name, csr_tohost[31:1]);
    end
    $finish();
  end

  initial begin
    repeat (`TIMEOUT_CYCLES) @(posedge clock);
    $display("Timeout!");
    $finish();
  end

endmodule