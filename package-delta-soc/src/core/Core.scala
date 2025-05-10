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
  
  // whether the instruction in the current decode stage
  // is valid or should be killed
  val kill_id = Wire(Bool())
  
  // whether the instruction in the current execute stage
  // is valid or should be killed
  val kill_ex = Wire(Bool())
  
  // whether the instruction in the current memory stage
  // is valid or should be killed
  val kill_mem = Wire(Bool())

  // whether the instruction in the current write back stage
  // is valid or should be killed
  val kill_wb = Wire(Bool())

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


  // backpressure the IF stage if we are busy with data memory
  stalled := (lsu.io.busy || vlsu.io.busy || valu.io.busy)
  
  // kill the instruction in the decode stage if either the instruction is not valid
  // or the backend is busy
  kill_id := !ifu.io.ex.fire // || csr.io.interrupt
  
  // in two-stage pipeline, everything is conbinationally connected
  kill_wb := kill_mem
  kill_mem := kill_ex
  kill_ex := kill_id


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
  //   Instruction Decode (ID) Stage
  // ================================================================
  val id_inst = ifu.io.ex.bits.inst
  ifu.io.ex.ready := !stalled

  idu.io.instruction := id_inst
  
  val id_ctrl = idu.io.control_signals

  val id_rs1_addr = id_inst(RS1_MSB, RS1_LSB)
  val id_rs2_addr = id_inst(RS2_MSB, RS2_LSB)
  val id_rd_addr  = id_inst(RD_MSB,  RD_LSB)
  dontTouch(id_rs1_addr)
  dontTouch(id_rs2_addr)
  dontTouch(id_rd_addr)


  // register file
  val id_rs1_data = Mux((id_rs1_addr =/= 0.U), regfile(id_rs1_addr), 0.U)
  val id_rs2_data = Mux((id_rs2_addr =/= 0.U), regfile(id_rs2_addr), 0.U)

  // vector register file
  val id_vrs1_data = vregfile(id_rs1_addr)
  val id_vrs2_data = vregfile(id_rs2_addr)
  val id_vrs3_data = vregfile(id_rd_addr)

  
  // ================================================================
  //   Execute (EX) Stage
  // ================================================================
  val ex_ctrl = id_ctrl
  val ex_inst = id_inst
  
  val ex_rs1_data = id_rs1_data
  val ex_rs2_data = id_rs2_data

  val ex_vrs1_data = id_vrs1_data
  val ex_vrs2_data = id_vrs2_data
  val ex_vrs3_data = id_vrs3_data
  
  val ex_wb_en = ex_ctrl.wb_en
  val ex_vwb_en = ex_ctrl.vwb_en
  val ex_alu_out = alu.io.out
  val ex_valu_out = valu.io.out


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
              (ex_ctrl.alu_op2_sel === OP2_PC)  -> ifu.io.ex.bits.pc,
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
  if_branch_target   := ifu.io.ex.bits.pc + imm_b_sext
  if_jump_target     := ifu.io.ex.bits.pc + imm_j_sext
  if_jump_reg_target := (ex_rs1_data.asUInt + imm_i_sext.asUInt) & ~1.U(32.W)
  if_exception_target := 0.U

  dontTouch(if_branch_target)
  dontTouch(if_jump_target)
  dontTouch(if_jump_reg_target)



  // ================================================================
  //   Memory Access (MEM) Stage
  // ================================================================  
  val mem_ctrl = ex_ctrl
  val mem_inst = ex_inst

  val mem_rs2_data = ex_rs2_data
  val vmem_rs3_data = ex_vrs3_data
  
  val mem_alu_out = ex_alu_out
  val mem_valu_out = ex_valu_out
  val mem_read_out = lsu.io.rdata
  val mem_vread_out = vlsu.io.rdata

  val mem_wb_en = ex_wb_en
  val mem_vwb_en = ex_vwb_en
  
  lsu.io.ex.bits.addr := ex_alu_out
  lsu.io.ex.bits.mem_func := mem_ctrl.mem_func
  lsu.io.ex.bits.wdata := ex_rs2_data
  lsu.io.ex.valid := ifu.io.ex.valid && ex_ctrl.mem_en
  lsu.io.ex.bits.ctl_dmem_mask_sel := mem_ctrl.mem_mask
  lsu.io.ex.bits.ctl_dmem_signed := mem_ctrl.mem_signed

  lsu.io.dmem <> io.dmem

  // vlsu.io.mem_func := Mux(kill_mem, M_X, mem_ctrl.vmem_func)
  vlsu.io.mem_func := Mux(ifu.io.ex.valid, mem_ctrl.vmem_func, M_X)
  vlsu.io.strided := mem_ctrl.vmem_stride
  vlsu.io.addr := ex_alu_out
  vlsu.io.wdata := ex_vrs3_data

  vlsu.io.dmem <> io.vdmem


  // ================================================================
  //   Write Back (WB) Stage
  // ================================================================
  val wb_ctrl = mem_ctrl
  val wb_inst = mem_inst
  val wb_csr_addr = wb_inst(CSR_ADDR_MSB, CSR_ADDR_LSB)
  val wb_rd_addr = wb_inst(RD_MSB, RD_LSB)
  
  val wb_alu_out = mem_alu_out
  val wb_valu_out = mem_valu_out
  val wb_read_out = mem_read_out
  val wb_vread_out = mem_vread_out

  // write back commit point
  commit := !kill_wb
  
  // regfile write commits
  val wb_wb_en = mem_wb_en && commit
  val wb_vwb_en = mem_vwb_en && commit
  
  // CSR write commits
  csr.io.command := Mux(commit, wb_ctrl.csr_cmd, CSR_N)
  csr.io.retire := commit
  
  
  dontTouch(wb_wb_en)
  dontTouch(wb_vwb_en)

  // write back mux
  val wb_wb_data = MuxCase(0.U, Seq(
              (wb_ctrl.wb_sel === WB_ALU) -> wb_alu_out,
              (wb_ctrl.wb_sel === WB_MEM) -> wb_read_out,
              (wb_ctrl.wb_sel === WB_PC4) -> (ifu.io.ex.bits.pc + 4.U),
              (wb_ctrl.wb_sel === WB_CSR) -> csr.io.out_data
  ))

  val wb_vwb_data = MuxCase(VecInit.fill(numVectors)(0.U(config.ELEN.W)), Seq(
              (wb_ctrl.vwb_sel === WB_ALU) -> wb_valu_out,
              (wb_ctrl.vwb_sel === WB_MEM) -> wb_vread_out,
  ))
  
  // register file write
  when(wb_wb_en && (wb_rd_addr =/= 0.U)) {
    regfile(wb_rd_addr) := wb_wb_data
  }
  when(wb_vwb_en) {
    vregfile(wb_rd_addr) := wb_vwb_data
  }

  // csr signal connections
  csr.io.in_data := alu.io.out
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

