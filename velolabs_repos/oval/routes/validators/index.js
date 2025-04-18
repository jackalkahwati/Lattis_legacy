const fleetMemberships = require('./fleet-memberships')
const reservations = require('./reservations')
const promotions = require('./promotions')
const equipment = require('./equipment')

const validator = require('express-joi-validation').createValidator({
  passError: true
})

module.exports = {
  validator,
  schema: {
    fleetMemberships,
    reservations,
    promotions,
    equipment
  }
}
