#ifndef __GPIO_H
#define __GPIO_H

#include <stdint.h>
#include <stddef.h>
#include <stdio.h>

#include "metal.h"


/* ======== Axi4Lite Uart Lite ======== */
typedef struct {
  uint32_t OUTPUT;
} XilinxGpio;


#define GPIOA_BASE  0x10000000

#define GPIOA                           ((XilinxGpio *) GPIOA_BASE)

#endif  // __GPIO_H


