const Joi = require('@hapi/joi')

const subscribe = Joi.object({
  membership_id: Joi.number().required()
})

const list = Joi.object({
  fleet_id: Joi.number()
})

module.exports = {
  list,
  subscribe,
  unsubscribe: subscribe,
  retrieve: subscribe
}
