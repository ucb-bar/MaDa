import chisel3.{BlackBox, _}
import chisel3.util._


class SyncReset(n: Int = 2) extends RawModule {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val reset = Input(Bool())
    val out = Output(Bool())
  })

  // (* ASYNC_REG = "TRUE" *)
  // reg [N-1: 0] sync_ff;
    
  // // Synchronizing logic
  // always @(posedge clk) begin   
  //   sync_ff <= {sync_ff[N-2 : 0], rst}; 
  // end
    
  // // Synchronized reset
  // assign out = sync_ff[N-1];

  withClockAndReset(io.clk, io.reset) {
    val sync_ff = Reg(Vec(n, Bool()))

    for (i <- 0 until n-1) {
      sync_ff(i+1) := sync_ff(i)
    }
    sync_ff(0) := io.reset
    io.out := sync_ff(n-1)
  }
}
