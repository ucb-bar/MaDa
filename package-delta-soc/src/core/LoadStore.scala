package delta

import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4Bundle, AxResponse, AxSize, AxBurst}

import Instructions._
import ScalarControlConstants._


class LoadStore(val XLEN: Int = 32) extends Module {
  val io = IO(new Bundle {
    val ex = Flipped(ValidIO(new Bundle {
      val mem_func = UInt(M_X.getWidth.W)
      val ctl_dmem_mask_sel = UInt(MSK_X.getWidth.W)
      val ctl_dmem_signed = Bool()
      val addr = UInt(XLEN.W)
      val wdata = UInt(XLEN.W)
    }))
    val ex_busy = Output(Bool())

    val wb = Flipped(ValidIO(new Bundle {
      val mem_func = UInt(M_X.getWidth.W)
      val ctl_dmem_mask_sel = UInt(MSK_X.getWidth.W)
      val ctl_dmem_signed = Bool()
      val addr = UInt(XLEN.W)
    }))
    val rdata = Output(UInt(XLEN.W))
    val wb_busy = Output(Bool())
    
    val dmem = new Axi4Bundle()
  })

  assert(XLEN == 32, "Currently only 32-bit (XLEN = 32) is supported")
  
  val ex_write_request = io.ex.valid && io.ex.bits.mem_func === M_WR
  val wb_write_request = io.wb.valid && io.wb.bits.mem_func === M_WR
  val reg_aw_pending = RegInit(false.B)
  val reg_w_pending = RegInit(false.B)
  val reg_b_pending = RegInit(false.B)
  dontTouch(ex_write_request)
  dontTouch(wb_write_request)
  dontTouch(reg_aw_pending)
  dontTouch(reg_w_pending)
  dontTouch(reg_b_pending)

  val ex_read_request = io.ex.valid && io.ex.bits.mem_func === M_RD
  val wb_read_request = io.wb.valid && io.wb.bits.mem_func === M_RD
  val reg_ar_pending = RegInit(false.B)
  val reg_r_pending = RegInit(false.B)
  dontTouch(ex_read_request)
  dontTouch(wb_read_request)
  dontTouch(reg_ar_pending)
  dontTouch(reg_r_pending)
  
  // stall entire pipeline on I$ or D$ miss
  io.ex_busy := (
    (ex_write_request && (reg_aw_pending && !io.dmem.aw.fire) || (reg_w_pending && !io.dmem.w.fire))
    || (ex_read_request && reg_ar_pending && !io.dmem.ar.fire)
  )
  io.wb_busy := (
    (wb_write_request && reg_b_pending && !io.dmem.b.fire)
    || (wb_read_request && reg_r_pending && !io.dmem.r.fire)
  )

  
  when (ex_write_request && !io.dmem.aw.fire) {
    reg_aw_pending := true.B
  }
  when (io.dmem.aw.fire) {
    reg_aw_pending := false.B
  }
  
  when (ex_write_request && !io.dmem.w.fire) {
    reg_w_pending := true.B
  }
  when (io.dmem.w.fire) {
    reg_w_pending := false.B
  }
  
  when (ex_write_request && !io.dmem.b.fire) {
    reg_b_pending := true.B
  }
  when (io.dmem.b.fire) {
    reg_b_pending := false.B
  }
  
  when (ex_read_request && !io.dmem.ar.fire) {
    reg_ar_pending := true.B
  }
  when (io.dmem.ar.fire) {
    reg_ar_pending := false.B
  }
  
  when (ex_read_request && !io.dmem.r.fire) {
    reg_r_pending := true.B
  }
  when (io.dmem.r.fire) {
    reg_r_pending := false.B
  }

