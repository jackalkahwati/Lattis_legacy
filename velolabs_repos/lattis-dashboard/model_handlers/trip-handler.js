'use strict'

const _ = require('underscore')
const moment = require('moment-timezone')
const platform = require('@velo-labs/platform')

const db = require('../db')
const bikeConstants = require('../constants/bike-constants')
const tripConstants = require('../constants/trip-constants')
const maintenanceConstants = require('../constants/maintenance-constants')
const tripUtil = require('../utils/trip-util')

const fleetHandler = require('./fleet-handler')
const paymentHandler = require('./payment-handler')

const bookingHandler = require('./booking-handler')
const bikeFleetHandler = require('./bike-fleet-handler')
const { executeCommand } = require('../utils/grow')
const { sendEndTripRequestToGPSService } = require('../utils/gpsService')
const mysql = require('mysql')
const { ActonAPI } = require('../utils/acton-api')

const { SegwayApiClient } = require('../utils/segway-api')
const oval = require('../utils/oval')
const envConfig = require('../envConfig')
const Sentry = require('../utils/configureSentry')

const segwayApiClientForUs = new SegwayApiClient({
  baseUrl: envConfig('SEGWAY_API_URL'),
  clientId: envConfig('SEGWAY_CLIENT_ID'),
  clientSecret: envConfig('SEGWAY_CLIENT_SECRET')
})

const segwayApiClientForEu = new SegwayApiClient({
  baseUrl: envConfig('SEGWAY_EU_API_URL'),
  clientId: envConfig('SEGWAY_EU_CLIENT_ID'),
  clientSecret: envConfig('SEGWAY_EU_CLIENT_SECRET')
})

const logger = platform.logger
const query = platform.queryCreator
const sqlPool = platform.sqlPool
const tripHelper = platform.tripHelper
const queryHelper = platform.queryHelper
const responseCodes = platform.responseCodes
const dbConstants = platform.dbConstants
const errors = platform.errors
const config = platform.config
const mapBoxHandler = platform.mapBoxHandler

/**
 * This methods fetches trips that are associated with an request identifier.
 *
 * @param {Object} requestParam - Request parameter for identifier.
 * @param {Function} done
 */

