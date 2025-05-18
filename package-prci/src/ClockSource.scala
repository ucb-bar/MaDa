package prci

import chisel3._
import chisel3.util._
import chisel3.experimental.DoubleParam

/**
  * Synchronizes a reset signal across clock domains.
  * 
  * This is equivalent to the Verilog code:
  * ```verilog
  *   (* ASYNC_REG = "TRUE" *)
  *   reg [N-1: 0] sync_ff;
  * 
  *   // Synchronizing logic
  *   always @(posedge clk) begin
  *     sync_ff <= {sync_ff[N-2 : 0], rst};
  *   end
  * 
  *   // Synchronized reset
  *   assign out = sync_ff[N-1];
  * ```
  * 
  * @param n
  */
class ClockSource(val frequencyMHz: Double) extends BlackBox(Map(
  "PERIOD" -> DoubleParam(1000 / frequencyMHz)
)) with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock = Output(Clock())
  })
  val moduleName = this.getClass.getSimpleName

  setInline(s"$moduleName.v",
    s"""
      |module $moduleName #(parameter PERIOD="") (output clock);
      |  timeunit 1ns/1ps;
      |  reg reg_clock = 1'b0;
      |  always #(PERIOD/2.0) reg_clock = ~reg_clock;
      |  assign clock = reg_clock;
      |endmodule
      |""".stripMargin)
}


