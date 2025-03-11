SECTIONS {
  . = 0x08000000;
  .text : {
    *(.text.init)
    *(.text*)
  }
  .rodata : {
    *(.rodata*)
  }
  .bss : {
    *(.bss*)
  }
  .data : {
    *(.data*)
  }
} 