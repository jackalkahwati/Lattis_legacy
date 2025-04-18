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
** File Name:  ble_interface.c
**
** Purpose:    Routines to manage the BLE interface
**
** Implementation Notest:
**
** #1). Nordic seems to like using code macros. I really do not approve of those things. For one they
**      make it difficult to debug code because when doing symbolic debugging they are typically hidden
**      from view. But more important they tend to waste code space. These ARM CPU's are fast so there is
**      no benefit for inline code. Using a function instead saves code space which may be at a premium
**      at some point. The one downside is a function will hit the stack, but this is a single stack product,
**      so the cost for that should be minimal.
**
** #2). The other thing I found is they have hidden things inside the macros. Like the Soft Device init macro
**      allocates a buffer. Don't keep things like this hidden.
**
** #3). The Nordic code seems to have some extra function layers (functions call functions that then call the
**      soft device. Again, this can unnecessarily load the stack. I don't necessarily want to avoid all of this
**      but if it makes more sense to just call the soft device function, then do that.
*/

#include "master.h"
#include "stdio.h"
#include "string.h"
#include "hardware.h"
#include "ble_gap.h"
#include "softdevice_handler.h"
#include "ble_skylock.h"
#include "app_timer.h"
#include "ble_advdata.h"
//#include "ble_lbs.h"
#include "utils.h"
#include "ble_conn_params.h"
#include "app_timer.h"
#include "i2c.h"
#include "ble_hci.h"
#include "uart.h"
#include "timers.h"

/**********************************************************************************************************************
** Global structures required by the Soft Device are listed next. Please describe everything that we know            **
** about these elements so as to help others understand what is going on.                                            **
**                                                                                                                   **
** When possible keep the same name as Nordic was using to possibly help reduce any confusion. Try to list where     **
** Noridic was using or defining the value though.                                                                   **
**********************************************************************************************************************/

/*
** Place all our BLE service definitions here.
*/
#define SKY_UUID_BASE {0xdd, 0xea, 0x70, 0x6a, 0x9d, 0x53, 0x4b, 0xbb, 0xac, 0x0b, 0x74, 0xba, 0x81, 0x9e, 0x7d, 0x9c}
#define SKY_UUID_LED_SERVICE     (0x1523)
#define SKY_UUID_LED_STATE       (0x1524)
#define SKY_UUID_LED_ON          (0x1525)
#define SKY_UUID_LED_OFF         (0x1526)
#define SKY_UUID_LED_TOGGLE      (SKY_UUID_LED_OFF + 1)
#define SKY_UUID_TXPOWER         (SKY_UUID_LED_TOGGLE + 1)
#define SKY_UUID_LOCK_SERVICE    (SKY_UUID_TXPOWER + 1)
#define SKY_UUID_LOCK_STATE      (SKY_UUID_LOCK_SERVICE + 1)
#define SKY_UUID_LOCK_SHIFT      (SKY_UUID_LOCK_STATE + 1)
#define SKY_UUID_LOCK_INVERT     (SKY_UUID_LOCK_SHIFT + 1)

typedef struct
{
   uint16_t                   service_handle;
   uint8_t                    uuid_type;
   uint16_t                   conn_handle;

   ble_gatts_char_handles_t   ledStateHandle;
   ble_gatts_char_handles_t   ledOnHandle;
   ble_gatts_char_handles_t   ledOffHandle;
   ble_gatts_char_handles_t   ledToggleHandle;

   ble_gatts_char_handles_t   txpower;

} bleLEDservices;

bleLEDservices       serviceLED;
uint8_t              serviceLEDState;
uint8_t              serviceLEDWrite;
uint8_t              txpower[2];

typedef struct
{
   uint16_t                   service_handle;
   uint8_t                    uuid_type;
   uint16_t                   conn_handle;

   ble_gatts_char_handles_t   lockStateHandle;
   ble_gatts_char_handles_t   lockShiftHandle;
   ble_gatts_char_handles_t   lockInvertHandle;

} bleLockservices;

bleLockservices      serviceLock;
uint8_t              serviceLockState, localLockState;
uint8_t              serviceLockShift;
uint8_t              serviceLockInvert;


#define  BLE_CHAR_READ        (0x01)
#define  BLE_CHAR_WRITE       (0x02)


