import chisel3._
import chisel3.util._

import java.io.PrintWriter


class Axi4CrossbarBlackboxBundle(n: Int, params: Axi4Params = Axi4Params()) extends Bundle {
  val awvalid = Output(UInt(n.W))
  val awready = Input(UInt(n.W))
  val awid = Output(UInt((n*params.idWidth).W))
  val awaddr = Output(UInt((n*params.addressWidth).W))
  val awlen = Output(UInt((n*8).W))
  val awsize = Output(UInt((n*3).W))
  val awburst = Output(UInt((n*2).W))
  
  val wvalid = Output(UInt(n.W))
  val wready = Input(UInt(n.W))
  val wdata = Output(UInt((n*params.dataWidth).W))
  val wstrb = Output(UInt((n*params.dataWidth/8).W))
  val wlast = Output(UInt(n.W))

  val bvalid = Input(UInt(n.W))
  val bready = Output(UInt(n.W))
  val bid = Input(UInt((n*params.idWidth).W))
  val bresp = Input(UInt((n*2).W))

  val arvalid = Output(UInt(n.W))
  val arready = Input(UInt(n.W))
  val arid = Output(UInt((n*params.idWidth).W))
  val araddr = Output(UInt((n*params.addressWidth).W))
  val arlen = Output(UInt((n*8).W))
  val arsize = Output(UInt((n*3).W))
  val arburst = Output(UInt((n*2).W))
  
  val rvalid = Input(UInt(n.W))
  val rready = Output(UInt(n.W))
  val rid = Input(UInt((n*params.idWidth).W))
  val rdata = Input(UInt((n*params.dataWidth).W))
  val rresp = Input(UInt((n*2).W))
  val rlast = Input(UInt(n.W))
}


