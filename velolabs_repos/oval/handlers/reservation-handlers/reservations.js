const util = require('util')

const moment = require('moment')
const { errors, logger, responseCodes: codes } = require('@velo-labs/platform')

const db = require('../../db')
const tripHandler = require('../trip-handler')
const conversions = require('../../utils/conversions')
const bikeConstants = require('../../constants/bike-constants')
const fleetConstants = require('../../constants/fleet-constants')
const subscriptionHelpers = require('../fleet-membership-handlers/helpers')

const emails = require('./emails')
const helpers = require('./helpers')
const payments = require('../payments')
const bikeHandler = require('../bike-handler')
const { GATEWAYS } = require('../payments/gateways')
const {configureSentry} = require('../../utils/configureSentry')

const Sentry = configureSentry()

const getDurationCost = util.promisify(tripHandler.getDurationCost)
const getBikeWithLock = util.promisify(bikeHandler.getBikeWithLock)

async function create ({ params, user }) {
  let reservationType
  if (params.bike_id) reservationType = 'bike'
  else if (params.port_id) reservationType = 'port'
  else if (params.hub_id) reservationType = 'hub'
  let details
  if (params.bike_id) {
    details = await helpers.fetchBikeFleetDetails(params.bike_id, {
      currentStatus: [
        bikeConstants.current_status.parked,
        bikeConstants.current_status.collect
      ]
    })
  } else if (params.port_id) {
    details = await helpers.fetchPortFleetDetails(params.port_id)
  } else if (params.hub_id) {
    details = await helpers.fetchHubFleetDetails(params.hub_id)
  }

  if (!details) {
    throw errors.customError(
      `${reservationType} ${
        params[reservationType + '_id']
      } is unavailable for reservation.`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  const { fleet, fleetMetadata, fleetPaymentSettings, reservationSettings } =
    details

  const [reservationStart, reservationEnd] = helpers.conformReservationTimes(
    [params.reservation_start, params.reservation_end],
    fleetMetadata
  )

  const rstart = moment.utc(reservationStart)
  const rend = moment.utc(reservationEnd)

  helpers.validateReservationTimes([rstart, rend])
  helpers.validateReservationDurations([rstart, rend], reservationSettings)

  let overlappingReservations

  let createReservationParams = {}
  if (reservationType === 'bike') {
    const filter = { bike_id: details.bike.bike_id }
    overlappingReservations = await helpers.retrieveOverlappingReservations(
      [reservationStart, reservationEnd],
      { filter, userId: user.user_id }
    )
    createReservationParams = filter
  } else if (reservationType === 'port') {
    const filter = { port_id: details.port.port_id }
    overlappingReservations = await helpers.retrieveOverlappingReservations(
      [reservationStart, reservationEnd],
      { filter, userId: user.user_id }
    )
    createReservationParams = filter
  } else if (reservationType === 'hub') {
    const filter = { hub_id: details.hub.hub_id }
    overlappingReservations = await helpers.retrieveOverlappingReservations(
      [reservationStart, reservationEnd],
      { filter, userId: user.user_id }
    )
    createReservationParams = filter
  }

  if (overlappingReservations.length) {
    throw errors.customError(
      `This user has a reservation or this vehicle is reserved for some time in the provided period.`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  if (payments.isMercadoPagoConnect(fleetPaymentSettings)) {
    const userPaymentProfile = await db
      .main('user_payment_profiles')
      .where({
        user_id: user.user_id,
        payment_gateway: GATEWAYS.mercadopago,
        source_fleet_id: fleet.fleet_id
      })
      .first()

    if (!userPaymentProfile) {
      throw errors.customError(
        'This user does not have a payment method that can pay into this fleet',
        codes.Conflict,
        'Conflict'
      )
    }
  }

  const reservation = await helpers.createReservation({
    user,
    params: {
      ...createReservationParams,
      reservation_start: reservationStart,
      reservation_end: reservationEnd,
      reservation_timezone: fleetMetadata.fleet_timezone,
      user_id: user.user_id
    }
  })

  const trip = await helpers.createTrip({ reservation, fleet, params })

  const rollback = async () => {
    const trx = await db.main.transaction()

    if (trip.transaction_id) {
      await trx('trip_payment_transactions')
        .where('trip_id', trip.trip_id)
        .del()
    }

    await trx('trips').where('trip_id', trip.trip_id).del()

    if (reservation.bike_id) {
      await trx('bikes')
        .where({ bike_id: reservation.bike_id, current_status: 'reserved' })
        .update({ current_status: bikeConstants.current_status.parked })
    } else if (reservation.port_id) {
      await trx('ports')
        .where({ port_id: reservation.port_id, current_status: 'reserved' })
        .update({ current_status: 'available' })
    } else if (reservation.hub_id) {
      await trx('hubs')
        .where({ hub_id: reservation.hub_id, current_status: 'reserved' })
        .update({ current_status: 'available' })
    }

    await trx('reservations')
      .where('reservation_id', reservation.reservation_id)
      .del()

    await trx.commit()
  }

  const fleetDetails = {
    ...fleet,
    ...fleetMetadata,
    ...fleetPaymentSettings
  }

  let payment = {}

  try {
    payment = await tripHandler.performTripCharge(trip, fleetDetails, {
      reservation
    })
  } catch (error) {
    Sentry.captureException(error)
    logger(
      'Error effecting payment for reservation',
      { reservation, trip, payment },
      error
    )

    await rollback()

    throw error
  }

  if (payment.charge) {
    if (payment.charge.id) {
      if (payment.error) {
        logger(
          'Partial errors while charging reservation',
          { reservation, trip, payment, error: payment.error.stack },
          payment.error
        )

        if (payment.charge.id) {
          await payments.refunds.refund(
            { payment_intent: payment.charge.id },
            { stripe_account: fleetDetails.stripe_account_id }
          )
        }

        await rollback()

        throw errors.customError(
          'Failed to charge for this reservation',
          codes.InternalServer,
          'InternalServer'
        )
      }

      return { reservation, payment }
    }

    const processedSuccessfully = !payment.charge.id && !payment.error

    // Trip wasn't charged since discount was applied to entire charge amount
    if (processedSuccessfully && payment.trip.membership_discount > 0) {
      return { reservation, payment }
    }

    // Trip wasn't charged because price for membership is zero
    if (
      processedSuccessfully &&
      Number(fleetPaymentSettings.price_for_membership) === 0
    ) {
      return { reservation, payment }
    }
  }

  logger(
    'Error creating reservation',
    { reservation, trip, payment },
    payment.error
  )

  await rollback()

  if (payment.error) {
    const e = payment.error
    e.originalError = payment.originalError

    throw e
  }

  throw errors.customError(
    'Could not create a reservation',
    codes.InternalServer,
    'InternalServer'
  )
}

async function createAndNotify ({ params, user }) {
  try {
    const { reservation, payment } = await create({ params, user })

    emails.reservationCreated({
      reservation,
      charge: payment.charge,
      trip: payment.trip
    })

    return helpers.retrieveReservation({
      params: { reservationId: reservation.reservation_id },
      user
    })
  } catch (error) {
    Sentry.captureException(error)
    throw error
  }
}

async function retrieve ({ params: { reservation_id: reservationId }, user }) {
  const reservation = await helpers.retrieveReservation({
    params: { reservationId },
    user,
    options: { includeCancelled: true }
  })

  if (!reservation) {
    throw errors.customError(
      `Reservation id ${reservationId} not found.`,
      codes.ResourceNotFound,
      'NotFound'
    )
  }

  return reservation
}

/**
 * List reservations.
 *
 * If a `bike_id` is provided in the query, list all reservations from
 * all users for that bike.
 * Otherwise, list only the reservations for the requesting user
 *
 * @param {Object} params
 * @param {Object} [params.query]
 * @param {string} [params.query.bike_id]
 * @param {Object} params.user
 * @param {number} params.user.user_id
 */
async function list ({ query = {}, user }) {
  const where = query.history
    ? {}
    : {
      'trips.date_created': null
    }

  if (query.bike_id) {
    where['reservations.bike_id'] = query.bike_id
  } else if (query.port_id) {
    where['reservations.port_id'] = query.port_id
  } else if (query.hub_id) {
    where['reservations.hub_id'] = query.hub_id
  }

  let hub
  if (where['reservations.port_id']) {
    const port = await db
      .main('ports')
      .where({ port_id: where['reservations.port_id'] })
      .select('hub_uuid')
      .first()
    hub = await db
      .main('hubs_and_fleets')
      .where({ hub_uuid: port.hub_uuid })
      .first()
  } else if (where['reservations.hub_id']) {
    const hubInfo = await db
      .main('hubs')
      .where({ hub_id: where['reservations.hub_id'] })
      .select('hub_uuid')
      .first()
    hub = await db
      .main('hubs_and_fleets')
      .where({ hub_uuid: hubInfo.hub_uuid })
      .first()
  }

  if (!where['reservations.bike_id']) {
    where['reservations.user_id'] = user.user_id
  }

  const builder = db.main('reservations').where(where)

  if (!query.history) {
    builder.andWhere(function () {
      this.where(
        'reservations.reservation_end',
        '>',
        moment.utc().format()
      ).andWhere('reservations.reservation_terminated', null)
    })
  }
  if (where['reservations.port_id']) {
    builder.join('ports', 'ports.port_id', 'reservations.port_id')
  }
  if (where['reservations.hub_id']) {
    builder.join('hubs', 'hubs.hub_id', 'reservations.hub_id')
  }
  builder
    .join('trips', 'trips.reservation_id', 'reservations.reservation_id')
    .join('bikes', 'bikes.bike_id', 'reservations.bike_id')
    .join('bike_group', 'bike_group.bike_group_id', 'bikes.bike_group_id')
    .join(
      'fleets',
      'fleets.fleet_id',
      (hub && hub.fleet_id) || 'bikes.fleet_id'
    )
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
    .options({ typeCast: helpers.castDataTypes, nestTables: true })
    .select()
    .groupBy('reservations.reservation_id')

  const records = await builder

  if (!records) {
    return []
  }

  return records.map(helpers.formatReservation)
}

async function cancel (payload) {
  const reservation = await retrieve(payload)

  const cancelled = await helpers.cancelReservation(reservation)

  // TODO: (waiyaki) Maybe inform the fleet operator of the cancellation
  return cancelled
}

async function priceEstimate ({
  params: {
    bike_id: bikeId,
    port_id: portId,
    hub_id: hubId,
    reservation_start: rStart,
    reservation_end: rEnd,
    pricing_option_id: pricingOptionId
  },
  user
}) {
  let details
  if (bikeId) {
    details = await helpers.fetchBikeFleetDetails(bikeId, {
      currentStatus: [
        bikeConstants.current_status.parked,
        bikeConstants.current_status.collect
      ]
    })
  } else if (portId) {
    details = await helpers.fetchPortFleetDetails(portId, {
      currentStatus: [
        bikeConstants.current_status.parked,
        bikeConstants.current_status.collect
      ]
    })
  } else if (hubId) {
    details = await helpers.fetchHubFleetDetails(hubId, {
      currentStatus: [
        bikeConstants.current_status.parked,
        bikeConstants.current_status.collect
      ]
    })
  }

  if (!details) {
    throw errors.customError(
      `This vehicle is unavailable for reservation.`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  const durationCost = await getDurationCost({
    type: fleetConstants.tripInfo.endTrip,
    fleet_id: details.fleet.fleet_id,
    date_created: moment(rStart).unix(),
    date_endtrip: moment(rEnd).unix(),
    trip: { pricing_option_id: pricingOptionId }
  })

  const bikeUnlockFee = durationCost.bike_unlock_fee || 0
  const totalCharge =
    durationCost.amount + durationCost.excess_fees + bikeUnlockFee

  const chargeForDuration = Number.isFinite(totalCharge)
    ? conversions.roundOffPricing(totalCharge)
    : 0.0

  const { discount, discountedAmount, taxInfo } =
    await subscriptionHelpers.applyMembershipIncentive({
      userId: user.user_id,
      fleetId: details.fleet.fleet_id,
      amount: chargeForDuration
    })

  const payload = {
    bike_id: details.bike.bike_id,
    hub_id: details.hub ? details.hub.hub_id : undefined,
    port_id: details.port ? details.port.port_id : undefined,
    duration: durationCost.duration,
    amount: chargeForDuration,
    membership_discount: discount,
    charge_for_duration: discountedAmount,
    currency: durationCost.currency,
    taxes: (taxInfo && taxInfo.taxes) ? JSON.stringify(taxInfo.taxes) : null,
    tax_sub_total: (taxInfo && taxInfo.taxTotal) || null
  }

  return payload
}

async function retrieveNextReservation ({ query, user }) {
  const next = await helpers.retrieveNextReservation({
    bikeId: query.bike_id,
    userId: user.user_id
  })

  return next || null
}

async function availableVehicles ({
  params: {
    fleet_id: fleetId,
    reservation_start: rstart,
    reservation_end: rend
  }
}) {
  const details = await db
    .main('reservation_settings')
    .where({
      'reservation_settings.fleet_id': fleetId,
      'reservation_settings.deactivation_date': null
    })
    .join('fleet_metadata', {
      'fleet_metadata.fleet_id': 'reservation_settings.fleet_id'
    })
    .options({ nestTables: true })
    .first()
    .select()

  if (!details) {
    return []
  }

  const [reservationStart, reservationEnd] = helpers.conformReservationTimes(
    [rstart, rend],
    details.fleet_metadata
  )

  const start = moment.utc(reservationStart)
  const end = moment.utc(reservationEnd)

  helpers.validateReservationTimes([start, end])
  helpers.validateReservationDurations(
    [start, end],
    details.reservation_settings
  )

  const available = await helpers.retrieveAvailableVehiclesInFleet({
    fleetId,
    reservationStart,
    reservationEnd
  })

  if (!(available && available.length > 0)) {
    return []
  }

  return available
}

async function startTrip ({ params, user }) {
  const reservation = await helpers.retrieveReservation({
    params: { reservationId: params.reservation_id },
    user
  })

  if (!reservation) {
    throw errors.customError(
      'Reservation not found.',
      codes.ResourceNotFound,
      'NotFound'
    )
  }

  if (moment.utc(reservation.reservation_start).isAfter(moment().utc())) {
    throw errors.customError(
      'Cannot start the reservation trip before the reservation time.',
      codes.Conflict,
      'Conflict'
    )
  }

  const details = await db
    .main('trips')
    .where({ 'trips.reservation_id': reservation.reservation_id })
    .join('fleet_metadata', { 'fleet_metadata.fleet_id': 'trips.fleet_id' })
    .first()
    .options({ nestTables: true })
    .select()

  if (!details) {
    throw errors.customError(
      'Cannot start reservation trip. This reservation is missing the automatically created trip.',
      codes.Conflict,
      'Conflict'
    )
  }

  const { trips: trip, fleet_metadata: metadata } = details

  if (trip.date_endtrip) {
    throw errors.customError(
      'The trip for this reservation was ended and cannot be restarted.',
      codes.BadRequest,
      'BadRequest'
    )
  }

  // If the trip was already previously started, return it.
  if (trip.date_created && !trip.date_endtrip) {
    trip.do_not_track_trip = metadata.do_not_track_trip === 1
    trip.steps = JSON.parse(trip.steps)

    return trip
  }

  let hubInfo
  let bikeInfo
  let portInfo

  if (reservation.bike_id) {
    const [bike] = await getBikeWithLock({ bike_id: reservation.bike_id })
    bikeInfo = bike

    if (bike.current_status === bikeConstants.current_status.onTrip) {
      throw errors.customError(
        `Vehicle ${bike.bike_id} is currently on trip.`,
        codes.Conflict,
        'Conflict'
      )
    }
  }

  if (reservation.port_id) {
    const port = await db.main('ports').where({ port_id: reservation.port_id })
    portInfo = port

    if (port.current_status === bikeConstants.current_status.onTrip) {
      throw errors.customError(
        `Port ${reservation.port_id} is currently on trip.`,
        codes.Conflict,
        'Conflict'
      )
    }
  }

  if (reservation.hub_id) {
    const hub = await db
      .main('hubs')
      .where({ hub_id: reservation.hub_id })
      .first()
    hubInfo = hub

    if (hub.current_status === bikeConstants.current_status.onTrip) {
      throw errors.customError(
        `Hub ${reservation.hub_id} is currently on trip.`,
        codes.Conflict,
        'Conflict'
      )
    }
  }

  return tripHandler.startReservationTrip({
    reservation,
    bike: bikeInfo,
    port: portInfo,
    hub: hubInfo,
    user,
    trip
  })
}

module.exports = {
  create,
  createAndNotify,
  retrieve,
  list,
  cancel,

  priceEstimate,
  retrieveNextReservation,
  availableVehicles,
  startTrip
}
