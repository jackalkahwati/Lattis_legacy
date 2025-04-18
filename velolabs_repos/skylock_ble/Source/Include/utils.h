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
** File Name:  utils.h
**
** Purpose:    Function prototypes and constants for utils C file
*/

#ifndef _UTILS_H_
#define _UTILS_H_

#define UTIL_TEMP_BUFFER_SIZE       (128)

   /* Function Prototypes */
extern void             UTIL_DelayUsec(unsigned int usec);
extern void             MFG_Mode (void);

   /* Commong global variables */
extern uint8_t          UTIL_tmpBuffer[UTIL_TEMP_BUFFER_SIZE];

#define  MFG_MAX_READ_SIZE       (128)
extern uint8_t mfgBuffer[MFG_MAX_READ_SIZE + 1];

#endif /* _UTILS_H_ */
