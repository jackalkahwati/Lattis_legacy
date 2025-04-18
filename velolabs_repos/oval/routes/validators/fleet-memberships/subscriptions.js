const Joi = require('@hapi/joi')

const retrieve = Joi.object({
  subscription_id: Joi.number().required()
})

const list = Joi.object({
  fleet_id: Joi.number()
})

module.exports = {
  retrieve,
  list
}
