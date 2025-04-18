const Joi = require('@hapi/joi')

const create = Joi.object({
  bike_id: Joi.number().positive().required(),
  reservation_start: Joi.date().min('now').required(),
  reservation_end: Joi.date().greater(Joi.ref('reservation_start')).required(),
  pricing_option_id: Joi.number().positive()
}).required()

const retrieve = Joi.object({
  reservation_id: Joi.number().positive().required()
}).required()

const list = Joi.object({
  bike_id: Joi.number().positive(),
  history: Joi.boolean()
})

const cancel = retrieve

const nextReservation = Joi.object({
  bike_id: Joi.number().positive()
})

const availableVehicles = Joi.object({
  fleet_id: Joi.number().positive().required(),
  reservation_start: Joi.date().min('now').required(),
  reservation_end: Joi.date().greater(Joi.ref('reservation_start')).required()
}).required()

module.exports = {
  create,
  retrieve,
  list,
  cancel,
  nextReservation,
  availableVehicles
}
