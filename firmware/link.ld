SECTIONS {
  . = 0x08000000;
  .text : {
    *(.text.init)
    *(.text*)
  }
  .rodata : {
    *(.rodata*)
  }
  .data : {
    *(.data*)
  }
  .bss : {
    *(.bss*)
  }
} 