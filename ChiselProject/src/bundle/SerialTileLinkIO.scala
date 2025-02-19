import chisel3._
import chisel3.util._


class SerialTileLinkPhit(width: Int) extends Bundle {
  val phit = UInt(width.W)
}

class SerialTileLinkIO(width: Int = 1) extends Bundle {
  val clock_in = Input(Clock())
  val in = Flipped(Decoupled(new SerialTileLinkPhit(width)))
  val out = Decoupled(new SerialTileLinkPhit(width))
}

