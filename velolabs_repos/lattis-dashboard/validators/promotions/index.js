const Joi = require('@hapi/joi')

const createPromotionSchema = Joi.object({
  fleet_id: Joi.number().positive().required(),
  amount: Joi.number().greater(0).max(100).required(),
  usage: Joi.string()
    .valid('single', 'multiple', 'multiple_unlimited')
    .required(),
  users: Joi.array().items(Joi.number().positive().required())
}).required()

const deactivatePromotionSchema = Joi.object({
  promotion_id: Joi.number().positive().required()
})

const listPromotionsSchema = Joi.object({
  fleet_id: Joi.number().positive().required()
})

const listUserPromotions = Joi.object({
  fleetId: Joi.number().positive().required(),
  userId: Joi.number().positive().required()
}).required()

module.exports = {
  createPromotionSchema,
  deactivatePromotionSchema,
  listPromotionsSchema,
  listUserPromotions
}
