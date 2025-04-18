/* Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the license.txt file.
 */

/**
 * This file is the main file for the application described in application note
 * nAN-36 Creating Bluetooth® Low Energy Applications Using nRF51822.
 */

#include "skylock_gpio.h"

// system
#include <stdint.h>
#include <string.h>
#include "nordic_common.h"
#include "nrf.h"
#include "app_error.h"
#include "nrf_gpio.h"
#include "nrf_delay.h"
#include "nrf51_bitfields.h"
#include "ble.h"
#include "ble_hci.h"
#include "ble_srv_common.h"
#include "ble_advdata.h"
#include "ble_conn_params.h"
#include "app_scheduler.h"
#include "softdevice_handler.h"
#include "app_timer.h"
#include "ble_error_log.h"
#include "app_gpiote.h"
#include "app_button.h"
#include "ble_debug_assert_handler.h"
#include "pstorage.h"

// application
#include "ble_lbs.h"
#include "servo_adc.h"
#include "i2c_sm.h"
#include "lock_sm.h"
#include "motor_quad.h"
#include "common_defines.h"
#include "uECC.h"
#include "uart.h"
#include "capsense.h"

#ifdef REAL_SKYLOCK
static char* __attribute__((unused)) ident = "$Build: SKYLOCK " __DATE__ \
    ", " __TIME__ " $";
#else
static char* __attribute__((unused)) ident = "$Build: DEVKIT " __DATE__ \
   ", " __TIME__ " $";
#endif



#define IS_SRVC_CHANGED_CHARACT_PRESENT 0                                           /**< Include or not the service_changed characteristic. if not enabled, the server's database cannot be changed for the lifetime of the device*/



#define DEVICE_NAME                     "SkyLock"                           /**< Name of device. Will be included in the advertising data. */

#define APP_ADV_INTERVAL                64                                          /**< The advertising interval (in units of 0.625 ms. This value corresponds to 40 ms). */
#define APP_ADV_TIMEOUT_IN_SECONDS      180                                         /**< The advertising timeout (in units of seconds). */


#define MIN_CONN_INTERVAL               MSEC_TO_UNITS(100, UNIT_1_25_MS)            /**< Minimum acceptable connection interval (0.5 seconds). */
#define MAX_CONN_INTERVAL               MSEC_TO_UNITS(200, UNIT_1_25_MS)           /**< Maximum acceptable connection interval (1 second). */
#define SLAVE_LATENCY                   0                                           /**< Slave latency. */
#define CONN_SUP_TIMEOUT                MSEC_TO_UNITS(4000, UNIT_10_MS)             /**< Connection supervisory timeout (4 seconds). */
#define FIRST_CONN_PARAMS_UPDATE_DELAY  APP_TIMER_TICKS(20000, APP_TIMER_PRESCALER) /**< Time from initiating event (connect or start of notification) to first time sd_ble_gap_conn_param_update is called (15 seconds). */
#define NEXT_CONN_PARAMS_UPDATE_DELAY   APP_TIMER_TICKS(5000, APP_TIMER_PRESCALER)  /**< Time between each call to sd_ble_gap_conn_param_update after the first call (5 seconds). */
#define MAX_CONN_PARAMS_UPDATE_COUNT    3                                           /**< Number of attempts before giving up the connection parameter negotiation. */

#define APP_GPIOTE_MAX_USERS            1                                           /**< Maximum number of users of the GPIOTE handler. */

#define BUTTON_DETECTION_DELAY          APP_TIMER_TICKS(50, APP_TIMER_PRESCALER)    /**< Delay from a GPIOTE event until a button is reported as pushed (in number of timer ticks). */

