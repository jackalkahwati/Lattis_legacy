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
** File Name:  ble_skylock.h
**
** Purpose:    Function prototypes and definitions for Skylock BLE and soft device
*/

#ifndef _BLE_SKYLOCK_H_
#define _BLE_SKYLOCK_H_

#include "ble.h"

   /* Function Prototypes */
extern void          BLE_Setup (void);
extern void          sys_evt_dispatch (uint32_t sys_evt);
extern void          ble_evt_dispatch (ble_evt_t * p_ble_evt);

extern void          SKY_softdevice_handler_init (void);
extern void          SKY_check_error (uint32_t code);
extern void          SKY_softdevice_events_execute(void);


#endif /* _BLE_SKYLOCK_H_ */
