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

/**
  * AXI4 interface burst type definition.
  */
object AxBurst extends ChiselEnum {
  /** Reads the same address repeatedly. Useful for FIFOs. */
  val FIXED     = Value(0.U(4.W))
  
  /** Incrementing burst. */
  val INCR      = Value(1.U(4.W))
  
  /** Wrapping burst. */
  val WRAP      = Value(2.U(4.W))

  /** Not for use. */
  val RESERVED  = Value(3.U(4.W))
}

/**
  * AXI4 interface size definition.
  */
object AxSize extends ChiselEnum {
  /** 1 byte. */
  val S_1_BYTE    = Value(0.U(3.W))
  
  /** 2 bytes. */
  val S_2_BYTES   = Value(1.U(3.W))
  
  /** 4 bytes. */
  val S_4_BYTES   = Value(2.U(3.W))
  
  /** 8 bytes. */
  val S_8_BYTES   = Value(3.U(3.W))
  
  /** 16 bytes. */
  val S_16_BYTES  = Value(4.U(3.W))
  
  /** 32 bytes. */
  val S_32_BYTES  = Value(5.U(3.W))
  
  /** 64 bytes. */
  val S_64_BYTES  = Value(6.U(3.W))
  
  /** 128 bytes. */
  val S_128_BYTES = Value(7.U(3.W))
}

/**
  * AXI4 interface response definition.
  */
object AxResponse extends ChiselEnum {
  /** Normal access success or exclusive access failure. */
  val OKAY      = Value(0.U(2.W))

  /** Exclusive access okay. */
  val EXOKAY    = Value(1.U(2.W))

  /** Subordinate error. */
  val SLVERR    = Value(2.U(2.W))

  /** Decode error. */
  val DECERR    = Value(3.U(2.W))
}

/**
  * AXI4-Lite AW and AR channel definition.
  */
class ChannelAxLite(params: Axi4Params = Axi4Params()) extends Bundle {
  val addr = Output(UInt(params.addressWidth.W))
}

/**
  * AXI4 AW and AR channel definition.
  */
class ChannelAx(params: Axi4Params = Axi4Params()) extends ChannelAxLite(params) {
  val id = Output(UInt(params.idWidth.W))
  val len = Output(UInt(8.W))
  val size = Output(AxSize())
  val burst = Output(AxBurst())
}

/**
  * AXI4-Lite W channel definition.
  */
class ChannelWLite(params: Axi4Params = Axi4Params()) extends Bundle {
  val data = Output(UInt(params.dataWidth.W))
  val strb = Output(UInt((params.dataWidth/8).W))
}

/**
  * AXI4 W channel definition.
  */
class ChannelW(params: Axi4Params = Axi4Params()) extends ChannelWLite(params) {
  val last = Output(Bool())
}

/**
  * AXI4-Lite B channel definition.
  */
class ChannelBLite(params: Axi4Params = Axi4Params()) extends Bundle {
  val resp = Input(AxResponse())
}

/**
  * AXI4 B channel definition.
  */
class ChannelB(params: Axi4Params = Axi4Params()) extends ChannelBLite(params) {
  val id = Input(UInt(params.idWidth.W))
}

/**
  * AXI4-Lite R channel definition.
  */
class ChannelRLite(params: Axi4Params = Axi4Params()) extends Bundle {
  val data = Input(UInt(params.dataWidth.W))
  val resp = Input(AxResponse())
}

/**
  * AXI4 R channel definition.
  */
class ChannelR(params: Axi4Params = Axi4Params()) extends ChannelRLite(params) {
  val id = Input(UInt(params.idWidth.W))
  val last = Input(Bool())
}

/**
  * AXI4-Lite bundle definition.
  */
class Axi4LiteBundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val aw = Decoupled(new ChannelAxLite(params))
  val w = Decoupled(new ChannelWLite(params))
  val b = Flipped(Decoupled(new ChannelBLite(params)))
  val ar = Decoupled(new ChannelAxLite(params))
  val r = Flipped(Decoupled(new ChannelRLite(params)))
}

/**
  * AXI4 bundle definition.
  */
class Axi4Bundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val aw = Decoupled(new ChannelAx(params))
  val w = Decoupled(new ChannelW(params))
  val b = Flipped(Decoupled(new ChannelB(params)))
  val ar = Decoupled(new ChannelAx(params))
  val r = Flipped(Decoupled(new ChannelR(params)))
}

/**
  * AXI4 to AXI4-Lite converter.
  */
object Axi4ToAxi4Lite {
  def apply(axi: Axi4Bundle): Axi4LiteBundle = {
    val converter = Module(new Axi4ProtocolConverter(
      s_params = Axi4Params(),
      m_params = Axi4Params()
    ))
    axi <> converter.io.s_axi
    converter.io.m_axi
  }
}

/**
  * AXI4 Stream bundle definition.
  */
class Axi4StreamBundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val t = Decoupled(new Bundle {
    val data = Output(UInt(params.dataWidth.W))
    val last = Output(Bool())
    val user = Output(Bool())
  })
}
