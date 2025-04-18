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
** File Name:  mfginterface.c
**
** Purpose:    Manufacturing interface and diagnostic mode interface
*/

#include "master.h"
#include "stdio.h"
#include "string.h"
#include "hardware.h"
#include "i2c.h"
#include "uart.h"
#include "utils.h"
#include "timers.h"

#define  UPPER_CASE_MASK         (~(0x20))
#define  ESCAPE_CHAR             (0x1b)
#define  BACKSPACE               ('\b')
#define  CARRIAGE_RETURN         ('\r')
#define  PROMPT_TIMEOUT          (4)

   /* For now allocate a large buffer. Depending on RAM usage we may need to do something different in the future */
uint8_t mfgBuffer[MFG_MAX_READ_SIZE + 1];

uint8_t
makeUpperCase (uint8_t c)
{
   if ((c >= 'a') && (c <= 'z'))
      c &= UPPER_CASE_MASK;

   return (c);
}

unsigned int
validHexChar (uint8_t c)
{
   if (   ((c >= '0') && (c <= '9'))
       || ((c >= 'A') && (c <= 'F')))
      return (TRUE);
   else
      return (FALSE);
}

uint8_t
asciitohex (uint8_t c)
{
   return ((c > '9') ? c - ('A' - 10) : c - '0');
}

void
dumpRegData (uint8_t regAddr, uint8_t *buffer, unsigned int len)
{
   unsigned int i, j;
   unsigned int burn;

   burn = regAddr & 0x0F;
   regAddr &= 0xF0;

   printf("      0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F");
   printf("\n   +-------------------------------------------------+");
   for (i = 0; i < len;)
      {
      printf("\n%02X | ", regAddr);
      for (j = 0; j < 16; j++)
         {
         if (((i + j - burn) < len) && (j >= burn))
            {
            printf ("%02X ", buffer[j - burn]);
            }
         else
            {
            printf ("-- ");
            }
         }
      printf ("| ");

      buffer += (16 - burn);
      i += (16 - burn);
      burn = 0;
      regAddr += 16;
      }
   printf("\n   +-------------------------------------------------+\n");
}


/*
** Routine that will ask user for a hex byte
**
** Returns 0 if there is a data byte, returns non-zero if their is an abort
*/
uint8_t
getHexByte (unsigned char *str, uint8_t *dataByte)
{
   int     i, c;
   char    buf[12];

   printf ("%s: %02X", str, *dataByte);
   sprintf (buf, "%02X", *dataByte);
   i = 2;

      /*
      ** If first thing user gives us is a valid character then clear the default value
      ** and start with what the user gives us. We need to wait until user gives us something
      ** valid first though.
      */
   while (TRUE)
      {
         /* Only way out is you have to give us characters! */
      c = makeUpperCase(getchar());

      if (validHexChar(c))
         {
         printf ("\b\b  \b\b");
         i = 0;
         break;
         }

      else if (c == ESCAPE_CHAR)
         {
         printf ("\n");
         return (1);
         }

      else if ((c == BACKSPACE) || (c == CARRIAGE_RETURN))
         break;
      }

   while (TRUE)
      {
      if (c == CARRIAGE_RETURN)
         break;

      else if (c == ESCAPE_CHAR)
         {
         printf ("\n");
         return (1);
         }

      else if ((c == BACKSPACE) && i)
         {
         printf("\b \b");
         buf[--i] = 0;
         }

      else if ((i < 2) && validHexChar(c))
         {
         buf[i++] = c;
         printf("%c", c);
         }

         /* Only way out is you have to give us characters! */
      c = makeUpperCase(getchar());
      }

   printf ("\n");

   if (i == 0)
      *dataByte = 0;

   else
      {
      *dataByte = asciitohex (buf[0]);
      if (i == 2)
         *dataByte = (*dataByte * 16) + asciitohex (buf[1]);
      }

   return (0);
}

/*
** Generic function that reads a block of data from an I2C device. Caller passes the I2C
** device address, a buffer to read into and the number of bytes to read.
**
** The first byte of the buffer should be the register address to read from. The buffer
** must be big enough to hold the number of bytes requested, including the register
** address byte.
*/
void
MFG_I2C_ReadBuffer (uint8_t I2Caddr, uint8_t *buffer, unsigned int len)
{
   I2C_Write (I2Caddr, buffer, 1, FALSE);
   I2C_Read (I2Caddr, &buffer[1], len);
}

/*
** Generic function that will prompt the user for an address and will read one byte
** from the specified I2C device.
**
** Caller provides the I2C device address and a buffer that is at least 2 bytes long.
** The first byte of the buffer is the default address that the user is prompted with.
*/
void
MFG_I2C_Read (uint8_t I2Caddr, uint8_t *buffer)
{
   if (getHexByte ("Enter register address (ESC aborts)", &buffer[0]) == 0)
      {
      I2C_FullRead (I2Caddr, buffer, 1);
      printf ("Register %02X: %02X\n", buffer[0], buffer[1]);
      }
}

