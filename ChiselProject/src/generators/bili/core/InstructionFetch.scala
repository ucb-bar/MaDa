import chisel3._
import chisel3.util._

import CsrControlConstants._
import Instructions._
import ScalarControlConstants._



class InstructionFetch extends Module {
  val io = IO(new Bundle {
    // PC reset vector
    val reset_vector = Input(UInt(32.W))

    // decoupled control signals to function units
    val ex = new DecoupledIO(new Bundle {
      val pc = UInt(32.W)
      val inst = UInt(32.W)
    })

    // interface to redirect the PC stream
    val redirected = Input(Bool())
    val redirected_pc = Input(UInt(32.W))
    
    // instruction memory interface
    val imem = new Axi4LiteBundle()
  })

  /* ================================ */
  /*  Pipeline Registers              */
  /* ================================ */
  // Program Counter
  val if_reg_pc = RegInit(io.reset_vector)

  // Instruction buffer
  val reg_inst_buffer = Reg(UInt(32.W))
  val reg_inst_buffer_valid = RegInit(false.B)


  /* ================================ */
  /*  PC Update Logic                 */
  /* ================================ */
  // next PC jump address
  val if_pc_next = Wire(UInt(32.W))

  // PC + 4
  val if_pc_plus4 = if_reg_pc + 4.U

  // current instruction is valid if either the previously stored instruction
  // is valid or the new instruction from the memory is valid
  val instruction_valid = reg_inst_buffer_valid || io.imem.r.valid

  // stall the PC update if IF or EX is not ready
  val stall = !io.ex.fire

  // update PC based on stall and redirect conditions
  when (io.redirected) {
    // if branch/jump/exception is taken, redirect the PC stream
    if_pc_next := io.redirected_pc
  }
  .elsewhen (stall) {
    // stall the PC update to wait for the instruction / backend to be ready
    if_pc_next := if_reg_pc
  }
  .otherwise {
    // follow normal instruction stream, proceed to next PC
    if_pc_next := if_pc_plus4
  }

  // update PC
  if_reg_pc := if_pc_next


  /* ================================ */
  /*  Instruction Memory Interface    */
  /* ================================ */
  
  // ignore write interface
  io.imem.aw := DontCare
  io.imem.w := DontCare
  io.imem.b := DontCare

  // request new instruction if instruction buffer is not updated
  io.imem.ar.valid := !reg_inst_buffer_valid
  io.imem.ar.bits.addr := if_pc_next
  io.imem.r.ready := true.B

  // update instruction buffer
  when (io.imem.r.fire && !io.ex.fire) {
    reg_inst_buffer := io.imem.r.bits.data
    reg_inst_buffer_valid := true.B
  }

  when (io.ex.fire || io.redirected) {
    reg_inst_buffer_valid := false.B
  }
  

  /* ================================ */
  /*  Function Unit Control Signals   */
  /* ================================ */
  // output to EX stage
  io.ex.bits.pc := if_reg_pc
  
  // BUBBLE is used to handle power-on situation, where both the instruction buffer
  // and the instruction memory are not valid
  // otherwise, select the instruction from either the instruction buffer or the
  // instruction memory
  io.ex.bits.inst := MuxCase(RiscvConstants.BUBBLE, Seq(
    (reg_inst_buffer_valid) -> reg_inst_buffer,
    (io.imem.r.valid) -> io.imem.r.bits.data,
  ))
  io.ex.valid := instruction_valid
}