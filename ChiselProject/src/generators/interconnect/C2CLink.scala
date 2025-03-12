import chisel3._
import chisel3.util._


object TransmitState extends ChiselEnum {
  val IDLE          = Value(0.U)
  val HEADER        = Value(1.U)
  val ADDRESS_UPPER = Value(2.U)
  val ADDRESS_LOWER = Value(3.U)
  val DATA          = Value(4.U)
}


class C2CLink extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle())
    val out = Decoupled(UInt(32.W))
    val in = Flipped(Decoupled(UInt(32.W)))
  })

  io.s_axi := DontCare

  io.s_axi.aw.ready := true.B
  io.s_axi.w.ready := true.B
  io.s_axi.ar.ready := true.B

  // AXI channel buffering
  val reg_write_request_pending = RegInit(false.B)
  val reg_read_request_pending = RegInit(false.B)
  
  val reg_write_data = RegInit(0.U(32.W))
  val reg_axi_b_valid = RegInit(false.B)
  val reg_axi_r_valid = RegInit(false.B)
  
  
  // Output channel states
  val reg_transmit_state = RegInit(TransmitState.IDLE)
  
  val reg_out_valid = RegInit(false.B)
  val reg_out_bits = RegInit(0.U(32.W))
  
  
  val reg_out_address = RegInit(0.U(64.W))
  val reg_out_burst_count = RegInit(0.U(16.W))


  when (io.s_axi.aw.fire) {
    reg_write_request_pending := true.B

    reg_out_address := io.s_axi.aw.bits.addr
    reg_out_burst_count := io.s_axi.aw.bits.len
  }

  when (io.s_axi.w.fire) {
    reg_write_data := io.s_axi.w.bits.data

    reg_transmit_state := TransmitState.HEADER
    reg_out_valid := true.B
    reg_out_bits := Cat(true.B, reg_out_burst_count)
  }

  when (io.s_axi.ar.fire) {
    reg_read_request_pending := true.B
    reg_out_address := io.s_axi.ar.bits.addr
    reg_out_burst_count := io.s_axi.ar.bits.len

    reg_transmit_state := TransmitState.HEADER
    reg_out_valid := true.B
    reg_out_bits := Cat(false.B, reg_out_burst_count)
  }

  when (reg_transmit_state === TransmitState.IDLE) {
    when (io.s_axi.b.fire) {
      reg_axi_b_valid := false.B
      reg_write_request_pending := false.B
    }
    when (io.s_axi.r.fire) {
      reg_axi_r_valid := false.B
      reg_read_request_pending := false.B
    }
  }
  when (reg_transmit_state === TransmitState.HEADER) {
    when (io.out.fire) {
      reg_transmit_state := TransmitState.ADDRESS_UPPER
      reg_out_bits := reg_out_address(63, 32)
    }
  }
  when (reg_transmit_state === TransmitState.ADDRESS_UPPER) {
    when (io.out.fire) {
      reg_transmit_state := TransmitState.ADDRESS_LOWER
      reg_out_bits := reg_out_address(31, 0)
    }
  }
  when (reg_transmit_state === TransmitState.ADDRESS_LOWER) {
    when (io.out.fire) {
      when (reg_write_request_pending) {
        reg_transmit_state := TransmitState.DATA
        reg_out_bits := reg_write_data
      }
      when (reg_read_request_pending) {
        reg_transmit_state := TransmitState.IDLE
        reg_out_valid := false.B
        reg_axi_r_valid := true.B
      }
    }
  }
  when (reg_transmit_state === TransmitState.DATA) {
    when (io.out.fire) {
      reg_out_bits := 0.U
      reg_transmit_state := TransmitState.IDLE
      reg_out_valid := false.B
      reg_axi_b_valid := true.B
    }
  }

  io.s_axi.b.valid := reg_axi_b_valid

  io.out.valid := reg_out_valid
  io.out.bits := reg_out_bits

  io.in.ready := true.B
}
