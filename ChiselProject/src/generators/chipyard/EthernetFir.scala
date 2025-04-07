import chisel3._
import chisel3.util._


/**
  * Parameters for the Ethernet FIR filter
  *
  * @param dataBits - Number of bits in the input data
  * @param accBits - Number of bits in the accumulator
  * @param numChannels - Number of channels (taps) in the filter
  * @param mmioAddr - MMIO address for the filter
  */
case class FfeParams(
  dataBits: Int = 8,
  accBits: Int = 18,
  numChannels: Int = 8,
  mmioAddr: BigInt = 0x10000000,
)

/**
  * Ethernet FIR filter
  *
  * Functionally this is equivalent with the following circuit:
  *
  * val y_0 = io.weights(7) * io.in
  * val y_1 = RegNext(y_0 + io.weights(6) * io.in)
  * val y_2 = RegNext(y_1 + io.weights(5) * io.in)
  * val y_3 = RegNext(y_2 + io.weights(4) * io.in)
  * val y_4 = RegNext(y_3 + io.weights(3) * io.in)
  * val y_5 = RegNext(y_4 + io.weights(2) * io.in)
  * val y_6 = RegNext(y_5 + io.weights(1) * io.in)
  * val y_7 = RegNext(y_6 + io.weights(0) * io.in)
  * io.out := y_7
  *
  * @param params
  */
class EthernetFir(params: FfeParams) extends Module {
  val io = IO(new Bundle {
    val in = Input(SInt(params.dataBits.W))
    val weights = Input(Vec(params.numChannels, SInt(params.dataBits.W)))
    val out = Output(SInt(params.accBits.W))
  })

  val ys = Wire(Vec(params.numChannels, SInt(params.accBits.W)))

  ys(0) := io.weights(params.numChannels - 1) * io.in
  
  for (i <- 1 until params.numChannels) {
    ys(i) := RegNext(ys(i - 1)) + io.weights(params.numChannels - i - 1) * io.in
  }
  
  io.out := RegNext(ys(params.numChannels - 1))
}

class EthernetFirInst() extends Module {
  val io = IO(new Bundle {})

  val fir = Module(new EthernetFir(FfeParams()))
  fir.io.in := DontCare
  fir.io.weights := DontCare
  fir.io.out := DontCare
  dontTouch(fir.io.in)
  dontTouch(fir.io.weights)
  dontTouch(fir.io.out)
}
