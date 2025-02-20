import chisel3._
import chisel3.util._

import Instructions._
import ScalarControlConstants._
import CsrControlConstants._


class CSR extends Module {
  val io = IO(new Bundle {
    val addr = Input(UInt(12.W))
    val command = Input(UInt(3.W))
    val in_data = Input(UInt(32.W))
    val out_data = Output(UInt(32.W))
    
    val retire = Input(Bool())

    // debug interface
    val tohost = Output(UInt(32.W))
  })

  val reg_csr_tohost = RegInit(0.U(32.W))
  val reg_csr_retire = RegInit(0.U(32.W))

  when (io.command === CSR_S) {
    reg_csr_tohost := reg_csr_tohost | io.in_data
  }
  .elsewhen (io.command === CSR_C) {
    reg_csr_tohost := reg_csr_tohost & ~io.in_data
  }
  .elsewhen (io.command === CSR_W) {
    reg_csr_tohost := io.in_data
  }


  io.out_data := MuxCase(0.U, Seq(
    (io.addr === 0x51E.U) -> reg_csr_tohost,
  ))

  io.tohost := reg_csr_tohost

  dontTouch(io.addr)
  dontTouch(io.out_data)
  dontTouch(reg_csr_tohost)
}
