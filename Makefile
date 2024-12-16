BUILD_DIR = ./generated-src

PRJ = ChiselProject

CONFIG ?= ExampleArty100TShell


verilog:
	mkdir -p $(BUILD_DIR)
	mill -i $(PRJ).runMain GenerateVerilog --target-dir $(BUILD_DIR) --module-name $(CONFIG)

bitstream: verilog
	mill -i $(PRJ).runMain GenerateBitstream --target-dir $(BUILD_DIR) --module-name $(CONFIG)

test:
	mill -i $(PRJ).Test

clean:
	rm -rf $(BUILD_DIR)
	rm -rf ./out
