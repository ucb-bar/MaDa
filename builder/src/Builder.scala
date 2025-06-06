package builder

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
  
  val vivadoTclDir = "out/vivado-tcl"
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

object addVivadoTclScript {
  def apply(path: String, content: String): Unit = {
    println(s"adding Vivado TCL script: $path")
    val file = new File(BuilderConfig.vivadoTclDir + "/" + path)

    // ensure the directory exists
    file.getParentFile.mkdirs()

    val writer = new PrintWriter(new FileWriter(file))
    writer.println(content)
    writer.close()
  }
}

object addVivadoIp {
  def apply(
    name: String,
    vendor: String,
    library: String,
    version: String,
    moduleName: String,
    extra: String,
    ): Unit = {
    addVivadoTclScript(s"ip/create_ip_${moduleName.toLowerCase()}.tcl", {
      s"""
create_ip -name ${name} -vendor ${vendor} -library ${library} -version ${version} -module_name ${moduleName}
generate_target {instantiation_template} [get_ips ${moduleName}]
update_compile_order -fileset sources_1
generate_target all [get_ips ${moduleName}]
catch { config_ip_cache -export [get_ips -all ${moduleName}] }
export_ip_user_files -of_objects [get_ips ${moduleName}] -no_script -sync -force -quiet
create_ip_run [get_ips ${moduleName}]
${extra}
"""
    })
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
      case (Array("--design-name", name), i) => (name, args.take(i) ++ args.drop(i + 2))
    }.getOrElse {
      println("Error: Please provide the design name with --design-name flag")
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
  new File(BuilderConfig.vivadoTclDir).mkdirs()

  val (designName, remainingArgs) = _parseModuleName(args)

  val designClass = () => {
    val design = Class.forName(designName)
      .getDeclaredConstructor()
      .newInstance()
      .asInstanceOf[chisel3.RawModule]
    design
  }
  val chiselOpts = remainingArgs ++ Array("--split-verilog")
  val firtoolOpts = Array(
    "-disable-all-randomization",
    "-strip-debug-info",
  )

  ChiselStage.emitSystemVerilogFile(
    gen=designClass(),
    args=chiselOpts,
    firtoolOpts=firtoolOpts
  )
}

object buildProject extends App {
  val (designName, remainingArgs) = _parseModuleName(args)

  new File(BuilderConfig.vivadoProjectDir).mkdirs()
  new File(BuilderConfig.vivadoTclDir).mkdirs()

  /* Arty A7 100T */
  // val fpgaPart = "xc7a100ticsg324-1L"
  // val boardPart = "digilentinc.com:arty-a7-100t:part0:1.1"

  /* Arty A7 35T */
  val fpgaPart = "xc7a35ticsg324-1L"
  val boardPart = "digilentinc.com:arty-a7-35:part0:1.1"

  /* Zedboard */
  // val fpgaPart = "xc7z020clg484-1"
  // val boardPart = "digilentinc.com:zedboard:part0:1.1"

  // HACK: the blackboxed sources are not included in the filelist.f, so we need to instead scan for the entire generated-src directory
  // val chiselGeneratedSources = scala.io.Source.fromFile(new File(BuilderConfig.chiselGeneratedFilelist))
  //   .getLines()
  //   .map(_.trim)
  //   .filter(_.nonEmpty)
  //   .map(line => s"generated-src/${line}")
  //   .toList
  val chiselGeneratedSources = new File("generated-src").listFiles(new FileFilter {
    def accept(file: File): Boolean = file.isFile || file.isDirectory && !file.getName.endsWith(".f")
  }).flatMap(file => if (file.isDirectory) file.listFiles().map(_.getAbsolutePath) else Array(file.getAbsolutePath))

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
    val runTcl = new PrintWriter(s"${BuilderConfig.vivadoTclDir}/create_project.tcl")

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

    val designClassName = designName.split("\\.").last
    runTcl.println(s"set_property top ${designClassName} [current_fileset]")

    /* create Vivado IPs */
    runTcl.println("update_ip_catalog")

    val create_ip_files = new File(BuilderConfig.vivadoTclDir + "/ip").listFiles(new FileFilter {
      def accept(file: File): Boolean = file.isFile
    }).map(_.getAbsolutePath)

    create_ip_files.foreach(file => {
      runTcl.println(s"source ${file}")
    })


    // configure simulation settings    
    runTcl.println(s"update_compile_order -fileset sources_1")
    runTcl.println(s"set_property -name {xsim.simulate.runtime} -value {1000us} -objects [get_filesets sim_1]")
    runTcl.println(s"set_property -name {xsim.simulate.log_all_signals} -value {true} -objects [get_filesets sim_1]")
    runTcl.println(s"set_property top ${designClassName}Testbench [get_filesets sim_1]")
    // run_tcl.println(s"set_property top_lib xil_defaultlib [get_filesets sim_1]")

    runTcl.println(s"close_project")

    runTcl.close()
    runTcl.flush()
  }


  s"vivado -mode batch -source ${BuilderConfig.vivadoTclDir}/create_project.tcl".!
}

object buildBitstream extends App {
  val (designName, remainingArgs) = _parseModuleName(args)

  {
    // create a generate_bitstream.tcl file
    val run_tcl = new PrintWriter(s"${BuilderConfig.vivadoTclDir}/generate_bitstream.tcl")

    run_tcl.println(s"open_project ${BuilderConfig.vivadoProjectDir}/VivadoProject.xpr")

    // val ip_name = "clk_wiz_0"

    // run_tcl.println(s"reset_run ${ip_name}_synth_1")
    // run_tcl.println(s"launch_runs ${ip_name}_synth_1")

    // run_tcl.println(s"wait_on_run ${ip_name}_synth_1")

    // run_tcl.println(s"reset_run synth_1")
    // run_tcl.println(s"launch_runs synth_1 -jobs 8")

    // run_tcl.println(s"wait_on_run synth_1")

    run_tcl.println(s"update_compile_order -fileset sources_1")
    run_tcl.println(s"launch_runs impl_1 -to_step write_bitstream -jobs 8")
    run_tcl.println(s"wait_on_run impl_1")

    run_tcl.println(s"open_run impl_1")
    run_tcl.println(s"write_bitstream ${BuilderConfig.vivadoTclDir}/Arty100TShell.bit -force")

    run_tcl.println(s"close_project")
    
    run_tcl.close()
    run_tcl.flush()   // make sure the file is written to the disk
  }

  s"vivado -mode batch -source ${BuilderConfig.vivadoTclDir}/generate_bitstream.tcl".!
}
