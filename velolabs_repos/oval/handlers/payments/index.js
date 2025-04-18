const refunds = require('./refunds')
const payments = require('./payments')

// TODO: Adding this for backwards compatibility. Remove when refunds are refactored
payments.refunds = refunds

module.exports = payments
