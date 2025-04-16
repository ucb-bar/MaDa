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

static uint32_t zero[1] __attribute__((aligned(16))) = { 0 };
static uint32_t y[4] __attribute__((aligned(16))) = { 0 };
static uint32_t x[4] __attribute__((aligned(16))) = { 0 };
static uint32_t w[16] __attribute__((aligned(16))) = { 0 };
static uint32_t b[4] __attribute__((aligned(16))) = { 0 };


const size_t SIMD_LEN = 2;


void linear(uint32_t out_features, uint32_t in_features, uint32_t *y, uint32_t *x, uint32_t *w, uint32_t *b) {
  // vy = vw * vx (broadcast x) + vb
  const size_t batch_size = 1;

  size_t tile_size = 2;

  uint32_t *w_ptr;
  uint32_t *x_ptr;

  // tiling, when out_features is greater than SIMD length
  for (size_t tile = 0; tile < 4; tile += tile_size) {
    w_ptr = w;
    x_ptr = x;

    // broadcast zero
    asm volatile("vlse32.v v0, (%0), zero" : : "r"(zero) : "v0");

    WRITE_CSR("0x51F", 4);
    // loop over x and column vector of w    
    for (size_t i = 0; i < in_features; i += 1) {
      // broadcast x
      asm volatile("vlse32.v v1, (%0), zero" : : "r"(x_ptr) : "v1");

      // load w column
      asm volatile("vle32.v v2, (%0)" : : "r"(w_ptr) : "v2");

      // y += w.T * x
      asm volatile("vfmacc.vv v0, v1, v2");

      // increment pointers
      x_ptr += 1;
      w_ptr += out_features;
    }
    WRITE_CSR("0x51F", 0);

    // load b
    asm volatile("vle32.v v3, (%0)" : : "r"(b) : "v3");
    
    // y += b
    asm volatile("vfadd.vv v0, v0, v3");
    
    // store y
    asm volatile("vse32.v v0, (%0)" : : "r"(y) : "memory");

    // increment pointers
    y += tile_size;
    w += tile_size;
    b += tile_size;
  }
  

  // asm volatile("vfadd.vv v3, v2, v1");
  // asm volatile("vfmul.vv v4, v2, v1");
  // asm volatile("vfmacc.vv v5, v1, v2");
  // asm volatile("vse32.v v2, 0(x1)");
}


int main(void) {
  // prints("start.\n");

  while (1) {

    uint32_t SPI_MEM_ADDR = 0x40000000;

    volatile uint32_t data = *((uint32_t *)SPI_MEM_ADDR);
    

    // vec_t val;

    // zero[0] = 0;

    // val.f32 = 0.11f;  // 3de147ae
    // x[0] = val.u32;
    // val.f32 = 0.22f;  // 3e6147ae
    // x[1] = val.u32;
    // val.f32 = 0.33f;  // 3ea8f5c3
    // x[2] = val.u32;
    
    // val.f32 = 0.12f;  // 3ea8f5c3
    // w[0] = val.u32;
    // val.f32 = 0.34f;  // 3ee147ae
    // w[1] = val.u32;
    // val.f32 = 0.07f;  // 3ea8f5c3
    // w[2] = val.u32;
    // val.f32 = -0.11f;  // 3ee147ae
    // w[3] = val.u32;
    // val.f32 = 0.56f;  // 3ea8f5c3
    // w[4] = val.u32;
    // val.f32 = -0.78f;  // 3ee147ae
    // w[5] = val.u32;
    // val.f32 = 0.08f;  // 3ea8f5c3
    // w[6] = val.u32;
    // val.f32 = 0.22f;  // 3ee147ae
    // w[7] = val.u32;
    // val.f32 = 0.90f;  // 3ea8f5c3
    // w[8] = val.u32;
    // val.f32 = 1.12f;  // 3ee147ae
    // w[9] = val.u32;
    // val.f32 = 0.09f;  // 3ea8f5c3
    // w[10] = val.u32;
    // val.f32 = -0.33f;  // 3ee147ae
    // w[11] = val.u32;

    // val.f32 = -0.55f;  // 3f0ccccd
    // b[0] = val.u32;
    // val.f32 = -0.66f;  // 3f28f5c3
    // b[1] = val.u32;
    // val.f32 = -0.77f;  // 3ea8f5c3
    // b[2] = val.u32;
    // val.f32 = -0.88f;  // 3ee147ae
    // b[3] = val.u32;

    // WRITE_CSR("0x51F", 0);
    
    // linear(4, 3, y, x, w, b);
  
    // // load C into tohost CSR
    // // WRITE_CSR("0x51E", y[0]);
    // // WRITE_CSR("0x51F", y[1]);
    // WRITE_CSR("0x51F", 2);

    // prints("finish loop.\n");


    // wait FIFO to be empty
    // while (!READ_BITS(UART0->STAT, UART_STAT_TX_FIFO_EMPTY_MSK)) {
    //   asm volatile("nop");
    // }
    
    // exit(1);
  }
}