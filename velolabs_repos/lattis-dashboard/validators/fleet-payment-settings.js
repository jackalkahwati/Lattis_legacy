const Joi = require('@hapi/joi')

const fleetPaymentSettings = Joi.object({
  fleet_id: Joi.number().positive().required(),
  enable_preauth: Joi.boolean(),
  preauth_amount: Joi.number()
    .min(0)
    .when('enable_preauth', {
      switch: [
        { is: true, then: Joi.number().positive().required() },
        { is: false, then: Joi.number().min(0).allow(null) }
      ]
    })
})
  .unknown(true)

module.exports = {
  fleetPaymentSettings
}
