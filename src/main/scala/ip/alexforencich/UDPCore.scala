import chisel3.{BlackBox, _}
import chisel3.util._
import chisel3.experimental.IntParam


class udp_core(
  mac_address: Long = 0x02_00_00_00_00_00L,
  ip_address: Int = 0xC0_A8_01_80,
  gateway_ip: Int = 0xC0_A8_01_01,
  subnet_mask: Int = 0xFF_FF_FF_00,
  udp_port: Int = 1234
) extends BlackBox(Map(
    "MAC_ADDRESS" -> IntParam(mac_address),
    "IP_ADDRESS" -> IntParam(ip_address),
    "GATEWAY_IP" -> IntParam(gateway_ip),
    "SUBNET_MASK" -> IntParam(subnet_mask),
    "UDP_PORT" -> IntParam(udp_port)
  )) with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val rst = Input(Reset())

    val btn = Input(UInt(4.W))
    val sw = Input(UInt(4.W))
    val led0_r = Output(Bool())
    val led0_g = Output(Bool())
    val led0_b = Output(Bool())
    val led1_r = Output(Bool())
    val led1_g = Output(Bool())
    val led1_b = Output(Bool())
    val led2_r = Output(Bool())
    val led2_g = Output(Bool())
    val led2_b = Output(Bool())
    val led3_r = Output(Bool())
    val led3_g = Output(Bool())
    val led3_b = Output(Bool())
    val led4 = Output(Bool())
    val led5 = Output(Bool())
    val led6 = Output(Bool())
    val led7 = Output(Bool())

    val phy_rx_clk = Input(Bool())
    val phy_rxd = Input(UInt(4.W))
    val phy_rx_dv = Input(Bool())
    val phy_rx_er = Input(Bool())
    val phy_tx_clk = Input(Bool())
    val phy_txd = Output(UInt(4.W))
    val phy_tx_en = Output(Bool())
    val phy_col = Input(Bool())
    val phy_crs = Input(Bool())
    val phy_reset_n = Output(Bool())

    val uart_rxd = Input(Bool())
    val uart_txd = Output(Bool())
    
    val rx_fifo_udp_payload_axis_tdata = Output(UInt(8.W))
    val rx_fifo_udp_payload_axis_tvalid = Output(Bool())
    val rx_fifo_udp_payload_axis_tready = Input(Bool())
    val rx_fifo_udp_payload_axis_tlast = Output(Bool())
    val rx_fifo_udp_payload_axis_tuser = Output(Bool())

    val tx_fifo_udp_payload_axis_tdata = Input(UInt(8.W))
    val tx_fifo_udp_payload_axis_tvalid = Input(Bool())
    val tx_fifo_udp_payload_axis_tready = Output(Bool())
    val tx_fifo_udp_payload_axis_tlast = Input(Bool())
    val tx_fifo_udp_payload_axis_tuser = Input(Bool())
  })
}

