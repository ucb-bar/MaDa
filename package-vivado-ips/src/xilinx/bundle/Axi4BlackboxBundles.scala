import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4Bundle, Axi4Constants, Axi4LiteBundle, Axi4StreamBundle, AxResponse, AxSize, AxBurst}


class Axi4LiteBlackboxBundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val awvalid = Output(Bool())
  val awready = Input(Bool())
  val awaddr = Output(UInt(params.addressWidth.W))

  val wvalid = Output(Bool())
  val wready = Input(Bool())
  val wdata = Output(UInt(params.dataWidth.W))
  val wstrb = Output(UInt((params.dataWidth/8).W))

  val bvalid = Input(Bool())
  val bready = Output(Bool())
  val bresp = Input(UInt(Axi4Constants.RESP_WIDTH.W))

  val arvalid = Output(Bool())
  val arready = Input(Bool())
  val araddr = Output(UInt(params.addressWidth.W))

  val rvalid = Input(Bool())
  val rready = Output(Bool())
  val rdata = Input(UInt(params.dataWidth.W))
  val rresp = Input(UInt(Axi4Constants.RESP_WIDTH.W))

  def connectFrom(axi: Axi4LiteBundle): Unit = {
    this.awvalid := axi.aw.valid
    axi.aw.ready := this.awready
    this.awaddr := axi.aw.bits.addr

    this.wvalid := axi.w.valid
    axi.w.ready := this.wready
    this.wdata := axi.w.bits.data
    this.wstrb := axi.w.bits.strb

    axi.b.valid := this.bvalid
    this.bready := axi.b.ready
    axi.b.bits.resp := this.bresp.asTypeOf(AxResponse())

    this.arvalid := axi.ar.valid
    axi.ar.ready := this.arready
    this.araddr := axi.ar.bits.addr

    axi.r.valid := this.rvalid
    this.rready := axi.r.ready
    axi.r.bits.data := this.rdata
    axi.r.bits.resp := this.rresp.asTypeOf(AxResponse())
  }

  def connectTo(axi: Axi4LiteBundle): Unit = {
    axi.aw.valid := this.awvalid
    this.awready := axi.aw.ready
    axi.aw.bits.addr := this.awaddr

    axi.w.valid := this.wvalid
    this.wready := axi.w.ready
    axi.w.bits.data := this.wdata
    axi.w.bits.strb := this.wstrb

    this.bvalid := axi.b.valid
    axi.b.ready := this.bready
    this.bresp := axi.b.bits.resp.asUInt

    axi.ar.valid := this.arvalid
    this.arready := axi.ar.ready
    axi.ar.bits.addr := this.araddr

    this.rvalid := axi.r.valid
    axi.r.ready := this.rready
    this.rdata := axi.r.bits.data
    this.rresp := axi.r.bits.resp.asUInt
  }
}


class Axi4BlackboxBundle(params: Axi4Params = Axi4Params()) extends Bundle {
  val awvalid = Output(Bool())
  val awready = Input(Bool())
  val awid = Output(UInt(params.idWidth.W))
  val awaddr = Output(UInt(params.addressWidth.W))
  val awlen = Output(UInt(Axi4Constants.LEN_WIDTH.W))
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
  val arlen = Output(UInt(Axi4Constants.LEN_WIDTH.W))
  val arsize = Output(AxSize())
  val arburst = Output(AxBurst())

  val rvalid = Input(Bool())
  val rready = Output(Bool())
  val rid = Input(UInt(params.idWidth.W))
  val rdata = Input(UInt(params.dataWidth.W))
  val rresp = Input(AxResponse())
  val rlast = Input(Bool())

  def connectFrom(axi: Axi4Bundle): Unit = {
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

  def connectTo(axi: Axi4Bundle): Unit = {
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
