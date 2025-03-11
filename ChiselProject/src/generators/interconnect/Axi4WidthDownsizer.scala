import chisel3._
import chisel3.util._

class Axi4WidthDownsizer(
  // s_params: Axi4Params = Axi4Params(dataWidth = 64)
  // m_params: Axi4Params = Axi4Params()
) extends Module {
  
  val s_params: Axi4Params = Axi4Params(dataWidth = 64)
  val m_params: Axi4Params = Axi4Params(dataWidth = 32)

  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle(s_params))
    val m_axi = new Axi4Bundle(m_params)
  })

  io.m_axi.aw.valid := io.s_axi.aw.valid
  io.s_axi.aw.ready := io.m_axi.aw.ready
  io.m_axi.aw.bits.id := io.s_axi.aw.bits.id
  io.m_axi.aw.bits.addr := io.s_axi.aw.bits.addr
  io.m_axi.aw.bits.len := io.s_axi.aw.bits.len
  io.m_axi.aw.bits.size := io.s_axi.aw.bits.size
  io.m_axi.aw.bits.burst := io.s_axi.aw.bits.burst
  
  io.m_axi.w.valid := io.s_axi.w.valid
  io.s_axi.w.ready := io.m_axi.w.ready
  io.m_axi.w.bits.data := io.s_axi.w.bits.data(m_params.dataWidth - 1, 0)
  io.m_axi.w.bits.strb := io.s_axi.w.bits.strb((m_params.dataWidth / 8) - 1, 0)
  io.m_axi.w.bits.last := io.s_axi.w.bits.last

  io.s_axi.b.valid := io.m_axi.b.valid
  io.m_axi.b.ready := io.s_axi.b.ready
  io.s_axi.b.bits.id := io.m_axi.b.bits.id
  io.s_axi.b.bits.resp := io.m_axi.b.bits.resp

  io.m_axi.ar.valid := io.s_axi.ar.valid
  io.s_axi.ar.ready := io.m_axi.ar.ready
  io.m_axi.ar.bits.id := io.s_axi.ar.bits.id
  io.m_axi.ar.bits.addr := io.s_axi.ar.bits.addr
  io.m_axi.ar.bits.len := io.s_axi.ar.bits.len
  io.m_axi.ar.bits.size := io.s_axi.ar.bits.size
  io.m_axi.ar.bits.burst := io.s_axi.ar.bits.burst

  io.s_axi.r.valid := io.m_axi.r.valid
  io.m_axi.r.ready := io.s_axi.r.ready
  io.s_axi.r.bits.id := io.m_axi.r.bits.id
  io.s_axi.r.bits.data := Cat(0.U((s_params.dataWidth - m_params.dataWidth).W), io.m_axi.r.bits.data)
  io.s_axi.r.bits.resp := io.m_axi.r.bits.resp
  io.s_axi.r.bits.last := io.m_axi.r.bits.last
}