#define SEC_PARAM_TIMEOUT               30                                          /**< Timeout for Pairing Request or Security Request (in seconds). */
#define SEC_PARAM_BOND                  1                                           /**< Perform bonding. */
#define SEC_PARAM_MITM                  0                                           /**< Man In The Middle protection not required. */
#define SEC_PARAM_IO_CAPABILITIES       BLE_GAP_IO_CAPS_NONE                        /**< No I/O capabilities. */
#define SEC_PARAM_OOB                   0                                           /**< Out Of Band data not available. */
#define SEC_PARAM_MIN_KEY_SIZE          7                                           /**< Minimum encryption key size. */
#define SEC_PARAM_MAX_KEY_SIZE          16                                          /**< Maximum encryption key size. */

#define DEAD_BEEF                       0xDEADBEEF                                  /**< Value used as error code on stack dump, can be used to identify stack location on stack unwind. */

static ble_gap_sec_params_t             m_sec_params;                               /**< Security requirements for this application. */
static uint16_t                         m_conn_handle = BLE_CONN_HANDLE_INVALID;    /**< Handle of the current connection. */
ble_lbs_t                        m_lbs;


static app_timer_id_t                        m_servo_timer_id;

#define SCHED_MAX_EVENT_DATA_SIZE       sizeof(app_timer_event_t)                   /**< Maximum size of scheduler events. Note that scheduler BLE stack events do not contain any data, as the events are being pulled from the stack in the event handler. */

#define SCHED_QUEUE_SIZE                25                                          /**< Maximum number of events in the scheduler queue. */



// Persistent storage system event handler
void pstorage_sys_event_handler (uint32_t p_evt);

/**@brief Function for error handling, which is called when an error has occurred.
 *
 * @warning This handler is an example only and does not fit a final product. You need to analyze
 *          how your product is supposed to react in case of error.
 *
 * @param[in] error_code  Error code supplied to the handler.
 * @param[in] line_num    Line number where the handler is called.
 * @param[in] p_file_name Pointer to the file name.
 */
void app_error_handler(uint32_t error_code, uint32_t line_num, const uint8_t * p_file_name)
{
    // This call can be used for debug purposes during application development.
    // @note CAUTION: Activating this code will write the stack to flash on an error.
    //                This function should NOT be used in a final product.
    //                It is intended STRICTLY for development/debugging purposes.
    //                The flash write will happen EVEN if the radio is active, thus interrupting
    //                any communication.
    //                Use with care. Un-comment the line below to use.
    ble_debug_assert_handler(error_code, line_num, p_file_name);

    // On assert, the system can only recover with a reset.
    //NVIC_SystemReset();
}


/**@brief Callback function for asserts in the SoftDevice.
 *
 * @details This function will be called in case of an assert in the SoftDevice.
 *
 * @warning This handler is an example only and does not fit a final product. You need to analyze
 *          how your product is supposed to react in case of Assert.
 * @warning On assert from the SoftDevice, the system can only recover on reset.
 *
 * @param[in]   line_num   Line number of the failing ASSERT call.
 * @param[in]   file_name  File name of the failing ASSERT call.
 */
void assert_nrf_callback(uint16_t line_num, const uint8_t * p_file_name)
{
    app_error_handler(DEAD_BEEF, line_num, p_file_name);
}


/**@brief Function for the LEDs initialization.
 *
 * @details Initializes all LEDs used by the application.
 */
static void leds_init(void)
{
    nrf_gpio_cfg_output(ADVERTISING_LED_PIN_NO);
    nrf_gpio_cfg_output(CONNECTED_LED_PIN_NO);
    nrf_gpio_cfg_output(LEDBUTTON_LED_PIN_NO);
}




/**@brief Function for the GAP initialization.
 *
 * @details This function sets up all the necessary GAP (Generic Access Profile) parameters of the
 *          device including the device name, appearance, and the preferred connection parameters.
 */
