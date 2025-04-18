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
** File Name:  debuguart.c
**
** Purpose:    Routines to manage a debug and manufacturing UART
*/

#include "master.h"
#include "stdio.h"
#include "hardware.h"
#include "uart.h"

/*
** UART buffers and variables
**
** Note we will only use the UART for debugging and manufacturing. So we don't need to commit that
** many resources to this interface. If it turns out we need more RAM for other things, then these
** buffers should get reduced. We will start the transmit at 256 bytes so that hopefully transmit
** doesn't hold things up too much. Since we only have to receive simple commands and data there isn't
** any reason for the receive buffer to be very big.
**
** Note the receiver has a 6-byte FIFO but I'm not sure we really care for this application
**
** Keep the buffers as a power of 2, that makes managing the pointers easier (just increment and mask)
*/
#define  UART_TX_BUFFER_SIZE           (256)
#define  UART_RX_BUFFER_SIZE           (64)

unsigned char  UARTTxBuffer[UART_TX_BUFFER_SIZE];
unsigned char  UARTRxBuffer[UART_RX_BUFFER_SIZE];
uint8_t  UARTTxHeadIndex;
uint8_t  UARTTxTailIndex;
uint8_t  UARTRxHeadIndex;
uint8_t  UARTRxTailIndex;
uint8_t  UARTTxEnabled;

