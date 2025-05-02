#ifndef __GLOSSY_H
#define __GLOSSY_H


#include "uart.h"

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


void exit(int code) {
  WRITE_CSR(CSR_SYSCALL1, code);
  WRITE_CSR(CSR_SYSCALL0, SYSCALL_EXIT);
}

void putfloat(uint32_t f) {
  WRITE_CSR(CSR_SYSCALL1, f);
  WRITE_CSR(CSR_SYSCALL0, SYSCALL_PRINT_F32);
  WRITE_CSR(CSR_SYSCALL0, 0);

  uart_write(UART0, SYSCALL_PRINT_F32);
  uart_write(UART0, f >> 24);
  uart_write(UART0, f >> 16);
  uart_write(UART0, f >> 8);
  uart_write(UART0, f);
}


// === system functions === //
int putchar(int c) {
  WRITE_CSR(CSR_SYSCALL1, c);
  WRITE_CSR(CSR_SYSCALL0, SYSCALL_PRINT_CHAR);
  WRITE_CSR(CSR_SYSCALL0, 0);

  uart_write(UART0, SYSCALL_PRINT_CHAR);
  uart_write(UART0, c);
}

int fflush(FILE *fd) {
  while (!READ_BITS(UART0->STAT, UART_STAT_TX_FIFO_EMPTY_MSK)) {
    asm volatile("nop");
  }
}

void prints(const char *str) {
  while (*str) {
    putchar(*str);
    str += 1;
  }
}


void sleep(uint32_t cycles) {
  for (size_t i=0; i<cycles; i+=1) {
    asm volatile("nop");
  }
}


#endif /* __GLOSSY_H */