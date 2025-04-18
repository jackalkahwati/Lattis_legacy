const Joi = require('@hapi/joi')

const reservationSettings = require('./reservation-settings')

const listQuery = Joi.object({
  bike_id: Joi.number().positive().required()
}).required()

const retrieveParams = Joi.object({
  reservation_id: Joi.number().positive().required()
}).required()

module.exports = {
  listQuery,
  retrieveParams,

  reservationSettings
}