/* Scheduler stuff - this should move when I write our own and toss the Nordic version */
#define SCHED_MAX_EVENT_DATA_SIZE      sizeof(app_timer_event_t)                /**< Maximum size of scheduler events. Note that scheduler BLE stack events do not contain any data, as the events are being pulled from the stack in the event handler. */
#define SCHED_QUEUE_SIZE               25                                       /**< Maximum number of events in the scheduler queue. */
#define APP_SCHED_BUFFER_SIZE          ((SCHED_MAX_EVENT_DATA_SIZE + APP_SCHED_EVENT_HEADER_SIZE) * (SCHED_QUEUE_SIZE + 1))
uint32_t APP_SCHED_BUF[CEIL_DIV(APP_SCHED_BUFFER_SIZE, sizeof(uint32_t))];



#define IS_SRVC_CHANGED_CHARACT_PRESENT 0                                        /**< Include or not the service_changed characteristic. if not enabled, the server's database cannot be changed for the lifetime of the device*/
#define APP_ADV_INTERVAL                64                                       /**< The advertising interval (in units of 0.625 ms. This value corresponds to 40 ms). */
#define APP_ADV_TIMEOUT_IN_SECONDS      180                                      /**< The advertising timeout (in units of seconds). */

static ble_gap_sec_params_t             m_sec_params;                               /**< Security requirements for this application. */
static uint16_t                         m_conn_handle = BLE_CONN_HANDLE_INVALID;    /**< Handle of the current connection. */


/**@brief Function for starting advertising.
 */
static void advertising_start(void)
{
    uint32_t             err_code;
    ble_gap_adv_params_t adv_params;

    // Start advertising
    memset(&adv_params, 0, sizeof(adv_params));

    adv_params.type        = BLE_GAP_ADV_TYPE_ADV_IND;
    adv_params.p_peer_addr = NULL;
    adv_params.fp          = BLE_GAP_ADV_FP_ANY;
    adv_params.interval    = APP_ADV_INTERVAL;
//    adv_params.timeout     = APP_ADV_TIMEOUT_IN_SECONDS;
      /* Disable timeout by setting to 0 */
    adv_params.timeout     = 0;

    err_code = sd_ble_gap_adv_start(&adv_params);
    APP_ERROR_CHECK(err_code);
//    nrf_gpio_pin_set(ADVERTISING_LED_PIN_NO);
}



/**@brief Function for handling the Application's BLE Stack events.
 *
 * @param[in]   p_ble_evt   Bluetooth stack event.
 */
static void on_ble_evt(ble_evt_t * p_ble_evt)
{
    uint32_t                         err_code;
    static ble_gap_evt_auth_status_t m_auth_status;
    ble_gap_enc_info_t *             p_enc_info;

    switch (p_ble_evt->header.evt_id)
    {
        case BLE_GAP_EVT_CONNECTED:
//            nrf_gpio_pin_set(CONNECTED_LED_PIN_NO);
//            nrf_gpio_pin_clear(ADVERTISING_LED_PIN_NO);
            m_conn_handle = p_ble_evt->evt.gap_evt.conn_handle;

//            err_code = app_button_enable();
//            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GAP_EVT_DISCONNECTED:
//            nrf_gpio_pin_clear(CONNECTED_LED_PIN_NO);
            m_conn_handle = BLE_CONN_HANDLE_INVALID;

//            err_code = app_button_disable();
//            APP_ERROR_CHECK(err_code);

            advertising_start();
            break;

        case BLE_GAP_EVT_SEC_PARAMS_REQUEST:
            err_code = sd_ble_gap_sec_params_reply(m_conn_handle,
                                                   BLE_GAP_SEC_STATUS_SUCCESS,
                                                   &m_sec_params);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GATTS_EVT_SYS_ATTR_MISSING:
            err_code = sd_ble_gatts_sys_attr_set(m_conn_handle, NULL, 0);
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GAP_EVT_AUTH_STATUS:
            m_auth_status = p_ble_evt->evt.gap_evt.params.auth_status;
            break;

        case BLE_GAP_EVT_SEC_INFO_REQUEST:
            p_enc_info = &m_auth_status.periph_keys.enc_info;
            if (p_enc_info->div == p_ble_evt->evt.gap_evt.params.sec_info_request.div)
            {
                err_code = sd_ble_gap_sec_info_reply(m_conn_handle, p_enc_info, NULL);
                APP_ERROR_CHECK(err_code);
            }
            else
            {
                // No keys found for this device
                err_code = sd_ble_gap_sec_info_reply(m_conn_handle, NULL, NULL);
                APP_ERROR_CHECK(err_code);
            }
            break;

        case BLE_GAP_EVT_TIMEOUT:
            if (p_ble_evt->evt.gap_evt.params.timeout.src == BLE_GAP_TIMEOUT_SRC_ADVERTISEMENT)
            {
//                nrf_gpio_pin_clear(ADVERTISING_LED_PIN_NO);

                // Configure buttons with sense level low as wakeup source.
//                nrf_gpio_cfg_sense_input(WAKEUP_BUTTON_PIN,
//                                         BUTTON_PULL,
//                                         NRF_GPIO_PIN_SENSE_LOW);

                // Go to system-off mode (this function will not return; wakeup will cause a reset)
//                err_code = sd_power_system_off();
//                APP_ERROR_CHECK(err_code);
            }
            break;

        default:
            // No implementation needed.
            break;
    }
}












