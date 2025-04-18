'use strict'

module.exports = function () {
  let code = (Math.floor(Math.random() * 899999)).toString()
  while (code.length < 6) {
    code = '0' + code
  }

  return code
}
