import chisel3._
import chisel3.util._
import java.io.PrintWriter


class Axi4BlockMemory(
  params: Axi4Params = Axi4Params()
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle(params))
    val rsta_busy = Output(Bool())
    val rstb_busy = Output(Bool())
  })

  val blackbox = Module(new Axi4BlockMemoryBlackbox(params))
  blackbox.io.s_aclk := clock
  blackbox.io.s_aresetn := ~reset.asBool
  blackbox.io.s_axi.connectFrom(io.s_axi)
  io.rsta_busy := blackbox.io.rsta_busy
  io.rstb_busy := blackbox.io.rstb_busy
}

class Axi4BlockMemoryBlackbox(params: Axi4Params = Axi4Params()) extends BlackBox {
  val io = IO(new Bundle {
    val s_aclk = Input(Clock())
    val s_aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4BlackboxBundle(params))
    val rsta_busy = Output(Bool())
    val rstb_busy = Output(Bool())
  })

  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/VivadoProject"
    val ip_name = "Axi4BlockMemoryBlackbox"
    val ip_name_lower = ip_name.toLowerCase()

    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")
    
    tcl_script.println(s"create_ip -name blk_mem_gen -vendor xilinx.com -library ip -version 8.4 -module_name ${ip_name}")

    val fill_unused = "true"

    tcl_script.println(s"""
set_property -dict [list \\
  CONFIG.AXI_Type {AXI4} \\
  CONFIG.Interface_Type {AXI4} \\
  CONFIG.Write_Depth_A {1024} \\
  CONFIG.Load_Init_File {false} \\
  CONFIG.Fill_Remaining_Memory_Locations {${fill_unused}} \\
] [get_ips ${ip_name}]
""")
    
    tcl_script.println(s"generate_target {instantiation_template} [get_ips ${ip_name}]")
    tcl_script.println("update_compile_order -fileset sources_1")
    tcl_script.println(s"generate_target all [get_ips ${ip_name}]")
    tcl_script.println(s"catch { config_ip_cache -export [get_ips -all ${ip_name}] }")
    tcl_script.println(s"export_ip_user_files -of_objects [get_ips ${ip_name}] -no_script -sync -force -quiet")
    tcl_script.println(s"create_ip_run [get_ips ${ip_name}]")

    tcl_script.close()
  }
  generate_tcl_script()
}
