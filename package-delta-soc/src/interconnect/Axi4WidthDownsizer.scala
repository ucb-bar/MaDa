package delta

import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4Bundle}


class Axi4WidthDownsizer(
  s_params: Axi4Params = Axi4Params(dataWidth = 64),
  m_params: Axi4Params = Axi4Params(dataWidth = 32)
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle(s_params))
    val m_axi = new Axi4Bundle(m_params)
  })

  val s_addr_index = log2Ceil(s_params.dataWidth / 8)
  val m_addr_index = log2Ceil(m_params.dataWidth / 8)

  val aw_address_offset = io.s_axi.aw.bits.addr(s_addr_index-1, m_addr_index)
  val ar_address_offset = io.s_axi.ar.bits.addr(s_addr_index-1, m_addr_index)
  val reg_aw_address_offset = RegInit(0.U((aw_address_offset.getWidth).W))
  val reg_ar_address_offset = RegInit(0.U((ar_address_offset.getWidth).W))

  when (io.m_axi.aw.fire) {
    reg_aw_address_offset := aw_address_offset
  }
  when (io.m_axi.ar.fire) {
    reg_ar_address_offset := ar_address_offset
  }

  // connect the signals as usual
  io.m_axi.aw <> io.s_axi.aw
  io.m_axi.w <> io.s_axi.w
  io.s_axi.b <> io.m_axi.b
  io.m_axi.ar <> io.s_axi.ar
  io.s_axi.r <> io.m_axi.r

  // handle bit shifting to align to the wider bus boundary
  io.m_axi.w.bits.data := Mux(
    io.s_axi.aw.fire,
    io.s_axi.w.bits.data >> (m_params.dataWidth.U * aw_address_offset),
    io.s_axi.w.bits.data >> (m_params.dataWidth.U * reg_aw_address_offset)
  )
  io.m_axi.w.bits.strb := Mux(
    io.s_axi.aw.fire,
    io.s_axi.w.bits.strb >> ((m_params.dataWidth.U / 8.U) * aw_address_offset),
    io.s_axi.w.bits.strb >> ((m_params.dataWidth.U / 8.U) * reg_aw_address_offset)
  )
  io.s_axi.r.bits.data := Mux(
    io.m_axi.ar.fire,
    io.m_axi.r.bits.data << (m_params.dataWidth.U * ar_address_offset),
    io.m_axi.r.bits.data << (m_params.dataWidth.U * reg_ar_address_offset)
  )
}