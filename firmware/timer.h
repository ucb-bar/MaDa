#ifndef __TIMER_H
#define __TIMER_H

#include <stdint.h>
#include <stddef.h>
#include <stdio.h>

#include "metal.h"


/* ======== Axi4Lite Timer ======== */
typedef struct {
  /** Timer 0 Control and Status Register */
  volatile uint32_t TCSR0;
  /** Timer 0 Load Register */
  volatile uint32_t TLR0;
  /** Timer 0 Counter Register */
  volatile uint32_t TCR0;

  uint8_t RESERVED0[4];
  /** Timer 1 Control and Status Register */
  volatile uint32_t TCSR1;
  /** Timer 1 Load Register */
  volatile uint32_t TLR1;
  /** Timer 1 Counter Register */
  volatile uint32_t TCR1;

  uint8_t RESERVED1[4];
} XilinxTimer;


#define TIMER_TCSRX_MDTX_POS      0x0
#define TIMER_TCSRX_MDTX_MSK      (0x1 << TIMER_TCSRX_MDTX_POS)
#define TIMER_TCSRX_UDTX_POS      0x1
#define TIMER_TCSRX_UDTX_MSK      (0x1 << TIMER_TCSRX_UDTX_POS)
#define TIMER_TCSRX_GENTX_POS     0x2
#define TIMER_TCSRX_GENTX_MSK     (0x1 << TIMER_TCSRX_GENTX_POS)
#define TIMER_TCSRX_CAPTX_POS     0x3
#define TIMER_TCSRX_CAPTX_MSK     (0x1 << TIMER_TCSRX_CAPTX_POS)
#define TIMER_TCSRX_ARHTX_POS     0x4
#define TIMER_TCSRX_ARHTX_MSK     (0x1 << TIMER_TCSRX_ARHTX_POS)
#define TIMER_TCSRX_LOADX_POS     0x5
#define TIMER_TCSRX_LOADX_MSK     (0x1 << TIMER_TCSRX_LOADX_POS)
#define TIMER_TCSRX_ENITX_POS     0x6
#define TIMER_TCSRX_ENITX_MSK     (0x1 << TIMER_TCSRX_ENITX_POS)
#define TIMER_TCSRX_ENTX_POS      0x7
#define TIMER_TCSRX_ENTX_MSK      (0x1 << TIMER_TCSRX_ENTX_POS)
#define TIMER_TCSRX_TXINT_POS     0x8
#define TIMER_TCSRX_TXINT_MSK     (0x1 << TIMER_TCSRX_T0INT_POS)
#define TIMER_TCSR0_PWMA0_POS     0x9
#define TIMER_TCSR0_PWMA0_MSK     (0x1 << TIMER_TCSR0_PWMA0_POS)
#define TIMER_TCSR0_ENALL_POS     0xA
#define TIMER_TCSR0_ENALL_MSK     (0x1 << TIMER_TCSR0_ENALL_POS)
#define TIMER_TCSR0_CASC_POS      0xB
#define TIMER_TCSR0_CASC_MSK      (0x1 << TIMER_TCSR0_CASC_POS)

#define TIMER_TCSR1_PWMB0_POS     0x9
#define TIMER_TCSR1_PWMB0_MSK     (0x1 << TIMER_TCSR1_PWMB0_POS)



void timer_pwm_init(XilinxTimer *timer) {
  // counting down
  SET_BITS(timer->TCSR0, TIMER_TCSRX_UDTX_MSK);
  SET_BITS(timer->TCSR1, TIMER_TCSRX_UDTX_MSK);

  // set timer mode to generate mode
  CLEAR_BITS(timer->TCSR0, TIMER_TCSRX_MDTX_MSK);
  CLEAR_BITS(timer->TCSR1, TIMER_TCSRX_MDTX_MSK);

  // enable PWM mode
  SET_BITS(timer->TCSR0, TIMER_TCSR0_PWMA0_MSK);
  SET_BITS(timer->TCSR1, TIMER_TCSR1_PWMB0_MSK);

  // enable generation mode
  SET_BITS(timer->TCSR0, TIMER_TCSRX_GENTX_MSK);
  SET_BITS(timer->TCSR1, TIMER_TCSRX_GENTX_MSK);
}

void timer_pwm_set_period(XilinxTimer *timer, uint32_t period_count) {
  // PWM_PERIOD = (TLR0 + 2) * AXI_CLOCK_PERIOD
  // PWM_HIGH_TIME = (TLR1 + 2) * AXI_CLOCK_PERIOD

  // load period
  timer->TLR0 = period_count - 2;
  SET_BITS(timer->TCSR0, TIMER_TCSRX_LOADX_MSK);
  CLEAR_BITS(timer->TCSR0, TIMER_TCSRX_LOADX_MSK);
}

void timer_pwm_set_high_time(XilinxTimer *timer, uint32_t high_time_count) {
  // load high time
  timer->TLR1 = high_time_count - 2;
  SET_BITS(timer->TCSR1, TIMER_TCSRX_LOADX_MSK);
  CLEAR_BITS(timer->TCSR1, TIMER_TCSRX_LOADX_MSK);
}

void timer_pwm_enable(XilinxTimer *timer) {
  SET_BITS(timer->TCSR0, TIMER_TCSR0_ENALL_MSK);
}



#endif  // __TIMER_H


