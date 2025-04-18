const _ = require('underscore')
const moment = require('moment')
const { CronJob } = require('cron')
const { logger } = require('@velo-labs/platform')

const db = require('../../db')
const payments = require('../payments')
const bikeConstants = require('../../constants/bike-constants')

const { castDataTypes } = require('./helpers')
const {
  reservationTripStarted,
  upcomingReservation,
  onTripVehicleHasUpcomingReservation,
  upcomingReservationWithUnavailableVehicle
  // lateReturnFeeCharged
} = require('./emails')

function reservationsAt ({ basis }, { bikeStatus = ['parked', 'collect'] } = {}) {
  return db
    .main('reservations')
    .where({
      'reservations.reservation_start': basis.format(),
      'reservations.reservation_terminated': null
    })
    .join('bikes', function () {
      this.on('bikes.bike_id', '=', 'reservations.bike_id').onIn(
        'bikes.current_status',
        '=',
        db.main.raw('?', bikeStatus)
      )
    })
    .join('fleets', { 'fleets.fleet_id': 'bikes.fleet_id' })
    .options({ nestTables: true, typeCast: castDataTypes })
    .select()
}

function currentReservations ({ basis }) {
  return reservationsAt({ basis })
}

function upcomingReservations ({ basis }) {
  const nextT = basis.clone().add(30, 'minutes').startOf('minute')

  return reservationsAt({ basis: nextT })
}

function upcomingReservationsWithOnTripVehicles ({ basis }) {
  const anHourFrom = basis.clone().add(1, 'hour').startOf('minute')

  return reservationsAt({ basis: anHourFrom }, { bikeStatus: 'on_trip' })
}

function upcomingReservationsWithParkedVehicles ({
  basis,
  basisDiff = 1,
  basisDiffUnit = 'hour'
}) {
  const basisFrom = basis
    .clone()
    .add(basisDiff, basisDiffUnit)
    .startOf('minute')

  return reservationsAt({ basis: basisFrom }, { bikeStatus: ['parked', 'collect'] })
}

function retrieveUpcomingReservationsWithUnavailableVehicles ({ basis }) {
  const nextT = basis.clone().add(30, 'minutes').startOf('minute')

  return db
    .main('reservations')
    .where({
      'reservations.reservation_start': nextT.format(),
      'reservations.reservation_terminated': null
    })
    .whereNotIn('bikes.current_status', ['parked', 'reserved', 'collect'])
    .join('bikes', {'bikes.bike_id': 'reservations.bike_id'})
    .join('fleets', { 'fleets.fleet_id': 'bikes.fleet_id' })
    .join('fleet_payment_settings', {
      'fleet_payment_settings.fleet_id': 'fleets.fleet_id'
    })
    .options({ nestTables: true, typeCast: castDataTypes })
    .groupBy('reservations.reservation_id')
    .select()
}

function retrieveReservationPaymentTransactions (reservations) {
  return db
    .main('trip_payment_transactions')
    .whereIn(
      'trip_payment_transactions.reservation_id',
      reservations.map((r) => r.reservation_id)
    )
    .join('fleet_payment_settings', {
      'fleet_payment_settings.fleet_id': 'trip_payment_transactions.fleet_id'
    })
    .select(
      'trip_payment_transactions.transaction_id',
      'fleet_payment_settings.stripe_account_id'
    )
}

function retrieveElapsedReservations ({ basis }) {
  return db
    .main('reservations')
    .where('reservations.reservation_end', basis.format())
    .join('trips', function () {
      this.on(
        'trips.reservation_id',
        '=',
        'reservations.reservation_id'
      ).onNull('trips.date_created')
    })
    .join('bikes', function () {
      this.on('bikes.bike_id', '=', 'reservations.bike_id').on(
        'bikes.current_status',
        '=',
        db.main.raw('?', [bikeConstants.current_status.reserved])
      )
    })
    .options({ nestTables: true, typeCast: castDataTypes })
    .select()
}

