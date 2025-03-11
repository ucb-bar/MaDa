
import chisel3._
import chisel3.util._
import scala.math._


object RiscvConstants {
   // abstract out instruction decode magic numbers
   val RD_MSB  = 11
   val RD_LSB  = 7
   val RS1_MSB = 19
   val RS1_LSB = 15
   val RS2_MSB = 24
   val RS2_LSB = 20

   val CSR_ADDR_MSB = 31
   val CSR_ADDR_LSB = 20

   val X0 = 0.U

   // The Bubble Instruction (Machine generated NOP)
   // Insert (XOR x0,x0,x0) which is different from software compiler
   // generated NOPs which are (ADDI x0, x0, 0).
   // Reasoning for this is to let visualizers and stat-trackers differentiate
   // between software NOPs and machine-generated Bubbles in the pipeline.
   val BUBBLE  = 0x4033.U(32.W)
}


object ScalarControlConstants {
   //************************************
   // Control Signals

   // Generic Constants
   val T      = true.B
   val F      = false.B
   val X      = false.B  // don't care

   // PC Select Signal
   val PC_4   = 0.asUInt(3.W)  // PC + 4
   val PC_BR  = 1.asUInt(3.W)  // branch_target
   val PC_J   = 2.asUInt(3.W)  // jump_target
   val PC_JR  = 3.asUInt(3.W)  // jump_reg_target
   val PC_EXC = 4.asUInt(3.W)  // exception

   // Branch Type
   val BR_X   = 0.asUInt(4.W) // Next (don't branch)
   val BR_NE  = 1.asUInt(4.W) // Branch on NotEqual
   val BR_EQ  = 2.asUInt(4.W) // Branch on Equal
   val BR_GE  = 3.asUInt(4.W) // Branch on Greater/Equal
   val BR_GEU = 4.asUInt(4.W) // Branch on Greater/Equal Unsigned
   val BR_LT  = 5.asUInt(4.W) // Branch on Less Than
   val BR_LTU = 6.asUInt(4.W) // Branch on Less Than Unsigned
   val BR_J   = 7.asUInt(4.W) // Jump
   val BR_JR  = 8.asUInt(4.W) // Jump Register

   // RS1 Operand Select Signal
   val OP1_RS1 = 0.asUInt(2.W) // Register Source #1
   val OP1_IMU = 1.asUInt(2.W) // immediate, U-type
   val OP1_IMZ = 2.asUInt(2.W) // zero-extended immediate for CSRI instructions
   val OP1_X   = 0.asUInt(2.W)

   // RS2 Operand Select Signal
   val OP2_RS2 = 0.asUInt(3.W) // Register Source #2
   val OP2_PC  = 1.asUInt(3.W) // PC
   val OP2_IMI = 2.asUInt(3.W) // immediate, I-type
   val OP2_IMS = 3.asUInt(3.W) // immediate, S-type
   val OP2_X   = 0.asUInt(3.W)

   // ALU Operation Signal
   val ALU_X    = 0.asUInt(4.W)
   val ALU_ADD  = 1.asUInt(4.W)
   val ALU_SUB  = 2.asUInt(4.W)
   val ALU_SLL  = 3.asUInt(4.W)
   val ALU_SRL  = 4.asUInt(4.W)
   val ALU_SRA  = 5.asUInt(4.W)
   val ALU_AND  = 6.asUInt(4.W)
   val ALU_OR   = 7.asUInt(4.W)
   val ALU_XOR  = 8.asUInt(4.W)
   val ALU_SLT  = 9.asUInt(4.W)
   val ALU_SLTU = 10.asUInt(4.W)
   val ALU_COPY = 11.asUInt(4.W)
      
   // Memory operation
   val M_X  = 0.asUInt(2.W)  // don't care
   val M_RD = 1.asUInt(2.W)   // int load
   val M_WR = 2.asUInt(2.W)   // int store

   // Memory mask size
   val MSK_X = 0.asUInt(2.W)  // don't care
   val MSK_B = 0.asUInt(2.W)  // byte
   val MSK_H = 1.asUInt(2.W)  // half-word
   val MSK_W = 2.asUInt(2.W)  // word
   val MSK_D = 3.asUInt(2.W)  // double-word

   // Writeback Select Signal
   val WB_ALU  = 0.asUInt(2.W)
   val WB_MEM  = 1.asUInt(2.W)
   val WB_PC4  = 2.asUInt(2.W)
   val WB_CSR  = 3.asUInt(2.W)
   val WB_X    = 0.asUInt(2.W)
}

object SimdControlConstants {
  val SIMD_X    = 0.asUInt(3.W)
  val SIMD_ADD  = 1.asUInt(3.W)
  val SIMD_MIN  = 2.asUInt(3.W)
  val SIMD_MAX  = 3.asUInt(3.W)
  val SIMD_MUL  = 4.asUInt(3.W)
  val SIMD_MACC = 5.asUInt(3.W)

  val STRIDE_X    = 0.asUInt(1.W)
  val STRIDE_UNIT = 0.asUInt(1.W)
  val STRIDE_0    = 1.asUInt(1.W)
}

object CsrControlConstants {
  val CSR_N = 0.asUInt(3.W)
  val CSR_W = 1.asUInt(3.W)
  val CSR_S = 2.asUInt(3.W)
  val CSR_C = 3.asUInt(3.W)
  val CSR_I = 4.asUInt(3.W)
}