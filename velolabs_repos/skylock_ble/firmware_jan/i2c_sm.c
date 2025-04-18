#include "twi_master_config.h"
//#include "twi_master.h"

#include <stdbool.h>
#include <stdint.h>

#include "nordic_common.h"
#include "softdevice_handler.h"
#include "nrf.h"
#include "nrf_soc.h"
#include "nrf_delay.h"
#include "nrf_gpio.h"

#include "i2c_sm.h"

/** @brief Function for initializing the twi_master.
 */

typedef enum IntVals {
  IV_RXDREADY,
  IV_TXDSENT,
  IV_STOPPED,
  IV_SUSPENDED,
  IV_ERROR
} intvals;

typedef struct {
  intvals reason;
  uint8_t busno;
} i2c_note;

// NRF_ERROR_INVALID_PARAM
// NRF_SUCCESS;
bool sm_twi_master_clear_bus(uint8_t busno, uint32_t* err_code)
{
  NRF_TWI_Type* iface;
  uint8_t TWI_MASTER_CONFIG_CLOCK_PIN_NUMBER;
  uint8_t TWI_MASTER_CONFIG_DATA_PIN_NUMBER;
  if(busno == 1){
    iface = NRF_TWI1;
    TWI_MASTER_CONFIG_CLOCK_PIN_NUMBER = TWI1_MASTER_CONFIG_CLOCK_PIN_NUMBER;
    TWI_MASTER_CONFIG_DATA_PIN_NUMBER = TWI1_MASTER_CONFIG_DATA_PIN_NUMBER;
  } else if (busno == 2){
    iface = NRF_TWI0;
    TWI_MASTER_CONFIG_CLOCK_PIN_NUMBER = TWI0_MASTER_CONFIG_CLOCK_PIN_NUMBER;
    TWI_MASTER_CONFIG_DATA_PIN_NUMBER = TWI0_MASTER_CONFIG_DATA_PIN_NUMBER;
  } else {
    *err_code = NRF_ERROR_INVALID_PARAM;
    return false;
  }

  uint32_t twi_state;
  bool     bus_clear;
  uint32_t clk_pin_config;
  uint32_t data_pin_config;

  // Save and disable TWI hardware so software can take control over the pins.
  twi_state        = iface->ENABLE;
  iface->ENABLE = TWI_ENABLE_ENABLE_Disabled << TWI_ENABLE_ENABLE_Pos;

  clk_pin_config = \
    NRF_GPIO->PIN_CNF[TWI_MASTER_CONFIG_CLOCK_PIN_NUMBER];
  NRF_GPIO->PIN_CNF[TWI_MASTER_CONFIG_CLOCK_PIN_NUMBER] =      \
    (GPIO_PIN_CNF_SENSE_Disabled  << GPIO_PIN_CNF_SENSE_Pos) \
    | (GPIO_PIN_CNF_DRIVE_S0D1    << GPIO_PIN_CNF_DRIVE_Pos)   \
    | (GPIO_PIN_CNF_PULL_Pullup   << GPIO_PIN_CNF_PULL_Pos)    \
    | (GPIO_PIN_CNF_INPUT_Connect << GPIO_PIN_CNF_INPUT_Pos)   \
    | (GPIO_PIN_CNF_DIR_Output    << GPIO_PIN_CNF_DIR_Pos);

  data_pin_config = \
    NRF_GPIO->PIN_CNF[TWI_MASTER_CONFIG_DATA_PIN_NUMBER];
  NRF_GPIO->PIN_CNF[TWI_MASTER_CONFIG_DATA_PIN_NUMBER] =       \
    (GPIO_PIN_CNF_SENSE_Disabled  << GPIO_PIN_CNF_SENSE_Pos) \
    | (GPIO_PIN_CNF_DRIVE_S0D1    << GPIO_PIN_CNF_DRIVE_Pos)   \
    | (GPIO_PIN_CNF_PULL_Pullup   << GPIO_PIN_CNF_PULL_Pos)    \
    | (GPIO_PIN_CNF_INPUT_Connect << GPIO_PIN_CNF_INPUT_Pos)   \
    | (GPIO_PIN_CNF_DIR_Output    << GPIO_PIN_CNF_DIR_Pos);

  *err_code = NRF_SUCCESS;

  //This really sucks
  if(busno == 1){
    TWI1_SDA_HIGH();
    TWI1_SCL_HIGH();
    TWI_DELAY();
    if ((TWI1_SDA_READ() == 1) && (TWI1_SCL_READ() == 1))
      {
        bus_clear = true;
      }
    else
      {
        uint_fast8_t i;
        bus_clear = false;
        for (i=18; i--;)
          {
            TWI1_SCL_LOW();
            TWI_DELAY();
            TWI1_SCL_HIGH();
            TWI_DELAY();

            if (TWI1_SDA_READ() == 1)
              {
                bus_clear = true;
                break;
              }
          }
      }
    NRF_GPIO->PIN_CNF[TWI1_MASTER_CONFIG_CLOCK_PIN_NUMBER] = clk_pin_config;
    NRF_GPIO->PIN_CNF[TWI1_MASTER_CONFIG_DATA_PIN_NUMBER]  = data_pin_config;
  } else {
    TWI0_SDA_HIGH();
    TWI0_SCL_HIGH();
    TWI_DELAY();
    if ((TWI0_SDA_READ() == 1) && (TWI0_SCL_READ() == 1))
      {
        bus_clear = true;
      }
    else
      {
        uint_fast8_t i;
        bus_clear = false;
        for (i=18; i--;)
          {
            TWI0_SCL_LOW();
            TWI_DELAY();
            TWI0_SCL_HIGH();
            TWI_DELAY();

            if (TWI0_SDA_READ() == 1)
              {
                bus_clear = true;
                break;
              }
          }
      }
    NRF_GPIO->PIN_CNF[TWI0_MASTER_CONFIG_CLOCK_PIN_NUMBER] = clk_pin_config;
    NRF_GPIO->PIN_CNF[TWI0_MASTER_CONFIG_DATA_PIN_NUMBER]  = data_pin_config;
  }


  iface->ENABLE = twi_state;

  return bus_clear;
}

