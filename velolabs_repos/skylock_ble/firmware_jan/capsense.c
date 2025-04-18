#include "capsense.h"
#include "ble_lbs.h"
#include "nordic_common.h"

extern ble_lbs_t                        m_lbs;

static uint8_t en_cmd[] = {0x06};
void enable_capsense(i2c_notify_cb done)
{
  write_i2c(1, CAP_I2C_ADDR, 0x5E, en_cmd, 1, true, done, NULL);
}

static uint8_t dis_cmd[] = {0x00};
void disable_capsense(void)
{
  write_i2c(1, CAP_I2C_ADDR, 0x5E, dis_cmd, 1, true, NULL, NULL);
}


static uint8_t capstat[1];
void capstat_cb(I2C_SM_STATES state, void* notused)
{
  UNUSED_PARAMETER(notused);
  if (state == DONE){
    ble_lbs_on_button_change(&m_lbs, capstat[1]);
  } else {
    // NOPe
  }

}

void wrote_capsense_cb(I2C_SM_STATES state, void* notused)
{
  UNUSED_PARAMETER(notused);
  if (state == DONE){
    read_i2c(1, CAP_I2C_ADDR, capstat, 1,
             capstat_cb, NULL);
  } else {
    // also NOPe
  }
}
void dispatch_capsense(void)
{
  uint8_t cmd[] = "";
  write_i2c(1, CAP_I2C_ADDR, 0x00, cmd, 0, false, wrote_capsense_cb, NULL);
}
