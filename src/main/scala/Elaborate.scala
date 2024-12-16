import circt.stage.ChiselStage
import scala.sys.process._
import java.io.PrintWriter
import java.io.File


object GenerateVerilog extends App {
  val chiselOpts = args ++ Array("--split-verilog")

  val firtoolOpts = Array(
    "-disable-all-randomization",
    "-strip-debug-info",
  )

  ChiselStage.emitSystemVerilogFile(
    gen=new Arty100TShell,
    args=chiselOpts,
    firtoolOpts=firtoolOpts
  )
}

object GenerateBitstream extends App {
  val vivado_project_dir = "out/VivadoProject"
  val fpga_part = "xc7a100ticsg324-1L"
  val board_part = "digilentinc.com:arty-a7-100:part0:1.1"

  // get all files under the generated-src directory
  val sources = new File("generated-src").listFiles().map(_.getAbsolutePath)


  // create directory
  new File(vivado_project_dir).mkdirs()
  new File(s"${vivado_project_dir}/scripts").mkdirs()

  // create a run.tcl file
  val run_tcl = new PrintWriter(s"${vivado_project_dir}/scripts/create_project.tcl")

  // create project
  run_tcl.println(s"create_project VivadoProject ${vivado_project_dir} -part ${fpga_part} -force")
  // run_tcl.println(s"set_property board_part $board_part [current_project]")
  
  // add sources
  sources.foreach(source => {
    run_tcl.println(s"add_files $source")
  })

  // create Vivado IPs
  run_tcl.println("update_ip_catalog")

  val ip_name = "clk_wiz_0"

  run_tcl.println(s"create_ip -name ${ip_name} -vendor xilinx.com -library ip -version 6.0 -module_name ${ip_name}")
  run_tcl.println("""
set_property -dict [list \
  CONFIG.CLKOUT1_JITTER {193.154} \
  CONFIG.CLKOUT1_PHASE_ERROR {109.126} \
  CONFIG.CLKOUT1_REQUESTED_OUT_FREQ {20} \
  CONFIG.MMCM_CLKFBOUT_MULT_F {8.500} \
  CONFIG.MMCM_CLKOUT0_DIVIDE_F {42.500} \
] [get_ips clk_wiz_0]""")

  run_tcl.println(s"generate_target {instantiation_template} [get_ips ${ip_name}]")
  run_tcl.println(s"generate_target all [get_ips ${ip_name}]")
  run_tcl.println(s"export_ip_user_files -of_objects [get_ips ${ip_name}] -no_script -sync -force -quiet")
  run_tcl.println(s"create_ip_run [get_ips ${ip_name}]")

  run_tcl.println(s"reset_run ${ip_name}_synth_1")
  run_tcl.println(s"launch_runs ${ip_name}_synth_1")


  run_tcl.close()

  // make sure the file is written to the disk
  run_tcl.flush()


  s"vivado -mode batch -source ${vivado_project_dir}/scripts/create_project.tcl".!

}
