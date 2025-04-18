const moment = require('moment-timezone')

const { errors, responseCodes: codes } = require('@velo-labs/platform')

const db = require('../../db')
const bikeConstants = require('../../constants/bike-constants')
const dbHelpers = require('../../helpers/db')
const pricing = require('../pricing')

function validateReservationTimes ([start, end]) {
  if (!start.isValid()) {
    throw errors.customError(
      `"${start}" is not a valid time.`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  if (!end.isValid()) {
    throw errors.customError(
      `"${end}" is not a valid time.`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  const ranges = new Set([0, 30])

  if (!(ranges.has(start.minute()) && ranges.has(end.minute()))) {
    throw errors.customError(
      'The reservation time has to have a minute that is exactly the zeroth or thirtieth minute of an hour.',
      codes.BadRequest,
      'BadRequest'
    )
  }

  if (end.isSameOrBefore(start)) {
    throw errors.customError(
      'Reservation end time cannot be the same or before the start time.',
      codes.BadRequest,
      'BadRequest'
    )
  }

  return [start, end]
}

function validateReservationDurations ([start, end], reservationSettings) {
  const reservationDuration = moment.duration(end.diff(start))
  const minReservationDuration = moment.duration(
    reservationSettings.min_reservation_duration
  )
  const maxReservationDuration = moment.duration(
    reservationSettings.max_reservation_duration
  )

  if (reservationDuration.valueOf() < minReservationDuration.valueOf()) {
    throw errors.customError(
      `This reservation duration is less than the minimum allowable reservation duration of ${minReservationDuration.humanize()} for vehicles in this fleet.`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  if (reservationDuration.valueOf() > maxReservationDuration.valueOf()) {
    throw errors.customError(
      `This reservation duration is more than the maximum allowable reservation duration of ${maxReservationDuration.humanize()} for vehicles in this fleet.`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  const reservationStartDate = moment.utc(start)
  const bookingWindow = moment.duration(
    reservationSettings.booking_window_duration
  )
  const reservationBookingWindowEnd = moment().add(bookingWindow)

  if (reservationStartDate.isAfter(reservationBookingWindowEnd)) {
    throw errors.customError(
      `Cannot reserve this vehicle further than ${bookingWindow.humanize()} in the future.`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  return [start, end]
}

function conformReservationTimes ([start, end], fleetMetadata) {
  const tz = fleetMetadata.fleet_timezone

  if (!tz) {
    throw errors.customError(
      `Fleet ${fleetMetadata.fleet_id} is missing a timezone.`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  const startUTC = moment.utc(moment.tz(start, tz)).startOf('minute').format()
  const endUTC = moment.utc(moment.tz(end, tz)).startOf('minute').format()

  return [startUTC, endUTC]
}

async function fetchBikeFleetDetails (bikeId, { currentStatus } = {}) {
  const filters = {
    'bikes.bike_id': bikeId,
    'bikes.status': bikeConstants.status.active
  }

  const details = await db
    .main('bikes')
    .where(filters)
    .whereIn('bikes.current_status', currentStatus)
    .join('fleets', { 'fleets.fleet_id': 'bikes.fleet_id' })
    .join('fleet_metadata', { 'fleet_metadata.fleet_id': 'fleets.fleet_id' })
    .join('fleet_payment_settings', {
      'fleet_payment_settings.fleet_id': 'fleets.fleet_id'
    })
    .join('reservation_settings', function () {
      this.on('reservation_settings.fleet_id', '=', 'fleets.fleet_id').onNull(
        'reservation_settings.deactivation_date'
      )
    })
    .options({ nestTables: true })
    .first()
    .select('*')

  if (!details) {
    return details
  }

  const {
    bikes: bike,
    fleets: fleet,
    fleet_metadata: fleetMetadata,
    fleet_payment_settings: fleetPaymentSettings,
    reservation_settings: reservationSettings
  } = details

  if (fleetPaymentSettings) {
    if (fleetPaymentSettings.usage_surcharge === 'No') {
      fleetPaymentSettings.excess_usage_fees = 0.00
      fleetPaymentSettings.excess_usage_type_after_value = 0.00
      fleetPaymentSettings.excess_usage_type_value = 0.00
      fleetPaymentSettings.excess_usage_type = ''
      fleetPaymentSettings.excess_usage_type_after_type = ''
    }
  }

  return {
    bike,
    fleet,
    fleetMetadata,
    fleetPaymentSettings,
    reservationSettings
  }
}

async function fetchPortFleetDetails (portId) {
  const filters = {
    'ports.port_id': portId,
    'ports.status': 'Available',
    'hubs_and_fleets.type': 'parking_station'
  }

  let details = await db
    .main('ports')
    .where(filters)
    .join('hubs_and_fleets', 'hubs_and_fleets.hub_uuid', 'ports.hub_uuid')
    .join('fleets', { 'fleets.fleet_id': 'hubs_and_fleets.fleet_id' })
    .join('fleet_metadata', { 'fleet_metadata.fleet_id': 'fleets.fleet_id' })
    .join('fleet_payment_settings', {
      'fleet_payment_settings.fleet_id': 'fleets.fleet_id'
    })
    .join('reservation_settings', function () {
      this.on('reservation_settings.fleet_id', '=', 'fleets.fleet_id').onNull(
        'reservation_settings.deactivation_date'
      )
    })
    .options({ nestTables: true })
    .first()
    .select('*')

  if (!details) {
    return details
  }

  const {
    ports: port,
    fleets: fleet,
    fleet_metadata: fleetMetadata,
    fleet_payment_settings: fleetPaymentSettings,
    reservation_settings: reservationSettings
  } = details

  return {
    port,
    fleet,
    fleetMetadata,
    fleetPaymentSettings,
    reservationSettings
  }
}

async function fetchHubFleetDetails (hubId) {
  const hub = await db.main('hubs').where({hub_id: hubId, status: 'Available'}).first()
  if (!hub) return
  const filters = {
    'hubs_and_fleets.hub_uuid': hub.uuid,
    'hubs_and_fleets.type': 'parking_station',
    'hubs_and_fleets.status': 'live'
  }

  let details = await db
    .main('hubs_and_fleets')
    .where(filters)
    .join('fleets', { 'fleets.fleet_id': 'hubs_and_fleets.fleet_id' })
    .join('fleet_metadata', { 'fleet_metadata.fleet_id': 'fleets.fleet_id' })
    .join('fleet_payment_settings', {
      'fleet_payment_settings.fleet_id': 'fleets.fleet_id'
    })
    .join('reservation_settings', function () {
      this.on('reservation_settings.fleet_id', '=', 'fleets.fleet_id').onNull(
        'reservation_settings.deactivation_date'
      )
    })
    .options({ nestTables: true })
    .first()
    .select('*')

  if (!details) {
    return details
  }

  const {
    hubs_and_fleets: hubOnFleet,
    fleets: fleet,
    fleet_metadata: fleetMetadata,
    fleet_payment_settings: fleetPaymentSettings,
    reservation_settings: reservationSettings
  } = details

  return {
    hub: hubOnFleet,
    fleet,
    fleetMetadata,
    fleetPaymentSettings,
    reservationSettings
  }
}

async function retrieveOverlappingReservations (
  [reservationStart, reservationEnd],
  { filter, userId }
) {
  return db
    .main('reservations')
    .where(function () {
      this.where({ ...filter, reservation_terminated: null }).orWhere({
        user_id: userId,
        reservation_terminated: null
      })
    })
    .andWhere(function () {
      this
        // reservation that starts within these times
        .whereBetween('reservation_start', [reservationStart, reservationEnd])
        // reservation that ends within these times
        .orWhereBetween('reservation_end', [reservationStart, reservationEnd])
        // these times fall within a reservation
        .orWhere(function () {
          this.where('reservation_start', '<=', reservationStart).andWhere(
            'reservation_end',
            '>=',
            reservationEnd
          )
        })
    })
    .select()
}

const dateFormats = new Set(['DATETIME', 'TIMESTAMP'])

function castDataTypes (field, next) {
  if (dateFormats.has(field.type)) {
    const value = field.string()

    if (value) {
      return moment.utc(value, 'YYYY-MM-DD HH:mm:ss').format()
    }

    return value
  }

  return dbHelpers.castDataTypes(field, next)
}

function formatReservation (reservationRecord) {
  const {
    reservations: reservation,
    trip_payment_transactions: paymentTx,
    bikes: bike,
    fleets: fleet,
    addresses: address,
    bike_group: bikeGroup,
    fleet_payment_settings: paymentSettings
  } = reservationRecord

  if (paymentTx && paymentTx.id) {
    paymentTx.bike_unlock_fee = Number(paymentTx.bike_unlock_fee) || 0
    paymentTx.membership_discount = Number(paymentTx.membership_discount) || 0

    reservation.trip_payment_transaction = paymentTx
  }

  if (paymentSettings && paymentSettings.usage_surcharge === 'No') {
    paymentSettings.excess_usage_fees = 0.00
    paymentSettings.excess_usage_type_after_value = 0.00
    paymentSettings.excess_usage_type_value = 0.00
    paymentSettings.excess_usage_type = ''
    paymentSettings.excess_usage_type_after_type = ''
  }

  fleet.address = address
  fleet.fleet_payment_settings = {...paymentSettings, enable_preauth: !!paymentSettings.enable_preauth}
  fleet.skip_parking_image = !!fleet.skip_parking_image
  fleet.parking_area_restriction = !!fleet.parking_area_restriction

  bike.bike_group = bikeGroup
  bike.fleet = fleet

  reservation.bike = bike

  return reservation
}

async function retrieveReservation (
  { params, user, options = { includeCancelled: false } },
  context = {}
) {
  const filters = {
    'reservations.reservation_id': params.reservationId,
    'reservations.user_id': user.user_id
  }

  if (!options.includeCancelled) {
    filters['reservations.reservation_terminated'] = null
  }

  const reservationRecord = await (context.trx
    ? context.trx('reservations')
    : db.main('reservations')
  )
    .first()
    .where(filters)
    .join('bikes', 'bikes.bike_id', 'reservations.bike_id')
    .join('bike_group', 'bike_group.bike_group_id', 'bikes.bike_group_id')
    .join('fleets', 'fleets.fleet_id', 'bikes.fleet_id')
    .join('addresses', function () {
      this.on('addresses.type', '=', db.main.raw('?', ['fleet'])).andOn(
        'addresses.type_id',
        '=',
        'fleets.fleet_id'
      )
    })
    .leftJoin('trip_payment_transactions', {
      'trip_payment_transactions.reservation_id': 'reservations.reservation_id'
    })
    .leftOuterJoin(
      'fleet_payment_settings',
      'fleet_payment_settings.fleet_id',
      'fleets.fleet_id'
    )
    .options({
      nestTables: true,
      typeCast: castDataTypes
    })
    .select('*')

  if (!reservationRecord) {
    return null
  }

  return formatReservation(reservationRecord)
}

async function createReservation ({ params, user }, context = {}) {
  const trx = context.trx || (await db.main.transaction())

  const [reservationId] = await trx('reservations').insert(params)

  // If we are reserving this bike less than 30 minutes to time, mark the bike as
  // reserved to ensure other people cannot book it via the create-booking
  // endpoint.
  if (moment.utc(params.reservation_start).diff(moment(), 'm') <= 30) {
    if (params.bike_id) {
      await trx('bikes')
        .where({ bike_id: params.bike_id })
        .update({ current_status: bikeConstants.current_status.reserved })
    } else if (params.port_id) {
      await trx('ports')
        .where({ port_id: params.port_id })
        .update({ current_status: bikeConstants.current_status.reserved })
    } else if (params.hub_id) {
      const hub = await trx('hubs').where({ hub_id: params.hub_id }).first()
      await trx('hubs_and_fleets')
        .where({ hub_uuid: hub.uuid })
        .update({ current_status: bikeConstants.current_status.reserved })
    }
  }

  if (!context.trx) {
    await trx.commit()
  }

  return retrieveReservation({ user, params: { reservationId } }, context)
}

async function createTrip (
  { reservation, fleet, params: requestDetails },
  context = {}
) {
  const params = {
    user_id: reservation.user_id,
    bike_id: reservation.bike_id || null,
    hub_id: reservation.hub_id || null,
    port_id: reservation.port_id || null,
    fleet_id: fleet.fleet_id,
    reservation_id: reservation.reservation_id,
    steps: JSON.stringify([[]])
  }

  if (requestDetails.pricing_option_id) {
    const pricingOption = await pricing.handlers.retrieve({
      id: requestDetails.pricing_option_id,
      fleetId: fleet.fleet_id
    })

    if (!pricingOption) {
      throw errors.customError(
        `Invalid pricing option: ${requestDetails.pricing_option_id}`,
        codes.BadRequest,
        'BadRequest'
      )
    }

    params.pricing_option_id = requestDetails.pricing_option_id
  }

  const trx = context.trx || (await db.main.transaction())

  const [tripId] = await trx('trips').insert(params)

  if (!context.trx) {
    await trx.commit()
  }

  return db.main('trips').where('trip_id', tripId).select().first()
}

async function cancelReservation (reservation, user) {
  const terminatedAt = moment.utc()
  const trx = await db.main.transaction()

  const currentDetails = await trx('reservations')
    .where({
      'reservations.reservation_id': reservation.reservation_id,
      'reservations.reservation_terminated': null
    })
    .andWhere(function () {
      this.where('reservations.reservation_start', '<=', terminatedAt.format())
    })
    .join('trips', { 'trips.reservation_id': 'reservations.reservation_id' })
    .first()
    .options({ nestTables: true })
    .select()

  if (currentDetails) {
    const { trips: currentTrip } = currentDetails

    if (currentTrip.date_created && !currentTrip.date_endtrip) {
      throw errors.customError(
        'Cannot cancel a reservation whose trip has started. Please end the trip instead.',
        codes.Conflict,
        'Conflict'
      )
    }

    if (currentTrip.date_endtrip) {
      throw errors.customError(
        'Cannot cancel a reservation whose trip has ended.',
        codes.BadRequest,
        'BadRequest'
      )
    }
  }

  await trx('reservations')
    .where({
      reservation_id: reservation.reservation_id,
      reservation_terminated: null
    })
    .update({
      reservation_terminated: terminatedAt.format(),
      termination_reason: 'cancellation'
    })

  await trx('trips')
    .where({
      reservation_id: reservation.reservation_id,
      date_endtrip: null
    })
    .update({
      start_address: 'Reservation Cancelled',
      end_address: 'Reservation Cancelled',
      date_created: moment.utc(reservation.reservation_start).unix(),
      date_endtrip: moment.utc(reservation.reservation_start).unix()
    })

  if (reservation.bike_id) {
    await trx('bikes')
      .where({ bike_id: reservation.bike_id })
      .update({ current_status: 'parked' })
  } else if (reservation.port_id) {
    await trx('ports')
      .where({ port_id: reservation.port_id })
      .update({ current_status: 'parked' })
  } else if (reservation.hub_id) {
    await trx('hubs')
      .where({ hub_id: reservation.hub_id })
      .update({ current_status: 'parked' })
  }

  await trx.commit()

  return retrieveReservation({
    params: { reservationId: reservation.reservation_id },
    user: user || { user_id: reservation.user_id },
    options: { includeCancelled: true }
  })
}

async function endTripReservation ({ trip }, trx) {
  const user = { user_id: trip.user_id }

  await trx('reservations')
    .where('reservation_id', trip.reservation_id)
    .update({
      reservation_terminated: moment
        .utc(moment.unix(trip.date_endtrip))
        .format(),
      termination_reason: 'trip_end'
    })

  return retrieveReservation({
    user,
    params: { reservationId: trip.reservation_id },
    options: { includeCancelled: true }
  })
}

async function retrieveNextReservation ({ bikeId, userId }) {
  const filters = { 'trips.date_created': null }

  if (bikeId) {
    filters['reservations.bike_id'] = bikeId
  } else {
    filters['reservations.user_id'] = userId
  }

  const record = await db
    .main('reservations')
    .where(filters)
    .andWhere(function () {
      this.where('reservation_start', '>', moment.utc().format()).whereNull(
        'reservation_terminated'
      )
    })
    .orderBy('reservation_start')
    .join('trips', 'trips.reservation_id', 'reservations.reservation_id')
    .join('bikes', 'bikes.bike_id', 'reservations.bike_id')
    .join('bike_group', 'bike_group.bike_group_id', 'bikes.bike_group_id')
    .join('fleets', 'fleets.fleet_id', 'bikes.fleet_id')
    .join('addresses', function () {
      this.on('addresses.type', '=', db.main.raw('?', ['fleet'])).andOn(
        'addresses.type_id',
        '=',
        'fleets.fleet_id'
      )
    })
    .leftJoin('trip_payment_transactions', {
      'trip_payment_transactions.reservation_id': 'reservations.reservation_id'
    })
    .leftOuterJoin(
      'fleet_payment_settings',
      'fleet_payment_settings.fleet_id',
      'fleets.fleet_id'
    )
    .first()
    .options({ typeCast: castDataTypes, nestTables: true })
    .select()

  if (!record) {
    return null
  }

  return formatReservation(record)
}

async function retrieveCurrentReservation ({ user }) {
  const now = moment.utc().format()

  const record = await db
    .main('reservations')
    .where({
      'reservations.reservation_terminated': null,
      'reservations.user_id': user.user_id,
      'trips.date_created': null
    })
    .andWhere(function () {
      this.where('reservations.reservation_start', '<=', now).andWhere(
        'reservations.reservation_end',
        '>=',
        now
      )
    })
    .join('trips', 'trips.reservation_id', 'reservations.reservation_id')
    .join('bikes', 'bikes.bike_id', 'reservations.bike_id')
    .join('bike_group', 'bike_group.bike_group_id', 'bikes.bike_group_id')
    .join('fleets', 'fleets.fleet_id', 'bikes.fleet_id')
    .join('addresses', function () {
      this.on('addresses.type', '=', db.main.raw('?', ['fleet'])).andOn(
        'addresses.type_id',
        '=',
        'fleets.fleet_id'
      )
    })
    .leftJoin('trip_payment_transactions', {
      'trip_payment_transactions.reservation_id': 'reservations.reservation_id'
    })
    .leftOuterJoin(
      'fleet_payment_settings',
      'fleet_payment_settings.fleet_id',
      'fleets.fleet_id'
    )
    .first()
    .options({ typeCast: castDataTypes, nestTables: true })
    .select()

  if (!record) {
    return null
  }

  return formatReservation(record)
}

async function retrieveAvailableVehiclesInFleet ({
  fleetId,
  reservationStart,
  reservationEnd
}) {
  const unavailableVehicles = db
    .main('reservations')
    .whereIn(
      'bike_id',
      db.main('bikes').where({ fleet_id: fleetId }).select('bike_id')
    )
    .andWhere({ reservation_terminated: null })
    .andWhere(function () {
      this
        // reservation that starts within these times
        .whereBetween('reservation_start', [reservationStart, reservationEnd])
        // reservation that ends within these times
        .orWhereBetween('reservation_end', [reservationStart, reservationEnd])
        // these times fall within a reservation
        .orWhere(function () {
          this.where('reservation_start', '<=', reservationStart).andWhere(
            'reservation_end',
            '>=',
            reservationEnd
          )
        })
    })
    .select('bike_id')
  const filters = {
    'bikes.fleet_id': fleetId,
    'bikes.status': bikeConstants.status.active
  }
  const availableVehicles = await db
    .main('bikes')
    .where(filters)
    .whereIn('bikes.current_status', [
      bikeConstants.current_status.parked,
      bikeConstants.current_status.collect
    ])
    .andWhere(function () {
      this.whereNotIn('bikes.bike_id', unavailableVehicles)
    })
    .join('fleets', 'fleets.fleet_id', 'bikes.fleet_id')
    .join('bike_group', 'bike_group.bike_group_id', 'bikes.bike_group_id')
    .join('addresses', function () {
      this.on('addresses.type', '=', db.main.raw('?', ['fleet'])).andOn(
        'addresses.type_id',
        '=',
        'fleets.fleet_id'
      )
    })
    .leftOuterJoin(
      'fleet_payment_settings',
      'fleet_payment_settings.fleet_id',
      'fleets.fleet_id'
    )
    .options({ nestTables: true, typeCast: dbHelpers.castDataTypes })
    .select()
    .groupBy('bikes.bike_id')

  return availableVehicles.map(
    ({
      bikes: bike,
      fleets: fleet,
      addresses: address,
      bike_group: bikeGroup,
      fleet_payment_settings: paymentSettings
    }) => {
      fleet.address = address
      const usageSurchage = {}
      if (paymentSettings.usage_surcharge === 'No') {
        usageSurchage.excess_usage_type_after_value = 0.00
        usageSurchage.excess_usage_fees = 0.00
        usageSurchage.excess_usage_type_value = 0.00
        usageSurchage.excess_usage_type = ''
        usageSurchage.excess_usage_type_after_type = ''
      }
      if (['public', 'private'].includes(fleet.type)) fleet.fleet_payment_settings = {...paymentSettings, ...usageSurchage, enable_preauth: !!paymentSettings.enable_preauth}
      else fleet.fleet_payment_settings = null
      bike.bike_group = bikeGroup
      bike.fleet = fleet

      return bike
    }
  )
}

module.exports = {
  castDataTypes,
  formatReservation,
  createReservation,
  cancelReservation,
  retrieveReservation,
  fetchBikeFleetDetails,
  retrieveOverlappingReservations,
  conformReservationTimes,
  validateReservationTimes,
  validateReservationDurations,
  endTripReservation,
  retrieveNextReservation,
  retrieveCurrentReservation,
  retrieveAvailableVehiclesInFleet,
  createTrip,
  fetchPortFleetDetails,
  fetchHubFleetDetails
}
