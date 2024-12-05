import chisel3.{BlackBox, _}
import chisel3.util._


class axi_gpio_0 extends BlackBox {
  val io = IO(new Bundle {
    val s_axi = Flipped(new RawAXI4Lite())
    val gpio_io_i = Input(UInt(32.W))
    val gpio_io_o = Output(UInt(32.W))
    val gpio_io_t = Output(UInt(32.W))
  })
}
