'use strict'

const randomString = (x) => {
  let s = ''
  while (s.length < x && x > 0) {
    let r = Math.random()
    s += (r < 0.1 ? Math.floor(r * 100) : String.fromCharCode(Math.floor(r * 26) + (r > 0.5 ? 97 : 65)))
  }
  return s
}

const randomDigits = (digit) => {
  let n = Math.floor((Math.random() * 9) + 1)
  for (let i = 0; i < digit - 1; i++) {
    n = (n * 10) + Math.floor((Math.random() * 9) + 1)
  }
  return n
}

module.exports = {
  randomString,
  randomDigits
}
