package delta

import chisel3._
import chisel3.util._


class AsyncRam(
  val addressWidth: Int = 12,
  val dataWidth: Int = 32,
  val memoryContentHex: String = "",
  val memoryContentBin: String = ""
) extends BlackBox(
  Map(
    "ADDR_WIDTH" -> addressWidth,
    "DEPTH" -> (1 << addressWidth),
    "DATA_WIDTH" -> dataWidth,
    "MEM_HEX" -> memoryContentHex,
    "MEM_BIN" -> memoryContentBin
  )
) with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val raddr = Input(UInt(addressWidth.W))
    val waddr = Input(UInt(addressWidth.W))
    val wdata = Input(UInt(dataWidth.W))
    val wstrb = Input(UInt((dataWidth / 8).W))
    val rdata = Output(UInt(dataWidth.W))
  })

  addResource("verilog/AsyncRam.v")
}
