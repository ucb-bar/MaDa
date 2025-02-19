import chisel3._
import chisel3.util._


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
class SyncReset(n: Int = 2) extends RawModule {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Reset())
    val out = Output(Bool())
  })

  withClockAndReset(io.clock, io.reset) {
    val reg_sync = Reg(Vec(n, Bool()))

    for (i <- 0 until n-1) {
      reg_sync(i+1) := reg_sync(i)
    }
    reg_sync(0) := io.reset
    io.out := reg_sync(n-1)
  }
}


