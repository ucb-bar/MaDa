import chisel3._
import chisel3.util._


class Tile extends Module {
  val io = IO(new Bundle {
    val reset_vector = Input(UInt(32.W))
    val debug = new DebugIO()

    val sbus = new Axi4Bundle()
    val pbus = new Axi4Bundle()
  })

  val busWidth = 64

  val core = Module(new Core(nVectors = busWidth / 32))

  // instruction memory must be a synchronous 1 cycle read delay memory
  val itim = Module(new Axi4LiteMemory(
    addressWidth=14,
    memoryFileHex="firmware.hex"
  ))

  // val dtim = Module(new Axi4LiteMemory(addressWidth=12))
  val dtim = Module(new Axi4Memory(
    params=Axi4Params(addressWidth=12, dataWidth=busWidth),
    memoryFileHex="firmware.64.hex"
  ))
  // val dtim = Module(new SimAxi4LiteMemory(readDelay = 10, writeDelay = 10))
  // val dtim = Module(new Axi4BlockMemory())
  
  val xbar = Module(new Axi4Crossbar(
    2, 3,
    Axi4Params(dataWidth = busWidth),
    deviceSizes = Array(
      0x10_000,     // scratchpad (16 kB)
      0x1000_0000,  // pbus (256 MB)
      0x1000_0000,  // sbus (256 MB)
    ),
    deviceAddresses = Array(
      0x0800_0000,  // scratchpad
      0x1000_0000,  // pbus
      0x2000_0000,  // sbus
    )
  ))

  val dmem_upsizer = Module(new Axi4WidthUpsizer(
    s_params = Axi4Params(dataWidth = 32),
    m_params = Axi4Params(dataWidth = busWidth)
  ))
  val sbus_downsizer = Module(new Axi4WidthDownsizer(
    s_params = Axi4Params(dataWidth = busWidth),
    m_params = Axi4Params(dataWidth = 32)
  ))
  val pbus_downsizer = Module(new Axi4WidthDownsizer(
    s_params = Axi4Params(dataWidth = busWidth),
    m_params = Axi4Params(dataWidth = 32)
  ))
  
  core.io.dmem <> dmem_upsizer.io.s_axi
  
  core.io.reset_vector := io.reset_vector
  
  // ibus connection
  core.io.imem <> itim.io.s_axi

  // tile crossbar connections
  dmem_upsizer.io.m_axi <> xbar.io.s_axi(0)
  core.io.vdmem <> xbar.io.s_axi(1)
  
  // dtim connection
  xbar.io.m_axi(0) <> dtim.io.s_axi

  // pbus connection
  xbar.io.m_axi(1) <> pbus_downsizer.io.s_axi
  pbus_downsizer.io.m_axi <> io.pbus
  
  // sbus connection
  xbar.io.m_axi(2) <> sbus_downsizer.io.s_axi
  sbus_downsizer.io.m_axi <> io.sbus

  // debug connection
  io.debug <> core.io.debug
  dontTouch(core.io.debug)

}
