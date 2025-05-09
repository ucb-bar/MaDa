import chisel3._
import chisel3.util._

import java.io.PrintWriter


class Axi4LiteGpio extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBundle())
    val gpio_io_i = Input(UInt(32.W))
    val gpio_io_o = Output(UInt(32.W))
    val gpio_io_t = Output(UInt(32.W))
  })

  val blackbox = Module(new Axi4LiteGpioBlackbox())

  blackbox.io.s_axi_aclk := clock
  blackbox.io.s_axi_aresetn := ~reset.asBool
  blackbox.io.s_axi.connectFrom(io.s_axi)

  blackbox.io.gpio_io_i := io.gpio_io_i
  io.gpio_io_o := blackbox.io.gpio_io_o
  io.gpio_io_t := blackbox.io.gpio_io_t
}

class Axi4LiteGpioBlackbox extends BlackBox {
  val io = IO(new Bundle {
    val s_axi_aclk = Input(Clock())
    val s_axi_aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4LiteBlackboxBundle())
    val gpio_io_i = Input(UInt(32.W))
    val gpio_io_o = Output(UInt(32.W))
    val gpio_io_t = Output(UInt(32.W))
  })


  val ipName = "Axi4LiteGpioBlackbox"
  addVivadoIp(
    name="axi_gpio",
    vendor="xilinx.com",
    library="ip",
    version="2.0",
    moduleName=ipName,
    extra = ""
  )
}
