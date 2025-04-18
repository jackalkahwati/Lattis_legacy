const moment = require('moment')

function castToUTCDateTime (field, next) {
  if (field.type === 'DATETIME') {
    const value = field.string()

    if (value) {
      return moment.utc(value, 'YYYY-MM-DD HH:mm:ss')
    }

    return value
  }

  return next()
}

module.exports = {
  castToUTCDateTime
}
