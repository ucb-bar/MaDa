#include <stdint.h>
#include <stddef.h>
#include <stdio.h>

#include "metal.h"
#include "riscv.h"


#define AXI_CLOCK_PERIOD      64  // 64 ns
#define PWM_PERIOD      20000000  // 20 ms
#define PWM_HIGH_TIME    1000000  // 1 ms

#define PWM_PERIOD_COUNT  (PWM_PERIOD / AXI_CLOCK_PERIOD)
#define PWM_HIGH_TIME_COUNT  (PWM_HIGH_TIME / AXI_CLOCK_PERIOD)


#define SIMULATION    1


#include "uart.h"
#include "gpio.h"
#include "timer.h"

#include "nn_mini.h"


#define SCRATCH_BASE          0x08000000
#define GPIOA_BASE            0x10010000
#define UART0_BASE            0x10020000
#define QSPI_BASE             0x10030000
#define TIM0_BASE             0x10040000

#define FLASH_BASE            0x20000000

#define GPIOA                           ((XilinxGpio *) GPIOA_BASE)
#define UART0                           ((XilinxUart *) UART0_BASE)
#define TIM0                            ((XilinxTimer *) TIM0_BASE)

#include "glossy.h"




#define VLEN   2


#if SIMULATION == 1
  #define DELAY_CYCLES 2
  #define N_ITER 1
#else
  #define DELAY_CYCLES 1000000
  #define N_ITER 10
#endif




// load the weight data block from the model.bin file
INCLUDE_FILE(".rodata", "./weights.bin", weights);
extern uint32_t weights_data[];
extern size_t weights_start[];
extern size_t weights_end[];


static uint32_t x_tensor[83] __attribute__((aligned(16)));
static uint32_t lin1_tensor[512] __attribute__((aligned(16)));
static uint32_t lin2_tensor[256] __attribute__((aligned(16)));
static uint32_t y_tensor[8] __attribute__((aligned(16)));


// === function declarations === //
int main();



