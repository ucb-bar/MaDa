import chisel3._
import chisel3.util._
import chisel3.util.experimental.decode._

class PrescalerCounter extends Module {
  val io = IO(new Bundle {
    val enable = Input(Bool())
    val target = Input(UInt(32.W))
    val inc = Output(Bool())
  })
  val regval = RegInit(0.U(32.W))
  val wrap = regval === io.target
  when (io.enable) {
    regval := Mux(wrap, 0.U, regval + 1.U)
  }
  io.inc := wrap
}

// register map
// 0x00: control and status
// 0x04: counter value (r)
// 0x08: auto-reload value (rw)
// 0x0c: prescaler value (rw)
// 0x10: capture and control 0 (rw)
// 0x14: capture and control 1 (rw)
// 0x18: capture and control 2 (rw)
// 0x1c: capture and control 3 (rw)
class AXIMadaTimer(n: Int = 4) extends RawModule {
  val io = IO(new Bundle {
    val s_axi = Flipped(new RawAXI4Lite())
    val pwms = Output(Vec(n, Bool()))
  })

  withClockAndReset(io.s_axi.aclk, ~io.s_axi.aresetn) {
    val csr = RegInit(0.U(32.W))
    val auto_reload = RegInit(0.U(32.W))
    val counter_reg = RegInit(0.U(32.W))
    val prescaler = RegInit(0.U(32.W))
    val ccrs = RegInit(VecInit(Seq.fill(n)(0.U(32.W))))

    val reg_mapping = Seq(
      (0x00, csr),
      (0x04, counter_reg),
      (0x08, auto_reload),
      (0x0c, prescaler),
    ) ++ ccrs.zipWithIndex.map { case (reg, i) => (0x10 + i * 4, reg) }

    // read handler
    val r_addr = RegInit(0.U(32.W))
    val r_active = RegInit(false.B)
    io.s_axi.arready := true.B
    io.s_axi.rvalid := r_active
    io.s_axi.rdata := 0.U(32.W)
    io.s_axi.rresp := 0.U(2.W)
    when (io.s_axi.arvalid && io.s_axi.arready) {
      r_addr := io.s_axi.araddr & 0x0FFF.U
      r_active := true.B
    }
    when (io.s_axi.rvalid && io.s_axi.rready) {
      r_active := false.B
    }
    reg_mapping.foreach { case (addr, reg) => 
      when (r_active && r_addr === addr.U) {
        io.s_axi.rdata := reg
      }
    }

    // write handler
    val w_active = RegInit(false.B)
    val aw_active = RegInit(false.B)
    val aw_addr = RegInit(0.U(32.W))
    val w_data = RegInit(0.U(32.W))
    val w_strb = RegInit(0.U(4.W))
    io.s_axi.wready := ~w_active
    io.s_axi.awready := ~aw_active
    io.s_axi.bvalid := w_active && aw_active
    io.s_axi.bresp := 0.U(2.W)
    when (io.s_axi.awvalid && io.s_axi.awready) {
      aw_active := true.B
      aw_addr := io.s_axi.awaddr & 0x0FFF.U
    }
    when (io.s_axi.wvalid && io.s_axi.wready) {
      w_active := true.B
      w_data := io.s_axi.wdata
      w_strb := io.s_axi.wstrb
    }
    when (io.s_axi.bvalid && io.s_axi.bready) {
      // FIXME: support write strb as byte mask
      w_active := false.B
      aw_active := false.B
    }
    reg_mapping.foreach { case (addr, reg) => 
      when (w_active && aw_active && aw_addr === addr.U) {
        reg := w_data
      }
    }
    val enable = csr(0)
    val counter_inc = Wire(Bool())
    val prescaler_counter = Module(new PrescalerCounter())
    prescaler_counter.io.enable := enable
    prescaler_counter.io.target := prescaler
    counter_inc := prescaler_counter.io.inc

    val direction = RegInit(false.B) // false: up, true: down
    when (counter_inc) {
      // detect wrap 1 cycle before target
      when (counter_reg >= auto_reload - 1.U) {
        direction := true.B
      }.elsewhen (counter_reg <= 1.U) {
        direction := false.B
      }
      counter_reg := Mux(direction, 
        Mux(counter_reg === 0.U, 0.U, counter_reg - 1.U), // down, min is 0
        Mux(counter_reg >= auto_reload, auto_reload, counter_reg + 1.U)) // up, max is auto_reload
    }
    io.pwms.zipWithIndex.foreach { case (pwm, i) =>
      pwm := counter_reg >= ccrs(i)
    }
    dontTouch(io.pwms)
  } // end of withClockAndReset
}
