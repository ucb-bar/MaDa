import chisel3._
import chisel3.util._


class Tile extends Module {
  val io = IO(new Bundle {
    val reset_vector = Input(UInt(32.W))
    val debug = Output(new DebugIO())

    val sbus = new Axi4Bundle()
  })

  val busWidth = 64

  val core = Module(new Core(nVectors = busWidth / 32))

  // instruction memory must be a synchronous 1 cycle read delay memory
  val itim = Module(new Axi4LiteMemory(
    addressWidth=14,
    memoryFileHex="firmware.hex"
  ))

  // val dtim = Module(new Axi4LiteMemory(addressWidth=17))
  val dtim = Module(new Axi4Memory(
    params=Axi4Params(addressWidth=14, dataWidth=busWidth),
    memoryFileHex="firmware.64.hex"
  ))
  // val dtim = Module(new SimAxi4LiteMemory(readDelay = 10, writeDelay = 10))
  // val dtim = Module(new Axi4BlockMemory())
  
  val xbar = Module(new Axi4Crossbar(2, 2, Axi4Params(dataWidth = busWidth), device1Size = 0x10000))

  val dmem_upsizer = Module(new Axi4WidthUpsizer(
    s_params = Axi4Params(dataWidth = 32),
    m_params = Axi4Params(dataWidth = busWidth)
  ))
  val sbus_downsizer = Module(new Axi4WidthDownsizer(
    s_params = Axi4Params(dataWidth = busWidth),
    m_params = Axi4Params(dataWidth = 32)
  ))
  
  core.io.dmem <> dmem_upsizer.io.s_axi
  
  core.io.reset_vector := io.reset_vector
  
  // ibus connection
  core.io.imem <> itim.io.s_axi

  
  // sbus crossbar connections
  // core.io.dmem <> xbar.io.s_axi(0)
  dmem_upsizer.io.m_axi <> xbar.io.s_axi(0)
  core.io.vdmem <> xbar.io.s_axi(1)
  xbar.io.m_axi(0) <> dtim.io.s_axi
  xbar.io.m_axi(1) <> sbus_downsizer.io.s_axi
  sbus_downsizer.io.m_axi <> io.sbus

  // core.io.dmem <> dtim.io.s_axi

  // debug connection
  io.debug := core.io.debug
}
