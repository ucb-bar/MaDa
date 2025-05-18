package vivadoips

import chisel3._
import chisel3.util._
import chisel3.experimental.Analog

/**
  * input can be either Bool or Analog
  *
  * @param gen type of input
  */
class IBUF[T <: Data](gen: T) extends BlackBox {
  val io = IO(new Bundle {
    val I = gen
    val O = Output(Bool())
  })
}

class OBUF extends BlackBox {
  val io = IO(new Bundle {
    val I = Input(Bool())
    val O = Analog(1.W)
  })
}

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
