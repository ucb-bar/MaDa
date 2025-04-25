import chisel3._
import chisel3.util._


class MemorySubsystem extends Module {
  val io = IO(new Bundle {
    val m_axi = Flipped(new Axi4Bundle())
  })

  val xbar = Module(new Axi4Crossbar(
    numSlave = 1,
    numMaster = 2,
    deviceSizes = Array(0x0001_0000, 0x0001_0000),
    deviceAddresses = Array(0x0000_0000, 0x0001_0000),
  ))
  

  // val mem = Module(new Axi4BlockMemory(
  //   params=Axi4Params(
  //     addressWidth=32,
  //     dataWidth=64,
  //     idWidth=4,
  //   )
  // ))
  
  val mem = Module(new Axi4Memory(
    params=Axi4Params(
      addressWidth=10,
      dataWidth=32,
      idWidth=4,
    )
  ))

  // mem.io.s_axi <> io.m_axi
  
  // val mem = Module(new Axi4LiteMemory(
  //   params=Axi4Params(
  //     addressWidth=10,
  //     dataWidth=32,
  //     idWidth=4,
  //   )
  // ))

  val flash = Module(new Axi4QuadSpi())
  
  xbar.io.s_axi(0) <> io.m_axi
  // mem.io.s_axi.connectFromAxi4(xbar.io.m_axi(0))
  mem.io.s_axi <> xbar.io.m_axi(0)
  flash.io.s_axi4 <> xbar.io.m_axi(1)

  flash.io.ext_spi_clk := clock
  flash.io.s_axi := DontCare
  flash.io.io0_i := false.B
  flash.io.io1_i := false.B
  flash.io.sck_i := false.B
  flash.io.ss_i := false.B
}
