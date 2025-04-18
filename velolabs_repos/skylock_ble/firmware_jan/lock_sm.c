#include "app_timer.h"
#include "lock_sm.h"
#include "motor_quad.h"
#include "servo_adc.h"
#include "uart.h"
#include "skylock_gpio.h"
#include "ble_lbs.h"
#include "stdlib.h"

#define LOCK_5V 0x3f
const uint8_t STALL_THRESHOLD = 8;
const uint8_t POS_STALL_THRESHOLD = 9;
const uint8_t STALL_CEILING = 0xC1;




LOCK_SM lock_motor;

/*
  new way to handle state machine:
  motor command starts voltage adc reporting
  voltage adc reporting drives state machine
  notification stops voltage adc reporting

 */
void notify_caller(void){
  motor_quad_disable();
  servo_adc_disable();
  if(lock_motor.notify != NULL){
    lock_motor.notify(lock_motor.state, lock_motor.cb_data);
  }
}

void i2c_callback(I2C_SM_STATES last_state, void* cb_data)
{
  lock_motor.io_done = true;

  /*
  if(last_state == DONE){
    advance_motor_sm();
  } else {
    kill_motor();
    lock_motor.state = S_ERROR;
    advance_motor_sm();
  }
  */
}

static void calib_braking_cb(I2C_SM_STATES last_state, void* cb_data)
{
  lock_motor.io_done = true;
  lock_motor.calib_braking = false;
  lock_motor.millis = 0;
  //advance_motor_sm();
}

extern ble_lbs_t                        m_lbs;

static uint8_t brake_cmd[] = {M_BRAKE};
static uint8_t stop_cmd[] = {M_OFF};
void voltage_callback(uint8_t val)
{
  static uint8_t pocount = 0;
  lock_motor.millis++;
  switch(lock_motor.state){
  case WAITING:
  case S_ERROR:
  case S_DONE:
    break;
  default:
    if( lock_motor.pos_delta_low_count > POS_STALL_THRESHOLD) {
      magwho = lock_motor.pos_delta_low_count | 128;
      ble_lbs_send_magwho_state(&m_lbs);
    }

    if( (lock_motor.motor_voltage >= STALL_CEILING) ||
        //(lock_motor.pos_delta_low_count > POS_STALL_THRESHOLD) ||
        (lock_motor.millis > 500)
        )
      {
        if(lock_motor.motor_voltage == val){
          lock_motor.motor_voltage_same_count++;
        } else {
          lock_motor.motor_voltage_same_count = 0;
        }


        // should set a BRAKE'd flag
        // FIXME

        if( ((lock_motor.motor_voltage_same_count >= STALL_THRESHOLD) ||
             //(lock_motor.pos_delta_low_count > POS_STALL_THRESHOLD) ||
             (lock_motor.millis > 500)) &&
            (lock_motor.calib_braking == false)
            ){
          lock_motor.calib_lock_pos = lock_motor.lock_pos;
          lock_motor.reason = E_STALLED;
          uart_putstring("STALL\r\n");
          if((lock_motor.calibrating == true)){
            lock_motor.calib_braking = true;
            write_i2c(1, LOCK_I2C_ADDR, 0, brake_cmd, 1, true, calib_braking_cb, NULL);
          } else {
            kill_motor();
            // TODO maybe error flagging here
          }
        }

      }

    lock_motor.motor_voltage = val;
    /*
    uart_puthexbyte(val);
    uart_putstring("-");
    uart_puthexbyte(lock_motor.lock_delta & 0xff);
    uart_putstring("-");
    uart_puthexbyte(lock_motor.state);
    uart_putstring("|");
    // */
    rb_add(&lock_motor.volt_rb, val);
    rb_add(&lock_motor.pos_rb, lock_motor.lock_delta & 0xff);

    break;
  }

  advance_motor_sm();

  if((pocount++ %17) == 0){
    pocount = 0;
    ble_lbs_send_voltage_state(&m_lbs, val);
  }
}

void pos_callback(int32_t pos){
  static uint8_t pocount = 0;
  lock_motor.lock_pos += pos;
  lock_motor.lock_delta = pos;
  if(abs(pos) < 3) {
    lock_motor.pos_delta_low_count ++;
  } else {
    lock_motor.pos_delta_low_count = 0;
  }

  // only do this in response to i2c?
  //advance_motor_sm();

  if ((pocount++ %10) == 0){
    ble_lbs_send_quad_state(&m_lbs, lock_motor.lock_pos);
  }

  if ((pocount++ %5) == 0){
    pocount = 0;
    ble_lbs_send_lock_pos_state(&m_lbs, lock_motor.lock_delta);
  }

}

// 0 -- forward -- counts down
// 1 -- backward -- counts up

