#include <stdint.h>
#include <stddef.h>
#include <stdio.h>

#include "metal.h"
#include "riscv.h"
#include "uart.h"
#include "gpio.h"
#include "nn_mini.h"


#define SCRATCH_BASE          0x08000000
#define GPIOA_BASE            0x10010000
#define UART0_BASE            0x10020000
#define FLASH_BASE            0x20000000


#define GPIOA                           ((XilinxGpio *) GPIOA_BASE)
#define UART0                           ((XilinxUart *) UART0_BASE)

#include "glossy.h"




#define VLEN   2


#define DELAY_CYCLES 2000000
// #define DELAY_CYCLES 2



// load the weight data block from the model.bin file
INCLUDE_FILE(".rodata", "./weights.bin", weights);
extern uint32_t weights_data[];
extern size_t weights_start[];
extern size_t weights_end[];


static uint32_t zero[1] __attribute__((aligned(16)));
static uint32_t y_tensor[4] __attribute__((aligned(16)));
static uint32_t x_tensor[4] __attribute__((aligned(16)));


// === function declarations === //
int main();



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

static inline void write_f32(uint32_t *ptr, float data) {
  vec_t val;
  val.f32 = data;
  ptr[0] = val.u32;
}



/**
 * Add two vectors
 * y = a + b
 */
void nn_mini_add(size_t out_features, uint32_t *y, const uint32_t *a, const uint32_t *b) {
  uint32_t *y_data = y;
  const uint32_t *a_data = a;
  const uint32_t *b_data = b;

  for (size_t tile = 0; tile < out_features; tile += VLEN) {
    // load a
    // WRITE_CSR(CSR_SYSCALL3, 2);
    asm volatile("vle32.v v2, (%0)" : : "r"(a_data) : "v2");

    // load b
    // WRITE_CSR(CSR_SYSCALL3, 3);
    asm volatile("vle32.v v3, (%0)" : : "r"(b_data) : "v3");

    // y = a + b
    // WRITE_CSR(CSR_SYSCALL3, 4);
    asm volatile("vfadd.vv v1, v2, v3");

    // store y
    // WRITE_CSR(CSR_SYSCALL3, 5);
    asm volatile("vse32.v v1, (%0)" : : "r"(y_data) : "memory");

    // increment pointers
    y_data += VLEN;
    a_data += VLEN;
    b_data += VLEN;
  }
}


/**
 * Fused linear layer with relu activation
 * y = max(0, w.T * x + b)
 */
void nn_mini_linear_relu(size_t out_features, size_t in_features, uint32_t *y, const uint32_t *x, const uint32_t *w, const uint32_t *b) {
  // vy = vw * vx (broadcast x) + vb

  uint32_t *y_data = y;
  const uint32_t *x_data = x;
  const uint32_t *w_data = w;
  const uint32_t *b_data = b;

  // tiling, when out_features is greater than SIMD length
  for (size_t tile = 0; tile < out_features; tile += VLEN) {
    const uint32_t *w_ptr = w_data;
    const uint32_t *x_ptr = x_data;

    /* 
      Register allocation plan:
      v0 stores zero, used to do relu activation
      v1 is used to store the final result. 
        It is initialized with the b vector value
        In each iteration, v1 is accumulated with the loop result
      v2 is used to store the x vector.
      v3 is used to store the w column vector.
    */

    // zero out v0
    asm volatile("vxor.vv v0, v0, v0");

    WRITE_CSR(CSR_SYSCALL3, 2);
    // load b first to avoid extra add
    asm volatile("vle32.v v1, (%0)" : : "r"(b_data) : "v1");

    // loop over x and column vector of w    
    for (size_t i = 0; i < in_features; i += 1) {
      // broadcast x
      WRITE_CSR(CSR_SYSCALL3, 3);
      asm volatile("vlse32.v v2, (%0), zero" : : "r"(x_ptr) : "v2");

      // load w column
      WRITE_CSR(CSR_SYSCALL3, 4);
      asm volatile("vle32.v v3, (%0)" : : "r"(w_ptr) : "v3");

      // y += w.T * x
      WRITE_CSR(CSR_SYSCALL3, 5);
      asm volatile("vfmacc.vv v1, v2, v3");

      // increment pointers
      x_ptr += 1;
      w_ptr += out_features;
    }

    // relu
    WRITE_CSR(CSR_SYSCALL3, 6);
    // ummmm okay so GCC thinks v1 is rs2 and v0 is rs1...
    asm volatile("vmax.vv v1, v1, v0");

    // store y
    WRITE_CSR(CSR_SYSCALL3, 7);
    asm volatile("vse32.v v1, (%0)" : : "r"(y_data) : "memory");

    // increment pointers
    y_data += VLEN;
    w_data += VLEN;
    b_data += VLEN;
  }
}

