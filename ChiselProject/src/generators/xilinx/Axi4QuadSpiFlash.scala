import chisel3._
import chisel3.util._

import java.io.PrintWriter

class Axi4QuadSpiFlash extends Module {
  val io = IO(new Bundle {
    val ext_spi_clk = Input(Clock())
    val s_axi = Flipped(new Axi4LiteBundle())
    val s_axi4 = Flipped(new Axi4Bundle())
    val io0_i = Input(Bool())
    val io0_o = Output(Bool())
    val io0_t = Output(Bool())
    val io1_i = Input(Bool())
    val io1_o = Output(Bool())
    val io1_t = Output(Bool())
    val io2_i = Input(Bool())
    val io2_o = Output(Bool())
    val io2_t = Output(Bool())
    val io3_i = Input(Bool())
    val io3_o = Output(Bool())
    val io3_t = Output(Bool())
    // val sck_i = Input(Bool())
    // val sck_o = Output(Bool())
    // val sck_t = Output(Bool())
    val ss_i = Input(Bool())
    val ss_o = Output(Bool())
    val ss_t = Output(Bool())
    val ip2intc_irpt = Output(Bool())
  })

  val blackbox = Module(new Axi4QuadSpiFlashBlackbox())

  blackbox.io.ext_spi_clk := io.ext_spi_clk
  blackbox.io.s_axi_aclk := clock
  blackbox.io.s_axi_aresetn := ~reset.asBool
  blackbox.io.s_axi.connectFrom(io.s_axi)
  blackbox.io.s_axi4_aclk := clock
  blackbox.io.s_axi4_aresetn := ~reset.asBool
  blackbox.io.s_axi4.connectFrom(io.s_axi4)
  io.s_axi4.aw.ready := false.B
  io.s_axi4.w.ready := false.B
  io.s_axi4.b.valid := false.B
  blackbox.io.s_axi4.awaddr := 0x00000000.U
  blackbox.io.s_axi4.awvalid := false.B
  blackbox.io.s_axi4_awlock := 0.B
  blackbox.io.s_axi4_awcache := 0.U
  blackbox.io.s_axi4_awprot := 0.U
  blackbox.io.s_axi4.wdata := 0x00000000.U
  blackbox.io.s_axi4.wstrb := 0x00.U
  blackbox.io.s_axi4.wlast := false.B
  blackbox.io.s_axi4.wvalid := false.B
  blackbox.io.s_axi4.bready := false.B
  blackbox.io.s_axi4_arlock := 0.B
  blackbox.io.s_axi4_arcache := 0.U
  blackbox.io.s_axi4_arprot := 0.U

  blackbox.io.io0_i := io.io0_i
  io.io0_o := blackbox.io.io0_o
  io.io0_t := blackbox.io.io0_t
  blackbox.io.io1_i := io.io1_i
  io.io1_o := blackbox.io.io1_o
  io.io1_t := blackbox.io.io1_t
  blackbox.io.io2_i := io.io2_i
  io.io2_o := blackbox.io.io2_o
  io.io2_t := blackbox.io.io2_t
  blackbox.io.io3_i := io.io3_i
  io.io3_o := blackbox.io.io3_o
  io.io3_t := blackbox.io.io3_t 
  // blackbox.io.sck_i := io.sck_i
  // io.sck_o := blackbox.io.sck_o
  // io.sck_t := blackbox.io.sck_t
  blackbox.io.ss_i := io.ss_i
  io.ss_o := blackbox.io.ss_o
  io.ss_t := blackbox.io.ss_t
  io.ip2intc_irpt := blackbox.io.ip2intc_irpt

  def connectFrom(axi: Axi4LiteBundle): Unit = {
    io.s_axi <> axi
  }

  def connectFrom(axi: Axi4Bundle): Unit = {
    io.s_axi.connectFromAxi4(axi)
  }
}

class Axi4QuadSpiFlashBlackbox extends BlackBox {
  val io = IO(new Bundle {
    val ext_spi_clk = Input(Clock())
    val s_axi_aclk = Input(Clock())
    val s_axi_aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4LiteBlackboxBundle())
    val s_axi4_aclk = Input(Clock())
    val s_axi4_aresetn = Input(Bool())
    val s_axi4 = Flipped(new Axi4BlackboxBundle())
    val s_axi4_awlock = Input(Bool())
    val s_axi4_awcache = Input(UInt(4.W))
    val s_axi4_awprot = Input(UInt(3.W))
    val s_axi4_arlock = Input(Bool())
    val s_axi4_arcache = Input(UInt(4.W))
    val s_axi4_arprot = Input(UInt(3.W))
    val io0_i = Input(Bool())
    val io0_o = Output(Bool())
    val io0_t = Output(Bool())
    val io1_i = Input(Bool())
    val io1_o = Output(Bool())
    val io1_t = Output(Bool())
    val io2_i = Input(Bool())
    val io2_o = Output(Bool())
    val io2_t = Output(Bool())
    val io3_i = Input(Bool())
    val io3_o = Output(Bool())
    val io3_t = Output(Bool())
    // val sck_i = Input(Bool())
    // val sck_o = Output(Bool())
    // val sck_t = Output(Bool())
    val ss_i = Input(Bool())
    val ss_o = Output(Bool())
    val ss_t = Output(Bool())
    val ip2intc_irpt = Output(Bool())
  })

  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/VivadoProject"
    val ip_name = "Axi4QuadSpiFlashBlackbox"
    val ip_name_lower = ip_name.toLowerCase()

    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")
    
    tcl_script.println(s"create_ip -name axi_quad_spi -vendor xilinx.com -library ip -version 3.2 -module_name ${ip_name}")

    // the Flash memory used on the Arty is Spansion S25FL128S
    // it has 8 dummy cycles for single-mode read commands
    // and 6 dummy cycles for quad-mode read commands
    tcl_script.println(s"""
set_property -dict [list \\
  CONFIG.C_SPI_MEMORY {3} \\
  CONFIG.C_USE_STARTUP {0} \\
  CONFIG.C_XIP_MODE {1} \\
  CONFIG.C_XIP_PERF_MODE {0} \\
  CONFIG.C_USE_STARTUP {1} \\
  CONFIG.C_SPI_MODE {2} \\
] [get_ips ${ip_name}]
""")

    tcl_script.println(s"generate_target {instantiation_template} [get_ips ${ip_name}]")
    tcl_script.println("update_compile_order -fileset sources_1")
    tcl_script.println(s"generate_target all [get_ips ${ip_name}]")
    tcl_script.println(s"catch { config_ip_cache -export [get_ips -all ${ip_name}] }")
    tcl_script.println(s"export_ip_user_files -of_objects [get_ips ${ip_name}] -no_script -sync -force -quiet")
    tcl_script.println(s"create_ip_run [get_ips ${ip_name}]")

    // HACK: add flash memory file to Vivado project
      // Get current working directory
      val file_path = System.getProperty("user.dir") + "/firmware/" + "firmware.flash.8.hex"
      
      // Use current directory to create paths
      tcl_script.println(s"add_files -norecurse ${file_path}")
      tcl_script.println(s"set_property file_type {Memory Initialization Files} [get_files ${file_path}]")

    tcl_script.close()
  }
  generate_tcl_script()
}