/*
** Generic function that will prompt the user for an address and value to write to
** any I2C device. Caller passes the I2C device address and a buffer to use.
**
** This routine will on purpose use the values in the buffer as defaults. That way
** if the user calls a write routine over and over the default value will be what they
** used last time.
*/
void
MFG_I2C_Write (uint8_t I2Caddr, uint8_t *buffer)
{
   if (getHexByte ("Enter register address (ESC aborts)", &buffer[0]) == 0)
      {
      I2C_Write (I2Caddr, buffer, 1, FALSE);
      I2C_Read (I2Caddr, &buffer[1], 1);

      if (getHexByte ("Enter value to write (ESC aborts)", &buffer[1]) == 0)
         {
         I2C_Write (I2Caddr, buffer, 2, TRUE);
         }
      }
}

/*
** Test menu for the Accelerometer
*/
void
MFG_Accel (void)
{
   int c;

      /* Start buffer out clear so default values are meaningful */
   memset (mfgBuffer, 0, sizeof(mfgBuffer));
   while (TRUE)
      {
      if (UART0_RxWaiting())
         {
         c = makeUpperCase(getchar());
         printf("%c\n", c);

         switch (c)
            {
            case 'D':
               mfgBuffer[0] = 0;
               MFG_I2C_ReadBuffer (I2C_ADDR_ACCEL, mfgBuffer, 64);
               dumpRegData (0, &mfgBuffer[1], 64);
               break;

            case 'R':
               MFG_I2C_Read (I2C_ADDR_ACCEL, mfgBuffer);
               break;

            case 'W':
               MFG_I2C_Write (I2C_ADDR_ACCEL, mfgBuffer);
               break;

            case 'X':
               printf(">");
               return;

            default:
               printf (
                  "Command Help\n"
                  "  D - Dump Motor Registers\n"
                  "  R - Read Register\n"
                  "  W - Write Register\n"
                  "  X - Return to main menu\n"
                  "  ? - Displays this message\n");
               break;
            }

         printf(">");
         }
      }
}


/*
** Test menu for the Magnetometer
*/
void
MFG_Magnet (void)
{
   int c;

      /* Start buffer out clear so default values are meaningful */
   memset (mfgBuffer, 0, sizeof(mfgBuffer));
   while (TRUE)
      {
      if (UART0_RxWaiting())
         {
         c = makeUpperCase(getchar());
         printf("%c\n", c);

         switch (c)
            {
            case 'D':
               mfgBuffer[0] = 0x20 | 0x80;
               MFG_I2C_ReadBuffer (I2C_ADDR_MAGNET, mfgBuffer, 14);
               dumpRegData (0x20, &mfgBuffer[1], 14);

               mfgBuffer[0] = 0x2E | 0x80;
               MFG_I2C_ReadBuffer (I2C_ADDR_MAGNET, mfgBuffer, 6);
               dumpRegData (0x2E, &mfgBuffer[1], 6);
               break;

            case 'R':
               MFG_I2C_Read (I2C_ADDR_MAGNET, mfgBuffer);
               break;

            case 'W':
               MFG_I2C_Write (I2C_ADDR_MAGNET, mfgBuffer);
               break;

            case 'X':
               printf(">");
               return;

            default:
               printf (
                  "Command Help\n"
                  "  D - Dump Motor Registers\n"
                  "  R - Read Register\n"
                  "  W - Write Register\n"
                  "  X - Return to main menu\n"
                  "  ? - Displays this message\n");
               break;
            }

         printf(">");
         }
      }
}