async function retrieveUsers (records) {
  const userIds = [...new Set(records.map((record) => record.user_id))]

  const users = await db
    .users('users')
    .whereIn('user_id', userIds)
    .select('user_id', 'email')

  return _.groupBy(users, 'user_id')
}

async function retrieveActiveBookings (bikeIds) {
  return db
    .main('booking')
    .where(function () {
      this.whereIn('bike_id', bikeIds).andWhere('status', 'active')
    })
    .select('user_id', 'bike_id', 'fleet_id')
}

function reflect (entity, promise) {
  return promise.then(
    (result) => ({ entity, result, status: 'fulfilled' }),
    (result) => ({
      entity,
      status: 'rejected',
      result: result instanceof Error ? result.stack : result
    })
  )
}

function conform (e) {
  return [{ result: e instanceof Error ? e.stack : e, status: 'rejected' }]
}

function resolveUser (usersById, record) {
  return _.first(usersById[record.user_id])
}

async function notifyUsers ({ records, notificationFn, users }) {
  if (records.length === 0) {
    return []
  }

  const usersById =
    users || (await retrieveUsers(records.map((c) => c.reservations)))

  return Promise.all(
    records.map((record) =>
      reflect(
        record.reservations
          ? { reservation_id: record.reservations.reservation_id }
          : record,
        notificationFn(
          record,
          resolveUser(
            usersById,
            record.reservations ? record.reservations : record
          )
        )
      )
    )
  )
}

async function notifyCurrentReservations ({ basis }) {
  const current = await currentReservations({ basis })

  return notifyUsers({
    records: current,
    notificationFn: reservationTripStarted
  })
}

async function notifyUpcomingReservations ({ basis }) {
  const upcoming = await upcomingReservations({ basis })

  return notifyUsers({
    records: upcoming,
    notificationFn: upcomingReservation
  })
}

async function notifyOnTripUsers ({ basis }) {
  const onTrip = await upcomingReservationsWithOnTripVehicles({ basis })

  if (!onTrip.length) {
    return []
  }

  const bikeIds = onTrip.bikes.map((bike) => bike.bike_id)
  const activeBookings = await retrieveActiveBookings(bikeIds)
  const onTripUsers = await retrieveUsers(activeBookings)

  return notifyUsers({
    records: onTrip,
    notificationFn: onTripVehicleHasUpcomingReservation,
    users: onTripUsers
  })
}

async function cancelReservations (
  reservations,
  basis,
  cancellationReason = 'vehicle_unavailable'
) {
  const trx = await db.main.transaction()

  const reservationIds = reservations.map((r) => r.reservation_id)

  // All these reservations are starting at the same time.
  // Make end trip date the same as start trip date which is usually when the
  // reservation starts. Trips that start and end at the same exact time are
  // interpreted as having been cancelled.
  const reservationStart = moment.utc(reservations[0].reservation_start)

  await trx('reservations').whereIn('reservation_id', reservationIds).update({
    reservation_terminated: basis.format(),
    termination_reason: cancellationReason
  })

  await trx('trips').whereIn('reservation_id', reservationIds).update({
    date_created: reservationStart.unix(),
    date_endtrip: reservationStart.unix()
  })

  return trx.commit()
}

function refundPaymentTransactions (records) {
  return Promise.all(
    records.map((t) =>
      reflect(
        t,
        payments.refunds.refund(
          { payment_intent: t.transaction_id },
          { stripe_account: t.stripe_account_id }
        )
      )
    )
  )
}

async function chargeLateReturnFee (records) {
  /** TODO:
   * 1. The user is in a regular ride
   *  - should have a booking
   *  - create an unpaid trip payment transaction? How will ending the ride handle it?
   * 2. The user is in a reserved ride
   *  - will not have a booking
   *  - update the trip payment transaction created when making the reservation.
   *    - how to store the extra charge transaction id?
   */
  return []
}