const getTrips = (requestParam, done) => {
  const comparisonValues = {}

  if (_.has(requestParam, 'trip_id')) {
    comparisonValues.trip_id = requestParam.trip_id
  }
  if (_.has(requestParam, 'user_id')) {
    comparisonValues.user_id = requestParam.user_id
  }
  if (_.has(requestParam, 'bike_id')) {
    comparisonValues.bike_id = requestParam.bike_id
  }
  if (_.has(requestParam, 'fleet_id')) {
    comparisonValues.fleet_id = requestParam.fleet_id
  }
  if (_.has(requestParam, 'customer_id')) {
    comparisonValues.customer_id = requestParam.customer_id
  }
  if (_.has(requestParam, 'operator_id')) {
    comparisonValues.operator_id = requestParam.operator_id
  }
  if (_.has(requestParam, 'port_id')) {
    comparisonValues.port_id = requestParam.port_id
  }

  getAllTrips(comparisonValues, (error, data) => {
    const {trips, pagination} = data
    if (error) {
      Sentry.captureException(error)
      logger(
        'Error: making get trip details query: getTrips',
        error,
        'with comparisonColumn',
        comparisonValues
      )
      done(errors.internalServer(false), null)
      return
    }

    if (trips && trips.length === 0) {
      logger('there are no trips for fleet_id', comparisonValues)
      done(null, {trips, pagination})
      return
    }
    const formattedTrips = tripUtil.formatTripsForWire(trips)
    if (formattedTrips.length === 0) {
      logger('there are no trips for fleet_id', comparisonValues)
      done(null, {trips: formattedTrips, pagination})
      return
    }
    getTripPaymentTransaction(
      { trip_id: _.uniq(_.pluck(formattedTrips, 'trip_id')) },
      (error, tripTransactions) => {
        if (error) {
          Sentry.captureException(error, {trip_id: _.uniq(_.pluck(formattedTrips, 'trip_id'))})
          done(error, null)
          return
        }
        const tripsLength = formattedTrips.length
        for (let i = 0; i < tripsLength; i++) {
          const tripTransaction = _.findWhere(tripTransactions, {
            trip_id: formattedTrips[i].trip_id
          })
          if (tripTransaction) {
            formattedTrips[i] = Object.assign(
              formattedTrips[i],
              tripTransaction
            )
          }
        }
        if (formattedTrips.length !== 0) {
          const userDetails = query.selectWithAnd(
            dbConstants.tables.users,
            null,
            { user_id: _.uniq(_.pluck(formattedTrips, 'user_id')) }
          )
          sqlPool.makeQuery(userDetails, (error, users) => {
            if (error) {
              Sentry.captureException(error, { user_id: _.uniq(_.pluck(formattedTrips, 'user_id')) })
              logger(
                'Error: getting trips. Failed to get user details for query:',
                userDetails,
                'with error',
                error
              )
              done(errors.internalServer(false), null)
              return
            }
            let trip
            for (
              let index = 0, tripLen = formattedTrips.length;
              index < tripLen;
              index++
            ) {
              trip = formattedTrips[index]
              if (!trip.steps) trip.steps = []
              const tripUser = _.findWhere(users, { user_id: trip.user_id })
              trip = tripUtil.formatTripForWire(trip)
              if (tripUser) {
                formattedTrips[index] = Object.assign(
                  trip,
                  _.omit(
                    tripUser,
                    'password',
                    'reg_id',
                    'rest_token',
                    'refresh_token',
                    'date_created'
                  )
                )
              }
            }
            done(null, {trips: formattedTrips, pagination})
          })
        } else {
          done(null, {trips: formattedTrips, pagination})
        }
      }
    )
  })
}

const _attachRefundsToPaymentTransactions = (
  tripPaymentTransactions,
  callback
) => {
  const tripIds = _.uniq(_.pluck(tripPaymentTransactions, 'trip_id'))

  if (!tripIds.length) {
    return callback(null, tripPaymentTransactions)
  }

  const refundsQuery = query.selectWithAnd(
    dbConstants.tables.user_refunded,
    null,
    { trip_id: tripIds }
  )

  sqlPool.makeQuery(refundsQuery, (error, refunds) => {
    if (error) {
      Sentry.captureException(error, { trip_id: tripIds, refundsQuery })
      logger('Error: making user_refunded query: ', refundsQuery)
      callback(errors.internalServer(false), null)
      return
    }

    if (refunds && refunds.length) {
      const refundsByTripId = _.groupBy(refunds, 'trip_id')

      tripPaymentTransactions.forEach((transaction) => {
        const txRefunds = refundsByTripId[transaction.trip_id]

        if (txRefunds) {
          transaction.total_refunded = txRefunds.reduce(
            (acc, next) => acc + next.amount_refunded,
            0
          )
          transaction.refunds = txRefunds
        }
      })
    }

    return callback(null, tripPaymentTransactions)
  })
}

const getTripPaymentTransaction = (requestParam, callback) => {
  let comparisonColumnAndValue
  if (_.has(requestParam, 'trip_id')) {
    comparisonColumnAndValue = {
      trip_id: requestParam.trip_id
    }
  } else if (_.has(requestParam, 'fleet_id')) {
    comparisonColumnAndValue = {
      fleet_id: requestParam.fleet_id
    }
  } else if (_.has(requestParam, 'operator_id')) {
    comparisonColumnAndValue = {
      operator_id: requestParam.operator_id
    }
  }

  const tripsQuery = query.selectWithAnd(
    dbConstants.tables.trip_payment_transactions,
    null,
    comparisonColumnAndValue
  )
  sqlPool.makeQuery(tripsQuery, (error, tripPaymentTransactions) => {
    if (error) {
      Sentry.captureException(error, { tripsQuery })
      logger(
        'Error: making trip_payment_transactions query with ',
        _.keys(comparisonColumnAndValue),
        ':',
        tripsQuery
      )
      callback(errors.internalServer(false), null)
      return
    }

    _attachRefundsToPaymentTransactions(tripPaymentTransactions, callback)
  })
}