static void gap_params_init(void)
{
    uint32_t                err_code;
    ble_gap_conn_params_t   gap_conn_params;
    ble_gap_conn_sec_mode_t sec_mode;

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&sec_mode);

    err_code = sd_ble_gap_device_name_set(&sec_mode,
                                          (const uint8_t *)DEVICE_NAME,
                                          strlen(DEVICE_NAME));
    APP_ERROR_CHECK(err_code);

    memset(&gap_conn_params, 0, sizeof(gap_conn_params));

    gap_conn_params.min_conn_interval = MIN_CONN_INTERVAL;
    gap_conn_params.max_conn_interval = MAX_CONN_INTERVAL;
    gap_conn_params.slave_latency     = SLAVE_LATENCY;
    gap_conn_params.conn_sup_timeout  = CONN_SUP_TIMEOUT;

    err_code = sd_ble_gap_ppcp_set(&gap_conn_params);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for initializing the Advertising functionality.
 *
 * @details Encodes the required advertising data and passes it to the stack.
 *          Also builds a structure to be passed to the stack when starting advertising.
 */
static void advertising_init(void)
{
    uint32_t      err_code;
    ble_advdata_t advdata;
    ble_advdata_t scanrsp;
    uint8_t       flags = BLE_GAP_ADV_FLAGS_LE_ONLY_LIMITED_DISC_MODE;

    ble_uuid_t adv_uuids[] = {{LBS_UUID_SERVICE, m_lbs.uuid_type}};

    // Build and set advertising data
    memset(&advdata, 0, sizeof(advdata));

    advdata.name_type               = BLE_ADVDATA_FULL_NAME;
    advdata.include_appearance      = true;
    advdata.flags.size              = sizeof(flags);
    advdata.flags.p_data            = &flags;

    memset(&scanrsp, 0, sizeof(scanrsp));
    scanrsp.uuids_complete.uuid_cnt = sizeof(adv_uuids) / sizeof(adv_uuids[0]);
    scanrsp.uuids_complete.p_uuids  = adv_uuids;

    err_code = ble_advdata_set(&advdata, &scanrsp);
    APP_ERROR_CHECK(err_code);
}

static void led_write_handler(ble_lbs_t * p_lbs, uint8_t led_state);

/**@brief Function for initializing services that will be used by the application.
 */
static void services_init(void)
{
    uint32_t err_code;
    ble_lbs_init_t init;

    init.led_write_handler = led_write_handler;

    err_code = ble_lbs_init(&m_lbs, &init);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for initializing security parameters.
 */
static void sec_params_init(void)
{
    m_sec_params.timeout      = SEC_PARAM_TIMEOUT;
    m_sec_params.bond         = SEC_PARAM_BOND;
    m_sec_params.mitm         = SEC_PARAM_MITM;
    m_sec_params.io_caps      = SEC_PARAM_IO_CAPABILITIES;
    m_sec_params.oob          = SEC_PARAM_OOB;
    m_sec_params.min_key_size = SEC_PARAM_MIN_KEY_SIZE;
    m_sec_params.max_key_size = SEC_PARAM_MAX_KEY_SIZE;
}


/**@brief Function for handling the Connection Parameters Module.
 *
 * @details This function will be called for all events in the Connection Parameters Module which
 *          are passed to the application.
 *          @note All this function does is to disconnect. This could have been done by simply
 *                setting the disconnect_on_fail config parameter, but instead we use the event
 *                handler mechanism to demonstrate its use.
 *
 * @param[in]   p_evt   Event received from the Connection Parameters Module.
 */
static void on_conn_params_evt(ble_conn_params_evt_t * p_evt)
{
    uint32_t err_code;

    if(p_evt->evt_type == BLE_CONN_PARAMS_EVT_FAILED)
    {
        err_code = sd_ble_gap_disconnect(m_conn_handle, BLE_HCI_CONN_INTERVAL_UNACCEPTABLE);
        APP_ERROR_CHECK(err_code);
    }
}


/**@brief Function for handling a Connection Parameters error.
 *
 * @param[in]   nrf_error   Error code containing information about what went wrong.
 */
static void conn_params_error_handler(uint32_t nrf_error)
{
    APP_ERROR_HANDLER(nrf_error);
}


/**@brief Function for initializing the Connection Parameters module.
 */
static void conn_params_init(void)
{
    uint32_t               err_code;
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

    err_code = ble_conn_params_init(&cp_init);
    APP_ERROR_CHECK(err_code);
}

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
    adv_params.timeout     = APP_ADV_TIMEOUT_IN_SECONDS;

    err_code = sd_ble_gap_adv_start(&adv_params);
    APP_ERROR_CHECK(err_code);
    nrf_gpio_pin_set(ADVERTISING_LED_PIN_NO);
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
            nrf_gpio_pin_set(CONNECTED_LED_PIN_NO);
            nrf_gpio_pin_clear(ADVERTISING_LED_PIN_NO);
            m_conn_handle = p_ble_evt->evt.gap_evt.conn_handle;

            err_code = app_button_enable();
            APP_ERROR_CHECK(err_code);
            break;

        case BLE_GAP_EVT_DISCONNECTED:
            nrf_gpio_pin_clear(CONNECTED_LED_PIN_NO);
            m_conn_handle = BLE_CONN_HANDLE_INVALID;

            err_code = app_button_disable();
            APP_ERROR_CHECK(err_code);

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
                nrf_gpio_pin_clear(ADVERTISING_LED_PIN_NO);

                // Configure buttons with sense level low as wakeup source.
                nrf_gpio_cfg_sense_input(WAKEUP_BUTTON_PIN,
                                         BUTTON_PULL,
                                         NRF_GPIO_PIN_SENSE_LOW);

                // Go to system-off mode (this function will not return; wakeup will cause a reset)
                err_code = sd_power_system_off();
                APP_ERROR_CHECK(err_code);
            }
            break;

        default:
            // No implementation needed.
            break;
    }
}