class Axi4Crossbar(
  numSlave: Int,
  numMaster: Int,
  params: Axi4Params = Axi4Params(),
  device0Size: Int = 1 << 16,  // 64KB address space
  device0Address: BigInt = 0x08000000,
  device1Size: Int = 1 << 12,  // 4KB address space
  device1Address: BigInt = 0x10000000,
  device2Size: Int = 0,
  device2Address: BigInt = 0x18000000,
  device3Size: Int = 0,
  device3Address: BigInt = 0x20000000,
  device4Size: Int = 0,
  device4Address: BigInt = 0x28000000,
  device5Size: Int = 0,
  device5Address: BigInt = 0x30000000,
  ) extends Module {
  val io = IO(new Bundle {
    val s_axi = Flipped(Vec(numSlave, new Axi4Bundle(params)))
    val m_axi = Vec(numMaster, new Axi4Bundle(params))
  })

  val blackbox = Module(new Axi4CrossbarBlackbox(
    numSlave,
    numMaster,
    params,
    device0Size,
    device0Address,
    device1Size,
    device1Address,
    device2Size,
    device2Address,
    device3Size,
    device3Address,
    device4Size,
    device4Address,
    device5Size,
    device5Address
    ))

  blackbox.io.aclk := clock
  blackbox.io.aresetn := ~reset.asBool
  
  // Map the vector of Axi4Lite slaves to a single wide Axi4LiteCrossbarBlackboxBundle
  // this is done because in Xilinx IP, multiple AXI4 Lite interface are concatenated
  // into a single wide AXI4 Lite signals
  blackbox.io.s_axi.awvalid := Cat(io.s_axi.reverse.map(_.aw.valid))
  (io.s_axi zip blackbox.io.s_axi.awready.asBools).foreach { case (s_axi, awready) => s_axi.aw.ready := awready }
  blackbox.io.s_axi.awid := Cat(io.s_axi.reverse.map(_.aw.bits.id))
  blackbox.io.s_axi.awaddr := Cat(io.s_axi.reverse.map(_.aw.bits.addr))
  blackbox.io.s_axi.awlen := Cat(io.s_axi.reverse.map(_.aw.bits.len))
  blackbox.io.s_axi.awsize := Cat(io.s_axi.reverse.map(_.aw.bits.size.asUInt))
  blackbox.io.s_axi.awburst := Cat(io.s_axi.reverse.map(_.aw.bits.burst.asUInt))

  blackbox.io.s_axi.wvalid := Cat(io.s_axi.reverse.map(_.w.valid))
  (io.s_axi zip blackbox.io.s_axi.wready.asBools).foreach { case (s_axi, wready) => s_axi.w.ready := wready }
  blackbox.io.s_axi.wdata := Cat(io.s_axi.reverse.map(_.w.bits.data))
  blackbox.io.s_axi.wstrb := Cat(io.s_axi.reverse.map(_.w.bits.strb))
  blackbox.io.s_axi.wlast := Cat(io.s_axi.reverse.map(_.w.bits.last))

  (io.s_axi zip blackbox.io.s_axi.bvalid.asBools).foreach { case (s_axi, bvalid) => s_axi.b.valid := bvalid }
  blackbox.io.s_axi.bready := Cat(io.s_axi.reverse.map(_.b.ready))
  for (i <- 0 until numSlave) {
    io.s_axi(i).b.bits.id := blackbox.io.s_axi.bid(4*i + 3, 4*i)
    io.s_axi(i).b.bits.resp := AxResponse(blackbox.io.s_axi.bresp(2*i + 1, 2*i))
  }

  blackbox.io.s_axi.arvalid := Cat(io.s_axi.reverse.map(_.ar.valid))
  (io.s_axi zip blackbox.io.s_axi.arready.asBools).foreach { case (s_axi, arready) => s_axi.ar.ready := arready }
  blackbox.io.s_axi.arid := Cat(io.s_axi.reverse.map(_.ar.bits.id))
  blackbox.io.s_axi.araddr := Cat(io.s_axi.reverse.map(_.ar.bits.addr))
  blackbox.io.s_axi.arlen := Cat(io.s_axi.reverse.map(_.ar.bits.len))
  blackbox.io.s_axi.arsize := Cat(io.s_axi.reverse.map(_.ar.bits.size.asUInt))
  blackbox.io.s_axi.arburst := Cat(io.s_axi.reverse.map(_.ar.bits.burst.asUInt))

  (io.s_axi zip blackbox.io.s_axi.rvalid.asBools).foreach { case (s_axi, rvalid) => s_axi.r.valid := rvalid }
  blackbox.io.s_axi.rready := Cat(io.s_axi.reverse.map(_.r.ready))
  for (i <- 0 until numSlave) {
    io.s_axi(i).r.bits.id := blackbox.io.s_axi.rid(4*i + 3, 4*i)
    io.s_axi(i).r.bits.data := blackbox.io.s_axi.rdata(params.dataWidth*i + params.dataWidth-1, params.dataWidth*i)
    io.s_axi(i).r.bits.resp := AxResponse(blackbox.io.s_axi.rresp(2*i + 1, 2*i))
    io.s_axi(i).r.bits.last := blackbox.io.s_axi.rlast(i)
  }

  // Similarly, map the single wide Axi4LiteCrossbarBlackboxBundle to a vector of Axi4Lite masters
  (io.m_axi zip blackbox.io.m_axi.awvalid.asBools).foreach { case (m_axi, awvalid) => m_axi.aw.valid := awvalid }
  blackbox.io.m_axi.awready := Cat(io.m_axi.reverse.map(_.aw.ready))
  for (i <- 0 until numMaster) {
    io.m_axi(i).aw.bits.id := blackbox.io.m_axi.awid(params.idWidth*i + params.idWidth-1, params.idWidth*i)
    io.m_axi(i).aw.bits.addr := blackbox.io.m_axi.awaddr(params.addressWidth*i + params.addressWidth-1, params.addressWidth*i)
    io.m_axi(i).aw.bits.len := blackbox.io.m_axi.awlen(8*i + 7, 8*i)
    io.m_axi(i).aw.bits.size := AxSize(blackbox.io.m_axi.awsize(3*i + 2, 3*i))
    io.m_axi(i).aw.bits.burst := AxBurst(blackbox.io.m_axi.awburst(2*i + 1, 2*i))
  }

  (io.m_axi zip blackbox.io.m_axi.wvalid.asBools).foreach { case (m_axi, wvalid) => m_axi.w.valid := wvalid }
  blackbox.io.m_axi.wready := Cat(io.m_axi.reverse.map(_.w.ready))
  for (i <- 0 until numMaster) {
    io.m_axi(i).w.bits.data := blackbox.io.m_axi.wdata(params.dataWidth*i + params.dataWidth-1, params.dataWidth*i)
    io.m_axi(i).w.bits.strb := blackbox.io.m_axi.wstrb(params.dataWidth/8*i + params.dataWidth/8-1, params.dataWidth/8*i)
    io.m_axi(i).w.bits.last := blackbox.io.m_axi.wlast(i)
  }

  (io.m_axi zip blackbox.io.m_axi.bready.asBools).foreach { case (m_axi, bready) => m_axi.b.ready := bready }
  blackbox.io.m_axi.bvalid := Cat(io.m_axi.reverse.map(_.b.valid))
  blackbox.io.m_axi.bid := Cat(io.m_axi.reverse.map(_.b.bits.id))
  blackbox.io.m_axi.bresp := Cat(io.m_axi.reverse.map(_.b.bits.resp.asUInt))

  (io.m_axi zip blackbox.io.m_axi.arvalid.asBools).foreach { case (m_axi, arvalid) => m_axi.ar.valid := arvalid }
  blackbox.io.m_axi.arready := Cat(io.m_axi.reverse.map(_.ar.ready))
  for (i <- 0 until numMaster) {
    io.m_axi(i).ar.bits.id := blackbox.io.m_axi.arid(params.idWidth*i + params.idWidth-1, params.idWidth*i)
    io.m_axi(i).ar.bits.addr := blackbox.io.m_axi.araddr(params.addressWidth*i + params.addressWidth-1, params.addressWidth*i)
    io.m_axi(i).ar.bits.len := blackbox.io.m_axi.arlen(8*i + 7, 8*i)
    io.m_axi(i).ar.bits.size := AxSize(blackbox.io.m_axi.arsize(3*i + 2, 3*i))
    io.m_axi(i).ar.bits.burst := AxBurst(blackbox.io.m_axi.arburst(2*i + 1, 2*i))
  }
  
  blackbox.io.m_axi.rvalid := Cat(io.m_axi.reverse.map(_.r.valid))
  (io.m_axi zip blackbox.io.m_axi.rready.asBools).foreach { case (m_axi, rready) => m_axi.r.ready := rready }
  blackbox.io.m_axi.rid := Cat(io.m_axi.reverse.map(_.r.bits.id))
  blackbox.io.m_axi.rdata := Cat(io.m_axi.reverse.map(_.r.bits.data))
  blackbox.io.m_axi.rresp := Cat(io.m_axi.reverse.map(_.r.bits.resp.asUInt))
  blackbox.io.m_axi.rlast := Cat(io.m_axi.reverse.map(_.r.bits.last))
}

