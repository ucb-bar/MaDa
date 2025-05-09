/**
 * Builder.scala
 *
 * This file is responsible for elaborating the Chisel design into SystemVerilog. It is the main entry point for
 * generating the hardware description.
 */

import circt.stage.ChiselStage
import scala.sys.process._
import java.io.PrintWriter
import java.io.File
import java.io.FileFilter
import java.io.FileWriter


object BuilderConfig {
  val chiselGeneratedFilelist = "generated-src/filelist.f"
  val simulationFilelist = "generated-src/filelist_simulation.f"
  val constraintsFilelist = "generated-src/filelist_constraints.f"
  
  val vivadoProjectDir = "out/vivado-project"
}


object addSimulationResource {
  def apply(path: String): Unit = {
    println(s"adding Simulation resource: $path")
    val filelists = new File(BuilderConfig.simulationFilelist)
    val writer = new PrintWriter(new FileWriter(filelists, true))
    writer.println(path)
    writer.close()
  }
}

object addConstraintResource {
  def apply(path: String): Unit = {
    println(s"adding Constraint resource: $path")
    val filelists = new File(BuilderConfig.constraintsFilelist)
    val writer = new PrintWriter(new FileWriter(filelists, true))
    writer.println(path)
    writer.close()
  }
}

// helper function to parse the module name from the arguments
object _parseModuleName {
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


object buildVerilog extends App {
  {
    val simulationFilelist = new FileWriter(new File(BuilderConfig.simulationFilelist))
    simulationFilelist.write("")
    simulationFilelist.close()

    val constraintsFilelist = new FileWriter(new File(BuilderConfig.constraintsFilelist))
    constraintsFilelist.write("")
    constraintsFilelist.close()
  }
  new File(BuilderConfig.vivadoProjectDir).mkdirs()
  new File(s"${BuilderConfig.vivadoProjectDir}/scripts").mkdirs()
  

  val (moduleName, remainingArgs) = _parseModuleName(args)

  val moduleClass = () => {
    val module = Class.forName(moduleName)
      .getDeclaredConstructor()
      .newInstance()
      .asInstanceOf[chisel3.RawModule]
    module
  }
  val chiselOpts = remainingArgs ++ Array("--split-verilog")
  val firtoolOpts = Array(
    "-disable-all-randomization",
    "-strip-debug-info",
  )

  ChiselStage.emitSystemVerilogFile(
    gen=moduleClass(),
    args=chiselOpts,
    firtoolOpts=firtoolOpts
  )
}

object buildProject extends App {
  val (moduleName, remainingArgs) = _parseModuleName(args)

  new File(BuilderConfig.vivadoProjectDir).mkdirs()
  new File(s"${BuilderConfig.vivadoProjectDir}/scripts").mkdirs()

  /* Arty A7 100T */
  // val fpgaPart = "xc7a100ticsg324-1L"
  // val boardPart = "digilentinc.com:arty-a7-100t:part0:1.1"

  /* Arty A7 35T */
  val fpgaPart = "xc7a35ticsg324-1L"
  val boardPart = "digilentinc.com:arty-a7-35:part0:1.1"

  /* Zedboard */
  // val fpgaPart = "xc7z020clg484-1"
  // val boardPart = "digilentinc.com:zedboard:part0:1.1"

  val chiselGeneratedSources = scala.io.Source.fromFile(new File(BuilderConfig.chiselGeneratedFilelist))
    .getLines()
    .map(_.trim)
    .filter(_.nonEmpty)
    .map(line => s"generated-src/${line}")
    .toList
  val simulationSources = scala.io.Source.fromFile(new File(BuilderConfig.simulationFilelist))
    .getLines()
    .map(_.trim)
    .filter(_.nonEmpty)
  val constraintResources = scala.io.Source.fromFile(new File(BuilderConfig.constraintsFilelist))
    .getLines()
    .map(_.trim)
    .filter(_.nonEmpty)


  {
    // create a run.tcl file
    val runTcl = new PrintWriter(s"${BuilderConfig.vivadoProjectDir}/scripts/create_project.tcl")

    // create project
    runTcl.println(s"create_project VivadoProject ${BuilderConfig.vivadoProjectDir} -part ${fpgaPart} -force")
    // run_tcl.println(s"set_property board_part $board_part [current_project]")
    
    // add constraints
    if (constraintResources.nonEmpty) {
      runTcl.print(s"add_files -fileset constrs_1 {")
      constraintResources.foreach(filepath => {
        runTcl.println(s" ${filepath} \\")
      })
      runTcl.println("}")
    }

    // add sources
    runTcl.print(s"add_files")
    chiselGeneratedSources.foreach(filepath => {
      runTcl.println(s" ${filepath} \\")
    })
    runTcl.println("")

    if (simulationSources.nonEmpty) {
      runTcl.print(s"add_files -fileset sim_1 {")
      simulationSources.foreach(filepath => {
        runTcl.println(s" ${filepath} \\")
      })
      runTcl.println("}")
    }

    runTcl.println(s"set_property top ${moduleName} [current_fileset]")

    /* create Vivado IPs */
    runTcl.println("update_ip_catalog")

    val create_ip_files = new File(s"${BuilderConfig.vivadoProjectDir}/scripts").listFiles(new FileFilter {
      def accept(file: File): Boolean = file.isFile && file.getName != "create_project.tcl"
    }).map(_.getAbsolutePath)

    create_ip_files.foreach(file => {
      runTcl.println(s"source ${file}")
    })


    // configure simulation settings    
    runTcl.println(s"update_compile_order -fileset sources_1")
    runTcl.println(s"set_property -name {xsim.simulate.runtime} -value {1000us} -objects [get_filesets sim_1]")
    runTcl.println(s"set_property -name {xsim.simulate.log_all_signals} -value {true} -objects [get_filesets sim_1]")
    runTcl.println(s"set_property top ${moduleName}Testbench [get_filesets sim_1]")
    // run_tcl.println(s"set_property top_lib xil_defaultlib [get_filesets sim_1]")


    runTcl.close()
    runTcl.flush()
  }


  s"vivado -mode batch -source ${BuilderConfig.vivadoProjectDir}/scripts/create_project.tcl".!
}

object GenerateBitstream extends App {
  val (module_name, remaining_args) = _parseModuleName(args)


  // {
  //   // create a generate_bitstream.tcl file
  //   val run_tcl = new PrintWriter(s"${vivado_project_dir}/scripts/generate_bitstream.tcl")

  //   run_tcl.println(s"open_project ${vivado_project_dir}/VivadoProject.xpr")

  //   val ip_name = "clk_wiz_0"

  //   run_tcl.println(s"reset_run ${ip_name}_synth_1")
  //   run_tcl.println(s"launch_runs ${ip_name}_synth_1")

  //   run_tcl.println(s"wait_on_run ${ip_name}_synth_1")

  //   run_tcl.println(s"reset_run synth_1")
  //   run_tcl.println(s"launch_runs synth_1 -jobs 8")

  //   run_tcl.println(s"wait_on_run synth_1")

  //   run_tcl.println(s"update_compile_order -fileset sources_1")
  //   run_tcl.println(s"launch_runs impl_1 -to_step write_bitstream -jobs 8")
  //   run_tcl.println(s"wait_on_run impl_1")

  //   run_tcl.println(s"open_run impl_1")
  //   run_tcl.println(s"write_bitstream ${vivado_project_dir}/Arty100TShell.bit -force")


  //   run_tcl.close()
  //   run_tcl.flush()   // make sure the file is written to the disk
  // }

  // s"vivado -mode batch -source ${vivado_project_dir}/scripts/generate_bitstream.tcl".!
}
