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


case class Axi4Params(
  idWidth: Int = 4,
  addressWidth: Int = 32,
  dataWidth: Int = 32,
  userWidth: Int = 1
)


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

class Axi4BlackboxBundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val awvalid = Output(Bool())
  val awready = Input(Bool())
  val awid = Output(UInt(params.idWidth.W))
  val awaddr = Output(UInt(params.addressWidth.W))
  val awlen = Output(UInt(8.W))
  val awsize = Output(AxSize())
  val awburst = Output(AxBurst())

  val wvalid = Output(Bool())
  val wready = Input(Bool())
  val wdata = Output(UInt(params.dataWidth.W))
  val wstrb = Output(UInt((params.dataWidth/8).W))
  val wlast = Output(Bool())

  val bvalid = Input(Bool())
  val bready = Output(Bool())
  val bid = Input(UInt(params.idWidth.W))
  val bresp = Input(AxResponse())

  val arvalid = Output(Bool())
  val arready = Input(Bool())
  val arid = Output(UInt(params.idWidth.W))
  val araddr = Output(UInt(params.addressWidth.W))
  val arlen = Output(UInt(8.W))
  val arsize = Output(AxSize())
  val arburst = Output(AxBurst())

  val rvalid = Input(Bool())
  val rready = Output(Bool())
  val rid = Input(UInt(params.idWidth.W))
  val rdata = Input(UInt(params.dataWidth.W))
  val rresp = Input(AxResponse())
  val rlast = Input(Bool())

  def connect(axi: Axi4Bundle): Unit = {
    this.awvalid := axi.aw.valid
    axi.aw.ready := this.awready
    this.awid := axi.aw.bits.id
    this.awaddr := axi.aw.bits.addr
    this.awlen := axi.aw.bits.len
    this.awsize := axi.aw.bits.size
    this.awburst := axi.aw.bits.burst

    this.wvalid := axi.w.valid
    axi.w.ready := this.wready
    this.wdata := axi.w.bits.data
    this.wstrb := axi.w.bits.strb
    this.wlast := axi.w.bits.last

    axi.b.valid := this.bvalid
    this.bready := axi.b.ready
    axi.b.bits.id := this.bid
    axi.b.bits.resp := this.bresp

    this.arvalid := axi.ar.valid
    axi.ar.ready := this.arready
    this.arid := axi.ar.bits.id
    this.araddr := axi.ar.bits.addr
    this.arlen := axi.ar.bits.len
    this.arsize := axi.ar.bits.size
    this.arburst := axi.ar.bits.burst

    axi.r.valid := this.rvalid
    this.rready := axi.r.ready
    axi.r.bits.id := this.rid
    axi.r.bits.data := this.rdata
    axi.r.bits.resp := this.rresp
    axi.r.bits.last := this.rlast
  }

  def flipConnect(axi: Axi4Bundle): Unit = {
    axi.aw.valid := this.awvalid
    this.awready := axi.aw.ready
    axi.aw.bits.id := this.awid
    axi.aw.bits.addr := this.awaddr
    axi.aw.bits.len := this.awlen
    axi.aw.bits.size := this.awsize
    axi.aw.bits.burst := this.awburst

    axi.w.valid := this.wvalid
    this.wready := axi.w.ready
    axi.w.bits.data := this.wdata
    axi.w.bits.strb := this.wstrb
    axi.w.bits.last := this.wlast

    this.bvalid := axi.b.valid
    axi.b.ready := this.bready
    this.bid := axi.b.bits.id
    this.bresp := axi.b.bits.resp

    axi.ar.valid := this.arvalid
    this.arready := axi.ar.ready
    axi.ar.bits.id := this.arid
    axi.ar.bits.addr := this.araddr
    axi.ar.bits.len := this.arlen
    axi.ar.bits.size := this.arsize
    axi.ar.bits.burst := this.arburst

    this.rvalid := axi.r.valid
    axi.r.ready := this.rready
    this.rid := axi.r.bits.id
    this.rdata := axi.r.bits.data
    this.rresp := axi.r.bits.resp
    this.rlast := axi.r.bits.last
  }
}

class Axi4StreamBundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val t = Decoupled(new Bundle {
    val data = Output(UInt(params.dataWidth.W))
    val last = Output(Bool())
    val user = Output(Bool())
  })
}

class Axi4StreamBlackboxBundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val tvalid = Output(Bool())
  val tready = Input(Bool())
  val tdata = Output(UInt(params.dataWidth.W))
  val tlast = Output(Bool())
  val tuser = Output(Bool())

  def connect(axi: Axi4StreamBundle): Unit = {
    this.tvalid := axi.t.valid
    axi.t.ready := this.tready

    this.tdata := axi.t.bits.data
    this.tlast := axi.t.bits.last
    this.tuser := axi.t.bits.user
  }

  def flipConnect(axi: Axi4StreamBundle): Unit = {
    axi.t.valid := this.tvalid
    this.tready := axi.t.ready
    
    axi.t.bits.data := this.tdata
    axi.t.bits.last := this.tlast
    axi.t.bits.user := this.tuser
  }
}
