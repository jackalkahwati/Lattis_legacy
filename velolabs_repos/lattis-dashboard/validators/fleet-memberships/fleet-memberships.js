const Joi = require('@hapi/joi')

const createMembershipSchema = Joi.object({
  fleet_id: Joi.number().required(),
  membership_price: Joi.number().min(0).required(),
  membership_price_currency: Joi.string().length(3).required(),
  membership_incentive: Joi.number().greater(0).max(100).required(),
  payment_frequency: Joi.string().valid('monthly', 'weekly', 'yearly').required()
})

const updateMembershipSchema = Joi.object({
  membership_price: Joi.number(),
  membership_price_currency: Joi.string().length(3),
  membership_incentive: Joi.number().greater(0).max(100),
  payment_frequency: Joi.string().valid('monthly', 'weekly', 'yearly')
})

const updateMembershipParamsSchema = Joi.object({
  fleet_membership_id: Joi.number().required()
})

const activate = updateMembershipParamsSchema
const deactivate = updateMembershipParamsSchema

const list = Joi.object({
  fleet_id: Joi.number().positive().required()
})

const subscriptions = Joi.object({
  fleet_id: Joi.number().positive().required(),
  user_id: Joi.number().positive().required()
})

module.exports = {
  createMembershipSchema,
  updateMembershipSchema,
  updateMembershipParamsSchema,
  activate,
  deactivate,
  list,
  subscriptions
}
