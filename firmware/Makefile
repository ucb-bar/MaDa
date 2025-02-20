
.PHONY: build hex dump clean

default: hex dump

build:
	riscv64-unknown-elf-gcc -O2 -march=rv32i -mabi=ilp32 -mcmodel=medany -specs=nosys.specs -nostdlib main.c -T link.ld -o firmware.elf

hex: build
	riscv64-unknown-elf-objcopy -O binary firmware.elf firmware.bin
	python bintohex.py firmware.bin firmware.hex

dump: build
	riscv64-unknown-elf-objdump -S firmware.elf > firmware.dis
	riscv64-unknown-elf-objdump -h firmware.elf > firmware.section

clean:
	rm -f firmware.elf firmware.bin firmware.hex firmware.dis firmware.section
