const Joi = require('@hapi/joi')

const params = Joi.object({
  fleet_id: Joi.number().positive().required()
})

const createBody = Joi.object({
  duration: Joi.number().positive().required(),
  duration_unit: Joi.string()
    .valid('minutes', 'hours', 'days', 'weeks', 'months')
    .required(),
  grace_period: Joi.number().positive(),
  grace_period_unit: Joi.string()
    .valid('minutes', 'hours', 'days', 'weeks', 'months')
    .when('grace_period', { is: Joi.exist(), then: Joi.required() }),
  price: Joi.number().positive().required(),
  price_currency: Joi.string().length(3)
})

const updateBody = Joi.object({
  duration: Joi.number().positive(),
  duration_unit: Joi.string()
    .valid('minutes', 'hours', 'days', 'weeks', 'months'),
  grace_period: Joi.number().positive(),
  grace_period_unit: Joi.string()
    .valid('minutes', 'hours', 'days', 'weeks', 'months')
    .when('grace_period', { is: Joi.exist(), then: Joi.required() }),
  price: Joi.number().positive(),
  price_currency: Joi.string().length(3)
}).min(1)

const updateParams = Joi.object({
  fleet_id: Joi.number().positive().required(),
  pricing_option_id: Joi.number().positive().required()
})

module.exports = {
  createBody,
  params,
  updateParams,
  updateBody
}
