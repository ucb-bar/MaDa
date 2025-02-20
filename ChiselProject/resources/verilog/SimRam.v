
/**
 * SimRAM is a simple RAM module for simulation that has a configurable delay.
 * Useful for testing memory access under different latency conditions.
 * 
 * **IMPORTANT**: this design is not synthesizable.
 */
module SimRam #(
  parameter ADDR_WIDTH = 12,
  parameter DEPTH = (1 << ADDR_WIDTH),
  parameter DATA_WIDTH = 32,
  parameter MEM_HEX = "",
  parameter MEM_BIN = "",
  parameter READ_DELAY = 2,
  parameter WRITE_DELAY = 2
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

  wire [ADDR_WIDTH-3:0] raddr_idx;
  wire [ADDR_WIDTH-3:0] waddr_idx;

  // delay line
  reg [DATA_WIDTH-1:0] rdata_delayed [READ_DELAY-1:0];
  reg [ADDR_WIDTH-3:0] waddr_idx_delayed [WRITE_DELAY-1:0];
  reg [DATA_WIDTH/8-1:0] wstrb_delayed [WRITE_DELAY-1:0];
  reg [DATA_WIDTH-1:0] wdata_delayed [WRITE_DELAY-1:0];

  assign raddr_idx = raddr[ADDR_WIDTH-1:2];
  assign waddr_idx = waddr[ADDR_WIDTH-1:2];

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
        rdata_delayed[i] <= 'h0;
      end
      for (i = 0; i < WRITE_DELAY; i = i + 1) begin
        waddr_idx_delayed[i] <= 'h0;
        wstrb_delayed[i] <= 'h0;
        wdata_delayed[i] <= 'h0;
      end
    end
    else begin
      for (i = 0; i < DATA_WIDTH/8; i = i+1) begin
        if (wstrb_delayed[WRITE_DELAY-1][i]) begin
          mem[waddr_idx_delayed[WRITE_DELAY-1]][i*8 +: 8] <= wdata_delayed[WRITE_DELAY-1][i*8 +: 8];
        end
      end

      rdata_delayed[0] <= mem[raddr_idx];

      waddr_idx_delayed[0] <= waddr_idx;
      wdata_delayed[0] <= wdata;
      wstrb_delayed[0] <= wstrb;

      // propagate the delayed data
      for (i = 0; i < READ_DELAY-1; i = i + 1) begin
        rdata_delayed[i+1] <= rdata_delayed[i];
      end
      for (i = 0; i < WRITE_DELAY-1; i = i + 1) begin
        waddr_idx_delayed[i+1] <= waddr_idx_delayed[i];
        wdata_delayed[i+1] <= wdata_delayed[i];
        wstrb_delayed[i+1] <= wstrb_delayed[i];
      end
    end
  end

  assign rdata = rdata_delayed[READ_DELAY-1];
endmodule
