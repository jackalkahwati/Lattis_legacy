const moment = require('moment')

function castDataTypes (field, next) {
  if (field.type === 'TIMESTAMP') {
    const value = field.string()

    return value ? moment(value, 'YYYY-MM-DD HH:mm:ss').utc().format() : value
  } else if (field.type === 'NEWDECIMAL') {
    const value = field.string()

    return value ? Number(value) : value
  }

  return next()
}

module.exports = {
  castDataTypes
}
