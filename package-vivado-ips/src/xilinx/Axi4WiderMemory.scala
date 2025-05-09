import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4Bundle, AxResponse}


class Axi4WiderMemory(
  val params: Axi4Params = Axi4Params(),
  val coeFile: String = "",
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle(params))
  })

  // bits used to address within a memory line
  val memAlignment = log2Ceil(params.dataWidth / 8)
  // remaining bits are used to address the memory lines
  val memAddressWidth = params.addressWidth - memAlignment

  val blackbox = Module(new WiderBlockMemoryBlackbox(params, coeFile))

  // implementing AXI4 state machine from https://zipcpu.com/blog/2019/05/29/demoaxi.html
  val sWRITE_IDLE :: sWRITE_MID :: sWRITE_LAST :: sWRITE_END :: Nil = Enum(4)
  val reg_w_state = RegInit(sWRITE_IDLE)
  val reg_aw_id = RegInit(0.U(params.idWidth.W))
  val reg_aw_addr = RegInit(0.U(memAddressWidth.W))
  val reg_w_data = RegInit(0.U(params.dataWidth.W))
  val reg_w_strb = RegInit(0.U((params.dataWidth/8).W))

  switch (reg_w_state) {
    is (sWRITE_IDLE) {  // IDLE state
      when (io.s_axi.aw.fire) {
        when (io.s_axi.w.fire) {
          // transition to END state
          reg_w_state := sWRITE_END
          reg_w_data := io.s_axi.w.bits.data
          reg_w_strb := io.s_axi.w.bits.strb
        }
        .otherwise {
          // transition to LAST state
          reg_w_state := sWRITE_LAST
        }
        reg_aw_id := io.s_axi.aw.bits.id
        reg_aw_addr := io.s_axi.aw.bits.addr(memAddressWidth-1, memAlignment)
      }
    }
    is (sWRITE_MID) {  // MID state
    }
    is (sWRITE_LAST) {  // LAST state
      when (io.s_axi.w.fire) {
        // transition to END state
        reg_w_state := sWRITE_END
        reg_w_data := io.s_axi.w.bits.data
        reg_w_strb := io.s_axi.w.bits.strb
      }
    }
    is (sWRITE_END) {  // ENDB state
      when (io.s_axi.b.fire) {
        // transition to IDLE state
        reg_w_state := sWRITE_IDLE
        reg_w_strb := 0.U((params.dataWidth/8).W)
      }
    }
  }

  io.s_axi.aw.ready := (reg_w_state === sWRITE_IDLE || reg_w_state === sWRITE_END)
  io.s_axi.w.ready := (reg_w_state === sWRITE_IDLE || reg_w_state === sWRITE_MID || reg_w_state === sWRITE_LAST || reg_w_state === sWRITE_END)
  io.s_axi.b.valid := (reg_w_state === sWRITE_END)
  io.s_axi.b.bits.id := reg_aw_id
  io.s_axi.b.bits.resp := AxResponse.OKAY


  val sREAD_IDLE :: sREAD_MID :: sREAD_HOLD :: sREAD_END :: Nil = Enum(4)
  val reg_r_state = RegInit(sREAD_IDLE)
  val reg_ar_id = RegInit(0.U(params.idWidth.W))

  switch (reg_r_state) {
    is (sREAD_IDLE) {  // IDLE state
      when (io.s_axi.ar.fire) {
        // transition to END state
        reg_r_state := sREAD_END
        reg_ar_id := io.s_axi.ar.bits.id
      }
    }
    is (sREAD_MID) {  // MID state

    }
    is (sREAD_HOLD) {  // HOLD state
      when (io.s_axi.r.fire) {
        // transition to END state
        reg_r_state := sREAD_END
      }
    }
    is (sREAD_END) {  // END state
      when (io.s_axi.r.fire && ~io.s_axi.ar.fire) {
        // transition to IDLE state
        reg_r_state := sREAD_IDLE
      }
      when (io.s_axi.r.fire && io.s_axi.ar.fire) {
        // stay in END state
        reg_r_state := sREAD_END
      }
    }
  }

  io.s_axi.ar.ready := (reg_r_state === sREAD_IDLE || reg_r_state === sREAD_END)
  io.s_axi.r.valid := (reg_r_state === sREAD_MID || reg_r_state === sREAD_HOLD || reg_r_state === sREAD_END)
  io.s_axi.r.bits.id := reg_ar_id
  io.s_axi.r.bits.data := blackbox.io.douta
  io.s_axi.r.bits.resp := AxResponse.OKAY
  io.s_axi.r.bits.last := (reg_r_state === sREAD_HOLD || reg_r_state === sREAD_END)

  // memory connections
  blackbox.io.clka := clock
  blackbox.io.ena := !reset.asBool

  blackbox.io.addra := Mux(io.s_axi.ar.fire, io.s_axi.ar.bits.addr(memAddressWidth-1, memAlignment), reg_aw_addr)
  blackbox.io.dina := reg_w_data
  blackbox.io.wea := reg_w_strb
}

class WiderBlockMemoryBlackbox(
  params: Axi4Params = Axi4Params(),
  coeFile: String = "",
) extends BlackBox {
  val io = IO(new Bundle {
    val clka = Input(Clock())
    val ena = Input(Bool())
    val wea = Input(UInt(params.dataWidth.W))
    val addra = Input(UInt(params.addressWidth.W))
    val dina = Input(UInt(params.dataWidth.W))
    val douta = Output(UInt(params.dataWidth.W))
  })

  val ipName = "WiderBlockMemoryBlackbox"
  val fillUnused = "true"
  addVivadoIp(
    name="blk_mem_gen",
    vendor="xilinx.com",
    library="ip",
    version="8.4",
    moduleName=ipName,
    extra=s"""
set_property -dict [list \\
  CONFIG.Use_Byte_Write_Enable {true} \\
  CONFIG.Byte_Size {8} \\
  CONFIG.Enable_32bit_Address {false} \\
  CONFIG.Write_Width_A {${params.dataWidth}} \\
  CONFIG.Write_Depth_A {2048} \\
  CONFIG.Register_PortA_Output_of_Memory_Primitives {false} \\
  CONFIG.Load_Init_File {true} \\
  CONFIG.Fill_Remaining_Memory_Locations {${fillUnused}} \\
  CONFIG.Coe_File {${coeFile}} \\
] [get_ips ${ipName}]
"""
  )
}

