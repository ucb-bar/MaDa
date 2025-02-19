import chisel3._
import chisel3.util._

import java.io.PrintWriter

class Axi4LiteMemory(
  val addressWidth: Int = 12,
  val dataWidth: Int = 32,
  val memoryFileHex: String = "",
  val memoryFileBin: String = ""
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4LiteBundle())
  })

  val mem = Module(new SyncRam(
    addressWidth = addressWidth,
    dataWidth = dataWidth,
    memoryFileHex = memoryFileHex,
    memoryFileBin = memoryFileBin
  ))

  val read_requested = RegInit(false.B)
  val write_requested = RegInit(false.B)

  mem.io.clock := clock
  mem.io.reset := reset

  // data line connections
  mem.io.raddr := io.s_axi.ar.bits.addr
  mem.io.waddr := io.s_axi.aw.bits.addr
  mem.io.wdata := io.s_axi.w.bits.data
  mem.io.wstrb := Mux(io.s_axi.w.fire, io.s_axi.w.bits.strb, 0.U(4.W))
  io.s_axi.r.bits.data := mem.io.rdata

  // control line connections
  io.s_axi.aw.ready := true.B
  io.s_axi.w.ready := true.B
  io.s_axi.b.valid := write_requested
  io.s_axi.b.bits.resp := 0.U

  when (io.s_axi.aw.fire && io.s_axi.w.fire) {
    write_requested := true.B
  }
  .elsewhen (io.s_axi.b.fire) {
    write_requested := false.B
  }

  io.s_axi.ar.ready := true.B
  io.s_axi.r.valid := read_requested
  io.s_axi.r.bits.resp := 0.U

  when (io.s_axi.ar.fire) {
    read_requested := true.B
  }
  .elsewhen (io.s_axi.r.fire) {
    read_requested := false.B
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