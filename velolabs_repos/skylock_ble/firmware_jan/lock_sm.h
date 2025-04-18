#ifndef __lock_sm__h__
#define __lock_sm__h__

#include "i2c_sm.h"
#include "ringbuf.h"

typedef enum {
  WAITING,                   // 0
  COMMAND_FOR,               // 1
  COMMAND_BACK,              // 2
  COMMAND_CALIBRATE,         // 3
  CALIB_FORSEEK,             // 4
  CALIB_FOR_WAITBRAKE,       // 5
  CALIB_BACKSEEK,            // 6
  CALIB_BACK_WAITBRAKE,      // 7
  WRITE_FAULT_CHECK_FOR,     // 8
  WRITE_FAULT_CHECK_BACK,    // 9
  CHECK_FAULT_FOR,           // A
  CHECK_FAULT_BACK,          // B
  S_ERROR,                   // C
  S_DONE,                    // D
  S_BAILOUT,                 // E
  S_DELAYCOUNT,              // F
  S_DELAYCOUNT2,             // 10
} LOCK_SM_STATES;

typedef enum {
  E_UNDERFLOW,
  E_OVERFLOW,
  E_FAULT,
  E_STALLED,
  E_NONE
} LOCK_ERR_REASONS;

typedef enum {
  M_OFF = 0,
  M_FORWARD = 1,
  M_BACKWARD = 2,
  M_BRAKE = 3,
  M_NONE = 4,
  M_RECOVER_ERROR = 5,
  M_CALIBRATE = 6,
} MOTOR_STATES;

typedef void (*lock_notify_cb)(LOCK_SM_STATES, void*);

typedef struct {
  LOCK_SM_STATES state;
  MOTOR_STATES command;
  I2C_SM* i2c_sm;
  int32_t lock_pos;
  int32_t calib_lock_pos;
  uint8_t motor_voltage_same_count; // motor voltage same == stall?
  uint8_t pos_delta_low_count; // position delta low == stall?
  int32_t lock_delta; // movement in the last 1 ms
  uint8_t motor_voltage; // adc output
  uint32_t millis; // incremented once a millisecond by voltage callback while motor on
  int32_t forward_stop;
  int32_t backward_stop;
  uint8_t lock_i2c_addr;
  bool calibrating;
  bool calib_braking;
  lock_notify_cb notify;
  void* cb_data;
  uint8_t fault_data[2];
  LOCK_ERR_REASONS reason;
  uint8_t volt_rb_store[20];
  ringbuffer volt_rb;
  uint8_t pos_rb_store[20];
  ringbuffer pos_rb;
  bool io_done;

} LOCK_SM;

void kill_motor(void);
bool lock_sm_init(I2C_SM* i2c, lock_notify_cb cb, void* cb_data, app_timer_id_t servo_tid);
void lock_sm_reset(void);
void advance_motor_sm(void);
void pos_callback(int32_t pos);
void voltage_callback(uint8_t val);
bool send_lock_command(MOTOR_STATES cmd);

LOCK_SM_STATES lock_sm_state(void);

#endif
