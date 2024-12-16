BUILD_DIR = ./generated-src

PRJ = ChiselProject

verilog:
	mkdir -p $(BUILD_DIR)
	mill -i $(PRJ).runMain GenerateVerilog --target-dir $(BUILD_DIR)

bitstream: verilog
	mill -i $(PRJ).runMain GenerateBitstream --target-dir $(BUILD_DIR)

test:
	mill -i $(PRJ).Test

clean:
	rm -rf $(BUILD_DIR)
	rm -rf ./out
