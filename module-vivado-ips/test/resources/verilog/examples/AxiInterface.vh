
interface axi_interface #(
  parameter DATA_WIDTH = 64,
  parameter ADDR_WIDTH = 12,
  parameter ID_WIDTH = 4
) ();
    // Write Address Channel
  logic                     awvalid;
  logic                     awready;
  logic [ID_WIDTH-1:0]      awid;
  logic [ADDR_WIDTH-1:0]    awaddr;
  logic [7:0]               awlen;
  logic [2:0]               awsize;
  logic [1:0]               awburst;
  // Write Data Channel
  logic                     wvalid;
  logic                     wready;
  logic [DATA_WIDTH-1:0]    wdata;
  logic [DATA_WIDTH/8-1:0]  wstrb;
  logic                     wlast;
  // Write Response Channel
  logic                     bvalid;
  logic                     bready;
  logic [ID_WIDTH-1:0]      bid;
  logic [1:0]               bresp;
  // Read Address Channel
  logic                     arvalid;
  logic                     arready;
  logic [ID_WIDTH-1:0]      arid;
  logic [ADDR_WIDTH-1:0]    araddr;
  logic [7:0]               arlen;
  logic [2:0]               arsize;
  logic [1:0]               arburst;
  // Read Data Channel
  logic                     rvalid;
  logic                     rready;
  logic [ID_WIDTH-1:0]      rid;
  logic [DATA_WIDTH-1:0]    rdata;
  logic [1:0]               rresp;
  logic                     rlast;

endinterface