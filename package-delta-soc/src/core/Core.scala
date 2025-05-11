package delta

import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4Bundle, Axi4LiteBundle}

import CsrControlConstants._
import RiscvConstants._
import Instructions._
import ScalarControlConstants._
import SimdControlConstants._


class DebugIO extends Bundle() {
  val x1 = Output(UInt(32.W))
  val x2 = Output(UInt(32.W))
  val x3 = Output(UInt(32.W))
  val x4 = Output(UInt(32.W))
  val x5 = Output(UInt(32.W))
  val x6 = Output(UInt(32.W))
  val x7 = Output(UInt(32.W))
  val x8 = Output(UInt(32.W))

  val syscall0 = Output(UInt(32.W))
  val syscall1 = Output(UInt(32.W))
  val syscall2 = Output(UInt(32.W))
  val syscall3 = Output(UInt(32.W))
  val sysresp0 = Input(UInt(32.W))
  val sysresp1 = Input(UInt(32.W))
  val sysresp2 = Input(UInt(32.W))
  val sysresp3 = Input(UInt(32.W))
}


case class CoreConfig(
  XLEN: Int = 32,
  ELEN: Int = 32,
  VLEN: Int = 64,
  pipelineStages: Int = 1,
)

class Core(
  val config: CoreConfig = CoreConfig()
) extends Module {
  val io = IO(new Bundle {
    val reset_vector = Input(UInt(32.W))

    val imem = new Axi4LiteBundle()
    val dmem = new Axi4Bundle()
    val vdmem = new Axi4Bundle(params=Axi4Params(dataWidth=config.VLEN))
    val debug = new DebugIO()
  })

  val numVectors = config.VLEN / config.ELEN
  

  // ================================================================
  //   Global control signals
  // ================================================================
  val exception = Wire(Bool())
  exception := false.B

  val interrupt = Wire(Bool())
  interrupt := false.B

  val eret = Wire(Bool())
  eret := false.B

  // control whether the execution pipeline will proceed 
  // forward to the next stage
  val stalled = Wire(Bool())
  
  // whether the instruction in the current EX stage
  // is valid or should be killed
  val kill_ex = Wire(Bool())

  // whether the instruction in the current EX stage should
  // be replayed or we can proceed forward
  val replay_ex = Wire(Bool())
  
  // whether the instruction in the current memory stage
  // is valid or should be killed
  val kill_wb = RegInit(false.B)

  // whether the instruction in the current write back stage
  // is valid or should be killed
  val replay_wb = Wire(Bool())

  // we should only update the architectural states when both the 
  // instruction and data at the write back stage are valid
  val commit = Wire(Bool())

  // ================================================================
  //   Modules
  // ================================================================
  val ifu = Module(new InstructionFetch())
  val idu = Module(new InstructionDecode())
  
  val regfile = Mem(32, UInt(32.W))
  val vregfile = Mem(32, Vec(numVectors, UInt(config.ELEN.W)))

  val alu = Module(new ALU())
  val lsu = Module(new LoadStore())
  val valu = Module(new SimdFloatingPoint(ELEN=config.ELEN, VLEN=config.VLEN, pipelineStages=config.pipelineStages))
  val vlsu = Module(new SimdLoadStore(nVectors=numVectors))
  
  val csr = Module(new CSR())


  
  dontTouch(stalled)
  dontTouch(kill_ex)
  dontTouch(replay_ex)
  dontTouch(kill_wb)
  dontTouch(replay_wb)

  replay_ex := lsu.io.ex_busy
  replay_wb := lsu.io.wb_busy
  

  // backpressure the IF stage if we are busy with data memory
  stalled := !ifu.io.ex.fire
  
  // kill the instruction in the decode stage if either the instruction is not valid
  kill_ex := !ifu.io.ex.valid
  
  when (!stalled) {
    kill_wb := kill_ex
  }

  // ================================================================
  //   Instruction Fetch (IF) Stage
  // ================================================================
  val if_pc_sel = Wire(UInt(3.W))
  val if_branch_target = Wire(UInt(32.W))
  val if_jump_target = Wire(UInt(32.W))
  val if_jump_reg_target = Wire(UInt(32.W))
  val if_exception_target = Wire(UInt(32.W))

  ifu.io.reset_vector := io.reset_vector
  
  // redirect the PC stream if the PC selection signal is not PC_4
  ifu.io.redirected.valid := if_pc_sel =/= PC_4
  ifu.io.redirected.bits.pc := MuxCase(0.U, Seq(
    (if_pc_sel === PC_BR) -> if_branch_target,
    (if_pc_sel === PC_J) -> if_jump_target,
    (if_pc_sel === PC_JR) -> if_jump_reg_target,
    (if_pc_sel === PC_EXC) -> if_exception_target
  ))
  
  ifu.io.imem <> io.imem


  
  // ================================================================
  //   Execute (EX) Stage
  // ================================================================

  // Instruction Decode (ID) Stage
  val ex_inst = ifu.io.ex.bits.inst
  val ex_pc = ifu.io.ex.bits.pc
  
  ifu.io.ex.ready := !(replay_ex || replay_wb)
  idu.io.instruction := ex_inst
  
  val ex_ctrl = idu.io.control_signals
  dontTouch(ex_ctrl)

  val ex_rs1_addr = ex_inst(RS1_MSB, RS1_LSB)
  val ex_rs2_addr = ex_inst(RS2_MSB, RS2_LSB)
  val ex_rd_addr  = ex_inst(RD_MSB,  RD_LSB)
  dontTouch(ex_rs1_addr)
  dontTouch(ex_rs2_addr)
  dontTouch(ex_rd_addr)
  
  // forward the result from the write back stage to the rs1 and rs2
  val forward_rs1 = Wire(Bool())
  val forward_rs2 = Wire(Bool())
  
  val wb_wb_data = Wire(UInt(32.W))
  val wb_vwb_data = Wire(Vec(numVectors, UInt(config.ELEN.W)))

  // register file
  val ex_rs1_data = Mux(forward_rs1, wb_wb_data, Mux((ex_rs1_addr =/= 0.U), regfile(ex_rs1_addr), 0.U))
  val ex_rs2_data = Mux(forward_rs2, wb_wb_data, Mux((ex_rs2_addr =/= 0.U), regfile(ex_rs2_addr), 0.U))

  // vector register file
  val ex_vrs1_data = vregfile(ex_rs1_addr)
  val ex_vrs2_data = vregfile(ex_rs2_addr)
  val ex_vrs3_data = vregfile(ex_rd_addr)

  // Execute (EX) Stage
  val ex_wb_en = ex_ctrl.wb_en
  val ex_vwb_en = ex_ctrl.vwb_en
  val ex_alu_out = alu.io.out
  val ex_valu_out = valu.io.out
  dontTouch(ex_wb_en)
  dontTouch(ex_vwb_en)
  dontTouch(ex_alu_out)
  dontTouch(ex_valu_out)


  // debug signal connections
  io.debug.x1 := regfile(1)
  io.debug.x2 := regfile(2)
  io.debug.x3 := regfile(3)
  io.debug.x4 := regfile(4)
  io.debug.x5 := regfile(5)
  io.debug.x6 := regfile(6)
  io.debug.x7 := regfile(7)
  io.debug.x8 := regfile(8)
  dontTouch(io.debug)
  
  // Immediates
  val imm_i = ex_inst(31,20)
  val imm_s = Cat(ex_inst(31,25), ex_inst(11,7))
  val imm_b = Cat(ex_inst(31), ex_inst(7), ex_inst(30,25), ex_inst(11,8))
  val imm_u = ex_inst(31,12)
  val imm_j = Cat(ex_inst(31), ex_inst(19,12), ex_inst(20), ex_inst(30,21))
  val imm_z = Cat(Fill(27, 0.U), ex_inst(19,15))

  // sign-extend immediates
  val imm_i_sext = Cat(Fill(20, imm_i(11)), imm_i)
  val imm_s_sext = Cat(Fill(20, imm_s(11)), imm_s)
  val imm_b_sext = Cat(Fill(19, imm_b(11)), imm_b, 0.U)
  val imm_u_sext = Cat(imm_u, Fill(12, 0.U))
  val imm_j_sext = Cat(Fill(11, imm_j(19)), imm_j, 0.U)


  // ALU
  alu.io.func := ex_ctrl.alu_func
  alu.io.op1 := MuxCase(0.U, Seq(
              (ex_ctrl.alu_op1_sel === OP1_RS1) -> ex_rs1_data,
              (ex_ctrl.alu_op1_sel === OP1_IMU) -> imm_u_sext,
              (ex_ctrl.alu_op1_sel === OP1_IMZ) -> imm_z
              )).asUInt

  alu.io.op2 := MuxCase(0.U, Seq(
              (ex_ctrl.alu_op2_sel === OP2_RS2) -> ex_rs2_data,
              (ex_ctrl.alu_op2_sel === OP2_PC)  -> ex_pc,
              (ex_ctrl.alu_op2_sel === OP2_IMI) -> imm_i_sext,
              (ex_ctrl.alu_op2_sel === OP2_IMS) -> imm_s_sext
              )).asUInt

  // Vector ALU
  valu.io.func := ex_ctrl.valu_func
  valu.io.op1 := ex_vrs1_data
  valu.io.op2 := ex_vrs2_data
  valu.io.op3 := ex_vrs3_data

  
  // Branch Logic
  val br_eq  = (ex_rs1_data === ex_rs2_data)
  val br_lt  = (ex_rs1_data.asSInt < ex_rs2_data.asSInt)
  val br_ltu = (ex_rs1_data.asUInt < ex_rs2_data.asUInt)
  
  val ex_pc_sel_no_exception = Mux(interrupt, 
    PC_EXC, 
    MuxCase(PC_4, Seq(
      (ex_ctrl.branch_type === BR_X) -> PC_4,
      (ex_ctrl.branch_type === BR_NE && !br_eq) -> PC_BR,
      (ex_ctrl.branch_type === BR_EQ && br_eq) -> PC_BR,
      (ex_ctrl.branch_type === BR_GE && !br_lt) -> PC_BR,
      (ex_ctrl.branch_type === BR_GEU && !br_ltu) -> PC_BR,
      (ex_ctrl.branch_type === BR_LT && br_lt) -> PC_BR,
      (ex_ctrl.branch_type === BR_LTU && br_ltu) -> PC_BR,
      (ex_ctrl.branch_type === BR_J) -> PC_J,
      (ex_ctrl.branch_type === BR_JR) -> PC_JR,
      )
    )
  )

  if_pc_sel := Mux(exception || eret, PC_EXC, ex_pc_sel_no_exception)

  // Branch / Jump Target Calculation
  if_branch_target   := ex_pc + imm_b_sext
  if_jump_target     := ex_pc + imm_j_sext
  if_jump_reg_target := (ex_rs1_data.asUInt + imm_i_sext.asUInt) & ~1.U(32.W)
  if_exception_target := 0.U

  dontTouch(if_branch_target)
  dontTouch(if_jump_target)
  dontTouch(if_jump_reg_target)


  // ================================================================
  //   Write Back (WB) Stage
  // ================================================================

  // Memory Access (MEM) Stage
  val wb_inst = RegInit(RiscvConstants.BUBBLE)
  val wb_pc = Reg(UInt(32.W))
  val wb_ctrl = Reg(new ControlSignals())
  dontTouch(wb_inst)
  dontTouch(wb_ctrl)
  
  val wb_rs2_data = Reg(UInt(32.W))
  val wb_vrs3_data = Reg(Vec(numVectors, UInt(config.ELEN.W)))
  val wb_alu_out = Reg(UInt(32.W))
  val wb_valu_out = Reg(Vec(numVectors, UInt(config.ELEN.W)))

  when (!stalled) {
    wb_inst := ex_inst
    wb_pc := ex_pc
    wb_ctrl := ex_ctrl

    wb_rs2_data := ex_rs2_data
    wb_vrs3_data := ex_vrs3_data
    wb_alu_out := ex_alu_out
    wb_valu_out := ex_valu_out
  }
  
  lsu.io.ex.bits.addr := ex_alu_out
  lsu.io.ex.bits.mem_func := ex_ctrl.mem_func
  lsu.io.ex.bits.wdata := ex_rs2_data
  lsu.io.ex.valid := !kill_ex && ex_ctrl.mem_en
  lsu.io.ex.bits.ctl_dmem_mask_sel := ex_ctrl.mem_mask
  lsu.io.ex.bits.ctl_dmem_signed := ex_ctrl.mem_signed

  lsu.io.wb.bits.addr := wb_alu_out
  lsu.io.wb.valid := !kill_wb && wb_ctrl.mem_en
  lsu.io.wb.bits.mem_func := wb_ctrl.mem_func
  lsu.io.wb.bits.ctl_dmem_mask_sel := wb_ctrl.mem_mask
  lsu.io.wb.bits.ctl_dmem_signed := wb_ctrl.mem_signed
  
  lsu.io.dmem <> io.dmem

  // vlsu.io.mem_func := Mux(kill_mem, M_X, mem_ctrl.vmem_func)
  vlsu.io.mem_func := Mux(ifu.io.ex.valid, ex_ctrl.vmem_func, M_X)
  vlsu.io.strided := ex_ctrl.vmem_stride
  vlsu.io.addr := ex_alu_out
  vlsu.io.wdata := ex_vrs3_data

  vlsu.io.dmem <> io.vdmem

  // Write Back (WB) Stage
  val wb_csr_addr = wb_inst(CSR_ADDR_MSB, CSR_ADDR_LSB)
  val wb_rd_addr = wb_inst(RD_MSB, RD_LSB)
  dontTouch(wb_wb_data)
  dontTouch(wb_vwb_data)
  dontTouch(wb_csr_addr)
  dontTouch(wb_rd_addr)


  // whether we need to forward the result from the write back stage to the rs1 and rs2
  forward_rs1 := (ex_rs1_addr === wb_rd_addr) && wb_ctrl.wb_en && (wb_rd_addr =/= 0.U)
  forward_rs2 := (ex_rs2_addr === wb_rd_addr) && wb_ctrl.wb_en && (wb_rd_addr =/= 0.U)


  // write back commit point
  commit := !kill_wb && !replay_wb && !stalled
  
  // regfile write commits
  when(commit && wb_ctrl.wb_en && (wb_rd_addr =/= 0.U)) {
    regfile(wb_rd_addr) := wb_wb_data
  }
  when(commit && wb_ctrl.vwb_en) {
    vregfile(wb_rd_addr) := wb_vwb_data
  }
  
  // CSR write commits
  csr.io.command := Mux(commit, wb_ctrl.csr_cmd, CSR_N)
  csr.io.retire := commit

  // write back mux
  wb_wb_data := MuxCase(0.U, Seq(
              (wb_ctrl.wb_sel === WB_ALU) -> wb_alu_out,
              (wb_ctrl.wb_sel === WB_MEM) -> lsu.io.rdata,
              (wb_ctrl.wb_sel === WB_PC4) -> (wb_pc + 4.U),
              (wb_ctrl.wb_sel === WB_CSR) -> csr.io.out_data
  ))

  wb_vwb_data := MuxCase(VecInit.fill(numVectors)(0.U(config.ELEN.W)), Seq(
              (wb_ctrl.vwb_sel === WB_ALU) -> wb_valu_out,
              (wb_ctrl.vwb_sel === WB_MEM) -> vlsu.io.rdata,
  ))

  // csr signal connections
  csr.io.in_data := wb_alu_out
  csr.io.addr := wb_csr_addr

  io.debug.syscall0 := csr.io.debug.syscall0
  io.debug.syscall1 := csr.io.debug.syscall1
  io.debug.syscall2 := csr.io.debug.syscall2
  io.debug.syscall3 := csr.io.debug.syscall3
  csr.io.debug.sysresp0 := io.debug.sysresp0
  csr.io.debug.sysresp1 := io.debug.sysresp1
  csr.io.debug.sysresp2 := io.debug.sysresp2
  csr.io.debug.sysresp3 := io.debug.sysresp3







  
  // // DebugModule
  //  io.ddpath.rdata := regfile(io.ddpath.addr)
  //  when(io.ddpath.validreq){
  //    regfile(io.ddpath.addr) := io.ddpath.wdata
  //  }

  // // Instruction misalignment detection
  // // In control path, instruction misalignment exception is always raised in the next cycle once the misaligned instruction reaches
  // // execution stage, regardless whether the pipeline stalls or not
  // io.dat.inst_misaligned :=  (exe_br_target(1, 0).orR       && io.ctl.pc_sel_no_xept === PC_BR) ||
  //                           (exe_jmp_target(1, 0).orR      && io.ctl.pc_sel_no_xept === PC_J)  ||
  //                           (exe_jump_reg_target(1, 0).orR && io.ctl.pc_sel_no_xept === PC_JR)
  // tval_inst_ma := MuxCase(0.U, Array(
  //               (io.ctl.pc_sel_no_xept === PC_BR) -> exe_br_target,
  //               (io.ctl.pc_sel_no_xept === PC_J)  -> exe_jmp_target,
  //               (io.ctl.pc_sel_no_xept === PC_JR) -> exe_jump_reg_target
  //               ))

  // csr.io.tval := MuxCase(0.U, Array(
  //               (io.ctl.exception_cause === Causes.illegal_instruction.U)     -> exe_reg_inst,
  //               (io.ctl.exception_cause === Causes.misaligned_fetch.U)  -> tval_inst_ma,
  //               (io.ctl.exception_cause === Causes.misaligned_store.U) -> tval_data_ma,
  //               (io.ctl.exception_cause === Causes.misaligned_load.U)  -> tval_data_ma,
  //               ))

}

