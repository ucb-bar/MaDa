import chisel3._
import chisel3.util._

import Instructions._
import ScalarControlConstants._


class SimdLoadStore(
  nVectors: Int = 1
) extends Module {
  val io = IO(new Bundle {
    val mem_func = Input(UInt(M_X.getWidth.W))

    val addr = Input(UInt(32.W))
    val wdata = Input(Vec(nVectors, UInt(32.W)))

    val dmem = new Axi4Bundle(params=Axi4Params(dataWidth=nVectors*32))

    val busy = Output(Bool())

    val rdata = Output(Vec(nVectors, UInt(32.W)))
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
  io.dmem.aw.bits.size := AxSize.S_32_BYTES
  io.dmem.aw.bits.burst := AxBurst.FIXED
  
  io.dmem.w.valid := reg_w_pending
  io.dmem.w.bits.strb := ~0.U(((nVectors*32)/8).W)
  io.dmem.w.bits.data := Cat(io.wdata.reverse)
  io.dmem.w.bits.last := true.B
  
  io.dmem.b.ready := true.B
  
  io.dmem.ar.valid := reg_ar_pending
  io.dmem.ar.bits.id := 0.U
  io.dmem.ar.bits.addr := io.addr
  io.dmem.ar.bits.len := 0.U
  io.dmem.ar.bits.size := AxSize.S_32_BYTES
  io.dmem.ar.bits.burst := AxBurst.FIXED
  
  io.dmem.r.ready := true.B

  // stall entire pipeline on I$ or D$ miss
  // val imem_transaction_pending = !io.dat.imem_resp_valid
  // val dmem_transaction_pending = c_mem_en && !(ex_reg_dmem_pending /*|| io.dat.data_misaligned)*/)
  io.busy := (io.mem_func =/= M_X || reg_aw_pending || reg_w_pending || reg_b_pending || reg_ar_pending || reg_r_pending) && !(io.dmem.r.fire || io.dmem.b.fire)
  
  // Split the wide AXI read data into individual 32-bit vectors
  for (i <- 0 until nVectors) {
    io.rdata(i) := io.dmem.r.bits.data((i + 1) * 32 - 1, i * 32)
  }
}