/*
** Function for handling a BLE stack event. This function i
*/

#if 0
  **@brief Function for dispatching a BLE stack event to all modules with a BLE stack event handler.
 *
 * @details This function is called from the scheduler in the main loop after a BLE stack
 *          event has been received.
 *
 * @param[in]   p_ble_evt   Bluetooth stack event.
 */
#endif
void ble_evt_dispatch(ble_evt_t * p_ble_evt)
{
   uint8_t tmp;
   printf("ble_evt called %02x\n", p_ble_evt->header.evt_id);
   UART0_TxFlush ();


   on_ble_evt(p_ble_evt);
   ble_conn_params_on_ble_evt(p_ble_evt);

   switch (p_ble_evt->header.evt_id)
      {
      case BLE_GAP_EVT_CONNECTED:
         //on_connect(p_lbs, p_ble_evt);
         break;

      case BLE_GAP_EVT_DISCONNECTED:
         //on_disconnect(p_lbs, p_ble_evt);
         break;

      case BLE_GATTS_EVT_WRITE:
         {
         ble_gatts_evt_write_t * p_evt_write = &p_ble_evt->evt.gatts_evt.params.write;

         if (p_evt_write->len == 1)
            {
            tmp = p_evt_write->data[0];

            if (p_evt_write->handle == serviceLED.ledToggleHandle.value_handle)
               {
               serviceLEDState = (TS_ToggleLED (tmp << 2)) >> 2;
               }

            else if (p_evt_write->handle == serviceLED.ledOnHandle.value_handle)
               {
               serviceLEDState |= tmp;
               TS_SetLED(serviceLEDState << 2);
               }

            else if (p_evt_write->handle == serviceLED.ledOffHandle.value_handle)
               {
               serviceLEDState &= ~tmp;
               TS_SetLED(serviceLEDState << 2);
               }

            else if (p_evt_write->handle == serviceLED.ledStateHandle.value_handle)
               {
               serviceLEDState = tmp;
               TS_SetLED(serviceLEDState << 2);
               }

            else if (p_evt_write->handle == serviceLED.txpower.value_handle)
               {
               uint8_t err_code;

               err_code = sd_ble_gap_tx_power_set (tmp);
               printf("Write TX power returns %02x %04X\n", tmp, err_code);
               UART0_TxFlush ();

               err_code = sd_ble_gap_rssi_start (m_conn_handle);
               printf("Error code from RSSI is %u\n", err_code);
               UART0_TxFlush ();
               }

            else if (p_evt_write->handle == serviceLock.lockStateHandle.value_handle)
               {
               printf("Got lock write of %u\n", tmp);
               if (tmp <= 1)
                  {
                  if (tmp != localLockState)
                     {
                     printf("Should be moving motor\n");
                     localLockState = tmp;
                     Timer2_Start();
                     Motor_Lock (localLockState == 1, MOTOR_LOCK_COUNT);
                     Timer2_Stop();
                     }
                  else
                     printf("not moving motor %u\n", tmp);
                  }
               }

            else if (p_evt_write->handle == serviceLock.lockShiftHandle.value_handle)
               {
               printf("Got lock shift of %u\n", tmp);
               if (tmp <= 1)
                  {
                  Timer2_Start();
                  Motor_Lock (tmp, 10);
                  Timer2_Stop();
                  }
               }

            else if (p_evt_write->handle == serviceLock.lockInvertHandle.value_handle)
               {
               if (tmp == 0)
                  {
                  localLockState ^= 0x01;
                  printf("Lock state is now %u\n", localLockState);
                  }

               else if (tmp == 1)
                  {
                  TS_Setup();
                  printf("Called TS init\n");
                  }
               }
            }
         break;
         }

      case BLE_GAP_EVT_RSSI_CHANGED:
         printf("got RSSI of %d\n", p_ble_evt->evt.gap_evt.params.rssi_changed.rssi);
         UART0_TxFlush ();
         txpower[1] = p_ble_evt->evt.gap_evt.params.rssi_changed.rssi;
         break;

      default:
         // No implementation needed.
         break;
      }
}


