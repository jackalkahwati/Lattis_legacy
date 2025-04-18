const Joi = require('@hapi/joi')

const retrieve = Joi.object({
  bike_id: Joi.number().positive(),
  port_id: Joi.number().positive(),
  hub_id: Joi.number().positive()
}).required()

module.exports = {
  retrieve
}
