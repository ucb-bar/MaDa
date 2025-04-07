
/**
 * SimRAM is a simple RAM module for simulation that has a configurable delay.
 * Useful for testing memory access under different latency conditions.
 * 
 * **IMPORTANT**: this design is not synthesizable.
 */
module SimRamV2 #(
  parameter ADDR_WIDTH = 12,
  parameter DEPTH = (1 << ADDR_WIDTH),
  parameter DATA_WIDTH = 32,
  parameter MEM_HEX = "",
  parameter MEM_BIN = "",
  parameter READ_DELAY = 1,
  parameter WRITE_DELAY = 1
) (
  input  clock,
  input  reset,
  input  [ADDR_WIDTH-1:0] raddr,
  input  [ADDR_WIDTH-1:0] waddr,
  input  [DATA_WIDTH/8-1:0] wstrb,
  input  [DATA_WIDTH-1:0] wdata,
  output [DATA_WIDTH-1:0] rdata
);

  reg [DATA_WIDTH-1:0] mem [0:DEPTH-1];

  // delay line
  reg [DATA_WIDTH-1:0]    rdata_fifo [READ_DELAY-1:0];
  reg [ADDR_WIDTH-3:0]    waddr_fifo [WRITE_DELAY-1:0];
  reg [DATA_WIDTH/8-1:0]  wstrb_fifo [WRITE_DELAY-1:0];
  reg [DATA_WIDTH-1:0]    wdata_fifo [WRITE_DELAY-1:0];

  integer i;
  initial begin
    if (MEM_HEX != "") begin
      $readmemh(MEM_HEX, mem);
    end
    else if (MEM_BIN != "") begin
      $readmemb(MEM_BIN, mem);
    end
    else begin
      for (i = 0; i < DEPTH; i = i + 1) begin
        mem[i] = 0;
      end
    end
  end

  always @(posedge clock) begin
    if (reset) begin
      for (i = 0; i < READ_DELAY; i = i + 1) begin
        rdata_fifo[i] <= 'h0;
      end
      for (i = 0; i < WRITE_DELAY; i = i + 1) begin
        waddr_fifo[i] <= 'h0;
        wstrb_fifo[i] <= 'h0;
        wdata_fifo[i] <= 'h0;
      end
    end
    else begin
      for (i = 0; i < DATA_WIDTH/8; i = i+1) begin
        if (wstrb_fifo[WRITE_DELAY-1][i]) begin
          mem[waddr_fifo[WRITE_DELAY-1]][i*8 +: 8] <= wdata_fifo[WRITE_DELAY-1][i*8 +: 8];
        end
      end

      rdata_fifo[0] <= mem[raddr];

      waddr_fifo[0] <= waddr;
      wdata_fifo[0] <= wdata;
      wstrb_fifo[0] <= wstrb;

      // propagate the delayed data
      for (i = 0; i < READ_DELAY-1; i = i + 1) begin
        rdata_fifo[i+1] <= rdata_fifo[i];
      end
      for (i = 0; i < WRITE_DELAY-1; i = i + 1) begin
        waddr_fifo[i+1] <= waddr_fifo[i];
        wdata_fifo[i+1] <= wdata_fifo[i];
        wstrb_fifo[i+1] <= wstrb_fifo[i];
      end
    end
  end

  assign rdata = rdata_fifo[READ_DELAY-1];
endmodule
