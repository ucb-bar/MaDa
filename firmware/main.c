#include <stdint.h>
#include <stddef.h>
#include <stdio.h>

#include "metal.h"
#include "riscv.h"
#include "uart.h"
#include "gpio.h"


#define SCRATCH_BASE          0x08000000
#define GPIOA_BASE            0x10010000
#define UART0_BASE            0x10020000
#define FLASH_BASE            0x20000000


#define GPIOA                           ((XilinxGpio *) GPIOA_BASE)
#define UART0                           ((XilinxUart *) UART0_BASE)

#include "glossy.h"






#define DELAY_CYCLES 2000000
// #define DELAY_CYCLES 2




// http://elm-chan.org/junk/32bit/binclude.html
#if defined(__APPLE__)
  #define INCLUDE_FILE(section, filename, symbol) asm (\
    ".align 4\n"                             /* Word alignment */\
    ".globl _"#symbol"_start\n"              /* Export the object start address */\
    ".globl _"#symbol"_data\n"               /* Export the object address */\
    "_"#symbol"_start:\n"                    /* Define the object start address label */\
    "_"#symbol"_data:\n"                     /* Define the object label */\
    ".incbin \""filename"\"\n"               /* Import the file */\
    ".globl _"#symbol"_end\n"                /* Export the object end address */\
    "_"#symbol"_end:\n"                      /* Define the object end address label */\
    ".align 4\n")                            /* Word alignment */
#elif defined(ARDUINO)
  #define INCBIN_PREFIX 
  #define INCBIN_STYLE INCBIN_STYLE_SNAKE
  #include "incbin.h"
  #define INCLUDE_FILE(section, filename, symbol) INCBIN(symbol, filename)
#else
  #define INCLUDE_FILE(section, filename, symbol) asm (\
    ".section "#section"\n"                   /* Change section */\
    ".balign 4\n"                             /* Word alignment */\
    ".global "#symbol"_start\n"               /* Export the object start address */\
    ".global "#symbol"_data\n"                /* Export the object address */\
    #symbol"_start:\n"                        /* Define the object start address label */\
    #symbol"_data:\n"                         /* Define the object label */\
    ".incbin \""filename"\"\n"                /* Import the file */\
    ".global "#symbol"_end\n"                 /* Export the object end address */\
    #symbol"_end:\n"                          /* Define the object end address label */\
    ".balign 4\n"                             /* Word alignment */\
    ".section \".text\"\n")                   /* Restore section */
#endif



// load the weight data block from the model.bin file
INCLUDE_FILE(".rodata", "./weights.bin", weights);
extern uint32_t weights_data[];
extern size_t weights_start[];
extern size_t weights_end[];


static uint32_t zero[1] __attribute__((aligned(16))) = {0, 0, 0, 0};
static uint32_t y_data[4] __attribute__((aligned(16))) = {0, 0, 0, 0};
static uint32_t w_data[4] __attribute__((aligned(16))) = {0, 0, 0, 0};
static uint32_t b_data[4] __attribute__((aligned(16))) = {0, 0, 0, 0};
static uint32_t x_data[4] __attribute__((aligned(16))) = {0, 0, 0, 0};



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


const size_t SIMD_LEN = 2;


void vec_add(uint32_t *y, const uint32_t *x, const uint32_t *b) {
  // load x
  WRITE_CSR(CSR_SYSCALL3, 2);
  asm volatile("vle32.v v0, (%0)" : : "r"(x) : "v0");

  // load b
  WRITE_CSR(CSR_SYSCALL3, 3);
  asm volatile("vle32.v v1, (%0)" : : "r"(b) : "v1");

  // y = x + b
  WRITE_CSR(CSR_SYSCALL3, 4);
  asm volatile("vfadd.vv v2, v0, v1");

  // store y
  WRITE_CSR(CSR_SYSCALL3, 5);
  asm volatile("vse32.v v2, (%0)" : : "r"(y) : "memory");
}


// void linear(Tensor1D_F32 *y, const Tensor1D_F32 *x, const Tensor2D_F32 *w, const Tensor1D_F32 *b) {
//   // vy = vw * vx (broadcast x) + vb
//   const size_t batch_size = 1;

//   uint32_t out_features = w->shape[0];
//   uint32_t in_features = w->shape[1];

//   size_t tile_size = 2;

//   uint32_t *y_data = y->data;
//   uint32_t *x_data = x->data;
//   uint32_t *w_data = w->data;
//   uint32_t *b_data = b->data;

//   uint32_t *w_ptr;
//   uint32_t *x_ptr;


//   // tiling, when out_features is greater than SIMD length
//   for (size_t tile = 0; tile < 4; tile += tile_size) {
//     w_ptr = w_data;
//     x_ptr = x_data;

//     // broadcast zero
//     asm volatile("vlse32.v v0, (%0), zero" : : "r"(zero) : "v0");

//     // loop over x and column vector of w    
//     for (size_t i = 0; i < in_features; i += 1) {
//       // broadcast x
//       asm volatile("vlse32.v v1, (%0), zero" : : "r"(x_ptr) : "v1");

//       // load w column
//       asm volatile("vle32.v v2, (%0)" : : "r"(w_ptr) : "v2");

//       // y += w.T * x
//       asm volatile("vfmacc.vv v0, v1, v2");

//       // increment pointers
//       x_ptr += 1;
//       w_ptr += out_features;
//     }

//     // load b
//     asm volatile("vle32.v v3, (%0)" : : "r"(b_data) : "v3");
    
//     // y += b
//     asm volatile("vfadd.vv v0, v0, v3");

//     // // relu
//     // asm volatile("vmax.vv v0, v0, zero");

//     // store y
//     asm volatile("vse32.v v0, (%0)" : : "r"(y_data) : "memory");

//     // increment pointers
//     y_data += tile_size;
//     w_data += tile_size;
//     b_data += tile_size;
//   }
// }


int main(void) {
  uint8_t counter = 3;

  while (1) {
    GPIOA->OUTPUT = counter & 0b1111;

    // initialize x
    write_f32(x_data + 0, 0.01f);
    write_f32(x_data + 1, 0.02f);
    write_f32(x_data + 2, 0.03f);
    
    putchar('x');
    putfloat(*(x_data + 0));
    putfloat(*(x_data + 1));
    putfloat(*(x_data + 2));


    putchar('w');
    putfloat(*(weights_data + 0));
    putfloat(*(weights_data + 1));
    putfloat(*(weights_data + 2));
    putfloat(*(weights_data + 3));
    
    WRITE_CSR(CSR_SYSCALL3, 1);
    vec_add(y_data, x_data, weights_data);

    putchar('y');
    putfloat(*(y_data + 0));
    putfloat(*(y_data + 1));
    putfloat(*(y_data + 2));
    putfloat(*(y_data + 3));

    fflush(0);


    counter += 1;

    sleep(DELAY_CYCLES);
  }
  
  exit(0);
}