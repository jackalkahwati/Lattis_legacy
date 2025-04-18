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
** File Name:  startup.c
**
** Purpose:    Primary entry point for code startup. There is a low level boot strap
**             routine that will take care of C variable initialization prior to this
**             which also provides a hook to a routine here that is exectuted immediately
**             upon startup and prior to setting up the C variables.
**
**             In general this file should contain all the one time events associated
**             with the startup event.
**
** Key Implementation Notes:
**
** #1). Setup all the interrupts we want to use before we activate the Soft Device. Once the Soft Device is
**      running you are no longer supposed to be using the CMSIS functions to control interrupts. But based
**      on the current Soft Device (7.2) the only functions I can find (sd_nvic_SetPriority, in nrf_soc.c)
**      just call the CMSIS functions anyways. So just get things setup before the Soft Device and don't worry
**      about it. Also, did confirm that the Soft Device will not initialize if you setup these interrupts
**      with a high priority. It does check and won't load.
*/

#include "master.h"
#include "stdio.h"
#include "hardware.h"
#include "i2c.h"
#include "uart.h"
#include "utils.h"
#include "timers.h"
#include "ble_skylock.h"

/*
** We really only want these located in the raw, binary code image but if we don't reference them
** then the linker will drop them from our code image.
*/
const unsigned char PartNumber[] = PART_NUMBER_VER_M "." PART_NUMBER_REV_M "." PART_NUMBER_MINOR_M;
const unsigned char CopyRight[] = COPYRIGHT_M;

#if 0
/*
** This routine is called from the IAR boot loader. If we don't provide this function, then
** they will provide their own. For IAR, this is called a few instructions into the raw boot cycle.
** The CPU comes to life, grabs the reboot vector and jumps to that and within a couple instructions
** this is called. After the return from this function, then all the C variables are setup and
** "main" is called.
**
** The primary things to put into this function (if anything) would be any initialization of I/O
** that needs to be done quick and grabbing any data from RAM that we might want to recover after
** a reboot. Otherwise pretty much everything else can wait until main.
**
** Since this product is running on the Nordic chip and there is a Soft Device which basically has
** control of the chip there really isn't anything important that we should need. So compile this
** function out so that if later we switch to GCC or some other compiler we don't have a conflict
** since we really don't have a use for this.
**
** But keep the code here for future reference.
*/
unsigned char __low_level_init (void)
{
   return (1);  //Tell c-startup to init the segments
}
#endif

/*
** Starting point for actual code execution
*/
void main(void)
{
   unsigned int count;

   Timer_Setup();
   UART_Setup();

   I2C_Setup();

   Motor_Setup();
   TS_Setup();
   Magnet_Setup();

   Accelerometer_Setup ();

   MFG_Mode();

   BLE_Setup();


   count = 0;
   while (TRUE)
      {
      if (count != runtimeSeconds)
         {
         count = runtimeSeconds;
         TS_ToggleTestLED ();
         }
      }
}

/*
** If we need to restart, this is the way to do it. This function will make
** sure we restart properly.
*/
void
ForceRestart (void)
{
   printf("Got restart request\n");
   UART0_TxFlush ();

   __disable_interrupt();

#ifdef DEBUG
   while (TRUE);
#else
   NVIC_SystemReset();
#endif
}

/*
** If we take one of these exceptions, what we will get on the stack when we hit the exception is the following:
**    PSR
**    PC
**    LR
**    R12
**    R3
**    R2
**    R1
**    R0
**
** If we keep each fault handler simple then they will push R7 and LR.
** The Fault Exit routine will also push R4 and LR
**
** So given all that, We want PC which should then be 11 pushes back on the stack. Go back 10 since SP is already
** pointing at the last one.
*/
void
FaultExit (unsigned char *str)
{
   unsigned int *sp = GetStackPointer();

   printf("%s Fault at %lx\n", str, *(sp + 10));

      /* Make sure we reboot after this */
   ForceRestart();
}


void
NMI_Handler (void)
{
   FaultExit ("NMI");
}

void
HardFault_Handler (void)
{
//break this down for MPU error
   FaultExit ("Hard");
}

void
intvec_NotSupported (void)
{
   FaultExit ("Not Supported");
}

