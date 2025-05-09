import chisel3._
import chisel3.util._
import chisel3.experimental.Analog


/**
  * 
  * 
  * Memory Organization:
  * SBUS
  * 0x2000_0000 - 0x200F_FFFF: QSPI Memory (1 MB)
  * PBUS
  * 0x1003_0000 - 0x1003_0FFF: QSPI Control
  * 0x1002_0000 - 0x1002_0FFF: UART
  * 0x1001_0000 - 0x1001_0FFF: GPIO
  * MBUS
  * 0x0800_0000 - 0x0800_3FFF: scratchpad (16 kB)
  * 
  */
class MemorySubsystem extends Module {
  val widerDataWidth = 128

  val io = IO(new Bundle {
    val m_axi_32 = Flipped(new Axi4Bundle(Axi4Params(dataWidth=32)))
    val m_axi_64 = Flipped(new Axi4Bundle(Axi4Params(dataWidth=widerDataWidth)))
    val qspi_cs = Output(Bool())
    val qspi_sck = Output(Clock())
    val qspi_dq = Vec(4, Analog(1.W))
  })


  // === Tile ===
  val tile_xbar = Module(new Axi4Crossbar(
    numSlave=2,
    numMaster=3,
    params=Axi4Params(
      dataWidth=widerDataWidth,
      idWidth=4,
    ),
    deviceSizes=Array(
      0x1000_0000,
      0x1000_0000,
      0x0100_0000
    ),
    deviceAddresses=Array(
      0x2000_0000,
      0x1000_0000,
      0x0800_0000
    ),
  ))

  val tile_upsizer = Module(new Axi4WidthUpsizer(
    s_params=Axi4Params(
      dataWidth=32,
      idWidth=4,
    ),
    m_params=Axi4Params(
      dataWidth=widerDataWidth,
      idWidth=4,
    ),
  ))
  val tile_downsizer = Module(new Axi4WidthDownsizer(
    s_params=Axi4Params(
      dataWidth=widerDataWidth,
      idWidth=4,
    ),
    m_params=Axi4Params(
      dataWidth=32,
      idWidth=4,
    ),
  ))
  val flash_downsizer = Module(new Axi4DataWidthConverter(
    s_params=Axi4Params(
      dataWidth=widerDataWidth,
      idWidth=4,
    ),
    m_params=Axi4Params(
      dataWidth=32,
      idWidth=0,
    ),
  ))
  
  
  val mem = Module(new Axi4Memory(
    params=Axi4Params(
      addressWidth=10,
      dataWidth=widerDataWidth,
      idWidth=4,
    )
  ))

  // === Peripherals ===
  val periph_xbar = Module(new Axi4Crossbar(
    numSlave=1,
    numMaster=2,
    params=Axi4Params(
      dataWidth=widerDataWidth,
      idWidth=4,
    ),
    deviceSizes=Array(
      0x0001_0000,
      0x0001_0000
    ),
    deviceAddresses=Array(
      0x0002_0000,
      0x0001_0000
    ),
  ))

  val flash = Module(new Axi4SpiFlash())
  val uart = Module(new Axi4LiteUartLite())
  val gpio = Module(new Axi4LiteGpio())

  tile_upsizer.io.s_axi <> io.m_axi_32
  tile_xbar.io.s_axi(0) <> tile_upsizer.io.m_axi
  tile_xbar.io.s_axi(1) <> io.m_axi_64
  flash_downsizer.io.s_axi <> tile_xbar.io.m_axi(0)
  tile_downsizer.io.s_axi <> tile_xbar.io.m_axi(1)
  mem.io.s_axi <> tile_xbar.io.m_axi(2)
  uart.io.s_axi.connectFromAxi4(periph_xbar.io.m_axi(0))
  gpio.io.s_axi.connectFromAxi4(periph_xbar.io.m_axi(1))
  periph_xbar.io.s_axi(0) <> tile_downsizer.io.m_axi
  flash.io.s_axi4 <> flash_downsizer.io.m_axi

  flash.io.s_axi4.ar.bits.size := 2.U.asTypeOf(flash.io.s_axi4.ar.bits.size)
  flash.io.s_axi4.ar.bits.burst := 1.U.asTypeOf(flash.io.s_axi4.ar.bits.burst)

  uart.io.rx := false.B
  gpio.io.gpio_io_i := 0.U

  flash.io.ext_spi_clk := clock
  flash.io.s_axi := DontCare
  flash.io.io0_i := false.B
  flash.io.io1_i := true.B
  flash.io.ss_i := false.B

  io.qspi_sck := DontCare
  io.qspi_cs := flash.io.ss_o
  flash.io.ss_i := 0.B

  val qspi_io0_buf = Module(new IOBUF())
  flash.io.io0_i := qspi_io0_buf.io.O
  qspi_io0_buf.io.IO <> io.qspi_dq(0)
  qspi_io0_buf.io.I := flash.io.io0_o
  qspi_io0_buf.io.T := flash.io.io0_t

  val qspi_io1_buf = Module(new IOBUF())
  flash.io.io1_i := qspi_io1_buf.io.O
  qspi_io1_buf.io.IO <> io.qspi_dq(1)
  qspi_io1_buf.io.I := flash.io.io1_o
  qspi_io1_buf.io.T := flash.io.io1_t

  addConstraintResource("package-vivado-ips/resources/constraints/Arty-A7-100-Master.xdc")

  addSimulationResource("package-delta-soc/test/MemorySubsystemTestbench.sv")
  addSimulationResource("package-delta-soc/resources/verilog/SimUart.sv")
  addSimulationResource("package-delta-soc/resources/verilog/SpiFlashMemCtrl.sv")
  addSimulationResource("package-delta-soc/resources/verilog/SimSpiFlashModel.sv")
  addSimulationResource("package-delta-soc/resources/verilog/Ram.v")
}