async function processUpcomingReservationsWithUnavailableVehicles ({ basis }) {
  const upcomingUnavailable = await retrieveUpcomingReservationsWithUnavailableVehicles(
    { basis }
  )

  if (!upcomingUnavailable.length) {
    return { upcomingReservationsWithUnavailableVehicles: {} }
  }

  const reservations = upcomingUnavailable.map((record) => record.reservations)

  const transactions = await retrieveReservationPaymentTransactions(
    reservations
  )

  await cancelReservations(reservations, basis)
  const refunds = await refundPaymentTransactions(transactions)

  // All the bikes that are unavailable because they are still in ride.
  const onTrip = upcomingUnavailable.filter(
    (record) => record.bikes.current_status === 'on_trip'
  )

  await chargeLateReturnFee(onTrip)

  // Out of the in-ride bikes, the ones that are in a normal ride as opposed to
  // a reserved ride.
  // const onTripBookings = await retrieveActiveBookings(
  //   onTrip.map((record) => record.bikes.bike_id)
  // )
  // const onTripUsersWithBookings = await retrieveUsers(onTripBookings)

  // Out of the in-ride bikes, the ones that are in an over-running reservation ride
  // const onTripWithoutBookings = onTrip.filter(
  //   (record) => !onTripUsersWithBookings[record.reservations.user_id]
  // )
  // const onTripUsersWithReservations = await retrieveUsers(
  //   onTripWithoutBookings.map((record) => record.reservations.user_id)
  // )

  const [
    cancelledForNotifications
    // lateReturnsFromNormalRides,
    // lateReturnsFromReservations
  ] = await Promise.all([
    notifyUsers({
      records: upcomingUnavailable,
      notificationFn: upcomingReservationWithUnavailableVehicle
    }).catch(conform)
    // notifyUsers({
    //   records: onTripBookings,
    //   notificationFn: lateReturnFeeCharged,
    //   users: onTripUsersWithBookings
    // }).catch(conform),
    // notifyUsers({
    //   records: upcomingUnavailable.filter(
    //     (record) => onTripUsersWithReservations[record.reservations.user_id]
    //   ),
    //   notificationFn: ({ reservations: reservation }) =>
    //     lateReturnFeeCharged(reservation),
    //   users: onTripUsersWithReservations
    // }).catch(conform)
  ])

  return {
    refunds: _.groupBy(refunds, 'status'),
    upcomingReservationsWithUnavailableVehicles: _.groupBy(
      cancelledForNotifications,
      'status'
    )
    // lateReturnsFromReservations: _.groupBy(
    //   lateReturnsFromReservations,
    //   'status'
    // ),
    // lateReturnsFromNormalRides: _.groupBy(lateReturnsFromNormalRides, 'status')
  }
}

async function processUpcomingReservationsWithParkedVehicles ({ basis }) {
  const records = await upcomingReservationsWithParkedVehicles({
    basis,
    basisDiff: 30,
    basisDiffUnit: 'minutes'
  })

  const noRecords = { upcomingReservationsWithParkedVehicles: {} }

  if (!(records && records.length)) {
    return noRecords
  }

  const currentActiveBookings = await retrieveActiveBookings(
    records.map((r) => r.bikes.bike_id)
  )

  const bookings = new Set(currentActiveBookings.map((b) => b.bike_id))

  const parkedWithoutBooking = records
    .map((r) => r.bikes)
    .filter((bike) => !bookings.has(bike.bike_id))

  if (!parkedWithoutBooking.length) {
    return noRecords
  }

  const freeVehiclesIds = parkedWithoutBooking.map((b) => b.bike_id)

  const freeVehicles = new Set(freeVehiclesIds)

  const reservationsWithParked = records
    .map((r) => r.reservations)
    .filter((reservation) => freeVehicles.has(reservation.bike_id))

  const update = db.main('bikes').whereIn('bike_id', freeVehiclesIds).update({
    current_status: bikeConstants.current_status.reserved
  })

  return {
    upcomingReservationsWithParkedVehicles: _.groupBy(
      [
        await reflect(
          {
            reservations: reservationsWithParked.map((r) => r.reservation_id),
            bikes: freeVehiclesIds
          },
          update
        )
      ],
      'status'
    )
  }
}

