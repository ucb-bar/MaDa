import chisel3.{BlackBox, _}
import chisel3.util._


class axis_data_fifo_0(width: Int) extends BlackBox {
  val io = IO(new Bundle {
    val s_axis_aresetn = Input(Reset())
    val s_axis_aclk = Input(Clock())
    val s_axis_tvalid = Input(Bool())
    val s_axis_tready = Output(Bool())
    val s_axis_tdata = Input(UInt(width.W))
    val s_axis_tlast = Input(Bool())
    val s_axis_tuser = Input(Bool())
    val m_axis_tvalid = Output(Bool())
    val m_axis_tready = Input(Bool())
    val m_axis_tdata = Output(UInt(width.W))
    val m_axis_tlast = Output(Bool())
    val m_axis_tuser = Output(Bool())
  })
}
