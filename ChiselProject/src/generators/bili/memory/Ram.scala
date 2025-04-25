import chisel3._
import chisel3.util._


class Ram(
  val addressWidth: Int = 12,
  val dataWidth: Int = 32,
  val memoryFileHex: String = "",
  val memoryFileBin: String = ""
) extends BlackBox(
  Map(
    "ADDR_WIDTH" -> addressWidth,
    "DEPTH" -> (1 << addressWidth),
    "DATA_WIDTH" -> dataWidth,
    "MEM_HEX" -> memoryFileHex,
    "MEM_BIN" -> memoryFileBin
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

  addResource("verilog/Ram.v")
}