const updateTrip = (columnsAndValues, comparisonColumns, done) => {
  const updateTripQuery = query.updateSingle(
    dbConstants.tables.trips,
    columnsAndValues,
    comparisonColumns
  )
  sqlPool.makeQuery(updateTripQuery, done)
}

const getDistinctColumnInTrips = (column, comparison, done) => {
  const tripsQuery = query.customQuery(
    dbConstants.dbNames.trips,
    `SELECT DISTINCT user_id FROM ${
      dbConstants.tables.trips
    } where ${queryHelper.whereEqualQuery(comparison, ' AND ')}`
  )
  sqlPool.makeQuery(tripsQuery, done)
}

/** This method gets the details of the booking from the booking table
 *
 * @param {Object} filterParam - Identifier to get active booking
 * @param {Function} callback
 */
const getActiveBooking = (filterParam, callback) => {
  const comparisonColumnAndValues = {
    status: tripConstants.booking.status.active
  }
  if (filterParam.user_id) {
    comparisonColumnAndValues.user_id = filterParam.user_id
  }
  if (filterParam.bike_id) {
    comparisonColumnAndValues.bike_id = filterParam.bike_id
  }
  if (filterParam.fleet_id) {
    comparisonColumnAndValues.fleet_id = filterParam.fleet_id
  }
  if (filterParam.customer_id) {
    comparisonColumnAndValues.customer_id = filterParam.customer_id
  }
  if (filterParam.operator_id) {
    comparisonColumnAndValues.operator_id = filterParam.operator_id
  }
  const bookingQuery = query.selectWithAnd(
    dbConstants.tables.booking,
    null,
    comparisonColumnAndValues
  )
  sqlPool.makeQuery(bookingQuery, (error, activeBooking) => {
    if (error) {
      Sentry.captureException(error, { bookingQuery })

      logger(
        'Error: Failed to get active booking with',
        filterParam,
        ':',
        error
      )
      callback(errors.internalServer(false), null)
      return
    }

    if (activeBooking.length > 0) {
      fleetHandler.getFleetMetaData(
        { fleet_id: _.pluck(activeBooking, 'fleet_id') },
        (error, fleets) => {
          if (error) {
            Sentry.captureException(error, { activeBooking })

            logger(
              'Error: Failed to get active booking with',
              filterParam,
              ' Failed to get fleetsWithMetadata:',
              error
            )
            callback(error, null)
            return
          }

          // Let's check if the booking is within fleet booking interval or default one from config
          let booking
          let bookingFleet
          const activeBookings = []
          const bookingLen = activeBooking.length
          for (let i = 0; i < bookingLen; i++) {
            booking = activeBooking[i]
            bookingFleet = _.findWhere(fleets, { fleet_id: booking.fleet_id })

            let bookingExpiration = config.bikeBookingExpiration
            if (bookingFleet && bookingFleet.bike_booking_interval) {
              bookingExpiration = bookingFleet.bike_booking_interval
            }
            booking.bike_booking_expiration = bookingExpiration

            // Add if the booking is not expired or it has a trip started already
            if (
              booking.booked_on + bookingExpiration >= moment().unix() ||
              !!booking.trip_id
            ) {
              activeBookings.push(booking)
            }
          }
          callback(
            errors.noError(),
            activeBookings.length > 0 ? activeBookings : null
          )
        }
      )
    } else {
      callback(errors.noError(), null)
    }
  })
}

/**
 * This method returns a sub query for
 *
 * @param {object} columnsAndValues
 * @param {string} deliminator
 * @returns {string}
 */
