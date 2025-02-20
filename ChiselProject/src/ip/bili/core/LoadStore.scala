import chisel3._
import chisel3.util._

import Instructions._
import ScalarControlConstants._


class LoadStore extends Module {
  val io = IO(new Bundle {

    val mem_func = Input(UInt(M_X.getWidth.W))

    val ctl_dmem_mask_sel = Input(UInt(MSK_X.getWidth.W))
    val ctl_dmem_signed = Input(Bool())

    val addr = Input(UInt(32.W))
    val wdata = Input(UInt(32.W))

    val dmem = new Axi4LiteBundle()

    val busy = Output(Bool())

    val rdata = Output(UInt(32.W))
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

  
  
  io.dmem.aw.valid := reg_aw_pending
  
  io.dmem.w.valid := reg_w_pending
  
  io.dmem.ar.valid := reg_ar_pending

  io.dmem.b.ready := true.B
  io.dmem.r.ready := true.B


  dontTouch(reg_aw_pending)
  dontTouch(reg_w_pending)
  dontTouch(reg_r_pending)


  // stall entire pipeline on I$ or D$ miss
  // val imem_transaction_pending = !io.dat.imem_resp_valid
  // val dmem_transaction_pending = c_mem_en && !(ex_reg_dmem_pending /*|| io.dat.data_misaligned)*/)
  io.busy := (io.mem_func =/= M_X || reg_aw_pending || reg_w_pending || reg_b_pending || reg_ar_pending || reg_r_pending) && !(io.dmem.r.fire || io.dmem.b.fire)






  
  val dmem_strb = Wire(UInt(4.W))
  val dmem_wdata = Wire(UInt(32.W))

  dmem_strb := MuxCase(0.U, Seq(
    ((io.ctl_dmem_mask_sel === MSK_W)) -> "b1111".U,
    ((io.ctl_dmem_mask_sel === MSK_H) && (io.addr(1) === 0.U)) -> "b0011".U,
    ((io.ctl_dmem_mask_sel === MSK_H) && (io.addr(1) === 1.U)) -> "b1100".U,
    ((io.ctl_dmem_mask_sel === MSK_B) && (io.addr(1,0) === 0.U)) -> "b0001".U,
    ((io.ctl_dmem_mask_sel === MSK_B) && (io.addr(1,0) === 1.U)) -> "b0010".U,
    ((io.ctl_dmem_mask_sel === MSK_B) && (io.addr(1,0) === 2.U)) -> "b0100".U,
    ((io.ctl_dmem_mask_sel === MSK_B) && (io.addr(1,0) === 3.U)) -> "b1000".U,
  ))

  val dmem_wdata_shamt = MuxCase(0.U, Seq(
    ((io.ctl_dmem_mask_sel === MSK_W)) -> 0.U,
    ((io.ctl_dmem_mask_sel === MSK_H) && (io.addr(1) === 0.U)) -> 0.U,
    ((io.ctl_dmem_mask_sel === MSK_H) && (io.addr(1) === 1.U)) -> 16.U,
    ((io.ctl_dmem_mask_sel === MSK_B) && (io.addr(1,0) === 0.U)) -> 0.U,
    ((io.ctl_dmem_mask_sel === MSK_B) && (io.addr(1,0) === 1.U)) -> 8.U,
    ((io.ctl_dmem_mask_sel === MSK_B) && (io.addr(1,0) === 2.U)) -> 16.U,
    ((io.ctl_dmem_mask_sel === MSK_B) && (io.addr(1,0) === 3.U)) -> 24.U
  ))

  dmem_wdata := (io.wdata << dmem_wdata_shamt).asUInt

  
  
  io.dmem.aw.bits.addr := io.addr
  // io.dmem.aw.valid := io.ctl.dmem.aw.valid
  // io.ctl.dmem.aw.ready := io.dmem.aw.ready

  io.dmem.w.bits.strb := dmem_strb
  io.dmem.w.bits.data := dmem_wdata
  // io.dmem.w.valid := io.ctl.dmem.w.valid
  // io.ctl.dmem.w.ready := io.dmem.w.ready

  // io.ctl.dmem.b.valid := io.dmem.b.valid
  // io.dmem.b.ready := io.ctl.dmem.b.ready


  // datapath to data memory outputs
  io.dmem.ar.bits.addr := io.addr
  // io.dmem.ar.valid := io.ctl.dmem.ar.valid
  // io.ctl.dmem.ar.ready := io.dmem.ar.ready
  
  // io.ctl.dmem.r.valid := io.dmem.r.valid
  // io.dmem.r.ready := io.ctl.dmem.r.ready

  
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
      (io.ctl_dmem_mask_sel === MSK_W) -> dmem_rdata_w,
      // unsigned half-word
      ((io.ctl_dmem_mask_sel === MSK_H) && (io.addr(1) === 0.U)) -> Mux(io.ctl_dmem_signed, dmem_rdata_h_0_sext, dmem_rdata_h_0),
      ((io.ctl_dmem_mask_sel === MSK_H) && (io.addr(1) === 1.U)) -> Mux(io.ctl_dmem_signed, dmem_rdata_h_2_sext, dmem_rdata_h_2),
      // byte
      ((io.ctl_dmem_mask_sel === MSK_B) && (io.addr(1,0) === 0.U)) -> Mux(io.ctl_dmem_signed, dmem_rdata_b_0_sext, dmem_rdata_b_0),
      ((io.ctl_dmem_mask_sel === MSK_B) && (io.addr(1,0) === 1.U)) -> Mux(io.ctl_dmem_signed, dmem_rdata_b_1_sext, dmem_rdata_b_1),
      ((io.ctl_dmem_mask_sel === MSK_B) && (io.addr(1,0) === 2.U)) -> Mux(io.ctl_dmem_signed, dmem_rdata_b_2_sext, dmem_rdata_b_2),
      ((io.ctl_dmem_mask_sel === MSK_B) && (io.addr(1,0) === 3.U)) -> Mux(io.ctl_dmem_signed, dmem_rdata_b_3_sext, dmem_rdata_b_3),
      ))
}