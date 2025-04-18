const router = require('./fleet')

const reservationSettings = require('./reservations-settings')
const pricingOptions = require('./pricing-options')
const memberships = require('./memberships')

reservationSettings(router)
pricingOptions(router)
memberships(router)

module.exports = router
