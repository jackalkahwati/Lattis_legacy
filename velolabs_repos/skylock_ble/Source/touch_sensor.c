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
** File Name:  touch_sensor.c
**
** Purpose:    Routines that support the capacitive touch interface and control the LED's
*/

#include "master.h"
#include "stdio.h"
#include "string.h"
#include "hardware.h"
#include "i2c.h"

volatile uint8_t  TS_IRQCounter;

#if 0
   const unsigned char configMHD[9] = { TS_MHD_RISING, 0x01, 0x01, 0x00, 0x00, 0x01, 0x01, 0xFF, 0x02 };
   const unsigned char configELE0[13] = { TS_ELE0_TOUCH, 0x0F, 0x0A, 0x0F, 0x0A, 0x0F, 0x0A, 0x0F, 0x0A, 0x0F, 0x0A, 0x0F, 0x0A };
   const unsigned char configAC[5] = { TS_AC_USL, 0xC9, 0x82, 0xB5 };
#else
      /* Latest values from Jeremy */
   const unsigned char configMHD[9] = { TS_MHD_RISING, 0x01, 0x01, 0x00, 0x00, 0x01, 0x01, 0xFF, 0x02 };
   const unsigned char configELE0[13] = { TS_ELE0_TOUCH, 0x01, 0x02, 0x01, 0x02, 0x01, 0x02, 0x01, 0x02, 0x01, 0x02, 0x01, 0x02 };
   const unsigned char configAC[5] = { TS_AC_USL, 0xC8, 0x82, 0xB4 };
#endif

#define USER_PIN_LIMIT        (8)
const uint8_t userNVPIN[4] = { 0x08, 0x10, 0x01, 0x04 };
uint8_t userNVPINCount = 4;
uint8_t userPIN[USER_PIN_LIMIT];
uint8_t userPINCount;

/*
** Setup the touch sensor. This involves the following:
**   #1). I2C address of touch sensor is 0x5A
**   #2). Reset the touch sensor chip
**   #3). Setup all LED's to OFF position
**   #4).
*/
void
TS_Setup (void)
{
   uint8_t data_byte[6];

      /* First send a reset command to the touch sensor chip */
   data_byte[0] = TS_SOFT_RESET_REG;
   data_byte[1] = TS_SOFT_RESET_CMD;
   I2C_Write (I2C_ADDR_TOUCH, data_byte, 2, TRUE);

      /*
      ** Next, setup all the LED pins and turn them off
      ** ELE5 is not connected, so rig it to be an input with a pullup per AN3894.PDF
      */
   data_byte[0] = TS_GPIO_CTRL1;
   data_byte[1] = TS_ALL_LEDS;
   data_byte[2] = TS_ALL_LEDS;
   data_byte[3] = 0;
   data_byte[4] = TS_ALL_LEDS;
   data_byte[5] = TS_ALL_LEDS | TS_UNUSED_ELE5;
   I2C_Write (I2C_ADDR_TOUCH, data_byte, 6, TRUE);

      /*
      ** Configuration the sensors. The default setting currently is from Freescale and
      ** document AN3944.
      */
   I2C_Write (I2C_ADDR_TOUCH, configMHD, 9, TRUE);
   I2C_Write (I2C_ADDR_TOUCH, configELE0, 13, TRUE);

      /* Set sample rate to 64 milliseconds */
   data_byte[0] = TS_FILTER_CFG;
   data_byte[1] = 0x06;
   I2C_Write (I2C_ADDR_TOUCH, data_byte, 2, TRUE);

   data_byte[0] = TS_AC_CTRL0;
   data_byte[1] = 0x0B;
   I2C_Write (I2C_ADDR_TOUCH, data_byte, 2, TRUE);

   I2C_Write (I2C_ADDR_TOUCH, configAC, 4, TRUE);
//   I2C_Write (I2C_ADDR_TOUCH, configE, 2, TRUE);

   data_byte[0] = TS_E_CONFIG;
   data_byte[1] = 0x06 | 0x80;
   I2C_Write (I2C_ADDR_TOUCH, data_byte, 2, TRUE);

      /*
      ** Setup IRQ pin to let us know when there is a button press by the user. The IRQ pin is active low
      ** and it will clear when we read the status register.
      */
   NRF_GPIO->PIN_CNF[MPR121_IRQ] =
            (GPIO_PIN_CNF_DIR_Input      << GPIO_PIN_CNF_DIR_Pos)
          | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos)
          | (GPIO_PIN_CNF_PULL_Disabled  << GPIO_PIN_CNF_PULL_Pos)
          | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos)
          | (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos);

   NRF_GPIOTE->CONFIG[2] =
            (GPIOTE_CONFIG_MODE_Event << GPIOTE_CONFIG_MODE_Pos)
          | (MPR121_IRQ << GPIOTE_CONFIG_PSEL_Pos)
          | (GPIOTE_CONFIG_POLARITY_HiToLo << GPIOTE_CONFIG_POLARITY_Pos);

   NRF_GPIOTE->INTENSET |= GPIOTE_INTENSET_IN2_Msk;

   data_byte[0] = TS_GPIO_DATA;
   I2C_Write (I2C_ADDR_TOUCH, data_byte, 1, FALSE);
   I2C_Read (I2C_ADDR_TOUCH, &data_byte[1], 2);
}

