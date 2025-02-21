`timescale 1ns / 1ns


`define RF_PATH dut.core.regfile_ext.Memory
`define RF_DEPTH 32

`define VRF_PATH dut.core.vregfile_0_ext.Memory
`define VRF_DEPTH 32

`define IMEM_PATH dut.itim.mem.mem

`define DMEM_PATH dut.dtim.mem.mem
`define DMEM_DEPTH 4096




// ***** Opcodes *****
// Special immediate instructions
`define OPC_LUI         7'b0110111
`define OPC_AUIPC       7'b0010111

// Jump instructions
`define OPC_JAL         7'b1101111
`define OPC_JALR        7'b1100111

// Branch instructions
`define OPC_BRANCH      7'b1100011

// Load and store instructions
`define OPC_STORE       7'b0100011
`define OPC_LOAD        7'b0000011

// Arithmetic instructions
`define OPC_ARI   7'b0110011
`define OPC_ARI_IMM   7'b0010011

// CSR instructions
`define OPC_CSR 7'b1110011

// Vector instructions
`define OPC_VEC_ARI      7'b1010111

`define OPC_VEC_VL      7'b0000111
`define OPC_VEC_VS      7'b0100111


// ***** Function codes *****


// ***** Function 3 codes *****
// Branch function codes
`define FNC3_BEQ         3'b000
`define FNC3_BNE         3'b001
`define FNC3_BLT         3'b100
`define FNC3_BGE         3'b101
`define FNC3_BLTU        3'b110
`define FNC3_BGEU        3'b111

// Load and store function codes
`define FNC3_LB          3'b000
`define FNC3_LH          3'b001
`define FNC3_LW          3'b010
`define FNC3_LBU         3'b100
`define FNC3_LHU         3'b101
`define FNC3_SB          3'b000
`define FNC3_SH          3'b001
`define FNC3_SW          3'b010

// Vector function codes
`define FNC3_FVV         3'b001
`define FNC3_FVF         3'b101

`define FNC3_F16         3'b001
`define FNC3_F32         3'b010
`define FNC3_F64         3'b011
`define FNC3_V8          3'b000
`define FNC3_V16         3'b101
`define FNC3_V32         3'b110
`define FNC3_V64         3'b111


// ***** Function 7 codes *****
// vector, lowest 1 bit is mop
`define FNC6_VADD        6'b000000  // vd[i] = vs1[i] + vs2[i]
`define FNC6_VFMIN       6'b000100  // vd[i] = min(vs1[i], vs2[i])
`define FNC6_VFMAX       6'b000110  // vd[i] = max(vs1[i], vs2[i])
`define FNC6_VFMUL       6'b100100  // vd[i] = vs1[i] * vs2[i]
`define FNC6_VFMADD      6'b101000  // vd[i] = +(vs1[i] * vd[i]) + vs2[i]
`define FNC6_VFMACC      6'b101100  // vd[i] = +(vs1[i] * vs2[i]) + vd[i]