/**@brief Function for dispatching a BLE stack event to all modules with a BLE stack event handler.
 *
 * @details This function is called from the scheduler in the main loop after a BLE stack
 *          event has been received.
 *
 * @param[in]   p_ble_evt   Bluetooth stack event.
 */
static void ble_evt_dispatch(ble_evt_t * p_ble_evt)
{
    on_ble_evt(p_ble_evt);
    ble_conn_params_on_ble_evt(p_ble_evt);
    ble_lbs_on_ble_evt(&m_lbs, p_ble_evt);
}


/**@brief Function for dispatching a system event to interested modules.
 *
 * @details This function is called from the System event interrupt handler after a system
 *          event has been received.
 *
 * @param[in]   sys_evt   System stack event.
 */
static void sys_evt_dispatch(uint32_t sys_evt)
{
    pstorage_sys_event_handler(sys_evt);
}


/**@brief Function for initializing the BLE stack.
 *
 * @details Initializes the SoftDevice and the BLE event interrupt.
 */
static void ble_stack_init(void)
{
    uint32_t err_code;
    static ble_gap_addr_t                   addr;
    // Initialize the SoftDevice handler module.
    SOFTDEVICE_HANDLER_INIT(NRF_CLOCK_LFCLKSRC_XTAL_20_PPM, false);

    // Enable BLE stack
    ble_enable_params_t ble_enable_params;
    memset(&ble_enable_params, 0, sizeof(ble_enable_params));
    ble_enable_params.gatts_enable_params.service_changed = IS_SRVC_CHANGED_CHARACT_PRESENT;
    err_code = sd_ble_enable(&ble_enable_params);
    APP_ERROR_CHECK(err_code);
    sd_ble_gap_address_get(&addr);
    sd_ble_gap_address_set(BLE_GAP_ADDR_CYCLE_MODE_NONE, &addr);
    // Subscribe for BLE events.
    err_code = softdevice_ble_evt_handler_set(ble_evt_dispatch);
    APP_ERROR_CHECK(err_code);

    // Register with the SoftDevice handler module for BLE events.
    err_code = softdevice_sys_evt_handler_set(sys_evt_dispatch);
    APP_ERROR_CHECK(err_code);
}


/**@brief Function for the Event Scheduler initialization.
 */
static void scheduler_init(void)
{
    APP_SCHED_INIT(SCHED_MAX_EVENT_DATA_SIZE, SCHED_QUEUE_SIZE);
}

/**@brief Function for initializing the GPIOTE handler module.
 */
