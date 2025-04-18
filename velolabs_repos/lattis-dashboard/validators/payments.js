const Joi = require('@hapi/joi')

const connectMercadoPago = Joi.object({
  fleet_id: Joi.number().positive().required(),
  privateKey: Joi.string().required(),
  publicKey: Joi.string().required()
}).required()

module.exports = {
  connectMercadoPago
}
