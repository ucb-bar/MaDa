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
  
  val reg_aw_active = RegInit(false.B)
  val reg_aw_id = RegInit(0.U(params.idWidth.W))
  val reg_aw_addr = RegInit(0.U(memAddressWidth.W))
  
  val reg_w_active = RegInit(false.B)
  val reg_w_data = RegInit(0.U(params.dataWidth.W))
  val reg_w_strb = RegInit(0.U((params.dataWidth/8).W))

  val reg_ar_active = RegInit(false.B)
  val reg_ar_id = RegInit(0.U(params.idWidth.W))
  val reg_ar_addr = RegInit(0.U(memAddressWidth.W))

  val write_valid = reg_aw_active && reg_w_active

  mem.io.clock := clock
  mem.io.reset := reset

  // data line connections
  mem.io.raddr := Mux(io.s_axi.ar.fire, io.s_axi.ar.bits.addr(memAddressWidth-1, memAlignment), reg_ar_addr)
  mem.io.waddr := reg_aw_addr
  mem.io.wdata := reg_w_data
  mem.io.wstrb := Mux(write_valid, reg_w_strb, 0.U((params.dataWidth/8).W))
  io.s_axi.r.bits.data := mem.io.rdata

  // control line connections
  io.s_axi.aw.ready := ~reg_aw_active
  io.s_axi.w.ready := ~reg_w_active
  io.s_axi.b.valid := write_valid
  io.s_axi.b.bits.id := reg_aw_id
  io.s_axi.b.bits.resp := AxResponse.OKAY

  when (io.s_axi.aw.fire) {
    reg_aw_active := true.B
    reg_aw_id := io.s_axi.aw.bits.id
    reg_aw_addr := io.s_axi.aw.bits.addr(memAddressWidth-1, memAlignment)
  }

  when (io.s_axi.w.fire) {
    reg_w_active := true.B
    reg_w_data := io.s_axi.w.bits.data
    reg_w_strb := io.s_axi.w.bits.strb
  }

  when (io.s_axi.b.fire) {
    reg_aw_active := false.B
    reg_w_active := false.B
  }

  // get around Vivado sim bug
  dontTouch(io.s_axi.aw.fire)
  dontTouch(io.s_axi.w.fire)
  dontTouch(io.s_axi.b.fire)
  dontTouch(write_valid)

  io.s_axi.ar.ready := ~(reg_ar_active && ~io.s_axi.r.fire)
  io.s_axi.r.valid := reg_ar_active
  io.s_axi.r.bits.id := reg_ar_id
  io.s_axi.r.bits.data := mem.io.rdata
  io.s_axi.r.bits.resp := AxResponse.OKAY
  io.s_axi.r.bits.last := true.B

  when (io.s_axi.ar.fire) {
    reg_ar_active := true.B
    reg_ar_id := io.s_axi.ar.bits.id
    reg_ar_addr := io.s_axi.ar.bits.addr(memAddressWidth-1, memAlignment)
  }

  when (io.s_axi.r.fire && ~io.s_axi.ar.fire) {
    reg_ar_active := false.B
  }

  // get around Vivado sim bug
  dontTouch(io.s_axi.ar.fire)
  dontTouch(io.s_axi.r.fire)

  
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