// === startup code === //
extern unsigned int __sp;

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
  asm volatile("la sp, __sp");
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
void nn_mini_add(size_t in_features, uint32_t *y, const uint32_t *a, const uint32_t *b) {
  uint32_t *y_data = y;
  const uint32_t *a_data = a;
  const uint32_t *b_data = b;

  for (size_t tile = 0; tile < in_features; tile += VLEN) {
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
void nn_mini_linear_relu(size_t in_features, size_t out_features, uint32_t *y, const uint32_t *x, const uint32_t *w, const uint32_t *b) {
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
    asm volatile("vfmax.vv v1, v1, v0");

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
void nn_mini_linear(size_t in_features, size_t out_features, uint32_t *y, const uint32_t *x, const uint32_t *w, const uint32_t *b) {
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



uint32_t pwm_high = 1000000;


int main(void) {
  uint8_t counter = 0;

  // configure timer
  // timer_pwm_init(TIM0);
  // timer_pwm_set_period(TIM0, PWM_PERIOD_COUNT);
  // timer_pwm_set_high_time(TIM0, pwm_high / 64);
  // timer_pwm_enable(TIM0);

  
  // ==== Model Initialization ==== //
  // NOTE: out_features must be a multiple of VLEN
  const size_t in_features = 83;
  const size_t out_features = 8;

  // weights are transposed, (in_features X out_features)
  const uint32_t *w1_tensor = weights_data + 0;
  const uint32_t *b1_tensor = weights_data + 2656;
  const uint32_t *w2_tensor = weights_data + 2688;
  const uint32_t *b2_tensor = weights_data + 3200;
  const uint32_t *w3_tensor = weights_data + 3216;
  const uint32_t *b3_tensor = weights_data + 3344;

  // const uint32_t *w1_tensor = weights_data + 0;
  // const uint32_t *b1_tensor = weights_data + 42496;
  // const uint32_t *w2_tensor = weights_data + 43008;
  // const uint32_t *b2_tensor = weights_data + 174080;
  // const uint32_t *w3_tensor = weights_data + 174336;
  // const uint32_t *b3_tensor = weights_data + 176384;


  while (1) {
    for (size_t loop_iter=0; loop_iter<N_ITER; loop_iter+=1) {
      // initialize input tensor
      write_f32(x_tensor + 0, 0.38226926);
      write_f32(x_tensor + 1, 0.41500396);
      write_f32(x_tensor + 2, -0.11713624);
      write_f32(x_tensor + 3, 0.45930564);
      write_f32(x_tensor + 4, -0.10955179);
      write_f32(x_tensor + 5, 0.10089535);
      write_f32(x_tensor + 6, -0.24342752);
      write_f32(x_tensor + 7, 0.29364133);
      write_f32(x_tensor + 8, 0.44077146);
      write_f32(x_tensor + 9, -0.36681408);
      write_f32(x_tensor + 10, 0.43459809);
      write_f32(x_tensor + 11, 0.09357965);
      write_f32(x_tensor + 12, 0.36940444);
      write_f32(x_tensor + 13, 0.06771529);
      write_f32(x_tensor + 14, 0.24109405);
      write_f32(x_tensor + 15, -0.07059550);
      write_f32(x_tensor + 16, 0.38544291);
      write_f32(x_tensor + 17, 0.07390445);
      write_f32(x_tensor + 18, -0.23341995);
      write_f32(x_tensor + 19, 0.12744915);
      write_f32(x_tensor + 20, -0.23036832);
      write_f32(x_tensor + 21, -0.05863643);
      write_f32(x_tensor + 22, -0.20307916);
      write_f32(x_tensor + 23, 0.33168548);
      write_f32(x_tensor + 24, -0.39468509);
      write_f32(x_tensor + 25, -0.23050517);
      write_f32(x_tensor + 26, -0.14118737);
      write_f32(x_tensor + 27, -0.30063623);
      write_f32(x_tensor + 28, 0.04719156);
      write_f32(x_tensor + 29, -0.49383956);
      write_f32(x_tensor + 30, 0.45155454);
      write_f32(x_tensor + 31, -0.42473412);
      write_f32(x_tensor + 32, 0.38601369);
      write_f32(x_tensor + 33, 0.08320957);
      write_f32(x_tensor + 34, -0.16235226);
      write_f32(x_tensor + 35, 0.30897498);
      write_f32(x_tensor + 36, 0.07792538);
      write_f32(x_tensor + 37, 0.40398169);
      write_f32(x_tensor + 38, 0.05465984);
      write_f32(x_tensor + 39, -0.15768659);
      write_f32(x_tensor + 40, 0.13434184);
      write_f32(x_tensor + 41, -0.13558972);
      write_f32(x_tensor + 42, 0.21042877);
      write_f32(x_tensor + 43, 0.44641107);
      write_f32(x_tensor + 44, 0.28902978);
      write_f32(x_tensor + 45, -0.21858627);
      write_f32(x_tensor + 46, 0.28863233);
      write_f32(x_tensor + 47, 0.08946311);
      write_f32(x_tensor + 48, 0.25391752);
      write_f32(x_tensor + 49, -0.30475253);
      write_f32(x_tensor + 50, -0.49495423);
      write_f32(x_tensor + 51, -0.19318026);
      write_f32(x_tensor + 52, -0.38351142);
      write_f32(x_tensor + 53, 0.41026944);
      write_f32(x_tensor + 54, 0.14401567);
      write_f32(x_tensor + 55, 0.20710677);
      write_f32(x_tensor + 56, 0.15813059);
      write_f32(x_tensor + 57, -0.00869799);
      write_f32(x_tensor + 58, 0.39130414);
      write_f32(x_tensor + 59, -0.35525680);
      write_f32(x_tensor + 60, 0.03148186);
      write_f32(x_tensor + 61, -0.34127009);
      write_f32(x_tensor + 62, 0.15417600);
      write_f32(x_tensor + 63, -0.17219114);
      write_f32(x_tensor + 64, 0.15320814);
      write_f32(x_tensor + 65, -0.10417074);
      write_f32(x_tensor + 66, 0.41469592);
      write_f32(x_tensor + 67, -0.29635096);
      write_f32(x_tensor + 68, -0.29819900);
      write_f32(x_tensor + 69, -0.29821700);
      write_f32(x_tensor + 70, 0.44972140);
      write_f32(x_tensor + 71, 0.16662556);
      write_f32(x_tensor + 72, 0.48112535);
      write_f32(x_tensor + 73, -0.41263813);
      write_f32(x_tensor + 74, -0.49593806);
      write_f32(x_tensor + 75, -0.39118189);
      write_f32(x_tensor + 76, -0.33634454);
      write_f32(x_tensor + 77, 0.20252007);
      write_f32(x_tensor + 78, 0.17903793);
      write_f32(x_tensor + 79, 0.41546220);
      write_f32(x_tensor + 80, -0.25821269);
      write_f32(x_tensor + 81, -0.34085590);
      write_f32(x_tensor + 82, 0.26528907);

      // forward
      nn_mini_linear_relu(in_features, 32, lin1_tensor, x_tensor, w1_tensor, b1_tensor);
      nn_mini_linear_relu(32, 16, lin2_tensor, lin1_tensor, w2_tensor, b2_tensor);
      nn_mini_linear(16, out_features, y_tensor, lin2_tensor, w3_tensor, b3_tensor);

      // nn_mini_linear_relu(in_features, 512, lin1_tensor, x_tensor, w1_tensor, b1_tensor);
      // nn_mini_linear_relu(512, 256, lin2_tensor, lin1_tensor, w2_tensor, b2_tensor);
      // nn_mini_linear(256, out_features, y_tensor, lin2_tensor, w3_tensor, b3_tensor);

      // update LED
      GPIOA->OUTPUT = loop_iter;
    }
    
    // print results
    putchar('x'); putchar('1'); putchar('\n');
    putfloat(x_tensor[0]);
    putfloat(x_tensor[1]);
    putfloat(x_tensor[2]);
    putfloat(x_tensor[3]);
    putchar('\n');

    putchar('w'); putchar('1'); putchar('.'); putchar('T'); putchar('\n');
    putfloat(w1_tensor[0]);
    putfloat(w1_tensor[1]);
    putfloat(w1_tensor[2]);
    putfloat(w1_tensor[3]);
    putchar('\n');
    putfloat(w1_tensor[4]);
    putfloat(w1_tensor[5]);
    putfloat(w1_tensor[6]);
    putfloat(w1_tensor[7]);
    putchar('\n');
    putfloat(w1_tensor[8]);
    putfloat(w1_tensor[9]);
    putfloat(w1_tensor[10]);
    putfloat(w1_tensor[11]);
    putchar('\n');

    putchar('b'); putchar('1'); putchar('\n');
    putfloat(b1_tensor[0]);
    putfloat(b1_tensor[1]);
    putfloat(b1_tensor[2]);
    putfloat(b1_tensor[3]);
    putchar('\n');

    putchar('l'); putchar('1'); putchar('\n');
    putfloat(lin1_tensor[0]);
    putfloat(lin1_tensor[1]);
    putfloat(lin1_tensor[2]);
    putfloat(lin1_tensor[3]);
    putfloat(lin1_tensor[4]);
    putfloat(lin1_tensor[5]);
    putfloat(lin1_tensor[6]);
    putfloat(lin1_tensor[7]);
    putchar('\n');
    
    putchar('l'); putchar('2'); putchar('\n');
    putfloat(lin2_tensor[0]);
    putfloat(lin2_tensor[1]);
    putfloat(lin2_tensor[2]);
    putfloat(lin2_tensor[3]);
    putfloat(lin2_tensor[4]);
    putfloat(lin2_tensor[5]);
    putfloat(lin2_tensor[6]);
    putfloat(lin2_tensor[7]);
    putchar('\n');

    putchar('y'); putchar('\n');
    putfloat(y_tensor[0]);
    putfloat(y_tensor[1]);
    putfloat(y_tensor[2]);
    putfloat(y_tensor[3]);
    putfloat(y_tensor[4]);
    putfloat(y_tensor[5]);
    putfloat(y_tensor[6]);
    putfloat(y_tensor[7]);
    putchar('\n');

    fflush(0);

    #if SIMULATION == 1
      exit(0);
    #endif

    // timer_pwm_set_high_time(TIM0, pwm_high / 64);
    // timer_pwm_enable(TIM0);
    // if (counter >> 7) {
    //   pwm_high = 2000000;
    // }
    // else {
    //   pwm_high = 1000000;
    // }

    counter += 1;

    sleep(DELAY_CYCLES);
  }
  
  exit(0);
}
