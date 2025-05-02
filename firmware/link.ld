
OUTPUT_ARCH("riscv")

/* Program entry point */
ENTRY(_start)

MEMORY {
  SRAM     (rwx): ORIGIN = 0x08000000, LENGTH = 128K
  
  /* the lower 4MB of flash is reserved for the FPGA bitstream */
  FLASH    (rx):  ORIGIN = 0x20400000, LENGTH = 32M - 4M
}

SECTIONS {
  /* Section containing the program */
  .text : ALIGN(4) {
    *(.text.init)
    *(.text*)
  }> SRAM

  .rodata : ALIGN(4) {
    *(.rodata*)
    *(.srodata*)
  }> FLASH

  .bss : ALIGN(16) {
    *(.bss*)
    *(.sbss*)
  }> SRAM

  .data : ALIGN(16) {
    *(.data*)
    *(.sdata*)
  }> SRAM
}
