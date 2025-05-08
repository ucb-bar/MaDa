import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers


class SyncRamSpec extends AnyFlatSpec {
  behavior of "SyncRam"
  it should "do something" in {
    simulate(new SimRam) { dut =>
      dut.reset.poke(true.B)
      
      for (i <- 0 until 10) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)

      dut.io.waddr.poke(0x00000000.U)
      dut.io.wstrb.poke(0x0F.U)
      dut.io.wdata.poke(0xDEADBEEFL.U)
      dut.clock.step()
      dut.io.waddr.poke(0x00000000.U)
      dut.io.wstrb.poke(0x00.U)
      dut.io.wdata.poke(0x00000000.U)
      dut.io.raddr.poke(0x00000000.U)
      dut.clock.step()
      dut.io.rdata.expect(0xDEADBEEFL.U)

      dut.io.waddr.poke(0x00000004.U)
      dut.io.wstrb.poke(0x0F.U)
      dut.io.wdata.poke(0xDEADBEEFL.U)
      dut.clock.step()
      dut.io.waddr.poke(0x00000000.U)
      dut.io.wdata.poke(0x00000000.U)
      dut.io.raddr.poke(0x00000004.U)
      dut.clock.step()
      dut.io.rdata.expect(0x0000BEEFL.U)
    }
  }
}