bool sm_twi_master_init(uint8_t busno, uint32_t* err_code)
{
  NRF_TWI_Type* iface;
  uint8_t channel;
  uint8_t TWI_MASTER_CONFIG_CLOCK_PIN_NUMBER;
  uint8_t TWI_MASTER_CONFIG_DATA_PIN_NUMBER;
  if(busno == 1){
    iface = NRF_TWI1;
    channel = 0;
    TWI_MASTER_CONFIG_CLOCK_PIN_NUMBER = TWI1_MASTER_CONFIG_CLOCK_PIN_NUMBER;
    TWI_MASTER_CONFIG_DATA_PIN_NUMBER = TWI1_MASTER_CONFIG_DATA_PIN_NUMBER;
  } else if (busno == 2){
    iface = NRF_TWI0;
    channel = 1;
    TWI_MASTER_CONFIG_CLOCK_PIN_NUMBER = TWI0_MASTER_CONFIG_CLOCK_PIN_NUMBER;
    TWI_MASTER_CONFIG_DATA_PIN_NUMBER = TWI0_MASTER_CONFIG_DATA_PIN_NUMBER;
  } else {
    *err_code = NRF_ERROR_INVALID_PARAM;
    return false;
  }

    /* To secure correct signal levels on the pins used by the TWI
       master when the system is in OFF mode, and when the TWI master is
       disabled, these pins must be configured in the GPIO peripheral.
    */
    NRF_GPIO->PIN_CNF[TWI_MASTER_CONFIG_CLOCK_PIN_NUMBER] =     \
        (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos) \
      | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos) \
      | (GPIO_PIN_CNF_PULL_Pullup    << GPIO_PIN_CNF_PULL_Pos)  \
      | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos) \
      | (GPIO_PIN_CNF_DIR_Input      << GPIO_PIN_CNF_DIR_Pos);

    NRF_GPIO->PIN_CNF[TWI_MASTER_CONFIG_DATA_PIN_NUMBER] =      \
        (GPIO_PIN_CNF_SENSE_Disabled << GPIO_PIN_CNF_SENSE_Pos) \
      | (GPIO_PIN_CNF_DRIVE_S0D1     << GPIO_PIN_CNF_DRIVE_Pos) \
      | (GPIO_PIN_CNF_PULL_Pullup    << GPIO_PIN_CNF_PULL_Pos)  \
      | (GPIO_PIN_CNF_INPUT_Connect  << GPIO_PIN_CNF_INPUT_Pos) \
      | (GPIO_PIN_CNF_DIR_Input      << GPIO_PIN_CNF_DIR_Pos);

    iface->EVENTS_RXDREADY = 0;
    iface->EVENTS_TXDSENT  = 0;
    iface->PSELSCL         = TWI_MASTER_CONFIG_CLOCK_PIN_NUMBER;
    iface->PSELSDA         = TWI_MASTER_CONFIG_DATA_PIN_NUMBER;
    iface->FREQUENCY       = TWI_FREQUENCY_FREQUENCY_K100 << TWI_FREQUENCY_FREQUENCY_Pos;
    NRF_PPI->CH[channel].EEP        = (uint32_t)&iface->EVENTS_BB;
    NRF_PPI->CH[channel].TEP        = (uint32_t)&iface->TASKS_SUSPEND;
    if(busno == 1){
      NRF_PPI->CHENCLR          = PPI_CHENCLR_CH0_Msk;
    } else {
      NRF_PPI->CHENCLR          = PPI_CHENCLR_CH1_Msk;
    }
    iface->ENABLE          = TWI_ENABLE_ENABLE_Enabled << TWI_ENABLE_ENABLE_Pos;

    return sm_twi_master_clear_bus(busno, err_code);
}

