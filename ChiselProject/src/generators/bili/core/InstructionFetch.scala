import chisel3._
import chisel3.util._

import CsrControlConstants._
import Instructions._
import ScalarControlConstants._



class InstructionFetch extends Module {
  val io = IO(new Bundle {
    val reset_vector = Input(UInt(32.W))


    val ex = new DecoupledIO(new Bundle {
      val pc = UInt(32.W)
      val inst = UInt(32.W)
    })

    // if branch/jump/exception is taken, redirect the PC stream
    val redirected = Input(Bool())
    val redirected_pc = Input(UInt(32.W))
    
    // instruction memory interface
    val imem = new Axi4LiteBundle()
  })

  // === Pipeline Stage Registers ===
  val if_reg_pc = RegInit(io.reset_vector)

  val ex_reg_pc = Reg(UInt(32.W))

  // instruction buffer
  val reg_inst_buffer = Reg(UInt(32.W))
  val reg_inst_buffer_valid = RegInit(false.B)


  // === Next PC Stage ===
  val if_pc_next = Wire(UInt(32.W))
  val if_next_valid = Wire(Bool())
  if_next_valid := false.B

  val if_pc_plus4 = if_reg_pc + 4.U



  // stall IF/EXE if backend is not ready
  val stall = !(io.ex.fire) || !io.imem.r.fire


  // update PC based on stall and redirect conditions
  when (stall) {
    if_pc_next := if_reg_pc
  }
  .elsewhen (io.redirected) {
    if_pc_next := io.redirected_pc
  }
  .otherwise {
    if_pc_next := if_pc_plus4
  }
  if_reg_pc := if_pc_next

  ex_reg_pc := if_reg_pc


  io.ex.bits.pc := if_reg_pc
  io.ex.bits.inst := Mux(reg_inst_buffer_valid, reg_inst_buffer, io.imem.r.bits.data)
  io.ex.valid := (reg_inst_buffer_valid || io.imem.r.fire) && !io.redirected


  
  dontTouch(if_reg_pc)
  dontTouch(ex_reg_pc)
  dontTouch(if_pc_next)
  dontTouch(if_next_valid)
  dontTouch(if_pc_plus4)
  dontTouch(reg_inst_buffer)
  dontTouch(reg_inst_buffer_valid)


  
  // if_buffer_in.valid := io.imem.r.valid
  // if_buffer_in.bits := io.imem.r.bits.data
  // // if the incoming inst goes to buffer, don't send the next request
  // if_next_valid := io.cpu_resp.ready || (if_buffer_in.ready && !io.imem.r.valid)
  // assert(if_buffer_in.ready || !if_buffer_in.valid, "Inst buffer overflow")
  // val if_buffer_out = Queue(if_buffer_in, entries = 1, pipe = false, flow = true)

  
  // if_pc_next := if_pc_plus4
  // when (io.cpu_req.valid) {
  //   // datapath is redirecting the PC stream (mis-speculation)
  //   if_reg_redirected := true.B
  //   if_reg_redirected_pc := io.cpu_req.bits.pc
  // }
  // when (if_reg_redirected) {
  //   if_pc_next := if_reg_redirected_pc
  // }

  // // go to next PC if both CPU and imem are ready, and the memory response
  // // for the current PC already arrived
  // val if_reg_pc_responded = RegInit(false.B)
  // val if_pc_responded = if_reg_pc_responded || io.imem.r.valid
  // when (io.cpu_resp.ready && io.imem.ar.ready && if_pc_responded) {
  //   if_reg_pc_responded := false.B
  //   if_reg_pc := if_pc_next
  //   when (!io.cpu_req.valid) {
  //     if_reg_redirected := false.B
  //   }
  // }
  // .elsewhen (io.imem.r.valid) {
  //   if_reg_pc_responded := true.B
  // }

  io.imem.aw := DontCare
  io.imem.w := DontCare
  io.imem.b := DontCare

  // set up outputs to instruction memory
  // io.imem.ar.valid := true.B // 
  io.imem.ar.valid := !(reg_inst_buffer_valid && !io.ex.ready)

  io.imem.ar.bits.addr := if_pc_next
  io.imem.r.ready := true.B


  // === Instruction fetch / return stage ===
  when (io.imem.r.fire && !io.ex.fire) {
    reg_inst_buffer := io.imem.r.bits.data
    reg_inst_buffer_valid := true.B
  }

  when (io.ex.fire) {
    reg_inst_buffer_valid := false.B
  }

  // when (io.ex_kill) {
  //   ex_reg_valid := false.B
  // }
  // .elsewhen (io.cpu_resp.valid) {
  //   ex_reg_valid := if_buffer_out.valid && !io.cpu_req.valid && !if_reg_redirected
  //   ex_reg_pc := if_reg_pc
  //   ex_reg_inst := if_buffer_out.bits
  // }

  // // Execute Stage
  // io.cpu_resp.valid := ex_reg_valid
  // io.cpu_resp.bits.pc := ex_reg_pc
  // io.cpu_resp.bits.inst := ex_reg_inst

}