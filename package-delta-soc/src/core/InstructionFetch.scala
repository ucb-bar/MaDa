package delta

import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4LiteBundle}

import CsrControlConstants._
import Instructions._
import ScalarControlConstants._


/**
  * Instruction Fetch Unit
  *
  * This module is responsible for fetching instructions from the instruction
  * memory and sending them to the execution stage.
  */
class InstructionFetch extends Module {
  val io = IO(new Bundle {
    // PC reset vector
    val reset_vector = Input(UInt(32.W))

    // decoupled control signals to execution stage
    val ex = new DecoupledIO(new Bundle {
      val pc = UInt(32.W)
      val inst = UInt(32.W)
    })

    // interface to redirect the PC stream
    val redirected = Flipped(new ValidIO(new Bundle {
      val pc = UInt(32.W)
    }))

    // instruction memory interface
    val imem = new Axi4LiteBundle()
  })


  /* ================================ */
  /*  Pipeline Registers              */
  /* ================================ */
  // Program Counter
  val if_reg_pc = RegInit(io.reset_vector)

  val ex_reg_valid = RegInit(false.B)
  val ex_reg_pc = Reg(UInt(32.W))
  val ex_reg_inst = Reg(UInt(32.W))

  val reg_reset_stall = RegInit(true.B)
  reg_reset_stall := false.B

  /* ================================ */
  /*  PC Update Logic                 */
  /* ================================ */
  // next PC jump address
  val if_pc_next = Wire(UInt(32.W))

  // PC + 4
  val if_pc_plus4 = if_reg_pc + 4.U

  // current instruction is valid if either the previously stored instruction
  // is valid or the new instruction from the memory is valid
  val instruction_valid = io.imem.r.valid && !io.redirected.valid

  // if frontend or backend is not ready for next instruction, stall the PC update
  val stall = !io.ex.fire

  // update PC based on stall and redirect conditions
  // ordering is important here, redirect has higher priority
  when (io.redirected.valid) {
    // if branch/jump/exception is taken, redirect the PC stream
    if_pc_next := io.redirected.bits.pc
  }
  .elsewhen (stall) {
    // stall the PC update to wait for the instruction / backend to be ready
    if_pc_next := if_reg_pc
  }
  .otherwise {
    // follow normal instruction stream, proceed to next PC
    if_pc_next := if_pc_plus4
  }

  // update PC register
  if_reg_pc := if_pc_next


  /* ================================ */
  /*  Instruction Memory Interface    */
  /* ================================ */
  // ignore write interface
  io.imem.aw := DontCare
  io.imem.w := DontCare
  io.imem.b := DontCare

  // request new instruction if instruction buffer is not updated
  io.imem.ar.valid := !reg_reset_stall
  io.imem.ar.bits.addr := if_pc_next
  io.imem.r.ready := true.B

  /* ================================ */
  /*  Function Unit Control Signals   */
  /* ================================ */
  when (io.ex.ready) {
    ex_reg_valid := instruction_valid
    ex_reg_pc := if_reg_pc
    ex_reg_inst := io.imem.r.bits.data
  }
  
  // output to EX stage
  io.ex.valid := ex_reg_valid
  io.ex.bits.pc := ex_reg_pc
  // BUBBLE is used to handle power-on situation, where both the instruction buffer
  // and the instruction memory are not valid
  // otherwise, select the instruction from either the instruction buffer or the
  // instruction memory
  io.ex.bits.inst := Mux(ex_reg_valid, ex_reg_inst, RiscvConstants.BUBBLE)
}