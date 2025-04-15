BUILD_DIR = ./generated-src

PRJ = ChiselProject

CONFIG ?= MinimalArty100T


verilog:
	mkdir -p $(BUILD_DIR)
	mill -i $(PRJ).runMain GenerateVerilog --target-dir $(BUILD_DIR) --module-name $(CONFIG)

project: verilog
	mill -i $(PRJ).runMain GenerateProject --target-dir $(BUILD_DIR) --module-name $(CONFIG)

bitstream: project
	mill -i $(PRJ).runMain GenerateBitstream --target-dir $(BUILD_DIR) --module-name $(CONFIG)

test:
	mill -i $(PRJ).Test

clean:
	rm -rf $(BUILD_DIR)
	rm -rf ./out
