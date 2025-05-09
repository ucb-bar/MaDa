BUILD_DIR = ./generated-src

PACKAGE ?= vivado-ips
MODULE ?= MinimalArty100T
CONFIG ?= MinimalArty100TConfig

MILL_PATH ?= ./toolchains/mill

verilog:
	mkdir -p $(BUILD_DIR)
	$(MILL_PATH) -i package-$(PACKAGE).runMain builder.buildVerilog --target-dir $(BUILD_DIR) --module-name $(MODULE)
	
project: verilog
	$(MILL_PATH) -i package-$(PACKAGE).runMain builder.buildProject --target-dir $(BUILD_DIR) --module-name $(MODULE)

bitstream: project
	$(MILL_PATH) -i package-$(PACKAGE).runMain builder.buildBitstream --target-dir $(BUILD_DIR) --module-name $(MODULE)

test:
	$(MILL_PATH) -i package-$(PACKAGE).Test

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
