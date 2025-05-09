BUILD_DIR = ./generated-src

MODULE = vivado-ips

CONFIG ?= MinimalArty100T

MILL_PATH ?= ./toolchains/mill

verilog:
	mkdir -p $(BUILD_DIR)
	$(MILL_PATH) -i module-$(MODULE).runMain buildVerilog --target-dir $(BUILD_DIR) --module-name $(CONFIG)

project: verilog
	$(MILL_PATH) -i module-$(MODULE).runMain buildProject --target-dir $(BUILD_DIR) --module-name $(CONFIG)

bitstream: project
	$(MILL_PATH) -i module-$(MODULE).runMain buildBitstream --target-dir $(BUILD_DIR) --module-name $(CONFIG)

test:
	$(MILL_PATH) -i module-$(MODULE).Test

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
