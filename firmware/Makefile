
PHONY: build build-hex dump clean


build:
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -mcmodel=medany -specs=nosys.specs -nostdlib main.c -o main.elf

build-hex: build
	riscv64-unknown-elf-objcopy -O binary main.elf main.bin
	python bintohex.py main.bin main.hex

dump: build
	riscv64-unknown-elf-objdump -S main.elf > main.dis
	riscv64-unknown-elf-objdump -h main.elf > main.section

clean:
	rm -f main.elf main.bin main.hex main.dis main.section
