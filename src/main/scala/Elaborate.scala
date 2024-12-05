import circt.stage.ChiselStage

object Elaborate extends App {
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