uint32_t setup_i2c_ints(uint8_t busno)
{
  uint32_t err_code = NRF_SUCCESS;
  NRF_TWI_Type* iface;
  if(busno == 1){
    iface = NRF_TWI1;
  } else if (busno == 2) {
    iface = NRF_TWI0;
  } else {
    return NRF_ERROR_INVALID_PARAM;
  }

  if(busno == 1){
    err_code = sd_nvic_SetPriority(SPI1_TWI1_IRQn, NRF_APP_PRIORITY_LOW);
    err_code = sd_nvic_EnableIRQ(SPI1_TWI1_IRQn);
  } else {
    err_code = sd_nvic_SetPriority(SPI0_TWI0_IRQn, NRF_APP_PRIORITY_LOW);
    err_code = sd_nvic_EnableIRQ(SPI0_TWI0_IRQn);
  }
  iface->INTENSET =
    (
     (TWI_INTENSET_SUSPENDED_Set << TWI_INTENSET_SUSPENDED_Pos) |
     (TWI_INTENSET_STOPPED_Set << TWI_INTENSET_STOPPED_Pos) |
     (TWI_INTENSET_ERROR_Set << TWI_INTENSET_ERROR_Pos) |
     (TWI_INTENSET_TXDSENT_Set << TWI_INTENSET_TXDSENT_Pos) |
     (TWI_INTENSET_RXDREADY_Set << TWI_INTENSET_RXDREADY_Pos) );
  return err_code;
}


static uint32_t sm_init(uint8_t busno, I2C_SM* sm, uint8_t address)
{
  if(busno == 1){
    sm->TWI = NRF_TWI1;
  } else if (busno == 2) {
     sm->TWI = NRF_TWI0;
  } else {
    return NRF_ERROR_INVALID_PARAM;
  }
  sm->busno = busno;
  sm->address = address;
  sm->abort = false;
  sm->TWI->ADDRESS = (address);
  return NRF_SUCCESS;
}

uint32_t write_sm_init(uint8_t busno, I2C_SM* sm, uint8_t address, uint8_t reg,
                       uint8_t* data, uint8_t data_length, bool issue_stop,
                       i2c_notify_cb cb, void* cb_data)
{
  uint32_t err_code = sm_init(busno, sm, address);
  if(err_code != NRF_SUCCESS){
    return err_code;
  }

  sm->notify = cb;
  sm->cb_data = cb_data;
  sm->state = STARTED;
  sm->data_length = data_length;
  sm->data = data;
  sm->read_mode = false;
  sm->issue_stop = issue_stop;
  if(busno == 1){
    NRF_TWI1->TXD = reg;
    NRF_TWI1->TASKS_STARTTX = 1;
  } else {
    NRF_TWI0->TXD = reg;
    NRF_TWI0->TASKS_STARTTX = 1;
  }
  return NRF_SUCCESS;
}