const whereEqualQuery = (columnsAndValues, deliminator, tableName) => {
  let query = ''
  let counter = 0
  const length = _.keys(columnsAndValues).length
  _.each(columnsAndValues, function (value, column) {
    if (_.isArray(value)) {
      query += '`' + tableName + '`.`' + column + '` IN ' + '(' + mysql.escape(value) + ')'
    } else {
      query += '`' + tableName + '`.`' + column + '`' + '=' + mysql.escape(value)
    }
    query += (counter === length - 1 ? '' : deliminator)
    counter++
  })

  return query
}

/**
 * Query to get all Trips list from trip table
 *
 * @param comparisonColumns - column to filter the trip
 * @param {Function} done - Callback with params {error, trips}
 */
const getAllTrips = async (comparisonColumns, done) => {
  try {
    let whereClause = comparisonColumns ? (' WHERE ' + whereEqualQuery(comparisonColumns, ' AND ', 'trips')) : ''
    if (comparisonColumns.port_id) {
      whereClause = whereClause + ' AND date_endtrip IS NULL'
    }
    const data = await db.main.raw(
      'SELECT trip_id, steps, start_address, ended_by_operator, end_address, `trips`.`port_id`, `trips`.`date_created`, rating, date_endtrip, parking_image, user_id, operator_id, customer_id, `trips`.`bike_id`, `trips`.`fleet_id`, `reservation_id`, vendor FROM trips LEFT JOIN controllers ON trips.bike_id = controllers.bike_id' +
        whereClause +
        ' GROUP BY trip_id ORDER BY trip_id DESC LIMIT 50'
    )

    const reservations = _.groupBy(
      await db
        .main('reservations')
        .whereIn(
          'reservation_id',
          data[0].map((t) => t.reservation_id).filter(Boolean)
        )
        .groupBy('reservation_id')
        .select(),
      'reservation_id'
    )

    return done(
      null,
      {
        trips: data[0].map((trip) => {
          if (trip.reservation_id) {
            trip.reservation = _.first(reservations[trip.reservation_id])
          }

          return trip
        }),
        pagination: {}
      }
    )
  } catch (error) {
    Sentry.captureException(error)
    return done(error)
  }
}

const _getTripAddress = (latitude, longitude, done) => {
  mapBoxHandler.getAddressInformationForCoordinate(
    {
      latitude: latitude,
      longitude: longitude
    },
    (error, address) => {
      if (error) {
        Sentry.captureException(error)
        logger('Error: making trip-address query to MapBox')
        return done(errors.internalServer(false), null)
      }
      const locationAddress = address
        ? !!address.address && !!address.street
          ? address.address + ' ' + address.street
          : ''
        : ''
      return done(errors.noError(), locationAddress)
    }
  )
}

async function determineLocation ({ controller, trip }) {
  if (controller) {
    let status
    switch (controller.vendor) {
      case 'Segway':
        status = await segwayApiClientForUs.getVehicleInformation(controller.key)
        break
      case 'Segway IoT EU':
        status = await segwayApiClientForEu.getVehicleInformation(controller.key)
        break
      case 'ACTON':
        status = await ActonAPI.getCurrentStatus(controller.key)
    }
    if (status && ['Segway', 'Segway IoT EU'].includes(controller.vendor)) {
      const latitude = status.latitude
      const longitude = status.longitude
      if (longitude && latitude) {
        return { latitude, longitude }
      }
    } else if (status && controller.vendor === 'ACTON') {
      const latitude = status.lat
      const longitude = status.lon
      if (longitude && latitude) {
        return { latitude, longitude }
      }
    }
  }

  const steps = trip.steps
    ? Array.isArray(trip.steps)
      ? trip.steps.filter((step) => step.length)
      : JSON.parse(trip.steps).filter((step) => step.length)
    : []

  const step = steps[steps.length - 1]

  return {
    latitude: step[0],
    longitude: step[1]
  }
}

