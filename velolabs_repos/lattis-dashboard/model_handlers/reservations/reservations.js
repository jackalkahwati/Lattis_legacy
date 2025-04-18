const moment = require('moment')
const { errors, responseCodes: codes } = require('@velo-labs/platform')

const db = require('../../db')

const helpers = require('./helpers')

/**
 * List reservations.
 *
 * If a `bike_id` is provided in the query, list all reservations from
 * all users for that bike.
 * Otherwise, list only the reservations for the requesting user
 *
 * @param {Object} params
 * @param {Object} [params]
 * @param {string} [params.bike_id]
 */
async function list ({ bike_id: bikeId }) {
  return db
    .main('reservations')
    .where({ bike_id: bikeId })
    .andWhere(function () {
      this
        .where('reservation_end', '>', moment.utc().format())
        .whereNull('reservation_terminated')
    })
    .options({ typeCast: helpers.castToUTCDateTime })
    .select()
}

async function retrieve ({ reservation_id: reservationId }, context = {}) {
  const reservation = await (context.trx
    ? context.trx('reservations')
    : db.main('reservations')
  )
    .first()
    .where({ reservation_id: reservationId })
    .options({
      typeCast: helpers.castToUTCDateTime
    })
    .select()

  if (!reservation) {
    throw errors.customError(
      'Reservation not found',
      codes.ResourceNotFound,
      'NotFound'
    )
  }

  return reservation
}

module.exports = {
  list,
  retrieve
}
