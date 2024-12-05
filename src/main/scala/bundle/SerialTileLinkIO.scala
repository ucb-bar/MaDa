import chisel3._
import chisel3.util._


class SerialTileLinkPhit extends Bundle {
  val phit = UInt(1.W)
}

class SerialTileLinkIO extends Bundle {
  val clock_in = Input(Clock())
  val in = Flipped(Decoupled(new SerialTileLinkPhit()))
  val out = Decoupled(new SerialTileLinkPhit())
}

