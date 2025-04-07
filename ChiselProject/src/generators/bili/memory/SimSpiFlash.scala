import chisel3._
import chisel3.util._

/**
 * SimSpiFlash is a simple SPI flash module for simulation that has a configurable delay.
 * Useful for testing SPI flash access under different latency conditions.
 * 
 * **IMPORTANT**: this design is not synthesizable.
 */
class SimSpiFlash() extends Module {
  val io = IO(new Bundle {
    val sck = Input(Clock())
    val cs = Input(Bool())
    val reset = Input(Bool())
    val mosi = Input(Bool())
    val miso = Output(Bool())
  })

  val state = RegInit(0.U(2.W))

  when (state === 0.U) {
    when (io.cs === 0.U) {
      state := 1.U
    }
  }
  when (state === 1.U) {
    when (io.cs === 1.U) {
      state := 2.U
    }
  }
  when (state === 2.U) {
    when (io.mosi === 1.U) {
      state := 3.U
    }
  }
}
