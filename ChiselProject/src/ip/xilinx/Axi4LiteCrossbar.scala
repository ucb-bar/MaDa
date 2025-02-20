import chisel3._
import chisel3.util._

import java.io.PrintWriter


class Axi4LiteCrossbarBlackboxBundle(n: Int) extends Bundle {
  val awaddr = Output(UInt((n*32).W))
  val awvalid = Output(UInt(n.W))
  val awready = Input(UInt(n.W))

  val wdata = Output(UInt((n*32).W))
  val wstrb = Output(UInt((n*4).W))
  val wvalid = Output(UInt(n.W))
  val wready = Input(UInt(n.W))

  val bresp = Input(UInt((n*2).W))
  val bvalid = Input(UInt(n.W))
  val bready = Output(UInt(n.W))

  val araddr = Output(UInt((n*32).W))
  val arvalid = Output(UInt(n.W))
  val arready = Input(UInt(n.W))

  val rdata = Input(UInt((n*32).W))
  val rresp = Input(UInt((n*2).W))
  val rvalid = Input(UInt(n.W))
  val rready = Output(UInt(n.W))
}


class Axi4LiteCrossbar(n_slave: Int, n_master: Int) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(Vec(n_slave, new Axi4LiteBundle()))
    val m_axi = Vec(n_master, new Axi4LiteBundle())
  })

  val blackbox = Module(new Axi4LiteCrossbarBlackbox(n_slave, n_master))

  blackbox.io.aclk := clock
  blackbox.io.aresetn := ~reset.asBool
  
  // Map the vector of Axi4Lite slaves to a single wide Axi4LiteCrossbarBlackboxBundle
  // this is done because in Xilinx IP, multiple AXI4 Lite interface are concatenated
  // into a single wide AXI4 Lite signals
  blackbox.io.s_axi.awvalid := Cat(io.s_axi.map(_.aw.valid))
  blackbox.io.s_axi.awaddr := Cat(io.s_axi.map(_.aw.bits.addr))
  (io.s_axi zip blackbox.io.s_axi.awready.asBools).foreach { case (s_axi, awready) => s_axi.aw.ready := awready }

  blackbox.io.s_axi.wvalid := Cat(io.s_axi.map(_.w.valid))
  blackbox.io.s_axi.wdata := Cat(io.s_axi.map(_.w.bits.data))
  blackbox.io.s_axi.wstrb := Cat(io.s_axi.map(_.w.bits.strb))
  (io.s_axi zip blackbox.io.s_axi.wready.asBools).foreach { case (s_axi, wready) => s_axi.w.ready := wready }

  (io.s_axi zip blackbox.io.s_axi.bvalid.asBools).foreach { case (s_axi, bvalid) => s_axi.b.valid := bvalid }
  for (i <- 0 until n_slave) {
    io.s_axi(i).b.bits.resp := blackbox.io.s_axi.bresp(2*i + 1, 2*i)
  }
  blackbox.io.s_axi.bready := Cat(io.s_axi.map(_.b.ready))

  blackbox.io.s_axi.arvalid := Cat(io.s_axi.map(_.ar.valid))
  blackbox.io.s_axi.araddr := Cat(io.s_axi.map(_.ar.bits.addr))
  (io.s_axi zip blackbox.io.s_axi.arready.asBools).foreach { case (s_axi, arready) => s_axi.ar.ready := arready }

  (io.s_axi zip blackbox.io.s_axi.rvalid.asBools).foreach { case (s_axi, rvalid) => s_axi.r.valid := rvalid }
  for (i <- 0 until n_slave) {
    io.s_axi(i).r.bits.data := blackbox.io.s_axi.rdata(32*i + 31, 32*i)
    io.s_axi(i).r.bits.resp := blackbox.io.s_axi.rresp(2*i + 1, 2*i)
  }
  blackbox.io.s_axi.rready := Cat(io.s_axi.map(_.r.ready))

  // Similarly, map the single wide Axi4LiteCrossbarBlackboxBundle to a vector of Axi4Lite masters
  (io.m_axi zip blackbox.io.m_axi.awvalid.asBools).foreach { case (m_axi, awvalid) => m_axi.aw.valid := awvalid }
  for (i <- 0 until n_master) {
    io.m_axi(i).aw.bits.addr := blackbox.io.m_axi.awaddr(32*i + 31, 32*i)
  }
  blackbox.io.m_axi.awready := Cat(io.m_axi.reverse.map(_.aw.ready))

  (io.m_axi zip blackbox.io.m_axi.wvalid.asBools).foreach { case (m_axi, wvalid) => m_axi.w.valid := wvalid }
  for (i <- 0 until n_master) {
    io.m_axi(i).w.bits.data := blackbox.io.m_axi.wdata(32*i + 31, 32*i)
    io.m_axi(i).w.bits.strb := blackbox.io.m_axi.wstrb(4*i + 3, 4*i)
  }
  blackbox.io.m_axi.wready := Cat(io.m_axi.reverse.map(_.w.ready))

  (io.m_axi zip blackbox.io.m_axi.bready.asBools).foreach { case (m_axi, bready) => m_axi.b.ready := bready }
  blackbox.io.m_axi.bresp := Cat(io.m_axi.reverse.map(_.b.bits.resp))
  blackbox.io.m_axi.bvalid := Cat(io.m_axi.reverse.map(_.b.valid))

  (io.m_axi zip blackbox.io.m_axi.arvalid.asBools).foreach { case (m_axi, arvalid) => m_axi.ar.valid := arvalid }
  for (i <- 0 until n_master) {
    io.m_axi(i).ar.bits.addr := blackbox.io.m_axi.araddr(32*i + 31, 32*i)
  }
  blackbox.io.m_axi.arready := Cat(io.m_axi.reverse.map(_.ar.ready))
  
  blackbox.io.m_axi.rdata := Cat(io.m_axi.reverse.map(_.r.bits.data))
  blackbox.io.m_axi.rresp := Cat(io.m_axi.reverse.map(_.r.bits.resp))
  blackbox.io.m_axi.rvalid := Cat(io.m_axi.reverse.map(_.r.valid))
  (io.m_axi zip blackbox.io.m_axi.rready.asBools).foreach { case (m_axi, rready) => m_axi.r.ready := rready }
}

class Axi4LiteCrossbarBlackbox(n_slave: Int, n_master: Int) extends BlackBox {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    val aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4LiteCrossbarBlackboxBundle(n_slave))
    val m_axi = new Axi4LiteCrossbarBlackboxBundle(n_master)
  })


  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/VivadoProject"
    val ip_name = "Axi4LiteCrossbarBlackbox"
    val ip_name_lower = ip_name.toLowerCase()

    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")
    
    tcl_script.println(s"create_ip -name axi_crossbar -vendor xilinx.com -library ip -version 2.1 -module_name ${ip_name}")
  
    tcl_script.println(s"""
set_property -dict [list \\
  CONFIG.M00_A00_ADDR_WIDTH {16} \\
  CONFIG.M00_A00_BASE_ADDR {0x0000000008000000} \\
  CONFIG.M01_A00_ADDR_WIDTH {12} \\
  CONFIG.M01_A00_BASE_ADDR {0x0000000010000000} \\
  CONFIG.PROTOCOL {AXI4LITE} \\
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