/**
 * Linear layer
 * y = w.T * x + b
 */
void nn_mini_linear(size_t out_features, size_t in_features, uint32_t *y, const uint32_t *x, const uint32_t *w, const uint32_t *b) {
  uint32_t *y_data = y;
  const uint32_t *x_data = x;
  const uint32_t *w_data = w;
  const uint32_t *b_data = b;

  // tiling, when out_features is greater than SIMD length
  for (size_t tile = 0; tile < out_features; tile += VLEN) {
    const uint32_t *w_ptr = w_data;
    const uint32_t *x_ptr = x_data;

    asm volatile("vle32.v v1, (%0)" : : "r"(b_data) : "v1");

    // loop over x and column vector of w    
    for (size_t i = 0; i < in_features; i += 1) {
      asm volatile("vlse32.v v2, (%0), zero" : : "r"(x_ptr) : "v2");
      asm volatile("vle32.v v3, (%0)" : : "r"(w_ptr) : "v3");
      asm volatile("vfmacc.vv v1, v2, v3");
      x_ptr += 1;
      w_ptr += out_features;
    }

    asm volatile("vse32.v v1, (%0)" : : "r"(y_data) : "memory");

    // increment pointers
    y_data += VLEN;
    w_data += VLEN;
    b_data += VLEN;
  }
}


int main(void) {
  uint8_t counter = 3;

  const size_t out_features = 4;
  const size_t in_features = 3;

  const uint32_t *w_tensor = weights_data;
  const uint32_t *b_tensor = weights_data + out_features * in_features;

  while (1) {
    GPIOA->OUTPUT = counter & 0b1111;

    // initialize tensor data
    write_f32(x_tensor + 0, 0.382f);
    write_f32(x_tensor + 1, 0.415f);
    write_f32(x_tensor + 2, -0.117f);
    write_f32(x_tensor + 3, 0.44f);

    // write_f32(x_tensor + 0, 0);
    // write_f32(x_tensor + 1, 0);
    // write_f32(x_tensor + 2, 0);
    // write_f32(x_tensor + 3, 0);


    // write_f32(x_tensor + 0, 0.11f);
    // write_f32(x_tensor + 1, 0.22f);
    // write_f32(x_tensor + 2, 0.33f);


    putchar('x');
    putfloat(x_tensor[0]);
    putfloat(x_tensor[1]);
    putfloat(x_tensor[2]);
    putfloat(x_tensor[3]);
    putchar('\n');

    putchar('w');  // transposed, (in X out)
    putfloat(w_tensor[0]);
    putfloat(w_tensor[1]);
    putfloat(w_tensor[2]);
    putfloat(w_tensor[3]);
    putchar('\n');
    putfloat(w_tensor[4]);
    putfloat(w_tensor[5]);
    putfloat(w_tensor[6]);
    putfloat(w_tensor[7]);
    putchar('\n');
    putfloat(w_tensor[8]);
    putfloat(w_tensor[9]);
    putfloat(w_tensor[10]);
    putfloat(w_tensor[11]);
    putchar('\n');

    putchar('b');
    putfloat(b_tensor[0]);
    putfloat(b_tensor[1]);
    putfloat(b_tensor[2]);
    putfloat(b_tensor[3]);
    putchar('\n');

    
    WRITE_CSR(CSR_SYSCALL3, 1);

    // nn_mini_add(4, y_tensor, x_tensor, b_tensor);
    nn_mini_linear(out_features, in_features, y_tensor, x_tensor, w_tensor, b_tensor);

    putchar('_'); putchar('y');
    putfloat(y_tensor[0]);
    putfloat(y_tensor[1]);
    putfloat(y_tensor[2]);
    putfloat(y_tensor[3]);
    putchar('\n');
    
    nn_mini_linear_relu(out_features, in_features, y_tensor, x_tensor, w_tensor, b_tensor);

    putchar('y');
    putfloat(y_tensor[0]);
    putfloat(y_tensor[1]);
    putfloat(y_tensor[2]);
    putfloat(y_tensor[3]);
    putchar('\n');

    fflush(0);

    exit(0);


    counter += 1;

    sleep(DELAY_CYCLES);
  }
  
  exit(0);
}