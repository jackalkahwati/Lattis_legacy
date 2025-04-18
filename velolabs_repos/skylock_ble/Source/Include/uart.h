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
** File Name:  uart.h
**
** Purpose:    Function prototypes the debug UART
*/

#ifndef _UART_H_
#define _UART_H_

   /* Function Prototypes */
extern void          UART_Setup (void);
extern int           UART0_RxWaiting (void);
extern void          UART0_TxFlush (void);


#endif /* _UART_H_ */
