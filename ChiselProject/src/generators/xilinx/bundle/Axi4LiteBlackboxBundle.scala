import chisel3._
import chisel3.util._


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
  val bresp = Input(UInt(2.W))

  val arvalid = Output(Bool())
  val arready = Input(Bool())
  val araddr = Output(UInt(params.addressWidth.W))

  val rvalid = Input(Bool())
  val rready = Output(Bool())
  val rdata = Input(UInt(params.dataWidth.W))
  val rresp = Input(UInt(2.W))

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
    axi.b.bits.resp := this.bresp.asTypeOf(AxResponse())

    this.arvalid := axi.ar.valid
    axi.ar.ready := this.arready
    this.araddr := axi.ar.bits.addr

    axi.r.valid := this.rvalid
    this.rready := axi.r.ready
    axi.r.bits.data := this.rdata
    axi.r.bits.resp := this.rresp.asTypeOf(AxResponse())
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
