import chisel3.{BlackBox, _}
import chisel3.util._



class DigitalTop(use_mem_axi: Boolean = false) extends BlackBox {
  val io = IO(new Bundle {
    val auto_chipyard_prcictrl_domain_reset_setter_clock_in_member_allClocks_uncore_clock = Input(Clock())
    val auto_chipyard_prcictrl_domain_reset_setter_clock_in_member_allClocks_uncore_reset = Input(Bool())
    // val auto_mbus_fixedClockNode_anon_out_clock = Output(Clock())
    val auto_cbus_fixedClockNode_anon_out_clock = Output(Clock())
    val auto_cbus_fixedClockNode_anon_out_reset = Output(Bool())
    val resetctrl_hartIsInReset_0 = Input(Bool())
    val debug_clock = Input(Clock())
    val debug_reset = Input(Bool())
    val debug_systemjtag = Flipped(new SystemJTAGIO())
    val debug_dmactive = Output(Bool())
    val debug_dmactiveAck = Input(Bool())
    val custom_boot = Input(Bool())
    val serial_tl_0 = new SerialTileLinkIO(32)
    val uart_0_txd = Output(Bool())
    val uart_0_rxd = Input(Bool())
    val clock_tap = Output(Clock())
    val axi4_lite_s_axi = new RawAXI4Lite()
    val mem_axi4_0 = if (use_mem_axi) new AXI4() else null
  })
}
