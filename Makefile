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

RISCV = /home/tk/Documents/chipyard/.conda-env/riscv-tools

isa-test:
	mkdir -p ./isa-tests/
	cd ./isa-tests/ && \
	~/Downloads/riscv-tests/configure --prefix=${RISCV}/target && \
	make && \
	make install

	# spike --isa=rv32i ./isa-tests/isa/rv32ui-p-add

	riscv64-unknown-elf-objcopy -O binary ./isa-tests/isa/rv32ui-p-add ./isa-tests/isa/rv32ui-p-add.bin
	python ./firmware/bintohex.py ./isa-tests/isa/rv32ui-p-add.bin ./isa-tests/isa/rv32ui-p-add.hex --data_width 32
