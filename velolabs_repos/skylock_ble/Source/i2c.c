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
** File Name:  i2c.c
**
** Purpose:    All the support functions needed to setup, read and write data
**             over an I2C interface.
*/

#include "master.h"
#include "stdio.h"
#include "hardware.h"
#include "i2c.h"
#include "utils.h"

   /* Bit bang delay for SCL/SDA given 100KHz clock constraint */
#define  I2C_DELAY         (5)

   /*
   ** Setup a timeout for lack of I2C response. No reason to set this long yet. If we run into
   ** something that takes more time then obviously stretch this out, but right now maximum
   ** measure is about 200 usec and that seems to include some setup time or something.
   */
#define  I2C_TIMEOUT       (2000 / I2C_DELAY)

   /*
   ** DEBUG Variables
   **
   ** Remove these once we have proved out what is going on.
   */
unsigned int debugMaxI2CWaitTime;
unsigned int debugMaxI2CStopTime;
unsigned int debugErrorCount;
unsigned int debugMaxI2CReadWait;

/*
** Setup the TWI port (I2C) for the following:
**    100 KHz
**    SDA and SCL GPIO pins
**    MAG_SW (next board EN_MOTOR) needs to be set high when using the I2C bus for touch, motor and magnet chip
*/
void
I2C_Setup(void)
{
#if 1
   NRF_GPIO->PIN_CNF[MAG_SW] =
            (GPIO_PIN_CNF_DIR_Output     << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Disabled  << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

      /*
      ** Currently MAG_SW needs to be low to enable the motor and to enable the I2C. It appears if MAG_SW is high
      ** that the motor chip is now trying to power itself through the SDA and SCL connector.
      */
   NRF_GPIO->OUTCLR = (1UL << MAG_SW);
#endif

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
   NRF_GPIO->PIN_CNF[I2C_SCL] =
            (GPIO_PIN_CNF_DIR_Input      << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Pullup    << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

   NRF_GPIO->PIN_CNF[I2C_SDA] =
            (GPIO_PIN_CNF_DIR_Input      << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Pullup    << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

   NRF_TWI0->EVENTS_RXDREADY = 0;
   NRF_TWI0->EVENTS_TXDSENT = 0;
   NRF_TWI0->PSELSCL = I2C_SCL;
   NRF_TWI0->PSELSDA = I2C_SDA;
   NRF_TWI0->FREQUENCY = TWI_FREQUENCY_FREQUENCY_K100 << TWI_FREQUENCY_FREQUENCY_Pos;

      /* This channel is used during reads, source of action is always BB */
   NRF_PPI->CH[0].EEP = (uint32_t)&NRF_TWI0->EVENTS_BB;

   NRF_TWI0->ENABLE = TWI_ENABLE_ENABLE_Enabled << TWI_ENABLE_ENABLE_Pos;

     /* In case a slave may be stuck, try to flush out bus */
   I2C_Abort();
}

/*
** So the Nordic sample code had this bus clear logic (no explanation why). I searched on the internet and I found a
** few people talking about the same thing. Basically there are conditions that may happen where a slave will think
** the master is talking to it and get stuck driving SDA. Could happen because of glitches on the line, maybe master
** reboots, who knows. Don't even know if it will ever happen on this product, but after poking around a little bit
** it seems like something good to have.
**
** The next question is what do you do? Comments I read on the internet were just set SDA high and clock out 9 bits
** and then clock out the STOP bit. Sounds reasonable. Nordic though opted to clock out as many as 18 bits. I assume
** thinking a read had just started? And if the SDA line goes high, they would quit right away. They didn't send
** a STOP bit. I'm thinking send 9 and the stop bit is a little more correct. Go ahead and send it twice though in
** case the slave is stuck still on the address byte. Seems pretty hard for the slave to be stuck that good, but
** we will only do this when we have a problem, so sending 2 seems cheap.
**
** I wanted to use the TWI interface but I can't quite figure out a clean way to do that. If you use the TWI
** interface then the first write will get stuck on waiting for a slave to ACK. But if the slave is looking for
** the master to ACK, then not going to budge. You could abort the TWI at this point, but I think SCL will then
** go high which is not a valid STOP condition since SDA would already be high. So bit bang it is I guess.
**
** So the reasons to call this right now are the following:
**    #1). Always call as part of initialization. Simple routine to flush out the bus
**    #2). If we go to read or write the I2C and SDA is low, this is an indication that something is wrong so
**         try to free the bus.
**    #3). Check for errors when talking to devices. If we hit an error, call this for each error to see if we
**         can get out of the error condition
**
** The protocol for the I2C is the following:
**    #1). Start with SDA & SCL high
**    #2). Lower SCL, delay 5 usec
**    #3). Set SDA, although we are going to clock out 1's on SDA so nothing to set
**    #4). Set SCL, delay 5 usec
**    #5). Repeat steps 2-4 total of 9 times
**    #6). Send STOP. In Motor drive PDF found reference to driving SDA low and high while SCL is high
**         is a valid STOP in the case where the slave did not acknowledge. Sounds good to me.
*/
void
I2C_Abort (void)
{
   unsigned int i;

      /* Disable TWI interface so we can bit bang */
   NRF_TWI0->ENABLE = TWI_ENABLE_ENABLE_Disabled << TWI_ENABLE_ENABLE_Pos;

   NRF_GPIO->OUTSET = (1UL << I2C_SDA);
   NRF_GPIO->OUTSET = (1UL << I2C_SCL);

      /* Set SCL and SDA to outputs, we are the only user so nothing to save */
   NRF_GPIO->PIN_CNF[I2C_SCL] =
            (GPIO_PIN_CNF_DIR_Output     << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Pullup    << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

   NRF_GPIO->PIN_CNF[I2C_SDA] =
            (GPIO_PIN_CNF_DIR_Output     << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Pullup    << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

   UTIL_DelayUsec(I2C_DELAY);

      /* Clock out normal 8 bits and a bit for ACK position */
   for (i = 0; i < 9; i++)
      {
      NRF_GPIO->OUTCLR = (1UL << I2C_SCL);
      UTIL_DelayUsec(I2C_DELAY);
      NRF_GPIO->OUTSET = (1UL << I2C_SCL);
      UTIL_DelayUsec(I2C_DELAY);
      }

      /* Now force a stop */
   NRF_GPIO->OUTCLR = (1UL << I2C_SDA);
   UTIL_DelayUsec(I2C_DELAY);
   NRF_GPIO->OUTSET = (1UL << I2C_SDA);
   UTIL_DelayUsec(I2C_DELAY);

   NRF_GPIO->PIN_CNF[I2C_SCL] =
            (GPIO_PIN_CNF_DIR_Input      << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Pullup    << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

   NRF_GPIO->PIN_CNF[I2C_SDA] =
            (GPIO_PIN_CNF_DIR_Input      << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Pullup    << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

   NRF_TWI0->EVENTS_ERROR = 0;
   NRF_TWI0->ENABLE = TWI_ENABLE_ENABLE_Enabled << TWI_ENABLE_ENABLE_Pos;
}

/*
** Function to write bytes out the I2C port. Not sure if a return value is really needed
** or not. Thinking is if we have an error, then this routine will call I2C_Abort. But I
** suppose the caller may want to know and may want to return.
**
** Returns 0 if there is no errors
** Returns non-zero if there is a problem writing
*/
uint8_t
I2C_Write (uint8_t addr, const uint8_t *data, uint8_t len, uint8_t stop)
{
   unsigned int delayLimit;

      /* If no data then return an error */
   if (!len)
      return (I2C_ERROR_USAGE);

   if (addr == I2C_ADDR_ACCEL)
      {
      return (ACC_Write (addr, data, len, stop));
      }

   NRF_TWI0->EVENTS_TXDSENT = 0;
   NRF_TWI0->ADDRESS = addr;
   NRF_TWI0->TXD = *data++;
   NRF_TWI0->TASKS_STARTTX = 1;

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
      while ((NRF_TWI0->EVENTS_TXDSENT == 0) && (delayLimit < I2C_TIMEOUT))
         {
         UTIL_DelayUsec(I2C_DELAY);
         delayLimit++;
         }

      if (delayLimit > debugMaxI2CWaitTime)
         debugMaxI2CWaitTime = delayLimit;

         /*
         ** Did the TWI device bail out with an error. Is that why we timed out?
         */
      if (NRF_TWI0->EVENTS_ERROR)
         {
         debugErrorCount++;
         I2C_Abort();
         return (I2C_ERROR_TWI);
         }

         /*
         ** If we timeout, call the abort routine to reset the port
         */
      if (delayLimit >= I2C_TIMEOUT)
         {
         I2C_Abort();
         return (I2C_ERROR_TIMEOUT);
         }

      NRF_TWI0->EVENTS_TXDSENT = 0;

      if (len)
         NRF_TWI0->TXD = *data++;
      }

      /*
      ** If the user actually wants to do a read they will call us with a write first and in that case
      ** they won't want us to send the STOP. So this parameter provides that option.
      */
   if (stop)
      {
      NRF_TWI0->EVENTS_STOPPED = 0;
      NRF_TWI0->TASKS_STOP = 1;

         /* Wait until stop sequence is sent */
      delayLimit = 0;
      while ((NRF_TWI0->EVENTS_STOPPED == 0) && (delayLimit < I2C_TIMEOUT))
         {
         UTIL_DelayUsec(I2C_DELAY);
         delayLimit++;
         }

      if (delayLimit > debugMaxI2CStopTime)
         debugMaxI2CStopTime = delayLimit;

      if (NRF_TWI0->EVENTS_ERROR)
         {
         debugErrorCount++;
         I2C_Abort();
         return (I2C_ERROR_TWI);
         }

      if (delayLimit >= I2C_TIMEOUT)
         {
         I2C_Abort();
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
I2C_Read (uint8_t addr, uint8_t *data, uint8_t len)
{
   unsigned int delayLimit;

      /* If no data then return an error */
   if (!len)
      return (I2C_ERROR_USAGE);

   if (addr == I2C_ADDR_ACCEL)
      {
      return (ACC_Read (addr, data, len));
      }

   NRF_PPI->CHENCLR = PPI_CHENCLR_CH0_Msk;
   NRF_TWI0->EVENTS_RXDREADY = 0;
   NRF_TWI0->ADDRESS = addr;

   if (len == 1)
      NRF_PPI->CH[0].TEP = (uint32_t)&NRF_TWI0->TASKS_STOP;
   else
      NRF_PPI->CH[0].TEP = (uint32_t)&NRF_TWI0->TASKS_SUSPEND;

   NRF_TWI0->EVENTS_STOPPED = 0;
   NRF_PPI->CHENSET = PPI_CHENSET_CH0_Msk;
   NRF_TWI0->TASKS_STARTRX = 1;

   while (len)
      {
         /*
         ** Setup 2 exit conditions from this loop. A timeout or the most likely reason, we received a byte.
         ** Note an error from the TWI module could also happen but that will lead to a timeout. So just
         ** check for a timeout and catch the error afterwards.
         */
      delayLimit = 0;
      while ((NRF_TWI0->EVENTS_RXDREADY == 0) && (delayLimit < I2C_TIMEOUT))
         {
         UTIL_DelayUsec(I2C_DELAY);
         delayLimit++;
         }

      if (delayLimit > debugMaxI2CReadWait)
         debugMaxI2CReadWait = delayLimit;

      NRF_TWI0->EVENTS_RXDREADY = 0;

         /*
         ** Search for these errors seperately so we can track them for now.
         */
      if (NRF_TWI0->EVENTS_ERROR)
         {
         debugErrorCount++;
         I2C_Abort();
         return (I2C_ERROR_TWI);
         }

         /*
         ** If we timeout, call the abort routine to reset the port
         */
      if (delayLimit >= I2C_TIMEOUT)
         {
         I2C_Abort();
         return (I2C_ERROR_TIMEOUT);
         }

      *data++ = NRF_TWI0->RXD;

      len--;

      if (len == 1)
         NRF_PPI->CH[0].TEP = (uint32_t)&NRF_TWI0->TASKS_STOP;

      if (len)
         {
            /* See twi_hw_master.c and the app note about CPU problem */
         UTIL_DelayUsec(20);
         NRF_TWI0->TASKS_RESUME = 1;
         }
      }

      /* Wait until stop sequence is done */
   delayLimit = 0;
   while ((NRF_TWI0->EVENTS_STOPPED == 0) && (delayLimit < I2C_TIMEOUT))
      {
      UTIL_DelayUsec(I2C_DELAY);
      delayLimit++;
      }

   if (delayLimit > debugMaxI2CStopTime)
      debugMaxI2CStopTime = delayLimit;

   if (NRF_TWI0->EVENTS_ERROR)
      {
      debugErrorCount++;
      I2C_Abort();
      return (I2C_ERROR_TWI);
      }

   if (delayLimit >= I2C_TIMEOUT)
      {
      I2C_Abort();
      return (I2C_ERROR_TIMEOUT);
      }

   NRF_TWI0->EVENTS_STOPPED = 0;
   NRF_PPI->CHENCLR = PPI_CHENCLR_CH0_Msk;

   return (I2C_ERROR_NONE);
}

/*
** This function does a complete read. Meaning it will first write 1 byte which is the address
** and then it will send the read command to fetch the actual data.
**
** The data pointer should be big enough for all the data being read plus the 1 byte register.
** The first byte of the buffer should be the register address. The data will be placed starting
** in the second byte.
**
** Returns 0 if there are no errors. A non-zero indicates an error and could come from either the
** read or write function.
*/
uint8_t
I2C_FullRead (uint8_t addr, uint8_t *data, uint8_t len)
{
   uint8_t status;

   status = I2C_Write (addr, data, 1, FALSE);
   if (status == 0)
      status = I2C_Read(addr, &data[1], len);

   return (status);
}