/*
*/
void
UART_Setup (void)
{
#if 0
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

   NRF_GPIO->PIN_CNF[UART_RX] =
            (GPIO_PIN_CNF_DIR_Input      << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Pullup    << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

   NRF_GPIO->PIN_CNF[UART_TX] =
            (GPIO_PIN_CNF_DIR_Output     << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Disabled  << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0S1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

   NRF_GPIO->OUTSET = (1UL << UART_TX);
   NRF_UART0->PSELTXD = UART_TX;
   NRF_UART0->PSELRXD = UART_RX;
   NRF_UART0->BAUDRATE      = (UART_BAUDRATE_BAUDRATE_Baud115200 << UART_BAUDRATE_BAUDRATE_Pos);
   NRF_UART0->ENABLE        = (UART_ENABLE_ENABLE_Enabled << UART_ENABLE_ENABLE_Pos);
   NRF_UART0->TASKS_STARTTX = 1;
   NRF_UART0->TASKS_STARTRX = 1;

      // Enable UART interrupt
   NRF_UART0->INTENCLR = 0xffffffffUL;
   NRF_UART0->INTENSET =   (UART_INTENSET_RXDRDY_Set << UART_INTENSET_RXDRDY_Pos)
                         | (UART_INTENSET_TXDRDY_Set << UART_INTENSET_TXDRDY_Pos)
                         | (UART_INTENSET_ERROR_Set << UART_INTENSET_ERROR_Pos);

   NVIC_ClearPendingIRQ (UART0_IRQn);
   NVIC_SetPriority(UART0_IRQn, SKYLOCK_PRIORITY);
   NVIC_EnableIRQ (UART0_IRQn);
}

int
putchar (int c)
{
   if (c == '\n')
      putchar ('\r');

      /*
      ** If tail catches up to head then wait. I don't believe we have any reason
      ** to run this product with interrupts disabled so waiting forever should be
      ** fine. Also we will have a watchdog setup so that will take care of any
      ** stuck conditions.
      */
   while (((UARTTxHeadIndex + 1) & (UART_TX_BUFFER_SIZE - 1)) == UARTTxTailIndex);

   disableInterrupt();
   UARTTxBuffer[UARTTxHeadIndex++] = c;
   if (!UARTTxEnabled)
      {
      UARTTxEnabled = TRUE;
      NRF_UART0->TXD = UARTTxBuffer[UARTTxTailIndex++];
      UARTTxTailIndex &= (UART_TX_BUFFER_SIZE - 1);
      }
   enableInterrupt();

   return(c);
}

/*
** __write
**
** This is called by printf when built with IAR. If built with GCC there likley
** will be a different function call I'm guessing. Once we know that, just setup
** a conditional compile to flip between the options.
*/
size_t __write(int handle, const unsigned char * buffer, size_t size)
{
   size_t nChars = 0;

   if (buffer == 0)
      {
         /*
         ** Don't believe this will happen, but should always watchout for a NULL pointer
         */
      return 0;
      }

   for (nChars = 0; nChars < size; ++nChars)
      {
      putchar (*buffer++);
      }

  return (nChars);
}

void
UART0_Handler (void)
{
   if (NRF_UART0->EVENTS_RXDRDY)
      {
         /* This will actually clear the RX interrupt */
      NRF_UART0->EVENTS_RXDRDY = 0;

         /*
         ** This is a really simple device, just grab the byte and stuff it in the buffer. If for some reason
         ** we haven't emptied the buffer, oh well. Based on what this device should be doing though, that should
         ** never even be an option to happen.
         */
      UARTRxBuffer[UARTRxHeadIndex++] = NRF_UART0->RXD;
      UARTRxHeadIndex &= (UART_RX_BUFFER_SIZE - 1);
      }

      /* Check for transmit activity */
   if (NRF_UART0->EVENTS_TXDRDY)
      {
         /* This will actually clear the TX interrupt */
      NRF_UART0->EVENTS_TXDRDY = 0;

      if (UARTTxTailIndex != UARTTxHeadIndex)
         {
         NRF_UART0->TXD = UARTTxBuffer[UARTTxTailIndex++];
         UARTTxTailIndex &= (UART_TX_BUFFER_SIZE - 1);
         }
      else
         {
            /* If no more bytes, disable TX for now */
         UARTTxEnabled = FALSE;
         }
      }

      /*
      ** Check for receive errors. Again we are a simple device so right now we really don't care about
      ** any of these. Just clear an error if it happens.
      */
   if (NRF_UART0->EVENTS_ERROR)
      {
         /* Need to clear the event to clear the interrupt */
      NRF_UART0->EVENTS_ERROR = 0;

         /*
         ** For errors you also have to clear the error. Since we don't care right now, just
         ** clear all the error bits
         */
      NRF_UART0->ERRORSRC = 0x0F;
      }
}

int
getchar (void)
{
   int c;

      /* If no character then just spin and wait */
   while (UARTRxTailIndex == UARTRxHeadIndex);

   c = UARTRxBuffer[UARTRxTailIndex++];
   UARTRxTailIndex &= (UART_RX_BUFFER_SIZE - 1);

   return (c);
}

/*
** Return the number of characters sitting in the buffer.
*/
int
UART0_RxWaiting (void)
{
   unsigned int count;

   count = UARTRxHeadIndex - UARTRxTailIndex;
   if (count > UART_RX_BUFFER_SIZE)
      count += UART_RX_BUFFER_SIZE;

   return (count);
}

/*
** Sit and spin until the transmit buffer is empty. At the moment interrupts should
** not be disabled, but that being said we may crash inside a handler. So in that case
** they would be disabled. So need to look at the interrupt state to see if we can
** just spin and watch the pointers or if we need to feed the UART ourselves.
**
** It should be safe to spin here because there is always a hardware watchdog that will
** kill us if we get stuck.
*/
void
UART0_TxFlush (void)
{
   if (!ASM_GetIPSR())
      {
      while (UARTTxTailIndex != UARTTxHeadIndex);
      }
   else
      {
      if (!UARTTxEnabled)
         {
         UARTTxEnabled = TRUE;
         NRF_UART0->TXD = UARTTxBuffer[UARTTxTailIndex++];
         UARTTxTailIndex &= (UART_TX_BUFFER_SIZE - 1);
         }

      while (UARTTxTailIndex != UARTTxHeadIndex);
         {
         if (NRF_UART0->EVENTS_TXDRDY)
            {
            NRF_UART0->EVENTS_TXDRDY = 0;
            NRF_UART0->TXD = UARTTxBuffer[UARTTxTailIndex++];
            UARTTxTailIndex &= (UART_TX_BUFFER_SIZE - 1);
            }
         }
      }
}
