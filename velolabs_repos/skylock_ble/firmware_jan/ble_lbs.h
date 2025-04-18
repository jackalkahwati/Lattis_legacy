#ifndef BLE_LBS_H__
#define BLE_LBS_H__

#include <stdint.h>
#include <stdbool.h>
#include "ble.h"
#include "ble_srv_common.h"

// LBS: "LED Button Service" -> "Lock, Bike, Skylock"
//#define kLockServiceUUID "\xdd\xea\x70\x6a\x9d\x53\x4b\xbb\xac\x0b\x74\xba\x81\x9e\x7d\x9c";
//#define kLockCharacteristicUUID = "\xf1\xc7\xc1\x02\x27\xbc\x40\x74\xae\xe6\x35\xc5\x8a\x3b\x31\xf6";

#define LBS_UUID_BASE {0xdd, 0xea, 0x70, 0x6a, 0x9d, 0x53, 0x4b, 0xbb, 0xac, 0x0b, 0x74, 0xba, 0x81, 0x9e, 0x7d, 0x9c}
//#define LBS_UUID_BASE {0x23, 0xD1, 0xBC, 0xEA, 0x5F, 0x78, 0x23, 0x15, 0xDE, 0xEF, 0x12, 0x12, 0x00, 0x00, 0x00, 0x00}
#define LBS_UUID_SERVICE 0x1523
#define LBS_UUID_BUTTON_CHAR 0x1524
#define LBS_UUID_LED_CHAR 0x1525
#define LBS_UUID_DEBUG_CHAR 0x1526
#define LBS_UUID_QUAD_CHAR 0x1527
#define LBS_UUID_MAGWHO_CHAR 0x1528
#define LBS_UUID_ACCWHO_CHAR 0x1529
#define LBS_UUID_VOLTAGE_CHAR 0x152A
#define LBS_UUID_LOCKSM_CHAR 0x152B
#define LBS_UUID_LOCK_POS_CHAR 0x152C
#define LBS_UUID_LOCK_BACK_CHAR 0x152D
#define LBS_UUID_LOCK_FOR_CHAR 0x152E

// Forward declaration of the ble_lbs_t type.
typedef struct ble_lbs_s ble_lbs_t;

typedef void (*ble_lbs_led_write_handler_t) (ble_lbs_t * p_lbs, uint8_t new_state);

typedef struct
{
    ble_lbs_led_write_handler_t led_write_handler;                    /**< Event handler to be called when LED characteristic is written. */
} ble_lbs_init_t;

/**@brief LED Button Service structure. This contains various status information for the service. */
typedef struct ble_lbs_s
{
  uint16_t                    service_handle;
  ble_gatts_char_handles_t    led_char_handles;
  ble_gatts_char_handles_t    button_char_handles;
  ble_gatts_char_handles_t    debug_char_handles;
  ble_gatts_char_handles_t    quad_char_handles;
  ble_gatts_char_handles_t    magwho_char_handles;
  ble_gatts_char_handles_t    accwho_char_handles;
  ble_gatts_char_handles_t    voltage_char_handles;
  ble_gatts_char_handles_t    locksm_char_handles;
  ble_gatts_char_handles_t    lock_pos_char_handles;
  ble_gatts_char_handles_t    lock_back_char_handles;
  ble_gatts_char_handles_t    lock_for_char_handles;

  uint8_t                     uuid_type;
  uint16_t                    conn_handle;
  ble_lbs_led_write_handler_t led_write_handler;
} ble_lbs_t;

extern uint8_t magwho;
extern uint8_t accwho;

/**@brief Function for initializing the LED Button Service.
 *
 * @param[out]  p_lbs       LED Button Service structure. This structure will have to be supplied by
 *                          the application. It will be initialized by this function, and will later
 *                          be used to identify this particular service instance.
 * @param[in]   p_lbs_init  Information needed to initialize the service.
 *
 * @return      NRF_SUCCESS on successful initialization of service, otherwise an error code.
 */
uint32_t ble_lbs_init(ble_lbs_t * p_lbs, const ble_lbs_init_t * p_lbs_init);

/**@brief Function for handling the Application's BLE Stack events.
 *
 * @details Handles all events from the BLE stack of interest to the LED Button Service.
 *
 *
 * @param[in]   p_lbs      LED Button Service structure.
 * @param[in]   p_ble_evt  Event received from the BLE stack.
 */
void ble_lbs_on_ble_evt(ble_lbs_t * p_lbs, ble_evt_t * p_ble_evt);

/**@brief Function for sending a button state notification.
 */
uint32_t ble_lbs_on_button_change(ble_lbs_t * p_lbs, uint8_t button_state);
uint32_t ble_lbs_send_debug_state(ble_lbs_t * p_lbs, uint8_t debug_state);
uint32_t ble_lbs_send_quad_state(ble_lbs_t * p_lbs, int32_t quad_state);
uint32_t ble_lbs_send_accwho_state(ble_lbs_t * p_lbs);
uint32_t ble_lbs_send_magwho_state(ble_lbs_t * p_lbs);
uint32_t ble_lbs_send_voltage_state(ble_lbs_t * p_lbs, uint8_t voltage_state);
uint32_t ble_lbs_send_locksm_state(ble_lbs_t * p_lbs, uint8_t locksm_state);
uint32_t ble_lbs_send_lock_pos_state(ble_lbs_t * p_lbs, int32_t lock_pos_state);
uint32_t ble_lbs_send_lock_back_state(ble_lbs_t * p_lbs, int32_t lock_back_state);
uint32_t ble_lbs_send_lock_for_state(ble_lbs_t * p_lbs, int32_t lock_for_state);

#endif // BLE_LBS_H__

/** @} */
