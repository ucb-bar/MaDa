package delta

import chisel3._
import chisel3.util._

import CsrControlConstants._
import Instructions._
import ScalarControlConstants._
import SimdControlConstants._


class ControlSignals extends Bundle {
  val valid_inst = Bool()
  val branch_type = UInt(BR_X.getWidth.W)
  val alu_op1_sel = UInt(OP1_X.getWidth.W)
  val alu_op2_sel = UInt(OP2_X.getWidth.W)
  val alu_func = UInt(ALU_X.getWidth.W)
  val mem_en = Bool()
  val mem_func = UInt(M_X.getWidth.W)
  val mem_mask = UInt(MSK_X.getWidth.W)
  val mem_signed = Bool()
  val wb_sel = UInt(WB_X.getWidth.W)
  val wb_en = Bool()
  val csr_cmd = UInt(CSR_N.getWidth.W)

  val valu_op1_sel = UInt(OP1_X.getWidth.W)
  val valu_op2_sel = UInt(OP2_X.getWidth.W)
  val valu_func = UInt(ALU_X.getWidth.W)
  val vmem_en = Bool()
  val vmem_func = UInt(M_X.getWidth.W)
  val vmem_stride = UInt(STRIDE_X.getWidth.W)
  val vwb_sel = UInt(WB_X.getWidth.W)
  val vwb_en = Bool()
}