/*
** Test menu for the Motor
**
** Next test for motor is add code to sense ENC1 and ENC2
** Next after that is can those be interrupts
** Next, can that interrupt feed a timer?
**
** For now, assume lock is open when we start any tests.
*/
void
MFG_Motor (void)
{
   int c;
   uint8_t lockState = 0;

      /* Start buffer out clear so default values are meaningful */
   memset (mfgBuffer, 0, sizeof(mfgBuffer));
   while (TRUE)
      {
      if (UART0_RxWaiting())
         {
         c = makeUpperCase(getchar());
         printf("%c\n", c);

         switch (c)
            {
         #if 0
            case 'A':
               {
               uint16_t r1, r2;

               Motor_Lock (TRUE, 200);
               r1 = Motor_Lock (FALSE, 200);
               r2 = Motor_Lock (TRUE, 200);
               lockMotorSteps = ((r1 + r2) / 2) - 5;

               printf("Got lock of %u %u %u\n", r1, r2, lockMotorSteps);

               break;
               }
         #endif

            case 'C':
               {
               uint32_t latch_enc;
               uint16_t t1, t2, t3;

               Motor_Start (FALSE);

                  /* First wait for motor to start moving */
               MOTOR_Enc01Events = 0;
               while (MOTOR_Enc01Events <= 10);

               latch_enc = MOTOR_Enc01Events;
               while (MOTOR_Enc01Events == latch_enc);

               t1 = Timer2_Count();
               latch_enc = MOTOR_Enc02Events;
               while (MOTOR_Enc02Events == latch_enc);

               t2 = Timer2_Count();
               latch_enc = MOTOR_Enc01Events;
               while (MOTOR_Enc01Events == latch_enc);

               t3 = Timer2_Count();

               Motor_Stop ();

               printf("t1->t2 is %u  and t2->t3 is %u\n", t2 - t1, t3 - t2);
               printf("t1 t2 t3 is %u %u %u\n", t1, t2, t3);
               break;
               }

            case 'D':
               mfgBuffer[0] = 0;
               MFG_I2C_ReadBuffer (I2C_ADDR_MOTOR, mfgBuffer, 2);
               dumpRegData (0, &mfgBuffer[1], 2);
               break;

            case 'K':
               {
               uint8_t latch_irq;

                  /* Force the code to read the status and clear the counter at least once */
               latch_irq = TS_IRQCounter - 1;
               while (TRUE)
                  {
                  if (latch_irq != TS_IRQCounter)
                     {
                     latch_irq = TS_IRQCounter;

                     if (TS_TouchLED (TRUE))
                        {
                        lockState ^= 0x01;
                        Timer2_Start();
                        Motor_Lock (lockState == 1, MOTOR_LOCK_COUNT);
                        Timer2_Stop();
                        }
                     }

                  if (UART0_RxWaiting())
                     {
                     c = getchar();
                     if (c == ESCAPE_CHAR)
                        {
                        break;
                        }
                     }
                  }
               break;
               }

            case 'L':
               {
               printf ("Use L to toggle lock position, ESC to exit\n");
               while (TRUE)
                  {
                  if (UART0_RxWaiting())
                     {
                     c = makeUpperCase(getchar());

                     if (c == ESCAPE_CHAR)
                        break;

                     else if (c == 'L')
                        {
                        lockState ^= 0x01;
                        Motor_Lock (lockState == 1, MOTOR_LOCK_COUNT);
                        }
                     }
                  }
               break;
               }

            case 'P':
               printf ("Use = or - to move motor, ESC to exit\n");
               while (TRUE)
                  {
                  if (UART0_RxWaiting())
                     {
                     c = getchar();

                     if (c == ESCAPE_CHAR)
                        break;

                     else if ((c == '=') || (c == '-'))
                        {
                        Motor_Lock (c == '=', 10);
                        }
                     }
                  }
               break;

            case 'R':
               MFG_I2C_Read (I2C_ADDR_MOTOR, mfgBuffer);
               break;

            case 'W':
               MFG_I2C_Write (I2C_ADDR_MOTOR, mfgBuffer);
               break;

            case 'X':
               printf(">");
               return;

            default:
               printf (
                  "Command Help\n"
                  "  C - Calibrate Direction\n"
                  "  D - Dump Motor Registers\n"
                  "  K - Keyboard Lock Test\n"
                  "  L - Lock/Unlock Motor\n"
                  "  P - Position Motor\n"
                  "  R - Read Register\n"
                  "  W - Write Register\n"
                  "  X - Return to main menu\n"
                  "  ? - Displays this message\n");
               break;
            }

         printf(">");
         }
      }
}


