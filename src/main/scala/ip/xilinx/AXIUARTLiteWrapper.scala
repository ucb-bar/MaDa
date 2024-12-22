import chisel3.{BlackBox, _}
import chisel3.util._


class axi_uartlite_0 extends BlackBox {
  val io = IO(new Bundle {
    val s_axi = Flipped(new RawAXI4Lite())
    val rx = Input(Bool())
    val tx = Output(Bool())
  })
}
