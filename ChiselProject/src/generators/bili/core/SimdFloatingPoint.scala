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


  val fmacc = Array.fill(nVectors)(Module(new FloatingPoint()))

  val one = 0x3F800000.U(32.W)
  val zero = 0x00000000.U(32.W)

  val op2_positive = io.op2.map(x => x(31) === 0.U)

  // since everybody will execute the same instruction, we need to
  // handle the flow control of the first instance
  val reg_op_pending = RegInit(false.B)

  val input_valid = io.func =/= SIMD_X && !reg_op_pending
  
  when (io.func =/= SIMD_X) {
    reg_op_pending := true.B
  }
  when (fmacc(0).io.result.valid) {
    reg_op_pending := false.B
  }
  
  io.busy := (io.func =/= SIMD_X || reg_op_pending) && !fmacc(0).io.result.valid


  for (i <- 0 until nVectors) {   
    fmacc(i).io.a.valid := input_valid
    fmacc(i).io.b.valid := input_valid
    fmacc(i).io.c.valid := input_valid

    when(io.func === SIMD_ADD) {
      fmacc(i).io.a.bits := io.op1(i)
      fmacc(i).io.b.bits := one
      fmacc(i).io.c.bits := io.op2(i)
    }
    .elsewhen(io.func === SIMD_MUL) {
      fmacc(i).io.a.bits := io.op1(i)
      fmacc(i).io.b.bits := io.op2(i)
      fmacc(i).io.c.bits := zero
    }
    .elsewhen(io.func === SIMD_MACC) {
      fmacc(i).io.a.bits := io.op1(i)
      fmacc(i).io.b.bits := io.op2(i)
      fmacc(i).io.c.bits := io.op3(i)
    }
    .otherwise {
      fmacc(i).io.a.bits := zero
      fmacc(i).io.b.bits := zero
      fmacc(i).io.c.bits := zero
    }

    io.out(i) := MuxCase(fmacc(i).io.result.bits, Seq(
      (io.func === SIMD_XOR) -> zero,
      (io.func === SIMD_MAX) -> Mux(op2_positive(i), io.op2(i), zero),
    ))
  }
}