/*
** Test menu for the touch sensor
*/
void
MFG_Touch (void)
{
   int c;

      /* Start buffer out clear so default values are meaningful */
   memset (mfgBuffer, 0, sizeof(mfgBuffer));
   while (TRUE)
      {
      if (UART0_RxWaiting())
         {
         c = makeUpperCase(getchar());
         printf("%c\n", c);

         switch (c)
            {
            case 'A':
               TS_ToggleLED(0xFC);
               break;

            case 'C':
               mfgBuffer[0] = TS_GPIO_DATA;
               I2C_Write (I2C_ADDR_TOUCH, mfgBuffer, 1, FALSE);
               I2C_Read (I2C_ADDR_TOUCH, &mfgBuffer[1], 2);

               if (getHexByte("Enter LED control byte (FC is all LED's ON)", &mfgBuffer[1]) == 0)
                  {
                  I2C_Write (I2C_ADDR_TOUCH, mfgBuffer, 2, TRUE);
                  }
               break;

            case 'D':
               mfgBuffer[0] = TS_TOUCH_STATUS;
               MFG_I2C_ReadBuffer (I2C_ADDR_TOUCH, mfgBuffer, 128);
               dumpRegData (TS_TOUCH_STATUS, &mfgBuffer[1], 128);
               break;

            case 'L':
               {
               unsigned int latch;

               printf("Hit ESC to exit test\n");
               latch = runtimeSeconds;
               while (TRUE)
                  {
                  if (UART0_RxWaiting())
                     {
                     c = getchar();
                     if (c == ESCAPE_CHAR)
                        {
                        break;
                        }
                     }

                  if (latch != runtimeSeconds)
                     {
                     latch = runtimeSeconds;
                     TS_ToggleTestLED ();
                     }
                  }
               break;
               }

            case 'R':
               MFG_I2C_Read (I2C_ADDR_TOUCH, mfgBuffer);
               break;

            case 'T':
               {
               uint8_t latch_irq;

                  /* Force the code to read the status and clear the counter at least once */
               latch_irq = TS_IRQCounter - 1;
               while (TRUE)
                  {
                  if (latch_irq != TS_IRQCounter)
                     {
                     latch_irq = TS_IRQCounter;

                     TS_TouchLED (FALSE);
                     }

                  if (UART0_RxWaiting())
                     {
                     c = getchar();
                     if (c == ESCAPE_CHAR)
                        {
                        break;
                        }
                     }
                  }
               break;
               }

            case 'W':
               MFG_I2C_Write (I2C_ADDR_TOUCH, mfgBuffer);
               break;

            case 'X':
               printf(">");
               return;

            default:
               printf (
                  "Command Help\n"
                  "  C - Control LED State\n"
                  "  D - Dump Touch Sensor Registers\n"
                  "  L - LED Spin Test\n"
                  "  R - Read Register\n"
                  "  T - Touch Test\n"
                  "  W - Write Register\n"
                  "  X - Return to main menu\n"
                  "  ? - Displays this message\n");
               break;
            }

         printf(">");
         }
      }
}


/*
** MFG Mode
**
** Not quite sure what to do here yet. I can think of a couple options though:
**
** Option #1: Keep this mode always available and active in the radio. In theory the device is locked
**    up and this is not reachable. As a general rule of thumb though I think this is probably not a
**    good practice. If this option is pursued then this routine has to be setup to be constantly called
**    because it can't be allowed to hold on to the only task the device has.
**
** Option #2: Make this a complete mode. Meaning if the device is configured or told to go into this mode
**    then it stays in this mode and now controls all features. This is best for development, testing, and
**    configuration because there is nothing else to compete against. The only downside is we need to
**    come up with a way to determine if the mode should run or not.
**
**    Probably want some NV flag that defaults to this mode active. Then during manufacturing this flag
**    gets cleared. Downside is if something goes wrong, unit is now dead.
**
**    A better option maybe would be by default the mode is off and even for manufacturing they need to
**    find a way to enable this mode.
**
**    TODO: Figure out how to configure and operate this mode.
*/
void
MFG_Mode (void)
{
   int c;

      /* Always put out the manufacturing mode banner */
   printf("\nSkylock Version %s %s\n", PartNumber, PART_DATECODE);

      /* Give user 10 seconds to enter in the correct access code - one chance */
   while ((runtimeSeconds < PROMPT_TIMEOUT) && (UART0_RxWaiting() < 2));

   if ((runtimeSeconds >= PROMPT_TIMEOUT) || (getchar() != 'm') || (getchar() != 'f'))
      return;

      /* Let timer run while we are in Diagnostic mode */
   Timer2_Start();

   printf("Manufacturing Interface Active\n>");

   /*
   ** When we get a watchdog setup, then ask user how long they want to live in this mode before watchdog
   ** will kill them.
   */

   while (TRUE)
      {
      if (UART0_RxWaiting())
         {
         c = makeUpperCase(getchar());
         printf("%c\n", c);

         switch (c)
            {
            case 'A':
               printf(">");
               MFG_Accel();
               break;

            case 'D':
               printf("FICR\n");
               dumpRegData (0, (unsigned char *) ADDR_FICR, 256);

               printf("\nUICR\n");
               dumpRegData (0, (unsigned char *) ADDR_UICR, 256);
               printf(">");
               break;

            case 'M':
               printf(">");
               MFG_Motor();
               break;

            case 'R':
               printf("Hey, need a reboot command here\n>");
               break;

            case 'S':
               printf(">");
               MFG_Magnet();
               break;

            case 'T':
               printf(">");
               MFG_Touch();
               break;

            default:
               printf (
                  "Command Help\n"
                  "  A - Accelerometer\n"
                  "  D - Dump FICR and UICR NV Memory\n"
                  "  M - Motor\n"
                  "  R - Reboot\n"
                  "  S - Magnetic Sensor\n"
                  "  T - Touch Sensor\n"
                  "  ? - Displays this message\n>");
            }
         }
      }
}