uint32_t read_sm_init(uint8_t busno, I2C_SM* sm, uint8_t address,
                      uint8_t* data, uint8_t data_length,
                      i2c_notify_cb cb, void* cb_data)
{
  uint32_t err_code = sm_init(busno, sm, address);
  if(err_code != NRF_SUCCESS){
    return err_code;
  }

  sm->notify = cb;
  sm->cb_data = cb_data;
  sm->state = READING;
  sm->r_data_length = data_length;
  sm->r_data = data;
  sm->read_mode = true;
  sm->issue_stop = false;

  if(busno == 1){
    NRF_PPI->CH[0].TEP = (uint32_t)&NRF_TWI1->TASKS_SUSPEND;
    NRF_PPI->CHENSET          = PPI_CHENSET_CH0_Msk;
    NRF_TWI1->EVENTS_RXDREADY = 0;
    NRF_TWI1->TASKS_STARTRX = 1;
  } else {
    NRF_PPI->CH[1].TEP = (uint32_t)&NRF_TWI0->TASKS_SUSPEND;
    NRF_PPI->CHENSET          = PPI_CHENSET_CH1_Msk;
    NRF_TWI0->EVENTS_RXDREADY = 0;
    NRF_TWI0->TASKS_STARTRX = 1;
  }

  return NRF_SUCCESS;
}

void i2c_sm_advance(I2C_SM* sm, intvals Reason)
{
  uint8_t channel;
  if(sm->busno == 1){
    channel = 0;
  } else {
    channel = 1;
  }
  uint32_t err_code;

  switch(sm->state){
  case STARTED:
    //nrf_gpio_pin_set(SOLAR_EN);
    if(Reason == IV_TXDSENT){
      if (sm->data_length == 0){
        if(sm->issue_stop){
          sm->TWI->EVENTS_STOPPED = 0;
          sm->TWI->TASKS_STOP     = 1;
          sm->state = STOPPING;
        } else {
          sm->state = DONE;
          if(sm->notify != NULL){
            //nrf_gpio_pin_clear(SOLAR_EN);
            sm->notify(sm->state, (void*)sm->cb_data);
          }
        }
      } else {
        sm->TWI->TXD = *(sm->data);
        sm->data_length--;
        sm->data++;
      }
    } else if (Reason == IV_ERROR){
      sm->state = CLEANUP;
      sm->err_next = ERROR;
    } else if(sm->abort != false){
      sm->state = CLEANUP;
      sm->err_next = ABORTED;
    }
    break;
  case READING:
    if(Reason == IV_RXDREADY){
      *(sm->r_data) = sm->TWI->RXD;
      /* Configure PPI to stop TWI master before we get last BB event */
      if (sm->r_data_length == 1)
        {
            NRF_PPI->CH[channel].TEP = (uint32_t)&sm->TWI->TASKS_STOP;
        }
        if (sm->r_data_length == 0)
        {
          NRF_PPI->CH[channel].TEP = (uint32_t)&sm->TWI->TASKS_SUSPEND;
          sm->state = STOPPING;
        }
        sm->r_data++;
        sm->r_data_length--;
        // do we need a delay here?
        sm->TWI->TASKS_RESUME = 1;
    } else if (Reason == IV_ERROR){
      sm->state = CLEANUP;
      sm->err_next = ERROR;
    } else if(sm->abort != false){
      sm->state = CLEANUP;
      sm->err_next = ABORTED;
    }

    break;
  case STOPPING:
    if(Reason == IV_STOPPED){
      sm->state = DONE;
      if(sm->read_mode){
        if(sm->busno == 1){
          NRF_PPI->CHENCLR = PPI_CHENCLR_CH0_Msk;
        } else {
          NRF_PPI->CHENCLR = PPI_CHENCLR_CH1_Msk;
        }
      }
      if(sm->notify != NULL){
        //nrf_gpio_pin_clear(SOLAR_EN);
        sm->notify(sm->state, (void*)sm->cb_data);
      }
    }
    break;
  case CLEANUP:
    sm->state = sm->err_next;
    // Recover the peripheral as indicated by PAN 56: "TWI: TWI module lock-up." found at
    // Product Anomaly Notification document found at
    // https://www.nordicsemi.com/eng/Products/Bluetooth-R-low-energy/nRF51822/#Downloads
    sm->TWI->EVENTS_ERROR = 0;
    sm->TWI->ENABLE       = TWI_ENABLE_ENABLE_Disabled << TWI_ENABLE_ENABLE_Pos;
    sm->TWI->POWER        = 0;
    nrf_delay_us(5);
    sm->TWI->POWER        = 1;
    sm->TWI->ENABLE       = TWI_ENABLE_ENABLE_Enabled << TWI_ENABLE_ENABLE_Pos;

    (void)sm_twi_master_init(sm->busno, &err_code);


    if(sm->state == ERROR){
      if(sm->notify != NULL){
        sm->notify(sm->state, (void*)sm->cb_data);
      }
    } else if (sm->state == ABORTED) {
      if(sm->notify != NULL){
        sm->notify(sm->state, (void*)sm->cb_data);
      }
    }
    break;
  default:
    break;
  }
}

