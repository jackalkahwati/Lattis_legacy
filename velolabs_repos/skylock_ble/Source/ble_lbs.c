/* Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the license.txt file.
 */

#include <ble_gatts.h>
#include <string.h>
#include "nordic_common.h"
#include "ble_srv_common.h"
#include "app_util.h"
#include "ble_lbs.h"

#define HTONL(n) (((((unsigned long)(n) & 0xFF)) << 24) | \
                  ((((unsigned long)(n) & 0xFF00)) << 8) | \
                  ((((unsigned long)(n) & 0xFF0000)) >> 8) | \
                  ((((unsigned long)(n) & 0xFF000000)) >> 24))

/**@brief Function for handling the Connect event.
 *
 * @param[in]   p_lbs       LED Button Service structure.
 * @param[in]   p_ble_evt   Event received from the BLE stack.
 */
static void on_connect(ble_lbs_t * p_lbs, ble_evt_t * p_ble_evt)
{
    p_lbs->conn_handle = p_ble_evt->evt.gap_evt.conn_handle;
}


/**@brief Function for handling the Disconnect event.
 *
 * @param[in]   p_lbs       LED Button Service structure.
 * @param[in]   p_ble_evt   Event received from the BLE stack.
 */
static void on_disconnect(ble_lbs_t * p_lbs, ble_evt_t * p_ble_evt)
{
    UNUSED_PARAMETER(p_ble_evt);
    p_lbs->conn_handle = BLE_CONN_HANDLE_INVALID;
}


/**@brief Function for handling the Write event.
 *
 * @param[in]   p_lbs       LED Button Service structure.
 * @param[in]   p_ble_evt   Event received from the BLE stack.
 */
static void on_write(ble_lbs_t * p_lbs, ble_evt_t * p_ble_evt)
{
    ble_gatts_evt_write_t * p_evt_write = &p_ble_evt->evt.gatts_evt.params.write;

printf("on write: %u %u\n", p_evt_write->len, p_evt_write->data[0]);
    if ((p_evt_write->handle == p_lbs->led_char_handles.value_handle) &&
        (p_evt_write->len == 1) &&
        (p_lbs->led_write_handler != NULL))
    {
        p_lbs->led_write_handler(p_lbs, p_evt_write->data[0]);
    }
}


void ble_lbs_on_ble_evt(ble_lbs_t * p_lbs, ble_evt_t * p_ble_evt)
{
    switch (p_ble_evt->header.evt_id)
    {
        case BLE_GAP_EVT_CONNECTED:
            on_connect(p_lbs, p_ble_evt);
            break;

        case BLE_GAP_EVT_DISCONNECTED:
            on_disconnect(p_lbs, p_ble_evt);
            break;

        case BLE_GATTS_EVT_WRITE:
            on_write(p_lbs, p_ble_evt);
            break;

        default:
            // No implementation needed.
            break;
    }
}

ble_gatts_char_pf_t ble_uint32_t = {
  .format = BLE_GATT_CPF_FORMAT_UINT32,
  .exponent = 0,
  .name_space = 1,
};
ble_gatts_char_pf_t ble_int32_t = {
  .format = BLE_GATT_CPF_FORMAT_SINT32,
  .exponent = 0,
  .name_space = 1,
};
ble_gatts_char_pf_t ble_uint16_t = {
  .format = BLE_GATT_CPF_FORMAT_UINT16,
  .exponent = 0,
  .name_space = 1,
};
ble_gatts_char_pf_t ble_int16_t = {
  .format = BLE_GATT_CPF_FORMAT_SINT16,
  .exponent = 0,
  .name_space = 1,
};
ble_gatts_char_pf_t ble_uint8_t = {
  .format = BLE_GATT_CPF_FORMAT_UINT8,
  .exponent = 0,
  .name_space = 1,
};
ble_gatts_char_pf_t ble_int8_t = {
  .format = BLE_GATT_CPF_FORMAT_SINT8,
  .exponent = 0,
  .name_space = 1,
};

