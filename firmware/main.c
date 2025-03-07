#include <stdint.h>
#include <stddef.h>


#define GPIO_OUTPUT  0x10000000

// #define DELAY_CYCLES 2000000
#define DELAY_CYCLES 2

// function declaration
int main();


void __attribute__((section(".text"), naked)) _start() {
  asm volatile("li x1, 0");
  asm volatile("li x2, 0");
  asm volatile("li x3, 0");
  asm volatile("li x4, 0");
  asm volatile("li x5, 0");
  asm volatile("li x6, 0");
  asm volatile("li x7, 0");
  asm volatile("li x8, 0");
  asm volatile("li x9, 0");
  asm volatile("li x10, 0");
  asm volatile("li x11, 0");
  asm volatile("li x12, 0");
  asm volatile("li x13, 0");
  asm volatile("li x14, 0");
  asm volatile("li x15, 0");
  asm volatile("li x16, 0");
  asm volatile("li x17, 0");
  asm volatile("li x18, 0");
  asm volatile("li x19, 0");
  asm volatile("li x20, 0");
  asm volatile("li x21, 0");
  asm volatile("li x22, 0");
  asm volatile("li x23, 0");
  asm volatile("li x24, 0");
  asm volatile("li x25, 0");
  asm volatile("li x26, 0");
  asm volatile("li x27, 0");
  asm volatile("li x28, 0");
  asm volatile("li x29, 0");
  asm volatile("li x30, 0");
  asm volatile("li x31, 0");

  // Set stack pointer to 0x08001000
  asm volatile("li sp, 0x08001000");
  
  // call main
  main();

  while (1) {
    // don't exit
  }
}


int main() {
  while (1) {

    asm volatile("vfadd.vv v3, v2, v1");
    asm volatile("vfmul.vv v4, v2, v1");
    asm volatile("vfmacc.vv v5, v1, v2");
    asm volatile("vle32.v v2, (x1)");
    asm volatile("vse32.v v2, (x1)");

    for (size_t i=0; i<DELAY_CYCLES; i+=1) {
      asm volatile("nop");
    }
    *((volatile uint32_t *)GPIO_OUTPUT) = 0x00000001;
    
    for (size_t i=0; i<DELAY_CYCLES; i+=1) {
      asm volatile("nop");
    }
    *((volatile uint32_t *)GPIO_OUTPUT) = 0x00000000;
  }
}