bool send_lock_command(MOTOR_STATES cmd)
{
  if(cmd == M_RECOVER_ERROR){
    // "oh shit" command ... shouldn't exist in final firmware
    lock_motor.state = S_BAILOUT;
    advance_motor_sm();
    return true;
  } else if(lock_motor.state == WAITING){
    uart_putstring("send motor command:  ");
    if(cmd == M_FORWARD){
      uart_putstring("forward\r\n");
    } else if   (cmd == M_BACKWARD){
      uart_putstring("backward\r\n");
    } else if (cmd == M_CALIBRATE) {
      uart_putstring("calibrate\r\n");
    } else {
      uart_puthexbyte(cmd & 0xff);
      uart_putstring("\r\n");
    }
    lock_motor.command = cmd;
    servo_adc_enable();
    motor_quad_enable();
    return true;
  } else {
    return false;
  }
}

// reset after every run of the state machine
void lock_sm_reset(void)
{
  lock_motor.lock_pos = 0; // FIXME: remove this in working code -- this, along with stops needs to go into NVRAM
  lock_motor.millis = 0;
  lock_motor.motor_voltage_same_count = 0;
  lock_motor.pos_delta_low_count = 0;
  lock_motor.state = WAITING;
  lock_motor.command = M_NONE;
  lock_motor.reason = E_NONE;
  lock_motor.calibrating = false;
  lock_motor.calib_braking = false;
  rb_init(&(lock_motor.volt_rb), lock_motor.volt_rb_store, 20);
  rb_init(&(lock_motor.pos_rb), lock_motor.pos_rb_store, 20);
  ble_lbs_send_lock_for_state(&m_lbs, lock_motor.forward_stop);
  ble_lbs_send_lock_back_state(&m_lbs, lock_motor.backward_stop);
}

// one-time only initialization
bool lock_sm_init(I2C_SM*i2c, lock_notify_cb cb, void* cb_data, app_timer_id_t servo_tid)
{
  //timers
  servo_set_timerid(servo_tid);

  // callbacks
  lock_motor.i2c_sm = i2c;
  lock_motor.notify = cb;
  lock_motor.cb_data = cb_data;

  // persistant lock state
  lock_motor.lock_pos = 0;  // TODO: get from NVRAM
  lock_motor.forward_stop = 500; // TODO: get from NVRAM
  lock_motor.backward_stop = -500; // TODO: get from NVRAM

  lock_sm_reset(); // stuff that can be called multiple times

  return true;
}


void kill_motor(void) // kills without waiting for I2C callback
{
  lock_motor.io_done = false;
  write_i2c(1, LOCK_I2C_ADDR, 0, stop_cmd, 1, true, NULL, NULL);
  lock_motor.millis = 0;
}

void stop_motor(void) // kills but triggers I2C callback, which advances state machine
{
  lock_motor.io_done = false;
  write_i2c(1, LOCK_I2C_ADDR, 0, stop_cmd, 1, true, i2c_callback, NULL);
  lock_motor.millis = 0;
}

// note swapping forward and backward here and below to reflect quadrature report
uint8_t forward_cmd[] = {(LOCK_5V<<2)|M_BACKWARD};
void motor_fwd(void)
{
  lock_motor.io_done = false;
  write_i2c(1, LOCK_I2C_ADDR, 0, forward_cmd, 1, true, i2c_callback, NULL);
  //ble_lbs_send_debug_state(&m_lbs, cmd[0]);
  lock_motor.millis = 0;
}

uint8_t backward_cmd[] = {(LOCK_5V<<2)|M_FORWARD};
void motor_back(void)
{
  lock_motor.io_done = false;
  write_i2c(1, LOCK_I2C_ADDR, 0, backward_cmd, 1, true, i2c_callback, NULL);
  lock_motor.millis = 0;
}

