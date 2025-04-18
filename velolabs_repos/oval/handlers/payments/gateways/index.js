const mercadoPago = require('./mercadopago')
const stripe = require('./stripe')

const GATEWAYS = {
  stripe: 'stripe',
  mercadopago: 'mercadopago'
}

module.exports = {
  stripe,
  mercadoPago,
  GATEWAYS
}
