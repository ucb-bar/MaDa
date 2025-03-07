import chisel3._
import chisel3.util._


class Axi4LiteBundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val aw = Decoupled(new Bundle {
    val addr = Output(UInt(params.addressWidth.W))
  })
  val w = Decoupled(new Bundle {
    val data = Output(UInt(params.dataWidth.W))
    val strb = Output(UInt((params.dataWidth/8).W))
  })
  val b = Flipped(Decoupled(new Bundle {
    val resp = Input(UInt(2.W))
  }))
  val ar = Decoupled(new Bundle {
    val addr = Output(UInt(params.addressWidth.W))
  })
  val r = Flipped(Decoupled(new Bundle {
    val data = Input(UInt(params.dataWidth.W))
    val resp = Input(UInt(2.W))
  }))
}

object Axi4ToAxi4Lite {
  def apply(axi: Axi4Bundle): Axi4LiteBundle = {
    val converter = Module(new Axi4ProtocolConverter(
      s_params = Axi4Params(),
      m_params = Axi4Params()
    ))
    converter.io.s_axi <> axi
    converter.io.m_axi
  }
}