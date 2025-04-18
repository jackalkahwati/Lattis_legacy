#ifndef __skylock_gpio_h__
#define __skylock_gpio_h__

//common stuff
#define MAG_I2C_ADDR 0x1c
#define CAP_I2C_ADDR 0x5a
#define LOCK_I2C_ADDR  0x60 // motor
#define ACCEL_I2C_ADDR 0x18

// board.h stuff here
#include "nrf_gpio.h"
#define BUTTON_PULL    NRF_GPIO_PIN_PULLUP

//#define REAL_SKYLOCK

#ifdef REAL_SKYLOCK
// real skylock GPIOs go here

// these have no meaning on the skylock ... assigning them unused GPIOs on that for now
#define WAKEUP_BUTTON_PIN               21                                    /**< Button used to wake up the application. */
#define ADVERTISING_LED_PIN_NO          22                                       /**< Is on when device is advertising. */
#define CONNECTED_LED_PIN_NO            23                                       /**< Is on when device has connected. */
#define LEDBUTTON_LED_PIN_NO            22
#define LEDBUTTON_BUTTON_PIN_NO         24

#define MOTOR_MAG_SW 30 // enables motor chip and hall effect sensors

#define I2C_SCL 0
#define I2C_SDA 1

#define ACC_I2C_SDA 21
#define ACC_I2C_SCL 22

// UART over USB
#define RX_PIN_NUMBER  18
#define TX_PIN_NUMBER  17

// quadrature decoder
#define IO_QDEC_SCROLL1_PIN 4
#define IO_QDEC_SCROLL2_PIN 3

// capacitive sensor stuff
#define CAPSENSE_INT_PIN 29

// motor current ADC
#define SERVO_ADC_ANALOG_INPUT ADC_CONFIG_PSEL_AnalogInput6

// enable solar charge
#define SOLAR_EN 9

// enable accelerometer
#define ACCEL_CS 13 // 1 for i2c mode, 0 for spi mode
#define ACCEL_SAD 14 // 0 for 0x18, 1 for 0x19

#else
// devkit GPIOS go here

#define BUTTON_0        16
#define BUTTON_1        17
#define LED_0           18
#define LED_1           19


// these have no meaning on the skylock ... assigning them unused GPIOs on that for now
#define WAKEUP_BUTTON_PIN               BUTTON_0                                    /**< Button used to wake up the application. */
#define ADVERTISING_LED_PIN_NO          LED_0                                       /**< Is on when device is advertising. */
#define CONNECTED_LED_PIN_NO            LED_1                                       /**< Is on when device has connected. */
#define LEDBUTTON_LED_PIN_NO            LED_0
#define LEDBUTTON_BUTTON_PIN_NO         BUTTON_1

#define MOTOR_MAG_SW 30 // enables motor chip and hall effect sensors

#define I2C_SCL 28
#define I2C_SDA 29

#define ACC_I2C_SDA 21
#define ACC_I2C_SCL 22

// UART over USB
#define RX_PIN_NUMBER  11
#define TX_PIN_NUMBER  9
#define CTS_PIN_NUMBER 10 // only used on
#define RTS_PIN_NUMBER 8 // dev board

// quadrature decoder
#define IO_QDEC_SCROLL1_PIN  6
#define IO_QDEC_SCROLL2_PIN  7

// capacitive sensor stuff
#define CAPSENSE_INT_PIN 16

// motor current ADC
#define SERVO_ADC_ANALOG_INPUT ADC_CONFIG_PSEL_AnalogInput2

// enable solar charge
#define SOLAR_EN 20

// enable accelerometer
#define ACCEL_CS 23 // 1 for i2c mode, 0 for spi mode
#define ACCEL_SAD 29 // 0 for 0x18, 1 for 0x19

#endif

#endif
