import chisel3._
import chisel3.util._


class MemorySubsystem extends Module {
  val io = IO(new Bundle {
    val m_axi = Flipped(new Axi4LiteBundle())
  })

  val xbar = Module(new TestAxi4LiteCrossbar(
    numSlave = 1,
    numMaster = 2,
    deviceSizes = Array(0x0000_1000, 0x0000_1000),
    deviceAddresses = Array(0x0000_0000, 0x0000_1000),
  ))
  
  val mem = Module(new Axi4LiteMemory(
    params=Axi4Params(
      addressWidth=10,
      dataWidth=32,
      idWidth=4,
    )
  ))
  val mem2 = Module(new Axi4LiteMemory(
    params=Axi4Params(
      addressWidth=10,
      dataWidth=32,
      idWidth=4,
    )
  ))

  io.m_axi <> xbar.io.s_axi(0)
  xbar.io.m_axi(0) <> mem.io.s_axi
  xbar.io.m_axi(1) <> mem2.io.s_axi
  

  // xbar.io.s_axi(0) <> io.m_axi
  // // mem.io.s_axi.connectFromAxi4(xbar.io.m_axi(0))
  // mem.io.s_axi <> xbar.io.m_axi(0)
  // flash.io.s_axi4 <> xbar.io.m_axi(1)

  // val flash = Module(new Axi4QuadSpi())
  // flash.io.ext_spi_clk := clock
  // flash.io.s_axi := DontCare
  // flash.io.io0_i := false.B
  // flash.io.io1_i := false.B
  // flash.io.sck_i := false.B
  // flash.io.ss_i := false.B
}
