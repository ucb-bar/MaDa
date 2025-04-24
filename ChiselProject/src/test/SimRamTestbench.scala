import chisel3._
import chisel3.util._


class SimRamTestbench extends RawModule {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val success = Output(Bool())
  })
//   override def desiredName = "Testbench"

  withClockAndReset(io.clock, io.reset) {
    val cycles = RegInit(0.U(32.W))
    cycles := cycles + 1.U

    val dut = Module(new SimRam(
      addressWidth = 8,
      dataWidth = 32,
      memoryFileHex = "firmware.hex",
      memoryFileBin = "firmware.bin",
      readLatency = 2,
      writeLatency = 2
    ))

    dut.io := DontCare

    dontTouch(dut.io)

    io.success := true.B
  }
}
