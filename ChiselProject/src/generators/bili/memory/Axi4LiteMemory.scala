import chisel3._
import chisel3.util._

import java.io.PrintWriter

class Axi4LiteMemory(
  params: Axi4Params = Axi4Params(),
  val memoryFileHex: String = "",
  val memoryFileBin: String = ""
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBundle())
  })

  val mem = Module(new Axi4Memory(
    params = params,
    memoryFileHex = memoryFileHex,
    memoryFileBin = memoryFileBin
  ))

  io.s_axi.connectToAxi4(mem.io.s_axi)
}