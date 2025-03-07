import chisel3._
import chisel3.util._


class Axi4StreamBundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val t = Decoupled(new Bundle {
    val data = Output(UInt(params.dataWidth.W))
    val last = Output(Bool())
    val user = Output(Bool())
  })
}

