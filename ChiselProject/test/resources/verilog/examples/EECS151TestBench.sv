`timescale 1ns / 1ns


`define RF_PATH dut.core.regfile_ext.Memory
`define RF_DEPTH 32

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

// Arithmetic R-type and I-type functions codes
`define FNC3_ADD_SUB     3'b000
`define FNC3_SLL         3'b001
`define FNC3_SLT         3'b010
`define FNC3_SLTU        3'b011
`define FNC3_XOR         3'b100
`define FNC3_OR          3'b110
`define FNC3_AND         3'b111
`define FNC3_SRL_SRA     3'b101

// ***** Function 7 codes *****
`define FNC7_0  7'b0000000 // ADD, SRL
`define FNC7_1  7'b0100000 // SUB, SRA




module EECS151TestBench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;

  // setup clock and reset
  reg clock, reset;
  initial clock = 0;
  always #(CLOCK_PERIOD/2) clock = ~clock;

  int timeout_cycle = 100;

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
    .io_debug_syscall0(csr_tohost)
  );

  // Reset IMem, DMem, and RegFile before running new test
  task reset_system;
    integer i;
    begin
      for (i = 0; i < `RF_DEPTH; i = i + 1) begin
        `RF_PATH[i] = 0;
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
    while (!all_tests_passed) begin
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
      current_output    = `RF_PATH[rf_wa];
      
      fork
        begin : timeout_block
          repeat(timeout_cycle) @(posedge clock);
          $display("[Failed] Timeout at [%d] test %s, expected_result = %h, got = %h",
                  current_test_id, test_type, result, `RF_PATH[rf_wa]);
          $finish();
        end
        
        begin : check_block
      while (`RF_PATH[rf_wa] !== result) begin
        current_output = `RF_PATH[rf_wa];
        @(posedge clock);
      end
          disable timeout_block;
      done = 1;
      $display("[%d] Test %s passed!", current_test_id, test_type);
        end
      join_any
      disable fork;
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
      current_output    = `DMEM_PATH[addr];
      
      fork
        begin : timeout_block
          repeat(timeout_cycle) @(posedge clock);
          $display("[Failed] Timeout at [%d] test %s, expected_result = %h, got = %h",
                  current_test_id, test_type, result, `DMEM_PATH[addr]);
          $finish();
        end
        
        begin : check_block
      while (`DMEM_PATH[addr] !== result) begin
        current_output = `DMEM_PATH[addr];
        @(posedge clock);
      end
          disable timeout_block;
      done = 1;
      $display("[%d] Test %s passed!", current_test_id, test_type);
        end
      join_any
      disable fork;
    end
  endtask

  integer i;

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

  reg [31:0] BR_TAKEN_OP1  [5:0];
  reg [31:0] BR_TAKEN_OP2  [5:0];
  reg [31:0] BR_NTAKEN_OP1 [5:0];
  reg [31:0] BR_NTAKEN_OP2 [5:0];
  reg [2:0]  BR_TYPE       [5:0];
  string BR_NAME_TK1   [5:0];
  string BR_NAME_TK2   [5:0];
  string BR_NAME_NTK   [5:0];

  initial begin
    $dumpfile("EECS151_testbench.vcd");
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
    RS1 = 1; RD1 = -100;
    RS2 = 2; RD2 =  200;
    RD  = 3;
    `RF_PATH[RS1] = RD1;
    `RF_PATH[RS2] = RD2;
    SHAMT           = 5'd20;
    INST_ADDR       = 14'h0000;

    `IMEM_PATH[INST_ADDR + 0]  = {`FNC7_0, RS2,   RS1, `FNC3_ADD_SUB, 5'd3,  `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 1]  = {`FNC7_1, RS2,   RS1, `FNC3_ADD_SUB, 5'd4,  `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 2]  = {`FNC7_0, RS2,   RS1, `FNC3_SLL,     5'd5,  `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 3]  = {`FNC7_0, RS2,   RS1, `FNC3_SLT,     5'd6,  `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 4]  = {`FNC7_0, RS2,   RS1, `FNC3_SLTU,    5'd7,  `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 5]  = {`FNC7_0, RS2,   RS1, `FNC3_XOR,     5'd8,  `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 6]  = {`FNC7_0, RS2,   RS1, `FNC3_OR,      5'd9,  `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 7]  = {`FNC7_0, RS2,   RS1, `FNC3_AND,     5'd10, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 8]  = {`FNC7_0, RS2,   RS1, `FNC3_SRL_SRA, 5'd11, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 9]  = {`FNC7_1, RS2,   RS1, `FNC3_SRL_SRA, 5'd12, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 10] = {`FNC7_0, SHAMT, RS1, `FNC3_SLL,     5'd13, `OPC_ARI_IMM};
    `IMEM_PATH[INST_ADDR + 11] = {`FNC7_0, SHAMT, RS1, `FNC3_SRL_SRA, 5'd14, `OPC_ARI_IMM};
    `IMEM_PATH[INST_ADDR + 12] = {`FNC7_1, SHAMT, RS1, `FNC3_SRL_SRA, 5'd15, `OPC_ARI_IMM};

    check_result_rf(5'd3,  32'h00000064, "R-Type ADD");
    check_result_rf(5'd4,  32'hfffffed4, "R-Type SUB");
    check_result_rf(5'd5,  32'hffff9c00, "R-Type SLL");
    check_result_rf(5'd6,  32'h1,        "R-Type SLT");
    check_result_rf(5'd7,  32'h0,        "R-Type SLTU");
    check_result_rf(5'd8,  32'hffffff54, "R-Type XOR");
    check_result_rf(5'd9,  32'hffffffdc, "R-Type OR");
    check_result_rf(5'd10, 32'h00000088, "R-Type AND");
    check_result_rf(5'd11, 32'h00ffffff, "R-Type SRL");
    check_result_rf(5'd12, 32'hffffffff, "R-Type SRA");
    check_result_rf(5'd13, 32'hf9c00000, "R-Type SLLI");
    check_result_rf(5'd14, 32'h00000fff, "R-Type SRLI");
    check_result_rf(5'd15, 32'hffffffff, "R-Type SRAI");

    // Test I-Type Insts --------------------------------------------------
    // - ADDI, SLTI, SLTUI, XORI, ORI, ANDI
    // - LW, LH, LB, LHU, LBU
    // - JALR

    // Test I-type arithmetic instructions
    reset_system();

    RS1 = 1; RD1 = -100;
    `RF_PATH[RS1] = RD1;
    IMM             = -200;
    INST_ADDR       = 14'h0000;

    `IMEM_PATH[INST_ADDR + 0] = {IMM[11:0], RS1, `FNC3_ADD_SUB, 5'd3, `OPC_ARI_IMM};
    `IMEM_PATH[INST_ADDR + 1] = {IMM[11:0], RS1, `FNC3_SLT,     5'd4, `OPC_ARI_IMM};
    `IMEM_PATH[INST_ADDR + 2] = {IMM[11:0], RS1, `FNC3_SLTU,    5'd5, `OPC_ARI_IMM};
    `IMEM_PATH[INST_ADDR + 3] = {IMM[11:0], RS1, `FNC3_XOR,     5'd6, `OPC_ARI_IMM};
    `IMEM_PATH[INST_ADDR + 4] = {IMM[11:0], RS1, `FNC3_OR,      5'd7, `OPC_ARI_IMM};
    `IMEM_PATH[INST_ADDR + 5] = {IMM[11:0], RS1, `FNC3_AND,     5'd8, `OPC_ARI_IMM};

    check_result_rf(5'd3,  32'hfffffed4, "I-Type ADD");
    check_result_rf(5'd4,  32'h00000000, "I-Type SLT");
    check_result_rf(5'd5,  32'h00000000, "I-Type SLTU");
    check_result_rf(5'd6,  32'h000000a4, "I-Type XOR");
    check_result_rf(5'd7,  32'hffffffbc, "I-Type OR");
    check_result_rf(5'd8,  32'hffffff18, "I-Type AND");

    // Test I-type load instructions
    reset_system();

    `RF_PATH[1] = 32'h0800_0100;
    IMM0            = 32'h0000_0000;
    IMM1            = 32'h0000_0001;
    IMM2            = 32'h0000_0002;
    IMM3            = 32'h0000_0003;
    INST_ADDR       = 14'h0000;
    DATA_ADDR       = (`RF_PATH[1] + IMM0[11:0]) >> 2;

    `IMEM_PATH[INST_ADDR + 0] = {IMM0[11:0], 5'd1, `FNC3_LW,  5'd2,  `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 1] = {IMM0[11:0], 5'd1, `FNC3_LH,  5'd3,  `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 2] = {IMM1[11:0], 5'd1, `FNC3_LH,  5'd4,  `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 3] = {IMM2[11:0], 5'd1, `FNC3_LH,  5'd5,  `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 4] = {IMM3[11:0], 5'd1, `FNC3_LH,  5'd6,  `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 5] = {IMM0[11:0], 5'd1, `FNC3_LB,  5'd7,  `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 6] = {IMM1[11:0], 5'd1, `FNC3_LB,  5'd8,  `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 7] = {IMM2[11:0], 5'd1, `FNC3_LB,  5'd9,  `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 8] = {IMM3[11:0], 5'd1, `FNC3_LB,  5'd10, `OPC_LOAD};

    `IMEM_PATH[INST_ADDR + 9] = {IMM0[11:0], 5'd1, `FNC3_LHU, 5'd11, `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 10] = {IMM1[11:0], 5'd1, `FNC3_LHU, 5'd12, `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 11] = {IMM2[11:0], 5'd1, `FNC3_LHU, 5'd13, `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 12] = {IMM3[11:0], 5'd1, `FNC3_LHU, 5'd14, `OPC_LOAD};

    `IMEM_PATH[INST_ADDR + 13] = {IMM0[11:0], 5'd1, `FNC3_LBU, 5'd15, `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 14] = {IMM1[11:0], 5'd1, `FNC3_LBU, 5'd16, `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 15] = {IMM2[11:0], 5'd1, `FNC3_LBU, 5'd17, `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 16] = {IMM3[11:0], 5'd1, `FNC3_LBU, 5'd18, `OPC_LOAD};

    `DMEM_PATH[DATA_ADDR] = 32'hdeadbeef;

    check_result_rf(5'd2,  32'hdeadbeef, "I-Type LW");

    check_result_rf(5'd3,  32'hffffbeef, "I-Type LH 0");
    check_result_rf(5'd4,  32'hffffbeef, "I-Type LH 1");
    check_result_rf(5'd5,  32'hffffdead, "I-Type LH 2");
    check_result_rf(5'd6,  32'hffffdead, "I-Type LH 3");

    check_result_rf(5'd7,  32'hffffffef, "I-Type LB 0");
    check_result_rf(5'd8,  32'hffffffbe, "I-Type LB 1");
    check_result_rf(5'd9,  32'hffffffad, "I-Type LB 2");
    check_result_rf(5'd10, 32'hffffffde, "I-Type LB 3");

    check_result_rf(5'd11, 32'h0000beef, "I-Type LHU 0");
    check_result_rf(5'd12, 32'h0000beef, "I-Type LHU 1");
    check_result_rf(5'd13, 32'h0000dead, "I-Type LHU 2");
    check_result_rf(5'd14, 32'h0000dead, "I-Type LHU 3");

    check_result_rf(5'd15, 32'h000000ef, "I-Type LBU 0");
    check_result_rf(5'd16, 32'h000000be, "I-Type LBU 1");
    check_result_rf(5'd17, 32'h000000ad, "I-Type LBU 2");
    check_result_rf(5'd18, 32'h000000de, "I-Type LBU 3");

    // Test S-Type Insts --------------------------------------------------
    // - SW, SH, SB

    reset_system();

    `RF_PATH[1]  = 32'h12345678;

    `RF_PATH[2]  = 32'h0800_0010;

    `RF_PATH[3]  = 32'h0800_0020;
    `RF_PATH[4]  = 32'h0800_0030;
    `RF_PATH[5]  = 32'h0800_0040;
    `RF_PATH[6]  = 32'h0800_0050;

    `RF_PATH[7]  = 32'h0800_0060;
    `RF_PATH[8]  = 32'h0800_0070;
    `RF_PATH[9]  = 32'h0800_0080;
    `RF_PATH[10] = 32'h0800_0090;

    IMM0 = 32'h0000_0100;
    IMM1 = 32'h0000_0101;
    IMM2 = 32'h0000_0102;
    IMM3 = 32'h0000_0103;

    INST_ADDR = 14'h0000;

    DATA_ADDR0 = (`RF_PATH[2]  + IMM0[11:0]) >> 2;

    DATA_ADDR1 = (`RF_PATH[3]  + IMM0[11:0]) >> 2;
    DATA_ADDR2 = (`RF_PATH[4]  + IMM1[11:0]) >> 2;
    DATA_ADDR3 = (`RF_PATH[5]  + IMM2[11:0]) >> 2;
    DATA_ADDR4 = (`RF_PATH[6]  + IMM3[11:0]) >> 2;

    DATA_ADDR5 = (`RF_PATH[7]  + IMM0[11:0]) >> 2;
    DATA_ADDR6 = (`RF_PATH[8]  + IMM1[11:0]) >> 2;
    DATA_ADDR7 = (`RF_PATH[9]  + IMM2[11:0]) >> 2;
    DATA_ADDR8 = (`RF_PATH[10] + IMM3[11:0]) >> 2;

    `IMEM_PATH[INST_ADDR + 0] = {IMM0[11:5], 5'd1, 5'd2,  `FNC3_SW, IMM0[4:0], `OPC_STORE};

    `IMEM_PATH[INST_ADDR + 1] = {IMM0[11:5], 5'd1, 5'd3,  `FNC3_SH, IMM0[4:0], `OPC_STORE};
    `IMEM_PATH[INST_ADDR + 2] = {IMM1[11:5], 5'd1, 5'd4,  `FNC3_SH, IMM1[4:0], `OPC_STORE};
    `IMEM_PATH[INST_ADDR + 3] = {IMM2[11:5], 5'd1, 5'd5,  `FNC3_SH, IMM2[4:0], `OPC_STORE};
    `IMEM_PATH[INST_ADDR + 4] = {IMM3[11:5], 5'd1, 5'd6,  `FNC3_SH, IMM3[4:0], `OPC_STORE};

    `IMEM_PATH[INST_ADDR + 5] = {IMM0[11:5], 5'd1, 5'd7,  `FNC3_SB, IMM0[4:0], `OPC_STORE};
    `IMEM_PATH[INST_ADDR + 6] = {IMM1[11:5], 5'd1, 5'd8,  `FNC3_SB, IMM1[4:0], `OPC_STORE};
    `IMEM_PATH[INST_ADDR + 7] = {IMM2[11:5], 5'd1, 5'd9,  `FNC3_SB, IMM2[4:0], `OPC_STORE};
    `IMEM_PATH[INST_ADDR + 8] = {IMM3[11:5], 5'd1, 5'd10, `FNC3_SB, IMM3[4:0], `OPC_STORE};

    `DMEM_PATH[DATA_ADDR0] = 0;
    `DMEM_PATH[DATA_ADDR1] = 0;
    `DMEM_PATH[DATA_ADDR3] = 0;
    `DMEM_PATH[DATA_ADDR4] = 0;
    `DMEM_PATH[DATA_ADDR5] = 0;
    `DMEM_PATH[DATA_ADDR6] = 0;
    `DMEM_PATH[DATA_ADDR7] = 0;
    `DMEM_PATH[DATA_ADDR8] = 0;

    check_result_dmem(DATA_ADDR0, 32'h12345678, "S-Type SW");

    check_result_dmem(DATA_ADDR1, 32'h00005678, "S-Type SH 1");
    check_result_dmem(DATA_ADDR2, 32'h00005678, "S-Type SH 2");
    check_result_dmem(DATA_ADDR3, 32'h56780000, "S-Type SH 3");
    check_result_dmem(DATA_ADDR4, 32'h56780000, "S-Type SH 4");

    check_result_dmem(DATA_ADDR5, 32'h00000078, "S-Type SB 1");
    check_result_dmem(DATA_ADDR6, 32'h00007800, "S-Type SB 2");
    check_result_dmem(DATA_ADDR7, 32'h00780000, "S-Type SB 3");
    check_result_dmem(DATA_ADDR8, 32'h78000000, "S-Type SB 4");

    // Test U-Type Insts --------------------------------------------------
    // - LUI, AUIPC
    reset_system();

    IMM = 32'h7FFF_0123;
    INST_ADDR = 14'h0000;

    `IMEM_PATH[INST_ADDR + 0] = {IMM[31:12], 5'd3, `OPC_LUI};
    `IMEM_PATH[INST_ADDR + 1] = {IMM[31:12], 5'd4, `OPC_AUIPC};

    check_result_rf(3,  32'h7fff0000, "U-Type LUI");
    check_result_rf(4,  32'h8fff0004, "U-Type AUIPC"); // assume PC is 1000_0004

    // Test J-Type Insts --------------------------------------------------
    // - JAL
    reset_system();

    `RF_PATH[1] = 100;
    `RF_PATH[2] = 200;
    `RF_PATH[3] = 300;
    `RF_PATH[4] = 400;

    IMM       = 32'h0000_0FF0;
    INST_ADDR = 14'h0000;
    JUMP_ADDR = (32'h1000_0000 + {IMM[20:1], 1'b0}) >> 2;

    `IMEM_PATH[INST_ADDR + 0]   = {IMM[20], IMM[10:1], IMM[11], IMM[19:12], 5'd5, `OPC_JAL};
    `IMEM_PATH[INST_ADDR + 1]   = {`FNC7_0, 5'd2, 5'd1, `FNC3_ADD_SUB, 5'd6, `OPC_ARI};
    `IMEM_PATH[JUMP_ADDR[13:0]] = {`FNC7_0, 5'd4, 5'd3, `FNC3_ADD_SUB, 5'd7, `OPC_ARI};

    check_result_rf(5'd5, 32'h1000_0004, "J-Type JAL");
    check_result_rf(5'd7, 700, "J-Type JAL");
    check_result_rf(5'd6, 0, "J-Type JAL");

    // Test I-Type JALR Insts ---------------------------------------------
    reset_system();

    `RF_PATH[1] = 32'h1000_0100;
    `RF_PATH[2] = 200;
    `RF_PATH[3] = 300;
    `RF_PATH[4] = 400;

    IMM       = 32'hFFFF_FFF0;
    INST_ADDR = 14'h0000;
    JUMP_ADDR = (`RF_PATH[1] + IMM) >> 2;

    `IMEM_PATH[INST_ADDR + 0]   = {IMM[11:0], 5'd1, 3'b000, 5'd5, `OPC_JALR};
    `IMEM_PATH[INST_ADDR + 1]   = {`FNC7_0,   5'd2, 5'd1, `FNC3_ADD_SUB, 5'd6, `OPC_ARI};
    `IMEM_PATH[JUMP_ADDR[13:0]] = {`FNC7_0,   5'd4, 5'd3, `FNC3_ADD_SUB, 5'd7, `OPC_ARI};

    check_result_rf(5'd5, 32'h1000_0004, "J-Type JALR");
    check_result_rf(5'd7, 700, "J-Type JALR");
    check_result_rf(5'd6, 0, "J-Type JALR");

    // Test B-Type Insts --------------------------------------------------
    // - BEQ, BNE, BLT, BGE, BLTU, BGEU

    IMM       = 32'h0000_0FF0;
    INST_ADDR = 14'h0000;
    JUMP_ADDR = (32'h1000_0000 + IMM[12:0]) >> 2;

    BR_TYPE[0]     = `FNC3_BEQ;
    BR_NAME_TK1[0] = "U-Type BEQ Taken 1";
    BR_NAME_TK2[0] = "U-Type BEQ Taken 2";
    BR_NAME_NTK[0] = "U-Type BEQ Not Taken";

    BR_TAKEN_OP1[0]  = 100; BR_TAKEN_OP2[0]  = 100;
    BR_NTAKEN_OP1[0] = 100; BR_NTAKEN_OP2[0] = 200;

    BR_TYPE[1]       = `FNC3_BNE;
    BR_NAME_TK1[1]   = "U-Type BNE Taken 1";
    BR_NAME_TK2[1]   = "U-Type BNE Taken 2";
    BR_NAME_NTK[1]   = "U-Type BNE Not Taken";
    BR_TAKEN_OP1[1]  = 100; BR_TAKEN_OP2[1]  = 200;
    BR_NTAKEN_OP1[1] = 100; BR_NTAKEN_OP2[1] = 100;

    BR_TYPE[2]       = `FNC3_BLT;
    BR_NAME_TK1[2]   = "U-Type BLT Taken 1";
    BR_NAME_TK2[2]   = "U-Type BLT Taken 2";
    BR_NAME_NTK[2]   = "U-Type BLT Not Taken";
    BR_TAKEN_OP1[2]  = 100; BR_TAKEN_OP2[2]  = 200;
    BR_NTAKEN_OP1[2] = 200; BR_NTAKEN_OP2[2] = 100;

    BR_TYPE[3]       = `FNC3_BGE;
    BR_NAME_TK1[3]   = "U-Type BGE Taken 1";
    BR_NAME_TK2[3]   = "U-Type BGE Taken 2";
    BR_NAME_NTK[3]   = "U-Type BGE Not Taken";
    BR_TAKEN_OP1[3]  = 300; BR_TAKEN_OP2[3]  = 200;
    BR_NTAKEN_OP1[3] = 100; BR_NTAKEN_OP2[3] = 200;

    BR_TYPE[4]       = `FNC3_BLTU;
    BR_NAME_TK1[4]   = "U-Type BLTU Taken 1";
    BR_NAME_TK2[4]   = "U-Type BLTU Taken 2";
    BR_NAME_NTK[4]   = "U-Type BLTU Not Taken";
    BR_TAKEN_OP1[4]  = 32'h0000_0001; BR_TAKEN_OP2[4]  = 32'hFFFF_0000;
    BR_NTAKEN_OP1[4] = 32'hFFFF_0000; BR_NTAKEN_OP2[4] = 32'h0000_0001;

    BR_TYPE[5]       = `FNC3_BGEU;
    BR_NAME_TK1[5]   = "U-Type BGEU Taken 1";
    BR_NAME_TK2[5]   = "U-Type BGEU Taken 2";
    BR_NAME_NTK[5]   = "U-Type BGEU Not Taken";
    BR_TAKEN_OP1[5]  = 32'hFFFF_0000; BR_TAKEN_OP2[5]  = 32'h0000_0001;
    BR_NTAKEN_OP1[5] = 32'h0000_0001; BR_NTAKEN_OP2[5] = 32'hFFFF_0000;

    for (i = 0; i < 6; i = i + 1) begin
      reset_system();

      `RF_PATH[1] = BR_TAKEN_OP1[i];
      `RF_PATH[2] = BR_TAKEN_OP2[i];
      `RF_PATH[3] = 300;
      `RF_PATH[4] = 400;

      // Test branch taken
      `IMEM_PATH[INST_ADDR + 0]   = {IMM[12], IMM[10:5], 5'd2, 5'd1, BR_TYPE[i], IMM[4:1], IMM[11], `OPC_BRANCH};
      `IMEM_PATH[INST_ADDR + 1]   = {`FNC7_0, 5'd4, 5'd3, `FNC3_ADD_SUB, 5'd5, `OPC_ARI};
      `IMEM_PATH[JUMP_ADDR[13:0]] = {`FNC7_0, 5'd4, 5'd3, `FNC3_ADD_SUB, 5'd6, `OPC_ARI};

      check_result_rf(5'd5, 0,   BR_NAME_TK1[i]);
      check_result_rf(5'd6, 700, BR_NAME_TK2[i]);

      reset_system();

      `RF_PATH[1] = BR_NTAKEN_OP1[i];
      `RF_PATH[2] = BR_NTAKEN_OP2[i];
      `RF_PATH[3] = 300;
      `RF_PATH[4] = 400;

      // Test branch not taken
      `IMEM_PATH[INST_ADDR + 0] = {IMM[12], IMM[10:5], 5'd2, 5'd1, BR_TYPE[i], IMM[4:1], IMM[11], `OPC_BRANCH};
      `IMEM_PATH[INST_ADDR + 1] = {`FNC7_0, 5'd4, 5'd3, `FNC3_ADD_SUB, 5'd5, `OPC_ARI};

      check_result_rf(5'd5, 700, BR_NAME_NTK[i]);
    end

    // Test CSR Insts -----------------------------------------------------
    // - CSRRW, CSRRWI
    reset_system();

    `RF_PATH[1] = 100;
    IMM       = 5'd16;
    INST_ADDR = 14'h0000;

    `IMEM_PATH[INST_ADDR + 0] = {12'h8C0, 5'd1,     3'b001, 5'd0, `OPC_CSR};
    `IMEM_PATH[INST_ADDR + 1] = {12'h8C0, IMM[4:0], 3'b101, 5'd0, `OPC_CSR};

    current_test_id = current_test_id + 1;
    current_test_type = "CSRRW Test";
    done = 0;
    wait (csr_tohost === `RF_PATH[1]);
    done = 1;

    $display("[%d] Test CSRRW passed!", current_test_id);

    current_test_id = current_test_id + 1;
    current_test_type = "CSRRWI Test";
    done = 0;
    wait (csr_tohost === IMM);
    done = 1;

    $display("[%d] Test CSRRWI passed!", current_test_id);

    // Test Hazards -------------------------------------------------------
    // ALU->ALU hazard (RS1)
    reset_system();
    init_rf();
    INST_ADDR = 14'h0000;
    `IMEM_PATH[INST_ADDR + 0] = {`FNC7_0, 5'd1, 5'd2, `FNC3_ADD_SUB, 5'd3, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 1] = {`FNC7_0, 5'd3, 5'd4, `FNC3_ADD_SUB, 5'd5, `OPC_ARI};
    check_result_rf(5'd5, `RF_PATH[1] + `RF_PATH[2] + `RF_PATH[4], "Hazard 1");

    // ALU->ALU hazard (RS2)
    reset_system();
    init_rf();
    INST_ADDR = 14'h0000;
    `IMEM_PATH[INST_ADDR + 0] = {`FNC7_0, 5'd1, 5'd2, `FNC3_ADD_SUB, 5'd3, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 1] = {`FNC7_0, 5'd4, 5'd3, `FNC3_ADD_SUB, 5'd5, `OPC_ARI};
    check_result_rf(5'd5, `RF_PATH[1] + `RF_PATH[2] + `RF_PATH[4], "Hazard 2");

    // Two-cycle ALU->ALU hazard (RS1)
    reset_system();
    init_rf();
    INST_ADDR = 14'h0000;
    `IMEM_PATH[INST_ADDR + 0] = {`FNC7_0, 5'd1, 5'd2, `FNC3_ADD_SUB, 5'd3, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 1] = {`FNC7_0, 5'd4, 5'd5, `FNC3_ADD_SUB, 5'd6, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 2] = {`FNC7_0, 5'd3, 5'd7, `FNC3_ADD_SUB, 5'd8, `OPC_ARI};

    check_result_rf(5'd8, `RF_PATH[1] + `RF_PATH[2] + `RF_PATH[7], "Hazard 3");

    // Two-cycle ALU->ALU hazard (RS2)
    reset_system();
    init_rf();
    INST_ADDR = 14'h0000;
    `IMEM_PATH[INST_ADDR + 0] = {`FNC7_0, 5'd1, 5'd2, `FNC3_ADD_SUB, 5'd3, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 1] = {`FNC7_0, 5'd4, 5'd5, `FNC3_ADD_SUB, 5'd6, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 2] = {`FNC7_0, 5'd7, 5'd3, `FNC3_ADD_SUB, 5'd8, `OPC_ARI};

    check_result_rf(5'd8, `RF_PATH[1] + `RF_PATH[2] + `RF_PATH[7], "Hazard 4");

    // Two ALU hazards
    reset_system();
    init_rf();
    INST_ADDR = 14'h0000;
    `IMEM_PATH[INST_ADDR + 0] = {`FNC7_0, 5'd1, 5'd2, `FNC3_ADD_SUB, 5'd3, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 1] = {`FNC7_0, 5'd4, 5'd3, `FNC3_ADD_SUB, 5'd5, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 2] = {`FNC7_0, 5'd5, 5'd6, `FNC3_ADD_SUB, 5'd7, `OPC_ARI};

    check_result_rf(5'd7, `RF_PATH[1] + `RF_PATH[2] + `RF_PATH[4] + `RF_PATH[6], "Hazard 5");

    // ALU->MEM hazard
    reset_system();
    init_rf();
    `RF_PATH[4] = 32'h0800_0100;
    IMM             = 32'h0000_0000;
    INST_ADDR       = 14'h0000;
    DATA_ADDR       = (`RF_PATH[4] + IMM[11:0]) >> 2;
    `IMEM_PATH[INST_ADDR + 0] = {`FNC7_0, 5'd1, 5'd2, `FNC3_ADD_SUB, 5'd3, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 1] = {IMM[11:5], 5'd3, 5'd4, `FNC3_SW, IMM[4:0], `OPC_STORE};

    check_result_dmem(DATA_ADDR, `RF_PATH[1] + `RF_PATH[2], "Hazard 6");

    // MEM->ALU hazard
    reset_system();
    init_rf();
    `RF_PATH[1] = 32'h0800_0100;
    IMM             = 32'h0000_0000;
    INST_ADDR       = 14'h0000;
    DATA_ADDR       = (`RF_PATH[1] + IMM[11:0]) >> 2;
    `DMEM_PATH[DATA_ADDR] = 32'h12345678;
    `IMEM_PATH[INST_ADDR + 0] = {IMM[11:0], 5'd1, `FNC3_LW, 5'd2, `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 1] = {`FNC7_0, 5'd2, 5'd3, `FNC3_ADD_SUB, 5'd4, `OPC_ARI};

    check_result_rf(5'd4, `DMEM_PATH[DATA_ADDR] + `RF_PATH[3], "Hazard 7");

    // MEM->MEM hazard (store data)
    reset_system();
    init_rf();
    `RF_PATH[1] = 32'h0800_0100;
    `RF_PATH[4] = 32'h0800_0200;
    IMM             = 32'h0000_0000;
    INST_ADDR       = 14'h0000;
    DATA_ADDR0      = (`RF_PATH[1] + IMM[11:0]) >> 2;
    DATA_ADDR1      = (`RF_PATH[4] + IMM[11:0]) >> 2;

    `DMEM_PATH[DATA_ADDR0] = 32'h12345678;
    `IMEM_PATH[INST_ADDR + 0] = {IMM[11:0], 5'd1, `FNC3_LW, 5'd2, `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 1] = {IMM[11:5], 5'd2, 5'd4, `FNC3_SW, IMM[4:0], `OPC_STORE};

    check_result_dmem(DATA_ADDR1, `DMEM_PATH[DATA_ADDR0], "Hazard 8");

    // MEM->MEM hazard (store address)
    reset_system();
    init_rf();
    `RF_PATH[1] = 32'h0800_0100;
    IMM             = 32'h0000_0000;
    INST_ADDR       = 14'h0000;
    DATA_ADDR0      = (`RF_PATH[1] + IMM[11:0]) >> 2;
    `DMEM_PATH[DATA_ADDR0] = 32'h0800_0200;
    DATA_ADDR1      = (`DMEM_PATH[DATA_ADDR0] + IMM[11:0]) >> 2;

    `IMEM_PATH[INST_ADDR + 0] = {IMM[11:0], 5'd1, `FNC3_LW, 5'd2, `OPC_LOAD};
    `IMEM_PATH[INST_ADDR + 1] = {IMM[11:5], 5'd4, 5'd2, `FNC3_SW, IMM[4:0], `OPC_STORE};

    check_result_dmem(DATA_ADDR1, `RF_PATH[4], "Hazard 9");

    // Hazard to Branch operands
    reset_system();
    init_rf();
    INST_ADDR = 14'h0000;
    IMM       = 32'h0000_0FF0;
    JUMP_ADDR = (32'h1000_0008 + IMM[12:0]) >> 2; // note the PC address here

    `IMEM_PATH[INST_ADDR + 0]   = {`FNC7_0, 5'd1, 5'd4, `FNC3_ADD_SUB, 5'd6, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 1]   = {`FNC7_0, 5'd2, 5'd3, `FNC3_ADD_SUB, 5'd7, `OPC_ARI};
    `IMEM_PATH[INST_ADDR + 2]   = {IMM[12], IMM[10:5], 5'd6, 5'd7, `FNC3_BEQ, IMM[4:1], IMM[11], `OPC_BRANCH}; // Branch will be taken
    `IMEM_PATH[INST_ADDR + 3]   = {`FNC7_0, 5'd8, 5'd9, `FNC3_ADD_SUB, 5'd10, `OPC_ARI};
    `IMEM_PATH[JUMP_ADDR[13:0]] = {`FNC7_1, 5'd8, 5'd9, `FNC3_ADD_SUB, 5'd11, `OPC_ARI};

    check_result_rf(5'd10, `RF_PATH[10], "Hazard 10 1"); // x10 should not be updated
    check_result_rf(5'd11, `RF_PATH[9] - `RF_PATH[8], "Hazard 10 2"); // x11 should be updated

    // JAL Writeback hazard
    reset_system();
    init_rf();
    IMM       = 32'h0000_0004;
    INST_ADDR = 14'h0000;
    JUMP_ADDR = (32'h1000_0000 + {IMM[20:1], 1'b0}) >> 2; // === INST_ADDR + 1


    `IMEM_PATH[INST_ADDR + 0] = {IMM[20], IMM[10:1], IMM[11], IMM[19:12], 5'd1, `OPC_JAL};
    `IMEM_PATH[INST_ADDR + 1] = {`FNC7_0, 5'd2, 5'd1, `FNC3_ADD_SUB, 5'd3, `OPC_ARI};

    check_result_rf(5'd3, `RF_PATH[2] + 32'h1000_0004, "Hazard 11");

    // JALR Writeback hazard
    reset_system();
    init_rf();
    `RF_PATH[4] = 32'h1000_0000;
    IMM       = 32'h0000_0004;
    INST_ADDR = 14'h0000;
    JUMP_ADDR = (`RF_PATH[1] + IMM[11:0]) >> 2; // === INST_ADDR + 1

    `IMEM_PATH[INST_ADDR + 0] = {IMM[11:0], 5'd4, 3'b000, 5'd1, `OPC_JALR};
    `IMEM_PATH[INST_ADDR + 1] = {`FNC7_0, 5'd2, 5'd1, `FNC3_ADD_SUB, 5'd3, `OPC_ARI};

    check_result_rf(5'd3, `RF_PATH[2] + 32'h1000_0004, "Hazard 12");

    // ... what else?
    all_tests_passed = 1'b1;

    #100;
    $display("All tests passed!");
    $finish();
  end

endmodule