import chisel3._
import chisel3.util._


class Tile extends Module {
  val io = IO(new Bundle {
    val reset_vector = Input(UInt(32.W))
    val debug = Output(new DebugIO())

    val sbus = new Axi4Bundle()
  })

  val busWidth = 256

  val core = Module(new Core(nVectors = busWidth / 32))

  // instruction memory must be a synchronous 1 cycle read delay memory
  val itim = Module(new Axi4LiteMemory(
    addressWidth=14,
    memoryFileHex="firmware.hex"
  ))

  // val dtim = Module(new Axi4LiteMemory(addressWidth=17))
  val dtim = Module(new Axi4Memory(params=Axi4Params(addressWidth=12, dataWidth=busWidth)))
  // val dtim = Module(new SimAxi4LiteMemory(readDelay = 10, writeDelay = 10))
  // val dtim = Module(new Axi4BlockMemory())
  
  val xbar = Module(new Axi4Crossbar(2, 2, Axi4Params(dataWidth = busWidth)))

  val dmem_width_converter = Module(new Axi4DataWidthConverter(
    s_params = Axi4Params(),
    m_params = Axi4Params(idWidth = 0, dataWidth = busWidth)
  ))
  core.io.dmem <> dmem_width_converter.io.s_axi
  
  core.io.reset_vector := io.reset_vector
  
  // ibus connection
  core.io.imem <> itim.io.s_axi

  
  // sbus crossbar connections
  // core.io.dmem <> xbar.io.s_axi(0)
  dmem_width_converter.io.m_axi <> xbar.io.s_axi(0)
  core.io.vdmem <> xbar.io.s_axi(1)
  xbar.io.m_axi(0) <> dtim.io.s_axi
  xbar.io.m_axi(1) <> io.sbus

  // core.io.dmem <> dtim.io.s_axi

  // debug connection
  io.debug := core.io.debug
}
