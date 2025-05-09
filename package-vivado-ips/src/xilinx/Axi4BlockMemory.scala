import chisel3._
import chisel3.util._
import amba.{Axi4Params, Axi4Bundle}


class Axi4BlockMemory(
  val params: Axi4Params = Axi4Params(),
  val coeFile: String = "",
) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(new Axi4Bundle(params))
    val rsta_busy = Output(Bool())
    val rstb_busy = Output(Bool())
  })

  val blackbox = Module(new Axi4BlockMemoryBlackbox(params, coeFile))
  blackbox.io.s_aclk := clock
  blackbox.io.s_aresetn := ~reset.asBool
  blackbox.io.s_axi.connectFrom(io.s_axi)
  io.rsta_busy := blackbox.io.rsta_busy
  io.rstb_busy := blackbox.io.rstb_busy
}

class Axi4BlockMemoryBlackbox(
  params: Axi4Params = Axi4Params(),
  coeFile: String = "",
) extends BlackBox {
  val io = IO(new Bundle {
    val s_aclk = Input(Clock())
    val s_aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4BlackboxBundle(params))
    val rsta_busy = Output(Bool())
    val rstb_busy = Output(Bool())
  })



  val ipName = "Axi4BlockMemoryBlackbox"
  val fillUnused = "true"
  
  addVivadoIp(
    name="blk_mem_gen",
    vendor="xilinx.com",
    library="ip",
    version="8.4",
    moduleName=ipName,
    extra=s"""
set_property -dict [list \\
  CONFIG.AXI_Type {AXI4} \\
  CONFIG.Interface_Type {AXI4} \\
  CONFIG.Write_Width_A {${params.dataWidth}} \\
  CONFIG.Write_Depth_A {${(1048576 / params.dataWidth).toInt}} \\
  CONFIG.Load_Init_File {true} \\
  CONFIG.Fill_Remaining_Memory_Locations {${fillUnused}} \\
  CONFIG.Coe_File {${coeFile}} \\
] [get_ips ${ipName}]
"""
  )
}