/**@brief Function for dispatching a system event to interested modules.
 *
 * @details This function is called from the System event interrupt handler after a system
 *          event has been received.
 *
 * @param[in]   sys_evt   System stack event.
 */
void sys_evt_dispatch(uint32_t sys_evt)
{
   printf("got sys_evt_dispatch\n");
   UART0_TxFlush ();
#if 0
    pstorage_sys_event_handler(sys_evt);
#endif
}


/*
**  GAP initialization.
**
**  This function sets up all the necessary GAP (Generic Access Profile) parameters of the
**  device including the device name, appearance, and the preferred connection parameters.
**
**  Sets:
**  #1). Sets the security mode. For now, using OPEN which is mode 1, level 1.
**
**  #2). Assigns the device name. For now this will by "Skylock" followed by an ASCII serial number.
**
**  #3). Connection parameters. The way this works is once we enter a connection (we are the server), the client will transmit
**       a connection event at some rate. We can choose to listen to all of these (which wastes energy so we really don't want
**       to listen to all of them). If we want to send some data though we have to wait for a connection event and then we
**       can send something. So the key things you want to accomplish here are the following:
**
**       a). What rate do we want to hit the client if we have something to send? This should most likely be our minimum
**           connection event time.
**
**       b). The reverse of this is how fast do we want the device to respond to a command the client
**           has. If we don't listen to every connection event then we have to figure out how many we choose to miss for our
**           latency response time. The Slave latency should address this although its not clear with this interface if we
**           are specifying how many events we are allowed to miss? I think that might be what we are providing. So the
**           slave latency in time would be the minimum connection interval * slave latency. Book I'm reading argues it
**           makes no sense to have an effictive slave latency less than 300 mSec or more than 1 second.
**
**       c). The supervisor timeout then needs to be something that is longer than the minimum connection interval * slave latency.
**           Because if we allow the device to miss multiple connection events, you can't allow it to miss so many that it now
**           reaches the timeout condition. In reading, it is suggested that you set this up so you have a minimum of at least
**           6 chances to hear a connection event before you allow a timeout to happen.
**
**       d). Not sure what the maximum connection interval is supposed to be. I guess the easy answer is set them the same? I suppose
**           it may be best to provide a range though in case there are some negotiations to deal with.
**
**       e). Skylock doesn't have to be that quick. A response time to user commands within 1/2 a second seems fine. Also, the speed
**           the Skylock needs to push a message probably isn't that fast either. So given that, lets go with the following:
**
**           Minimum connection interval of 100 mSec
**           Maximum connection interval of 150 mSec
**           Slave Latency (5 failures in a row allowed)
**           Connection Timeout (2 seconds)
*/

//#define MIN_CONN_INTERVAL               (MSEC_TO_UNITS(100, UNIT_1_25_MS))
#define MIN_CONN_INTERVAL               (MSEC_TO_UNITS(30, UNIT_1_25_MS))  // IPhone BLE apps keeps wanting 30 msec or we disconnect
#define MAX_CONN_INTERVAL               (MSEC_TO_UNITS(150, UNIT_1_25_MS))
#define SLAVE_LATENCY                   (5)
#define CONN_SUP_TIMEOUT                (MSEC_TO_UNITS(2000, UNIT_10_MS))

