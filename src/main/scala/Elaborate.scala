import circt.stage.ChiselStage

object Elaborate extends App {
  val chiselOpts = args ++ Array("--split-verilog")

  val firtoolOpts = Array(
    "-disable-all-randomization",
    "-strip-debug-info",
  )

  ChiselStage.emitSystemVerilogFile(
    gen=new Top,
    args=chiselOpts,
    firtoolOpts=firtoolOpts
  )
}
