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
** File Name:  timers.h
**
** Purpose:    Function prototypes any timers being used
*/

#ifndef _TIMERS_H_
#define _TIMERS_H_

#define  TIMER_USEC_PER_MSEC        (1000)
#define  TIMER2_BASE_USEC           (4)
#define  TIMER2_20_MSEC             (20 * (TIMER_USEC_PER_MSEC / TIMER2_BASE_USEC))




extern uint16_t   Timer2_Count (void);
extern void       Timer_Setup (void);
extern void       Timer2_Start (void);
extern void       Timer2_Stop (void);

extern unsigned int     runtimeSeconds;

#endif /* _TIMERS_H_ */
