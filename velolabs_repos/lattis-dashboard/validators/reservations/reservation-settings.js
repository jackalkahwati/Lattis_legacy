const Joi = require('@hapi/joi')

const duration = Joi.object()
  .keys({
    minutes: Joi.number().min(0),
    hours: Joi.number().min(0),
    days: Joi.number().min(0),
    weeks: Joi.number().min(0),
    months: Joi.number().integer().min(0),
    years: Joi.number().integer().min(0)
  })
  .or('years', 'months', 'weeks', 'days', 'hours', 'minutes')

const createBody = Joi.object({
  min_reservation_duration: duration
    .keys({
      minutes: Joi.number().min(30)
    })
    .required(),
  max_reservation_duration: duration.required(),
  booking_window_duration: duration.required()
}).required()

const createParams = Joi.object({
  fleet_id: Joi.number().positive().required()
})

const createQuery = Joi.object({
  parse: Joi.boolean()
})

const updateBody = Joi.object({
  min_reservation_duration: duration,
  max_reservation_duration: duration,
  booking_window_duration: duration
})
  .or(
    'min_reservation_duration',
    'max_reservation_duration',
    'booking_window_duration'
  )
  .required()

const updateParams = Joi.object({
  fleet_id: Joi.number().positive().required(),
  reservation_settings_id: Joi.number().positive().required()
}).required()

const activationParams = Joi.object({
  fleet_id: Joi.number().positive().required()
})

module.exports = {
  createBody,
  createParams,
  createQuery,
  updateBody,
  updateParams,
  activationParams
}
