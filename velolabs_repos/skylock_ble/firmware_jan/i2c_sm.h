#ifndef __i2c_sm__h__
#define __i2c_sm__h__

#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>
#include <nrf.h>

typedef enum {
  STARTED,
  READING,
  CLEANUP,
  ERROR,
  DONE,
  ABORTED,
  STOPPING,
} I2C_SM_STATES;

typedef void (*i2c_notify_cb)(I2C_SM_STATES, void*);

typedef struct {
  NRF_TWI_Type* TWI;
  uint8_t busno;
  I2C_SM_STATES state;
  I2C_SM_STATES err_next;
  uint8_t address;
  uint8_t data_length;
  uint8_t* data;
  uint8_t* r_data;
  uint8_t r_data_length;
  bool abort;
  bool issue_stop;
  bool read_mode;
  i2c_notify_cb notify;
  void* cb_data;
} I2C_SM;

bool sm_twi_master_init(uint8_t busno, uint32_t* err_code);
uint32_t setup_i2c_ints(uint8_t busno);

uint32_t write_i2c(uint8_t busno, uint8_t address, uint8_t reg, uint8_t* data,
                   uint8_t data_length, bool issue_stop,
                   i2c_notify_cb cb, void* cb_data);

uint32_t read_i2c(uint8_t busno, uint8_t address, uint8_t* data, uint8_t data_length,
                   i2c_notify_cb cb, void* cb_data);

I2C_SM* get_i2c(uint8_t busno);
I2C_SM_STATES i2c_sm_state(uint8_t busno);

#endif
