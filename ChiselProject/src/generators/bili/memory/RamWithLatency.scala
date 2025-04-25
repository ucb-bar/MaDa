import chisel3._
import chisel3.util._


/**
 * SimRam is a simple RAM module for simulation that has a configurable delay.
 * Useful for testing memory access under different latency conditions.
 * 
 * **IMPORTANT**: this design is not synthesizable.
 */
class RamWithLatency(
  val addressWidth: Int = 12,
  val dataWidth: Int = 32,
  val memoryFileHex: String = "",
  val memoryFileBin: String = "",
  val readLatency: Int = 2,
  val writeLatency: Int = 2
) extends Module {
  val io = IO(new Bundle {
    val raddr = Input(UInt(addressWidth.W))
    val waddr = Input(UInt(addressWidth.W))
    val wdata = Input(UInt(dataWidth.W))
    val wstrb = Input(UInt((dataWidth / 8).W))
    val rdata = Output(UInt(dataWidth.W))
  })

  val mem = Module(new Ram(
    addressWidth = addressWidth,
    dataWidth = dataWidth,
    memoryFileHex = memoryFileHex,
    memoryFileBin = memoryFileBin
  ))

  val write_delay = Module(new Pipe(new Bundle {
    val waddr = UInt(addressWidth.W)
    val wdata = UInt(dataWidth.W)
    val wstrb = UInt((dataWidth / 8).W)
  }, writeLatency))

  val read_delay = Module(new Pipe(new Bundle {
    val raddr = UInt(addressWidth.W)
  }, readLatency))

  mem.io.clock := clock
  mem.io.reset := reset

  write_delay.io.enq.valid := true.B
  write_delay.io.enq.bits.waddr := io.waddr
  write_delay.io.enq.bits.wdata := io.wdata
  write_delay.io.enq.bits.wstrb := io.wstrb
  mem.io.waddr := write_delay.io.deq.bits.waddr
  mem.io.wdata := write_delay.io.deq.bits.wdata
  mem.io.wstrb := write_delay.io.deq.bits.wstrb
  
  read_delay.io.enq.valid := true.B
  read_delay.io.enq.bits.raddr := io.raddr
  mem.io.raddr := read_delay.io.deq.bits.raddr
  io.rdata := mem.io.rdata
}

