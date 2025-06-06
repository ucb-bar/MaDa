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
  
  val exception = Wire(Bool())
  exception := false.B

  val interrupt = Wire(Bool())
  interrupt := false.B

  val eret = Wire(Bool())
  eret := false.B



  val if_pc_sel = Wire(UInt(3.W))


  val ex_wb_en = Wire(Bool())

  val ex_vwb_en = Wire(Bool())


  
  // Instruction Fetch (IF) Stage
  val if_branch_target = Wire(UInt(32.W))
  val if_jump_target = Wire(UInt(32.W))
  val if_jump_reg_target = Wire(UInt(32.W))
  val if_exception_target = Wire(UInt(32.W))


  val ex_wb_data = Wire(UInt(config.XLEN.W))

  val ex_vwb_data = Wire(Vec(numVectors, UInt(config.ELEN.W)))


  val ifu = Module(new InstructionFetch())
  val idu = Module(new InstructionDecode())
  val csr = Module(new CSR())
  val alu = Module(new ALU())
  val lsu = Module(new LoadStore())


  // ================================================================
  //   Instruction Fetch (IF) Stage
  // ================================================================
  ifu.io.reset_vector := io.reset_vector
  
  // redirect the PC stream if the PC selection signal is not PC_4
  ifu.io.redirected := if_pc_sel =/= PC_4
  ifu.io.redirected_pc := MuxCase(0.U, Seq(
    (if_pc_sel === PC_BR) -> if_branch_target,
    (if_pc_sel === PC_J) -> if_jump_target,
    (if_pc_sel === PC_JR) -> if_jump_reg_target,
    (if_pc_sel === PC_EXC) -> if_exception_target
  ))
  
  // we should only update the architectural states when both the instruction
  // is valid and the backend is ready to process the instruction
  val commit = ifu.io.ex.fire
  
  ifu.io.imem <> io.imem


  // ================================================================
  //   Execute (EX) Stage
  // ================================================================

  // Instruction Decode (ID)
  idu.io.instruction := ifu.io.ex.bits.inst

  val ctrl = idu.io.control_signals
  

  // Execute (EX)
  val inst = ifu.io.ex.bits.inst

  // Register File
  val regfile = Mem(32, UInt(32.W))
  
  val rs1_addr = inst(RS1_MSB, RS1_LSB)
  val rs2_addr = inst(RS2_MSB, RS2_LSB)
  val rd_addr  = inst(RD_MSB,  RD_LSB)

  dontTouch(rs1_addr)
  dontTouch(rs2_addr)
  dontTouch(rd_addr)

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
  

  when(ex_wb_en && (rd_addr =/= 0.U)) {
    regfile(rd_addr) := ex_wb_data
  }
  
  val rs1_data = Mux((rs1_addr =/= 0.U), regfile(rs1_addr), 0.U)
  val rs2_data = Mux((rs2_addr =/= 0.U), regfile(rs2_addr), 0.U)


  // Immediates
  val imm_i = inst(31,20)
  val imm_s = Cat(inst(31,25), inst(11,7))
  val imm_b = Cat(inst(31), inst(7), inst(30,25), inst(11,8))
  val imm_u = inst(31,12)
  val imm_j = Cat(inst(31), inst(19,12), inst(20), inst(30,21))
  val imm_z = Cat(Fill(27, 0.U), inst(19,15))

  // sign-extend immediates
  val imm_i_sext = Cat(Fill(20, imm_i(11)), imm_i)
  val imm_s_sext = Cat(Fill(20, imm_s(11)), imm_s)
  val imm_b_sext = Cat(Fill(19, imm_b(11)), imm_b, 0.U)
  val imm_u_sext = Cat(imm_u, Fill(12, 0.U))
  val imm_j_sext = Cat(Fill(11, imm_j(19)), imm_j, 0.U)


  // ALU
  alu.io.op1 := MuxCase(0.U, Seq(
              (ctrl.alu_op1_sel === OP1_RS1) -> rs1_data,
              (ctrl.alu_op1_sel === OP1_IMU) -> imm_u_sext,
              (ctrl.alu_op1_sel === OP1_IMZ) -> imm_z
              )).asUInt

  alu.io.op2 := MuxCase(0.U, Seq(
              (ctrl.alu_op2_sel === OP2_RS2) -> rs2_data,
              (ctrl.alu_op2_sel === OP2_PC)  -> ifu.io.ex.bits.pc,
              (ctrl.alu_op2_sel === OP2_IMI) -> imm_i_sext,
              (ctrl.alu_op2_sel === OP2_IMS) -> imm_s_sext
              )).asUInt

  alu.io.func := ctrl.alu_func
  
  
  // Branch Logic
  val br_eq  = (rs1_data === rs2_data)
  val br_lt  = (rs1_data.asSInt < rs2_data.asSInt)
  val br_ltu = (rs1_data.asUInt < rs2_data.asUInt)
  
  val ctrl_pc_sel_no_exception = Mux(interrupt, 
    PC_EXC, 
    MuxCase(PC_4, Seq(
      (ctrl.branch_type === BR_X) -> PC_4,
      (ctrl.branch_type === BR_NE && !br_eq) -> PC_BR,
      (ctrl.branch_type === BR_EQ && br_eq) -> PC_BR,
      (ctrl.branch_type === BR_GE && !br_lt) -> PC_BR,
      (ctrl.branch_type === BR_GEU && !br_ltu) -> PC_BR,
      (ctrl.branch_type === BR_LT && br_lt) -> PC_BR,
      (ctrl.branch_type === BR_LTU && br_ltu) -> PC_BR,
      (ctrl.branch_type === BR_J) -> PC_J,
      (ctrl.branch_type === BR_JR) -> PC_JR,
      )
    )
  )

  if_pc_sel := Mux(exception || eret, PC_EXC, ctrl_pc_sel_no_exception)

  // Branch / Jump Target Calculation
  if_branch_target   := ifu.io.ex.bits.pc + imm_b_sext
  if_jump_target     := ifu.io.ex.bits.pc + imm_j_sext
  if_jump_reg_target := (rs1_data.asUInt + imm_i_sext.asUInt) & ~1.U(32.W)
  if_exception_target := 0.U

  dontTouch(if_branch_target)
  dontTouch(if_jump_target)
  dontTouch(if_jump_reg_target)


  // Control Status Registers
  // csr commit point
  csr.io.command := Mux(commit, ctrl.csr_cmd, CSR_N)
  csr.io.in_data := alu.io.out
  csr.io.addr := inst(CSR_ADDR_MSB, CSR_ADDR_LSB)
  csr.io.retire := commit

  io.debug.syscall0 := csr.io.debug.syscall0
  io.debug.syscall1 := csr.io.debug.syscall1
  io.debug.syscall2 := csr.io.debug.syscall2
  io.debug.syscall3 := csr.io.debug.syscall3
  csr.io.debug.sysresp0 := io.debug.sysresp0
  csr.io.debug.sysresp1 := io.debug.sysresp1
  csr.io.debug.sysresp2 := io.debug.sysresp2
  csr.io.debug.sysresp3 := io.debug.sysresp3



  // Vector Register File
  val vregfile = Mem(32, Vec(numVectors, UInt(config.ELEN.W)))

  when(ex_vwb_en) {
    vregfile(rd_addr) := ex_vwb_data
  }
  
  val vrs1_data = vregfile(rs1_addr)
  val vrs2_data = vregfile(rs2_addr)
  val vrd_data = vregfile(rd_addr)


  val valu = Module(new SimdFloatingPoint(ELEN=config.ELEN, VLEN=config.VLEN, pipelineStages=config.pipelineStages))
  // result = a * b + c

  valu.io.func := ctrl.valu_func
  valu.io.op1 := vrs1_data
  valu.io.op2 := vrs2_data
  valu.io.op3 := vrd_data

  
  // Memory Access (MEM)
  lsu.io.addr := alu.io.out
  lsu.io.mem_func := Mux(ifu.io.ex.valid, ctrl.mem_func, M_X)
  lsu.io.wdata := rs2_data
  lsu.io.ctl_dmem_mask_sel := ctrl.mem_mask
  lsu.io.ctl_dmem_signed := ctrl.mem_signed

  lsu.io.dmem <> io.dmem


  val vlsu = Module(new SimdLoadStore(nVectors=numVectors))

  vlsu.io.mem_func := Mux(ifu.io.ex.valid, ctrl.vmem_func, M_X)
  vlsu.io.strided := ctrl.vmem_stride
  vlsu.io.addr := alu.io.out
  vlsu.io.wdata := vrd_data

  vlsu.io.dmem <> io.vdmem


  // backpressure the IF stage if we are busy with data memory
  ifu.io.ex.ready := !(lsu.io.busy || vlsu.io.busy || valu.io.busy)


  // Write Back (WB)
  // write back commit point
  ex_wb_en := Mux(commit, ctrl.wb_en, false.B)
  ex_vwb_en := Mux(commit, ctrl.vwb_en, false.B)

  dontTouch(ex_wb_en)
  dontTouch(ex_vwb_en)

  // write back mux
  ex_wb_data := MuxCase(0.U, Seq(
              (ctrl.wb_sel === WB_ALU) -> alu.io.out,
              (ctrl.wb_sel === WB_MEM) -> lsu.io.rdata,
              (ctrl.wb_sel === WB_PC4) -> (ifu.io.ex.bits.pc + 4.U),
              (ctrl.wb_sel === WB_CSR) -> csr.io.out_data
  ))

  ex_vwb_data := MuxCase(VecInit.fill(numVectors)(0.U(config.ELEN.W)), Seq(
              (ctrl.vwb_sel === WB_ALU) -> valu.io.out,
              (ctrl.vwb_sel === WB_MEM) -> vlsu.io.rdata,
  ))






  
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

