import chisel3._
import chisel3.util._


/**
 * SimRam is a simple RAM module for simulation that has a configurable delay.
 * Useful for testing memory access under different latency conditions.
 * 
 * **IMPORTANT**: this design is not synthesizable.
 */
class SimRam(
  val addressWidth: Int = 12,
  val dataWidth: Int = 32,
  val memoryContentHex: String = "",
  val memoryContentBin: String = "",
  val readDelay: Int = 2,
  val writeDelay: Int = 2
) extends BlackBox(
  Map(
    "ADDR_WIDTH" -> addressWidth,
    "DEPTH" -> (1 << addressWidth),
    "DATA_WIDTH" -> dataWidth,
    "MEM_HEX" -> memoryContentHex,
    "MEM_BIN" -> memoryContentBin,
    "READ_DELAY" -> readDelay,
    "WRITE_DELAY" -> writeDelay
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

  addResource("verilog/SimRam.v")
}
