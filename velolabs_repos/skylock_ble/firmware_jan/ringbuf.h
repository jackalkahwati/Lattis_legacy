#ifndef __RINGBUF_H__
#define __RINGBUF_H__

#include <stdint.h>

typedef uint8_t rbe_t;


/* ring buffer implementation */

typedef struct
{
  unsigned int len; // memory length of buffer
  unsigned int count; // number of entries in buffer
  unsigned int ptr;   // index of newest member
  unsigned int old;   // index of oldest member
  rbe_t* buf; // memory buffer
} ringbuffer;

#define RB ringbuffer*
void rb_init(RB rb, rbe_t* mem, unsigned int length);
void rb_add(RB rb, rbe_t c);
int rb_get(RB rb);
int rb_count(RB rb);

#endif