static void gpiote_init(void)
{
    APP_GPIOTE_INIT(APP_GPIOTE_MAX_USERS);
}

/**@brief Function for the Power manager.
 */
static void power_manage(void)
{
    uint32_t err_code = sd_app_evt_wait();
    APP_ERROR_CHECK(err_code);
}


/*

  MEAT OF PROGRAM STARTS HERE.  BOILERPLATE ABOVE.  DOMAIN LOGIC BELOW.

*/

typedef enum {
  DBG_OK,
  DBG_ERR,
  DBG_RESET
} debug_state_t;

void mag_insert_whoami(void);
void acc_insert_whoami(void);
void toggle_lights(void);

// lock characteristic write handler
static void led_write_handler(ble_lbs_t * p_lbs, uint8_t led_state)
{
  if (led_state == 0){
    send_lock_command(M_FORWARD);
    nrf_gpio_pin_set(SOLAR_EN);
    ble_lbs_send_debug_state(&m_lbs, 0x66);
  } else if (led_state == 1) {
    send_lock_command(M_BACKWARD);
    nrf_gpio_pin_set(SOLAR_EN);
    ble_lbs_send_debug_state(&m_lbs, 0x77);
  } else if (led_state == 2) {
    // STOP MOTOR AND RESET STATE MACHINE
    nrf_gpio_pin_clear(SOLAR_EN);
    send_lock_command(M_RECOVER_ERROR);
    ble_lbs_send_debug_state(&m_lbs, 0x88);
  } else if (led_state == 3) {
    // calibrate
    nrf_gpio_pin_set(SOLAR_EN);
    send_lock_command(M_CALIBRATE);
    ble_lbs_send_debug_state(&m_lbs, 0x99);
  } else if (led_state == 4){
      mag_insert_whoami();
  } else if (led_state == 5){
      acc_insert_whoami();
  } else if (led_state == 6){
      toggle_lights();
  } else {
      //nothing
  }
}

static void motor_voltage_adc_timeout_handler(void * p_context)
{
    UNUSED_PARAMETER(p_context);
    servo_start(voltage_callback);
}

/**@brief Function for the Timer initialization.
 *
 * @details Initializes the timer module.
 */

static void timers_init(void)
{
  uint32_t err_code;

  // Initialize timer module, making it use the scheduler
  APP_TIMER_INIT(APP_TIMER_PRESCALER, APP_TIMER_MAX_TIMERS, APP_TIMER_OP_QUEUE_SIZE, true);

  // Create timers.
  err_code = app_timer_create(&m_servo_timer_id,
                              APP_TIMER_MODE_REPEATED,
                              motor_voltage_adc_timeout_handler);
  APP_ERROR_CHECK(err_code);
}


/**@brief Function for starting timers.
*/



static void button_event_handler(uint8_t pin_no, uint8_t button_action)
{
    uint32_t err_code;

    switch (pin_no)
    {
        case CAPSENSE_INT_PIN:
          dispatch_capsense();
          //err_code = ble_lbs_on_button_change(&m_lbs, button_action);
          err_code = NRF_SUCCESS;
            if (err_code != NRF_SUCCESS &&
                err_code != BLE_ERROR_INVALID_CONN_HANDLE &&
                err_code != NRF_ERROR_INVALID_STATE)
            {
                APP_ERROR_CHECK(err_code);
            }
            break;

        default:
            APP_ERROR_HANDLER(pin_no);
            break;
    }
}


/**@brief Function for initializing the button handler module.
 */
static void buttons_init(void)
{
    // Note: Array must be static because a pointer to it will be saved in the Button handler
    //       module.
    static app_button_cfg_t buttons[] =
    {
        {WAKEUP_BUTTON_PIN, false, BUTTON_PULL, NULL},
        {CAPSENSE_INT_PIN, false, NRF_GPIO_PIN_NOPULL, button_event_handler}
    };

    APP_BUTTON_INIT(buttons, sizeof(buttons) / sizeof(buttons[0]), BUTTON_DETECTION_DELAY, true);
}