/*
** For now, a simple little function that will keep 1 LED active and will rotate the LED through
** all the possible choices.
*/
void
TS_ToggleTestLED (void)
{
   uint8_t data_byte[6];

   data_byte[0] = TS_GPIO_DATA;
   I2C_Write (I2C_ADDR_TOUCH, data_byte, 1, FALSE);
   I2C_Read (I2C_ADDR_TOUCH, &data_byte[1], 2);

   data_byte[1] <<= 1;
   if (!data_byte[1])
      data_byte[1] = TS_FIRST_LED;

   I2C_Write (I2C_ADDR_TOUCH, data_byte, 2, TRUE);
}

/*
** Should be able to just implement this using the Toggle command. Although testing has shown that
** the first call to this routine does not work. Tried extra calls during the initialization routine
** and nothing seems to work. Have also let the system set for minutes and then made the first call
** to Toggle and it also doesn't work. On the second call it then works just fine.
**
** What does work is reading back the current settings and then sending the Toggle command.
** At this point, might as well just do the toggle ourselves then.
*/
uint8_t
TS_ToggleLED (uint8_t led)
{
   uint8_t data_byte[3];

   data_byte[0] = TS_GPIO_DATA;
   I2C_Write (I2C_ADDR_TOUCH, data_byte, 1, FALSE);
   I2C_Read (I2C_ADDR_TOUCH, &data_byte[2], 1);

   data_byte[1] = (data_byte[2] ^ led) & TS_ALL_LEDS;
   I2C_Write (I2C_ADDR_TOUCH, data_byte, 2, TRUE);

   data_byte[0] = TS_GPIO_DATA;
   I2C_Write (I2C_ADDR_TOUCH, data_byte, 1, FALSE);
   I2C_Read (I2C_ADDR_TOUCH, &data_byte[1], 1);

   return (data_byte[1]);
}

/*
** Ran into a similar problem with this function. Found some cases where I called this and nothing happened
** and then the next call it worked. Found that adding a read seemed to help
*/
void
TS_SetLED(uint8_t led)
{
   uint8_t data_byte[6];

   data_byte[0] = TS_GPIO_DATA;
   I2C_Write (I2C_ADDR_TOUCH, data_byte, 1, FALSE);
   I2C_Read (I2C_ADDR_TOUCH, &data_byte[2], 1);

   data_byte[1] = led & TS_ALL_LEDS;
   I2C_Write (I2C_ADDR_TOUCH, data_byte, 2, TRUE);
}

/*
** This routine will read in the touch button status and will match an LED
** to each button that is lit. The idea is to keep the LED lit while the button
** is pushed. So a routine somewhere else will notice the touch interrupt
** has been called and will then call this routine to then do something
** about it.
**
** If lockActive is TRUE then implement the logic for capturing a PIN sequence
** If user enters a valid value then return TRUE, otherwise return FALSE.
*/
uint8_t
TS_TouchLED (bool lockActive)
{
   uint8_t buffer[3];
   uint8_t led_state = 0;
   uint8_t status = FALSE;

      /* Fetch the state of the buttons */
   buffer[0] = TS_TOUCH_STATUS;
   I2C_Write (I2C_ADDR_TOUCH, buffer, 1, FALSE);
   I2C_Read (I2C_ADDR_TOUCH, &buffer[1], 2);

   buffer[1] &= TS_SENSOR_MASK;
   if (buffer[1])
      {
      if (buffer[1] & TS_ELE0)
         led_state |= TS_LED1;

      if (buffer[1] & TS_ELE1)
         led_state |= TS_LED6;

      if (buffer[1] & TS_ELE2)
         led_state |= TS_LED3;

      if (buffer[1] & TS_ELE3)
         led_state |= TS_LED4;

      if (buffer[1] & TS_ELE4)
         led_state |= TS_LED5;
      }

      /* Always set the LED's when this is called */
   TS_SetLED (led_state);

   if (lockActive)
      {
         /*
         ** For now, only take single button presses. Ignore any multiple button presses and don't punish.
         ** I don't know if we want to punish on multiple pushes or not. Something to discuss.
         */
      if (   (buffer[1] == TS_ELE0)
          || (buffer[1] == TS_ELE2)
          || (buffer[1] == TS_ELE3)
          || (buffer[1] == TS_ELE4))
         {
         if (userPINCount < USER_PIN_LIMIT)
            {
            userPIN[userPINCount] = buffer[1];
            userPINCount++;
            }
         }

         /*
         ** Did user press the center button? If so then check the PIN entered. If good, toggle
         ** lock. In either case, clear the PIN and start over looking for a password again.
         */
      if (buffer[1] == TS_ELE1)
         {
         if ((userNVPINCount == userPINCount) && !(memcmp (userNVPIN, userPIN, userNVPINCount)))
            {
            printf("Lock sequence is good!\n");
            status = TRUE;
            }

         userPINCount = 0;
         }
      }

   return (status);
}
