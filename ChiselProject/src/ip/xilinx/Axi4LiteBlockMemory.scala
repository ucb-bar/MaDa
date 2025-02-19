import chisel3._
import chisel3.util._


class Axi4LiteBlockMemory extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBundle())
    val rsta_busy = Output(Bool())
    val rstb_busy = Output(Bool())
  })

  val blackbox = Module(new Axi4LiteBlockMemoryBlackbox())

  blackbox.io.s_aclk := clock
  blackbox.io.s_aresetn := ~reset.asBool
  blackbox.io.s_axi.connect(io.s_axi)

  io.rsta_busy := blackbox.io.rsta_busy
  io.rstb_busy := blackbox.io.rstb_busy
}

class Axi4LiteBlockMemoryBlackbox extends BlackBox {
  val io = IO(new Bundle {
    val s_aclk = Input(Clock())
    val s_aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4LiteBlackboxBundle())
    
    val rsta_busy = Output(Bool())
    val rstb_busy = Output(Bool())
  })
}