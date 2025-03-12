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
    val I = Input(Bool())
    val O = Output(Bool())
    val T = Input(Bool())
    val IO = Analog(1.W)
  })
}
