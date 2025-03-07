/**
  * Axi4Bundle.scala
  * 
  * This file contains the definitions for the Axi4 protocol bundles.
  * 
  * For AXI4 protocol specification, please @see https://developer.arm.com/documentation/ihi0022/latest/
  * 
  */

import chisel3._
import chisel3.util._

object AxBurst extends ChiselEnum {
  val FIXED     = Value(0.U(4.W))  // Reads the same address repeatedly. Useful for FIFOs.
  val INCR      = Value(1.U(4.W))  // Incrementing burst.
  val WRAP      = Value(2.U(4.W))  // Wrapping burst.
  val RESERVED  = Value(3.U(4.W))  // Not for use.
}

object AxSize extends ChiselEnum {
  val S_1_BYTE    = Value(0.U(3.W))
  val S_2_BYTES   = Value(1.U(3.W))
  val S_4_BYTES   = Value(2.U(3.W))
  val S_8_BYTES   = Value(3.U(3.W))
  val S_16_BYTES  = Value(4.U(3.W))
  val S_32_BYTES  = Value(5.U(3.W))
  val S_64_BYTES  = Value(6.U(3.W))
  val S_128_BYTES = Value(7.U(3.W))
}

object AxResponse extends ChiselEnum {
  val OKAY      = Value(0.U(2.W))  // Normal access success or exclusive access failure.
  val EXOKAY    = Value(1.U(2.W))  // Exclusive access okay.
  val SLVERR    = Value(2.U(2.W))  // Subordinate error.
  val DECERR    = Value(3.U(2.W))  // Decode error.
}

class Axi4Bundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val aw = Decoupled(new Bundle {
    val id = Output(UInt(params.idWidth.W))
    val addr = Output(UInt(params.addressWidth.W))
    val len = Output(UInt(8.W))
    val size = Output(AxSize())
    val burst = Output(AxBurst())
    // val lock = Output(Bool())
    // val cache = Output(UInt(4.W))
    // val prot = Output(UInt(3.W))
    // val user = Output(UInt(userWidth.W))
  })
  val w = Decoupled(new Bundle {
    val data = Output(UInt(params.dataWidth.W))
    val strb = Output(UInt((params.dataWidth/8).W))
    val last = Output(Bool())
    // val user = Output(UInt(userWidth.W))
  })
  val b = Flipped(Decoupled(new Bundle {
    val id = Input(UInt(params.idWidth.W))
    val resp = Input(AxResponse())
    // val user = Input(UInt(userWidth.W))
  }))
  val ar = Decoupled(new Bundle {
    val id = Output(UInt(params.idWidth.W))
    val addr = Output(UInt(params.addressWidth.W))
    val len = Output(UInt(8.W))
    val size = Output(AxSize())
    val burst = Output(AxBurst())
    // val lock = Output(Bool())
    // val cache = Output(UInt(4.W))
    // val prot = Output(UInt(3.W))
    // val user = Output(UInt(userWidth.W))
  })
  val r = Flipped(Decoupled(new Bundle {
    val id = Input(UInt(params.idWidth.W))
    val data = Input(UInt(params.dataWidth.W))
    val resp = Input(AxResponse())
    val last = Input(Bool())
    // val user = Input(UInt(userWidth.W))
  }))
}