// callback from state machine
void lock_state_changed(LOCK_SM_STATES state, void* cb_data)
{
  debug_state_t dbg = DBG_OK;
  UNUSED_PARAMETER(cb_data);
  //nrf_gpio_pin_clear(SOLAR_EN);
  if(state == S_DONE){
    lock_sm_reset();
  } else {
    //FIXME do the right thing here
    dbg = DBG_ERR;
    //APP_ERROR_HANDLER(0);
  }
  ble_lbs_send_debug_state(&m_lbs, dbg);
}

void quad_update_event(int32_t num)
{
  //static int send_count = 0;
  //uint8_t     count = num & 0xff;
  uint32_t    err_code = NRF_SUCCESS;
  UNUSED_PARAMETER(err_code);

  pos_callback(num);
}

static int RNG(uint8_t *p_dest, unsigned p_size)
{
  UNUSED_PARAMETER(p_dest);
  UNUSED_PARAMETER(p_size);
  return 42;
}

void crypto_init(void)
{
  uECC_set_rng(&RNG);
}

static void motor_mag_init(void)
{
  nrf_gpio_cfg_output(MOTOR_MAG_SW);
}

// side effect -- enables main i2c bus.  Fucking gerardo.
static void enable_motor_mag(void)
{
  nrf_gpio_pin_clear(MOTOR_MAG_SW);
}

static void disable_motor_mag(void)
{
  nrf_gpio_pin_set(MOTOR_MAG_SW);
}


// magnetometer detection

uint8_t magwho_in = 41;
void magwho_cb(I2C_SM_STATES state, void* notused)
{
  UNUSED_PARAMETER(notused);
  if (state == DONE){
    magwho = magwho_in;
  } else {
    magwho = 43;
  }
  ble_lbs_send_magwho_state(&m_lbs);
}
void wrote_magwho_cb(I2C_SM_STATES state, void* notused)
{
  UNUSED_PARAMETER(notused);
  if (state == DONE){
    read_i2c(1, MAG_I2C_ADDR, &magwho_in, 1,
             magwho_cb, NULL);
  } else {
    magwho = 44;
    ble_lbs_send_magwho_state(&m_lbs);
  }
}
void mag_insert_whoami(void)
{
  uint8_t cmd[] = "";
  write_i2c(1, MAG_I2C_ADDR, 0x0f, cmd, 0, false, wrote_magwho_cb, NULL);
}

// accelerometer detection

uint8_t accwho_in = 41;
void accwho_cb(I2C_SM_STATES state, void* notused)
{
  UNUSED_PARAMETER(notused);
  if (state == DONE){
    accwho = accwho_in;
  } else {
    accwho = 43;
  }
  uart_putstring("sending accwho\r\n");
  ble_lbs_send_accwho_state(&m_lbs);
  uart_putstring("sent accwho\r\n");
}
void wrote_accwho_cb(I2C_SM_STATES state, void* notused)
{
  UNUSED_PARAMETER(notused);
  if (state == DONE){
    read_i2c(2, ACCEL_I2C_ADDR, &accwho_in, 1,
             accwho_cb, NULL);
  } else {
    accwho = 44;
    uart_putstring("sending bad accwho\r\n");
    ble_lbs_send_accwho_state(&m_lbs);
    uart_putstring("sent bad accwho\r\n");
  }
}
void acc_insert_whoami(void)
{
  uint8_t cmd[] = "";
  write_i2c(2, ACCEL_I2C_ADDR, 0x0f, cmd, 0, false, wrote_accwho_cb, NULL);
}

// cap sense test

static uint8_t lights_on_cmd[] = {0xFC,0xFC,0xFC,0xFC,0xFC};
void lights_on(void) // lights up
{
  write_i2c(1, CAP_I2C_ADDR, 0x73, lights_on_cmd, 5, true, NULL, NULL);
  //nrf_delay_us(5000);
}