async function endTripOval (tripDetails, done) {
  try {
    const trip = await db
      .main('trips')
      .where('trip_id', tripDetails.trip_id)
      .first()

    const controller = trip.bike_id ? await db
      .main('controllers')
      .where({ bike_id: trip.bike_id })
      .first() : null

    const location =
      trip.steps && controller
        ? await determineLocation({ controller, trip })
        : { latitude: null, longitude: null }

    const params = {
      ...location,
      trip_id: trip.trip_id
    }
    const user = await db.users('users').where('user_id', trip.user_id).first()

    logger('Ending trip as user', {
      user: _.pick(user, 'user_id', 'email'),
      ovalParams: params,
      tripDetails
    })

    const client = oval.client.configure({ token: user.rest_token })
    const response = await oval.api.trips.endTrip(params, { client })
    return done(null, response)
  } catch (error) {
    Sentry.captureException(error)
    logger('Failed to end oval trip as user', errors.errorWithMessage(error))

    return done(error)
  }
}

const endTripPort = async (tripDetails, done) => {
  try {
    const trip = await db
      .main('trips')
      .where('trip_id', tripDetails.trip_id)
      .first()

    const controller = trip.bike_id ? await db
      .main('controllers')
      .where({ bike_id: trip.bike_id })
      .first() : null

    const location =
      trip.steps && controller
        ? await determineLocation({ controller, trip })
        : { latitude: null, longitude: null }

    const params = {
      ...location,
      trip_id: trip.trip_id,
      skip_parking_fee: true
    }

    const user = await db.users('users').where('user_id', trip.user_id).first()

    logger('Ending trip as user', {
      user: _.pick(user, 'user_id', 'email'),
      ovalParams: params,
      tripDetails
    })

    const client = oval.client.configure({ token: user.rest_token })
    const response = await oval.api.trips.endTrip(params, { client })

    if (tripDetails.ended_by_operator) {
      await db.main('trips').where('trip_id', tripDetails.trip_id).update({
        ended_by_operator: true
      })
    }

    return done(null, response)
  } catch (error) {
    Sentry.captureException(error)
    logger('Failed to end oval trip as user', errors.errorWithMessage(error))

    return done(error)
  }
}

