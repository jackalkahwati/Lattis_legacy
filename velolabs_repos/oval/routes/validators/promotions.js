const Joi = require('@hapi/joi')

const redeem = Joi.object({
  promo_code: Joi.string()
    .length(12)
    .pattern(/(\w|\d){2}-(\w|\d){4}-(\w|\d){4}/)
    .uppercase()
    .required()
    .messages({
      'string.pattern.base': 'Invalid promo code.'
    })
}).required()

const list = Joi.object({
  fleet_id: Joi.number().positive(),
  include_claimed: Joi.boolean()
})

module.exports = {
  redeem,
  list
}
