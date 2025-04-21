#include <stdint.h>
#include <stddef.h>
#include <stdio.h>


#include "metal.h"
#include "riscv.h"
#include "uart.h"
#include "gpio.h"

#include "nn.h"
#include "model.h"


#define SCRATCH_BASE          0x08000000
#define GPIOA_BASE            0x10010000
#define UART0_BASE            0x10020000
#define SPI_MEM_BASE          0x20000000


#define GPIOA                           ((XilinxGpio *) GPIOA_BASE)
#define UART0                           ((XilinxUart *) UART0_BASE)


#define CSR_SYSCALL0                 "0x8C0"
#define CSR_SYSCALL1                 "0x8C1"
#define CSR_SYSCALL2                 "0x8C2"
#define CSR_SYSCALL3                 "0x8C3"
#define CSR_SYSRESP0                 "0xCC0"
#define CSR_SYSRESP1                 "0xCC1"
#define CSR_SYSRESP2                 "0xCC2"
#define CSR_SYSRESP3                 "0xCC3"

#define SYSCALL_EXIT                0x01
#define SYSCALL_PRINT_CHAR          0x03
#define SYSCALL_PRINT_F32           0x04



#define DELAY_CYCLES 2000000
// #define DELAY_CYCLES 2


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
  WRITE_CSR(CSR_SYSCALL1, code);
  WRITE_CSR(CSR_SYSCALL0, SYSCALL_EXIT);
}

void print_char(char c) {
  WRITE_CSR(CSR_SYSCALL1, c);
  WRITE_CSR(CSR_SYSCALL0, SYSCALL_PRINT_CHAR);
  WRITE_CSR(CSR_SYSCALL0, 0);
}

