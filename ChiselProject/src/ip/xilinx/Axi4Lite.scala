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

class Axi4Bundle extends Bundle {
  val aw = Decoupled(new Bundle {
    val addr = Output(UInt(32.W))
    val burst = Output(AxBurst())
    val id = Output(UInt(4.W))
    val len = Output(UInt(8.W))
    val size = Output(AxSize())
  })
  val w = Decoupled(new Bundle {
    val data = Output(UInt(32.W))
    val strb = Output(UInt((32/8).W))
    val last = Output(Bool())
  })
  val b = Flipped(Decoupled(new Bundle {
    val resp = Input(AxResponse())
    val id = Input(UInt(4.W))
  }))
  val ar = Decoupled(new Bundle {
    val addr = Output(UInt(32.W))
    val burst = Output(AxBurst())
    val id = Output(UInt(4.W))
    val len = Output(UInt(8.W))
    val size = Output(AxSize())
  })
  val r = Flipped(Decoupled(new Bundle {
    val data = Input(UInt(32.W))
    val resp = Input(AxResponse())
    val id = Input(UInt(4.W))
    val last = Input(Bool())
  }))
}

class Axi4LiteBundle extends Bundle {
  val aw = Decoupled(new Bundle {
    val addr = Output(UInt(32.W))
  })
  val w = Decoupled(new Bundle {
    val data = Output(UInt(32.W))
    val strb = Output(UInt((32/8).W))
  })
  val b = Flipped(Decoupled(new Bundle {
    val resp = Input(UInt(2.W))
  }))
  val ar = Decoupled(new Bundle {
    val addr = Output(UInt(32.W))
  })
  val r = Flipped(Decoupled(new Bundle {
    val data = Input(UInt(32.W))
    val resp = Input(UInt(2.W))
  }))
}

class Axi4BlackboxBundle extends Bundle {
  val awvalid = Output(Bool())
  val awready = Input(Bool())
  val awaddr = Output(UInt(32.W))
  val awburst = Output(AxBurst())
  val awid = Output(UInt(4.W))
  val awlen = Output(UInt(8.W))
  val awsize = Output(AxSize())

  val wvalid = Output(Bool())
  val wready = Input(Bool())
  val wdata = Output(UInt(32.W))
  val wstrb = Output(UInt((32/8).W))
  val wlast = Output(Bool())

  val bvalid = Input(Bool())
  val bready = Output(Bool())
  val bresp = Input(AxResponse())
  val bid = Input(UInt(4.W))

  val arvalid = Output(Bool())
  val arready = Input(Bool())
  val araddr = Output(UInt(32.W))
  val arburst = Output(AxBurst())
  val arid = Output(UInt(4.W))
  val arlen = Output(UInt(8.W))
  val arsize = Output(AxSize())

  val rvalid = Input(Bool())
  val rready = Output(Bool())
  val rdata = Input(UInt(32.W))
  val rresp = Input(AxResponse())
  val rid = Input(UInt(4.W))
  val rlast = Input(Bool())

  def connect(axi: Axi4Bundle): Unit = {
    this.awvalid := axi.aw.valid
    axi.aw.ready := this.awready
    this.awaddr := axi.aw.bits.addr
    this.awburst := axi.aw.bits.burst
    this.awid := axi.aw.bits.id
    this.awlen := axi.aw.bits.len
    this.awsize := axi.aw.bits.size

    this.wvalid := axi.w.valid
    axi.w.ready := this.wready
    this.wdata := axi.w.bits.data
    this.wstrb := axi.w.bits.strb
    this.wlast := axi.w.bits.last

    axi.b.valid := this.bvalid
    this.bready := axi.b.ready
    axi.b.bits.resp := this.bresp
    axi.b.bits.id := this.bid

    this.arvalid := axi.ar.valid
    axi.ar.ready := this.arready
    this.araddr := axi.ar.bits.addr
    this.arburst := axi.ar.bits.burst
    this.arid := axi.ar.bits.id
    this.arlen := axi.ar.bits.len
    this.arsize := axi.ar.bits.size

    axi.r.valid := this.rvalid
    this.rready := axi.r.ready
    axi.r.bits.data := this.rdata
    axi.r.bits.resp := this.rresp
    axi.r.bits.id := this.rid
    axi.r.bits.last := this.rlast
  }

