#include "ringbuf.h"

void rb_init(RB rb, rbe_t* mem, unsigned int length)
{
  rb->buf = mem;
  rb->len = length;
  rb->count = 0;
  rb->ptr = 0;
  rb->old = 0;
}

int rb_count(RB rb)
{
  return rb->count;
}

void rb_add(RB rb, rbe_t c)
{
  rb->buf[rb->ptr] = c;
  rb->ptr++;

  if(rb->ptr == rb->len){
    rb->ptr = 0;
  }

  if(rb->count < rb->len){
    rb->count ++;
  } else {
    rb->old = rb->ptr;
  }
}

int rb_get(RB rb)
{
  int c;

  if(rb->count == 0){
    return -1;
  }

  c = rb->buf[rb->old];

  rb->old++;
  if(rb->old == rb->len){
    rb->old = 0;
  }

  rb->count --;
  return c;
}

