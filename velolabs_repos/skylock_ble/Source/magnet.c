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
** File Name:  magnet.c
**
** Purpose:    Routines to manage the magnetometer
*/

#include "master.h"
#include "stdio.h"
#include "hardware.h"
#include "i2c.h"

#define  MAG_WHO_AM_I      (0x0F)

/*
*/
void
Magnet_Setup (void)
{
   uint8_t data_byte[6];

   data_byte[0] = MAG_WHO_AM_I;
   I2C_Write (I2C_ADDR_MAGNET, data_byte, 1, FALSE);
   I2C_Read (I2C_ADDR_MAGNET, data_byte, 1);
}

