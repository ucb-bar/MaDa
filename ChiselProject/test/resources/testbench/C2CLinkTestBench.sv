`timescale 1ns / 1ps


module C2CLinkTestBench();
  parameter CLOCK_FREQ = 100_000_000;
  parameter CLOCK_PERIOD = 1_000_000_000 / CLOCK_FREQ;
  
  // setup clock and reset
  reg clock, reset;
  initial clock = 'b0;
  always #(CLOCK_PERIOD/2) clock = ~clock;

  // Full AXI4 signals
  // AW channel
  logic axi_aw_valid;
  logic axi_aw_ready;
  logic [3:0] axi_aw_id;      // Added ID width 4
  logic [31:0] axi_aw_addr;
  logic [7:0] axi_aw_len;     // Added len
  logic [2:0] axi_aw_size;    // Added size
  logic [1:0] axi_aw_burst;   // Added burst

  // W channel
  logic axi_w_valid;
  logic axi_w_ready;
  logic [31:0] axi_w_data;
  logic [3:0] axi_w_strb;
  logic axi_w_last;          // Added last

  // B channel
  logic axi_b_valid;
  logic axi_b_ready;
  logic [3:0] axi_b_id;      // Added ID
  logic [1:0] axi_b_resp;

  // AR channel
  logic axi_ar_valid;
  logic axi_ar_ready;
  logic [3:0] axi_ar_id;     // Added ID
  logic [31:0] axi_ar_addr;
  logic [7:0] axi_ar_len;    // Added len
  logic [2:0] axi_ar_size;   // Added size
  logic [1:0] axi_ar_burst;  // Added burst

  // R channel
  logic axi_r_valid;
  logic axi_r_ready;
  logic [3:0] axi_r_id;      // Added ID
  logic [31:0] axi_r_data;
  logic [1:0] axi_r_resp;
  logic axi_r_last;          // Added last

  logic c2c_out_valid;
  logic c2c_out_ready;
  logic [31:0] c2c_out_data;

  logic c2c_in_valid;
  logic c2c_in_ready;
  logic [31:0] c2c_in_data;

  // Verification signals
  int frame_count = 0;
  logic [31:0] expected_header, expected_addr_high, expected_addr_low, expected_data;
  logic test_passed = 1'b1;
  
  // Test parameters
  logic write_op, read_op;
  int bursts;
  logic [31:0] write_addr, read_addr, write_data;
  
  // Function to generate expected header
  // operation: 0 for read, 1 for write
  // burst_count: number of bursts (1-65536)
  function logic [31:0] generate_header(logic operation, int burst_count);
    return {15'b0, operation, 16'(burst_count - 1)};
  endfunction
  
  C2CLink dut(
    .clock(clock),
    .reset(reset),
    
    .io_s_axi_aw_ready(axi_aw_ready),
    .io_s_axi_aw_valid(axi_aw_valid),
    .io_s_axi_aw_bits_id(axi_aw_id),
    .io_s_axi_aw_bits_addr(axi_aw_addr),
    .io_s_axi_aw_bits_len(axi_aw_len),
    .io_s_axi_aw_bits_size(axi_aw_size),
    .io_s_axi_aw_bits_burst(axi_aw_burst),
    
    .io_s_axi_w_ready(axi_w_ready),
    .io_s_axi_w_valid(axi_w_valid),
    .io_s_axi_w_bits_data(axi_w_data),
    .io_s_axi_w_bits_strb(axi_w_strb),
    .io_s_axi_w_bits_last(axi_w_last),
    
    .io_s_axi_b_ready(axi_b_ready),
    .io_s_axi_b_valid(axi_b_valid),
    .io_s_axi_b_bits_id(axi_b_id),
    .io_s_axi_b_bits_resp(axi_b_resp),
    
    .io_s_axi_ar_ready(axi_ar_ready),
    .io_s_axi_ar_valid(axi_ar_valid),
    .io_s_axi_ar_bits_id(axi_ar_id),
    .io_s_axi_ar_bits_addr(axi_ar_addr),
    .io_s_axi_ar_bits_len(axi_ar_len),
    .io_s_axi_ar_bits_size(axi_ar_size),
    .io_s_axi_ar_bits_burst(axi_ar_burst),
    
    .io_s_axi_r_ready(axi_r_ready),
    .io_s_axi_r_valid(axi_r_valid),
    .io_s_axi_r_bits_id(axi_r_id),
    .io_s_axi_r_bits_data(axi_r_data),
    .io_s_axi_r_bits_resp(axi_r_resp),
    .io_s_axi_r_bits_last(axi_r_last),

    .io_out_valid(c2c_out_valid),
    .io_out_ready(c2c_out_ready),
    .io_out_bits(c2c_out_data),

    .io_in_valid(c2c_in_valid),
    .io_in_ready(c2c_in_ready),
    .io_in_bits(c2c_in_data)
  );

  // C2C output monitoring
  always @(posedge clock) begin
    if (!reset && c2c_out_valid && c2c_out_ready) begin
      case (frame_count)
        0: begin // Header
           if (c2c_out_data !== expected_header) begin
             $display("ERROR: Header mismatch. Got %h, Expected %h", c2c_out_data, expected_header);
             $display("  - Operation: Got %b, Expected %b", c2c_out_data[16], expected_header[16]);
             $display("  - Length: Got %d, Expected %d", c2c_out_data[15:0] + 1, expected_header[15:0] + 1);
             test_passed = 1'b0;
           end else begin
             $display("INFO: Header correct: %h (Operation: %s, Bursts: %d)", 
                     c2c_out_data, 
                     c2c_out_data[16] ? "WRITE" : "READ", 
                     c2c_out_data[15:0] + 1);
           end
        end
        1: begin // Address High
           if (c2c_out_data !== expected_addr_high) begin
             $display("ERROR: Address High mismatch. Got %h, Expected %h", c2c_out_data, expected_addr_high);
             test_passed = 1'b0;
           end else begin
             $display("INFO: Address High correct: %h", c2c_out_data);
           end
        end
        2: begin // Address Low
           if (c2c_out_data !== expected_addr_low) begin
             $display("ERROR: Address Low mismatch. Got %h, Expected %h", c2c_out_data, expected_addr_low);
             test_passed = 1'b0;
           end else begin
             $display("INFO: Address Low correct: %h", c2c_out_data);
           end
        end
        3: begin // Data
           if (c2c_out_data !== expected_data) begin
             $display("ERROR: Data mismatch. Got %h, Expected %h", c2c_out_data, expected_data);
             test_passed = 1'b0;
           end else begin
             $display("INFO: Data correct: %h", c2c_out_data);
           end
           $display("Transaction verified!");
        end
        default: begin
           $display("WARNING: Unexpected frame data: %h", c2c_out_data);
        end
      endcase
      frame_count = frame_count + 1;
    end
  end

  initial begin
    // Initialize AXI signals
    axi_aw_valid = 'b0;
    axi_aw_id = 'h0;
    axi_aw_addr = 'h00000000;
    axi_aw_len = 8'h00;     // 1 beat
    axi_aw_size = 3'b010;   // 4 bytes (32 bits)
    axi_aw_burst = 2'b01;   // INCR type
    
    axi_w_valid = 'b0;
    axi_w_data = 'h00000000;
    axi_w_strb = 'h0;
    axi_w_last = 'b0;
    
    axi_b_ready = 'b1;
    
    axi_ar_valid = 'b0;
    axi_ar_id = 'h0;
    axi_ar_addr = 'h00000000;
    axi_ar_len = 8'h00;     // 1 beat
    axi_ar_size = 3'b010;   // 4 bytes (32 bits)
    axi_ar_burst = 2'b01;   // INCR type
    
    axi_r_ready = 'b1;

    c2c_out_ready = 'b1;

    reset = 1'b1;
    repeat (4) @(posedge clock);
    reset = 1'b0;
    repeat (4) @(posedge clock);

    // Test 1: Write transaction with 1 burst
    $display("\n=== Test 1: Write Transaction (1 burst) ===");
    frame_count = 0;
    
    // Setup the AXI write transaction parameters
    write_op = 1'b1;
    bursts = 1;
    write_addr = 'h00000000;
    write_data = 'h11111111;
    
    // Automatically generate expected values
    expected_header = generate_header(write_op, bursts);
    expected_addr_high = 'h00000000; // Upper bits are 0 for 32-bit address
    expected_addr_low = write_addr;
    expected_data = write_data;
    
    $display("Generated golden pattern:");
    $display(" - Header: %h (Operation: WRITE, Bursts: %d)", expected_header, bursts);
    $display(" - Address High: %h", expected_addr_high);
    $display(" - Address Low: %h", expected_addr_low);
    $display(" - Data: %h", expected_data);
    
    // Perform AXI write transaction
    axi_aw_valid = 'b1;
    axi_aw_id = 4'h1;
    axi_aw_addr = write_addr;
    axi_aw_len = 8'h00;      // 1 beat
    repeat (1) @(posedge clock);
    axi_aw_valid = 'b0;
    
    axi_w_valid = 'b1;
    axi_w_data = write_data;
    axi_w_strb = 'b1111;
    axi_w_last = 'b1;       // Last beat
    repeat (1) @(posedge clock);
    axi_w_valid = 'b0;
    axi_w_data = 'h00000000;
    axi_w_strb = 'h00;
    axi_w_last = 'b0;

    repeat (10) @(posedge clock);
    
    // Test 2: Read transaction with 1 burst
    $display("\n=== Test 2: Read Transaction (1 burst) ===");
    frame_count = 0;
    
    // Setup the AXI read transaction parameters
    read_op = 1'b0;
    bursts = 1;
    read_addr = 'h00000024;
    
    // Automatically generate expected values
    expected_header = generate_header(read_op, bursts);
    expected_addr_high = 'h00000000; // Upper bits are 0 for 32-bit address
    expected_addr_low = read_addr;
    expected_data = 32'h00000000; // No data for read request
    
    $display("Generated golden pattern:");
    $display(" - Header: %h (Operation: READ, Bursts: %d)", expected_header, bursts);
    $display(" - Address High: %h", expected_addr_high);
    $display(" - Address Low: %h", expected_addr_low);
    $display(" - Data: %h (No data for read)", expected_data);
    
    // Perform AXI read transaction
    axi_ar_valid = 'b1;
    axi_ar_id = 4'h2;
    axi_ar_addr = read_addr;
    axi_ar_len = 8'h00;     // 1 beat
    repeat (1) @(posedge clock);
    axi_ar_valid = 'b0;
    
    repeat (10) @(posedge clock);
    
    // Test 3: Write transaction with 4 bursts
    $display("\n=== Test 3: Write Transaction (4 bursts) ===");
    frame_count = 0;
    
    // Setup the AXI write transaction parameters
    write_op = 1'b1;
    bursts = 4;
    write_addr = 'h00003000;
    write_data = 'hAABBCCDD;
    
    // Automatically generate expected values
    expected_header = generate_header(write_op, bursts);
    expected_addr_high = 'h00000000;
    expected_addr_low = write_addr;
    expected_data = write_data;
    
    $display("Generated golden pattern:");
    $display(" - Header: %h (Operation: WRITE, Bursts: %d)", expected_header, bursts);
    $display(" - Address High: %h", expected_addr_high);
    $display(" - Address Low: %h", expected_addr_low);
    $display(" - Data: %h", expected_data);
    
    // Perform AXI write transaction with 4 bursts
    axi_aw_valid = 'b1;
    axi_aw_id = 4'h3;
    axi_aw_addr = write_addr;
    axi_aw_len = 8'h03;     // 4 beats (len=3 means 4 beats in AXI4)
    repeat (1) @(posedge clock);
    axi_aw_valid = 'b0;
    
    // First beat
    axi_w_valid = 'b1;
    axi_w_data = write_data;
    axi_w_strb = 'b1111;
    axi_w_last = 'b0;       // Not last beat
    repeat (1) @(posedge clock);
    
    // Second beat
    axi_w_data = 'h22222222;
    axi_w_last = 'b0;       // Not last beat
    repeat (1) @(posedge clock);
    
    // Third beat
    axi_w_data = 'h33333333;
    axi_w_last = 'b0;       // Not last beat
    repeat (1) @(posedge clock);
    
    // Fourth beat (last)
    axi_w_data = 'h44444444;
    axi_w_last = 'b1;       // Last beat
    repeat (1) @(posedge clock);
    
    axi_w_valid = 'b0;
    axi_w_data = 'h00000000;
    axi_w_strb = 'h00;
    axi_w_last = 'b0;
    
    repeat (10) @(posedge clock);
    
    if (test_passed) begin
      $display("\nTEST PASSED! All transactions verified successfully.");
    end else begin
      $display("\nTEST FAILED! One or more transactions had errors.");
    end
    
    $finish;
  end
endmodule