void
gap_params_init (void)
{
   ble_gap_conn_params_t   gap_conn_params;
   ble_gap_conn_sec_mode_t sec_mode;

      /*
      ** For now, set security mode to OPEN
      ** Also provide the device name that we will advertise. Should probably be "Skylock" plus
      ** a serial number or something so we can tell devices apart.
      */
   sec_mode.sm = 1;
   sec_mode.lv = 1;
   sprintf((char *)UTIL_tmpBuffer, "Skylock %08u", 1);
   SKY_check_error (sd_ble_gap_device_name_set (&sec_mode, (char const *)UTIL_tmpBuffer, strlen((char *)UTIL_tmpBuffer)));

   memset (&gap_conn_params, 0, sizeof(gap_conn_params));

   gap_conn_params.min_conn_interval = MIN_CONN_INTERVAL;
   gap_conn_params.max_conn_interval = MAX_CONN_INTERVAL;
   gap_conn_params.slave_latency     = SLAVE_LATENCY;
   gap_conn_params.conn_sup_timeout  = CONN_SUP_TIMEOUT;

   SKY_check_error (sd_ble_gap_ppcp_set (&gap_conn_params));
}

/**@brief Function for initializing the Advertising functionality.
 *
 * @details Encodes the required advertising data and passes it to the stack.
 *          Also builds a structure to be passed to the stack when starting advertising.
 */
void
advertising_init(void)
{
   ble_advdata_t advdata;
   ble_advdata_t scanrsp;
//   uint8_t       flags = BLE_GAP_ADV_FLAGS_LE_ONLY_LIMITED_DISC_MODE;
   uint8_t       flags = BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE;

   ble_uuid_t adv_uuids[] = {SKY_UUID_LED_SERVICE, serviceLED.uuid_type};

      // Build and set advertising data
   memset(&advdata, 0, sizeof(advdata));

   advdata.name_type               = BLE_ADVDATA_FULL_NAME;
   advdata.include_appearance      = true;
   advdata.flags.size              = sizeof(flags);
   advdata.flags.p_data            = &flags;

   memset(&scanrsp, 0, sizeof(scanrsp));
   scanrsp.uuids_complete.uuid_cnt = sizeof(adv_uuids) / sizeof(adv_uuids[0]);
   scanrsp.uuids_complete.p_uuids  = adv_uuids;

   SKY_check_error (ble_advdata_set(&advdata, &scanrsp));
}


/*
** This function is used to add a characteristic to a primary service
*/
void
BLE_addCharacteristic (uint16_t serviceHandle, ble_gatts_char_handles_t *charHandle,
                       uint8_t flag, char const *name, uint8_t *ptrData, uint8_t lenData, ble_uuid_t * uuid)
{
   ble_gatts_char_md_t  char_md;
   ble_gatts_attr_t     attr_char_value;
   ble_gatts_attr_md_t  attr_md;

   memset(&char_md, 0, sizeof(char_md));
   memset(&attr_md, 0, sizeof(attr_md));
   memset(&attr_char_value, 0, sizeof(attr_char_value));

   if (flag & BLE_CHAR_READ)
      {
      char_md.char_props.read   = 1;
      BLE_GAP_CONN_SEC_MODE_SET_OPEN (&attr_md.read_perm);
      }

   if (flag & BLE_CHAR_WRITE)
      {
      char_md.char_props.write  = 1;
      BLE_GAP_CONN_SEC_MODE_SET_OPEN (&attr_md.write_perm);
      }

   char_md.p_char_user_desc  = (uint8_t *)name;
   char_md.char_user_desc_max_size = strlen(name);
   char_md.char_user_desc_size =  strlen(name);
   char_md.p_char_pf         = NULL;
   char_md.p_user_desc_md    = NULL;
   char_md.p_cccd_md         = NULL;
   char_md.p_sccd_md         = NULL;

   attr_md.vloc       = BLE_GATTS_VLOC_USER;
   attr_md.rd_auth    = 0;
   attr_md.wr_auth    = 0;
   attr_md.vlen       = 0;

   attr_char_value.p_uuid       = uuid;
   attr_char_value.p_attr_md    = &attr_md;
   attr_char_value.init_len     = lenData;
   attr_char_value.init_offs    = 0;
   attr_char_value.max_len      = lenData;
   attr_char_value.p_value      = ptrData;

   SKY_check_error (sd_ble_gatts_characteristic_add (serviceHandle, &char_md, &attr_char_value, charHandle));
}

