import chisel3._
import chisel3.util._


/**
 * SimAxi4LiteMemory is a simple memory module for simulation that has a configurable latency.
 * Useful for testing AXI4-Lite memory access under different latency conditions.
 * 
 * **IMPORTANT**: this design is not synthesizable.
 */
class SimAxi4LiteMemory(
  val readLatency: Int = 2,
  val writeLatency: Int = 2,
  val awToWDelay: Int = 2
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBundle())
  })

  val mem = Module(new SimRam(
    readLatency = readLatency,
    writeLatency = writeLatency
  ))

  val reg_write_addr_requested = RegInit(false.B)
  val write_addr_requested = reg_write_addr_requested || io.s_axi.aw.fire
  val (write_addr_wait_counter, write_addr_wait_done) = Counter(write_addr_requested, awToWDelay)
  val write_addr_keep = RegInit(false.B)

  val reg_write_requested = RegInit(false.B)
  val write_requested = reg_write_requested || io.s_axi.aw.fire
  val (write_wait_counter, write_wait_done) = Counter(write_requested, writeLatency)

  val reg_read_requested = RegInit(false.B)
  val read_requested = reg_read_requested || io.s_axi.ar.fire
  val (read_wait_counter, read_wait_done) = Counter(read_requested, readLatency)
  
  // data line connections
  mem.io.raddr := io.s_axi.ar.bits.addr(io.s_axi.ar.bits.addr.getWidth - 1, 2)
  mem.io.waddr := io.s_axi.aw.bits.addr(io.s_axi.aw.bits.addr.getWidth - 1, 2)
  mem.io.wdata := io.s_axi.w.bits.data
  mem.io.wstrb := Mux(io.s_axi.w.fire, io.s_axi.w.bits.strb, 0.U(4.W))
  io.s_axi.r.bits.data := mem.io.rdata

  // control line connections
  io.s_axi.aw.ready := true.B
  io.s_axi.w.ready := write_addr_keep
  io.s_axi.b.valid := write_requested && write_wait_done
  io.s_axi.b.bits.resp := 0.U

  dontTouch(io.s_axi.w.ready)

  when (io.s_axi.aw.fire) {
    reg_write_addr_requested := true.B
  }
  when (write_addr_wait_done) {
    write_addr_keep := true.B
  }
  
  when (io.s_axi.w.fire) {
    reg_write_requested := true.B
  }
  
  when (io.s_axi.b.fire) {
    reg_write_addr_requested := false.B
    reg_write_requested := false.B
    write_addr_keep := false.B
  }

  io.s_axi.ar.ready := true.B
  io.s_axi.r.valid := read_requested && read_wait_done
  io.s_axi.r.bits.resp := 0.U

  when (io.s_axi.ar.fire) {
    reg_read_requested := true.B
  }
  .elsewhen (io.s_axi.r.fire) {
    reg_read_requested := false.B
  }
}