

ARCH = rv32iv
ABI = ilp32
MODEL = medany

ARCH_FLAGS = -march=$(ARCH) -mabi=$(ABI) -mcmodel=$(MODEL)


.PHONY: build hex dump clean

default: hex dump

build:
	riscv64-unknown-elf-gcc -O2 $(ARCH_FLAGS) -specs=nosys.specs -nostdlib main.c -T link.ld -o firmware.elf

hex: build
	riscv64-unknown-elf-objcopy -O binary firmware.elf firmware.bin
	python bintohex.py firmware.bin firmware.hex

dump: build
	riscv64-unknown-elf-objdump -S firmware.elf > firmware.dis
	riscv64-unknown-elf-objdump -h firmware.elf > firmware.section

clean:
	rm -f firmware.elf firmware.bin firmware.hex firmware.dis firmware.section