void print_float(uint32_t f) {
  WRITE_CSR(CSR_SYSCALL1, f);
  WRITE_CSR(CSR_SYSCALL0, SYSCALL_PRINT_F32);
  WRITE_CSR(CSR_SYSCALL0, 0);
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

// load the weight data block from the model.bin file
INCLUDE_FILE(".rodata", "./weights.bin", weights);
extern uint8_t weights_data[];
extern size_t weights_start[];
extern size_t weights_end[];


Model model;


Tensor2D_F32 w = {
  .shape = { 4, 3 },
  .data = (uint32_t *)weights_data,
};
Tensor1D_F32 b = {
  .shape = { 4 },
  .data = (uint32_t *)b_data,
};
Tensor1D_F32 x = {
  .shape = { 3 },
  .data = (uint32_t *)x_data,
};
Tensor1D_F32 y = {
  .shape = { 4 },
  .data = (uint32_t *)y_data,
};

const size_t SIMD_LEN = 2;


void linear(Tensor1D_F32 *y, const Tensor1D_F32 *x, const Tensor2D_F32 *w, const Tensor1D_F32 *b) {
  // vy = vw * vx (broadcast x) + vb
  const size_t batch_size = 1;

  uint32_t out_features = w->shape[0];
  uint32_t in_features = w->shape[1];

  size_t tile_size = 2;

  uint32_t *y_data = y->data;
  uint32_t *x_data = x->data;
  uint32_t *w_data = w->data;
  uint32_t *b_data = b->data;

  uint32_t *w_ptr;
  uint32_t *x_ptr;


  // tiling, when out_features is greater than SIMD length
  for (size_t tile = 0; tile < 4; tile += tile_size) {
    w_ptr = w_data;
    x_ptr = x_data;

    // broadcast zero
    asm volatile("vlse32.v v0, (%0), zero" : : "r"(zero) : "v0");

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

    // load b
    asm volatile("vle32.v v3, (%0)" : : "r"(b_data) : "v3");
    
    // y += b
    asm volatile("vfadd.vv v0, v0, v3");

    // // relu
    // asm volatile("vmax.vv v0, v0, zero");

    // store y
    asm volatile("vse32.v v0, (%0)" : : "r"(y_data) : "memory");

    // increment pointers
    y_data += tile_size;
    w_data += tile_size;
    b_data += tile_size;
  }
}

static inline void write_data(uint32_t *ptr, float data) {
  vec_t val;
  val.f32 = data;
  ptr[0] = val.u32;
}


// Tensor1D_F32 b = {
//   .shape = { 4 },
//   .data = (uint32_t *)b,
// };

int main(void) {
  vec_t val;


  WRITE_CSR(CSR_SYSCALL2, 1);
  zero[0] = 0;

  write_data((uint32_t *)x.data, 0.11f);  // 3de147ae
  write_data((uint32_t *)x.data + 1, 0.22f);  // 3e6147ae
  write_data((uint32_t *)x.data + 2, 0.33f);  // 3ea8f5c3
  
  // write_data((uint32_t *)w.data, 0.12f);  // 3ea8f5c3

  // write_data((uint32_t *)w.data + 1, 0.34f);  // 3ee147ae
  // write_data((uint32_t *)w.data + 2, 0.07f);  // 3ea8f5c3
  // write_data((uint32_t *)w.data + 3, -0.11f);  // 3ee147ae
  // write_data((uint32_t *)w.data + 4, 0.56f);  // 3ea8f5c3
  // write_data((uint32_t *)w.data + 5, -0.78f);  // 3ee147ae
  // write_data((uint32_t *)w.data + 6, 0.08f);  // 3ea8f5c3
  // write_data((uint32_t *)w.data + 7, 0.22f);  // 3ee147ae
  // write_data((uint32_t *)w.data + 8, 0.90f);  // 3ea8f5c3
  // write_data((uint32_t *)w.data + 9, 1.12f);  // 3ee147ae
  // write_data((uint32_t *)w.data + 10, 0.09f);  // 3ea8f5c3
  // write_data((uint32_t *)w.data + 11, -0.33f);  // 3ee147ae

  write_data((uint32_t *)b.data, -0.55f);  // 3f0ccccd
  write_data((uint32_t *)b.data + 1, -0.66f);  // 3f28f5c3
  write_data((uint32_t *)b.data + 2, -0.77f);  // 3ea8f5c3
  write_data((uint32_t *)b.data + 3, -0.88f);  // 3ee147ae

  WRITE_CSR(CSR_SYSCALL2, 2);
  // linear(4, 3, y, x, w, b);

  model_init(&model);


  print_char('x');
  print_float(*((uint32_t *)x.data + 0));
  print_float(*((uint32_t *)x.data + 1));
  print_float(*((uint32_t *)x.data + 2));

  print_char('w');
  print_float(*((uint32_t *)w.data + 0));
  print_float(*((uint32_t *)w.data + 1));
  print_float(*((uint32_t *)w.data + 2));
  print_float(*((uint32_t *)w.data + 3));

  // model_forward(&model);
  linear(&y, &x, &w, &b);

  // load C into debug CSR
  WRITE_CSR(CSR_SYSCALL2, y.data[0]);
  WRITE_CSR(CSR_SYSCALL3, y.data[1]);
  print_float(*((uint32_t *)y.data + 0));
  print_float(*((uint32_t *)y.data + 1));
  print_float(*((uint32_t *)y.data + 2));
  print_float(*((uint32_t *)y.data + 3));

  exit(0);


  // // prints("start.\n");

  // uint8_t counter = 0;

  // model_init(&model);

  // for (int i = 0; i < 8; i += 1) {
  //   model.input_1.data[i] = 1.0;
  // }

  // model_forward(&model);


  // prints("fi in!\n");

  // while (1) {

  //   // volatile uint32_t data = *((uint32_t *)SPI_MEM_BASE);
    
    


  //   counter += 1;
  
  //   GPIOA->OUTPUT = counter & 0b1111;

  //   // for (size_t i=0; i<DELAY_CYCLES; i+=1) {
  //   //   asm volatile("nop");
  //   // }

  //   // load C into tohost CSR
  //   // WRITE_CSR("0x51E", y[0]);
  //   // WRITE_CSR("0x51F", y[1]);

  //   prints("finish loop.\n");


  //   // wait FIFO to be empty
  //   while (!READ_BITS(UART0->STAT, UART_STAT_TX_FIFO_EMPTY_MSK)) {
  //     asm volatile("nop");
  //   }
    
  //   // exit(1);
  // }
}