  val dmem_strb = MuxCase(0.U, Seq(
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_W)) -> "b1111".U,
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_H) && (io.ex.bits.addr(1) === 0.U)) -> "b0011".U,
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_H) && (io.ex.bits.addr(1) === 1.U)) -> "b1100".U,
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_B) && (io.ex.bits.addr(1,0) === 0.U)) -> "b0001".U,
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_B) && (io.ex.bits.addr(1,0) === 1.U)) -> "b0010".U,
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_B) && (io.ex.bits.addr(1,0) === 2.U)) -> "b0100".U,
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_B) && (io.ex.bits.addr(1,0) === 3.U)) -> "b1000".U,
  ))
  val dmem_wdata_shamt = MuxCase(0.U, Seq(
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_W)) -> 0.U,
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_H) && (io.ex.bits.addr(1) === 0.U)) -> 0.U,
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_H) && (io.ex.bits.addr(1) === 1.U)) -> 16.U,
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_B) && (io.ex.bits.addr(1,0) === 0.U)) -> 0.U,
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_B) && (io.ex.bits.addr(1,0) === 1.U)) -> 8.U,
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_B) && (io.ex.bits.addr(1,0) === 2.U)) -> 16.U,
    ((io.ex.bits.ctl_dmem_mask_sel === MSK_B) && (io.ex.bits.addr(1,0) === 3.U)) -> 24.U
  ))
  
  io.dmem.aw.valid := ex_write_request
  io.dmem.aw.bits.id := 0.U
  io.dmem.aw.bits.addr := io.ex.bits.addr
  io.dmem.aw.bits.len := 0.U
  io.dmem.aw.bits.size := AxSize.S_4_BYTES
  io.dmem.aw.bits.burst := AxBurst.INCR
  
  io.dmem.w.valid := ex_write_request
  io.dmem.w.bits.strb := dmem_strb
  io.dmem.w.bits.data := (io.ex.bits.wdata << dmem_wdata_shamt).asUInt
  io.dmem.w.bits.last := true.B

  io.dmem.b.ready := true.B
  
  io.dmem.ar.valid := ex_read_request
  io.dmem.ar.bits.id := 0.U
  io.dmem.ar.bits.addr := io.ex.bits.addr
  io.dmem.ar.bits.len := 0.U
  io.dmem.ar.bits.size := AxSize.S_4_BYTES
  io.dmem.ar.bits.burst := AxBurst.INCR
  
  io.dmem.r.ready := true.B
  
  // stage separation
  
  val dmem_rdata_w = io.dmem.r.bits.data
  val dmem_rdata_h_0 = dmem_rdata_w(15,0)
  val dmem_rdata_h_2 = dmem_rdata_w(31,16)
  val dmem_rdata_b_0 = dmem_rdata_w(7,0)
  val dmem_rdata_b_1 = dmem_rdata_w(15,8)
  val dmem_rdata_b_2 = dmem_rdata_w(23,16)
  val dmem_rdata_b_3 = dmem_rdata_w(31,24)
  val dmem_rdata_h_0_sext = Cat(Fill(16, dmem_rdata_h_0(15)), dmem_rdata_h_0)
  val dmem_rdata_h_2_sext = Cat(Fill(16, dmem_rdata_h_2(15)), dmem_rdata_h_2)
  val dmem_rdata_b_0_sext = Cat(Fill(24, dmem_rdata_b_0(7)), dmem_rdata_b_0)
  val dmem_rdata_b_1_sext = Cat(Fill(24, dmem_rdata_b_1(7)), dmem_rdata_b_1)
  val dmem_rdata_b_2_sext = Cat(Fill(24, dmem_rdata_b_2(7)), dmem_rdata_b_2)
  val dmem_rdata_b_3_sext = Cat(Fill(24, dmem_rdata_b_3(7)), dmem_rdata_b_3)


  io.rdata := MuxCase(0.U, Seq(
      (io.wb.bits.ctl_dmem_mask_sel === MSK_W) -> dmem_rdata_w,
      // unsigned half-word
      ((io.wb.bits.ctl_dmem_mask_sel === MSK_H) && (io.wb.bits.addr(1) === 0.U)) -> Mux(io.wb.bits.ctl_dmem_signed, dmem_rdata_h_0_sext, dmem_rdata_h_0),
      ((io.wb.bits.ctl_dmem_mask_sel === MSK_H) && (io.wb.bits.addr(1) === 1.U)) -> Mux(io.wb.bits.ctl_dmem_signed, dmem_rdata_h_2_sext, dmem_rdata_h_2),
      // byte
      ((io.wb.bits.ctl_dmem_mask_sel === MSK_B) && (io.wb.bits.addr(1,0) === 0.U)) -> Mux(io.wb.bits.ctl_dmem_signed, dmem_rdata_b_0_sext, dmem_rdata_b_0),
      ((io.wb.bits.ctl_dmem_mask_sel === MSK_B) && (io.wb.bits.addr(1,0) === 1.U)) -> Mux(io.wb.bits.ctl_dmem_signed, dmem_rdata_b_1_sext, dmem_rdata_b_1),
      ((io.wb.bits.ctl_dmem_mask_sel === MSK_B) && (io.wb.bits.addr(1,0) === 2.U)) -> Mux(io.wb.bits.ctl_dmem_signed, dmem_rdata_b_2_sext, dmem_rdata_b_2),
      ((io.wb.bits.ctl_dmem_mask_sel === MSK_B) && (io.wb.bits.addr(1,0) === 3.U)) -> Mux(io.wb.bits.ctl_dmem_signed, dmem_rdata_b_3_sext, dmem_rdata_b_3),
      ))
}