import chisel3._
import chisel3.util._


class Axi4LiteUartLite extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBundle())
    val rx = Input(Bool())
    val tx = Output(Bool())
  })

  val blackbox = Module(new Axi4LiteUartLiteBlackbox())

  blackbox.io.s_axi.connect(io.s_axi)
  blackbox.io.rx := io.rx
  io.tx := blackbox.io.tx
}

class Axi4LiteUartLiteBlackbox extends BlackBox {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBlackboxBundle())
    val rx = Input(Bool())
    val tx = Output(Bool())
  })
}