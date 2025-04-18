#include <stdint.h>
#include <string.h>
#include "nordic_common.h"
#include "nrf.h"
#include "app_error.h"
#include "nrf_gpio.h"
#include "nrf51_bitfields.h"
#include "softdevice_handler.h"
#include "servo_adc.h"
#include "app_util.h"
#include "ble_lbs.h"

#include "skylock_gpio.h"

extern ble_lbs_t m_lbs;

servo_pos_cb servo_cb;

static void adc_handler(void* p_event_data, uint16_t event_size)
{
  uint8_t adc_result = *((uint8_t*)p_event_data);
  if(servo_cb != NULL){
    servo_cb(adc_result);
  }
}

void ADC_IRQHandler(void)
{
  uint8_t     adc_result;
  if (NRF_ADC->EVENTS_END != 0) {
      NRF_ADC->EVENTS_END     = 0;
      adc_result              = NRF_ADC->RESULT;
      NRF_ADC->TASKS_STOP     = 1;
      // bounce us out of interrupt space and back into the main loop
      app_sched_event_put(&adc_result, sizeof(adc_result), adc_handler);
    }
}

void servo_start(servo_pos_cb cb)
{
  servo_cb = cb;
  uint32_t err_code;

  // Configure ADC
  NRF_ADC->INTENSET   = ADC_INTENSET_END_Msk;

  NRF_ADC->CONFIG = (ADC_CONFIG_EXTREFSEL_None << ADC_CONFIG_EXTREFSEL_Pos)                 /* Bits 17..16 : ADC external reference pin selection. */
    | (SERVO_ADC_ANALOG_INPUT << ADC_CONFIG_PSEL_Pos)                 /*!< Use analog input 2 as analog input (P0.01). */
    | (ADC_CONFIG_REFSEL_VBG << ADC_CONFIG_REFSEL_Pos)                      /*!< Use internal 1.2V bandgap voltage as reference for conversion. */
    | (ADC_CONFIG_INPSEL_AnalogInputOneThirdPrescaling << ADC_CONFIG_INPSEL_Pos)  /*!< Analog input specified by PSEL with 1/3 prescaling used as input for the conversion. */
    | (ADC_CONFIG_RES_8bit << ADC_CONFIG_RES_Pos);                          /*!< 8bit ADC resolution. */



  NRF_ADC->EVENTS_END = 0;
  NRF_ADC->ENABLE     = ADC_ENABLE_ENABLE_Enabled;

  // Enable ADC interrupt
  err_code = sd_nvic_ClearPendingIRQ(ADC_IRQn);
  APP_ERROR_CHECK(err_code);

  err_code = sd_nvic_SetPriority(ADC_IRQn, NRF_APP_PRIORITY_LOW);
  APP_ERROR_CHECK(err_code);

  err_code = sd_nvic_EnableIRQ(ADC_IRQn);
  APP_ERROR_CHECK(err_code);

  NRF_ADC->EVENTS_END  = 0;    // Stop any running conversions.
  NRF_ADC->TASKS_START = 1;
}

app_timer_id_t servo_timer_id;
void servo_set_timerid(app_timer_id_t tid)
{
  servo_timer_id = tid;
}


#define SERVO_MEAS_INTERVAL             APP_TIMER_TICKS(1, APP_TIMER_PRESCALER) // 1 ms

void servo_adc_enable(void)
{
  NRF_ADC->ENABLE     = ADC_ENABLE_ENABLE_Enabled;
  NRF_ADC->EVENTS_END  = 0;    // Stop any running conversions.
  NRF_ADC->TASKS_START = 1;

  uint32_t err_code;
  err_code = app_timer_start(servo_timer_id, SERVO_MEAS_INTERVAL, NULL);
  APP_ERROR_CHECK(err_code);
}

void servo_adc_disable(void)
{
  app_timer_stop(servo_timer_id);

  NRF_ADC->ENABLE     = ADC_ENABLE_ENABLE_Disabled;
  NRF_ADC->EVENTS_END  = 0;    // Stop any running conversions.
  NRF_ADC->TASKS_START = 0;
}
