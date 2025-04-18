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
** File Name:  motor.c
**
** Purpose:    Routines to manage the motor circuit
*/

#include "master.h"
#include "stdio.h"
#include "hardware.h"
#include "i2c.h"
#include "timers.h"
#include "utils.h"

volatile uint32_t MOTOR_Enc01Events;
volatile uint32_t MOTOR_Enc02Events;

/*
** Motor setup function. There are only 2 registers so there shouldn't be that much to actually do.
** But it appears if you don't first read the motor that the first command to the motor then doesn't work.
** So read the registers once.
**
** Although once we can power off the motor, we will probably need a motor power up and down function.
**
** For now, setup will do the following:
** #1). Setup the 2 encoder bits to go be interrupts. This also may change because once we know direction
**      we really only should need 1 bit.
**
** #2). Read from the motor chip so it is ready to go.
**
** #3). Setup the motor ADC pin
*/
void
Motor_Setup (void)
{
   uint8_t buffer[2];

      /*
      ** Setup the 2 encoding bits for interrupts right now. Go ahead and setup the pins as
      ** inputs for now. The GPIOTE seems to indicate it doesn't matter what we set them to
      ** but in case we don't use that, have them setup as an input.
      */
   NRF_GPIO->PIN_CNF[ENC_01] =
            (GPIO_PIN_CNF_DIR_Input      << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Disabled  << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

   NRF_GPIO->PIN_CNF[ENC_02] =
            (GPIO_PIN_CNF_DIR_Input      << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Disabled  << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

      /* Set ENC GPIO pins to events, rising edge */
   NRF_GPIOTE->CONFIG[0] =
            (GPIOTE_CONFIG_MODE_Event << GPIOTE_CONFIG_MODE_Pos)
          | (ENC_01 << GPIOTE_CONFIG_PSEL_Pos)
          | (GPIOTE_CONFIG_POLARITY_LoToHi << GPIOTE_CONFIG_POLARITY_Pos);

   NRF_GPIOTE->CONFIG[1] =
            (GPIOTE_CONFIG_MODE_Event << GPIOTE_CONFIG_MODE_Pos)
          | (ENC_02 << GPIOTE_CONFIG_PSEL_Pos)
          | (GPIOTE_CONFIG_POLARITY_LoToHi << GPIOTE_CONFIG_POLARITY_Pos);

   NRF_GPIOTE->INTENSET = GPIOTE_INTENSET_IN0_Msk | GPIOTE_INTENSET_IN1_Msk;

   NVIC_ClearPendingIRQ (GPIOTE_IRQn);
   NVIC_SetPriority(GPIOTE_IRQn, SKYLOCK_PRIORITY);
   NVIC_EnableIRQ (GPIOTE_IRQn);

      /*
      ** Make sure motor is clear. I saw case where after reboot, register 0 was FF and status register was 04. Actually
      ** turned off power supply to reboot and condition was still there. Writing 0 to the control register cleared the
      ** condition. So, it would seem a smart thing to do would be to do that as part of initialization.
      */
   buffer[0] = MOTOR_CTRL_REG;
   buffer[1] = MOTOR_STANDBY;
   I2C_Write (I2C_ADDR_MOTOR, buffer, 2, TRUE);
}

/*
** Kick the motor off in either the forward or backwards direction
*/
unsigned int
Motor_Start (bool lock)
{
   uint8_t buffer[2];
   unsigned int status;

      /*
      ** Might as well just start these off at zero each time. That way anything testing can just reference them from
      ** 0 all of the time.
      */
   MOTOR_Enc01Events = 0;
   MOTOR_Enc02Events = 0;

      /*
      ** Motor doesn't seem to respond until we read it. So always read it before we try to move it. Since we will
      ** be powering the motor down, probably want to do this everytime.
      */
   buffer[0] = MOTOR_CTRL_REG;
   status = I2C_Write (I2C_ADDR_MOTOR, buffer, 1, FALSE);
   if (status == 0)
      {
      status = I2C_Read(I2C_ADDR_MOTOR, buffer, 2);

      if (status == 0)
         {
         if (lock)
            buffer[1] = MOTOR_REVERSE;
         else
            buffer[1] = MOTOR_FORWARD;

         status = I2C_Write (I2C_ADDR_MOTOR, buffer, 2, TRUE);
         }
      }

   return (status);
}

/*
** Stop the motor by first braking, then waiting for the motor to come to a stop and then putting the motor
** into standby mode.
*/
unsigned int
Motor_Stop (void)
{
   uint8_t buffer[2];
   uint16_t latchTime;
   uint32_t latchEnc;
   unsigned int status;

   mfgBuffer[0] = MOTOR_STATUS_REG;
   I2C_FullRead (I2C_ADDR_MOTOR, mfgBuffer, 1);
   if (mfgBuffer[1])
      printf("Motor error step 1 %u\n", mfgBuffer[1]);

      /*
      ** Use the brake option to stop the motor because it is much quicker. On a test where I was only going 10 counts, it looks
      ** like it took an extra 5 to stop. While the same test with going to standby first took an extra 20 or so steps.
      */
   buffer[0] = MOTOR_CTRL_REG;
   buffer[1] = MOTOR_BRAKE;
   status = I2C_Write (I2C_ADDR_MOTOR, buffer, 2, TRUE);

   if (status == 0)
      {
         /*
         ** Wait for motor to actually stop. Watch both the encoder and a timer. We are looking for 20 milliseconds
         ** after the last encoder reading. Then we will put the motor in standby mode.
         */
      latchTime = Timer2_Count();
      latchEnc = MOTOR_Enc01Events;
      while ((uint16_t)(Timer2_Count() - latchTime) < TIMER2_20_MSEC)
         {
         if (latchEnc != MOTOR_Enc01Events)
            {
               /*
               ** If the encoder moves, latch a new time value and start looking again.
               */
            latchTime = Timer2_Count();
            latchEnc = MOTOR_Enc01Events;
            }
         }

      if (mfgBuffer[1] == 0)
         {
         mfgBuffer[0] = MOTOR_STATUS_REG;
         I2C_FullRead (I2C_ADDR_MOTOR, mfgBuffer, 1);
         if (mfgBuffer[1])
            printf("Motor error step 2 %u\n", mfgBuffer[1]);
         }

         /*
         ** Motor does not seem to like the VSET ADC set to non-zero when turning the motor off. Had a test where I turned the motor
         ** on, then turned it off and left VSET field enabled. I then tried the test again and the motor did not move. But if
         ** I change the motor off function to write all 0's to the control register then there was no problem.
         */
      buffer[0] = MOTOR_CTRL_REG;
      buffer[1] = MOTOR_STANDBY;
      status = I2C_Write (I2C_ADDR_MOTOR, buffer, 2, TRUE);

      if (mfgBuffer[1] == 0)
         {
         mfgBuffer[0] = MOTOR_STATUS_REG;
         I2C_FullRead (I2C_ADDR_MOTOR, mfgBuffer, 1);
         if (mfgBuffer[1])
            printf("Motor error step 2 %u\n", mfgBuffer[1]);
         }
      }

   return (status);
}

/*
** There isn't much to configure on ADC and since we will have more than 1 channel to read, might as well just hit everything each time a read
** is requested.
**
** The data sheet lists 68 usec typical for a conversion. Using timer 2 set at 4usec units I am seeing about 19 ticks on average for this function
** to run. So basically just under 100 usec it looks like. Note the same specification mentions that a conversion pulls 260 uA during the
** conversion. Something to consider if we are looking for absolute power savings.
*/
uint16_t
Motor_ADC (void)
{
   uint16_t result;

      /*
      ** Setup motor ADC pin
      */
   NRF_ADC->CONFIG =
            ADC_CONFIG_RES_10bit
          | (ADC_CONFIG_INPSEL_AnalogInputNoPrescaling << ADC_CONFIG_INPSEL_Pos)
          | (ADC_CONFIG_REFSEL_VBG << ADC_CONFIG_REFSEL_Pos)
          | (ADC_CONFIG_PSEL_AnalogInput3 << ADC_CONFIG_PSEL_Pos)
          | (ADC_CONFIG_EXTREFSEL_None << ADC_CONFIG_EXTREFSEL_Pos);

   NRF_ADC->ENABLE = ADC_ENABLE_ENABLE_Enabled;

   NRF_ADC->TASKS_START = 1;

   while (NRF_ADC->EVENTS_END == 0);

   result = NRF_ADC->RESULT;

   NRF_ADC->EVENTS_END = 0;
   NRF_ADC->ENABLE = ADC_ENABLE_ENABLE_Disabled;
   NRF_ADC->TASKS_STOP = 1;

   return (result);
}

/*
** Move the motor into either the locked position (lock is TRUE) or the unlock position (lock is FALSE)
**
** Counts is the number of encoder pulses that should be measured. There are extra pulses that happen
** once the brake is thrown, but those are somewhat variable. This routine will attempt to account for
** those. Meaning if asked to move 120 pulses and we think it will take 5 to stop, we will move 115 and
** stop the motor. After the motor stops, if the total was indeed 120, we will return 120. If it is 121
** we will return 121 and the user knows we went 1 farther than they asked.
**
** Basically this is the low level workhorse routine for moving the lock. A function must sit above this
** that then manages where the lock is (lock/unlock) and also how many counts it may be off so that the
** next time the lock moves, that adjustment will be factored in.
**
** If the lock crashes into the stop, then we will back the lock up by 5 counts plus the brake amount. The
** total amount we moved will then be returned. Meaning if we move 120 and crash, then we will back up 5,
** plus the brake amount and maybe return something like 110.
*/
#if 0
/* this motor lock tries to use ADC and timing of encoder to stop */
uint16_t
Motor_Lock (bool lock, uint16_t encoderCounts)
{
   bool     crash = FALSE;
   uint16_t t1, t2, lastTime[3], timeAve, timeMinAve;
   uint16_t lastAdc[3], adcAve, adcMinAve;
   uint32_t latch_enc;
   unsigned int status;
   unsigned int count;

printf("Motor lock called with %u\n", lock);
   status = Motor_Start (lock);
   if (status)
      {
      printf("Write failed with %u\n", status);
      return (0);
      }

   t1 = Timer2_Count();
   latch_enc = 0;
   timeMinAve = adcMinAve = timeAve = adcAve = 10000;
   count = 10;
   while (MOTOR_Enc01Events < (encoderCounts - 5))
      {
      if (latch_enc != MOTOR_Enc01Events)
         {
         t2 = Timer2_Count();
         latch_enc = MOTOR_Enc01Events;

         lastTime[0] = lastTime[1];
         lastTime[1] = lastTime[2];
         lastTime[2] = (uint16_t)(t2 - t1);
         lastAdc[0] = lastAdc[1];
         lastAdc[1] = lastAdc[2];
         lastAdc[2] = Motor_ADC();

            /*
            ** First couple of measurements could be bad and we are avoiding doing anything with them, so just
            ** burn these.
            */
         if (latch_enc <= 4)
            {
            timeMinAve = lastTime[0] = lastTime[1] = timeAve = lastTime[2];
            adcMinAve = lastAdc[0] = lastAdc[1] = adcAve = lastAdc[2];
            }

         timeAve = (lastTime[0] + lastTime[1] + lastTime[2]) / 3;
         adcAve = (lastAdc[0] + lastAdc[1] + lastAdc[2]) / 3;

         count--;
         if (count)
            {
            if (timeAve < timeMinAve)
               timeMinAve = timeAve;

            if (adcAve < adcMinAve)
               adcMinAve = adcAve;
            }
         else
            {
            count = 10;
            timeMinAve = timeAve;
            adcMinAve = adcAve;
            }

         if ((latch_enc < 20) || (latch_enc > 100))
            printf("%03u: %03u/%03u %04u/%04u\n", latch_enc, lastAdc[2], adcAve, lastTime[2], timeAve);
         t1 = t2;
         }

      if ((MOTOR_Enc01Events > 12) && (timeAve > (timeMinAve + 10)) && (adcAve > (adcMinAve + 5)))
         {
         crash = TRUE;
         break;
         }

      if ((uint16_t)(Timer2_Count() - t1) > TIMER2_20_MSEC)
         break;
      }

   status = Motor_Stop();
   printf("Enc #1: %u   Enc #2: %u  Status: %02x %02x\n", MOTOR_Enc01Events, MOTOR_Enc02Events, mfgBuffer[1], status);
   count = MOTOR_Enc01Events;

      /*
      ** Did we crash? If so, then backup some!
      */
   if (crash)
      {
         /* Need to backup */
      status = Motor_Start (!lock);
      t1 = Timer2_Count();
      while (MOTOR_Enc01Events < 5)
         {
         if ((uint16_t)(Timer2_Count() - t1) > TIMER2_20_MSEC)
            break;
         }

      status = Motor_Stop ();
      count -= MOTOR_Enc01Events;
      }
   printf("Total steps requested and taken %u %u %u\n", encoderCounts, count, crash);

   return (count);
}
#else
/* This version just uses fixed encoder counts to lock and unlock */
uint16_t
Motor_Lock (bool lock, uint16_t encoderCounts)
{
   uint16_t t1, t2, lastTime[3], timeAve, timeMinAve;
   uint32_t latch_enc;
   unsigned int status;
   unsigned int count;

printf("Motor lock called with %u %u\n", lock, encoderCounts);
   status = Motor_Start (lock);
   if (status)
      {
      printf("Write failed with %u\n", status);
      return (0);
      }

   t1 = Timer2_Count();
   latch_enc = 0;
   timeMinAve = timeAve = 10000;
   count = 10;
   while (MOTOR_Enc01Events < (encoderCounts - 5))
      {
      if (latch_enc != MOTOR_Enc01Events)
         {
         t2 = Timer2_Count();
         latch_enc = MOTOR_Enc01Events;

         lastTime[0] = lastTime[1];
         lastTime[1] = lastTime[2];
         lastTime[2] = (uint16_t)(t2 - t1);

            /*
            ** First couple of measurements could be bad and we are avoiding doing anything with them, so just
            ** burn these.
            */
         if (latch_enc <= 4)
            {
            timeMinAve = lastTime[0] = lastTime[1] = timeAve = lastTime[2];
            }

         timeAve = (lastTime[0] + lastTime[1] + lastTime[2]) / 3;

         count--;
         if (count)
            {
            if (timeAve < timeMinAve)
               timeMinAve = timeAve;
            }
         else
            {
            count = 10;
            timeMinAve = timeAve;
            }

         printf("%03u: %04u/%04u\n", latch_enc, lastTime[2], timeAve);
         t1 = t2;
         }

         /* If encoder not moving, stop */
      if ((uint16_t)(Timer2_Count() - t1) > TIMER2_20_MSEC)
         break;
      }

   status = Motor_Stop();
   printf("Enc #1: %u   Enc #2: %u  Status: %02x %02x\n", MOTOR_Enc01Events, MOTOR_Enc02Events, mfgBuffer[1], status);
   count = MOTOR_Enc01Events;

   printf("Total steps requested and taken %u %u\n", encoderCounts, count);

   return (count);
}
#endif


/*
** There is only 1 interrupt handler for GPIO pins so if we get an event we need to search which device
** caused the event.
*/
void
GPIOTE_Handler (void)
{
      /* Checking Config #0 */
   if (NRF_GPIOTE->EVENTS_IN[0])
      {
      MOTOR_Enc01Events++;
      NRF_GPIOTE->EVENTS_IN[0] = 0;
      }

      /* Checking Config #1 */
   else if (NRF_GPIOTE->EVENTS_IN[1])
      {
      MOTOR_Enc02Events++;
      NRF_GPIOTE->EVENTS_IN[1] = 0;
      }

      /*
      ** Checking Config #2 - Touch sensor IRQ. The interrupt will only fire once but
      ** it won't fire again until you read the touch sensor. So application has to
      ** respond to the IRQCounter and read the button states. That will allow the
      ** touch sensor to enable the interrupt pin again if there is more activity.
      */
   else if (NRF_GPIOTE->EVENTS_IN[2])
      {
      NRF_GPIOTE->EVENTS_IN[2] = 0;
      TS_IRQCounter++;
      }

         /* Checking Config #2 */
   else if (NRF_GPIOTE->EVENTS_IN[2])
      {
      NRF_GPIOTE->EVENTS_IN[2] = 0;
      }
}
