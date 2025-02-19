import chisel3._
import chisel3.util._


class Axi4LiteBundle extends Bundle {
  val aw = Decoupled(new Bundle {
    val addr = Output(UInt(32.W))
  })
  val w = Decoupled(new Bundle {
    val data = Output(UInt(32.W))
    val strb = Output(UInt(4.W))
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

class Axi4LiteBlackboxBundle extends Bundle {
  val awaddr = Output(UInt(32.W))
  val awvalid = Output(Bool())
  val awready = Input(Bool())

  val wdata = Output(UInt(32.W))
  val wstrb = Output(UInt(4.W))
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
    this.awaddr := axi.aw.bits.addr
    this.awvalid := axi.aw.valid
    axi.aw.ready := this.awready

    this.wdata := axi.w.bits.data
    this.wstrb := axi.w.bits.strb
    this.wvalid := axi.w.valid
    axi.w.ready := this.wready

    axi.b.bits.resp := this.bresp
    axi.b.valid := this.bvalid
    this.bready := axi.b.ready

    this.araddr := axi.ar.bits.addr
    this.arvalid := axi.ar.valid
    axi.ar.ready := this.arready

    axi.r.bits.data := this.rdata
    axi.r.bits.resp := this.rresp
    axi.r.valid := this.rvalid
    this.rready := axi.r.ready
  }

  def flipConnect(axi: Axi4LiteBundle): Unit = {
    axi.aw.bits.addr := this.awaddr
    axi.aw.valid := this.awvalid
    this.awready := axi.aw.ready

    axi.w.bits.data := this.wdata
    axi.w.bits.strb := this.wstrb
    axi.w.valid := this.wvalid
    this.wready := axi.w.ready

    this.bresp := axi.b.bits.resp
    this.bvalid := axi.b.valid
    axi.b.ready := this.bready

    axi.ar.bits.addr := this.araddr
    axi.ar.valid := this.arvalid
    this.arready := axi.ar.ready

    this.rresp := axi.r.bits.resp
    this.rvalid := axi.r.valid
    axi.r.ready := this.rready
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