  def flipConnect(axi: Axi4Bundle): Unit = {
    axi.aw.valid := this.awvalid
    this.awready := axi.aw.ready
    axi.aw.bits.addr := this.awaddr
    axi.aw.bits.burst := this.awburst
    axi.aw.bits.id := this.awid
    axi.aw.bits.len := this.awlen
    axi.aw.bits.size := this.awsize

    axi.w.valid := this.wvalid
    this.wready := axi.w.ready
    axi.w.bits.data := this.wdata
    axi.w.bits.strb := this.wstrb
    axi.w.bits.last := this.wlast

    this.bvalid := axi.b.valid
    axi.b.ready := this.bready
    this.bresp := axi.b.bits.resp
    this.bid := axi.b.bits.id

    axi.ar.valid := this.arvalid
    this.arready := axi.ar.ready
    axi.ar.bits.addr := this.araddr
    axi.ar.bits.burst := this.arburst
    axi.ar.bits.id := this.arid
    axi.ar.bits.len := this.arlen
    axi.ar.bits.size := this.arsize

    this.rvalid := axi.r.valid
    axi.r.ready := this.rready
    this.rdata := axi.r.bits.data
    this.rresp := axi.r.bits.resp
    this.rid := axi.r.bits.id
    this.rlast := axi.r.bits.last
  }
}


class Axi4LiteBlackboxBundle extends Bundle {
  val awaddr = Output(UInt(32.W))
  val awvalid = Output(Bool())
  val awready = Input(Bool())

  val wdata = Output(UInt(32.W))
  val wstrb = Output(UInt((32/8).W))
  val wvalid = Output(Bool())
  val wready = Input(Bool())

  val bresp = Input(UInt(2.W))
  val bvalid = Input(Bool())
  val bready = Output(Bool())

  val araddr = Output(UInt(32.W))
  val arvalid = Output(Bool())
  val arready = Input(Bool())

  val rdata = Input(UInt(32.W))
  val rresp = Input(UInt(2.W))
  val rvalid = Input(Bool())
  val rready = Output(Bool())

  def connect(axi: Axi4LiteBundle): Unit = {
    this.awvalid := axi.aw.valid
    axi.aw.ready := this.awready
    this.awaddr := axi.aw.bits.addr

    this.wvalid := axi.w.valid
    axi.w.ready := this.wready
    this.wdata := axi.w.bits.data
    this.wstrb := axi.w.bits.strb

    axi.b.valid := this.bvalid
    this.bready := axi.b.ready
    axi.b.bits.resp := this.bresp

    this.arvalid := axi.ar.valid
    axi.ar.ready := this.arready
    this.araddr := axi.ar.bits.addr

    axi.r.valid := this.rvalid
    this.rready := axi.r.ready
    axi.r.bits.data := this.rdata
    axi.r.bits.resp := this.rresp
  }

  def flipConnect(axi: Axi4LiteBundle): Unit = {
    axi.aw.valid := this.awvalid
    this.awready := axi.aw.ready
    axi.aw.bits.addr := this.awaddr

    axi.w.valid := this.wvalid
    this.wready := axi.w.ready
    axi.w.bits.data := this.wdata
    axi.w.bits.strb := this.wstrb

    this.bvalid := axi.b.valid
    axi.b.ready := this.bready
    this.bresp := axi.b.bits.resp

    axi.ar.valid := this.arvalid
    this.arready := axi.ar.ready
    axi.ar.bits.addr := this.araddr

    this.rvalid := axi.r.valid
    axi.r.ready := this.rready
    this.rresp := axi.r.bits.resp
    this.rdata := axi.r.bits.data
  }
}

class Axi4LiteStreamBundle extends Bundle {
  val t = Decoupled(new Bundle {
    val data = Output(UInt(32.W))
    val last = Output(Bool())
    val user = Output(Bool())
  })
}

class Axi4LiteStreamBlackboxBundle extends Bundle {
  val tvalid = Output(Bool())
  val tready = Input(Bool())
  val tdata = Output(UInt(32.W))
  val tlast = Output(Bool())
  val tuser = Output(Bool())

  def connect(axi: Axi4LiteStreamBundle): Unit = {
    this.tvalid := axi.t.valid
    axi.t.ready := this.tready

    this.tdata := axi.t.bits.data
    this.tlast := axi.t.bits.last
    this.tuser := axi.t.bits.user
  }

  def flipConnect(axi: Axi4LiteStreamBundle): Unit = {
    axi.t.valid := this.tvalid
    this.tready := axi.t.ready
    
    axi.t.bits.data := this.tdata
    axi.t.bits.last := this.tlast
    axi.t.bits.user := this.tuser
  }
}
