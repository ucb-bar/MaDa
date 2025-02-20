import chisel3._
import chisel3.util._

import CsrControlConstants._
import Instructions._
import ScalarControlConstants._



class InstructionFetch extends Module {
  val io = IO(new Bundle {
    val reset_vector = Input(UInt(32.W))
    val stall = Input(Bool())
    
    val pc_sel = Input(UInt(3.W))
    val branch_target = Input(UInt(32.W))
    val jump_target = Input(UInt(32.W))
    val jump_reg_target = Input(UInt(32.W))
    val exception_target = Input(UInt(32.W))

    val imem = new Axi4LiteBundle()
    
    val ex_pc = Output(UInt(32.W))
    val ex_inst = Output(UInt(32.W))
  })

  val reg_pc = RegInit(io.reset_vector)

  // pipeline register to execute stage
  val reg_ex_pc = RegInit(0.U(32.W))
  
  val pc_next = Wire(UInt(32.W))
  val pc_plus4 = Wire(UInt(32.W))
  
  pc_plus4 := reg_pc + 4.U

  when (!io.stall) {
    reg_pc := pc_next
    reg_ex_pc := reg_pc
  }

  
  val tmp_exception = false.B

  pc_next := MuxCase(pc_plus4, Seq(
                  (io.pc_sel === PC_4)  -> pc_plus4,
                  (io.pc_sel === PC_BR) -> io.branch_target,
                  (io.pc_sel === PC_J) -> io.jump_target,
                  (io.pc_sel === PC_JR) -> io.jump_reg_target,
                  (io.pc_sel === PC_EXC)-> io.exception_target
                  ))

  
  
  io.imem.aw.valid := false.B
  io.imem.aw.bits.addr := 0.U
  io.imem.w.valid := false.B
  io.imem.w.bits.strb := 0.U
  io.imem.w.bits.data := 0.U
  io.imem.b.ready := false.B
  io.imem.ar.valid := true.B
  io.imem.ar.bits.addr := reg_pc
  io.imem.r.ready := true.B


  // Instruction memory buffer; if the core is stalled and a multi-cycle request arrives, save it in the buffer and supply it to the pipeline once
  // the execution is resumed

  val ex_inst_buffer = RegInit(0.U(32.W))
  val ex_inst_buffer_valid = RegInit(false.B)
  

  when (io.stall) {
    when (io.imem.r.fire && !ex_inst_buffer_valid) {
      ex_inst_buffer := io.imem.r.bits.data
      ex_inst_buffer_valid := true.B
    }
  }
  .otherwise {
    ex_inst_buffer := 0.U(32.W)
    ex_inst_buffer_valid := false.B
  }


  io.ex_pc := reg_ex_pc

  io.ex_inst := Mux(ex_inst_buffer_valid, ex_inst_buffer, io.imem.r.bits.data)
}