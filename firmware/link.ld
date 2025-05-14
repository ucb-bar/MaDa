
OUTPUT_ARCH("riscv")

/* Program entry point */
ENTRY(_start)

MEMORY {
  SRAM     (rwx): ORIGIN = 0x08000000, LENGTH = 16K
  /* the lower 4MB of flash is reserved for the FPGA bitstream */
  FLASH    (rx):  ORIGIN = 0x20400000, LENGTH = 32M - 4M
}

SECTIONS {

  /* Default stack size */
  __stack_size = DEFINED(__stack_size) ? __stack_size : 8K;

  /* Section containing the program */
  .text : ALIGN(4) {
    PROVIDE(__text_start = .);
    *(.text.init)
    *(.text.trap_vector)
    *(.text.unlikely .text.unlikely.*)
    *(.text.startup .text.startup.*)
    *(.text .text.*)
    *(.gnu.linkonce.t.*)
    PROVIDE(__text_end = .);
  }> SRAM
  PROVIDE(__text_load_start = LOADADDR(.text));

  /* Section containing read-only data */
  .rodata : {
    . = ALIGN(64);
    *(.rodata .rodata.*)
    *(.gnu.linkonce.r.*)
    . = ALIGN(64);
    *(.srodata.cst16)
    *(.srodata.cst8)
    *(.srodata.cst4)
    *(.srodata.cst2)
    *(.srodata .srodata.*)
  }> FLASH

  /* Section for initialized data */
  .data : ALIGN(8) {
    PROVIDE(__data_start = .);
    *(.data)
    *(.gnu.linkonce.d.*)
  }> SRAM
  
  PROVIDE(__data_load_start = LOADADDR(.data));

  /* Section for initialized small data */
  .sdata : {
    PROVIDE(__global_pointer$ = . + 0x800);
    *(.sdata .sdata.*)
    *(.gnu.linkonce.s.*)
  }> SRAM

  /* Section for initialized thread-local small data */
  .tdata : {
    PROVIDE_HIDDEN(__tdata_start = .);
    *(.tdata .tdata.*)
    *(.gnu.linkonce.td.*)
    PROVIDE_HIDDEN(__tdata_end = .);
    PROVIDE(__data_end = .);
  }> SRAM
  PROVIDE(__tdata_size = SIZEOF(.tdata));

  /* Section for zero-initialized thread-local uninitialized data */
  .tbss (NOLOAD) : ALIGN(8) {
    PROVIDE_HIDDEN(__tbss_start = .);
    PROVIDE_HIDDEN(__tbss_offset = ABSOLUTE(__tbss_start - __tdata_start));
    *(.tbss .tbss.*)
    *(.gnu.linkonce.tb.*)
    *(.tcommon)
    PROVIDE_HIDDEN(__tbss_end = .);
  }> SRAM
  PROVIDE(__tbss_size = SIZEOF(.tbss));

  /* Section for uninitialized data */
  .bss : ALIGN (16) {
    PROVIDE_HIDDEN(__bss_start = .);
    *(.sbss .sbss.*)
    *(.gnu.linkonce.sb.*)
    *(.bss .bss.*)
    *(.gnu.linkonce.b.*)
    *(COMMON)
    PROVIDE_HIDDEN(__bss_end = .);
  }> SRAM

  . = ALIGN (8);
  PROVIDE(__end = .);

  /* Reserve stack space */
  .stack (NOLOAD) : ALIGN(16) {
    PROVIDE_HIDDEN(__stack_start = .);
    . += __stack_size;
    PROVIDE(__sp = .);
    PROVIDE_HIDDEN(__stack_end = .);
  }> SRAM
}
