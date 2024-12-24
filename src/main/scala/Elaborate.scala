import circt.stage.ChiselStage
import scala.sys.process._
import java.io.PrintWriter
import java.io.File
import java.io.FileFilter

// helper function to parse the module name from the arguments
object ParseModuleName {
  def apply(args: Array[String]): (String, Array[String]) = {
    if (args.length < 1) {
      println("Error: Please provide the module name as an argument")
      System.exit(1)
    }

    args.sliding(2).zipWithIndex.collectFirst {
      case (Array("--module-name", name), i) => (name, args.take(i) ++ args.drop(i + 2))
    }.getOrElse {
      println("Error: Please provide the module name with --module-name flag")
      System.exit(1)
      ("", Array.empty[String])  // This is never reached but needed for type inference
    }
  }
}

object CreateVivadoDirectory {
  def apply(vivado_project_dir: String): Unit = {
    new File(vivado_project_dir).mkdirs()
    new File(s"${vivado_project_dir}/scripts").mkdirs()
  }
}



object GenerateVerilog extends App {
  val (module_name, remaining_args) = ParseModuleName(args)

  val vivado_project_dir = "out/VivadoProject"
  CreateVivadoDirectory(vivado_project_dir)


  val moduleClass = () => {
    val module = Class.forName(module_name)
      .getDeclaredConstructor()
      .newInstance()
      .asInstanceOf[chisel3.RawModule]
    module
  }
  
  println(s"elaborating module: $module_name")
  val chisel_opts = remaining_args ++ Array("--split-verilog")

  val firtool_opts = Array(
    "-disable-all-randomization",
    "-strip-debug-info",
  )

  ChiselStage.emitSystemVerilogFile(
    gen=moduleClass(),
    args=chisel_opts,
    firtoolOpts=firtool_opts
  )
}

object GenerateBitstream extends App {
  val (module_name, remaining_args) = ParseModuleName(args)

  val project_source_dir = "src/main"
  val vivado_project_dir = "out/VivadoProject"

  CreateVivadoDirectory(vivado_project_dir)

  /* Arty A7 100T */
  // val fpga_part = "xc7a100ticsg324-1L"
  // val board_part = "digilentinc.com:arty-a7-100t:part0:1.1"

  /* Arty A7 35T */
  val fpga_part = "xc7a35ticsg324-1L"
  val board_part = "digilentinc.com:arty-a7-35:part0:1.1"

  /* Zedboard */
  // val fpga_part = "xc7z020clg484-1"
  // val board_part = "digilentinc.com:zedboard:part0:1.1"

  // get all files under the generated-src directory
  val sources = new File("generated-src").listFiles(new FileFilter {
    def accept(file: File): Boolean = file.isFile || file.isDirectory
  }).flatMap(file => if (file.isDirectory) file.listFiles().map(_.getAbsolutePath) else Array(file.getAbsolutePath))
  val verilog_sources = new File("src/main/vsrc").listFiles(new FileFilter {
    def accept(file: File): Boolean = file.isFile || file.isDirectory
  }).flatMap(file => if (file.isDirectory) file.listFiles().map(_.getAbsolutePath) else Array(file.getAbsolutePath))

  val excluded_sources = Array(
    "ClockSourceAtFreqMHz.v",
    "SimJTAG.v",
    "SimTSI.v",
    "SimUART.v",
    "TestDriver.v",
  )

  val chipyard_sources = Option(new File("chipyard/sims/verilator/generated-src/chipyard.harness.TestHarness.WithLTraceEncoderRocketConfig/gen-collateral").listFiles(new FileFilter {
    def accept(file: File): Boolean = file.isFile || file.isDirectory
  }))
  .map(files => files.flatMap(file => if (file.isDirectory) file.listFiles().map(_.getAbsolutePath) else Array(file.getAbsolutePath))
    .filterNot(source => excluded_sources.contains(new File(source).getName)))
  .getOrElse(Array.empty[String])  // Return empty array if directory doesn't exist

  {
    // create a run.tcl file
    val run_tcl = new PrintWriter(s"${vivado_project_dir}/scripts/create_project.tcl")

    // create project
    run_tcl.println(s"create_project VivadoProject ${vivado_project_dir} -part ${fpga_part} -force")
    // run_tcl.println(s"set_property board_part $board_part [current_project]")
    
    // add constraints
    run_tcl.println(s"add_files -fileset constrs_1 -norecurse ${project_source_dir}/constraints/Arty-A7-100-Master.xdc")

    run_tcl.println(s"add_files -fileset constrs_1 -norecurse ${project_source_dir}/constraints/axis_async_fifo.tcl")
    run_tcl.println(s"add_files -fileset constrs_1 -norecurse ${project_source_dir}/constraints/eth_mac_fifo.tcl")
    run_tcl.println(s"add_files -fileset constrs_1 -norecurse ${project_source_dir}/constraints/mii_phy_if.tcl")
    run_tcl.println(s"add_files -fileset constrs_1 -norecurse ${project_source_dir}/constraints/sync_reset.tcl")


    // add sources
    run_tcl.print(s"add_files")
    sources.foreach(source => {
      run_tcl.println(s" ${source} \\")
    })
    run_tcl.println("")

    run_tcl.print(s"add_files")
    verilog_sources.foreach(source => {
      run_tcl.println(s" ${source} \\")
    })
    run_tcl.println("")


    if (chipyard_sources.nonEmpty) {
      run_tcl.print(s"add_files")
      chipyard_sources.foreach(source => {
        run_tcl.println(s" ${source} \\")
      })
      run_tcl.println("")
    }

    run_tcl.println(s"set_property top ${module_name} [current_fileset]")

    /* create Vivado IPs */
    run_tcl.println("update_ip_catalog")

    val create_ip_files = new File(s"${vivado_project_dir}/scripts").listFiles(new FileFilter {
      def accept(file: File): Boolean = file.isFile && file.getName.contains("create_ip")
    }).map(_.getAbsolutePath)

    create_ip_files.foreach(file => {
      run_tcl.println(s"source ${file}")
    })


    run_tcl.close()
    run_tcl.flush()
  }

  {
    // create a generate_bitstream.tcl file
    val run_tcl = new PrintWriter(s"${vivado_project_dir}/scripts/generate_bitstream.tcl")

    run_tcl.println(s"open_project ${vivado_project_dir}/VivadoProject.xpr")

    val ip_name = "clk_wiz_0"

    run_tcl.println(s"reset_run ${ip_name}_synth_1")
    run_tcl.println(s"launch_runs ${ip_name}_synth_1")

    run_tcl.println(s"wait_on_run ${ip_name}_synth_1")

    run_tcl.println(s"reset_run synth_1")
    run_tcl.println(s"launch_runs synth_1 -jobs 8")

    run_tcl.println(s"wait_on_run synth_1")

    run_tcl.println(s"update_compile_order -fileset sources_1")
    run_tcl.println(s"launch_runs impl_1 -to_step write_bitstream -jobs 8")
    run_tcl.println(s"wait_on_run impl_1")

    run_tcl.println(s"open_run impl_1")
    run_tcl.println(s"write_bitstream ${vivado_project_dir}/Arty100TShell.bit -force")

    run_tcl.close()
    run_tcl.flush()   // make sure the file is written to the disk
  }



  s"vivado -mode batch -source ${vivado_project_dir}/scripts/create_project.tcl".!
  
  // s"vivado -mode batch -source ${vivado_project_dir}/scripts/generate_bitstream.tcl".!

}