/*
** Setup all the services we are going to provide on this device. All services will be primary unless
** otherwise noted. For each service there will be characteristics
**
** Service: LED
**    Characteristic #1: LED ON (Write only)
**    Characteristic #2: LED OFF (Write only)
**    Characteristic #3: LED Toggle (Write only)
**    Characteristic #4: LED State (Read only)
**
**    All characteristics take a 1 byte field that maps to the LED's. There are 6 LED's so the low 6 bits
**    each represent one LED. Each characteristic works likes by only addressing the LED's that have a bit
**    set. So if you sent LED ON with 0x01, that would light the first LED. if you then sent LED ON with 0x02
**    that would light the 2nd LED, and the 1st LED would still be lit. Sending LED ON with 0x3F would turn
**    all LED's on.
*/
void
services_init (void)
{
   ble_uuid128_t        base_uuid = {SKY_UUID_BASE};
   ble_uuid_t           uuid;

      /* Initialize service structure */
   serviceLED.conn_handle = BLE_CONN_HANDLE_INVALID;
   serviceLock.conn_handle = BLE_CONN_HANDLE_INVALID;

      /* Register the Vendor specific 128-bit UUID */
   SKY_check_error (sd_ble_uuid_vs_add (&base_uuid, &serviceLED.uuid_type));

   uuid.type = serviceLED.uuid_type;
   uuid.uuid = SKY_UUID_LED_SERVICE;

   SKY_check_error (sd_ble_gatts_service_add (BLE_GATTS_SRVC_TYPE_PRIMARY, &uuid, &serviceLED.service_handle));

   uuid.uuid = SKY_UUID_LED_STATE;
   BLE_addCharacteristic (serviceLED.service_handle, &serviceLED.ledStateHandle, BLE_CHAR_READ | BLE_CHAR_WRITE,
                          "LED State", &serviceLEDState, sizeof (uint8_t), &uuid);

   uuid.uuid = SKY_UUID_LED_ON;
   BLE_addCharacteristic (serviceLED.service_handle, &serviceLED.ledOnHandle, BLE_CHAR_WRITE,
                          "LED On", &serviceLEDWrite, sizeof (uint8_t), &uuid);

   uuid.uuid = SKY_UUID_LED_OFF;
   BLE_addCharacteristic (serviceLED.service_handle, &serviceLED.ledOffHandle, BLE_CHAR_WRITE,
                          "LED Off", &serviceLEDWrite, sizeof (uint8_t), &uuid);

   uuid.uuid = SKY_UUID_LED_TOGGLE;
   BLE_addCharacteristic (serviceLED.service_handle, &serviceLED.ledToggleHandle, BLE_CHAR_WRITE,
                          "LED Toggle", &serviceLEDWrite, sizeof (uint8_t), &uuid);

   uuid.uuid = SKY_UUID_TXPOWER;
   BLE_addCharacteristic (serviceLED.service_handle, &serviceLED.txpower, BLE_CHAR_WRITE | BLE_CHAR_READ,
                          "TX Power", txpower, sizeof(txpower), &uuid);

   uuid.uuid = SKY_UUID_LOCK_SERVICE;

   SKY_check_error (sd_ble_gatts_service_add (BLE_GATTS_SRVC_TYPE_PRIMARY, &uuid, &serviceLock.service_handle));

   uuid.uuid = SKY_UUID_LOCK_STATE;
   BLE_addCharacteristic (serviceLock.service_handle, &serviceLock.lockStateHandle, BLE_CHAR_READ | BLE_CHAR_WRITE,
                          "Lock State", &serviceLockState, sizeof (uint8_t), &uuid);

   uuid.uuid = SKY_UUID_LOCK_SHIFT;
   BLE_addCharacteristic (serviceLock.service_handle, &serviceLock.lockShiftHandle, BLE_CHAR_WRITE,
                          "Lock Shift", &serviceLockShift, sizeof (uint8_t), &uuid);

      /* Make this guy a multi-purpose diagnostic tool. Fixup name and locate appropriately later */
   uuid.uuid = SKY_UUID_LOCK_INVERT;
   BLE_addCharacteristic (serviceLock.service_handle, &serviceLock.lockInvertHandle, BLE_CHAR_WRITE,
                          "Testing", &serviceLockInvert, sizeof (uint8_t), &uuid);
}



#define APP_TIMER_PRESCALER             0                                           /**< Value of the RTC1 PRESCALER register. */
#define APP_TIMER_MAX_TIMERS            4                                           /**< Maximum number of simultaneously created timers. */
#define APP_TIMER_OP_QUEUE_SIZE         4                                           /**< Size of timer operation queues. */


