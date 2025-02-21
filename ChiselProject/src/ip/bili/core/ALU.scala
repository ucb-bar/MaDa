import chisel3._
import chisel3.util._

import Instructions._
import ScalarControlConstants._


class ALU extends Module {
  val io = IO(new Bundle {
    val op1 = Input(UInt(32.W))
    val op2 = Input(UInt(32.W))
    val func = Input(UInt(ALU_X.getWidth.W))

    val out = Output(UInt(32.W))
  })


  val alu_shamt = io.op2(4,0).asUInt

  io.out := MuxCase(io.op1, Seq(
                  (io.func === ALU_ADD)  -> (io.op1 + io.op2).asUInt,
                  (io.func === ALU_SUB)  -> (io.op1 - io.op2).asUInt,
                  (io.func === ALU_AND)  -> (io.op1 & io.op2).asUInt,
                  (io.func === ALU_OR)   -> (io.op1 | io.op2).asUInt,
                  (io.func === ALU_XOR)  -> (io.op1 ^ io.op2).asUInt,
                  (io.func === ALU_SLT)  -> (io.op1.asSInt < io.op2.asSInt).asUInt,
                  (io.func === ALU_SLTU) -> (io.op1 < io.op2).asUInt,
                  (io.func === ALU_SLL)  -> ((io.op1 << alu_shamt)(31, 0)).asUInt,
                  (io.func === ALU_SRA)  -> (io.op1.asSInt >> alu_shamt).asUInt,
                  (io.func === ALU_SRL)  -> (io.op1 >> alu_shamt).asUInt,
                  (io.func === ALU_COPY) -> (io.op1).asUInt
                  ))
}