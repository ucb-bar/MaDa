#include <stdint.h>
#include <stddef.h>
#include <stdio.h>


#include "metal.h"
#include "riscv.h"
#include "uart.h"
#include "gpio.h"


// #define DELAY_CYCLES 2000000
#define DELAY_CYCLES 2


// === function declarations === //
int main();


// === system functions === //
int putchar(int c) {
  while (READ_BITS(UART0->STAT, UART_STAT_TX_FIFO_FULL_MSK)) {
    asm volatile("nop");
  }
  UART0->TXFIFO = READ_BITS(c, 0xFF);
}

void prints(const char *str) {
  while (*str) {
    putchar(*str);
    str += 1;
  }
}

void exit(int code) {
  WRITE_CSR("0x51E", 0x01);
}



// === startup code === //
void __attribute__((section(".text.init"), naked)) _start() {
  // clear all registers to avoid X's
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

  // set stack pointer to 0x08001000
  // and set C runtime argc to 0
  asm volatile("li sp, 0x08001000");
  asm volatile("addi s0, sp, -32");
  asm volatile("sw zero, 0x00(s0)");
  asm volatile("sw zero, 0x04(s0)");
  asm volatile("sw zero, 0x08(s0)");
  asm volatile("sw zero, 0x0C(s0)");
  asm volatile("sw zero, 0x10(s0)");
  asm volatile("sw zero, 0x14(s0)");
  asm volatile("sw zero, 0x18(s0)");
  asm volatile("sw zero, 0x1C(s0)");
  
  // clear all vector registers to avoid X's
  // we use the argc as the initializer
  asm volatile("vle32.v v0, (s0)");
  asm volatile("vle32.v v1, (s0)");
  asm volatile("vle32.v v2, (s0)");
  asm volatile("vle32.v v3, (s0)");
  asm volatile("vle32.v v4, (s0)");
  asm volatile("vle32.v v5, (s0)");
  asm volatile("vle32.v v6, (s0)");
  asm volatile("vle32.v v7, (s0)");
  asm volatile("vle32.v v8, (s0)");
  asm volatile("vle32.v v9, (s0)");
  asm volatile("vle32.v v10, (s0)");
  asm volatile("vle32.v v11, (s0)");
  asm volatile("vle32.v v12, (s0)");
  asm volatile("vle32.v v13, (s0)");
  asm volatile("vle32.v v14, (s0)");
  asm volatile("vle32.v v15, (s0)");
  asm volatile("vle32.v v16, (s0)");
  asm volatile("vle32.v v17, (s0)");
  asm volatile("vle32.v v18, (s0)");
  asm volatile("vle32.v v19, (s0)");
  asm volatile("vle32.v v20, (s0)");
  asm volatile("vle32.v v21, (s0)");
  asm volatile("vle32.v v22, (s0)");
  asm volatile("vle32.v v23, (s0)");
  asm volatile("vle32.v v24, (s0)");
  asm volatile("vle32.v v25, (s0)");
  asm volatile("vle32.v v26, (s0)");
  asm volatile("vle32.v v27, (s0)");
  asm volatile("vle32.v v28, (s0)");
  asm volatile("vle32.v v29, (s0)");
  asm volatile("vle32.v v30, (s0)");
  asm volatile("vle32.v v31, (s0)");
  
  // call main
  main();

  exit(1);
}

typedef union {
  float f32;
  uint32_t u32;
} vec_t;


void linear(uint32_t *y, uint32_t *x, uint32_t *w, uint32_t *b) {
  // Load values from pointers into vector registers
  asm volatile("vlse32.v v1, (%0), zero" : : "r"(x) : "v1");
  asm volatile("vle32.v v2, (%0)" : : "r"(w) : "v2");
  asm volatile("vlse32.v v3, (%0), zero" : : "r"(b) : "v3");
  
  // y = x * w + b
  asm volatile("vfmul.vv v4, v1, v2");
  asm volatile("vfadd.vv v4, v4, v3");
  
  // Store result back to the destination pointer
  asm volatile("vse32.v v4, (%0)" : : "r"(y) : "memory");
  

  // asm volatile("vfadd.vv v3, v2, v1");
  // asm volatile("vfmul.vv v4, v2, v1");
  // asm volatile("vfmacc.vv v5, v1, v2");
  // asm volatile("vse32.v v2, 0(x1)");
}


static uint32_t w[2] = { 0 };

int main(void) {
  // prints("start.\n");

  while (1) {
    vec_t vy, vx, vw, vb;

    // vx.f32 = 0.3367f;
    // vw.f32 = 0.1288f;
    // vb.f32 = 0.2345f;
    vx.f32 = 0.1f;
    vb.f32 = 1.f;
    
    vw.f32 = 4.f;
    w[0] = vw.u32;
    vw.f32 = 2.f;
    w[1] = vw.u32;

    uint32_t uy = vy.u32;
    uint32_t ux = vx.u32;
    uint32_t ub = vb.u32;

    linear(&uy, &ux, w, &ub);
  
    // load C into tohost CSR
    WRITE_CSR("0x51E", uy);

    // prints("finish loop.\n");


    // wait FIFO to be empty
    // while (!READ_BITS(UART0->STAT, UART_STAT_TX_FIFO_EMPTY_MSK)) {
    //   asm volatile("nop");
    // }
    
    // exit(1);
  }
}