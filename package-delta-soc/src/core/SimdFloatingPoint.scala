package delta

import chisel3._
import chisel3.util._
import vivadoips.{FloatingPoint, FloatingPointConfig}

import Instructions._
import SimdControlConstants._


class SimdFloatingPoint(
  val ELEN: Int = 32,
  val VLEN: Int = 64,
  pipelineStages: Int = 1,
  ) extends Module {
  
  val numVectors = VLEN / ELEN

  val io = IO(new Bundle {
    val op1 = Input(Vec(numVectors, UInt(ELEN.W)))
    val op2 = Input(Vec(numVectors, UInt(ELEN.W)))
    val op3 = Input(Vec(numVectors, UInt(ELEN.W)))
    val func = Input(UInt(SIMD_X.getWidth.W))

    val out = Output(Vec(numVectors, UInt(ELEN.W)))
    val busy = Output(Bool())
  })

  assert(ELEN == 32, "Currently only 32-bit (ELEN = 32) is supported")
  assert(VLEN == 64 || VLEN == 128 || VLEN == 256, "Currently only 64-bit, 128-bit and 256-bit (VLEN = {64, 128, 256}) is supported")

  val fmacc = Array.fill(numVectors)(Module(new FloatingPoint(FloatingPointConfig(pipelineStages=pipelineStages))))

  val one = 0x3F800000.U(32.W)
  val zero = 0x00000000.U(32.W)


  // since everybody will execute the same instruction, we need to
  // handle the flow control of the first instance
  val reg_op_pending = RegInit(false.B)

  val input_valid = io.func =/= SIMD_X && !reg_op_pending
  
  when (input_valid) {
    reg_op_pending := true.B
  }
  when (fmacc(0).io.result.valid) {
    reg_op_pending := false.B
  }
  
  io.busy := (io.func =/= SIMD_X || reg_op_pending) && !fmacc(0).io.result.valid


  // EX 2 stage pipeline registers
  val ex2_reg_op_a = Reg(Vec(numVectors, UInt(ELEN.W)))
  val ex2_reg_op_b = Reg(Vec(numVectors, UInt(ELEN.W)))
  val ex2_reg_op_c = Reg(Vec(numVectors, UInt(ELEN.W)))
  
  val ex2_reg_op_rs2 = RegNext(io.op2)
  val ex2_reg_func = RegNext(io.func)
  val ex2_reg_valid = RegNext(input_valid)


  for (i <- 0 until numVectors) {
    when(io.func === SIMD_ADD) {
      ex2_reg_op_a(i) := io.op1(i)
      ex2_reg_op_b(i) := one
      ex2_reg_op_c(i) := io.op2(i)
    }
    .elsewhen(io.func === SIMD_MUL) {
      ex2_reg_op_a(i) := io.op1(i)
      ex2_reg_op_b(i) := io.op2(i)
      ex2_reg_op_c(i) := zero
    }
    .elsewhen(io.func === SIMD_MACC) {
      ex2_reg_op_a(i) := io.op1(i)
      ex2_reg_op_b(i) := io.op2(i)
      ex2_reg_op_c(i) := io.op3(i)
    }
    .otherwise {
      ex2_reg_op_a(i) := zero
      ex2_reg_op_b(i) := zero
      ex2_reg_op_c(i) := zero
    }
  }

  // ====== pipeline cross here ======

  val ex2_op2_positive = ex2_reg_op_rs2.map(x => x(31) === 0.U)

  for (i <- 0 until numVectors) {
    fmacc(i).io.a.valid := ex2_reg_valid
    fmacc(i).io.b.valid := ex2_reg_valid
    fmacc(i).io.c.valid := ex2_reg_valid

    fmacc(i).io.a.bits := ex2_reg_op_a(i)
    fmacc(i).io.b.bits := ex2_reg_op_b(i)
    fmacc(i).io.c.bits := ex2_reg_op_c(i)

    io.out(i) := MuxCase(fmacc(i).io.result.bits, Seq(
      (ex2_reg_func === SIMD_XOR) -> zero,
      (ex2_reg_func === SIMD_MAX) -> Mux(ex2_op2_positive(i), ex2_reg_op_rs2(i), zero),
    ))
  }
}