package delta

import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4Bundle, ChannelAx, ChannelW, ChannelR}


class Axi4MemoryWithLatency(
  val params: Axi4Params = Axi4Params(),
  val memoryFileHex: String = "",
  val memoryFileBin: String = "",
  val readLatency: Int = 2,
  val writeLatency: Int = 2,
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle(params))
  })

  // delay the signals by the specified latency
  val aw_delay = Module(new Pipe(new ChannelAx(params), writeLatency))
  val w_delay = Module(new Pipe(new ChannelW(params), writeLatency))
  val r_delay = Module(new Pipe(new ChannelR(params), readLatency))

  // gate off the ready signal when the memory is busy
  val reg_aw_pending = RegInit(false.B)
  val reg_ar_pending = RegInit(false.B)

  when (io.s_axi.aw.fire) {
    reg_aw_pending := true.B
  }
  .elsewhen (io.s_axi.b.fire) {
    reg_aw_pending := false.B
  }

  when (io.s_axi.ar.fire) {
    reg_ar_pending := true.B
  }
  .elsewhen (io.s_axi.r.fire) {
    reg_ar_pending := false.B
  }

  dontTouch(reg_aw_pending)
  dontTouch(reg_ar_pending)
  dontTouch(io.s_axi.ar.ready)
    

  val mem = Module(new Axi4Memory(
    params = params,
    memoryFileHex = memoryFileHex,
    memoryFileBin = memoryFileBin
  ))

  aw_delay.io.enq.bits <> io.s_axi.aw.bits
  aw_delay.io.enq.valid := io.s_axi.aw.valid
  mem.io.s_axi.aw.bits <> aw_delay.io.deq.bits
  mem.io.s_axi.aw.valid := aw_delay.io.deq.valid
  io.s_axi.aw.ready := mem.io.s_axi.aw.ready && !reg_aw_pending

  w_delay.io.enq.bits <> io.s_axi.w.bits
  w_delay.io.enq.valid := io.s_axi.w.valid
  mem.io.s_axi.w.bits <> w_delay.io.deq.bits
  mem.io.s_axi.w.valid := w_delay.io.deq.valid
  io.s_axi.w.ready := mem.io.s_axi.w.ready && !reg_aw_pending

  io.s_axi.b <> mem.io.s_axi.b

  io.s_axi.ar <> mem.io.s_axi.ar
  io.s_axi.ar.ready := mem.io.s_axi.ar.ready && !reg_ar_pending
  mem.io.s_axi.ar.valid := io.s_axi.ar.valid && !reg_ar_pending

  r_delay.io.enq.bits <> mem.io.s_axi.r.bits
  r_delay.io.enq.valid := mem.io.s_axi.r.valid
  io.s_axi.r.bits <> r_delay.io.deq.bits
  io.s_axi.r.valid := r_delay.io.deq.valid
  mem.io.s_axi.r.ready := io.s_axi.r.ready
}

class Axi4MemoryForTest extends Axi4MemoryWithLatency(
  params = Axi4Params(
    addressWidth = 8,
    dataWidth = 32,
    idWidth = 0,
  ),
  readLatency = 1,
  writeLatency = 1,
)