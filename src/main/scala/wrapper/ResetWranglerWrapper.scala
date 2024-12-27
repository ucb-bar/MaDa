import chisel3._

class ResetWrangler extends BlackBox {
  val io = IO(new Bundle {
    val auto_in_0_clock = Input(Clock())
    val auto_in_0_reset = Input(Bool())
    val auto_in_1_clock = Input(Clock())
    val auto_in_1_reset = Input(Bool())
    val auto_in_2_clock = Input(Clock())
    val auto_in_2_reset = Input(Bool())
    val auto_in_3_clock = Input(Clock())
    val auto_in_3_reset = Input(Bool())

    val auto_out_0_clock = Output(Clock())
    val auto_out_0_reset = Output(Bool())
    val auto_out_1_clock = Output(Clock())
    // val auto_out_1_reset = Output(Bool()) // unused
    val auto_out_2_clock = Output(Clock())
    // val auto_out_2_reset = Output(Bool())
    // val auto_out_3_clock = Output(Clock())
    val auto_out_3_reset = Output(Bool())
  })

}