static uint32_t ble_char_add(ble_lbs_t * p_lbs, const ble_lbs_init_t * p_lbs_init,
                             ble_gatts_char_handles_t *const  p_handles, uint16_t uuid,
                             uint8_t val_len, uint8_t* name, uint8_t name_len, bool write,
                             uint8_t* val_loc, ble_gatts_char_pf_t* pres_fmt)
{
    ble_gatts_char_md_t char_md;
    //ble_gatts_attr_md_t cccd_md;
    ble_gatts_attr_t    attr_char_value;
    ble_uuid_t          ble_uuid;
    ble_gatts_attr_md_t attr_md;

    //memset(&cccd_md, 0, sizeof(cccd_md));

    //BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.read_perm);
    //BLE_GAP_CONN_SEC_MODE_SET_OPEN(&cccd_md.write_perm);
    //cccd_md.vloc = BLE_GATTS_VLOC_STACK;

    memset(&char_md, 0, sizeof(char_md));

    char_md.char_props.read   = 1;
    if(write){
      char_md.char_props.write = 1;
    } else {
      char_md.char_props.notify = 1;
    }

    char_md.p_char_user_desc  = name;
    char_md.char_user_desc_max_size = name_len;
    char_md.char_user_desc_size =  name_len;
    char_md.p_char_pf         = pres_fmt;;
    char_md.p_user_desc_md    = NULL;

    //char_md.p_cccd_md         = &cccd_md;
    char_md.p_cccd_md         = NULL;

    char_md.p_sccd_md         = NULL;

    ble_uuid.type = p_lbs->uuid_type;
    ble_uuid.uuid = uuid;

    memset(&attr_md, 0, sizeof(attr_md));

    BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.read_perm);
    if(write){
      BLE_GAP_CONN_SEC_MODE_SET_OPEN(&attr_md.write_perm);
    } else {
      BLE_GAP_CONN_SEC_MODE_SET_NO_ACCESS(&attr_md.write_perm);
    }
    attr_md.vloc       = BLE_GATTS_VLOC_STACK;
    attr_md.rd_auth    = 0;
    attr_md.wr_auth    = 0;
    attr_md.vlen       = 0;

    memset(&attr_char_value, 0, sizeof(attr_char_value));

    attr_char_value.p_uuid       = &ble_uuid;
    attr_char_value.p_attr_md    = &attr_md;
    attr_char_value.init_len     = val_len;
    attr_char_value.init_offs    = 0;
    attr_char_value.max_len      = val_len;
    attr_char_value.p_value      = val_loc;

    return sd_ble_gatts_characteristic_add(p_lbs->service_handle, &char_md,
                                               &attr_char_value,
                                               p_handles);
}

static uint8_t led_char_name[] = "LOCK";
static uint8_t button_char_name[] = "BUTTON";
static uint8_t debug_char_name[] = "DEBUG";
static uint8_t quad_char_name[] = "QUAD";
static uint8_t magwho_char_name[] = "MAGWHO";
static uint8_t accwho_char_name[] = "ACCWHO";
static uint8_t voltage_char_name[] = "VOLTAGE";
static uint8_t locksm_char_name[] = "LOCKSM";
static uint8_t lock_pos_char_name[] = "LOCK_POS";
static uint8_t lock_back_char_name[] = "LOCK_BACK";
static uint8_t lock_for_char_name[] = "LOCK_FOR";

uint8_t magwho;
uint8_t accwho;

