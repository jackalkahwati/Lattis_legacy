const { config } = require('@velo-labs/platform')
const stripe = require('stripe')(config.stripe.apiKey)

function refund (params, options) {
  return stripe.refunds.create(params, options)
}

module.exports = {
  refund
}
