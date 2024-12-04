`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 11/26/2024 04:43:47 PM
// Design Name: 
// Module Name: sync_reset
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module sync_reset #(
  parameter N = 2             // No. of flops in the chain, min. 2
) (
  input clk,        // Clock @ destination clock domain   
  input rst,        // Asynchronous reset
  output out        // Synchronized reset
);    
  (* ASYNC_REG = "TRUE" *)
  reg [N-1: 0] sync_ff;
    
  // Synchronizing logic
  always @(posedge clk) begin   
    sync_ff <= {sync_ff[N-2 : 0], rst}; 
  end
    
  // Synchronized reset
  assign out = sync_ff[N-1];
    
endmodule