package delta

import chisel3._
import chisel3.util._
import builder.{addConstraintResource, addSimulationResource}


class MlpPolicyRunner extends MlpPolicyRunnerBase(
  new SoCConfig(
    tile = new TileConfig(
      core = new CoreConfig(
        XLEN = 32,
        ELEN = 16,
        VLEN = 64,
        pipelineStages = 3,
      ),
      sbusFrequency = 50,
    )
  )
) {
  addConstraintResource("package-vivado-ips/resources/constraints/Arty-A7-100-Master.xdc")

  addSimulationResource("package-delta-soc/test/MlpPolicyRunnerTestbench.sv")
  addSimulationResource("package-delta-soc/resources/verilog/SimUart.sv")
  addSimulationResource("package-delta-soc/resources/verilog/SpiFlashMemCtrl.sv")
  addSimulationResource("package-delta-soc/resources/verilog/SimSpiFlashModel.sv")
  addSimulationResource("package-delta-soc/resources/verilog/Ram.v")
}