const endTrip = (tripDetails, done) => {
  if (tripDetails.charge) {
    return endTripOval(tripDetails, done)
  }

  fleetHandler.getFleetWithFleetId(tripDetails.fleet_id, (error, fleet) => {
    if (error) {
      Sentry.captureException(error)
      logger(
        'Error: get trip duration cost. Failed to get fleet',
        tripDetails.fleet_id
      )
      done(errors.internalServer(false), null)
      return
    }

    _getTrip(_.pick(tripDetails, 'trip_id'), async (error, trip) => {
      if (error) {
        Sentry.captureException(error)
        logger(
          'Error: making end-trip query with user id:',
          tripDetails.user_id
        )
        return done(errors.internalServer(false), null)
      }

      const controller = await db.main('controllers').where({ bike_id: trip[0].bike_id }).first()

      if (controller) {
        const locationFromController = await determineLocation({ controller, trip: trip[0] })
        tripDetails = { ...tripDetails, ...locationFromController }
      }

      const steps = trip[0].steps
        ? Array.isArray(trip[0].steps)
          ? trip[0].steps.filter((step) => step.length)
          : JSON.parse(trip[0].steps).filter((step) => step.length)
        : []
      const step = steps[steps.length - 1]
      const tripDetail = {
        latitude: step[0],
        longitude: step[1]
      }
      _getTripAddress(
        tripDetail.latitude,
        tripDetail.longitude,
        (error, endAddress) => {
          if (error) {
            Sentry.captureException(error, trip)
            return done(errors.internalServer(false), null)
          }
          if (trip[0].end_address) {
            logger('Error: the trip is already ended', tripDetails.trip_id)
            return done(errors.resourceNotFound(false), null)
          } else {
            trip[0].distance = tripHelper.getTripDistance(trip[0])
            tripDetails.end_address = endAddress.length
              ? endAddress
              : `Unnamed Road ${tripDetail.latitude}, ${tripDetail.longitude}`
            const updateObject = _.pick(
              tripDetails,
              'trip_id',
              'end_address',
              'parking_image'
            )
            updateObject.operator_id = tripDetails.acl.operator_id
            updateTrip(
              _.extend(updateObject, { date_endtrip: moment().unix() }),
              { trip_id: tripDetails.trip_id },
              async (error) => {
                Sentry.captureException(error)
                if (error) {
                  logger(
                    'Error: making end-trip query with user id:',
                    tripDetails.user_id
                  )
                  return done(errors.internalServer(false), null)
                }
                const controller = await db
                  .main('controllers')
                  .where({ bike_id: trip[0].bike_id })
                  .first()
                if (
                  controller &&
                  ['Segway', 'Segway IoT EU', 'Grow', 'Geotab IoT', 'ACTON'].includes(
                    controller.vendor
                  )
                ) {
                  // Turn off lights
                  if (controller.vendor === 'Grow') {
                    executeCommand(`s/c/${controller.key}`, '16,0', (error) => {
                      if (error) {
                        Sentry.captureException(error)
                        logger(
                          `Error turning off Grow lights ${controller.key}`
                        )
                      }
                    })
                  }
                  // turn of lights for segway
                  if (
                    ['Segway', 'Segway IoT EU'].includes(
                      controller.vendor
                    )
                  ) {
                    await bikeFleetHandler.setBikeLights(0, controller)
                  }
                  const tripDetailsForGPSTrackingService = {
                    ..._.pick(
                      tripDetails,
                      'trip_id',
                      'steps',
                      'end_address',
                      'date_endtrip'
                    ),
                    bike_id: trip[0].bike_id,
                    date_endtrip: moment().unix()
                  }
                  await sendEndTripRequestToGPSService(
                    tripDetailsForGPSTrackingService
                  )
                }
                bikeFleetHandler.getBikes(
                  { bike_id: trip[0].bike_id },
                  (error, bike) => {
                    if (error) {
                      Sentry.captureException(error)
                      return done(errors.internalServer(false), null)
                    }
                    bookingHandler.updateBooking(
                      {
                        status: tripConstants.booking.status.finished,
                        cancelled_on: moment().unix()
                      },
                      _.pick(tripDetails, 'trip_id'),
                      (error) => {
                        if (error) {
                          Sentry.captureException(error)
                          return done(errors.internalServer(false), null)
                        }
                        if (
                          tripDetails.source &&
                          tripDetails.source === 'Self End'
                        ) {
                          done(null, null)
                          return
                        }
                        if (tripDetails.to === 'live') {
                          const bikeColumns = {
                            distance: parseFloat(
                              (bike[0].distance + trip[0].distance).toFixed(2)
                            ),
                            distance_after_service: parseFloat(
                              (
                                bike[0].distance_after_service +
                                trip[0].distance
                              ).toFixed(2)
                            ),
                            current_status: bikeConstants.current_status.parked
                          }
                          bikeFleetHandler.updateBike(
                            _.extend(
                              _.pick(tripDetail, 'latitude', 'longitude'),
                              bikeColumns
                            ),
                            { bike_id: bike[0].bike_id },
                            (error) => {
                              if (error) {
                                Sentry.captureException(error)
                                done(error, null)
                                return
                              }
                              done(null, null)
                            }
                          )
                        } else {
                          const bikeColumns = {
                            distance: parseFloat(
                              (bike[0].distance + trip[0].distance).toFixed(2)
                            ),
                            distance_after_service: parseFloat(
                              (
                                bike[0].distance_after_service +
                                trip[0].distance
                              ).toFixed(2)
                            ),
                            current_status:
                              bikeConstants.current_status.underMaintenance
                          }
                          const maintenanceColumns = {
                            bike_id: bike[0].bike_id,
                            operator_id: tripDetails.acl.operator_id,
                            fleet_id: tripDetails.fleet_id,
                            category: maintenanceConstants.category.serviceDue,
                            notes: 'end ride was done from dashboard',
                            type: 'dashboard_end_ride',
                            customer_id: fleet.customer_id
                          }
                          bikeFleetHandler.sendToService(
                            maintenanceColumns,
                            null,
                            null,
                            (error) => {
                              Sentry.captureException(error)
                              if (error) {
                                done(error, null)
                                return
                              }

                              bikeFleetHandler.updateBike(
                                _.extend(
                                  _.pick(tripDetail, 'latitude', 'longitude'),
                                  bikeColumns
                                ),
                                { bike_id: bike[0].bike_id },
                                (error) => {
                                  if (error) {
                                    done(error, null)
                                    return
                                  }
                                  done(null, null)
                                }
                              )
                            }
                          )
                        }
                      }
                    )
                  }
                )
              }
            )
          }
        }
      )
    })
  })
}

