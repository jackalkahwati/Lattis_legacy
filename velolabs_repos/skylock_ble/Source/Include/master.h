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
** File Name:  master.h
**
** Purpose:    This is the master include file. All C files must include this file first.
**
**             This file includes the part numbers, date codes, copyright strings, etc.
**
**             This file should also be used to define any compile time options that control
**             features, general program behavior and things like that.
**
**             Don't include definitions and prototypes here that are unique to this project.
**             For that, use other .H files.
*/

#ifndef _MASTER_H_
#define _MASTER_H_

/*
** Include files that we should always have available to all C files
*/
#include "intrinsics.h"
#include "stdint.h"
#include "nrf.h"

/*
** Commong definitions that may be used
*/
#define  FALSE                   (0)
#define  TRUE                    !FALSE

/*
** Define the program part number here. Format is VER.REV.MINOR (1.0.0 for example)
*/
#define  PART_NUMBER_VER_M       "01"
#define  PART_NUMBER_REV_M       "00"
#define  PART_NUMBER_MINOR_M     "07"
#define  PART_DATECODE           "04/21/2015"

#define  COPYRIGHT_M             "Copyright (c) 2015, Velo Labs, All Rights Reserved"
extern const unsigned char PartNumber[];

/* Uncomment DEV_BUILD when building a release build */
#define  DEV_BUILD
#define  DEBUG

/* Pick a board to use */
//#define  BOARD_NUMBER_2
#define  BOARD_NUMBER_3

/*
** Apparently the soft device limits the interrupt priority you can use. In general I don't
** like to use priority on interrupts. Instead I like to keep interrupts really small and fast such
** that they can then all be the same priority.
*/
#define  SKYLOCK_PRIORITY        (3)


/*
** Place compiler specific functions here as macros. That way they are easy to replace
** if we switch compilers.
*/
#define  disableInterrupt()      __disable_interrupt()
#define  enableInterrupt()       __enable_interrupt()





#endif /* _MASTER_H_ */
