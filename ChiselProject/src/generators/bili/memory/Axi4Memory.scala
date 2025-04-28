import chisel3._
import chisel3.util._

import java.io.PrintWriter


class Axi4Memory(
  params: Axi4Params = Axi4Params(),
  memoryFileHex: String = "",
  memoryFileBin: String = ""
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle(params))
  })

  // bits used to address within a memory line
  val memAlignment = log2Ceil(params.dataWidth / 8)
  // remaining bits are used to address the memory lines
  val memAddressWidth = params.addressWidth - memAlignment

  val mem = Module(new Ram(
    addressWidth = memAddressWidth,
    dataWidth = params.dataWidth,
    memoryFileHex = memoryFileHex,
    memoryFileBin = memoryFileBin
  ))
  
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
  io.s_axi.r.bits.data := mem.io.rdata
  io.s_axi.r.bits.resp := AxResponse.OKAY
  io.s_axi.r.bits.last := (reg_r_state === sREAD_HOLD || reg_r_state === sREAD_END)


  
  // memory connections
  mem.io.clock := clock
  mem.io.reset := reset

  mem.io.raddr := io.s_axi.ar.bits.addr(memAddressWidth-1, memAlignment)
  mem.io.waddr := reg_aw_addr
  mem.io.wdata := reg_w_data
  mem.io.wstrb := reg_w_strb
  io.s_axi.r.bits.data := mem.io.rdata


  def generate_tcl_script(): Unit = {
    if (memoryFileHex != "") {
      val vivado_project_dir = "out/VivadoProject"
      
      // Get current working directory
      val file_path = System.getProperty("user.dir") + "/firmware/" + memoryFileHex
      val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/add_memory_${memoryFileHex}.tcl")
      
      // Use current directory to create paths
      tcl_script.println(s"add_files -norecurse ${file_path}")
      tcl_script.println(s"set_property file_type {Memory Initialization Files} [get_files ${file_path}]")

      tcl_script.close()
    }
  }
  generate_tcl_script()
}


class Axi4MemoryForTest extends Axi4Memory(
  params = Axi4Params(
    addressWidth = 8,
    dataWidth = 32,
    idWidth = 0
  )
)