static I2C_SM i2c_1_sm;
static I2C_SM i2c_2_sm;

I2C_SM* get_i2c(uint8_t busno)
{
  if(busno == 1){
    return &i2c_1_sm;
  } else {
    return &i2c_2_sm;
  }
}

static void i2c_handler(void* p_event_data, uint16_t event_size)
{
  UNUSED_PARAMETER(event_size);

  i2c_note* which = (i2c_note*)p_event_data;
  if(which->busno == 1){
    i2c_sm_advance(&i2c_1_sm, which->reason);
  } else {
    i2c_sm_advance(&i2c_2_sm, which->reason);
  }
}

void SPI1_TWI1_IRQHandler(void)
{
  i2c_note note;
  note.busno = 1;
  if(NRF_TWI1->EVENTS_RXDREADY){
    NRF_TWI1->EVENTS_RXDREADY = 0;
    note.reason = IV_RXDREADY;
  }else if(NRF_TWI1->EVENTS_TXDSENT){
    NRF_TWI1->EVENTS_TXDSENT = 0;
    note.reason = IV_TXDSENT;
  }else if(NRF_TWI1->EVENTS_STOPPED){
    NRF_TWI1->EVENTS_STOPPED = 0;
    note.reason = IV_STOPPED;
  }else if(NRF_TWI1->EVENTS_SUSPENDED){
    NRF_TWI1->EVENTS_SUSPENDED = 0;
    note.reason = IV_SUSPENDED;
  }else if(NRF_TWI1->EVENTS_ERROR){
    NRF_TWI1->EVENTS_ERROR = 0;
    note.reason = IV_ERROR;
  }

  uint32_t err_code = app_sched_event_put(&note, sizeof(note), i2c_handler);
  APP_ERROR_CHECK(err_code);
}

void SPI0_TWI0_IRQHandler(void)
{
  uint8_t which = 2;
  app_sched_event_put(&which, sizeof(which), i2c_handler);
}

uint32_t write_i2c(uint8_t busno, uint8_t address, uint8_t reg, uint8_t* data,
               uint8_t data_length, bool issue_stop,
               i2c_notify_cb cb, void* cb_data)
{
  I2C_SM* sm;
  if(busno == 1){
    sm = &i2c_1_sm;
  } else {
    sm = &i2c_2_sm;
  }
  write_sm_init(busno, sm, address, reg, data,
                data_length, issue_stop,
                cb, cb_data);
}

uint32_t read_i2c(uint8_t busno, uint8_t address, uint8_t* data, uint8_t data_length,
              i2c_notify_cb cb, void* cb_data)
{
  I2C_SM* sm;
  if(busno == 1){
    sm = &i2c_1_sm;
  } else {
    sm = &i2c_2_sm;
  }
  read_sm_init(busno, sm, address, data, data_length,
               cb, cb_data);
}

I2C_SM_STATES i2c_sm_state(uint8_t busno)
{
  if(busno == 1){
    return i2c_1_sm.state;
  } else {
    return i2c_2_sm.state;
  }
}
