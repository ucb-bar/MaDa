package delta

import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4Bundle, AxResponse, AxSize, AxBurst}

import Instructions._
import ScalarControlConstants._
import SimdControlConstants._


class SimdLoadStore(
  val nVectors: Int = 1,
) extends Module {
  val io = IO(new Bundle {
    val mem_func = Input(UInt(M_X.getWidth.W))
    val strided = Input(UInt(STRIDE_X.getWidth.W))

    val addr = Input(UInt(32.W))
    val wdata = Input(Vec(nVectors, UInt(32.W)))
    val rdata = Output(Vec(nVectors, UInt(32.W)))
    val busy = Output(Bool())

    val dmem = new Axi4Bundle(params=Axi4Params(dataWidth=nVectors*32))
  })

  val reg_aw_pending = RegInit(false.B)
  val reg_w_pending = RegInit(false.B)
  val reg_b_pending = RegInit(false.B)
  val reg_ar_pending = RegInit(false.B)
  val reg_r_pending = RegInit(false.B)


  when (io.dmem.aw.fire) {
    reg_aw_pending := false.B
  }
  .elsewhen (io.mem_func === M_WR && !reg_w_pending && !reg_b_pending) {
    reg_aw_pending := true.B
  }

  when (io.dmem.w.fire) {
    reg_w_pending := false.B
    reg_b_pending := true.B
  }
  .elsewhen (io.mem_func === M_WR && !reg_b_pending) {
    reg_w_pending := true.B
  }

  when (io.dmem.ar.fire) {
    reg_ar_pending := false.B
    reg_r_pending := true.B
  }
  .elsewhen (io.mem_func === M_RD && !reg_r_pending) {
    reg_ar_pending := true.B
  }

  when (io.dmem.b.fire) {
    reg_b_pending := false.B
  }
  when (io.dmem.r.fire) {
    reg_r_pending := false.B
  }
  

  dontTouch(reg_aw_pending)
  dontTouch(reg_w_pending)
  dontTouch(reg_r_pending)

  
  io.dmem.aw.valid := reg_aw_pending
  io.dmem.aw.bits.id := 0.U
  io.dmem.aw.bits.addr := io.addr
  io.dmem.aw.bits.len := 0.U
  nVectors match {
    case 1 => io.dmem.aw.bits.size := AxSize.S_4_BYTES
    case 2 => io.dmem.aw.bits.size := AxSize.S_8_BYTES
    case 4 => io.dmem.aw.bits.size := AxSize.S_16_BYTES
    case 8 => io.dmem.aw.bits.size := AxSize.S_32_BYTES
    case 16 => io.dmem.aw.bits.size := AxSize.S_64_BYTES
    case 32 => io.dmem.aw.bits.size := AxSize.S_128_BYTES
    case _ => 
      throw new IllegalArgumentException(s"Unsupported number of vectors: $nVectors")
  }
  io.dmem.aw.bits.burst := AxBurst.INCR
  
  io.dmem.w.valid := reg_w_pending
  io.dmem.w.bits.strb := ~0.U(((nVectors*32)/8).W)
  io.dmem.w.bits.data := Cat(io.wdata.reverse)
  io.dmem.w.bits.last := true.B
  
  io.dmem.b.ready := true.B
  
  io.dmem.ar.valid := reg_ar_pending
  io.dmem.ar.bits.id := 0.U
  io.dmem.ar.bits.addr := io.addr
  io.dmem.ar.bits.len := 0.U
  nVectors match {
    case 1 => io.dmem.ar.bits.size := AxSize.S_4_BYTES
    case 2 => io.dmem.ar.bits.size := AxSize.S_8_BYTES
    case 4 => io.dmem.ar.bits.size := AxSize.S_16_BYTES
    case 8 => io.dmem.ar.bits.size := AxSize.S_32_BYTES
    case 16 => io.dmem.ar.bits.size := AxSize.S_64_BYTES
    case 32 => io.dmem.ar.bits.size := AxSize.S_128_BYTES
    case _ => 
      throw new IllegalArgumentException(s"Unsupported number of vectors: $nVectors")
  }
  io.dmem.ar.bits.burst := AxBurst.INCR

  dontTouch(io.dmem.ar.bits.size)
  dontTouch(io.dmem.ar.bits.burst)
  
  io.dmem.r.ready := true.B

  // stall entire pipeline on I$ or D$ miss
  // val imem_transaction_pending = !io.dat.imem_resp_valid
  // val dmem_transaction_pending = c_mem_en && !(ex_reg_dmem_pending /*|| io.dat.data_misaligned)*/)
  io.busy := (io.mem_func =/= M_X || reg_aw_pending || reg_w_pending || reg_b_pending || reg_ar_pending || reg_r_pending) && !(io.dmem.r.fire || io.dmem.b.fire)
  
  // @see Axi4WidthUpsizer.scala
  val s_addr_index = log2Ceil(32 / 8)
  val bus_addr_index = log2Ceil(nVectors*32 / 8)

  if (nVectors == 1) {
    io.rdata(0) := io.dmem.r.bits.data
  }
  else {
    val addr_offset = io.dmem.ar.bits.addr(bus_addr_index-1, s_addr_index)
    // Split the wide AXI read data into individual 32-bit vectors
    for (i <- 0 until nVectors) {
      io.rdata(i) := Mux(io.strided === STRIDE_0, io.dmem.r.bits.data >> (32.U * addr_offset), io.dmem.r.bits.data((i + 1) * 32 - 1, i * 32))
    }
  }
}