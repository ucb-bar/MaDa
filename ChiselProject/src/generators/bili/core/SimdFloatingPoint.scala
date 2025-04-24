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
  })


  val fmacc = Array.fill(nVectors)(Module(new FloatingPoint()))

  val one = 0x3F800000.U(32.W)
  val zero = 0x00000000.U(32.W)

  for (i <- 0 until nVectors) {   
    fmacc(i).io.a.valid := true.B
    fmacc(i).io.b.valid := true.B
    fmacc(i).io.c.valid := true.B
  
    

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

    io.out(i) := fmacc(i).io.result.bits
  }

}