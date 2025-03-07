import chisel3._
import chisel3.util._


class ZedboardIO extends Bundle {
  val GCLK = Input(Clock())

  val JA1 = Input(Bool())
  val JA2 = Input(Bool())
  val JA3 = Input(Bool())
  val JA4 = Input(Bool())
  val JA7 = Input(Bool())
  val JA8 = Input(Bool())
  val JA9 = Input(Bool())
  val JA10 = Input(Bool())

  val JB1 = Input(Bool())
  val JB2 = Input(Bool())
  val JB3 = Input(Bool())
  val JB4 = Input(Bool())
  val JB7 = Input(Bool())
  val JB8 = Input(Bool())
  val JB9 = Input(Bool())
  val JB10 = Input(Bool())

  val JC1_N = Input(Bool())
  val JC1_P = Input(Bool())
  val JC2_N = Input(Bool())
  val JC2_P = Input(Bool())
  val JC3_N = Input(Bool())
  val JC3_P = Input(Bool())
  val JC4_N = Input(Bool())
  val JC4_P = Input(Bool())

  val JD1_N = Input(Bool())
  val JD1_P = Input(Bool())
  val JD2_N = Input(Bool())
  val JD2_P = Input(Bool())
  val JD3_N = Input(Bool())
  val JD3_P = Input(Bool())
  val JD4_N = Input(Bool())
  val JD4_P = Input(Bool())

  val LD0 = Output(Bool())
  val LD1 = Output(Bool())
  val LD2 = Output(Bool())
  val LD3 = Output(Bool())
  val LD4 = Output(Bool())
  val LD5 = Output(Bool())
  val LD6 = Output(Bool())
  val LD7 = Output(Bool())

  val BTNC = Input(Bool())
  val BTND = Input(Bool())
  val BTNL = Input(Bool())
  val BTNR = Input(Bool())
  val BTNU = Input(Bool())

  val SW0 = Input(Bool())
  val SW1 = Input(Bool())
  val SW2 = Input(Bool())
  val SW3 = Input(Bool())
  val SW4 = Input(Bool())
  val SW5 = Input(Bool())
  val SW6 = Input(Bool())
  val SW7 = Input(Bool())
}

class ZedboardTop extends RawModule {
  val io = IO(new ZedboardIO())
}