/**@brief Function for initializing the Connection Parameters module.
 */
#define FIRST_CONN_PARAMS_UPDATE_DELAY  APP_TIMER_TICKS(20000, APP_TIMER_PRESCALER) /**< Time from initiating event (connect or start of notification) to first time sd_ble_gap_conn_param_update is called (15 seconds). */
#define NEXT_CONN_PARAMS_UPDATE_DELAY   APP_TIMER_TICKS(5000, APP_TIMER_PRESCALER)  /**< Time between each call to sd_ble_gap_conn_param_update after the first call (5 seconds). */
#define MAX_CONN_PARAMS_UPDATE_COUNT    3                                           /**< Number of attempts before giving up the connection parameter negotiation. */
static void on_conn_params_evt(ble_conn_params_evt_t * p_evt)
{
    uint32_t err_code;

    printf("Got on_conn_params_evt call %u\n", p_evt->evt_type);
    UART0_TxFlush ();
    if(p_evt->evt_type == BLE_CONN_PARAMS_EVT_FAILED)
    {
        err_code = sd_ble_gap_disconnect(m_conn_handle, BLE_HCI_CONN_INTERVAL_UNACCEPTABLE);
        APP_ERROR_CHECK(err_code);
    }
}

static void conn_params_error_handler(uint32_t nrf_error)
{
   printf("conn_params_error_handler\n");
   UART0_TxFlush ();
    APP_ERROR_HANDLER(nrf_error);
}

static void conn_params_init(void)
{
    ble_conn_params_init_t cp_init;

    memset(&cp_init, 0, sizeof(cp_init));

    cp_init.p_conn_params                  = NULL;
    cp_init.first_conn_params_update_delay = FIRST_CONN_PARAMS_UPDATE_DELAY;
    cp_init.next_conn_params_update_delay  = NEXT_CONN_PARAMS_UPDATE_DELAY;
    cp_init.max_conn_params_update_count   = MAX_CONN_PARAMS_UPDATE_COUNT;
    cp_init.start_on_notify_cccd_handle    = BLE_GATT_HANDLE_INVALID;
    cp_init.disconnect_on_fail             = false;
    cp_init.evt_handler                    = on_conn_params_evt;
    cp_init.error_handler                  = conn_params_error_handler;

    SKY_check_error (ble_conn_params_init(&cp_init));
}




/*
** This routine should get Bluetooth configured and up and running
*/
void
BLE_Setup (void)
{
   uint8_t latch_irq;

   ble_enable_params_t  ble_enable_params;

      // Initialize timer module, making it use the scheduler
   APP_TIMER_INIT(APP_TIMER_PRESCALER, APP_TIMER_MAX_TIMERS, APP_TIMER_OP_QUEUE_SIZE, true);

      /* Initialize the SoftDevice handler module */
   SKY_softdevice_handler_init ();

      /* Enable BLE stack */
   memset(&ble_enable_params, 0, sizeof(ble_enable_params));
   ble_enable_params.gatts_enable_params.service_changed = IS_SRVC_CHANGED_CHARACT_PRESENT;
   SKY_check_error (sd_ble_enable(&ble_enable_params));

   SKY_check_error (app_sched_init (SCHED_MAX_EVENT_DATA_SIZE, SCHED_QUEUE_SIZE, APP_SCHED_BUF));

   gap_params_init();
   services_init();
   advertising_init();
   conn_params_init();
   advertising_start();

   latch_irq = TS_IRQCounter;
   for (;;)
      {
      SKY_softdevice_events_execute();
      app_sched_execute();

      SKY_check_error (sd_app_evt_wait());

         /*
         ** May want to just call this everytime through the loop to make sure an LED doesn't get stuck.
         ** Although have to counter that with when BLE plays with the LED's. Maybe have a lockout
         ** period when something else is using the LED's?
         */
      if (latch_irq != TS_IRQCounter)
         {
         latch_irq = TS_IRQCounter;
         if (TS_TouchLED (TRUE))
            {
            localLockState ^= 0x01;
            Timer2_Start();
            Motor_Lock (localLockState == 1, MOTOR_LOCK_COUNT);
            Timer2_Stop();
            }
         }
      }
}


