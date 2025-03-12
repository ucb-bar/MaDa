import chisel3._
import chisel3.util._


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
