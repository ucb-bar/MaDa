package delta

import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4Bundle, Axi4LiteBundle}
import vivadoips.{Axi4Crossbar, Axi4BlockMemory, Axi4DataWidthConverter}


case class TileConfig(
  CoreConfig: CoreConfig = new CoreConfig(),
  sbusFrequency: Int = 20,
)

class Tile(
  val config: TileConfig = TileConfig()
) extends Module {
  val io = IO(new Bundle {
    val reset_vector = Input(UInt(32.W))
    val debug = new DebugIO()

    val sbus = new Axi4Bundle()
    val pbus = new Axi4Bundle()
  })

  dontTouch(io.reset_vector)
  dontTouch(io.debug)

  val busWidth = config.CoreConfig.VLEN

  val core = Module(new Core(
    CoreConfig(
      VLEN=busWidth,
      ELEN=config.CoreConfig.ELEN,
      pipelineStages=config.CoreConfig.pipelineStages,
    )
  ))

  val itim = Module(new Axi4Memory(
    params=Axi4Params(addressWidth=16, dataWidth=32),
    memoryFileHex="firmware.hex"
  ))

  val dtim = Module(new Axi4BlockMemory(
    params=Axi4Params(addressWidth=32, dataWidth=busWidth),
    coeFile=s"/home/tk/Desktop/MaDa/firmware/firmware.${busWidth}.coe"
  ))
  // val dtim = Module(new Axi4WiderMemory(
  //   params=Axi4Params(addressWidth=32, dataWidth=busWidth),
  //   coeFile=s"/home/tk/Desktop/MaDa/firmware/firmware.${busWidth}.coe"
  // ))
  
  val xbar = Module(new Axi4Crossbar(
    numSlave=2,
    numMaster=3,
    params=Axi4Params(
      dataWidth=busWidth,
      idWidth=4,
    ),
    deviceSizes=Array(
      0x0100_0000,  // scratchpad (16 kB)
      0x1000_0000,  // pbus (256 MB)
      0x1000_0000,  // sbus (256 MB)
    ),
    deviceAddresses=Array(
      0x0800_0000,  // scratchpad
      0x1000_0000,  // pbus
      0x2000_0000,  // sbus
    ),
  ))

  val dmem_upsizer = Module(new Axi4WidthUpsizer(
    s_params=Axi4Params(
      dataWidth=32,
      idWidth=4,
    ),
    m_params=Axi4Params(
      dataWidth=busWidth,
      idWidth=4,
    ),
  ))
  val sbus_downsizer = Module(new Axi4DataWidthConverter(
    s_params=Axi4Params(
      dataWidth=busWidth,
      idWidth=4,
    ),
    m_params=Axi4Params(
      dataWidth=32,
      idWidth=0,
    ),
  ))
  val pbus_downsizer = Module(new Axi4WidthDownsizer(
    s_params=Axi4Params(
      dataWidth=busWidth,
      idWidth=4,
    ),
    m_params=Axi4Params(
      dataWidth=32,
      idWidth=4,
    ),
  ))

  // itim connection
  core.io.imem.connectToAxi4(itim.io.s_axi)
  core.io.reset_vector := io.reset_vector

  // xbar connections
  dmem_upsizer.io.s_axi <> core.io.dmem
  xbar.io.s_axi(0) <> dmem_upsizer.io.m_axi
  xbar.io.s_axi(1) <> core.io.vdmem
  
  // dtim connection
  dtim.io.s_axi <> xbar.io.m_axi(0)

  // pbus connection
  pbus_downsizer.io.s_axi <> xbar.io.m_axi(1)
  io.pbus <> pbus_downsizer.io.m_axi
  
  // sbus connection
  sbus_downsizer.io.s_axi <> xbar.io.m_axi(2)
  io.sbus <> sbus_downsizer.io.m_axi

  // debug connection
  io.debug <> core.io.debug
  dontTouch(core.io.debug)
}