/**
 * Query to get all Trips list from trip table
 *
 * @param comparisonColumn - column to filter the trip
 * @param {Function} done - Callback with params {error, trips}
 */
const _getTrip = (comparisonColumn, done) => {
  const getTripsQuery = query.selectWithAnd(
    dbConstants.tables.trips,
    null,
    comparisonColumn
  )
  sqlPool.makeQuery(getTripsQuery, done)
}

/**
 * To count trips for each day on weeks
 *
 * @param  {Object} tripParam - parameter to filter out trips
 * @param {Function} done - callback
 */
const tripAnalytics = (tripParam, done) => {
  fleetHandler.getFleetMetaData(
    { fleet_id: tripParam.fleet_id },
    (error, fleetMetaData) => {
      if (error) {
        Sentry.captureException(error, { fleet_id: tripParam.fleet_id })
        logger(
          'Error getting fleetMetaData with fleets',
          tripParam,
          errors.errorWithMessage(error)
        )
        done(error, null)
        return
      }
      getTrips(tripParam, (error, trips) => {
        if (error) {
          Sentry.captureException(error, { fleet_id: tripParam.fleet_id })
          done(errors.internalServer(false), null)
          return
        }
        const tripTimes = trips && trips.length ? trips
          .map((trip) => moment.unix(trip.date_created))
          .filter(
            (time) =>
              !tripParam.after || time.isAfter(moment.unix(tripParam.after))
          ) : []

        const tripTimestamps = tripTimes.map((time) => time.unix())
        const weekCount = Math.ceil(
          (moment().unix() - _.min(tripTimestamps)) /
            moment.duration(1, 'week').asSeconds()
        )

        const fleetTimezone =
          fleetMetaData && fleetMetaData.length
            ? fleetMetaData[0].fleet_timezone
            : null

        // setting default trips count as 0 for all the days in weeks
        const daysArray = Array.from({ length: 7 }).map(() => {
          return Array.from({ length: 24 }).fill(0)
        })

        for (const time of tripTimes) {
          const day = fleetTimezone
            ? time.tz(fleetTimezone).format('d')
            : time.format('d')
          const hour = fleetTimezone
            ? time.tz(fleetTimezone).format('H')
            : time.format('H')
          daysArray[day][hour]++
        }
        done(errors.noError(), { trips_count: daysArray, weekCount })
      })
    }
  )
}

const _retrieveUserRefund = ({ userRefundId }) => {
  return new Promise((resolve, reject) => {
    const getUserRefundQuery = query.selectWithAnd(
      dbConstants.tables.user_refunded,
      null,
      { refund_id: userRefundId }
    )

    sqlPool.makeQuery(getUserRefundQuery, (error, [refund]) => {
      if (error) {
        Sentry.captureException(error, { refund_id: userRefundId })
        logger('Error retrieving user refunded record', error)

        return reject(
          errors.customError(
            `Error retrieving user refund record ${userRefundId}`,
            responseCodes.InternalServer,
            null,
            true
          )
        )
      }

      return resolve(refund)
    })
  })
}