/**
 * Process reservations that have elapsed without the user taking the trip.
 * Updates the `reserved` bike back to `parked` status.
 * @param {object} options
 * @param {import('moment').Moment} basis `reservation_end` as a moment UTC Date object
 */
async function processElapsedReservations ({ basis }) {
  const records = await retrieveElapsedReservations({ basis })

  if (!(records && records.length)) {
    return { elapsedReservations: {} }
  }

  const reservedVehicleIds = records.map((r) => r.bikes.bike_id)

  const update = db
    .main('bikes')
    .whereIn('bike_id', reservedVehicleIds)
    .update({
      current_status: bikeConstants.current_status.parked
    })

  return {
    elapsedReservations: _.groupBy(
      [
        await reflect(
          {
            bikes: reservedVehicleIds,
            reservations: records.map((r) => r.reservations.reservation_id)
          },
          update
        )
      ],
      'status'
    )
  }
}

async function notify ({ basis }) {
  const elapsed = await processElapsedReservations({ basis }).catch((e) => ({
    elapsedReservations: _.groupBy(conform(e), 'status')
  }))

  const upcomingWithParked = await processUpcomingReservationsWithParkedVehicles(
    { basis }
  ).catch((e) => ({
    upcomingReservationsWithParkedVehicles: _.groupBy(conform(e), 'status')
  }))

  const upcomingUnavailable = await processUpcomingReservationsWithUnavailableVehicles(
    { basis }
  ).catch((e) => ({
    upcomingReservationsWithUnavailableVehicles: _.groupBy(
      conform(e),
      'status'
    )
  }))

  const onTrip = await notifyOnTripUsers({ basis }).catch(conform)
  const current = await notifyCurrentReservations({ basis }).catch(conform)
  const upcoming = await notifyUpcomingReservations({ basis }).catch(conform)

  return {
    ...elapsed,
    ...upcomingWithParked,
    ...upcomingUnavailable,
    onTrip: _.groupBy(onTrip, 'status'),
    current: _.groupBy(current, 'status'),
    upcoming: _.groupBy(upcoming, 'status')
  }
}

async function performNotifications ({ basis }) {
  const notifications = await notify({ basis })

  Object.keys(notifications).forEach((key) => {
    const results = notifications[key]

    if (Object.keys(results).length === 0) {
      logger(`No ${key} reservations found.`, { basis })
    }

    if (results.fulfilled) {
      logger(
        `Successfully processed ${results.fulfilled.length} ${key} entities.`,
        {
          records: results.fulfilled.map((record) => record.entity)
        }
      )
    }

    if (results.rejected) {
      logger(
        `Failure while processing ${results.rejected.length} ${key} entities.`,
        results.rejected
      )
    }
  })
}

function scheduleNotifications ({ start = true } = {}) {
  // Every hour, at minutes 0 and 30.
  const schedule = '00,30 * * * *'

  const job = new CronJob(schedule, async function () {
    const basis = moment.utc().startOf('minute')

    logger('Starting reservation notifications...', { basis })

    try {
      await performNotifications({ basis })

      logger('Reservation notifications complete', {
        basis,
        nextDate: job.nextDate()
      })
    } catch (error) {
      // TODO: (waiyaki) Notify engineering of failing notifications
      logger('Error running reservation notifications', error)
    }
  })

  if (start) {
    const nextDate = job.nextDate()
    const basis = moment.utc().startOf('minute')

    logger(`Creating reservations notifications schedule:`, {
      start,
      nextDate
    })

    performNotifications({ basis })

    job.start()
  }

  return job
}

module.exports = {
  scheduleNotifications
}
