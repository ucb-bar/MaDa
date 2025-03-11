import chisel3._
import chisel3.util._

class Axi4WidthUpsizer(
  s_params: Axi4Params = Axi4Params(),
  m_params: Axi4Params = Axi4Params(dataWidth = 64)
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle(s_params))
    val m_axi = new Axi4Bundle(m_params)
  })

  val s_addr_index = log2Ceil(s_params.dataWidth / 8)
  val m_addr_index = log2Ceil(m_params.dataWidth / 8)

  val reg_write_addr_offset = RegInit(0.U((m_addr_index - s_addr_index).W))
  val reg_read_addr_offset = RegInit(0.U((m_addr_index - s_addr_index).W))
  val write_addr_offset = io.m_axi.aw.bits.addr(m_addr_index-1, s_addr_index)
  val read_addr_offset = io.m_axi.ar.bits.addr(m_addr_index-1, s_addr_index)

  when (io.m_axi.aw.fire) {
    reg_write_addr_offset := write_addr_offset
  }
  when (io.m_axi.ar.fire) {
    reg_read_addr_offset := read_addr_offset
  }

  io.m_axi.aw.valid := io.s_axi.aw.valid
  io.s_axi.aw.ready := io.m_axi.aw.ready
  io.m_axi.aw.bits.id := io.s_axi.aw.bits.id
  io.m_axi.aw.bits.addr := io.s_axi.aw.bits.addr
  io.m_axi.aw.bits.len := io.s_axi.aw.bits.len
  io.m_axi.aw.bits.size := io.s_axi.aw.bits.size
  io.m_axi.aw.bits.burst := io.s_axi.aw.bits.burst
  
  io.m_axi.w.valid := io.s_axi.w.valid
  io.s_axi.w.ready := io.m_axi.w.ready
  io.m_axi.w.bits.data := Mux(
    io.m_axi.aw.fire,
    io.s_axi.w.bits.data << (s_params.dataWidth.U * write_addr_offset),
    io.s_axi.w.bits.data << (s_params.dataWidth.U * reg_write_addr_offset)
  )
  io.m_axi.w.bits.strb := Mux(
    io.m_axi.aw.fire,
    io.s_axi.w.bits.strb << ((s_params.dataWidth.U / 8.U) * write_addr_offset),
    io.s_axi.w.bits.strb << ((s_params.dataWidth.U / 8.U) * reg_write_addr_offset)
  )
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
  io.s_axi.r.bits.data := Mux(
    io.m_axi.ar.fire,
    io.m_axi.r.bits.data >> (s_params.dataWidth.U * read_addr_offset),
    io.m_axi.r.bits.data >> (s_params.dataWidth.U * reg_read_addr_offset)
  )
  io.s_axi.r.bits.resp := io.m_axi.r.bits.resp
  io.s_axi.r.bits.last := io.m_axi.r.bits.last
}