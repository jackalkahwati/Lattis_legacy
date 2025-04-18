const moment = require('moment')
const { errors, responseCodes: codes } = require('@velo-labs/platform')

const db = require('../../db')

const parseDuration = (duration) => {
  const d = moment.duration(duration)

  return { ...d._data }
}

const retrieve = async (
  { reservationSettingsId, fleetId },
  options = {},
  context = {}
) => {
  if (!(fleetId || reservationSettingsId)) {
    throw errors.customError(
      'One of either "fleetId" or "reservationSettingsId" is required.',
      codes.BadRequest,
      'BadRequest'
    )
  }

  const builder = context.trx
    ? context.trx('reservation_settings')
    : db.main('reservation_settings')

  if (reservationSettingsId) {
    builder.where({ reservation_settings_id: reservationSettingsId })
  }

  if (fleetId) {
    builder.where({ fleet_id: fleetId })
  }

  const [reservationSettings] = await builder.select('*')

  if (!reservationSettings) {
    throw errors.customError(
      fleetId
        ? `No reservation settings found for fleet ${fleetId}.`
        : `Reservation settings id ${reservationSettingsId} not found.`,
      codes.ResourceNotFound,
      'NotFound'
    )
  }

  if (options.parse) {
    return {
      ...reservationSettings,
      min_reservation_duration: parseDuration(
        reservationSettings.min_reservation_duration
      ),
      max_reservation_duration: parseDuration(
        reservationSettings.max_reservation_duration
      ),
      booking_window_duration: parseDuration(
        reservationSettings.booking_window_duration
      )
    }
  }

  return reservationSettings
}

const validateDurations = (minReservationDuration, maxReservationDuration) => {
  // Use moment.duration twice here to workaround a bug where duration.toISOString()
  // would yield Invalid date when the duration data was a mix of absolute units
  // and fractional units that needed to be promoted. E.g, parsing the
  // duration moment.duration({hours: 1.75, minutes: 30}).toISOString(), yields
  // Invalid date, while parsing `moment.duration({hours: 2, minutes: 15}).toISOString()`
  // yields the right duration.
  // However, `moment.duration({hours: 1.75}).toISOString()` works fine.
  const min = moment.duration(
    parseDuration(moment.duration(minReservationDuration))
  )
  const max = moment.duration(
    parseDuration(moment.duration(maxReservationDuration))
  )

  const thirtyMinutes = 30 * 60 * 1000

  if (min.valueOf() < thirtyMinutes) {
    throw errors.customError(
      'Minimum duration should be more than 30 minutes.',
      codes.BadRequest,
      'BadRequest'
    )
  }

  if (max.valueOf() < min.valueOf()) {
    throw errors.customError(
      'Maximum reservation duration should be more than the minimum reservation duration.',
      codes.BadRequest,
      'BadRequest'
    )
  }

  return [min, max]
}

const validateParkingSpots = async (params, context = {}) => {
  const trx = context.trx || db.main
  const parkingSpots = await trx('parking_spots')
    .count('parking_spot_id', { as: 'count' })
    .where('fleet_id', params.fleet_id)
    .first()

  const parkingZones = await trx('parking_areas')
    .count('parking_area_id', { as: 'count' })
    .where('fleet_id', params.fleet_id)
    .first()

  if (parkingSpots.count > 1 || parkingZones.count > 1) {
    throw errors.customError(
      [
        'Reservations can only be enabled on fleets with a single parking spot or zone.',
        `This fleet has ${parkingSpots.count} parking spots and ${parkingZones.count} parking zones.`
      ].join(' '),
      codes.Conflict,
      'Conflict'
    )
  }
}

const create = async (params, options = {}, context = {}) => {
  const trx = context.trx || (await db.main.transaction())

  const [min, max] = validateDurations(
    params.min_reservation_duration,
    params.max_reservation_duration
  )

  await validateParkingSpots(params, { trx })

  const data = {
    fleet_id: params.fleet_id,
    min_reservation_duration: min.toISOString(),
    max_reservation_duration: max.toISOString(),
    booking_window_duration: moment
      .duration(params.booking_window_duration)
      .toISOString()
  }

  const [reservationSettingsId] = await trx('reservation_settings').insert(
    data
  )

  if (!context.trx) {
    await trx.commit()
  }

  return retrieve({ reservationSettingsId }, options, context)
}

const update = async ({ params, body, query }, context = {}) => {
  const {
    reservation_settings_id: reservationSettingsId,
    fleet_id: fleetId
  } = params

  const trx = context.trx || (await db.main.transaction())

  const reservationSettings = retrieve(
    { reservationSettingsId, fleetId },
    query,
    context.trx ? context : { trx }
  )

  const [min, max] = validateDurations(
    body.min_reservation_duration ||
      reservationSettings.min_reservation_duration,
    body.max_reservation_duration ||
      reservationSettings.max_reservation_duration
  )

  await trx('reservation_settings')
    .where('reservation_settings_id', reservationSettingsId)
    .update({
      min_reservation_duration: min.toISOString(),
      max_reservation_duration: max.toISOString(),
      ...(body.booking_window_duration
        ? {
          booking_window_duration: moment
            .duration(body.booking_window_duration)
            .toISOString()
        }
        : undefined)
    })

  if (!context.trx) {
    await trx.commit()
  }

  return retrieve({ reservationSettingsId }, query, context)
}

const deactivate = async (params) => {
  await db
    .main('reservation_settings')
    .where({ fleet_id: params.fleet_id, deactivation_date: null })
    .update({ deactivation_date: new Date() })

  return retrieve({ fleetId: params.fleet_id }, { parse: true })
}

const activate = async (params) => {
  const deactivated = await db
    .main('reservation_settings')
    .where({ fleet_id: params.fleet_id })
    .andWhere(function () {
      this.whereNotNull('deactivation_date')
    })
    .first()

  if (!deactivated) {
    return retrieve({ fleetId: params.fleet_id }, { parse: true })
  }

  await validateParkingSpots(params)

  await db
    .main('reservation_settings')
    .where({ fleet_id: params.fleet_id })
    .update({ deactivation_date: null })

  return retrieve({ fleetId: params.fleet_id }, { parse: true })
}

module.exports = {
  create,
  retrieve,
  update,
  activate,
  deactivate
}
