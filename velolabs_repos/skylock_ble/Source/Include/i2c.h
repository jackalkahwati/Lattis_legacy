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
** File Name:  i2c.h
**
** Purpose:    Function prototypes and constants for I2C interface
*/

#ifndef _I2C_H_
#define _I2C_H_

#include "stdbool.h"

   /* I2C device addresses */
#define  I2C_ADDR_ACCEL    (0x18)
#define  I2C_ADDR_MAGNET   (0x1C)
#define  I2C_ADDR_TOUCH    (0x5A)
#define  I2C_ADDR_MOTOR    (0x60)

   /* I2C Error */
#define  I2C_ERROR_NONE       (0x00)
#define  I2C_ERROR_TIMEOUT    (0x01)
#define  I2C_ERROR_TWI        (0x02)
#define  I2C_ERROR_USAGE      (0x03)

   /* I2C Function Prototypes */
extern void             I2C_Abort (void);
extern uint8_t          I2C_FullRead (uint8_t addr, uint8_t *data, uint8_t len);
extern uint8_t          I2C_Read (uint8_t addr, uint8_t *data, uint8_t len);
extern void             I2C_Setup (void);
extern uint8_t          I2C_Write (uint8_t addr, const uint8_t *data, uint8_t len, uint8_t stop);

extern uint8_t          ACC_Read (uint8_t addr, uint8_t *data, uint8_t len);
extern uint8_t          ACC_Write (uint8_t addr, const uint8_t *data, uint8_t len, uint8_t stop);


   /* Touch Sensor Prototypes and data */
extern void             TS_SetLED (uint8_t led);
extern void             TS_Setup (void);
extern uint8_t          TS_ToggleLED (uint8_t led);
extern void             TS_ToggleTestLED (void);
extern uint8_t          TS_TouchLED (bool lockActive);

extern volatile uint8_t TS_IRQCounter;

#define  TS_TOUCH_STATUS            (0x00)
#define  TS_MHD_RISING              (0x2B)
#define  TS_ELE0_TOUCH              (0x41)
#define  TS_FILTER_CFG              (0x5D)
#define  TS_E_CONFIG                (0x5E)
#define  TS_AC_CTRL0                (0x7B)
#define  TS_AC_USL                  (0x7D)
#define  TS_AC_LSL                  (0x7E)
#define  TS_AC_TARGET               (0x7F)
#define  TS_SOFT_RESET_REG          (0x80)
#define  TS_SOFT_RESET_CMD          (0x63)
#define  TS_GPIO_CTRL1              (0x73)
#define  TS_GPIO_DATA               (0x75)
#define  TS_GPIO_SET                (0x78)
#define  TS_GPIO_CLEAR              (0x79)
#define  TS_GPIO_TOGGLE             (0x7A)
#define  TS_ALL_LEDS                (0xFC)
#define  TS_FIRST_LED               (0x04)
#define  TS_UNUSED_ELE5             (0x02)
#define  TS_LED1                    (0x04)
#define  TS_LED2                    (0x08)
#define  TS_LED3                    (0x10)
#define  TS_LED4                    (0x20)
#define  TS_LED5                    (0x40)
#define  TS_LED6                    (0x80)
#define  TS_SENSOR_MASK             (0x1F)
#define  TS_ELE0                    (0x01)
#define  TS_ELE1                    (0x02)
#define  TS_ELE2                    (0x04)
#define  TS_ELE3                    (0x08)
#define  TS_ELE4                    (0x10)


   /* Motor control functinos */
extern uint16_t         Motor_ADC (void);
extern uint16_t         Motor_Lock (bool lock, uint16_t encoderCounts);
extern void             Motor_Setup (void);
extern unsigned int     Motor_Start (bool lock);
extern unsigned int     Motor_Stop  (void);

extern volatile uint32_t MOTOR_Enc01Events;
extern volatile uint32_t MOTOR_Enc02Events;

#define  MOTOR_CTRL_REG             (0x00)
#define  MOTOR_STATUS_REG           (0x01)
//#define  MOTOR_VSET_DAC             (0x1D)      /* Max output voltage 2.33V, ADC has limit of 2.4V with no prescaling */
#define  MOTOR_VSET_DAC             (0x3F)      /* Max output voltage 2.33V, ADC has limit of 2.4V with no prescaling */
#define  MOTOR_STANDBY              (0x00)
#define  MOTOR_REVERSE              ((MOTOR_VSET_DAC << 2) | 0x01)
#define  MOTOR_FORWARD              ((MOTOR_VSET_DAC << 2) | 0x02)
#define  MOTOR_BRAKE                ((MOTOR_VSET_DAC << 2) | 0x03)
#define  MOTOR_LOCK_COUNT           (70)

   /* Magnetometer control functions */
extern void             Magnet_Setup (void);

   /* Accelerometer control Functions */
extern void             Accelerometer_Setup (void);

#endif /* _I2C_H_ */
