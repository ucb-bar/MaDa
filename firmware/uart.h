#ifndef __UART_H
#define __UART_H

#include <stdint.h>
#include <stddef.h>
#include <stdio.h>

#include "metal.h"


/* ======== Axi4Lite Uart Lite ======== */
typedef struct {
  uint32_t RXFIFO;
  uint32_t TXFIFO;
  uint32_t STAT;
  uint32_t CTRL;
} XilinxUart;



#define UART_STAT_RX_FIFO_VALID_POS     0x0
#define UART_STAT_RX_FIFO_VALID_MSK     (0x1 << UART_STAT_RX_FIFO_VALID_POS)
#define UART_STAT_RX_FIFO_FULL_POS      0x1
#define UART_STAT_RX_FIFO_FULL_MSK      (0x1 << UART_STAT_RX_FIFO_FULL_POS)
#define UART_STAT_TX_FIFO_EMPTY_POS     0x2
#define UART_STAT_TX_FIFO_EMPTY_MSK     (0x1 << UART_STAT_TX_FIFO_EMPTY_POS)
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


void uart_write(XilinxUart *uart, uint32_t data) {
  while (READ_BITS(uart->STAT, UART_STAT_TX_FIFO_FULL_MSK)) {
    asm volatile("nop");
  }
  uart->TXFIFO = READ_BITS(data, 0xFF);
}




#endif  // __UART_H
