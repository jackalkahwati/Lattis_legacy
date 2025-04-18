/*
** Proprietary Rights Notice
**
** This material contains the valuable properties and trade secrets of:
**
**    Velo Labs
**    San Francisco, CA, USA
**
** All rights reserved. No part of this work may be reproduced, distributed, or
** transmitted in any form or by any means, including photocopying, recording,
** or other electronic or mechanical methods, without the prior written permission
** of Velo Labs.
**
** Copyright (c) 2015, Velo Labs
** Contains Confidential and Trade Secret Information
*/

/*
** File Name:  timers.c
**
** Purpose:    Routines to manage any timers we are using
*/

#include "master.h"
#include "stdio.h"
#include "hardware.h"

/* Master count of how long we have been alive and running */
unsigned int   runtimeSeconds;

/*
** Timer Setup
**
** Get the basic timer setup done here. Since this device is low power we likely won't keep a timer running
** all the time. But we need a timer now and then to help out with certain tasks.
**
** RTC0:
**    Using this during development to kick out a 1 second interrupt. Good chance this one might go away
**    in the future.
**
** TIMER2:
**    Set this guy up to be a timer that counts at a 1 microsecond (usec) rate. This guy should usually be
**    off, but should be available to be turned on to time things and then disabled again.
*/
void
Timer_Setup (void)
{
      /*
      ** We are a low power device so don't use SysTick as a timer. Apparently the RTC is a better
      ** system clock source for a battery device.
      **
      ** Avoid rounding, divide the clock by a power of 2 to give us our base tick rate. This works down
      ** to 125 mSec easily which right now that is good enough for what we want to do.
      **
      ** Set the LFCLK to a crystal oscillator and then start the clock.
      */
   NRF_CLOCK->LFCLKSRC = CLOCK_LFCLKSRC_SRC_Xtal;
   NRF_CLOCK->TASKS_LFCLKSTART = 1;
   NRF_RTC1->PRESCALER = (CPU_SLOW_CLOCK / 8) - 1;
   NRF_RTC1->TASKS_START = 1;
   NRF_RTC1->CC[0] = 8;
   NRF_RTC1->INTENSET = RTC_INTENSET_COMPARE0_Set << RTC_INTENSET_COMPARE0_Pos;

   NVIC_SetPriority(RTC1_IRQn, SKYLOCK_PRIORITY);
   NVIC_EnableIRQ(RTC1_IRQn);

      /*
      ** Setup TIMER 2 to a rate of 4 usec. Enable interrupts too, but leave the timer off for now.
      ** Prescaler is set to 4 which should be 16MHz / 2^4, which then should be 1 tick per usec
      **
      ** Note, failed to get timer to work with 24 or 32 bits. Product specification online seems to
      ** indicate there should be 24 bits, but so far I only have seen 16. So we will use 16. Not that
      ** important.
      */
   NRF_TIMER2->MODE = TIMER_MODE_MODE_Timer;
   NRF_TIMER2->BITMODE = TIMER_BITMODE_BITMODE_16Bit;
   NRF_TIMER2->PRESCALER = 6;
   NRF_TIMER2->TASKS_SHUTDOWN = 1;

      /*
      ** Setup handler, but we will only enable interrupts when the user has a case where they
      ** want/need an interrupt.
      */
   NVIC_SetPriority(TIMER2_IRQn, SKYLOCK_PRIORITY);
   NVIC_EnableIRQ(TIMER2_IRQn);
}

/*
** For now we just need a timer that we can poll, so no parameters.
*/
void
Timer2_Start (void)
{
   NRF_TIMER2->TASKS_START = 1;
}

void
Timer2_Stop (void)
{
   NRF_TIMER2->TASKS_STOP = 1;
   NRF_TIMER2->TASKS_SHUTDOWN = 1;
}

uint16_t
Timer2_Count (void)
{
   NRF_TIMER2->TASKS_CAPTURE[0] = 1;
   return (NRF_TIMER2->CC[0]);
}

void
TIMER2_Handler(void)
{
   /* We haven't enabled any interrupts yet so nothing to clear yet */
}


/*
** Use this as a basic timer for now. Currently set to go off once a second
*/
void
RTC1_Handler(void)
{
   runtimeSeconds++;

      /* Clear the interrupt */
   NRF_RTC1->EVENTS_COMPARE[0] = 0;

      /* Reset the counter so we will compare again */
   NRF_RTC1->TASKS_CLEAR = 1;

      /* Temporary for now */
   if (runtimeSeconds > 5)
      RTC1_IRQHandler();
}


