import chisel3._
import chisel3.util._


class Tile extends Module {
  val io = IO(new Bundle {
    val reset_vector = Input(UInt(32.W))
    val debug = Output(new DebugIO())

    val sbus = new Axi4Bundle()
  })

  val core = Module(new Core())

  // instruction memory must be a synchronous 1 cycle read delay memory
  val itim = Module(new Axi4LiteMemory(
    addressWidth=14,
    memoryFileHex="firmware.hex"
  ))

  // val dtim = Module(new Axi4LiteMemory(addressWidth=17))
  val dtim = Module(new Axi4Memory(params=Axi4Params(addressWidth=12, dataWidth=64)))
  // val dtim = Module(new SimAxi4LiteMemory(readDelay = 10, writeDelay = 10))
  // val dtim = Module(new Axi4BlockMemory())
  
  val xbar = Module(new Axi4Crossbar(2, 2, Axi4Params(dataWidth = 64)))

  // val vdmem_width_converter = Module(new Axi4DataWidthConverter(
  //   s_params = Axi4Params(dataWidth = 64),
  //   m_params = Axi4Params()
  // ))
  // core.io.vdmem <> vdmem_width_converter.io.s_axi
  
  core.io.reset_vector := io.reset_vector
  
  // ibus connection
  core.io.imem <> itim.io.s_axi

  
  // sbus crossbar connections
  core.io.dmem <> xbar.io.s_axi(0)
  // vdmem_width_converter.io.m_axi <> xbar.io.s_axi(1)
  core.io.vdmem <> xbar.io.s_axi(1)
  xbar.io.m_axi(0) <> dtim.io.s_axi
  xbar.io.m_axi(1) <> io.sbus

  // core.io.dmem <> dtim.io.s_axi

  // debug connection
  io.debug := core.io.debug
}