module EECS252TestBench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;

  // setup clock and reset
  reg clock, reset;
  initial clock = 0;
  always #(CLOCK_PERIOD/2) clock = ~clock;

  int timeout_cycle = 20;

  // Init PC with 32'h1000_0000 -- address space of IMem
  wire [31:0] reset_vector = 32'h1000_0000;
  wire [31:0] csr_tohost;

  Tile dut (
    .clock(clock),
    .reset(reset),
    .io_reset_vector(reset_vector),
    .io_sbus_aw_ready('b0),
    .io_sbus_w_ready('b0),
    .io_sbus_b_valid('b0),
    .io_sbus_b_bits_resp('h0),
    .io_sbus_ar_ready('b0),
    .io_sbus_r_valid('b0),
    .io_sbus_r_bits_resp('h0),
    .io_debug_x1(),
    .io_debug_x2(),
    .io_debug_x3(),
    .io_debug_x4(),
    .io_debug_x5(),
    .io_debug_x6(),
    .io_debug_x7(),
    .io_debug_x8(),
    .io_debug_tohost(csr_tohost)
  );


  // Reset IMem, DMem, and RegFile before running new test
  task reset_system;
    integer i;
    begin
      for (i = 0; i < `RF_DEPTH; i = i + 1) begin
        `RF_PATH[i] = 0;
      end
      for (i = 0; i < `VRF_DEPTH; i = i + 1) begin
        `VRF_PATH[i] = 0;
      end
      for (i = 0; i < `DMEM_DEPTH; i = i + 1) begin
        `DMEM_PATH[i] = 0;
      end

      @(negedge clock);
      reset = 1;
      @(negedge clock);
      reset = 0;
    end
  endtask

  task init_rf;
    integer i;
    begin
      for (i = 1; i < `RF_DEPTH; i = i + 1) begin
        `RF_PATH[i] = 100 * i + 1;
      end
      
      `VRF_PATH[1] = 'h3f800000;  // 1
      `VRF_PATH[2] = 'h40000000;  // 2
      `VRF_PATH[3] = 'h40400000;  // 3
      `VRF_PATH[4] = 'h40800000;  // 4
      `VRF_PATH[5] = 'h40A00000;  // 5
      `VRF_PATH[6] = 'h40C00000;  // 6
      `VRF_PATH[7] = 'h40E00000;  // 7
      `VRF_PATH[8] = 'h41000000;  // 8
      `VRF_PATH[9] = 'h41100000;  // 9
      `VRF_PATH[10] = 'h41200000;  // 10
      `VRF_PATH[11] = 'h41300000;  // 11
      `VRF_PATH[12] = 'h41400000;  // 12
      `VRF_PATH[13] = 'h41500000;  // 13
      `VRF_PATH[14] = 'h41600000;  // 14
      `VRF_PATH[15] = 'h41700000;  // 15
      `VRF_PATH[16] = 'h41800000;  // 16
      `VRF_PATH[17] = 'h41880000;  // 17
      `VRF_PATH[18] = 'h41900000;  // 18
      `VRF_PATH[19] = 'h41980000;  // 19
      `VRF_PATH[20] = 'h41A00000;  // 20
      `VRF_PATH[21] = 'h41A80000;  // 21
      `VRF_PATH[22] = 'h41B00000;  // 22
      `VRF_PATH[23] = 'h41B80000;  // 23
      `VRF_PATH[24] = 'h41C00000;  // 24
      `VRF_PATH[25] = 'h41C80000;  // 25
      `VRF_PATH[26] = 'h41D00000;  // 26
      `VRF_PATH[27] = 'h41D80000;  // 27
      `VRF_PATH[28] = 'h41E00000;  // 28
      `VRF_PATH[29] = 'h41E80000;  // 29
      `VRF_PATH[30] = 'h41F00000;  // 30
      `VRF_PATH[31] = 'h41F80000;  // 31
    end
  endtask

  int cycle;
  bit done;
  int current_test_id = 0;
  string current_test_type;
  int current_output;
  int current_result;
  bit all_tests_passed = 0;


  // Check for timeout
  // If a test does not return correct value in a given timeout cycle,
  // we terminate the testbench
  initial begin
    while (all_tests_passed === 0) begin
      @(posedge clock);
      if (cycle === timeout_cycle) begin
        $display("[Failed] Timeout at [%d] test %s, expected_result = %h, got = %h",
                current_test_id, current_test_type, current_result, current_output);
        $finish();
      end
    end
  end

  always @(posedge clock) begin
    if (done === 0)
      cycle <= cycle + 1;
    else
      cycle <= 0;
  end

  // Check result of RegFile
  // If the write_back (destination) register has correct value (matches "result"), test passed
  // This is used to test instructions that update RegFile
  task check_result_rf;
    input [31:0]  rf_wa;
    input [31:0]  result;
    input string test_type;
    begin
      done = 0;
      current_test_id   = current_test_id + 1;
      current_test_type = test_type;
      current_result    = result;
      while (`RF_PATH[rf_wa] !== result) begin
        current_output = `RF_PATH[rf_wa];
        @(posedge clock);
      end
      done = 1;
      $display("[%d] Test %s passed!", current_test_id, test_type);
    end
  endtask

  // Check result of Vector RegFile
  // If the write_back (destination) register has correct value (matches "result"), test passed
  // This is used to test instructions that update Vector RegFile
  task check_result_vrf;
    input [31:0]  vrf_wa;
    input [31:0]  result;
    input string test_type;
    begin
      done = 0;
      current_test_id   = current_test_id + 1;
      current_test_type = test_type;
      current_result    = result;
      while (`VRF_PATH[vrf_wa] !== result) begin
        current_output = `VRF_PATH[vrf_wa];
        @(posedge clock);
      end
      done = 1;
      $display("[%d] Test %s passed!", current_test_id, test_type);
    end
  endtask

  // Check result of DMem
  // If the memory location of DMem has correct value (matches "result"), test passed
  // This is used to test store instructions
  task check_result_dmem;
    input [31:0]  addr;
    input [31:0]  result;
    input string test_type;
    begin
      done = 0;
      current_test_id   = current_test_id + 1;
      current_test_type = test_type;
      current_result    = result;
      while (`DMEM_PATH[addr] !== result) begin
        current_output = `DMEM_PATH[addr];
        @(posedge clock);
      end
      done = 1;
      $display("[%d] Test %s passed!", current_test_id, test_type);
    end
  endtask

  reg [4:0]  RD, RS1, RS2;
  reg [31:0] RD1, RD2;
  reg [14:0] INST_ADDR;
  reg [14:0] DATA_ADDR;

  reg [31:0] JUMP_ADDR;

  initial begin
    $dumpfile("EECS252_testbench.vcd");
    $dumpvars;

    #0;
    reset = 0;

    // Reset the CPU
    reset = 1;
    // Hold reset for a while
    repeat (10) @(posedge clock);

    @(negedge clock);
    reset = 0;

    // Test Vector-Vector Insts --------------------------------------------------
    // - VADD, VFMUL, VFMACC
    reset_system();

    // We can also use $random to generate random values for testing
    RS1 = 1; RD1 = 'hC2C80000;
    RS2 = 2; RD2 = 'h43480000;
    RD  = 3;
    `VRF_PATH[RS1] = RD1;
    `VRF_PATH[RS2] = RD2;
    INST_ADDR       = 14'h0000;

    `VRF_PATH[5] = 'h3F800000;

    `IMEM_PATH[INST_ADDR + 0]  = {`FNC6_VADD,   1'b0, RS2, RS1, `FNC3_FVV, 5'd3,  `OPC_VEC_ARI};
    `IMEM_PATH[INST_ADDR + 1]  = {`FNC6_VFMUL,  1'b0, RS2, RS1, `FNC3_FVV, 5'd4,  `OPC_VEC_ARI};
    `IMEM_PATH[INST_ADDR + 2]  = {`FNC6_VFMACC, 1'b0, RS2, RS1, `FNC3_FVV, 5'd5,  `OPC_VEC_ARI};

    check_result_vrf(5'd3,  32'h42C80000, "Vector FP ADD");
    check_result_vrf(5'd4,  32'hC69C4000, "Vector FP MUL");
    check_result_vrf(5'd5,  32'hC69C3E00, "Vector FP MACC");


    // Test Vector Load Insts --------------------------------------------------
    // - VLOAD
    reset_system();
    
    `RF_PATH[1] = 32'h0800_0100;
    INST_ADDR       = 14'h0000;
    DATA_ADDR       = (`RF_PATH[1] + 32'h00000000) >> 2;

    // nf = 0 (single value field), mew = 0 (no extended memory element width)
    // mop = 0 (unit stride), vm = 1 (mask disabled), lumop = 0 (unit-stride load), width = v32b
    `IMEM_PATH[INST_ADDR + 0]  = {3'b000, 1'b0, 2'b00, 1'b1, 5'b0, 5'd1, `FNC3_V32, 5'd2,  `OPC_VEC_VL};

    `DMEM_PATH[DATA_ADDR] = 32'hdeadbeef;

    check_result_vrf(5'd2,  32'hdeadbeef, "Vector Load");


    // Test Vector Store Insts --------------------------------------------------
    // - VSTORE

    reset_system();

    `VRF_PATH[2] = 32'h12345678;

    `RF_PATH[1] = 32'h0800_0100;

    INST_ADDR = 14'h0000;

    DATA_ADDR = (`RF_PATH[1] + 32'h00000000) >> 2;

    `IMEM_PATH[INST_ADDR + 0]  = {3'b000, 1'b0, 2'b00, 1'b1, 5'b0, 5'd1, `FNC3_V32, 5'd2,  `OPC_VEC_VS};

    `DMEM_PATH[DATA_ADDR] = 0;

    check_result_dmem(DATA_ADDR, 32'h12345678, "Vector Store");

    // ... what else?
    all_tests_passed = 1'b1;

    #100;
    $display("All tests passed!");
    $finish();
  end

endmodule