class InstructionDecode extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))

    val control_signals = Output(new ControlSignals)
  })
  

  dontTouch(io.control_signals.valid_inst)
  dontTouch(io.control_signals.branch_type)
  dontTouch(io.control_signals.alu_op1_sel)
  dontTouch(io.control_signals.alu_op2_sel)
  dontTouch(io.control_signals.alu_func)
  dontTouch(io.control_signals.mem_func)
  dontTouch(io.control_signals.mem_mask)
  dontTouch(io.control_signals.mem_signed)
  dontTouch(io.control_signals.wb_sel)
  dontTouch(io.control_signals.wb_en)
  dontTouch(io.control_signals.csr_cmd)

  dontTouch(io.control_signals.valu_op1_sel)
  dontTouch(io.control_signals.valu_op2_sel)
  dontTouch(io.control_signals.valu_func)
  dontTouch(io.control_signals.vmem_func)
  dontTouch(io.control_signals.vmem_stride)
  dontTouch(io.control_signals.vwb_sel)
  dontTouch(io.control_signals.vwb_en)

  val control_signals = ListLookup(
    io.instruction,
                /* | valid | BR     | ALU     | ALU     | ALU      | mem | mem  | mem   | sign |        | WB | CSR   | */
                /* | inst? | type   | op1 sel | op2 sel | function | en  | op   | size  | ext? | WB sel | en | cmd   | */
                   // default values
                   List( F , BR_X   , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , X    , WB_X   , F  , CSR_N ),
    Array(
      LW        -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_ADD  , T   , M_RD , MSK_W , T    , WB_MEM , T  , CSR_N ),
      LB        -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_ADD  , T   , M_RD , MSK_B , T    , WB_MEM , T  , CSR_N ),
      LBU       -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_ADD  , T   , M_RD , MSK_B , F    , WB_MEM , T  , CSR_N ),
      LH        -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_ADD  , T   , M_RD , MSK_H , T    , WB_MEM , T  , CSR_N ),
      LHU       -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_ADD  , T   , M_RD , MSK_H , F    , WB_MEM , T  , CSR_N ),
      SW        -> List( T , BR_X   , OP1_RS1 , OP2_IMS , ALU_ADD  , T   , M_WR , MSK_W , T    , WB_X   , F  , CSR_N ),
      SB        -> List( T , BR_X   , OP1_RS1 , OP2_IMS , ALU_ADD  , T   , M_WR , MSK_B , T    , WB_X   , F  , CSR_N ),
      SH        -> List( T , BR_X   , OP1_RS1 , OP2_IMS , ALU_ADD  , T   , M_WR , MSK_H , T    , WB_X   , F  , CSR_N ),
      AUIPC     -> List( T , BR_X   , OP1_IMU , OP2_PC  , ALU_ADD  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      LUI       -> List( T , BR_X   , OP1_IMU , OP2_X   , ALU_COPY , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      ADDI      -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_ADD  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      ANDI      -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_AND  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      ORI       -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_OR   , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      XORI      -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_XOR  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      SLTI      -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_SLT  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      SLTIU     -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_SLTU , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      SLLI_RV32 -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_SLL  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      SRAI_RV32 -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_SRA  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      SRLI_RV32 -> List( T , BR_X   , OP1_RS1 , OP2_IMI , ALU_SRL  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      SLL       -> List( T , BR_X   , OP1_RS1 , OP2_RS2 , ALU_SLL  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      ADD       -> List( T , BR_X   , OP1_RS1 , OP2_RS2 , ALU_ADD  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      SUB       -> List( T , BR_X   , OP1_RS1 , OP2_RS2 , ALU_SUB  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      SLT       -> List( T , BR_X   , OP1_RS1 , OP2_RS2 , ALU_SLT  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      SLTU      -> List( T , BR_X   , OP1_RS1 , OP2_RS2 , ALU_SLTU , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      AND       -> List( T , BR_X   , OP1_RS1 , OP2_RS2 , ALU_AND  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      OR        -> List( T , BR_X   , OP1_RS1 , OP2_RS2 , ALU_OR   , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      XOR       -> List( T , BR_X   , OP1_RS1 , OP2_RS2 , ALU_XOR  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      SRA       -> List( T , BR_X   , OP1_RS1 , OP2_RS2 , ALU_SRA  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      SRL       -> List( T , BR_X   , OP1_RS1 , OP2_RS2 , ALU_SRL  , F   , M_X  , MSK_X , F    , WB_ALU , T  , CSR_N ),
      JAL       -> List( T , BR_J   , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_PC4 , T  , CSR_N ),
      JALR      -> List( T , BR_JR  , OP1_RS1 , OP2_IMI , ALU_X    , F   , M_X  , MSK_X , F    , WB_PC4 , T  , CSR_N ),
      BEQ       -> List( T , BR_EQ  , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_N ),
      BNE       -> List( T , BR_NE  , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_N ),
      BGE       -> List( T , BR_GE  , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_N ),
      BGEU      -> List( T , BR_GEU , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_N ),
      BLT       -> List( T , BR_LT  , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_N ),
      BLTU      -> List( T , BR_LTU , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_N ),
      CSRRWI    -> List( T , BR_X   , OP1_IMZ , OP2_X   , ALU_COPY , F   , M_X  , MSK_X , F    , WB_CSR , T  , CSR_W ),
      CSRRSI    -> List( T , BR_X   , OP1_IMZ , OP2_X   , ALU_COPY , F   , M_X  , MSK_X , F    , WB_CSR , T  , CSR_S ),
      CSRRW     -> List( T , BR_X   , OP1_RS1 , OP2_X   , ALU_COPY , F   , M_X  , MSK_X , F    , WB_CSR , T  , CSR_W ),
      CSRRS     -> List( T , BR_X   , OP1_RS1 , OP2_X   , ALU_COPY , F   , M_X  , MSK_X , F    , WB_CSR , T  , CSR_S ),
      CSRRC     -> List( T , BR_X   , OP1_RS1 , OP2_X   , ALU_COPY , F   , M_X  , MSK_X , F    , WB_CSR , T  , CSR_C ),
      CSRRCI    -> List( T , BR_X   , OP1_IMZ , OP2_X   , ALU_COPY , F   , M_X  , MSK_X , F    , WB_CSR , T  , CSR_C ),
      ECALL     -> List( T , BR_X   , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_I ),
      MRET      -> List( T , BR_X   , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_I ),
      DRET      -> List( T , BR_X   , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_I ),
      EBREAK    -> List( T , BR_X   , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_I ),
      WFI       -> List( T , BR_X   , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_N ),  // implemented as a NOP
      FENCE_I   -> List( T , BR_X   , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_N ),
      FENCE     -> List( T , BR_X   , OP1_X   , OP2_X   , ALU_X    , F   , M_X  , MSK_X , F    , WB_X   , F  , CSR_N ),  // we are already sequentially consistent, so no need to honor the fence instruction
    )
  )

  val vector_control_signals = ListLookup(
    io.instruction,
                /* | valid | VALU    | VALU    | VALU      | mem | mem  | memory   |        | WB | */
                /* | inst? | op1 sel | op2 sel | function  | en  | op   | stride   | WB sel | en | */
                   // default values
                   List( F , OP1_X   , OP2_X   , SIMD_X    , F   , M_X  , STRIDE_X , WB_X   , F  ),
    Array(

      VADD_VV   -> List( T , OP1_RS1 , OP2_RS2 , SIMD_ADD  , F   , M_X  , STRIDE_X , WB_ALU , T  ),
      VADD_VF   -> List( T , OP1_RS1 , OP2_RS2 , SIMD_ADD  , F   , M_X  , STRIDE_X , WB_ALU , T  ),
      VXOR_VV   -> List( T , OP1_RS1 , OP2_RS2 , SIMD_XOR  , F   , M_X  , STRIDE_X , WB_ALU , T  ),
      VFMIN_VV  -> List( T , OP1_RS1 , OP2_RS2 , SIMD_MIN  , F   , M_X  , STRIDE_X , WB_ALU , T  ),
      VFMAX_VV  -> List( T , OP1_RS1 , OP2_RS2 , SIMD_MAX  , F   , M_X  , STRIDE_X , WB_ALU , T  ),
      VFMUL_VV  -> List( T , OP1_RS1 , OP2_RS2 , SIMD_MUL  , F   , M_X  , STRIDE_X , WB_ALU , T  ),
      VFMACC_VV -> List( T , OP1_RS1 , OP2_RS2 , SIMD_MACC , F   , M_X  , STRIDE_X , WB_ALU , T  ),
      VLE32_V   -> List( T , OP1_X   , OP2_X   , SIMD_X    , T   , M_RD , STRIDE_X , WB_MEM , T  ),
      VLSE32_V  -> List( T , OP1_X   , OP2_X   , SIMD_X    , T   , M_RD , STRIDE_0 , WB_MEM , T  ),  // used as broadcast load
      VSE32_V   -> List( T , OP1_X   , OP2_X   , SIMD_X    , T   , M_WR , STRIDE_X , WB_MEM , F  ),
    )
  )

  // Put these control signals in variables
  val ((c_valid_base_inst: Bool)
   :: c_branch_type
   :: c_alu_op1_sel
   :: c_alu_op2_sel
   :: c_alu_func
   :: c_mem_en
   :: c_mem_func
   :: c_mem_mask
   :: (c_mem_signed: Bool)
   :: c_wb_sel
   :: (c_wb_en: Bool)
   :: c_csr_cmd
   :: Nil) = control_signals

  // Put these control signals in variables
  val ((c_valid_vector_inst: Bool)
   :: c_valu_op1_sel
   :: c_valu_op2_sel
   :: c_valu_func
   :: c_vmem_en
   :: c_vmem_func
   :: c_vmem_stride
   :: c_vwb_sel
   :: (c_vwb_en: Bool)
   :: Nil) = vector_control_signals

  io.control_signals.valid_inst := c_valid_base_inst || c_valid_vector_inst
  io.control_signals.branch_type := c_branch_type
  io.control_signals.alu_op1_sel := c_alu_op1_sel
  io.control_signals.alu_op2_sel := c_alu_op2_sel
  io.control_signals.alu_func := c_alu_func
  io.control_signals.mem_en := c_mem_en
  io.control_signals.mem_func := c_mem_func
  io.control_signals.mem_mask := c_mem_mask
  io.control_signals.mem_signed := c_mem_signed
  io.control_signals.wb_sel := c_wb_sel
  io.control_signals.wb_en := c_wb_en
  io.control_signals.csr_cmd := c_csr_cmd

  io.control_signals.valu_op1_sel := c_valu_op1_sel
  io.control_signals.valu_op2_sel := c_valu_op2_sel
  io.control_signals.valu_func := c_valu_func
  io.control_signals.vmem_en := c_vmem_en
  io.control_signals.vmem_func := c_vmem_func
  io.control_signals.vmem_stride := c_vmem_stride
  io.control_signals.vwb_sel := c_vwb_sel
  io.control_signals.vwb_en := c_vwb_en
}
