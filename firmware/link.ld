
OUTPUT_ARCH("riscv")

/* Program entry point */
ENTRY(_start)

MEMORY {
  SRAM     (rwx): ORIGIN = 0x08000000, LENGTH = 128K
  FLASH    (rx):  ORIGIN = 0x20000000, LENGTH = 128K
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
