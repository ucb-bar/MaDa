`timescale 1ns / 1ps


module RamTestbench();
  logic clock, reset;  
  initial clock = 'b0;
  initial reset = 'b1;
  always #5 clock = ~clock;


  reg  [31:0] waddr;
  reg  [31:0] wdata;
  reg  [3:0]  wstrb;
  reg  [31:0] raddr;
  wire [31:0] rdata;

  SimRamForTest dut (
    .clock(clock),
    .reset(reset),
    .raddr(raddr[14:2]),
    .waddr(waddr[14:2]),
    .wstrb(wstrb),
    .wdata(wdata),
    .rdata(rdata)
  );

  initial begin
    waddr = 0;
    wdata = 0;
    wstrb = 0;
    raddr = 0;
    
    reset = 1;

    repeat (10) @(posedge clock);
    reset = 0;

    // word write
    wstrb = 'h0f;
    wdata = 'hdeadbeef;
    waddr = 'h00000000;
    @(posedge clock);
    wstrb = 'h00;
    wdata = 'h00000000;
    waddr = 'h00000000;
    @(posedge clock);
    raddr = 'h00000000;
    @(posedge clock);
    assert (rdata == 'hdeadbeef) else $error($time, "\tword write failed, expected 0xdeadbeef, got 0x%x", rdata);

    // half word write
    wstrb = 'h03;
    wdata = 'hdeadbeef;
    waddr = 'h00000004;
    @(posedge clock);
    wstrb = 'h00;
    wdata = 'h00000000;
    waddr = 'h00000000;
    @(posedge clock);
    raddr = 'h00000004;
    @(posedge clock);
    assert (rdata == 'h0000beef) else $error($time, "\thalf word write failed, expected 0x0000beef, got 0x%x", rdata);

    // half word write
    wstrb = 'hc;
    wdata = 'hdeadbeef;
    waddr = 'h00000008;
    @(posedge clock);
    wstrb = 'h00;
    wdata = 'h00000000;
    waddr = 'h00000000;
    @(posedge clock);
    raddr = 'h00000008;
    @(posedge clock);
    assert (rdata == 'hdead0000) else $error($time, "\thalf word write failed, expected 0xdead0000, got 0x%x", rdata);
    
    // byte write
    wstrb = 'h01;
    wdata = 'hdeadbeef;
    waddr = 'h0000000c;
    @(posedge clock);
    wstrb = 'h00;
    wdata = 'h00000000;
    waddr = 'h00000000;
    @(posedge clock);
    raddr = 'h0000000c;
    @(posedge clock);
    assert (rdata == 'h000000ef) else $error($time, "\tbyte write failed, expected 0x000000ef, got 0x%x", rdata);

    // byte write
    wstrb = 'h02;
    wdata = 'hdeadbeef;
    waddr = 'h00000012;
    @(posedge clock);
    wstrb = 'h00;
    wdata = 'h00000000;
    waddr = 'h00000000;
    @(posedge clock);
    raddr = 'h00000012;
    @(posedge clock);
    assert (rdata == 'h0000be00) else $error($time, "\tbyte write failed, expected 0x0000be00, got 0x%x", rdata);

    #100 $finish;
  end

endmodule
