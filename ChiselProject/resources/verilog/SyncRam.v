
// Single-port RAM with synchronous read with write byte-enable
// read data will be available after one cycle
module SyncRam #(
  parameter ADDR_WIDTH = 12,
  parameter DEPTH = (1 << ADDR_WIDTH),
  parameter DATA_WIDTH = 32,
  parameter MEM_HEX = "",
  parameter MEM_BIN = ""
) (
  input  clock,
  input  reset,
  input  [ADDR_WIDTH-1:0] raddr,
  input  [ADDR_WIDTH-1:0] waddr,
  input  [DATA_WIDTH/8-1:0] wstrb,
  input  [DATA_WIDTH-1:0] wdata,
  output [DATA_WIDTH-1:0] rdata
);

  (* ram_style="block" *) reg [DATA_WIDTH-1:0] mem [0:DEPTH-1];
  
  wire [ADDR_WIDTH-3:0] raddr_idx;
  wire [ADDR_WIDTH-3:0] waddr_idx;

  reg [DATA_WIDTH-1:0] reg_rdata;
  
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
      reg_rdata <= 'h0;
    end
    else begin
      for (i = 0; i < DATA_WIDTH/8; i = i+1) begin
        if (wstrb[i])
          mem[waddr_idx][i*8 +: 8] <= wdata[i*8 +: 8];
      end
      reg_rdata <= mem[raddr_idx];
    end
  end

  assign rdata = reg_rdata;

endmodule