uint32_t ble_lbs_init(ble_lbs_t * p_lbs, const ble_lbs_init_t * p_lbs_init)
{
    uint32_t   err_code;
    ble_uuid_t ble_uuid;

    // Initialize service structure
    p_lbs->conn_handle       = BLE_CONN_HANDLE_INVALID;
    p_lbs->led_write_handler = p_lbs_init->led_write_handler;

    // Add service
    ble_uuid128_t base_uuid = {LBS_UUID_BASE};
    err_code = sd_ble_uuid_vs_add(&base_uuid, &p_lbs->uuid_type);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    ble_uuid.type = p_lbs->uuid_type;
    ble_uuid.uuid = LBS_UUID_SERVICE;

    err_code = sd_ble_gatts_service_add(BLE_GATTS_SRVC_TYPE_PRIMARY, &ble_uuid, &p_lbs->service_handle);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    // add button characteristic
    err_code = ble_char_add(p_lbs, p_lbs_init,
                            &p_lbs->button_char_handles, LBS_UUID_BUTTON_CHAR,
                            sizeof(uint8_t),
                            button_char_name, sizeof(button_char_name), false,
                            NULL, NULL);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    // add led (lock) characteristic
    err_code = ble_char_add(p_lbs, p_lbs_init,
                            &p_lbs->led_char_handles, LBS_UUID_LED_CHAR,
                            sizeof(uint8_t),
                            led_char_name, sizeof(led_char_name), true,
                            NULL, NULL);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    // add debug characteristic
    err_code = ble_char_add(p_lbs, p_lbs_init,
                            &p_lbs->debug_char_handles, LBS_UUID_DEBUG_CHAR,
                            sizeof(uint8_t),
                            debug_char_name, sizeof(debug_char_name), false,
                            NULL, NULL);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    // add quadrature characteristic
    err_code = ble_char_add(p_lbs, p_lbs_init,
                            &p_lbs->quad_char_handles, LBS_UUID_QUAD_CHAR,
                            sizeof(int32_t),
                            quad_char_name, sizeof(quad_char_name), false,
                            NULL, &ble_int32_t);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    //*
    magwho = 42;
    // add magwho characteristic
    err_code = ble_char_add(p_lbs, p_lbs_init,
                            &p_lbs->magwho_char_handles, LBS_UUID_MAGWHO_CHAR,
                            sizeof(uint8_t),
                            magwho_char_name, sizeof(magwho_char_name), false,
                            &magwho, NULL);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
    // */

    //*
    accwho = 42;
    // add magwho characteristic
    err_code = ble_char_add(p_lbs, p_lbs_init,
                            &p_lbs->accwho_char_handles, LBS_UUID_ACCWHO_CHAR,
                            sizeof(uint8_t),
                            accwho_char_name, sizeof(accwho_char_name), false,
                            &accwho, NULL);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
    // */

    // add voltage characteristic
    err_code = ble_char_add(p_lbs, p_lbs_init,
                            &p_lbs->voltage_char_handles, LBS_UUID_VOLTAGE_CHAR,
                            sizeof(uint8_t),
                            voltage_char_name, sizeof(voltage_char_name), false,
                            NULL, NULL);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    // add locksm characteristic
    err_code = ble_char_add(p_lbs, p_lbs_init,
                            &p_lbs->locksm_char_handles, LBS_UUID_LOCKSM_CHAR,
                            sizeof(uint8_t),
                            locksm_char_name, sizeof(locksm_char_name), false,
                            NULL, NULL);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }

    err_code = ble_char_add(p_lbs, p_lbs_init,
                            &p_lbs->lock_pos_char_handles, LBS_UUID_LOCK_POS_CHAR,
                            sizeof(int32_t),
                            lock_pos_char_name, sizeof(lock_pos_char_name), false,
                            NULL, &ble_int32_t);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
    err_code = ble_char_add(p_lbs, p_lbs_init,
                            &p_lbs->lock_back_char_handles, LBS_UUID_LOCK_BACK_CHAR,
                            sizeof(int32_t),
                            lock_back_char_name, sizeof(lock_back_char_name), false,
                            NULL, &ble_int32_t);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }
    err_code = ble_char_add(p_lbs, p_lbs_init,
                            &p_lbs->lock_for_char_handles, LBS_UUID_LOCK_FOR_CHAR,
                            sizeof(int32_t),
                            lock_for_char_name, sizeof(lock_for_char_name), false,
                            NULL, &ble_int32_t);
    if (err_code != NRF_SUCCESS)
    {
        return err_code;
    }


    return NRF_SUCCESS;
}

uint32_t ble_lbs_on_button_change(ble_lbs_t * p_lbs, uint8_t button_state)
{
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(button_state);

    memset(&params, 0, sizeof(params));
    params.type = BLE_GATT_HVX_NOTIFICATION;
    params.handle = p_lbs->button_char_handles.value_handle;
    params.p_data = &button_state;
    params.p_len = &len;

    return sd_ble_gatts_hvx(p_lbs->conn_handle, &params);
}

uint32_t ble_lbs_send_debug_state(ble_lbs_t * p_lbs, uint8_t debug_state)
{
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(debug_state);

    memset(&params, 0, sizeof(params));
    params.type = BLE_GATT_HVX_NOTIFICATION;
    params.handle = p_lbs->debug_char_handles.value_handle;
    params.p_data = &debug_state;
    params.p_len = &len;

    return sd_ble_gatts_hvx(p_lbs->conn_handle, &params);
}

