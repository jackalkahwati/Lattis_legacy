#ifndef __SERVO_ADC_H__
#define __SERVO_ADC_H__
#include <stdint.h>
#include "app_timer.h"
#include "common_defines.h"

typedef void(*servo_pos_cb)(uint8_t pos);

void servo_set_timerid(app_timer_id_t tid);
void servo_start(servo_pos_cb cb);
void servo_adc_enable(void);
void servo_adc_disable(void);
#endif
