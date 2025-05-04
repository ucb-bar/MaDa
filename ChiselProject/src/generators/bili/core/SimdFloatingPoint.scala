import chisel3._
import chisel3.util._

import Instructions._
import SimdControlConstants._


class SimdFloatingPoint(
  nVectors: Int = 1
) extends Module {
  val io = IO(new Bundle {
    val op1 = Input(Vec(nVectors, UInt(32.W)))
    val op2 = Input(Vec(nVectors, UInt(32.W)))
    val op3 = Input(Vec(nVectors, UInt(32.W)))
    val func = Input(UInt(SIMD_X.getWidth.W))

    val out = Output(Vec(nVectors, UInt(32.W)))
    val busy = Output(Bool())
  })

  val fmacc = Array.fill(nVectors)(Module(new FloatingPoint(pipelineStages=1)))

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


  // EX 2 stage pipeline
  val ex2_reg_op_1 = RegNext(io.op1)
  val ex2_reg_op_2 = RegNext(io.op2)
  val ex2_reg_op_3 = RegNext(io.op3)
  val ex2_reg_func = RegNext(io.func)
  val ex2_reg_valid = RegNext(input_valid)

  val op2_positive = ex2_reg_op_2.map(x => x(31) === 0.U)

  for (i <- 0 until nVectors) {
    fmacc(i).io.a.valid := ex2_reg_valid
    fmacc(i).io.b.valid := ex2_reg_valid
    fmacc(i).io.c.valid := ex2_reg_valid

    when(ex2_reg_func === SIMD_ADD) {
      fmacc(i).io.a.bits := ex2_reg_op_1(i)
      fmacc(i).io.b.bits := one
      fmacc(i).io.c.bits := ex2_reg_op_2(i)
    }
    .elsewhen(ex2_reg_func === SIMD_MUL) {
      fmacc(i).io.a.bits := ex2_reg_op_1(i)
      fmacc(i).io.b.bits := ex2_reg_op_2(i)
      fmacc(i).io.c.bits := zero
    }
    .elsewhen(ex2_reg_func === SIMD_MACC) {
      fmacc(i).io.a.bits := ex2_reg_op_1(i)
      fmacc(i).io.b.bits := ex2_reg_op_2(i)
      fmacc(i).io.c.bits := ex2_reg_op_3(i)
    }
    .otherwise {
      fmacc(i).io.a.bits := zero
      fmacc(i).io.b.bits := zero
      fmacc(i).io.c.bits := zero
    }

    io.out(i) := MuxCase(fmacc(i).io.result.bits, Seq(
      (ex2_reg_func === SIMD_XOR) -> zero,
      (ex2_reg_func === SIMD_MAX) -> Mux(op2_positive(i), ex2_reg_op_2(i), zero),
    ))
  }
}