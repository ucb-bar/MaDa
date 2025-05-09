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


/* ================================ Field Definitions ================================ */


/**
  * AXI4 constants.
  * 
  * These constants are used to define the width of the signals in the AXI4 protocol.
  */
object Axi4Constants {
  val REGION_WIDTH = 4
  val LEN_WIDTH = 8
  val SIZE_WIDTH = 3
  val BURST_WIDTH = 2
  val LOCK_WIDTH = 1
  val CACHE_WIDTH = 4
  val PROT_WIDTH = 3
  val RESP_WIDTH = 2
}


/**
  * AXI4 interface burst type definition.
  */
object AxBurst extends ChiselEnum {
  /** Reads the same address repeatedly. Useful for FIFOs. */
  val FIXED     = Value(0.U(Axi4Constants.BURST_WIDTH.W))
  
  /** Incrementing burst. */
  val INCR      = Value(1.U(Axi4Constants.BURST_WIDTH.W))
  
  /** Wrapping burst. */
  val WRAP      = Value(2.U(Axi4Constants.BURST_WIDTH.W))

  /** Not for use. */
  val RESERVED  = Value(3.U(Axi4Constants.BURST_WIDTH.W))
}

/**
  * AXI4 interface size definition.
  */
object AxSize extends ChiselEnum {
  /** transfer 1 byte per beat. */
  val S_1_BYTE    = Value(0.U(Axi4Constants.SIZE_WIDTH.W))
  
  /** transfer 2 bytes per beat. */
  val S_2_BYTES   = Value(1.U(Axi4Constants.SIZE_WIDTH.W))
  
  /** transfer 4 bytes per beat. */
  val S_4_BYTES   = Value(2.U(Axi4Constants.SIZE_WIDTH.W))
  
  /** transfer 8 bytes per beat. */
  val S_8_BYTES   = Value(3.U(Axi4Constants.SIZE_WIDTH.W))
  
  /** transfer 16 bytes per beat. */
  val S_16_BYTES  = Value(4.U(Axi4Constants.SIZE_WIDTH.W))
  
  /** transfer 32 bytes per beat. */
  val S_32_BYTES  = Value(5.U(Axi4Constants.SIZE_WIDTH.W))
  
  /** transfer 64 bytes per beat. */
  val S_64_BYTES  = Value(6.U(Axi4Constants.SIZE_WIDTH.W))
  
  /** transfer 128 bytes per beat. */
  val S_128_BYTES = Value(7.U(Axi4Constants.SIZE_WIDTH.W))
}

/**
  * AXI4 interface response definition.
  */
object AxResponse extends ChiselEnum {
  /** Normal access success or exclusive access failure. */
  val OKAY      = Value(0.U(Axi4Constants.RESP_WIDTH.W))

  /** Exclusive access okay. */
  val EXOKAY    = Value(1.U(Axi4Constants.RESP_WIDTH.W))

  /** Subordinate error. */
  val SLVERR    = Value(2.U(Axi4Constants.RESP_WIDTH.W))

  /** Decode error. */
  val DECERR    = Value(3.U(Axi4Constants.RESP_WIDTH.W))
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
  val len = Output(UInt(Axi4Constants.LEN_WIDTH.W))
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


/* ================================ Main Bundle Definitions ================================ */

/**
  * AXI4-Lite bundle definition.
  */
class Axi4LiteBundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val aw = Decoupled(new ChannelAxLite(params))
  val w = Decoupled(new ChannelWLite(params))
  val b = Flipped(Decoupled(new ChannelBLite(params)))
  val ar = Decoupled(new ChannelAxLite(params))
  val r = Flipped(Decoupled(new ChannelRLite(params)))

  /**
   * Connect the AXI4-Lite bundle (S) from a driver AXI4 bundle (M).
   */
  def connectFromAxi4(axi4: Axi4Bundle): Unit = {
    // val converter = Module(new Axi4ProtocolConverter(
    //   s_params = Axi4Params(),
    //   m_params = Axi4Params()
    // ))
    // converter.io.s_axi <> axi4
    // this <> converter.io.m_axi

    // AXI4 to AXI4-Lite does not support bursts
    assert(axi4.ar.bits.len === 0.U(Axi4Constants.LEN_WIDTH.W))
    assert(axi4.aw.bits.len === 0.U(Axi4Constants.LEN_WIDTH.W))

    val reg_aw_id = RegInit(0.U(params.idWidth.W))
    val reg_ar_id = RegInit(0.U(params.idWidth.W))

    when (axi4.aw.fire) {
      reg_aw_id := axi4.aw.bits.id
    }
    when (axi4.ar.fire) {
      reg_ar_id := axi4.ar.bits.id
    }

    this.aw.valid := axi4.aw.valid
    axi4.aw.ready := this.aw.ready
    this.aw.bits.addr := axi4.aw.bits.addr
    this.w.valid := axi4.w.valid
    axi4.w.ready := this.w.ready
    this.w.bits.data := axi4.w.bits.data
    this.w.bits.strb := axi4.w.bits.strb
    axi4.b.valid := this.b.valid
    this.b.ready := axi4.b.ready
    axi4.b.bits.resp := this.b.bits.resp
    axi4.b.bits.id := reg_aw_id

    this.ar.valid := axi4.ar.valid
    axi4.ar.ready := this.ar.ready
    this.ar.bits.addr := axi4.ar.bits.addr
    axi4.r.valid := this.r.valid
    this.r.ready := axi4.r.ready
    axi4.r.bits.data := this.r.bits.data
    axi4.r.bits.resp := this.r.bits.resp
    axi4.r.bits.id := reg_ar_id
    axi4.r.bits.last := true.B
  }
  
  /**
   * Connect the driver AXI4-Lite bundle (M) to an AXI4 bundle (S).
   */
  def connectToAxi4(axi4: Axi4Bundle): Unit = {
    axi4.aw.valid := this.aw.valid
    this.aw.ready := axi4.aw.ready
    axi4.aw.bits.id := 0.U(params.idWidth.W)
    axi4.aw.bits.addr := this.aw.bits.addr
    axi4.aw.bits.len := 0.U(Axi4Constants.LEN_WIDTH.W)
    axi4.aw.bits.size := AxSize.S_4_BYTES
    axi4.aw.bits.burst := AxBurst.FIXED
    axi4.w.valid := this.w.valid
    this.w.ready := axi4.w.ready
    axi4.w.bits.data := this.w.bits.data
    axi4.w.bits.strb := this.w.bits.strb
    axi4.w.bits.last := true.B
    this.b.valid := axi4.b.valid
    axi4.b.ready := this.b.ready
    this.b.bits.resp := axi4.b.bits.resp

    axi4.ar.valid := this.ar.valid
    this.ar.ready := axi4.ar.ready
    axi4.ar.bits.id := 0.U(params.idWidth.W)
    axi4.ar.bits.addr := this.ar.bits.addr
    axi4.ar.bits.len := 0.U(Axi4Constants.LEN_WIDTH.W)
    axi4.ar.bits.size := AxSize.S_4_BYTES
    axi4.ar.bits.burst := AxBurst.FIXED
    this.r.valid := axi4.r.valid
    axi4.r.ready := this.r.ready
    this.r.bits.data := axi4.r.bits.data
    this.r.bits.resp := axi4.r.bits.resp
  }
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
  * AXI4 Stream bundle definition.
  */
class Axi4StreamBundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val t = Decoupled(new Bundle {
    val data = Output(UInt(params.dataWidth.W))
    val last = Output(Bool())
    val user = Output(Bool())
  })
}
