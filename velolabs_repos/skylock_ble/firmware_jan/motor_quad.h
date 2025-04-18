 /* Copyright (c) 2009 Nordic Semiconductor. All Rights Reserved.
 *
 * The information contained herein is confidential property of Nordic
 * Semiconductor ASA.Terms and conditions of usage are described in detail
 * in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 *
 * Licensees are granted free, non-transferable use of the information. NO
 * WARRANTY of ANY KIND is provided. This heading must NOT be removed from
 * the file.
 *
 */

#ifndef __motor_quad__h__
#define __motor_quad__h__

#include <stdbool.h>
#include <stdint.h>

#include "nrf.h"
#include "nrf51_bitfields.h"


/**@brief Scroll wheel initialization.
 *
 * @param[in] scroll_event_cb Scroll event callback. NOTE: might be called from interrupt context!
 * @return
 * @retval NRF_SUCCESS
 * @retval NRF_ERROR_INVALID_PARAM
 */
uint32_t motor_quad_init(void (*scroll_event_cb)(int32_t));

/**@brief Utility function to see if scroll pins has set off PORT GPIOTE interrupt
 *
 * @return true scroll pins are in a state to trigger PORT interrupt
 */
bool motor_quad_int_set(void);

/**@brief Enable scroll wheel.
 *
 * @return
 * @retval NRF_SUCCESS
 */
uint32_t motor_quad_enable(void);

/**@brief Enable scroll wheel.
 */
void motor_quad_disable(void);

/**@brief Prepare for sleep and subsequent wakeup.
 */
void motor_quad_wakeup_prepare(void);

#endif
