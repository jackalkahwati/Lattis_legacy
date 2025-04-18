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
** File Name:  hardware.h
**
** Purpose:    This is the include file to place all general hardware unique
**             definitions and prototypes.
*/

#ifndef _HARDWARE_H_
#define _HARDWARE_H_


/******************************************************************************
** GPIO Pin Definitions
******************************************************************************/

#if defined(BOARD_NUMBER_2)
   #define  I2C_SCL                       (0)
   #define  I2C_SDA                       (1)
   #define  DRV8830_FAULT                 (2)
   #define  ENC_02                        (3)
   #define  ENC_01                        (4)
   #define  ISENSE_ADC                    (5)
   #define  VBAT_ADC                      (6)
   #define  VBAT_CTRL                     (7)
   #define  UNUSED_PORT_08                (8)
   #define  BQ25505_EN                    (9)
   #define  VBAT_OK                       (10)
   #define  LIS2DH_INT1                   (11)
   #define  LIS2DH_INT2                   (12)
   #define  LIS2DH_MOSI                   (13)
   #define  LIS2DH_MISO                   (14)
   #define  LIS2DH_CS                     (15)
   #define  LIS2DH_CLK                    (16)
   #define  UNUSED_PORT_17                (17)
   #define  UNUSED_PORT_18                (18)
   #define  USB_CHG                       (19)
   #define  USB_CTRL                      (20)
   #define  UNUSED_PORT_21                (21)
   #define  UNUSED_PORT_22                (22)
   #define  UNUSED_PORT_23                (23)
   #define  UNUSED_PORT_24                (24)
   #define  UNUSED_PORT_25                (25)
   #define  XL2                           (26)
   #define  XL1                           (27)
   #define  LIS3MDL_INT                   (28)
   #define  MPR121_IRQ                    (29)
   #define  MAG_SW                        (30)

   /* Should be able to cut traces and use the following */
   #define  UART_RX                       (MPR121_IRQ)
   #define  UART_TX                       (LIS3MDL_INT)
   //#define  UART_RX                       (I2C_SCL)
   //#define  UART_TX                       (I2C_SDA)

#elif defined (BOARD_NUMBER_3)
   #define  VBAT_ADC                      (0)
   #define  VBAT_CTRL                     (1)
   #define  ISENSE_ADC                    (2)
   #define  VBAT_OK                       (3)
   #define  SOLAR_EN                      (4)
   #define  UNUSED_PORT_05                (5)
   #define  USB_CHRG                      (6)
   #define  USB_CTRL                      (7)
   #define  UNUSED_PORT_08                (8)
   #define  MPR121_IRQ                    (9)
   #define  LIS3MDL_INT                   (10)
   #define  EN_MOTOR                      (11)
   #define  I2C_SCL                       (12)
   #define  I2C_SDA                       (13)
   #define  FAULTn                        (14)
   #define  ENC_02                        (15)
   #define  ENC_01                        (16)
   #define  UART2                         (17)
   #define  UART1                         (18)
   #define  UNUSED_PORT_19                (19)
   #define  UNUSED_PORT_20                (20)
   #define  LIS2DH_INT1                   (21)
   #define  LIS2DH_INT2                   (22)
   #define  LIS2DH_CSS                    (23)
   #define  SDA2                          (24)
   #define  SCL2                          (25)
   #define  XL2                           (26)
   #define  XL1                           (27)
   #define  UNUSED_PORT_28                (28)
   #define  UNUSED_PORT_29                (29)
   #define  UNUSED_PORT_30                (30)

   #define  UART_RX                       (UART1)
   #define  UART_TX                       (UART2)

   #define  LIS2DH_CLK                    (SCL2)
   #define  LIS2DH_CS                     (SDA2)
   #define  LIS2DH_MOSI                   (LIS2DH_CSS)
   #define  MAG_SW                        (EN_MOTOR)

#endif


/******************************************************************************
** CPU Specific definitions
******************************************************************************/

#define  CPU_FAST_CLOCK                (16000000)
#define  CPU_SLOW_CLOCK                (32768)

#define  ADDR_FICR                     (0x10000000)
#define  ADDR_UICR                     (0x10001000)
#define  ADDR_START_SRAM               (0x20000000)

extern unsigned int *                  GetStackPointer(void);
extern unsigned int                    ASM_GetIPSR(void);


/******************************************************************************
** Miscellaneous function prototypes
******************************************************************************/

extern void                            ForceRestart (void);

#endif /* _HARDWARE_H_ */