static uint8_t lights_off_cmd[] = {0xFF};
void lights_off(void)
{
  write_i2c(1, CAP_I2C_ADDR, 0x79, lights_off_cmd, 1, true, NULL, NULL);
}

void toggle_lights(void)
{
  static bool state = true;
  if(state){
    lights_on();
  } else {
    lights_off();
  }
  state = !state;
}

//typedef void (*i2c_notify_cb)(I2C_SM_STATES, void*);

static void hw_init_3(I2C_SM_STATES state, void* cb_data){
  nrf_gpio_pin_set(SOLAR_EN);
  kill_motor();
  nrf_gpio_pin_clear(SOLAR_EN);
  uart_putstring("booted");
}

static void hw_init_2(void* p_event_data, uint16_t event_size){
  enable_capsense(hw_init_3);
  uart_putstring("capsense\r\n");

  nrf_gpio_pin_clear(SOLAR_EN);
}


void lock_hardware_init(void)
{
  uint32_t err_code;
  UNUSED_PARAMETER(err_code);

  nrf_gpio_cfg_output(SOLAR_EN);
  nrf_gpio_pin_set(SOLAR_EN);

  nrf_gpio_cfg_output(ACCEL_CS);
  nrf_gpio_cfg_output(ACCEL_SAD);
  nrf_gpio_pin_set(ACCEL_CS); // enable i2c mode on accelerometer
  nrf_gpio_pin_clear(ACCEL_SAD); // set accelerometer i2c addr to 0x18

  uart_config(TX_PIN_NUMBER, RX_PIN_NUMBER);
  uart_putstring("configured UART\r\n");

  motor_mag_init();
  enable_motor_mag();
  motor_quad_init(quad_update_event);

  uart_putstring("configured quad\r\n");

  //uart_putstring("uart configured\r\n");
  (void)sm_twi_master_init(1, &err_code);
  setup_i2c_ints(1);
  (void)sm_twi_master_init(2, &err_code);
  setup_i2c_ints(2);
  //uart_putstring("i2c configured\r\n");
  uart_putstring("... i2c\r\n");


  lock_sm_init(get_i2c(1), lock_state_changed, NULL, m_servo_timer_id);
  //uart_putstring("lock sm configured\r\n");

  uart_putstring("...lock sm\r\n");


  //lights_on();
}

void lock_hardware_init_2(void)
{
  //uart_putstring("chaining ...\r\n");
  // chain together anything that needs I2C
  app_sched_event_put(NULL, 0, hw_init_2);
}

/**@brief Function for application main entry.
 */
int main(void)
{
  for(int i=0; i<10; i++){};

    // Initialize
    leds_init();
    timers_init();
    gpiote_init();
    buttons_init();
    crypto_init();
    ble_stack_init();
    scheduler_init();
    gap_params_init();
    services_init();
    advertising_init();
    conn_params_init();
    sec_params_init();
    lock_hardware_init();

    // Start execution
    //timers_start(); // only using one timer, in servo_adc now
    advertising_start();
    lock_hardware_init_2();

    //https://devzone.nordicsemi.com/question/17526/examples-using-the-scheduler/

    /*

      Need to init sheduler in main() like this:

      APP_SCHED_INIT(SCHED_MAX_EVENT_DATA_SIZE, SCHED_QUEUE_SIZE);

      in main() need insert this code:

      while(1) { app_sched_execute(); ... }

      in app_sheduler.h we have Scheduler event handler type:

      typedef void (*app_sched_event_handler_t)(void * p_event_data, uint16_t event_size);

      Declare own function like than typedef:

      void my_flash_event_func(void *data, uint16_t size); //this is event handler

      In any place where it is need, we call:

      app_sched_event_put(pointer_to_own_data, size_of_data, my_flash_event_func); //this is event

      And sheduler will call your function "my_flash_event_func" with params: "pointer_to_own_data" and "size_of_data" like this:

      my_flash_event_func(pointer_to_data, size_of_data);

     */

    // Enter main loop
    for (;;)
    {
        app_sched_execute();
        power_manage();
    }
}

/**
 * @}
 */
