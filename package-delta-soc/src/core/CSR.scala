package delta

import chisel3._
import chisel3.util._

import Instructions._
import ScalarControlConstants._
import CsrControlConstants._



object CsrAddress {
  val MSTATUS   = 0x300.U
  val MISA      = 0x301.U
  val MTVEC     = 0x305.U

  val MSCRATCH  = 0x340.U
  val MEPC      = 0x341.U
  val MCAUSE    = 0x342.U
  val MTVAL     = 0x343.U
  val MIP       = 0x344.U

  val SYSCALL0  = 0x8C0.U
  val SYSCALL1  = 0x8C1.U
  val SYSCALL2  = 0x8C2.U
  val SYSCALL3  = 0x8C3.U
  val SYSRESP0  = 0xCC0.U
  val SYSRESP1  = 0xCC1.U
  val SYSRESP2  = 0xCC2.U
  val SYSRESP3  = 0xCC3.U

  
  val MCYCLE    = 0xB00.U
  val MINSTRET  = 0xB02.U
  
  val MVENDORID = 0xF11.U
  val MARCHID   = 0xF12.U
  val MIMPID    = 0xF13.U
  val MHARTID   = 0xF14.U
}

class CSR(val XLEN: Int = 32) extends Module {
  val io = IO(new Bundle {
    val addr = Input(UInt(12.W))
    val command = Input(UInt(3.W))
    val in_data = Input(UInt(XLEN.W))
    val out_data = Output(UInt(XLEN.W))
    
    val retire = Input(Bool())

    // debug interface
    val debug = new Bundle {
      val syscall0 = Output(UInt(XLEN.W))
      val syscall1 = Output(UInt(XLEN.W))
      val syscall2 = Output(UInt(XLEN.W))
      val syscall3 = Output(UInt(XLEN.W))
      val sysresp0 = Input(UInt(XLEN.W))
      val sysresp1 = Input(UInt(XLEN.W))
      val sysresp2 = Input(UInt(XLEN.W))
      val sysresp3 = Input(UInt(XLEN.W))
    }
  })


  val reg_csr_syscall0 = RegInit(0.U(XLEN.W))
  val reg_csr_syscall1 = RegInit(0.U(XLEN.W))
  val reg_csr_syscall2 = RegInit(0.U(XLEN.W))
  val reg_csr_syscall3 = RegInit(0.U(XLEN.W))
  val reg_csr_mcycle = RegInit(0.U(XLEN.W))
  val reg_csr_minstret = RegInit(0.U(XLEN.W))
  

  switch (io.addr) {
    is (CsrAddress.SYSCALL0) {
      reg_csr_syscall0 := MuxCase(reg_csr_syscall0, Seq(
        (io.command === CSR_S) -> (reg_csr_syscall0 | io.in_data),
        (io.command === CSR_C) -> (reg_csr_syscall0 & ~io.in_data),
        (io.command === CSR_W) -> io.in_data,
      ))
    }
    is (CsrAddress.SYSCALL1) {
      reg_csr_syscall1 := MuxCase(reg_csr_syscall1, Seq(
        (io.command === CSR_S) -> (reg_csr_syscall1 | io.in_data),
        (io.command === CSR_C) -> (reg_csr_syscall1 & ~io.in_data),
        (io.command === CSR_W) -> io.in_data,
      ))
    }
    is (CsrAddress.SYSCALL2) {
      reg_csr_syscall2 := MuxCase(reg_csr_syscall2, Seq(
        (io.command === CSR_S) -> (reg_csr_syscall2 | io.in_data),
        (io.command === CSR_C) -> (reg_csr_syscall2 & ~io.in_data),
        (io.command === CSR_W) -> io.in_data,
      ))
    }
    is (CsrAddress.SYSCALL3) {
      reg_csr_syscall3 := MuxCase(reg_csr_syscall3, Seq(
        (io.command === CSR_S) -> (reg_csr_syscall3 | io.in_data),
        (io.command === CSR_C) -> (reg_csr_syscall3 & ~io.in_data),
        (io.command === CSR_W) -> io.in_data,
      ))
    }
    is (CsrAddress.MCYCLE) {
      // do nothing, read only
    }
    is (CsrAddress.MINSTRET) {
      // do nothing, read only
    }
  }

  reg_csr_mcycle := reg_csr_mcycle + 1.U


  io.out_data := MuxCase(0.U, Seq(
    (io.addr === CsrAddress.MHARTID) -> 0.U,
    (io.addr === CsrAddress.SYSCALL0) -> reg_csr_syscall0,
    (io.addr === CsrAddress.SYSCALL1) -> reg_csr_syscall1,
    (io.addr === CsrAddress.SYSCALL2) -> reg_csr_syscall2,
    (io.addr === CsrAddress.SYSCALL3) -> reg_csr_syscall3,
    (io.addr === CsrAddress.SYSRESP0) -> io.debug.sysresp0,
    (io.addr === CsrAddress.SYSRESP1) -> io.debug.sysresp1,
    (io.addr === CsrAddress.SYSRESP2) -> io.debug.sysresp2,
    (io.addr === CsrAddress.SYSRESP3) -> io.debug.sysresp3,
    (io.addr === CsrAddress.MCYCLE) -> reg_csr_mcycle,
    (io.addr === CsrAddress.MINSTRET) -> reg_csr_minstret,

  ))

  io.debug.syscall0 := reg_csr_syscall0
  io.debug.syscall1 := reg_csr_syscall1
  io.debug.syscall2 := reg_csr_syscall2
  io.debug.syscall3 := reg_csr_syscall3

  dontTouch(io.addr)
  dontTouch(io.out_data)

  dontTouch(reg_csr_mcycle)
  dontTouch(reg_csr_minstret)
}
