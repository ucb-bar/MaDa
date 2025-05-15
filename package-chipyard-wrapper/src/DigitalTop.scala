package chipyard_wrapper

import chisel3.{BlackBox, _}
import chisel3.util._
import amba.Axi4Bundle
import prci.SyncReset
class SystemJTAGIO extends Bundle {
  val jtag = new Bundle {
    val TCK = Output(Clock())
    val TMS = Output(Bool())
    val TDI = Output(Bool())
    val TDO_data = Input(Bool())
  }
  val reset = Output(Reset())
}

class SerialTileLinkIO(val dataBits: Int) extends Bundle {
  val clock_in = Input(Clock())
  val in = Flipped(DecoupledIO(new Bundle{
    val phit = UInt(dataBits.W)
  }))
  val out = DecoupledIO(new Bundle{
    val phit = UInt(dataBits.W)
  })
}


class DigitalTop extends BlackBox {
  val io = IO(new Bundle {
    val auto_chipyard_prcictrl_domain_reset_setter_clock_in_member_allClocks_uncore_clock = Input(Clock())
    val auto_chipyard_prcictrl_domain_reset_setter_clock_in_member_allClocks_uncore_reset = Input(Bool())
    val auto_mbus_fixedClockNode_anon_out_clock = Output(Clock())
    val auto_cbus_fixedClockNode_anon_out_clock = Output(Clock())
    val auto_cbus_fixedClockNode_anon_out_reset = Output(Bool())
    val resetctrl_hartIsInReset_0 = Input(Bool())
    val debug_clock = Input(Clock())
    val debug_reset = Input(Bool())
    val debug_systemjtag = Flipped(new SystemJTAGIO())
    val debug_dmactive = Output(Bool())
    val debug_dmactiveAck = Input(Bool())
    val mem_axi4_0 = new Axi4Bundle()
    val custom_boot = Input(Bool())
    val serial_tl_0 = new SerialTileLinkIO(32)
    val uart_0_txd = Output(Bool())
    val uart_0_rxd = Input(Bool())
    val clock_tap = Output(Clock())
  })
}


class ChipTop extends BlackBox {
  val io = IO(new Bundle {
    val uart_0_txd = Output(Bool())
    val uart_0_rxd = Input(Bool())
    val axi4_mem_0_clock = Output(Clock())
    val axi4_mem_0_bits = new Axi4Bundle()
    val custom_boot = Input(Bool())
    val jtag_TCK = Input(Clock())
    val jtag_TMS = Input(Bool())
    val jtag_TDI = Input(Bool())
    val jtag_TDO = Output(Bool())
    val jtag_reset = Input(Reset())
    val reset_io = Input(Reset())
    val clock_uncore = Input(Clock())
    val clock_tap = Output(Clock())
    val serial_tl_0 = new SerialTileLinkIO(32)
  })
}
