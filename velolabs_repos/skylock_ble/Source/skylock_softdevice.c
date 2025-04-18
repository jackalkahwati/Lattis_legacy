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
** File Name:  skylock_softdevice.c
**
** Purpose:    This is the skylock version of Nordic's softdevice_handler C file. The Nordic version is a generic, one size
**             fits all version while this version is taylored specifically for Skylocks needs.
*/

#include "master.h"
#include "stdio.h"
#include "hardware.h"
#include "ble_gatt.h"
#include "app_util.h"
#include "nrf_soc.h"
#include "nrf_sdm.h"
#include "ble_skylock.h"

/**********************************************************************************************************************
** Global structures required by the Soft Device are listed next. Please describe everything that we know            **
** about these elements so as to help others understand what is going on.                                            **
**                                                                                                                   **
** When possible keep the same name as Nordic was using to possibly help reduce any confusion. Try to list where     **
** Noridic was using or defining the value though.                                                                   **
**********************************************************************************************************************/

/*
** Need a buffer to fetch events from the Soft Device. Nordic reserved a single global buffer for this so we will keep
** doing that. This buffer is meant to hold some structures and things so it should be aligned on a 4-byte interval.
*/
#define BLE_EVT_BUFFER_SIZE            (sizeof(ble_evt_t) + (GATT_MTU_SIZE_DEFAULT))
#define BLE_EVT_BUFFER_PTR             ((uint8_t *) BLE_EVT_BUFFER)
uint32_t    BLE_EVT_BUFFER[CEIL_DIV(BLE_EVT_BUFFER_SIZE, sizeof(uint32_t))];

/*
** Flag that is set anytime the Soft Device informs us there is event data to pull. This will wake us up and at task level
** we will pull the event data.
*/
bool        SKY_sd_event_waiting;


/*
** This function should be called after every soft device function is used if an error is not tolerated. Basically
** most soft device functions should never have an error if used properly. If we hit an error that then means
** something bad has happened. So in that case, a reboot is probably the best option here.
**
** Nordic used several code macros for this. As noted in other files I don't like code macros. A simple function
** is much more efficient. Noridic had several error macros that eventually led to a function. So for Skylock
** we will just have a function that takes care of all this.
**
** This function ultimately will reboot the device if there is an error. But prior to that, this would be the
** place to add other logic. May want to dump information out the serial port first. May want to record some
** information in RAM or NV memory possibly to help with debugging. For normal release code, a reboot is probably
** about all we need.
*/
void
SKY_check_error (uint32_t code)
{
   if (code != NRF_SUCCESS)
      {
      printf("SKY_check_error called\n");
      ForceRestart ();
      }
}

/*
** This is a callback from the Soft Device if it hits a critical failure. The only exit from this condition
** is a reset. This function is passed to the Soft Device when the soft device is first enabled.
**
** pc         The value of the program counter when the ASSERT call failed.
** line_num   Line number of the failing ASSERT call.
** file_name  File name of the failing ASSERT call.
*/
void
SKY_softdevice_assertion_handler (uint32_t pc, uint16_t line_num, const uint8_t * file_name)
{
   printf("Soft device assert: %s %u PC: %u\n", file_name, line_num, pc);
   ForceRestart ();
}

/*
** This function is the Skylock version of the Nordic function that will enable the soft device. Once
** this function is called the soft device is running and basically it is now in charge of many of the
** CPU features. THe ASM file with the interrupt vectors is a quick cheat sheet of what the soft device
** controls.
*/
void
SKY_softdevice_handler_init (void)
{
   uint32_t err_code;

      /* Initialize SoftDevice */
   err_code = sd_softdevice_enable (NRF_CLOCK_LFCLKSRC_XTAL_20_PPM, SKY_softdevice_assertion_handler);
   SKY_check_error (err_code);

      /* Enable BLE event interrupt (interrupt priority has already been set by the stack) */
   sd_nvic_EnableIRQ (SWI2_IRQn);
}

/*
** Call this at task level to check if there are events from the Soft Device to process and if there
** are then go get them and process them.
*/
void
SKY_softdevice_events_execute(void)
{
   bool no_more_soc_evts = false;
   bool no_more_ble_evts = false;

   if (SKY_sd_event_waiting)
      {
      for (;;)
         {
         uint32_t err_code;

         if (!no_more_soc_evts)
            {
            uint32_t evt_id;

               // Pull event from SOC.
            err_code = sd_evt_get(&evt_id);

            if (err_code == NRF_ERROR_NOT_FOUND)
               no_more_soc_evts = true;

            else
               {
               SKY_check_error (err_code);

                  // Call application's SOC event handler.
               sys_evt_dispatch(evt_id);
               }
            }

            // Fetch BLE Events.
         if (!no_more_ble_evts)
            {
               // Pull event from stack
            uint16_t evt_len = BLE_EVT_BUFFER_SIZE;

            err_code = sd_ble_evt_get(BLE_EVT_BUFFER_PTR, &evt_len);

            if (err_code == NRF_ERROR_NOT_FOUND)
               no_more_ble_evts = true;

            else
               {
               SKY_check_error (err_code);

                  // Call application's BLE stack event handler.
               ble_evt_dispatch((ble_evt_t *)BLE_EVT_BUFFER_PTR);
               }
            }

         if (no_more_soc_evts && no_more_ble_evts)
            break;
         }

      SKY_sd_event_waiting = false;
      }
}

/*
** This is how the soft device lets us know there is an event ready for us to go pull.
** Since this is a handler, we will set a flag to indicate there is something to pull and
** then go pull the event at task level.
*/
void
SWI2_IRQHandler (void)
{
   SKY_sd_event_waiting = true;
}