const _createUserRefund = ({ trip, paymentTransaction, stripeRefund }) => {
  return new Promise((resolve, reject) => {
    const userRefund = {
      deposit_amount: 0,
      user_id: trip.user_id,
      fleet_id: paymentTransaction.fleet_id,
      trip_id: paymentTransaction.trip_id,
      charge_id: paymentTransaction.transaction_id,
      date_charged: paymentTransaction.date_charged,
      date_refunded: moment().unix(),
      amount_refunded: stripeRefund.amount / 100,
      stripe_refund_id: stripeRefund.id
    }

    const queryUserRefund = query.insertSingle(
      dbConstants.tables.user_refunded,
      userRefund
    )

    sqlPool.makeQuery(queryUserRefund, (error, data) => {
      if (error) {
        Sentry.captureException(error, { user_id: trip.user_id })
        logger(
          'Error: making user funded query with ',
          _.keys(userRefund),
          ':',
          queryUserRefund
        )

        return reject(
          errors.customError(
            `Error saving trip refund for trip ${paymentTransaction.trip_id}`,
            responseCodes.InternalServer,
            null,
            true
          )
        )
      }

      return _retrieveUserRefund({ userRefundId: data.insertId })
        .then(resolve)
        .catch(reject)
    })
  })
}

const refundTrip = ({ trip_id: tripId, amount }) => {
  return new Promise((resolve, reject) => {
    _getTrip({ trip_id: tripId }, (tripError, [trip]) => {
      if (tripError) {
        return reject(tripError)
      }

      if (!trip) {
        return reject(
          errors.customError(
            `Trip ${tripId} not found.`,
            responseCodes.ResourceNotFound,
            'NotFound',
            true
          )
        )
      }

      getTripPaymentTransaction(
        { trip_id: tripId },
        (error, [paymentTransaction]) => {
          if (error) {
            Sentry.captureException(error, { trip_id: tripId })
            return reject(error)
          }

          if (!paymentTransaction) {
            return reject(
              errors.customError(
                `Payment transaction for trip ${tripId} not found.`,
                responseCodes.ResourceNotFound,
                'NotFound',
                true
              )
            )
          }

          if (amount && amount > paymentTransaction.total) {
            return reject(
              errors.customError(
                'Refund amount cannot exceed the amount charged',
                responseCodes.BadRequest,
                null,
                true
              )
            )
          }

          const fleetId = paymentTransaction.fleet_id

          fleetHandler.getFleetsWithPayment(
            { fleet_id: fleetId },
            (error, [fleet]) => {
              if (error) {
                Sentry.captureException(error, { fleet_id: fleetId })
                return reject(error)
              }

              if (!fleet) {
                return reject(
                  errors.customError(
                    `Fleet ${fleetId} not found.`,
                    responseCodes.ResourceNotFound,
                    'NotFound',
                    true
                  )
                )
              }

              paymentHandler
                .refundPaymentIntent({
                  amount,
                  paymentIntentId: paymentTransaction.transaction_id,
                  connectedAccountId: fleet.stripe_account_id
                })
                .then((stripeRefund) =>
                  _createUserRefund({ trip, paymentTransaction, stripeRefund })
                )
                .then(resolve)
                .catch((refundError) => {
                  logger(
                    'Error refunding trip payment',
                    { tripId, amount },
                    refundError
                  )

                  return reject(
                    errors.customError(
                      refundError.message ||
                        `Error refunding payment for trip ${tripId}`,
                      refundError.statusCode ||
                        refundError.code ||
                        responseCodes.InternalServer,
                      null,
                      true
                    )
                  )
                })
            }
          )
        }
      )
    })
  })
}

module.exports = Object.assign(module.exports, {
  getTrips: getTrips,
  getAllTrips: getAllTrips,
  updateTrip: updateTrip,
  getActiveBooking: getActiveBooking,
  getTripPaymentTransaction: getTripPaymentTransaction,
  endTrip: endTrip,
  refundTrip: refundTrip,
  tripAnalytics: tripAnalytics,
  getDistinctColumnInTrips: getDistinctColumnInTrips,
  endTripPort,
  endTripOval
})
