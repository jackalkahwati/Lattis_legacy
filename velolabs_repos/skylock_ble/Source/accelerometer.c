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
** File Name:  accelerometer.c
**
** Purpose:    Routines to manage the accelerometer
*/

#include "master.h"
#include "stdio.h"
#include "hardware.h"
#include "i2c.h"
#include "utils.h"

#define  MAG_WHO_AM_I      (0x0F)

#define  ACC_DELAY         (5)
#define  ACC_TIMEOUT       (1000 / ACC_DELAY)

/*
** Function to write bytes out the I2C port. Not sure if a return value is really needed
** or not. Thinking is if we have an error, then this routine will call I2C_Abort. But I
** suppose the caller may want to know and may want to return.
**
** Returns 0 if there is no errors
** Returns non-zero if there is a problem writing
*/
uint8_t
ACC_Write (uint8_t addr, const uint8_t *data, uint8_t len, uint8_t stop)
{
   unsigned int delayLimit;

      /* If no data then return an error */
   if (!len)
      return (I2C_ERROR_USAGE);

   NRF_TWI1->EVENTS_TXDSENT = 0;
   NRF_TWI1->ADDRESS = addr;
   NRF_TWI1->TXD = *data++;
   NRF_TWI1->TASKS_STARTTX = 1;

   while (len)
      {
      len--;

         /*
         ** Setup 2 exit conditions from this loop. A timeout or the most likely reason, we sent the byte.
         ** We could also search for ERROR but that is not necessary. The timeout will catch both the timeout
         ** and error condition. We can test for error afterwards though. Just remember to test for error before
         ** timeout since an error will also be a timeout.
         */
      delayLimit = 0;
      while ((NRF_TWI1->EVENTS_TXDSENT == 0) && (delayLimit < ACC_TIMEOUT))
         {
         UTIL_DelayUsec(ACC_DELAY);
         delayLimit++;
         }

//      if (delayLimit > debugMaxI2CWaitTime)
//         debugMaxI2CWaitTime = delayLimit;

         /*
         ** Did the TWI device bail out with an error. Is that why we timed out?
         */
      if (NRF_TWI1->EVENTS_ERROR)
         {
//         debugErrorCount++;
//         I2C_Abort();
         return (I2C_ERROR_TWI);
         }

         /*
         ** If we timeout, call the abort routine to reset the port
         */
      if (delayLimit >= ACC_TIMEOUT)
         {
//         I2C_Abort();
         return (I2C_ERROR_TIMEOUT);
         }

      NRF_TWI1->EVENTS_TXDSENT = 0;

      if (len)
         NRF_TWI1->TXD = *data++;
      }

      /*
      ** If the user actually wants to do a read they will call us with a write first and in that case
      ** they won't want us to send the STOP. So this parameter provides that option.
      */
   if (stop)
      {
      NRF_TWI1->EVENTS_STOPPED = 0;
      NRF_TWI1->TASKS_STOP = 1;

         /* Wait until stop sequence is sent */
      delayLimit = 0;
      while ((NRF_TWI1->EVENTS_STOPPED == 0) && (delayLimit < ACC_TIMEOUT))
         {
         UTIL_DelayUsec(ACC_DELAY);
         delayLimit++;
         }

//      if (delayLimit > debugMaxI2CStopTime)
//         debugMaxI2CStopTime = delayLimit;

      if (NRF_TWI1->EVENTS_ERROR)
         {
//         debugErrorCount++;
//         I2C_Abort();
         return (I2C_ERROR_TWI);
         }

      if (delayLimit >= ACC_TIMEOUT)
         {
//         I2C_Abort();
         return (I2C_ERROR_TIMEOUT);
         }
      }

   return (I2C_ERROR_NONE);
}