uint32_t ble_lbs_send_accwho_state(ble_lbs_t * p_lbs)
{
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(accwho);

    memset(&params, 0, sizeof(params));
    params.type = BLE_GATT_HVX_NOTIFICATION;
    params.handle = p_lbs->accwho_char_handles.value_handle;
    params.p_data = &accwho;
    params.p_len = &len;

    return sd_ble_gatts_hvx(p_lbs->conn_handle, &params);
}

uint32_t ble_lbs_send_magwho_state(ble_lbs_t * p_lbs)
{
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(magwho);

    memset(&params, 0, sizeof(params));
    params.type = BLE_GATT_HVX_NOTIFICATION;
    params.handle = p_lbs->magwho_char_handles.value_handle;
    params.p_data = &magwho;
    params.p_len = &len;

    return sd_ble_gatts_hvx(p_lbs->conn_handle, &params);
}

static int32_t net_quad_state;
uint32_t ble_lbs_send_quad_state(ble_lbs_t * p_lbs, int32_t quad_state)
{
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(quad_state);

    memset(&params, 0, sizeof(params));
    params.type = BLE_GATT_HVX_NOTIFICATION;
    params.handle = p_lbs->quad_char_handles.value_handle;
    net_quad_state = HTONL(quad_state);
    params.p_data = (void*)&net_quad_state;
    params.p_len = &len;

    return sd_ble_gatts_hvx(p_lbs->conn_handle, &params);
}

static int32_t net_pos_state;
uint32_t ble_lbs_send_lock_pos_state(ble_lbs_t * p_lbs, int32_t lock_pos_state)
{
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(lock_pos_state);

    memset(&params, 0, sizeof(params));
    params.type = BLE_GATT_HVX_NOTIFICATION;
    params.handle = p_lbs->lock_pos_char_handles.value_handle;
    net_pos_state = HTONL(lock_pos_state);
    params.p_data = (void*)&net_pos_state;
    params.p_len = &len;

    return sd_ble_gatts_hvx(p_lbs->conn_handle, &params);
}

uint32_t ble_lbs_send_voltage_state(ble_lbs_t * p_lbs, uint8_t voltage_state)
{
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(voltage_state);

    memset(&params, 0, sizeof(params));
    params.type = BLE_GATT_HVX_NOTIFICATION;
    params.handle = p_lbs->voltage_char_handles.value_handle;
    params.p_data = &voltage_state;
    params.p_len = &len;

    return sd_ble_gatts_hvx(p_lbs->conn_handle, &params);
}

uint32_t ble_lbs_send_locksm_state(ble_lbs_t * p_lbs, uint8_t locksm_state)
{
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(locksm_state);

    memset(&params, 0, sizeof(params));
    params.type = BLE_GATT_HVX_NOTIFICATION;
    params.handle = p_lbs->locksm_char_handles.value_handle;
    params.p_data = &locksm_state;
    params.p_len = &len;

    return sd_ble_gatts_hvx(p_lbs->conn_handle, &params);
}

uint32_t ble_lbs_send_lock_back_state(ble_lbs_t * p_lbs, int32_t lock_back_state)
{
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(lock_back_state);

    memset(&params, 0, sizeof(params));
    params.type = BLE_GATT_HVX_NOTIFICATION;
    params.handle = p_lbs->lock_back_char_handles.value_handle;
    params.p_data = (void*)&lock_back_state;
    params.p_len = &len;

    return sd_ble_gatts_hvx(p_lbs->conn_handle, &params);
}

uint32_t ble_lbs_send_lock_for_state(ble_lbs_t * p_lbs, int32_t lock_for_state)
{
    ble_gatts_hvx_params_t params;
    uint16_t len = sizeof(lock_for_state);

    memset(&params, 0, sizeof(params));
    params.type = BLE_GATT_HVX_NOTIFICATION;
    params.handle = p_lbs->lock_for_char_handles.value_handle;
    params.p_data = (void*)&lock_for_state;
    params.p_len = &len;

    return sd_ble_gatts_hvx(p_lbs->conn_handle, &params);
}
