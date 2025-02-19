`timescale 1ns / 1ns


`define RF_PATH dut.core.regfile_ext.Memory
`define RF_DEPTH 32

`define VRF_PATH dut.core.vregfile_0_ext.Memory
`define VRF_DEPTH 32

`define IMEM_PATH dut.itim.mem.mem

`define DMEM_PATH dut.dtim.mem.mem
`define DMEM_DEPTH 4096


`define FNC7_0  7'b0000000 // ADD, SRL
`define FNC7_1  7'b0100000 // SUB, SRA
`define OPC_CSR 7'b1110011



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
`define OPC_ARI_RTYPE   7'b0110011
`define OPC_ARI_ITYPE   7'b0010011

// Vector instructions
`define OPC_VECTOR      7'b1010111

// ***** 5-bit Opcodes *****
`define OPC_LUI_5       5'b01101
`define OPC_AUIPC_5     5'b00101
`define OPC_JAL_5       5'b11011
`define OPC_JALR_5      5'b11001
`define OPC_BRANCH_5    5'b11000
`define OPC_STORE_5     5'b01000
`define OPC_LOAD_5      5'b00000
`define OPC_ARI_RTYPE_5 5'b01100
`define OPC_ARI_ITYPE_5 5'b00100


// ***** Function codes *****

`define FNC_FVV         3'b001
`define FNC_FVF         3'b101


`define FNC6_VADD        6'b000000  // vd[i] = vs1[i] + vs2[i]
`define FNC6_VFMIN       6'b000100  // vd[i] = min(vs1[i], vs2[i])
`define FNC6_VFMAX       6'b000110  // vd[i] = max(vs1[i], vs2[i])
`define FNC6_VFMUL       6'b100100  // vd[i] = vs1[i] * vs2[i]
`define FNC6_VFMADD      6'b101000  // vd[i] = +(vs1[i] * vd[i]) + vs2[i]
`define FNC6_VFMACC      6'b101100  // vd[i] = +(vs1[i] * vs2[i]) + vd[i]




// Branch function codes
`define FNC_BEQ         3'b000
`define FNC_BNE         3'b001
`define FNC_BLT         3'b100
`define FNC_BGE         3'b101
`define FNC_BLTU        3'b110
`define FNC_BGEU        3'b111

// Load and store function codes
`define FNC_LB          3'b000
`define FNC_LH          3'b001
`define FNC_LW          3'b010
`define FNC_LBU         3'b100
`define FNC_LHU         3'b101
`define FNC_SB          3'b000
`define FNC_SH          3'b001
`define FNC_SW          3'b010

// Arithmetic R-type and I-type functions codes
`define FNC_ADD_SUB     3'b000
`define FNC_SLL         3'b001
`define FNC_SLT         3'b010
`define FNC_SLTU        3'b011
`define FNC_XOR         3'b100
`define FNC_OR          3'b110
`define FNC_AND         3'b111
`define FNC_SRL_SRA     3'b101

// ADD and SUB use the same opcode + function code
// SRA and SRL also use the same opcode + function code
// For these operations, we also need to look at bit 30 of the instruction
`define FNC2_ADD        1'b0
`define FNC2_SUB        1'b1
`define FNC2_SRL        1'b0
`define FNC2_SRA        1'b1




module EECS252TestBench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;

  // setup clock and reset
  reg clock, reset;
  initial clock = 0;
  always #(CLOCK_PERIOD/2) clock = ~clock;

  wire [31:0] timeout_cycle = 20;

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

  reg [31:0] cycle;
  reg done;
  reg [31:0]  current_test_id = 0;
  reg [255:0] current_test_type;
  reg [31:0]  current_output;
  reg [31:0]  current_result;
  reg all_tests_passed = 0;


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
    input [255:0] test_type;
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
    input [255:0] test_type;
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
    input [255:0] test_type;
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

  integer i;

  reg [31:0] num_cycles = 0;
  reg [31:0] num_insts  = 0;
  reg [4:0]  RD, RS1, RS2;
  reg [31:0] RD1, RD2;
  reg [4:0]  SHAMT;
  reg [31:0] IMM, IMM0, IMM1, IMM2, IMM3;
  reg [14:0] INST_ADDR;
  reg [14:0] DATA_ADDR;
  reg [14:0] DATA_ADDR0, DATA_ADDR1, DATA_ADDR2, DATA_ADDR3;
  reg [14:0] DATA_ADDR4, DATA_ADDR5, DATA_ADDR6, DATA_ADDR7;
  reg [14:0] DATA_ADDR8, DATA_ADDR9;

  reg [31:0] JUMP_ADDR;

  reg [31:0]  BR_TAKEN_OP1  [5:0];
  reg [31:0]  BR_TAKEN_OP2  [5:0];
  reg [31:0]  BR_NTAKEN_OP1 [5:0];
  reg [31:0]  BR_NTAKEN_OP2 [5:0];
  reg [2:0]   BR_TYPE       [5:0];
  reg [255:0] BR_NAME_TK1   [5:0];
  reg [255:0] BR_NAME_TK2   [5:0];
  reg [255:0] BR_NAME_NTK   [5:0];

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

    // Test R-Type Insts --------------------------------------------------
    // - ADD, SUB, SLL, SLT, SLTU, XOR, OR, AND, SRL, SRA
    // - SLLI, SRLI, SRAI
    reset_system();

    // We can also use $random to generate random values for testing
    RS1 = 1; RD1 = 'hC2C80000;
    RS2 = 2; RD2 = 'h43480000;
    RD  = 3;
    `VRF_PATH[RS1] = RD1;
    `VRF_PATH[RS2] = RD2;
    SHAMT           = 5'd20;
    INST_ADDR       = 14'h0000;

    `VRF_PATH[5] = 'h3F800000;

    `IMEM_PATH[INST_ADDR + 0]  = {`FNC6_VADD,   1'b0, RS2, RS1, `FNC_FVV, 5'd3,  `OPC_VECTOR};
    `IMEM_PATH[INST_ADDR + 1]  = {`FNC6_VFMUL,  1'b0, RS2, RS1, `FNC_FVV, 5'd4,  `OPC_VECTOR};
    `IMEM_PATH[INST_ADDR + 2]  = {`FNC6_VFMACC, 1'b0, RS2, RS1, `FNC_FVV, 5'd5,  `OPC_VECTOR};

    check_result_vrf(5'd3,  32'h42C80000, "Vector FP ADD");
    check_result_vrf(5'd4,  32'hC69C4000, "Vector FP MUL");
    check_result_vrf(5'd5,  32'hC69C3E00, "Vector FP MACC");


    // ... what else?
    all_tests_passed = 1'b1;

    #100;
    $display("All tests passed!");
    $finish();
  end

endmodule