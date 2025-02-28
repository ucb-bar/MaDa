import chisel3._
import chisel3.util._

import java.io.PrintWriter

class Axi4Memory(
  val addressWidth: Int = 12,
  val dataWidth: Int = 32,
  val memoryFileHex: String = "",
  val memoryFileBin: String = ""
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle())
  })

  val mem = Module(new SyncRam(
    addressWidth = addressWidth,
    dataWidth = dataWidth,
    memoryFileHex = memoryFileHex,
    memoryFileBin = memoryFileBin
  ))

  val reg_write_addr_requested = RegInit(false.B)
  val reg_write_addr = RegInit(0.U(addressWidth.W))
  val reg_write_id = RegInit(0.U(4.W))
  val write_addr_requested = reg_write_addr_requested || io.s_axi.aw.fire
  
  val reg_write_requested = RegInit(false.B)

  val reg_read_requested = RegInit(false.B)
  val reg_read_id = RegInit(0.U(4.W))

  mem.io.clock := clock
  mem.io.reset := reset

  // data line connections
  mem.io.raddr := io.s_axi.ar.bits.addr
  mem.io.waddr := Mux(io.s_axi.aw.fire && io.s_axi.w.fire, io.s_axi.aw.bits.addr, reg_write_addr)
  mem.io.wdata := io.s_axi.w.bits.data
  mem.io.wstrb := Mux(io.s_axi.w.fire, io.s_axi.w.bits.strb, 0.U(4.W))
  io.s_axi.r.bits.data := mem.io.rdata

  // control line connections
  io.s_axi.aw.ready := true.B
  io.s_axi.w.ready := true.B
  io.s_axi.b.valid := reg_write_requested
  io.s_axi.b.bits.resp := AxResponse.OKAY
  io.s_axi.b.bits.id := reg_write_id

  when (io.s_axi.aw.fire) {
    reg_write_addr_requested := true.B
    reg_write_addr := io.s_axi.aw.bits.addr
    reg_write_id := io.s_axi.aw.bits.id
  }

  when (io.s_axi.w.fire) {
    reg_write_requested := true.B
  }

  when (io.s_axi.b.fire) {
    reg_write_addr_requested := false.B
    reg_write_requested := false.B
  }

  io.s_axi.ar.ready := true.B
  io.s_axi.r.valid := reg_read_requested
  io.s_axi.r.bits.resp := AxResponse.OKAY
  io.s_axi.r.bits.id := reg_read_id
  io.s_axi.r.bits.last := true.B

  when (io.s_axi.ar.fire) {
    reg_read_requested := true.B
    reg_read_id := io.s_axi.ar.bits.id
  }
  .elsewhen (io.s_axi.r.fire) {
    reg_read_requested := false.B
  }

  
  def generate_tcl_script(): Unit = {
    if (memoryFileHex != "") {
      val vivado_project_dir = "out/VivadoProject"
      val ip_name = "Axi4LiteMemory"
      val ip_name_lower = ip_name.toLowerCase()
      
      // Get current working directory
      val file_path = System.getProperty("user.dir") + "/firmware/" + memoryFileHex
      val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")
      
      // Use current directory to create paths
      tcl_script.println(s"add_files -norecurse ${file_path}")
      tcl_script.println(s"set_property file_type {Memory Initialization Files} [get_files ${file_path}]")

      tcl_script.close()
    }
  }
  generate_tcl_script()
}