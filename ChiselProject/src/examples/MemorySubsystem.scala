import chisel3._
import chisel3.util._


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
  val io = IO(new Bundle {
    val m_axi = Flipped(new Axi4LiteBundle())
  })

  val tile_xbar = Module(new Axi4Crossbar(
    numSlave = 1,
    numMaster = 3,
    deviceSizes = Array(
      0x1000_0000,
      0x1000_0000,
      0x0100_0000
    ),
    deviceAddresses = Array(
      0x2000_0000,
      0x1000_0000,
      0x0800_0000
    ),
  ))
  
  val mem = Module(new Axi4Memory(
    params=Axi4Params(
      addressWidth=10,
      dataWidth=32,
      idWidth=4,
    )
  ))

  val periph_xbar = Module(new Axi4Crossbar(
    numSlave = 1,
    numMaster = 2,
    deviceSizes = Array(
      0x0001_0000,
      0x0001_0000
    ),
    deviceAddresses = Array(
      0x0002_0000,
      0x0001_0000
    ),
  ))

  val flash = Module(new Axi4QuadSpi())
  val uart = Module(new Axi4LiteUartLite())
  val gpio = Module(new Axi4LiteGpio())

  io.m_axi.connectToAxi4(tile_xbar.io.s_axi(0))
  tile_xbar.io.m_axi(0) <> flash.io.s_axi4
  tile_xbar.io.m_axi(1) <> periph_xbar.io.s_axi(0)
  tile_xbar.io.m_axi(2) <> mem.io.s_axi
  uart.io.s_axi.connectFromAxi4(periph_xbar.io.m_axi(0))
  gpio.io.s_axi.connectFromAxi4(periph_xbar.io.m_axi(1))


  flash.io.s_axi4.ar.bits.size := 2.U.asTypeOf(flash.io.s_axi4.ar.bits.size)
  flash.io.s_axi4.ar.bits.burst := 1.U.asTypeOf(flash.io.s_axi4.ar.bits.burst)

  uart.io.rx := false.B
  gpio.io.gpio_io_i := 0.U

  flash.io.ext_spi_clk := clock
  flash.io.s_axi := DontCare
  flash.io.io0_i := false.B
  flash.io.io1_i := true.B
  flash.io.sck_i := false.B
  flash.io.ss_i := false.B
}