void advance_motor_sm()
{
  static int delaycount;
  switch(lock_motor.state){

  case WAITING:
    {
      if (lock_motor.command == M_CALIBRATE){
        lock_motor.state = COMMAND_CALIBRATE;
        lock_motor.calibrating = true;
        lock_motor.lock_pos = 0;
        ble_lbs_send_lock_pos_state(&m_lbs, lock_motor.lock_pos);
        lock_motor.reason = E_NONE;
        lock_motor.state = CALIB_BACKSEEK;
        motor_back();
      } else if (lock_motor.command == M_FORWARD){
        if (lock_motor.lock_pos < lock_motor.forward_stop ) {
          lock_motor.state = COMMAND_FOR;
          motor_quad_enable();
          motor_fwd();
        } else {
          lock_motor.state = S_DONE;
          notify_caller();
        }
      } else if (lock_motor.command == M_BACKWARD){
        if (lock_motor.lock_pos > lock_motor.backward_stop ) {
          lock_motor.state = COMMAND_BACK;
          motor_quad_enable();
          motor_back();
        } else {
          lock_motor.state = S_DONE;
          notify_caller();
        }
      }
      break;

  /* TODO:

    keep last 20 chunks of history to determine exactly where stall started and use
    that to get a real value for the start and stop boundaries

    */

  /* TODO:

    Check for wildly out of bounds counts (>|2000| or so?) when calibrating to catch free-running
    (broken gear?)

  */

    case CALIB_BACKSEEK:
    {
      if(lock_motor.reason == E_STALLED){
        lock_motor.backward_stop = lock_motor.calib_lock_pos;
        ble_lbs_send_lock_back_state(&m_lbs, lock_motor.backward_stop);
        uart_puthexquad(lock_motor.backward_stop);
        uart_putstring("  backward stop\r\n");
        lock_motor.reason = E_NONE;
        lock_motor.state = CALIB_BACK_WAITBRAKE;
      } else if(lock_motor.reason == E_NONE){
        // continue
      } else {
        lock_motor.state = S_ERROR;
      }
    }
    break;

    case CALIB_BACK_WAITBRAKE:
    {
      if(lock_motor.calib_braking == false){
        lock_motor.motor_voltage_same_count = 0;
        lock_motor.state = S_DELAYCOUNT;
      }
    }
    break;

    case S_DELAYCOUNT:
      {
        if(delaycount++ == 60){
          delaycount = 0;
          lock_motor.state = CALIB_FORSEEK;
          motor_fwd();
        }
      }
      break;

    case CALIB_FORSEEK:
    {
      if(lock_motor.reason == E_STALLED){
        lock_motor.forward_stop = lock_motor.calib_lock_pos;
        ble_lbs_send_lock_for_state(&m_lbs, lock_motor.forward_stop);
        uart_puthexquad(lock_motor.forward_stop);
        uart_putstring("  forward stop\r\n");
        lock_motor.reason = E_NONE;
        lock_motor.state = CALIB_FOR_WAITBRAKE;
      } else if(lock_motor.reason == E_NONE){
        // continue
      } else {
        lock_motor.state = S_ERROR;
      }
    }
    break;

    case CALIB_FOR_WAITBRAKE:
    {
      if(lock_motor.calib_braking == false){
        lock_motor.state = S_DELAYCOUNT2;
      }
    }
    break;

    case S_DELAYCOUNT2:
      {
        if(delaycount++ == 60){
          delaycount = 0;
          kill_motor();
          lock_motor.state = S_DONE;
          notify_caller();
        }
      }
      break;

    case COMMAND_FOR:
      {
        if( (lock_motor.lock_pos < lock_motor.backward_stop) ){
          // out of bounds
          lock_motor.state = S_ERROR;
          lock_motor.reason = E_UNDERFLOW;
          stop_motor();
          notify_caller();
        } else if(lock_motor.lock_pos >= lock_motor.forward_stop){
          ble_lbs_send_debug_state(&m_lbs, 69);
          lock_motor.state = S_DONE;
          stop_motor();
          notify_caller();
        } else {
          lock_motor.state = WRITE_FAULT_CHECK_FOR;
          uint8_t cmd[] = "";
          lock_motor.io_done = false;
          write_i2c(1, LOCK_I2C_ADDR, 1, cmd, 0, false, i2c_callback, NULL);
        }
      }
      break;
    case WRITE_FAULT_CHECK_FOR:
      if(lock_motor.io_done){
        lock_motor.state = CHECK_FAULT_FOR;
        lock_motor.io_done = false;
        read_i2c(1, LOCK_I2C_ADDR, lock_motor.fault_data, 2,
                 i2c_callback, NULL);
      }
      break;
    case CHECK_FAULT_FOR:
      if(lock_motor.io_done){
        if(lock_motor.fault_data[1] != 0){
          stop_motor();
          lock_motor.state = S_ERROR;
          lock_motor.reason = E_FAULT;
          notify_caller();
        } else {
          lock_motor.state = COMMAND_FOR;
        }
      }
      break;

    case COMMAND_BACK:
      {
        if( (lock_motor.lock_pos > lock_motor.forward_stop) ){
          // out of bounds
          lock_motor.state = S_ERROR;
          stop_motor();
          notify_caller();
        } else if(lock_motor.lock_pos <= lock_motor.backward_stop ){
          lock_motor.state = S_DONE;
          stop_motor();
          notify_caller();
        } else {
          lock_motor.state = WRITE_FAULT_CHECK_BACK;
          uint8_t cmd[] = "";
          lock_motor.io_done = false;
          write_i2c(1, LOCK_I2C_ADDR, 1, cmd, 0, false, i2c_callback, NULL);
        }
      }
      break;
    case WRITE_FAULT_CHECK_BACK:
      if(lock_motor.io_done){
        lock_motor.state = CHECK_FAULT_BACK;
        lock_motor.io_done = false;
        read_i2c(1, LOCK_I2C_ADDR, lock_motor.fault_data, 2,
                 i2c_callback, NULL);
      }
      break;
    case CHECK_FAULT_BACK:
      if(lock_motor.io_done){
        if(lock_motor.fault_data[1] != 0){
          stop_motor();
          lock_motor.state = S_ERROR;
          lock_motor.reason = E_FAULT;
          notify_caller();
        } else {
          lock_motor.state = COMMAND_BACK;
        }
      }
      break;

    case S_ERROR:
      {
      }
      break;
    case S_DONE:
      {
        //kill_motor();
      }
      break;
    case S_BAILOUT:
      {
        uart_putstring("recover\r\n");
        // HALP
        servo_adc_disable();
        motor_quad_disable();
        kill_motor();
        lock_sm_reset();
      }
      break;
    default:
      {
        lock_motor.state = S_ERROR;
      }
    }
  }
  ble_lbs_send_locksm_state(&m_lbs, lock_motor.state);
}

