import chisel3._
import chisel3.util._

import Instructions._
import SimdControlConstants._


class SimdFloatingPoint extends Module {
  val io = IO(new Bundle {
    val op1 = Input(UInt(32.W))
    val op2 = Input(UInt(32.W))
    val op3 = Input(UInt(32.W))
    val func = Input(UInt(SIMD_X.getWidth.W))

    val out = Output(UInt(32.W))
  })


  val fmacc_0 = Module(new FloatingPoint())

  val one = 0x3F800000.U(32.W)
  val zero = 0x00000000.U(32.W)

  fmacc_0.io.a.valid := true.B
  fmacc_0.io.b.valid := true.B
  fmacc_0.io.c.valid := true.B

  when(io.func === SIMD_ADD) {
    fmacc_0.io.a.bits := io.op1
    fmacc_0.io.b.bits := one
    fmacc_0.io.c.bits := io.op2
  }
  .elsewhen(io.func === SIMD_MUL) {
    fmacc_0.io.a.bits := io.op1
    fmacc_0.io.b.bits := io.op2
    fmacc_0.io.c.bits := zero
  }
  .elsewhen(io.func === SIMD_MACC) {
    fmacc_0.io.a.bits := io.op1
    fmacc_0.io.b.bits := io.op2
    fmacc_0.io.c.bits := io.op3
  }
  .otherwise {
    fmacc_0.io.a.bits := zero
    fmacc_0.io.b.bits := zero
    fmacc_0.io.c.bits := zero
  }

  io.out := fmacc_0.io.result.bits
}