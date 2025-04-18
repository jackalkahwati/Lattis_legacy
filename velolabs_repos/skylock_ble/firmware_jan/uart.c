#include <stdint.h>

#include "nordic_common.h"
#include "softdevice_handler.h"
#include "nrf.h"
#include "nrf_gpio.h"
#include "nrf_soc.h"

#include "skylock_gpio.h"
#include "uart.h"
#include "ringbuf.h"

uint8_t uart_char_buffer[100];
ringbuffer uart_rb;

const char* hexen = "01234567890ABCDEF";

static void uart_sm_advance();

static void uart_handler(void* p_event_data, uint16_t event_size)
{
  UNUSED_PARAMETER(p_event_data);
  UNUSED_PARAMETER(event_size);
  uart_sm_advance();
}

static void uart_sm_advance()
{
  int icr;
  if( rb_count(&uart_rb) ) {
    icr = rb_get(&uart_rb);
    if(icr>-1){
      //NRF_UART0->EVENTS_TXDRDY = 0;
      NRF_UART0->TXD = (uint8_t) icr;
      //app_sched_event_put(NULL, 0, uart_handler);
    }
  }
}

void uart_puthexbyte(uint8_t hexnum)
{
  char out[3] = {0,0,0};
  out[0] = hexen[hexnum>>4];
  out[1] = hexen[hexnum & 0x0f];
  uart_putstring(out);
}

void uart_puthexword(uint16_t hexword)
{
  uart_puthexbyte(hexword>>8);
  uart_puthexbyte(hexword & 0xff);
}

void uart_puthexquad(uint32_t hexquad)
{
  uart_puthexword(hexquad>>16);
  uart_puthexword(hexquad & 0xFFFF);
}


void uart_putstring(const char * str)
{
#ifdef REAL_SKYLOCK
  return;
#endif
  const char* c = str;
  do{
    rb_add(&uart_rb, (uint8_t)*c);
  } while(*c++ != 0);
  app_sched_event_put(NULL, 0, uart_handler);
}

void UART0_IRQHandler(void)
{
  //uint8_t dontcare;
  NRF_UART0->EVENTS_TXDRDY = 0;
  app_sched_event_put(NULL, 0, uart_handler);
}


void uart_config(uint8_t txd_pin_number,
                 uint8_t rxd_pin_number)
{
#ifdef REAL_SKYLOCK
  return;
#endif
  rb_init(&uart_rb, uart_char_buffer, sizeof(uart_char_buffer));
  nrf_gpio_cfg_output(txd_pin_number);
  nrf_gpio_cfg_input(rxd_pin_number, NRF_GPIO_PIN_NOPULL);

#ifndef REAL_SKYLOCK
  // setup hardware flow control, required by dev board
  nrf_gpio_cfg_output(RTS_PIN_NUMBER);
  nrf_gpio_cfg_input(CTS_PIN_NUMBER, NRF_GPIO_PIN_NOPULL);
  NRF_UART0->PSELCTS = CTS_PIN_NUMBER;
  NRF_UART0->PSELRTS = RTS_PIN_NUMBER;
  NRF_UART0->CONFIG  = (UART_CONFIG_HWFC_Enabled << UART_CONFIG_HWFC_Pos);
#endif

  NRF_UART0->INTENSET = (UART_INTENSET_TXDRDY_Set << UART_INTENSET_TXDRDY_Pos);
  sd_nvic_SetPriority(UART0_IRQn, NRF_APP_PRIORITY_LOW);
  sd_nvic_EnableIRQ(UART0_IRQn);

  NRF_UART0->PSELTXD = txd_pin_number;
  NRF_UART0->PSELRXD = rxd_pin_number;


  NRF_UART0->BAUDRATE      = (UART_BAUDRATE_BAUDRATE_Baud115200 << UART_BAUDRATE_BAUDRATE_Pos);
  NRF_UART0->ENABLE        = (UART_ENABLE_ENABLE_Enabled << UART_ENABLE_ENABLE_Pos);
  NRF_UART0->TASKS_STARTTX = 1;
  //NRF_UART0->TASKS_STARTRX = 1;
  //NRF_UART0->EVENTS_RXDRDY = 0;
}