/*
** This function will read a fixed number of bytes back from the specified device.
**
** Returns 0 if there is no errors
** Returns non-zero if thre is a problem
*/
uint8_t
ACC_Read (uint8_t addr, uint8_t *data, uint8_t len)
{
   unsigned int delayLimit;

      /* If no data then return an error */
   if (!len)
      return (I2C_ERROR_USAGE);

   NRF_PPI->CHENCLR = PPI_CHENCLR_CH0_Msk;
   NRF_TWI1->EVENTS_RXDREADY = 0;
   NRF_TWI1->ADDRESS = addr;

   if (len == 1)
      NRF_PPI->CH[1].TEP = (uint32_t)&NRF_TWI1->TASKS_STOP;
   else
      NRF_PPI->CH[1].TEP = (uint32_t)&NRF_TWI1->TASKS_SUSPEND;

   NRF_TWI1->EVENTS_STOPPED = 0;
   NRF_PPI->CHENSET = PPI_CHENSET_CH0_Msk;
   NRF_TWI1->TASKS_STARTRX = 1;

   while (len)
      {
         /*
         ** Setup 2 exit conditions from this loop. A timeout or the most likely reason, we received a byte.
         ** Note an error from the TWI module could also happen but that will lead to a timeout. So just
         ** check for a timeout and catch the error afterwards.
         */
      delayLimit = 0;
      while ((NRF_TWI1->EVENTS_RXDREADY == 0) && (delayLimit < ACC_TIMEOUT))
         {
         UTIL_DelayUsec(ACC_DELAY);
         delayLimit++;
         }

//      if (delayLimit > debugMaxI2CReadWait)
//         debugMaxI2CReadWait = delayLimit;

      NRF_TWI1->EVENTS_RXDREADY = 0;

         /*
         ** Search for these errors seperately so we can track them for now.
         */
      if (NRF_TWI1->EVENTS_ERROR)
         {
//         debugErrorCount++;
//         I2C_Abort();
         return (I2C_ERROR_TWI);
         }

         /*
         ** If we timeout, call the abort routine to reset the port
         */
      if (delayLimit >= ACC_TIMEOUT)
         {
//         I2C_Abort();
         return (I2C_ERROR_TIMEOUT);
         }

      *data++ = NRF_TWI1->RXD;

      len--;

      if (len == 1)
         NRF_PPI->CH[1].TEP = (uint32_t)&NRF_TWI1->TASKS_STOP;

      if (len)
         {
            /* See twi_hw_master.c and the app note about CPU problem */
         UTIL_DelayUsec(20);
         NRF_TWI1->TASKS_RESUME = 1;
         }
      }

      /* Wait until stop sequence is done */
   delayLimit = 0;
   while ((NRF_TWI1->EVENTS_STOPPED == 0) && (delayLimit < ACC_TIMEOUT))
      {
      UTIL_DelayUsec(ACC_DELAY);
      delayLimit++;
      }

//   if (delayLimit > debugMaxI2CStopTime)
//      debugMaxI2CStopTime = delayLimit;

   if (NRF_TWI1->EVENTS_ERROR)
      {
//      debugErrorCount++;
//      I2C_Abort();
      return (I2C_ERROR_TWI);
      }

   if (delayLimit >= ACC_TIMEOUT)
      {
//      I2C_Abort();
      return (I2C_ERROR_TIMEOUT);
      }

   NRF_TWI1->EVENTS_STOPPED = 0;
   NRF_PPI->CHENCLR = PPI_CHENCLR_CH0_Msk;

   return (I2C_ERROR_NONE);
}


/*
** For now the Accelerometer is on a 2nd, I2C bus. So just double up the code to support this for now.
** On the next board we should get back to only 1 I2C bus.
*/
void
Accelerometer_Setup (void)
{
   uint8_t data_byte[6];

      /*
      ** Per the nRF51 Reference manual in the TWI section. To get proper signal levels
      ** on SCL and SDA when the TWI interface is off or when it is disabled, these pins
      ** should be configured as GPIO pins with the following settings:
      **    Direction should be Input
      **    Drive strength should be S0D1
      **    Pull-up, not stated but that seems to make the most sense
      **    Connected, not stated but that allows us to query the pin status
      **    Sense disabled, not stated but don't believe this would be needed on these pins
      */
   NRF_GPIO->PIN_CNF[LIS2DH_CLK] =
            (GPIO_PIN_CNF_DIR_Input      << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Pullup    << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

   NRF_GPIO->PIN_CNF[LIS2DH_CS] =
            (GPIO_PIN_CNF_DIR_Input      << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Pullup    << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

      /*
      ** For the current hardware, there is a CS pin that is connected to Pin 13 (LIS2DH_MOSI)
      ** This needs to be tied high for I2C. Also there the I2C address pin which needs to be
      ** driven either high or low and is connected to pin 14 (LIS2DH_MISO).
      */
   NRF_GPIO->PIN_CNF[LIS2DH_MOSI] =
            (GPIO_PIN_CNF_DIR_Output     << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Pullup    << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

#if defined(BOARD_NUMBER_1)
   NRF_GPIO->PIN_CNF[LIS2DH_MISO] =
            (GPIO_PIN_CNF_DIR_Output     << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Disabled  << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

   NRF_GPIO->OUTCLR = (1UL << LIS2DH_MISO);
#endif

   NRF_GPIO->OUTSET = (1UL << LIS2DH_MOSI);

   NRF_TWI1->EVENTS_RXDREADY = 0;
   NRF_TWI1->EVENTS_TXDSENT = 0;
   NRF_TWI1->PSELSCL = LIS2DH_CLK;
   NRF_TWI1->PSELSDA = LIS2DH_CS;
   NRF_TWI1->FREQUENCY = TWI_FREQUENCY_FREQUENCY_K100 << TWI_FREQUENCY_FREQUENCY_Pos;

      /* This channel is used during reads, source of action is always BB */
   NRF_PPI->CH[1].EEP = (uint32_t)&NRF_TWI1->EVENTS_BB;

   NRF_TWI1->ENABLE = TWI_ENABLE_ENABLE_Enabled << TWI_ENABLE_ENABLE_Pos;

     /* In case a slave may be stuck, try to flush out bus */
//   I2C_Abort();


   data_byte[0] = MAG_WHO_AM_I;
   ACC_Write (I2C_ADDR_ACCEL, data_byte, 1, FALSE);
   ACC_Read (I2C_ADDR_ACCEL, data_byte, 1);
}