class Axi4CrossbarBlackbox(
  numSlave: Int,
  numMaster: Int,
  params: Axi4Params,
  device0Size: Int = 1 << 16,  // 64KB address space
  device0Address: BigInt = 0x08000000,
  device1Size: Int = 1 << 12,  // 4KB address space
  device1Address: BigInt = 0x10000000,
  device2Size: Int = 0,
  device2Address: BigInt = 0x18000000,
  device3Size: Int = 0,
  device3Address: BigInt = 0x20000000,
  device4Size: Int = 0,
  device4Address: BigInt = 0x28000000,
  device5Size: Int = 0,
  device5Address: BigInt = 0x30000000,
  ) extends BlackBox {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    val aresetn = Input(Bool())
    val s_axi = Flipped(new Axi4CrossbarBlackboxBundle(numSlave, params))
    val m_axi = new Axi4CrossbarBlackboxBundle(numMaster, params)
  })


  def generate_tcl_script(): Unit = {
    val vivado_project_dir = "out/VivadoProject"
    val ip_name = "Axi4CrossbarBlackbox"
    val ip_name_lower = ip_name.toLowerCase()

    val tcl_script = new PrintWriter(s"${vivado_project_dir}/scripts/create_ip_${ip_name_lower}.tcl")
    
    tcl_script.println(s"create_ip -name axi_crossbar -vendor xilinx.com -library ip -version 2.1 -module_name ${ip_name}")

    tcl_script.println(s"generate_target {instantiation_template} [get_ips ${ip_name}]")

    tcl_script.println("update_compile_order -fileset sources_1")
    tcl_script.println(s"generate_target all [get_ips ${ip_name}]")
    tcl_script.println(s"catch { config_ip_cache -export [get_ips -all ${ip_name}] }")
    tcl_script.println(s"export_ip_user_files -of_objects [get_ips ${ip_name}] -no_script -sync -force -quiet")
    tcl_script.println(s"create_ip_run [get_ips ${ip_name}]")

    tcl_script.println(s"""
set_property -dict [list \\
  CONFIG.PROTOCOL {AXI4} \\
  CONFIG.NUM_MI {${numMaster}} \\
  CONFIG.NUM_SI {${numSlave}} \\
  CONFIG.ID_WIDTH {${params.idWidth}} \\
  CONFIG.DATA_WIDTH {${params.dataWidth}} \\
  CONFIG.S00_SINGLE_THREAD {1} \\
  CONFIG.S00_THREAD_ID_WIDTH {1} \\
  CONFIG.S01_SINGLE_THREAD {1} \\
  CONFIG.S01_THREAD_ID_WIDTH {1} \\
  CONFIG.M00_A00_ADDR_WIDTH {${log2Ceil(device0Size)}} \\
  CONFIG.M00_A00_BASE_ADDR {0x${device0Address.toString(16).reverse.padTo(16, '0').reverse}} \\
  CONFIG.M01_A00_ADDR_WIDTH {${log2Ceil(device1Size)}} \\
  CONFIG.M01_A00_BASE_ADDR {0x${device1Address.toString(16).reverse.padTo(16, '0').reverse}} \\
] [get_ips ${ip_name}]
""")

    tcl_script.close()
  }
  generate_tcl_script()
}