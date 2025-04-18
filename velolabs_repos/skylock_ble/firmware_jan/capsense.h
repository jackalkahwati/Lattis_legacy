#ifndef __capsense_h__
#define __capsense_h__

#include "skylock_gpio.h"
#include "i2c_sm.h"
#include "common_defines.h"

void enable_capsense(i2c_notify_cb done);
void disable_capsense(void);
void dispatch_capsense(void);

#endif
