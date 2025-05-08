import chisel3._
import chisel3.util._


class EECS151Tile extends Module {
  val io = IO(new Bundle {
    val reset_vector = Input(UInt(32.W))
    val debug = new DebugIO()
  })

  val core = Module(new Core(nVectors=1))

  // instruction memory must be a synchronous 1 cycle read delay memory
  val itim = Module(new Axi4MemoryWithLatency(
    params=Axi4Params(addressWidth=14, dataWidth=32),
    memoryFileHex="firmware.hex",
    readLatency=2,
    writeLatency=2,
  ))

  val dtim = Module(new Axi4Memory(
    params=Axi4Params(addressWidth=14, dataWidth=32),
    memoryFileHex="firmware.hex"
  ))
  
  // itim connection
  core.io.imem.connectToAxi4(itim.io.s_axi)

  // dtim connection
  dtim.io.s_axi <> core.io.dmem
  core.io.reset_vector := io.reset_vector

  // vector memory connection
  core.io.vdmem := DontCare

  // debug connection
  io.debug <> core.io.debug
  dontTouch(core.io.debug)
}


class EECS252Tile extends Module {
  val io = IO(new Bundle {
    val reset_vector = Input(UInt(32.W))
    val debug = new DebugIO()
  })

  val core = Module(new Core(nVectors=2))

  val xbar = Module(new Axi4Crossbar(
    2, 1,
    Axi4Params(dataWidth=64),
    deviceSizes=Array(
      0x10_000,     // scratchpad (16 kB)
    ),
    deviceAddresses=Array(
      0x0800_0000,  // scratchpad
    )
  ))

  val dmem_upsizer = Module(new Axi4WidthUpsizer(
    s_params=Axi4Params(dataWidth=32),
    m_params=Axi4Params(dataWidth=64)
  ))

  // instruction memory must be a synchronous 1 cycle read delay memory
  val itim = Module(new Axi4MemoryWithLatency(
    params=Axi4Params(addressWidth=14, dataWidth=32),
    memoryFileHex="firmware.hex",
    readLatency=2,
    writeLatency=2,
  ))

  val dtim = Module(new Axi4Memory(
    params=Axi4Params(addressWidth=14, dataWidth=64),
    memoryFileHex="firmware.64.hex"
  ))
  
  // itim connection
  core.io.imem.connectToAxi4(itim.io.s_axi)
  core.io.reset_vector := io.reset_vector

  // dtim connection
  dmem_upsizer.io.s_axi <> core.io.dmem
  xbar.io.s_axi(0) <> dmem_upsizer.io.m_axi
  xbar.io.s_axi(1) <> core.io.vdmem

  dtim.io.s_axi <> xbar.io.m_axi(0)

  // debug connection
  io.debug <> core.io.debug
  dontTouch(core.io.debug)
}
