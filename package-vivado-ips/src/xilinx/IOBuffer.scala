package vivadoips

import chisel3._
import chisel3.util._
import chisel3.experimental.Analog


class IBUFG extends BlackBox {
  val io = IO(new Bundle {
    val I = Input(Bool())
    val O = Output(Bool())
  })
}

class BUFG extends BlackBox {
  val io = IO(new Bundle {
    val I = Input(Bool())
    val O = Output(Bool())
  })
}

class IOBUF extends BlackBox {
  val io = IO(new Bundle {
    // Buffer output
    val O = Output(Bool())
    // Buffer tristate inout
    val IO = Analog(1.W)
    // Buffer input
    val I = Input(Bool())
    // Buffer output enable, 1 = input, 0 = output
    val T = Input(Bool())  
  })
}
