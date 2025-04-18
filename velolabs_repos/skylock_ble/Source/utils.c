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
** File Name:  utils.c
**
** Purpose:    Simple and miscellaneous support functions.
*/

#include "master.h"
#include "stdio.h"
#include "hardware.h"
#include "utils.h"

/*
** Provide a temporary buffer that can be used anywhere at task level. Since there is only one task, if we aren't in
** an interrupt routine then it is safe to use (more or less).
*/
uint8_t  UTIL_tmpBuffer[UTIL_TEMP_BUFFER_SIZE];

/*
** Normally like to do this with a timer, but for a lower power device it is better to just figure out a little
** loop that is roughly correct. Bigger delays can use a timer and can be more accurate then.
**
** Right now there is only a 16MHz clock. If that changes, then add some conditional compiler code or something
** to deal with more than one clock. But for now that is not necessary.
**
** Measured the following routine when called from a routine that sets a GPIO low, calls this routine, and sets GPIO high.
**    5 usec call resulted in delay of 6.3 usec
**    100 usec call resulted in delay of 100 usec
*/
void
UTIL_DelayUsec (unsigned int usec)
{
   while (usec)
      {
      asm ("NOP");
      asm ("NOP");
      asm ("NOP");
      asm ("NOP");
      asm ("NOP");
      asm ("NOP");
      asm ("NOP");
      asm ("NOP");
      asm ("NOP");
      asm ("NOP");
      asm ("NOP");

      usec--;
      }
}
