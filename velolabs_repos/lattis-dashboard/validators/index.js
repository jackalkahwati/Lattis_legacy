const validator = require('./validator')
const fleetMemberships = require('./fleet-memberships')
const reservations = require('./reservations')
const promotions = require('./promotions')
const payments = require('./payments')
const pricingOptions = require('./pricing-options')
const fleetPaymentSettings = require('./fleet-payment-settings')

module.exports = {
  validator,
  schema: {
    fleetMemberships,
    reservations,
    promotions,
    payments,
    pricingOptions,
    fleetPaymentSettings
  }
}
