SECTIONS {
  . = 0x08000000;
  .text : {
    *(.text.init)
    *(.text*)
  }
  .rodata : {
    *(.rodata*)
  }
  .bss : ALIGN(16) {
    *(.bss*)
    *(.sbss*)
  }
  .data : ALIGN(16) {
    *(.data*)
  }
} 