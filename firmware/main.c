#include <stdint.h>
#include <stddef.h>
#include <stdio.h>


#define GPIO_OUTPUT  0x10000000


/* ======== Axi4Lite Uart Lite ======== */
#define UART_RXFIFO  0x10001000
#define UART_TXFIFO  0x10001004
#define UART_STAT    0x10001008
#define UART_CTRL    0x1000100C

#define UART_STAT_RX_FIFO_VALID_POS     0x0
#define UART_STAT_RX_FIFO_VALID_MSK     (0x1 << UART_STAT_RX_FIFO_VALID_POS)
#define UART_STAT_RX_FIFO_FULL_POS      0x1
#define UART_STAT_RX_FIFO_FULL_MSK      (0x1 << UART_STAT_RX_FIFO_FULL_POS)
#define UART_STAT_TX_FIFO_VALID_POS     0x2
#define UART_STAT_TX_FIFO_VALID_MSK     (0x1 << UART_STAT_TX_FIFO_VALID_POS)
#define UART_STAT_TX_FIFO_FULL_POS      0x3
#define UART_STAT_TX_FIFO_FULL_MSK      (0x1 << UART_STAT_TX_FIFO_FULL_POS)
#define UART_STAT_INTR_ENABLED_POS      0x4
#define UART_STAT_INTR_ENABLED_MSK      (0x1 << UART_STAT_INTR_ENABLED_POS)
#define UART_STAT_ERR_OVERRUN_POS       0x5
#define UART_STAT_ERR_OVERRUN_MSK       (0x1 << UART_STAT_ERR_OVERRUN_POS)
#define UART_STAT_ERR_FRAME_POS         0x6
#define UART_STAT_ERR_FRAME_MSK         (0x1 << UART_STAT_ERR_FRAME_POS)
#define UART_STAT_ERR_PARITY_POS        0x7
#define UART_STAT_ERR_PARITY_MSK        (0x1 << UART_STAT_ERR_PARITY_POS)

#define UART_CTRL_RST_TX_FIFO_POS       0x0
#define UART_CTRL_RST_TX_FIFO_MSK       (0x1 << UART_CTRL_RST_TX_FIFO_POS)
#define UART_CTRL_RST_RX_FIFO_POS       0x1
#define UART_CTRL_RST_RX_FIFO_MSK       (0x1 << UART_CTRL_RST_RX_FIFO_POS)
#define UART_CTRL_ENABLE_INTR_POS       0x4
#define UART_CTRL_ENABLE_INTR_MSK       (0x1 << UART_CTRL_ENABLE_INTR_POS)





/* ================ RISC-V specific definitions ================ */
#define READ_CSR(REG) ({                          \
  unsigned long __tmp;                            \
  asm volatile ("csrr %0, " REG : "=r"(__tmp));  \
  __tmp; })

#define WRITE_CSR(REG, VAL) ({                    \
  asm volatile ("csrw " REG ", %0" :: "rK"(VAL)); })

#define SWAP_CSR(REG, VAL) ({                     \
  unsigned long __tmp;                            \
  asm volatile ("csrrw %0, " REG ", %1" : "=r"(__tmp) : "rK"(VAL)); \
  __tmp; })

#define SET_CSR_BITS(REG, BIT) ({                 \
  unsigned long __tmp;                            \
  asm volatile ("csrrs %0, " REG ", %1" : "=r"(__tmp) : "rK"(BIT)); \
  __tmp; })

#define CLEAR_CSR_BITS(REG, BIT) ({               \
  unsigned long __tmp;                            \
  asm volatile ("csrrc %0, " REG ", %1" : "=r"(__tmp) : "rK"(BIT)); \
  __tmp; })




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

  asm volatile("sw zero, 0(sp)");
  
  asm volatile("vle32.v v0, 0(sp)");
  asm volatile("vle32.v v1, 0(sp)");
  asm volatile("vle32.v v2, 0(sp)");
  asm volatile("vle32.v v3, 0(sp)");
  asm volatile("vle32.v v4, 0(sp)");
  asm volatile("vle32.v v5, 0(sp)");
  asm volatile("vle32.v v6, 0(sp)");
  asm volatile("vle32.v v7, 0(sp)");
  asm volatile("vle32.v v8, 0(sp)");
  asm volatile("vle32.v v9, 0(sp)");
  asm volatile("vle32.v v10, 0(sp)");
  asm volatile("vle32.v v11, 0(sp)");
  asm volatile("vle32.v v12, 0(sp)");
  asm volatile("vle32.v v13, 0(sp)");
  asm volatile("vle32.v v14, 0(sp)");
  asm volatile("vle32.v v15, 0(sp)");
  asm volatile("vle32.v v16, 0(sp)");
  asm volatile("vle32.v v17, 0(sp)");
  asm volatile("vle32.v v18, 0(sp)");
  asm volatile("vle32.v v19, 0(sp)");
  asm volatile("vle32.v v20, 0(sp)");
  asm volatile("vle32.v v21, 0(sp)");
  asm volatile("vle32.v v22, 0(sp)");
  asm volatile("vle32.v v23, 0(sp)");
  asm volatile("vle32.v v24, 0(sp)");
  asm volatile("vle32.v v25, 0(sp)");
  asm volatile("vle32.v v26, 0(sp)");
  asm volatile("vle32.v v27, 0(sp)");
  asm volatile("vle32.v v28, 0(sp)");
  asm volatile("vle32.v v29, 0(sp)");
  asm volatile("vle32.v v30, 0(sp)");
  asm volatile("vle32.v v31, 0(sp)");
  
  
  // call main
  main();

  while (1) {
    // don't exit
  }
}


int main() {
  // Create a union that can access the same memory as either float or uint32_t
  union {
    float f;
    uint32_t i;
  } converter;
  
  // Set the float value
  converter.f = .1f;
  
  // Now converter.i contains the bit pattern of 1.0f
  uint32_t a = converter.i;
  
  
  while (1) {
    *((volatile uint32_t *)UART_TXFIFO) = 'h';
    // *((volatile uint32_t *)UART_TXFIFO) = a;
    // *((volatile uint32_t *)UART_TXFIFO) = a;
    
    WRITE_CSR("0x51E", a);


    asm volatile("vfadd.vv v3, v2, v1");
    asm volatile("vfmul.vv v4, v2, v1");
    asm volatile("vfmacc.vv v5, v1, v2");
    asm volatile("vse32.v v2, 0(x1)");

    for (size_t i=0; i<DELAY_CYCLES; i+=1) {
      asm volatile("nop");
    }
    *((volatile uint32_t *)GPIO_OUTPUT) = 0x00000001;
    
    for (size_t i=0; i<DELAY_CYCLES; i+=1) {
      asm volatile("nop");
    }
    *((volatile uint32_t *)GPIO_OUTPUT) = 0x00000000;

    // *((volatile uint32_t *)UART_TXFIFO) = 0xCA;
  }
}