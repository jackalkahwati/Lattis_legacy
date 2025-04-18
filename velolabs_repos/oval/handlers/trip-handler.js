'use strict'

const util = require('util')
const mysql = require('mysql2')

const _ = require('underscore')
const moment = require('moment')
const platform = require('@velo-labs/platform')
const axios = require('axios')

const config = require('./../config')
const conversions = require('./../utils/conversions')
const bikeHandler = require('./bike-handler')
const bookingHandler = require('./booking-handler')
const tripConstants = require('./../constants/trip-constants')
const bikeConstants = require('./../constants/bike-constants')
const fleetConstants = require('./../constants/fleet-constants')
const db = require('../db')
const fleetHandler = require('./fleet-handler')
const userHandler = require('./user-handler')
const ticketHandler = require('./ticket-handler')
const endTripEmailObject = require('../helpers/end-trip-email')
const endTripEmailObjectFrench = require('../helpers/end-trip-email-french')
const paymentHandler = require('./payment-handler')
const subscriptionHelpers = require('./fleet-membership-handlers/helpers')
const reservationHelpers = require('./reservation-handlers/helpers')
const integrationsHandler = require('./integrations-handler')
const payments = require('./payments')
const { applyPromotionDiscount } = require('./promotions/promotions-handler')
const { generateToken } = require('../utils/auth')
const { executeCommand } = require('../helpers/grow')
const { castDataTypes } = require('../helpers/db')
const { sendStartTripRequestToGPSService, sendEndTripRequestToGPSService } = require('../helpers/gpsService')

const { SegwayApiClient } = require('../helpers/segway-api')
const { ActonAPI, OmniAPI, OmniIoTApiClient, TeltonikaApiClient, OkaiAPIClient } = require('../helpers/acton-api')
const { fetchTapKeyConfiguration, TapKeyApiClient } = require('../helpers/tapkey-api')
const ticketConstants = require('../constants/ticket-constants')
const pricing = require('./pricing')
const { retrievePort, getHubById } = require('./hubs-handler')
const momentTimezone = require('moment-timezone')
const { configureSentry } = require('../utils/configureSentry')
const Sentinel = require('../helpers/sentinel')
const { EdgeApiClient } = require('../helpers/edge-api')
const envConfig = require('../envConfig')

const Sentry = configureSentry()

const actonFamily = {
  ACTON: ActonAPI,
  'Omni IoT': OmniIoTApiClient,
  Teltonika: TeltonikaApiClient,
  'Omni Lock': OmniAPI,
  'Okai': OkaiAPIClient
}

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

const queryCreator = platform.queryCreator
const sqlPool = platform.sqlPool
const logger = platform.logger
const errors = platform.errors
const mapBoxHandler = platform.mapBoxHandler
const mailHandler = platform.mailHandler
const S3Handler = platform.S3Handler
const dbConstants = platform.dbConstants
const responseCodes = platform.responseCodes
const tripHelper = platform.tripHelper
const googleMapsHandler = platform.googleMapsHandler

const GPS_SERVICE_URL = envConfig('GPS_TRACKING_SERVICE')

/**
 * Used to update trips.
 *
 * @param {Object} columnsAndValues
 * @param {Object} comparisonColumnAndValue
 * @param {Function} callback
 * @private
 */
const _updateTrips = (columnsAndValues, comparisonColumnAndValue, callback) => {
  const tripsQuery = queryCreator.updateSingle(
    dbConstants.tables.trips,
    columnsAndValues,
    comparisonColumnAndValue
  )
  sqlPool.makeQuery(tripsQuery, callback)
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
      query +=
        '`' +
        tableName +
        '`.`' +
        column +
        '` IN ' +
        '(' +
        mysql.escape(value) +
        ')'
    } else {
      query +=
        '`' + tableName + '`.`' + column + '`' + '=' + mysql.escape(value)
    }
    query += counter === length - 1 ? '' : deliminator
    counter++
  })

  return query
}

/**
 * This methods fetches trips that are associated with an request identifier.
 * @param {Object} requestParam - Request parameter for identifier.
 * @param {boolean} filter criteria whether ongoing or ended trips
 * @param {Function} callback
 */
const getTrips = async (requestParam, active = false, callback) => {
  const comparisonColumnAndValue = {}
  if (requestParam.trip_id) {
    comparisonColumnAndValue.trip_id = requestParam.trip_id
  }
  if (requestParam.user_id) {
    comparisonColumnAndValue.user_id = requestParam.user_id
  }
  if (requestParam.bike_id) {
    comparisonColumnAndValue.bike_id = requestParam.bike_id
  }
  if (requestParam.port_id) {
    comparisonColumnAndValue.port_id = requestParam.port_id
  }
  if (requestParam.hub_id) {
    comparisonColumnAndValue.hub_id = requestParam.hub_id
  }
  if (requestParam.fleet_id) {
    comparisonColumnAndValue.fleet_id = requestParam.fleet_id
  }
  if (requestParam.customer_id) {
    comparisonColumnAndValue.customer_id = requestParam.customer_id
  }
  if (requestParam.operator_id) {
    comparisonColumnAndValue.operator_id = requestParam.operator_id
  }

  try {
    const whereEqual =
      Object.keys(comparisonColumnAndValue).length &&
      whereEqualQuery(comparisonColumnAndValue, ' AND ', 'trips')
    let whereClause = whereEqual ? ` WHERE ${whereEqual}` : ''
    if (active) { whereClause += ' AND date_endtrip IS NULL AND end_address IS NULL' }
    const queryString =
      'SELECT trip_id, steps, start_address, end_address, `trips`.`date_created`, rating, date_endtrip, parking_image, `trips`.`user_id`, operator_id, customer_id, `trips`.`bike_id`, first_lock_connect, `trips`.`fleet_id`, `trips`.`reservation_id`, vendor, pricing_option_id, `trips`.`port_id`, `trips`.`hub_id`, `trips`.`device_type`, reservation_start, reservation_end, reservation_terminated, termination_reason FROM trips LEFT JOIN controllers ON trips.bike_id = controllers.bike_id LEFT JOIN reservations ON trips.reservation_id = reservations.reservation_id' +
      whereClause +
      ' ORDER BY trip_id DESC'
    const data = await db.main.raw(queryString)
    callback(null, data[0])
  } catch (error) {
    Sentry.captureException(error)
    logger(
      'Error: making trips with ',
      comparisonColumnAndValue,
      'with query:'
    )
    callback(errors.internalServer(false), null)
  }
}

const getTripsByIds = async (tripsIds, token) => {
  try {
    const response = await axios.get(
      `${GPS_SERVICE_URL}/trips?trips=${tripsIds.join(',')}`,
      {
        headers: {
          'x-access-token': token
        }
      }
    )
    return response.data
  } catch (error) {
    Sentry.captureException(error, { tripsIds })
    logger('Error getting trips from GPS service')
    logger('Error:::', error.message || error.response.data.message)
    return null
  }
}

/**
 * This methods fetches trips with trip payment and user payment information.
 *
 * @param {Object} requestParam - Request parameter for identifier.
 * @param {Function} callback
 */
const getTripsWithPaymentInfo = (requestParam, callback) => {
  getTrips(requestParam, false, async (error, trips) => {
    if (error) {
      Sentry.captureException(error, { requestParam })
      callback(error, null)
      return
    }
    const tripsLen = trips.length
    if (tripsLen === 0) {
      callback(errors.noError(), trips)
      return
    }

    const token = generateToken()
    const tripIds = []
    if (trips && trips.length) {
      for (let trip of trips) {
        if (
          trip.vendor &&
        [
          'Segway',
          'Segway IoT EU',
          'Grow',
          'Geotab IoT',
          'ACTON',
          'Omni IoT',
          'Teltonika',
          'Sentinel',
          'Edge'
        ].includes(trip.vendor)
        ) {
          const oneMonthAgo = moment().subtract(1, 'months').unix()
          if (trip.date_created > oneMonthAgo) {
          // Only make requests for trips after this date to avoid unnecessary requests
            tripIds.push(trip.trip_id)
          }
        }
      }
    }
    let tripsObject = {}
    try {
      const tripsInfo = await getTripsByIds(tripIds, token)
      if (tripsInfo && tripsInfo.payload) {
        for (let i = 0; i < tripsInfo.payload.length; i++) {
          const trip = tripsInfo.payload[i]
          tripsObject[trip.trip_id] = {
            distance:
              trip.steps && Array.isArray(tripsInfo.steps)
                ? tripHelper.getTripDistance(trip.steps)
                : 0,
            steps: trip.steps,
            end_address: trip.end_address,
            date_endtrip: trip.date_endtrip,
            trip_id: trip.trip_id,
            bike_id: trip.bike_id,
            start_address: trip.start_address,
            date_created: trip.date_created,
            manualEndTrip: trip.manualEndTrip
          }
        }
      }

      trips = trips.map((trip) => {
        if (tripsObject[trip.trip_id]) {
          const tripInObject = tripsObject[trip.trip_id]
          if (!tripInObject) return trip
          const endAddress = tripInObject.end_address || trip.end_address
          return {
            ...trip,
            ...tripsObject[trip.trip_id],
            date_endtrip:
              tripsObject[trip.trip_id].date_endtrip || trip.date_endtrip,
            end_address: endAddress
          }
        }
        return trip
      })
    } catch (error) {
      Sentry.captureException(error, { tripIds })
      logger('Error getting trips info from GPS Service', error)
    }
    fleetHandler.getFleets(
      {
        fleet_id: _.uniq(_.pluck(trips, 'fleet_id')),
        includePricingOptions: false
      },
      (error, fleets) => {
        if (error) {
          Sentry.captureException(error, { tripIds })
          logger(
            'Error: failed to get fleets with id',
            _.uniq(_.pluck(trips, 'fleet_id')),
            error
          )
          callback(errors.internalServer(false), null)
          return
        }

        getTripsPaymentInfo(trips, fleets, (error, tripsPayments) => {
          if (error) {
            Sentry.captureException(error, { trips, fleets })
            callback(error, null)
            return
          }
          callback(
            errors.noError(),
            formatTripsWithPaymentInfo(trips, fleets, tripsPayments)
          )
        })
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

  const refundsQuery = queryCreator.selectWithAnd(
    dbConstants.tables.user_refunded,
    null,
    { trip_id: tripIds }
  )

  sqlPool.makeQuery(refundsQuery, (error, refunds) => {
    if (error) {
      Sentry.captureException(error, { refundsQuery })
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

/**
 * This methods fetches trips with trip payment information with user payments profile.
 *
 * @param {Object} trips
 * @param {Object} fleetDetails
 * @param {Function} callback
 */
const getTripsPaymentInfo = (trips, fleetDetails, callback) => {
  if (trips.length === 0) {
    callback(null, [])
    return
  }

  const tripIds = _.uniq(_.pluck(trips, 'trip_id'))

  db.main(dbConstants.tables.trips)
    .whereIn('trips.trip_id', tripIds)
    .innerJoin(
      'trip_payment_transactions',
      'trip_payment_transactions.trip_id',
      'trips.trip_id'
    )
    .leftOuterJoin(
      dbConstants.tables.user_payment_profiles,
      'trip_payment_transactions.card_id',
      'user_payment_profiles.card_id'
    )
    .leftOuterJoin(
      'promotion',
      'promotion.promotion_id',
      'trip_payment_transactions.promotion_id'
    )
    .leftOuterJoin(
      'pricing_options',
      'pricing_options.pricing_option_id',
      'trips.pricing_option_id'
    )
    .options({ nestTables: true, typeCast: castDataTypes })
    .groupBy('trip_payment_transactions.id')
    .then((results) => {
      return _attachRefundsToPaymentTransactions(
        results.map(
          ({
            trip_payment_transactions: trx,
            user_payment_profiles: profile,
            pricing_options: pricingOption,
            promotion
          }) => ({
            ...trx,
            ...(profile.id ? profile : undefined),
            promotion: promotion.promotion_id ? promotion : undefined,
            pricing_option: pricingOption.pricing_option_id
              ? pricingOption
              : undefined
          })
        ),
        callback
      )
    })
    .catch(callback)
}

/**
 * This methods fetches trips with trip payment information.
 *
 * @param {Object} requestParam - trips identifier
 * @param {Function} callback
 */
const getTripPaymentTransaction = (requestParam, callback) => {
  db.main(dbConstants.tables.trip_payment_transactions)
    .where('trip_payment_transactions.trip_id', requestParam.trip_id)
    .leftOuterJoin(
      'promotion',
      'promotion.promotion_id',
      'trip_payment_transactions.promotion_id'
    )
    .options({ nestTables: true, typeCast: castDataTypes })
    .then((results) => {
      return _attachRefundsToPaymentTransactions(
        results.map(({ trip_payment_transactions: trx, promotion }) => ({
          ...trx,
          promotion: promotion.promotion_id ? promotion : undefined
        })),
        callback
      )
    })
    .catch(callback)
}

const integrationTypes = {
  Segway: 'segway',
  'Segway IoT EU': 'segway',
  Grow: 'grow',
  'Geotab IoT': 'geotab',
  ACTON: 'acton',
  'Omni IoT': 'Omni IoT',
  Teltonika: 'Teltonika',
  Sentinel: 'Sentinel',
  Edge: 'Edge'
}

const isUserOnActiveTrip = async (userId) => {
  let activeTrip
  try {
    const latestTripByUser = await db
      .main('trips')
      .where({ user_id: userId })
      .orderBy('trip_id', 'desc')
      .first() // Get latest trip by user
    activeTrip =
      latestTripByUser &&
      !latestTripByUser.date_endtrip &&
      !latestTripByUser.end_address
        ? latestTripByUser
        : undefined // Trip without date end trip is still active
  } catch (error) {
    Sentry.captureException(error, { userId })
    logger('An error occurred checking latest trip by user')
  }
  return activeTrip
}

const endTripWithCancelledOrElapsedReservation = async (activeTrip) => {
  if (activeTrip.reservation_id) {
    await db.main.transaction(async (trx) => {
      const reservation = await trx('reservations')
        .where({ reservation_id: activeTrip.reservation_id })
        .first()

      const reservationEndUtc = momentTimezone
        .tz(
          reservation.reservation_end,
          'YYYY-MM-DD HH:mm:ss',
          reservation.reservation_timezone
        )
        .toISOString()
      const reservationStartUtc = momentTimezone
        .tz(
          reservation.reservation_start,
          'YYYY-MM-DD HH:mm:ss',
          reservation.reservation_timezone
        )
        .toISOString()

      const duration = moment
        .duration(moment.utc().diff(moment.utc(reservationEndUtc)))
        .asMinutes()
      const durationToStart = moment
        .duration(moment.utc(reservationStartUtc).diff(moment.utc()))
        .asMinutes()
      // Reservation time already elapsed. Cancel trip
      if (duration >= 60) {
        const rStart = momentTimezone
          .tz(
            reservation.reservation_start,
            'YYYY-MM-DD HH:mm:ss',
            reservation.reservation_timezone
          )
          .toISOString()
        const rEnd = momentTimezone
          .tz(
            reservation.reservation_end,
            'YYYY-MM-DD HH:mm:ss',
            reservation.reservation_timezone
          )
          .toISOString()
        const tripUpdates = {
          start_address: 'Reservation Time Elapsed',
          end_address: 'Reservation Time Elapsed',
          date_created: moment.utc(rStart).unix(),
          date_endtrip: moment.utc(rEnd).unix()
        }
        await trx('trips')
          .update(tripUpdates)
          .where({
            reservation_id: activeTrip.reservation_id,
            first_lock_connect: 0
          })
        // TODO: Update termination to include 'elapsed' as termination reason
        await trx('reservations')
          .update({
            reservation_terminated: momentTimezone()
              .tz(reservation.reservation_timezone)
              .format('YYYY-MM-DD HH:mm:ss'),
            termination_reason: 'cancellation'
          })
          .where({ reservation_id: activeTrip.reservation_id })
      } else if (durationToStart <= 30 && durationToStart > 0) {
        // You've an active reservation in less than 30 mins
        // throw new Error(`You have a scheduled reservation in ${durationToStart.toFixed(0)} minutes`)
        logger(
          `You have a scheduled reservation in ${durationToStart.toFixed(
            0
          )} minutes`
        )
      }
    })
  }
}

const startTripsInGPSService = async (tripDetails, geofenceEnabled, doNotTrackTrip) => {
  try {
    const controller = await db
      .main('controllers')
      .where({ bike_id: tripDetails.bikeId || tripDetails.bike_id })
      .select(
        'controller_id',
        'key',
        'device_type',
        'vendor',
        'make',
        'fleet_id'
      ).first()

    if (
      controller &&
        [
          'Segway',
          'Segway IoT EU',
          'Grow',
          'Geotab IoT',
          'ACTON',
          'Omni IoT',
          'Teltonika',
          'Sentinel',
          'Edge'
        ].includes(controller.vendor)
    ) {
      const integration = await db
        .main('integrations')
        .where({
          fleet_id: controller.fleet_id,
          integration_type:
              integrationTypes[controller.vendor]
        })
        .first()
      const trip = await db
        .main('trips')
        .where({ trip_id: tripDetails.trip_id })
        .select(
          'trip_id',
          'start_address',
          'date_created',
          'steps',
          'bike_id'
        )
        .first()
      if (controller.vendor === 'Sentinel') {
        try {
          await new Sentinel().startTrip(controller.key, trip.trip_id)
        } catch (error) {
          logger(`:::An error occurred starting sentinel API trip ${controller.key}: ${trip.trip_id}`)
        }
      }

      if (controller.vendor === 'Edge') {
        try {
          const tripData = await db.main('trips').where({ trip_id: tripDetails.trip_id }).first()
          const lastUserId = tripData ? tripData.user_id : ''
          const edgeTrip = await new EdgeApiClient(controller.fleet_id, lastUserId).startTrip(controller.key)
          await db.main('trips').update({ edge_trip_id: edgeTrip.trip.id }).where({ trip_id: trip.trip_id })
          const startLocation = { latitude: edgeTrip.start.lat, longitude: edgeTrip.start.lon }
          db.main('bikes').update(startLocation).where({ bike_id: tripDetails.bikeId })
          db.main('controllers').update(startLocation).where({ bike_id: tripDetails.bikeId })
        } catch (error) {
          Sentry.captureException(error)
          logger(`:::An error occurred starting Edge API trip ${controller.key}: ${trip.trip_id}`)
        }
      }
      let meta = { geofenceEnabled }
      if (integration) {
        let info = {}
        const integrationMeta =
            integration.metadata &&
            JSON.parse(integration.metadata)
        if (integrationMeta) info = integrationMeta
        if (integration.email) {
          info.userName = integration.email
        }
        meta = {
          ...meta,
          fleetId: integration.fleet_id,
          apiKey: integration.api_key,
          sessionId: integration.session_id,
          info
        }
      }
      meta = _.pick(meta, _.identity)
      const requestDetails = {
        doNotTrackTrip,
        ..._.pick(
          tripDetails,
          'bike_id',
          'start_address',
          'date_created',
          'device_token',
          'steps',
          'trip_id',
          'bike_name'
        ),
        ...controller,
        ...trip,
        meta: meta
      }
      if (controller.vendor === 'Grow') {
        await sendGrowStartupStartTripCommands(controller)
      }
      await sendStartTripRequestToGPSService(
        requestDetails
      )
    }
  } catch (error) {
    logger('An error occurred starting trip in GPS service')
    Sentry.captureException(error, { tripDetails })
  }
}

/**
 * To start trip with trip details
 *
 * @param {Object} tripDetails - Contains user and lock identifiers
 * @param {Function} callback
 */
const startTrip = (tripDetails, callback) => {
  const now = moment().unix()
  let fleetDetails
  let geofenceEnabled
  let doNotTrackTrip = false
  bikeHandler.getBikeWithLock(
    _.pick(tripDetails, 'bike_id'),
    async (error, bikeDetails) => {
      if (error) {
        Sentry.captureException(error, { userId: tripDetails.user_id })
        logger(
          'Error: Failed to start trip. Unable to get the bike details with bike_id: ',
          tripDetails.bike_id
        )
        return callback(errors.internalServer(false), null)
      }
      fleetHandler.getFleetMetaData(
        { fleet_id: bikeDetails[0].fleet_id },
        (error, result) => {
          if (error) {
            Sentry.captureException(error, { userId: tripDetails.user_id })
            logger(
              'Error: getting fleets with fleet_id:',
              bikeDetails[0].fleet_id
            )
            return callback(error, null)
          }
          fleetDetails = result
          doNotTrackTrip =
            fleetDetails.length &&
            fleetDetails[0].do_not_track_trip &&
            fleetDetails[0].do_not_track_trip === 1
          geofenceEnabled =
            fleetDetails.length &&
            fleetDetails[0].geofence_enabled &&
            fleetDetails[0].geofence_enabled === 1
          if (fleetDetails[0].require_phone_number) {
            if (!tripDetails.phone_number) {
              logger(
                'Info: The user does not have a phone number which is required for this fleet'
              )
              return callback(
                errors.customError(
                  'Phone number required for this fleet',
                  responseCodes.Conflict,
                  'Phone number required'
                ),
                null
              )
            }
          }

          tripDetails = Object.assign(
            tripDetails,
            _.pick(
              bikeDetails[0],
              'lock_id',
              'fleet_id',
              'operator_id',
              'customer_id'
            )
          )

          bookingHandler.getActiveBooking(
            _.pick(tripDetails, 'bike_id', 'user_id'),
            (error, bookingDetails) => {
              if (error) {
                Sentry.captureException(error, { userId: tripDetails.user_id })
                logger(
                  'Error: Failed to get booking details with',
                  _.pick(tripDetails, 'bike_id', 'user_id'),
                  'with error:',
                  error
                )
                return callback(error, null)
              }
              if (!bookingDetails) {
                logger(
                  'there is no active booking for the bike to this user:',
                  tripDetails.user_id
                )
                return callback(errors.resourceNotFound(false), null)
              }
              // Update trip record if it has trip started already
              if (bookingDetails[0].trip_id) {
                updateTrip(
                  null,
                  Object.assign(_.omit(tripDetails, 'steps'), {
                    trip_id: bookingDetails[0].trip_id
                  }),
                  async (err, data) => {
                    if (err) callback(err)
                    else {
                      try {
                        await startTripsInGPSService(tripDetails, bookingDetails, geofenceEnabled, doNotTrackTrip)
                      } catch (error) {
                        Sentry.captureException(error, {
                          userId: tripDetails.user_id
                        })
                        logger('Error, startTrip::', error)
                      }
                      callback(null, data)
                    }
                  }
                )
              } else {
                if (tripDetails.is_payment_fleet) {
                  tripDetails.latitude = bikeDetails[0].latitude
                  tripDetails.longitude = bikeDetails[0].longitude
                  tripDetails.date_created = tripDetails.trip_start_time
                } else {
                  tripDetails.time_lock_connected = now
                  tripDetails.date_created = now
                }
                tripDetails.steps = [
                  [
                    tripDetails.latitude,
                    tripDetails.longitude,
                    moment().unix()
                  ]
                ]

                _getTripAddress(
                  tripDetails.latitude,
                  tripDetails.longitude,
                  async (error, address) => {
                    if (error) {
                      Sentry.captureException(error, {
                        userId: tripDetails.user_id
                      })
                      logger(
                        `Error: Failed to get address for location: [${tripDetails.latitude},${tripDetails.longitude}]`
                      )
                    }
                    tripDetails.start_address = address || `Unnamed Road`
                    const tripInfo = Object.assign(
                      _.pick(
                        tripDetails,
                        'bike_id',
                        'user_id',
                        'lock_id',
                        'fleet_id',
                        'customer_id',
                        'start_address',
                        'date_created',
                        'first_lock_connect',
                        'time_lock_connected',
                        'device_token'
                      ),
                      { steps: JSON.stringify(tripDetails.steps) }
                    )

                    if (tripDetails.pricing_option_id) {
                      try {
                        const pricingOption = await pricing.handlers.retrieve({
                          id: tripDetails.pricing_option_id,
                          fleetId: bookingDetails[0].fleet_id
                        })

                        if (!pricingOption) {
                          throw errors.customError(
                            `Invalid pricing option: ${tripDetails.pricing_option_id}`,
                            responseCodes.BadRequest,
                            'BadRequest'
                          )
                        }

                        tripInfo.pricing_option_id =
                          pricingOption.pricing_option_id
                      } catch (error) {
                        Sentry.captureException(error, {
                          userId: tripDetails.user_id
                        })
                        return callback(error)
                      }
                    }

                    const activeTrip = await isUserOnActiveTrip(
                      tripInfo['user_id']
                    )
                    if (activeTrip) {
                      /*
              try {
                await endTripWithCancelledOrElapsedReservation(activeTrip)
              } catch (error) {
                throw errors.customError(error.message, responseCodes.Conflict, 'ErrorCancellingScheduledReservation', true)
              }
              */
                      if (!activeTrip.reservation_id) {
                        // A user can start a trip with an existing future reservation beyond 30 mins
                        logger(
                          `The user ${tripInfo['user_id']} is already on a trip with bike ${activeTrip['bike_id']}`
                        )
                        return callback(
                          errors.formatErrorForWire({
                            message: `The user ${tripInfo['user_id']} is already on a trip with bike ${activeTrip['bike_id']}`,
                            code: 500
                          }),
                          { trip: activeTrip }
                        )
                      }
                    }

                    const startTripQuery = queryCreator.insertSingle(
                      dbConstants.tables.trips,
                      tripInfo
                    )
                    sqlPool.makeQuery(
                      startTripQuery,
                      async (error, insertStatus) => {
                        if (error) {
                          Sentry.captureException(error, {
                            userId: tripDetails.user_id
                          })
                          logger(
                            'Error: Failed to start trip. Unable to start trip for user with user id:',
                            tripDetails.user_id,
                            ' query:',
                            startTripQuery
                          )
                          return callback(errors.internalServer(false), null)
                        }
                        tripDetails.trip_id = insertStatus.insertId
                        try {
                          const controller = await db
                            .main('controllers')
                            .where({ bike_id: tripDetails.bike_id })
                            .select(
                              'controller_id',
                              'key',
                              'device_type',
                              'vendor',
                              'make',
                              'fleet_id'
                            )
                            .first()
                          if (
                            controller &&
                            [
                              'Segway',
                              'Segway IoT EU',
                              'Grow',
                              'Geotab IoT',
                              'ACTON',
                              'Omni IoT',
                              'Teltonika',
                              'Sentinel',
                              'Edge'
                            ].includes(controller.vendor)
                          ) {
                            const integration = await db
                              .main('integrations')
                              .where({
                                fleet_id: controller.fleet_id,
                                integration_type:
                                  integrationTypes[controller.vendor]
                              })
                              .first()
                            const trip = await db
                              .main('trips')
                              .where({ trip_id: tripDetails.trip_id })
                              .select(
                                'trip_id',
                                'start_address',
                                'date_created',
                                'steps'
                              )
                              .first()
                            let meta = { geofenceEnabled }
                            if (integration) {
                              let info = {}
                              const integrationMeta =
                                integration.metadata &&
                                JSON.parse(integration.metadata)
                              if (integrationMeta) info = integrationMeta
                              if (integration.email) {
                                info.userName = integration.email
                              }
                              meta = {
                                ...meta,
                                fleetId: integration.fleet_id,
                                apiKey: integration.api_key,
                                sessionId: integration.session_id,
                                info
                              }
                            }
                            meta = _.pick(meta, _.identity)
                            const requestDetails = {
                              doNotTrackTrip,
                              ..._.pick(
                                tripDetails,
                                'bike_id',
                                'start_address',
                                'date_created',
                                'device_token',
                                'steps',
                                'trip_id',
                                'bike_name'
                              ),
                              ...controller,
                              ...trip,
                              meta: meta
                            }
                            await sendStartTripRequestToGPSService(
                              requestDetails
                            )
                            if (controller.vendor === 'Sentinel') {
                              try {
                                await new Sentinel().startTrip(controller.key, trip.trip_id)
                              } catch (error) {
                                logger(`:::An error occurred starting sentinel API trip ${controller.key}: ${trip.trip_id}`)
                              }
                            }
                          }
                        } catch (error) {
                          Sentry.captureException(error, {
                            userId: tripDetails.user_id
                          })
                          logger('<Error::>', error)
                        }
                        bikeHandler.updateBike(
                          null,
                          {
                            bike_id: tripDetails.bike_id,
                            current_status: bikeConstants.current_status.onTrip,
                            current_trip_id: tripDetails.trip_id,
                            current_user_id: tripDetails.user_id,
                            last_user_id: tripDetails.user_id
                          },
                          (error) => {
                            if (error) {
                              Sentry.captureException(error, {
                                userId: tripDetails.user_id
                              })
                              logger(
                                'Error: Failed to start trip. Unable to update bike details'
                              )
                              return callback(error, null)
                            }
                            bookingHandler.updateBooking(
                              null,
                              { trip_id: tripDetails.trip_id },
                              { booking_id: bookingDetails[0].booking_id },
                              (error) => {
                                if (error) {
                                  Sentry.captureException(error, {
                                    userId: tripDetails.user_id
                                  })
                                  logger(
                                    'Error: Failed to start trip. Unable to update booking'
                                  )
                                  return callback(error, null)
                                }
                                fleetHandler.getFleets(
                                  { fleet_id: bikeDetails[0].fleet_id },
                                  (error, fleets) => {
                                    logger('Get fleets', fleets)
                                    if (error) {
                                      Sentry.captureException(error, {
                                        userId: tripDetails.user_id
                                      })
                                      logger(
                                        `Error: Could not send email to ${tripDetails.email}. Error fetching fleet`
                                      )
                                    }
                                    if (
                                      fleets[0].start_trip_email &&
                                      fleetDetails[0].email_on_start_trip
                                    ) {
                                      const bucket =
                                        envConfig('OVAL_MODE') &&
                                        envConfig('OVAL_MODE') === 'production'
                                          ? 'lattis.production/fleet_start_trip_email'
                                          : 'lattis.development/fleet_start_trip_email'

                                      const options = {
                                        bucket,
                                        key: fleets[0].start_trip_email
                                      }
                                      const s3Handler = new S3Handler()
                                      logger('Info:  S3 options', options)
                                      s3Handler.download(
                                        options,
                                        (error, email) => {
                                          if (error) {
                                            Sentry.captureException(error, {
                                              userId: tripDetails.user_id
                                            })
                                            logger(
                                              'Error: failed to start trip email content with error:',
                                              error
                                            )
                                            return
                                          }

                                          const mailObject = {
                                            subject: 'New Trip Started',
                                            content: email.body
                                              .toString('utf-8')
                                              .replace(
                                                /(?:\r\n|\r|\n)/g,
                                                '<br>'
                                              )
                                          }

                                          logger(
                                            'Info: Sending email to',
                                            tripDetails.email
                                          )
                                          mailHandler.sendMail(
                                            tripDetails.email,
                                            mailObject.subject,
                                            mailObject.content,
                                            null,
                                            null,
                                            (error) => {
                                              if (error) {
                                                Sentry.captureException(error, {
                                                  userId: tripDetails.user_id
                                                })
                                                logger(
                                                  'Error: Failed to send mail for createTicket to the recipient with email',
                                                  tripDetails.email,
                                                  error
                                                )
                                              }
                                            }
                                          )
                                        }
                                      )
                                    }
                                  }
                                )

                                callback(null, {
                                  trip_id: tripDetails.trip_id,
                                  do_not_track_trip: doNotTrackTrip
                                })
                              }
                            )
                          }
                        )
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

/**
 * To start trip with trip details
 *
 * @param {Object} tripDetails - Contains user and lock identifiers
 * @param {Function} callback
 */
const startTripV2 = async (tripDetails, callback) => {
  const tripForMap = {
    port_id: { name: 'port', key: 'port_id' },
    bike_id: { name: 'bike', key: 'bike_id' },
    hub_id: { name: 'hub', key: 'hub_id' }
  }
  let tripFor
  if (tripDetails.port_id) tripFor = tripForMap['port_id']
  else if (tripDetails.bike_id) tripFor = tripForMap['bike_id']
  else if (tripDetails.hub_id) tripFor = tripForMap['hub_id']
  const now = moment().unix()
  let fleetDetails
  let geofenceEnabled
  let doNotTrackTrip = false
  switch (tripFor.key) {
    case 'bike_id':
      bikeHandler.getBikeWithLock(
        _.pick(tripDetails, 'bike_id'),
        async (error, bikeDetails) => {
          if (error) {
            Sentry.captureException(error, { userId: tripDetails.user_id })
            logger(
              'Error: Failed to start trip. Unable to get the bike details with bike_id: ',
              tripDetails.bike_id
            )
            return callback(errors.internalServer(false), null)
          }
          fleetHandler.getFleetMetaData(
            { fleet_id: bikeDetails[0].fleet_id },
            (error, result) => {
              if (error) {
                Sentry.captureException(error, { userId: tripDetails.user_id })
                logger(
                  'Error: getting fleets with fleet_id:',
                  bikeDetails[0].fleet_id
                )
                return callback(error, null)
              }
              fleetDetails = result
              doNotTrackTrip =
                fleetDetails.length &&
                fleetDetails[0].do_not_track_trip &&
                fleetDetails[0].do_not_track_trip === 1
              geofenceEnabled =
                fleetDetails.length &&
                fleetDetails[0].geofence_enabled &&
                fleetDetails[0].geofence_enabled === 1
              if (fleetDetails[0].require_phone_number) {
                if (!tripDetails.phone_number) {
                  logger(
                    'Info: The user does not have a phone number which is required for this fleet'
                  )
                  return callback(
                    errors.customError(
                      'Phone number required for this fleet',
                      responseCodes.Conflict,
                      'Phone number required'
                    ),
                    null
                  )
                }
              }

              tripDetails = Object.assign(
                tripDetails,
                _.pick(
                  bikeDetails[0],
                  'lock_id',
                  'fleet_id',
                  'operator_id',
                  'customer_id'
                )
              )

              bookingHandler.getActiveBooking(
                _.pick(tripDetails, 'bike_id', 'user_id'),
                (error, bookingDetails) => {
                  if (error) {
                    Sentry.captureException(error, {
                      userId: tripDetails.user_id,
                      tripDetails
                    })
                    logger(
                      'Error: Failed to get booking details with',
                      _.pick(tripDetails, 'bike_id', 'user_id'),
                      'with error:',
                      error
                    )
                    return callback(error, null)
                  }
                  if (!bookingDetails) {
                    logger(
                      'there is no active booking for the bike to this user:',
                      tripDetails.user_id
                    )
                    return callback(errors.resourceNotFound(false), null)
                  }
                  tripDetails.device_type = 'bike'
                  // Update trip record if it has trip started already
                  if (bookingDetails[0].trip_id) {
                    updateTrip(
                      null,
                      Object.assign(_.omit(tripDetails, 'steps'), {
                        trip_id: bookingDetails[0].trip_id
                      }),
                      async (err, data) => {
                        if (err) callback(err)
                        else {
                          try {
                            const controller = await db
                              .main('controllers')
                              .where({ bike_id: tripDetails.bike_id })
                              .select(
                                'controller_id',
                                'key',
                                'device_type',
                                'vendor',
                                'make',
                                'fleet_id'
                              )
                              .first()
                            if (
                              controller &&
                              [
                                'Segway',
                                'Segway IoT EU',
                                'Grow',
                                'Geotab IoT',
                                'ACTON',
                                'Omni IoT',
                                'Teltonika',
                                'Sentinel',
                                'Edge'
                              ].includes(controller.vendor)
                            ) {
                              const integration = await db
                                .main('integrations')
                                .where({
                                  fleet_id: controller.fleet_id,
                                  integration_type:
                                    integrationTypes[controller.vendor]
                                })
                                .first()
                              const trip = await db
                                .main('trips')
                                .where({ trip_id: bookingDetails[0].trip_id })
                                .select(
                                  'trip_id',
                                  'start_address',
                                  'date_created',
                                  'steps'
                                )
                                .first()
                              let meta = { geofenceEnabled }
                              if (integration) {
                                let info = {}
                                const integrationMeta =
                                  integration.metadata &&
                                  JSON.parse(integration.metadata)
                                if (integrationMeta) info = integrationMeta
                                if (integration.email) {
                                  info.userName = integration.email
                                }
                                meta = {
                                  ...meta,
                                  fleetId: integration.fleet_id,
                                  apiKey: integration.api_key,
                                  sessionId: integration.session_id,
                                  info
                                }
                              }
                              meta = _.pick(meta, _.identity)
                              const requestDetails = {
                                doNotTrackTrip,
                                ..._.pick(
                                  tripDetails,
                                  'bike_id',
                                  'start_address',
                                  'date_created',
                                  'device_token',
                                  'steps',
                                  'trip_id',
                                  'bike_name'
                                ),
                                ...controller,
                                ...trip,
                                meta: meta
                              }
                              if (controller.vendor === 'Grow') {
                                await sendGrowStartupStartTripCommands(
                                  controller
                                )
                              }

                              if (controller.vendor === 'Sentinel') {
                                try {
                                  await new Sentinel().startTrip(controller.key, trip.trip_id)
                                } catch (error) {
                                  logger(`:::An error occurred starting sentinel API trip ${controller.key}: ${trip.trip_id}`)
                                }
                              }

                              if (controller.vendor === 'Edge') {
                                try {
                                  const tripData = await db.main('trips').where({ trip_id: trip.trip_id }).first()
                                  const lastUserId = tripData ? tripData.user_id : ''
                                  const edgeTrip = await new EdgeApiClient(controller.fleet_id, lastUserId).startTrip(controller.key)
                                  await db.main('trips').update({ edge_trip_id: edgeTrip.trip.id }).where({ trip_id: trip.trip_id })
                                  const startLocation = { latitude: edgeTrip.start.lat, longitude: edgeTrip.start.lon }
                                  db.main('bikes').update(startLocation).where({ bike_id: tripDetails.bikeId })
                                  db.main('controllers').update(startLocation).where({ bike_id: tripDetails.bikeId })
                                } catch (error) {
                                  Sentry.captureException(error)
                                  logger(`:::An error occurred starting Edge API trip ${controller.key}: ${trip.trip_id}`)
                                }
                              }
                              await sendStartTripRequestToGPSService(
                                requestDetails
                              )
                            }
                          } catch (error) {
                            Sentry.captureException(error, {
                              userId: tripDetails.user_id
                            })
                            logger('Error, startTrip::', error)
                          }
                          callback(null, data)
                        }
                      }
                    )
                  } else {
                    if (tripDetails.is_payment_fleet) {
                      tripDetails.latitude = bikeDetails[0].latitude
                      tripDetails.longitude = bikeDetails[0].longitude
                      tripDetails.date_created = tripDetails.trip_start_time
                    } else {
                      tripDetails.time_lock_connected = now
                      tripDetails.date_created = now
                    }
                    tripDetails.steps = [
                      [
                        tripDetails.latitude,
                        tripDetails.longitude,
                        moment().unix()
                      ]
                    ]

                    _getTripAddress(
                      tripDetails.latitude,
                      tripDetails.longitude,
                      async (error, address) => {
                        if (error) {
                          Sentry.captureException(error, { tripDetails })
                          logger(
                            `Error: Failed to get address for location: [${tripDetails.latitude},${tripDetails.longitude}]`
                          )
                        }
                        tripDetails.start_address = address || `Unnamed Road`
                        const tripInfo = Object.assign(
                          _.pick(
                            tripDetails,
                            'bike_id',
                            'user_id',
                            'lock_id',
                            'fleet_id',
                            'customer_id',
                            'start_address',
                            'date_created',
                            'first_lock_connect',
                            'time_lock_connected',
                            'device_token',
                            'device_type'
                          ),
                          { steps: JSON.stringify(tripDetails.steps) }
                        )

                        if (tripDetails.pricing_option_id) {
                          try {
                            const pricingOption =
                              await pricing.handlers.retrieve({
                                id: tripDetails.pricing_option_id,
                                fleetId: bookingDetails[0].fleet_id
                              })

                            if (!pricingOption) {
                              throw errors.customError(
                                `Invalid pricing option: ${tripDetails.pricing_option_id}`,
                                responseCodes.BadRequest,
                                'BadRequest'
                              )
                            }

                            tripInfo.pricing_option_id =
                              pricingOption.pricing_option_id
                          } catch (error) {
                            Sentry.captureException(error, { tripDetails })
                            return callback(error)
                          }
                        }

                        const activeTrip = await isUserOnActiveTrip(
                          tripInfo['user_id']
                        )
                        if (activeTrip) {
                          /*
                          try {
                            await endTripWithCancelledOrElapsedReservation(activeTrip)
                          } catch (error) {
                            throw errors.customError(error.message, responseCodes.Conflict, 'ErrorCancellingScheduledReservation', true)
                          }
                          */
                          if (!activeTrip.reservation_id) {
                            logger(
                              `The user ${tripInfo['user_id']} is already on a trip with bike ${activeTrip['bike_id']}`
                            )
                            return callback(
                              errors.formatErrorForWire({
                                message: `The user ${tripInfo['user_id']} is already on a trip with bike ${activeTrip['bike_id']}`,
                                code: 500
                              }),
                              { trip: activeTrip }
                            )
                          }
                        }

                        const startTripQuery = queryCreator.insertSingle(
                          dbConstants.tables.trips,
                          tripInfo
                        )
                        sqlPool.makeQuery(
                          startTripQuery,
                          async (error, insertStatus) => {
                            if (error) {
                              Sentry.captureException(error, { tripDetails })
                              logger(
                                'Error: Failed to start trip. Unable to start trip for user with user id:',
                                tripDetails.user_id,
                                ' query:',
                                startTripQuery
                              )
                              return callback(
                                errors.internalServer(false),
                                null
                              )
                            }
                            tripDetails.trip_id = insertStatus.insertId
                            try {
                              const controller = await db
                                .main('controllers')
                                .where({ bike_id: tripDetails.bike_id })
                                .select(
                                  'controller_id',
                                  'key',
                                  'device_type',
                                  'vendor',
                                  'make',
                                  'fleet_id'
                                )
                                .first()
                              if (
                                controller &&
                                [
                                  'Segway',
                                  'Segway IoT EU',
                                  'Grow',
                                  'Geotab IoT',
                                  'ACTON',
                                  'Omni IoT',
                                  'Teltonika',
                                  'Sentinel',
                                  'Edge'
                                ].includes(controller.vendor)
                              ) {
                                const integration = await db
                                  .main('integrations')
                                  .where({
                                    fleet_id: controller.fleet_id,
                                    integration_type:
                                      integrationTypes[controller.vendor]
                                  })
                                  .first()
                                const trip = await db
                                  .main('trips')
                                  .where({ trip_id: tripDetails.trip_id })
                                  .select(
                                    'trip_id',
                                    'start_address',
                                    'date_created',
                                    'steps'
                                  )
                                  .first()
                                let meta = { geofenceEnabled }
                                if (integration) {
                                  let info = {}
                                  const integrationMeta =
                                    integration.metadata &&
                                    JSON.parse(integration.metadata)
                                  if (integrationMeta) info = integrationMeta
                                  if (integration.email) {
                                    info.userName = integration.email
                                  }
                                  meta = {
                                    ...meta,
                                    fleetId: integration.fleet_id,
                                    apiKey: integration.api_key,
                                    sessionId: integration.session_id,
                                    info
                                  }
                                }
                                meta = _.pick(meta, _.identity)
                                const requestDetails = {
                                  doNotTrackTrip,
                                  ..._.pick(
                                    tripDetails,
                                    'bike_id',
                                    'start_address',
                                    'date_created',
                                    'device_token',
                                    'steps',
                                    'trip_id',
                                    'bike_name'
                                  ),
                                  ...controller,
                                  ...trip,
                                  meta: meta
                                }
                                await sendStartTripRequestToGPSService(
                                  requestDetails
                                )
                                if (controller.vendor === 'Sentinel') {
                                  try {
                                    await new Sentinel().startTrip(controller.key, trip.trip_id)
                                  } catch (error) {
                                    logger(`:::An error occurred starting sentinel API trip ${controller.key}: ${trip.trip_id}`)
                                  }
                                }
                              }
                            } catch (error) {
                              Sentry.captureException(error, {
                                tripDetails
                              })
                              logger('<Error::>', error)
                            }
                            bikeHandler.updateBike(
                              null,
                              {
                                bike_id: tripDetails.bike_id,
                                current_status:
                                  bikeConstants.current_status.onTrip,
                                current_trip_id: tripDetails.trip_id,
                                current_user_id: tripDetails.user_id,
                                last_user_id: tripDetails.user_id
                              },
                              (error) => {
                                if (error) {
                                  Sentry.captureException(error, {
                                    tripDetails
                                  })
                                  logger(
                                    'Error: Failed to start trip. Unable to update bike details'
                                  )
                                  return callback(error, null)
                                }
                                bookingHandler.updateBooking(
                                  null,
                                  { trip_id: tripDetails.trip_id },
                                  { booking_id: bookingDetails[0].booking_id },
                                  (error) => {
                                    if (error) {
                                      Sentry.captureException(error, {
                                        tripDetails,
                                        booking_id:
                                          bookingDetails[0].booking_id
                                      })
                                      logger(
                                        'Error: Failed to start trip. Unable to update booking'
                                      )
                                      return callback(error, null)
                                    }
                                    fleetHandler.getFleets(
                                      { fleet_id: bikeDetails[0].fleet_id },
                                      (error, fleets) => {
                                        logger('Get fleets', fleets)
                                        if (error) {
                                          Sentry.captureException(error, {
                                            tripDetails,
                                            booking_id:
                                              bookingDetails[0].booking_id
                                          })
                                          logger(
                                            `Error: Could not send email to ${tripDetails.email}. Error fetching fleet`
                                          )
                                        }
                                        if (
                                          fleets[0].start_trip_email &&
                                          fleetDetails[0].email_on_start_trip
                                        ) {
                                          const bucket =
                                            envConfig('OVAL_MODE') &&
                                            envConfig('OVAL_MODE') ===
                                              'production'
                                              ? 'lattis.production/fleet_start_trip_email'
                                              : 'lattis.development/fleet_start_trip_email'

                                          const options = {
                                            bucket,
                                            key: fleets[0].start_trip_email
                                          }
                                          const s3Handler = new S3Handler()
                                          logger('Info:  S3 options', options)
                                          s3Handler.download(
                                            options,
                                            (error, email) => {
                                              if (error) {
                                                Sentry.captureException(error, {
                                                  tripDetails,
                                                  booking_id:
                                                    bookingDetails[0]
                                                      .booking_id
                                                })
                                                logger(
                                                  'Error: failed to start trip email content with error:',
                                                  error
                                                )
                                                return
                                              }

                                              const mailObject = {
                                                subject: 'New Trip Started',
                                                content: email.body
                                                  .toString('utf-8')
                                                  .replace(
                                                    /(?:\r\n|\r|\n)/g,
                                                    '<br>'
                                                  )
                                              }

                                              logger(
                                                'Info: Sending email to',
                                                tripDetails.email
                                              )
                                              mailHandler.sendMail(
                                                tripDetails.email,
                                                mailObject.subject,
                                                mailObject.content,
                                                null,
                                                null,
                                                (error) => {
                                                  if (error) {
                                                    Sentry.captureException(
                                                      error,
                                                      {
                                                        tripDetails,
                                                        booking_id:
                                                          bookingDetails[0]
                                                            .booking_id
                                                      }
                                                    )
                                                    logger(
                                                      'Error: Failed to send mail for createTicket to the recipient with email',
                                                      tripDetails.email,
                                                      error
                                                    )
                                                  }
                                                }
                                              )
                                            }
                                          )
                                        }
                                      }
                                    )

                                    callback(null, {
                                      trip_id: tripDetails.trip_id,
                                      do_not_track_trip: doNotTrackTrip
                                    })
                                  }
                                )
                              }
                            )
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
      break
    case 'port_id':
      try {
        const portInfo = await retrievePort({ portId: tripDetails.port_id })
        const fleet = portInfo.fleet
        const fleetId = fleet.fleet_id
        portInfo.fleet_id = fleetId

        const hubInfo = portInfo.hub
        if (hubInfo.type !== 'parking_station') {
          return callback(
            errors.customError(
              'A trip can only be started on a port on a  hub of type parking station',
              responseCodes.Conflict,
              'InvalidHubType'
            ),
            null
          )
        }
        fleetHandler.getFleetMetaData(
          { fleet_id: portInfo.fleet_id },
          (error, result) => {
            if (error) {
              Sentry.captureException(error, { tripDetails })
              logger('Error: getting fleets with fleet_id:', portInfo.fleet_id)
              return callback(error, null)
            }
            fleetDetails = result
            if (fleetDetails[0].require_phone_number) {
              if (!tripDetails.phone_number) {
                logger(
                  'Info: The user does not have a phone number which is required for this fleet'
                )
                return callback(
                  errors.customError(
                    'Phone number required for this fleet',
                    responseCodes.Conflict,
                    'Phone number required'
                  ),
                  null
                )
              }
            }

            tripDetails = Object.assign(
              tripDetails,
              _.pick(fleet, 'fleet_id', 'operator_id', 'customer_id')
            )
            bookingHandler.getActiveBooking(
              _.pick(tripDetails, 'port_id', 'user_id'),
              (error, bookingDetails) => {
                if (error) {
                  Sentry.captureException(error, { tripDetails })
                  logger(
                    'Error: Failed to get booking details with',
                    _.pick(tripDetails, 'port_id', 'user_id'),
                    'with error:',
                    error
                  )
                  return callback(error, null)
                }
                if (!bookingDetails) {
                  logger(
                    'there is no active booking for the port to this user:',
                    tripDetails.user_id
                  )
                  return callback(
                    errors.customError(
                      `There's no booking for the user on the ${tripFor.name} ${
                        tripDetails[tripFor.key]
                      }`,
                      responseCodes.ResourceNotFound,
                      'MissingBoookingError',
                      true
                    ),
                    null
                  )
                }
                tripDetails.device_type = 'port'
                if (bookingDetails[0].trip_id) {
                  tripDetails.date_created = now
                  updateTrip(
                    null,
                    Object.assign(_.omit(tripDetails, 'steps'), {
                      trip_id: bookingDetails[0].trip_id
                    }),
                    async (err, data) => {
                      if (err) callback(err)
                      else {
                        callback(null, data)
                      }
                    }
                  )
                } else {
                  if (tripDetails.is_payment_fleet) {
                    tripDetails.date_created = tripDetails.trip_start_time
                  } else {
                    tripDetails.time_lock_connected = now
                    tripDetails.date_created = now
                  }
                  tripDetails.steps = null

                  _getTripAddress(
                    hubInfo.latitude,
                    hubInfo.longitude,
                    async (error, address) => {
                      if (error) {
                        Sentry.captureException(error, {
                          tripDetails,
                          hubInfo
                        })
                        logger(
                          `Error: Failed to get address for location: [${hubInfo.latitude},${hubInfo.longitude}]`
                        )
                      }
                      tripDetails.start_address = address || `Unnamed Road`
                      const tripInfo = _.pick(
                        tripDetails,
                        'port_id',
                        'user_id',
                        'fleet_id',
                        'customer_id',
                        'start_address',
                        'date_created',
                        'first_lock_connect',
                        'time_lock_connected',
                        'device_token',
                        'device_type'
                      )

                      if (tripDetails.pricing_option_id) {
                        try {
                          const pricingOption = await pricing.handlers.retrieve(
                            {
                              id: tripDetails.pricing_option_id,
                              fleetId: bookingDetails[0].fleet_id
                            }
                          )

                          if (!pricingOption) {
                            throw errors.customError(
                              `Invalid pricing option: ${tripDetails.pricing_option_id}`,
                              responseCodes.BadRequest,
                              'BadRequest'
                            )
                          }

                          tripInfo.pricing_option_id =
                            pricingOption.pricing_option_id
                        } catch (error) {
                          Sentry.captureException(error, { tripDetails })
                          return callback(error)
                        }
                      }

                      const activeTrip = await isUserOnActiveTrip(
                        tripInfo['user_id']
                      )
                      if (activeTrip) {
                        /*
                  try {
                    await endTripWithCancelledOrElapsedReservation(activeTrip)
                  } catch (error) {
                    throw errors.customError(error.message, responseCodes.Conflict, 'ErrorCancellingScheduledReservation', true)
                  }
                  */
                        if (!activeTrip.reservation_id) {
                          logger(
                            `The user ${tripInfo['user_id']} is already on a trip with port ${activeTrip['port_id']}`
                          )
                          return callback(
                            errors.formatErrorForWire({
                              message: `The user ${tripInfo['user_id']} is already on a trip with port ${activeTrip['port_id']}`,
                              code: 500
                            }),
                            { trip: activeTrip }
                          )
                        }
                      }

                      const startTripQuery = queryCreator.insertSingle(
                        dbConstants.tables.trips,
                        tripInfo
                      )
                      sqlPool.makeQuery(
                        startTripQuery,
                        async (error, insertStatus) => {
                          if (error) {
                            Sentry.captureException(error, { tripDetails })
                            logger(
                              'Error: Failed to start trip. Unable to start trip for user with user id:',
                              tripDetails.user_id,
                              ' query:',
                              startTripQuery
                            )
                            return callback(errors.internalServer(false), null)
                          }
                          tripDetails.trip_id = insertStatus.insertId
                          try {
                            await db
                              .main('ports')
                              .update({
                                current_status:
                                  bikeConstants.current_status.onTrip,
                                current_trip_id: tripDetails.trip_id,
                                current_user_id: tripDetails.user_id,
                                last_user_id: tripDetails.user_id
                              })
                              .where({ port_id: tripDetails.port_id })
                            bookingHandler.updateBooking(
                              null,
                              { trip_id: tripDetails.trip_id },
                              { booking_id: bookingDetails[0].booking_id },
                              async (error) => {
                                if (error) {
                                  Sentry.captureException(error, {
                                    tripDetails
                                  })
                                  logger(
                                    'Error: Failed to start trip. Unable to update booking'
                                  )
                                  return callback(error, null)
                                }
                                const fleetMeta = await db
                                  .main('fleet_metadata')
                                  .where({ fleet_id: fleetId })
                                  .first()
                                if (
                                  fleet.start_trip_email &&
                                  fleetMeta.email_on_start_trip
                                ) {
                                  const bucket =
                                    envConfig('OVAL_MODE') &&
                                    envConfig('OVAL_MODE') === 'production'
                                      ? 'lattis.production/fleet_start_trip_email'
                                      : 'lattis.development/fleet_start_trip_email'

                                  const options = {
                                    bucket,
                                    key: fleet.start_trip_email
                                  }
                                  const s3Handler = new S3Handler()
                                  logger('Info:  S3 options', options)
                                  s3Handler.download(
                                    options,
                                    (error, email) => {
                                      if (error) {
                                        Sentry.captureException(error, {
                                          tripDetails,
                                          options
                                        })
                                        logger(
                                          'Error: failed to start trip email content with error:',
                                          error
                                        )
                                        return
                                      }

                                      const mailObject = {
                                        subject:
                                          'New Occupation On Port Started',
                                        content: email.body
                                          .toString('utf-8')
                                          .replace(/(?:\r\n|\r|\n)/g, '<br>')
                                      }

                                      logger(
                                        'Info: Sending email to',
                                        tripDetails.email
                                      )
                                      mailHandler.sendMail(
                                        tripDetails.email,
                                        mailObject.subject,
                                        mailObject.content,
                                        null,
                                        null,
                                        (error) => {
                                          if (error) {
                                            Sentry.captureException(error, {
                                              tripDetails
                                            })
                                            logger(
                                              'Error: Failed to send mail for createTicket to the recipient with email',
                                              tripDetails.email,
                                              error
                                            )
                                          }
                                        }
                                      )
                                    }
                                  )
                                }
                                callback(null, {
                                  trip_id: tripDetails.trip_id,
                                  do_not_track_trip: doNotTrackTrip
                                })
                              }
                            )
                          } catch (error) {
                            Sentry.captureException(error, { tripDetails })
                            logger(
                              'Error: Failed to start trip. Unable to update port details'
                            )
                            return callback(error, null)
                          }
                        }
                      )
                    }
                  )
                }
              }
            )
          }
        )
      } catch (error) {
        Sentry.captureException(error, { tripDetails })
        logger(
          'Error: Failed to start trip. Unable to get the port details with port_id: ',
          tripDetails.port_id
        )
        return callback(
          errors.customError(
            error.message || 'An error occurred starting trip on port',
            error.code || responseCodes.InternalServer,
            error.name || 'PortStartTripError',
            true
          ),
          null
        )
      }
      break
    case 'hub_id':
      try {
        const hubInfo = await getHubById(tripDetails.hub_id)
        if (hubInfo.type !== 'parking_station') {
          return callback(
            errors.customError(
              'A trip can only be started on hub of type parking station',
              responseCodes.Conflict,
              'InvalidHubType'
            ),
            null
          )
        }
        const fleet = hubInfo.fleet
        const fleetId = fleet.fleet_id
        hubInfo.fleet_id = fleetId

        fleetHandler.getFleetMetaData(
          { fleet_id: fleetId },
          (error, result) => {
            if (error) {
              Sentry.captureException(error, { tripDetails })
              logger('Error: getting fleets with fleet_id:', fleetId)
              return callback(error, null)
            }
            fleetDetails = result
            if (fleetDetails[0].require_phone_number) {
              if (!tripDetails.phone_number) {
                logger(
                  'Info: The user does not have a phone number which is required for this fleet'
                )
                return callback(
                  errors.customError(
                    'Phone number required for this fleet',
                    responseCodes.Conflict,
                    'Phone number required'
                  ),
                  null
                )
              }
            }

            tripDetails = Object.assign(
              tripDetails,
              _.pick(fleet, 'fleet_id', 'operator_id', 'customer_id')
            )
            bookingHandler.getActiveBooking(
              _.pick(tripDetails, 'hub_id', 'user_id'),
              (error, bookingDetails) => {
                if (error) {
                  Sentry.captureException(error, { tripDetails })
                  logger(
                    'Error: Failed to get booking details with',
                    _.pick(tripDetails, 'hub_id', 'user_id'),
                    'with error:',
                    error
                  )
                  return callback(error, null)
                }
                if (!bookingDetails) {
                  logger(
                    'there is no active booking for the hub to this user:',
                    tripDetails.user_id
                  )
                  return callback(errors.resourceNotFound(false), null)
                }
                tripDetails.device_type = 'hub'
                if (bookingDetails[0].trip_id) {
                  tripDetails.date_created = now
                  updateTrip(
                    null,
                    Object.assign(_.omit(tripDetails, 'steps'), {
                      trip_id: bookingDetails[0].trip_id
                    }),
                    async (err, data) => {
                      if (err) callback(err)
                      else {
                        callback(null, data)
                      }
                    }
                  )
                } else {
                  if (tripDetails.is_payment_fleet) {
                    tripDetails.date_created = tripDetails.trip_start_time
                  } else {
                    tripDetails.time_lock_connected = now
                    tripDetails.date_created = now
                  }
                  tripDetails.steps = null

                  _getTripAddress(
                    hubInfo.latitude,
                    hubInfo.longitude,
                    async (error, address) => {
                      if (error) {
                        Sentry.captureException(error, { tripDetails })
                        logger(
                          `Error: Failed to get address for location: [${hubInfo.latitude},${hubInfo.longitude}]`
                        )
                      }
                      tripDetails.start_address = address || `Unnamed Road`
                      const tripInfo = _.pick(
                        tripDetails,
                        'hub_id',
                        'user_id',
                        'fleet_id',
                        'customer_id',
                        'start_address',
                        'date_created',
                        'first_lock_connect',
                        'time_lock_connected',
                        'device_token',
                        'device_type'
                      )

                      if (tripDetails.pricing_option_id) {
                        try {
                          const pricingOption = await pricing.handlers.retrieve(
                            {
                              id: tripDetails.pricing_option_id,
                              fleetId: bookingDetails[0].fleet_id
                            }
                          )

                          if (!pricingOption) {
                            throw errors.customError(
                              `Invalid pricing option: ${tripDetails.pricing_option_id}`,
                              responseCodes.BadRequest,
                              'BadRequest'
                            )
                          }

                          tripInfo.pricing_option_id =
                            pricingOption.pricing_option_id
                        } catch (error) {
                          Sentry.captureException(error, { tripDetails })
                          return callback(error)
                        }
                      }

                      const activeTrip = await isUserOnActiveTrip(
                        tripInfo['user_id']
                      )
                      if (activeTrip && !activeTrip.reservation_id) {
                        logger(
                          `The user ${tripInfo['user_id']} is already using hub ${activeTrip['hub_id']}`
                        )
                        return callback(
                          errors.formatErrorForWire({
                            message: `The user ${tripInfo['user_id']} is already using hub ${activeTrip['hub_id']}`,
                            code: 500
                          }),
                          { trip: activeTrip }
                        )
                      }

                      const startTripQuery = queryCreator.insertSingle(
                        dbConstants.tables.trips,
                        tripInfo
                      )
                      sqlPool.makeQuery(
                        startTripQuery,
                        async (error, insertStatus) => {
                          if (error) {
                            Sentry.captureException(error, {
                              tripDetails,
                              startTripQuery
                            })
                            logger(
                              'Error: Failed to start trip. Unable to start trip for user with user id:',
                              tripDetails.user_id,
                              ' query:',
                              startTripQuery
                            )
                            return callback(errors.internalServer(false), null)
                          }
                          tripDetails.trip_id = insertStatus.insertId
                          try {
                            await db
                              .main('hubs_and_fleets')
                              .update({
                                current_status:
                                  bikeConstants.current_status.onTrip
                              })
                              .where({ hub_uuid: hubInfo.hub_uuid })
                            await db
                              .main('hubs')
                              .update({
                                current_status:
                                  bikeConstants.current_status.onTrip,
                                current_trip_id: tripDetails.trip_id,
                                current_user_id: tripDetails.user_id,
                                last_user_id: tripDetails.user_id
                              })
                              .where({ uuid: hubInfo.hub_uuid })

                            bookingHandler.updateBooking(
                              null,
                              { trip_id: tripDetails.trip_id },
                              { booking_id: bookingDetails[0].booking_id },
                              async (error) => {
                                if (error) {
                                  Sentry.captureException(error, {
                                    tripDetails,
                                    booking_id: bookingDetails[0].booking_id
                                  })
                                  logger(
                                    'Error: Failed to start trip. Unable to update booking'
                                  )
                                  return callback(error, null)
                                }
                                if (
                                  fleet.start_trip_email &&
                                  fleet.email_on_start_trip
                                ) {
                                  const bucket =
                                    envConfig('OVAL_MODE') &&
                                    envConfig('OVAL_MODE') === 'production'
                                      ? 'lattis.production/fleet_start_trip_email'
                                      : 'lattis.development/fleet_start_trip_email'

                                  const options = {
                                    bucket,
                                    key: fleet.start_trip_email
                                  }
                                  const s3Handler = new S3Handler()
                                  logger('Info:  S3 options', options)
                                  s3Handler.download(
                                    options,
                                    (error, email) => {
                                      if (error) {
                                        Sentry.captureException(error, {
                                          tripDetails,
                                          options
                                        })
                                        logger(
                                          'Error: failed to start trip email content with error:',
                                          error
                                        )
                                        return
                                      }

                                      const mailObject = {
                                        subject:
                                          'New Rental on Parking Station Started',
                                        content: email.body
                                          .toString('utf-8')
                                          .replace(/(?:\r\n|\r|\n)/g, '<br>')
                                      }

                                      logger(
                                        'Info: Sending email to',
                                        tripDetails.email
                                      )
                                      mailHandler.sendMail(
                                        tripDetails.email,
                                        mailObject.subject,
                                        mailObject.content,
                                        null,
                                        null,
                                        (error) => {
                                          if (error) {
                                            Sentry.captureException(error, {
                                              tripDetails
                                            })
                                            logger(
                                              'Error: Failed to send mail for createTicket to the recipient with email',
                                              tripDetails.email,
                                              error
                                            )
                                          }
                                        }
                                      )
                                    }
                                  )
                                }
                                callback(null, {
                                  trip_id: tripDetails.trip_id,
                                  do_not_track_trip: doNotTrackTrip
                                })
                              }
                            )
                          } catch (error) {
                            Sentry.captureException(error, { tripDetails })
                            logger('Error: Failed to start trip n hub.')
                            return callback(error, null)
                          }
                        }
                      )
                    }
                  )
                }
              }
            )
          }
        )
      } catch (error) {
        Sentry.captureException(error, { tripDetails })
        logger('Error: Failed to start trip on hub: ', tripDetails.hub_id)
        return callback(errors.internalServer(false), null)
      }
      break
    default:
      return callback(
        errors.errorWithMessage({ message: 'Not supported' }),
        null
      )
  }
}

const startReservationTrip = async ({ reservation, bike, port, hub, trip }) => {
  const _getTripAddressAsync = util.promisify(_getTripAddress)

  const now = moment().unix()

  const currentTrip = await db
    .main('trips')
    .where({ user_id: reservation.user_id })
    .andWhere(function () {
      this.whereNotNull('date_created')
      this.whereNull('end_address')
      this.whereNull('date_endtrip')
      this.where('date_created', '<=', now)
    })
    .first()
    .select('trip_id')

  if (currentTrip) {
    throw errors.customError(
      'Cannot start reservation trip. This user has a trip in progress.',
      responseCodes.Conflict,
      'Conflict'
    )
  }

  const startAddress = await _getTripAddressAsync(
    bike.latitude,
    bike.longitude
  )

  const tripDetails = {
    start_address: startAddress || '',
    // TODO (waiyaki 2020-07-16) How to handle bikes with controllers?
    customer_id: bike.customer_id || null,
    first_lock_connect: 1,
    time_lock_connected: now,
    date_created: now
  }
  if (bike) {
    tripDetails.steps = JSON.stringify([[bike.latitude, bike.longitude, now]])
    tripDetails.lock_id = bike.lock_id || null
  }

  const trx = await db.main.transaction()

  await trx('trips').where({ trip_id: trip.trip_id }).update(tripDetails)
  if (bike) {
    await trx('bikes').where({ bike_id: bike.bike_id }).update({
      current_status: bikeConstants.current_status.onTrip,
      current_trip_id: trip.trip_id,
      current_user_id: reservation.user_id,
      last_user_id: reservation.user_id
    })
  }

  if (port) {
    await trx('ports').where({ port_id: port.port_id }).update({
      current_status: bikeConstants.current_status.onTrip,
      current_trip_id: trip.trip_id,
      current_user_id: reservation.user_id
    })
  }

  if (hub) {
    await trx('hubs').where({ hub_id: hub.hub_id }).update({
      current_status: bikeConstants.current_status.onTrip
    })
    await trx('hubs_and_fleets').where({ hub_uuid: hub.uuid }).update({
      current_status: bikeConstants.current_status.onTrip,
      current_trip_id: trip.trip_id,
      current_user_id: reservation.user_id
    })
  }

  await trx.commit()

  const { trips: startedTrip, fleet_metadata: metadata } = await db
    .main('trips')
    .where({ 'trips.trip_id': trip.trip_id })
    .join('fleet_metadata', { 'fleet_metadata.fleet_id': 'trips.fleet_id' })
    .first()
    .options({ nestTables: true })
    .select('trips.*', 'fleet_metadata.do_not_track_trip')

  if (bike) {
    startedTrip.do_not_track_trip = metadata.do_not_track_trip === 1
    startedTrip.steps = JSON.parse(startedTrip.steps)
  } else {
    startedTrip.do_not_track_trip = 1
  }

  await startTripsInGPSService(startedTrip, metadata.geofence_enabled === 1, metadata.do_not_track_trip === 1)

  return startedTrip
}

const sendGrowStartupStartTripCommands = async (controller) => {
  try {
    if (controller && controller.vendor === 'Grow') {
      executeCommand(`s/c/${controller.key}`, `39,10`, (error, message) => {
        // Set GPS interval to 10sec
        if (error) {
          Sentry.captureException(error, { controller })
          logger(`An error occurred setting GPS interval to 10 sec: ${error}`)
        } else {
          logger(`GPS interval command dispatched to ${controller.key}`)
        }
      })
    }
  } catch (error) {
    Sentry.captureException(error, { controller })
    logger('An error occurred GROW commands')
  }
}

const checkForParkingViolation = util.promisify(
  function checkForParkingViolation (
    parkingDetails,
    parkingViolation,
    callback
  ) {
    if (parkingViolation) {
      const ticketParameters = {
        category: ticketConstants.category.parkingOutsideGeofence,
        bike_id: parkingDetails.bike_id,
        lock_id: parkingDetails.lock_id,
        fleet_id: parkingDetails.fleet_id,
        operator_id: parkingDetails.operator_id,
        customer_id: parkingDetails.customer_id,
        trip_id: parkingDetails.trip_id
      }
      if (parkingDetails.parking_image) {
        ticketParameters.parking_image = parkingDetails.parking_image
      }
      ticketHandler.createTicket(ticketParameters, (error) => {
        if (error) {
          Sentry.captureException(error, { parkingDetails })
          logger('Error: creating ticket for parking violation', error)
          return callback(error)
        }
        callback(null)
      })
    } else {
      callback(null)
    }
  }
)

const getBikeLocationsFromIoTs = async (trip, tripDetails) => {
  try {
    const controller = await db
      .main('controllers')
      .where({ bike_id: trip.bike_id })
      .first()
    if (controller) {
      let status
      const vendor = controller.vendor
      switch (vendor) {
        case 'Segway':
          status = await segwayApiClientForUs.getVehicleInformation(
            controller.key
          )
          break
        case 'Segway IoT EU':
          status = await segwayApiClientForEu.getVehicleInformation(
            controller.key
          )
          break
        case 'Edge':
          const tripData = await db.main('trips').where({ trip_id: trip.trip_id }).first()
          const lastUserId = tripData ? tripData.user_id : ''
          status = await new EdgeApiClient(controller.fleet_id, lastUserId).getVehicleInformation(controller.key)
          break
        case 'ACTON':
        case 'Omni Lock':
        case 'Omni IoT':
        case 'Teltonika':
          let clientApi = actonFamily[vendor]
          status = await clientApi.getCurrentStatus(controller.key)
      }
      if (status && ['Segway', 'Segway IoT EU', 'Edge'].includes(controller.vendor)) {
        const latitude = status.latitude
        const longitude = status.longitude
        if (longitude && latitude) {
          tripDetails = { ...tripDetails, latitude, longitude }
        }
      } else if (
        status &&
        ['ACTON', 'Omni IoT', 'Teltonika'].includes(vendor)
      ) {
        const latitude = status.lat
        const longitude = status.lon
        if (longitude && latitude) {
          tripDetails = { ...tripDetails, latitude, longitude }
        }
      }
    }
    return tripDetails
  } catch (error) {
    Sentry.captureException(error, { trip })
    logger(`Error:: error getting end trip GPS location from IoTs, ${error.message}`)
    return false
  }
}

const createBikeParams = (bike, tripObj, tripDetails, trips) => {
  const bikeColumns = {
    bike_id: bike.bike_id,
    current_status: bikeConstants.current_status.parked,
    distance: bike.distance + tripObj.distance,
    distance_after_service: bike.distance_after_service + tripObj.distance,
    current_trip_id: null,
    current_user_id: null
  }
  if (tripDetails.bike_damaged) {
    bikeColumns.status = bikeConstants.status.suspended
    bikeColumns.current_status = bikeConstants.current_status.damaged
  }
  if (['reported_stolen'].includes(bike.current_status)) {
    bikeColumns.current_status = bikeConstants.current_status.reportedStolen
    bikeColumns.maintenance_status = null
  }
  const bikeParams = Object.assign(
    { parking_spot_id: tripDetails.parking_spot_id },
    bikeColumns,
    trips[0].first_lock_connect === 1
      ? {
        latitude: tripDetails.latitude,
        longitude: tripDetails.longitude
      }
      : {}
  )
  return bikeParams
}

const processEndTripRequestToGPSService = async (
  bike,
  tripDetails,
  callback
) => {
  try {
    const controller = await db
      .main('controllers')
      .where({ bike_id: bike.bike_id })
      .select(
        'controller_id',
        'key',
        'device_type',
        'vendor',
        'make',
        'fleet_id'
      ).first()
    if (
      controller &&
      [
        'Segway',
        'Segway IoT EU',
        'Grow',
        'Geotab IoT',
        'ACTON',
        'Omni IoT',
        'Teltonika',
        'Sentinel',
        'Edge'
      ].includes(controller.vendor)
    ) {
      const tripDetailsForGPSTrackingService = {
        ..._.pick(
          tripDetails,
          'trip_id',
          'steps',
          'end_address',
          'date_endtrip'
        ),
        bike_id: bike.bike_id
      }
      if (controller.vendor === 'Sentinel') {
        try {
          await new Sentinel().endTrip(controller.key)
        } catch (error) {
          logger(`:::An error occurred ending sentinel API trip ${controller.key}: ${tripDetails.trip_id}`)
          Sentry.captureException(error, { controller })
        }
      }

      if (controller.vendor === 'Edge') {
        try {
          const tripData = await db.main('trips').where({ trip_id: tripDetails.trip_id }).first()
          const lastUserId = tripData ? tripData.user_id : ''
          const edgeInfo = await new EdgeApiClient(controller.fleet_id, lastUserId).getVehicleInformation(controller.key)
          const edgeTripId = edgeInfo.trip && edgeInfo.trip.id
          if (edgeTripId) {
            const endtripInfo = await new EdgeApiClient(controller.fleet_id, lastUserId).endTrip(edgeTripId)
            await db.main('trips').update({ edge_trip_id: null }).where({ edge_trip_id: edgeTripId })
            if (endtripInfo.end) {
              const endLocation = { latitude: endtripInfo.end.lat, longitude: endtripInfo.end.lon }
              db.main('bikes').update(endLocation).where({ bike_id: tripDetails.bikeId })
              db.main('controllers').update(endLocation).where({ bike_id: tripDetails.bikeId })
            }
          }
        } catch (error) {
          logger(`:::An error occurred ending Edge API trip ${controller.key}: ${tripDetails.trip_id}`)
          Sentry.captureException(error, { controller })
        }
      }
      await sendEndTripRequestToGPSService(tripDetailsForGPSTrackingService)
      if (['Segway', 'Segway IoT EU'].includes(controller.vendor)) {
        await bikeHandler.setBikeLights(0, controller)
      }
    }
  } catch (error) {
    Sentry.captureException(error, { bike })
    logger(
      `Error updating GPS service ${tripDetails.trip_id}:: ${
        error.message || error
      }`
    )
    return callback(
      errors.customError(
        `An error occurred updating GPS service trip ${tripDetails.trip_id})`,
        responseCodes.InternalServer,
        'InternalServer'
      )
    )
  }
}

/**
 * To end trip with trip details
 *
 * @param {Object} tripDetails - Contains user and lock identifiers
 * @param {Function} callback
 */
const endTrip = (tripDetails, callback) => {
  getTrips({ trip_id: tripDetails.trip_id }, true, async (error, trips) => {
    if (error) {
      Sentry.captureException(error, { tripDetails })
      return callback(error, null)
    }
    if (trips.length === 0) {
      logger(
        'Error: While Ending trip. No trip found with trip_id:',
        tripDetails.trip_id
      )
      return callback(errors.resourceNotFound(false), null)
    }
    const trip = trips[0]
    if (trip.bike_id) {
      const tripDetailsWithGPSData = await getBikeLocationsFromIoTs(
        trip,
        tripDetails
      )
      tripDetails = tripDetailsWithGPSData || tripDetails
    }
    ticketHandler.getTicket(
      {
        fleet_id: trip.fleet_id,
        category: 'reported_theft',
        status: 'created',
        trip_id: trip.trip_id
      },
      async (error, tickets) => {
        if (error) {
          Sentry.captureException(error, { tripDetails })
          logger('Error: getting theft ticket for trip', trip.fleet_id)
          return callback(error, null)
        }
        let theftTicket
        if (tickets.length > 0) {
          theftTicket = tickets[0]
        }
        if (!trip.end_address) {
          logger(`endtrip:: trip missing end address, ending with payment...`, {
            trip_id: trip.trip_id,
            fleet: trip.fleet_id
          })

          fleetHandler.getFleets(
            { fleet_id: trip.fleet_id },
            (error, fleetDetails) => {
              if (error) {
                Sentry.captureException(error, { tripDetails })
                logger(
                  'Error: get trip duration cost. Failed to get fleet',
                  trip.fleet_id
                )
                return callback(errors.internalServer(false), null)
              }
              bookingHandler.getActiveBooking(
                _.pick(tripDetails, 'trip_id'),
                async (error, activeBooking) => {
                  if (error) {
                    Sentry.captureException(error, { tripDetails })
                    logger(
                      'Error: Failed to get booking details with',
                      _.pick(tripDetails, 'trip_id'),
                      'with error:',
                      error
                    )
                    return callback(error, null)
                  }
                  const bikeHasAdapter = await db.main('controllers').where({ bike_id: trip.bike_id, device_type: 'adapter' }).first()
                  let isDockedToHub
                  if (trip.bike_id) {
                    const bikeInfo = await db.main('bikes').where({ bike_id: trip.bike_id }).first()
                    isDockedToHub = await db.main('ports')
                      .where({ vehicle_uuid: bikeInfo.bike_uuid })
                      .leftJoin('hubs', 'ports.hub_uuid', 'hubs.uuid')
                      .select('hubs.latitude', 'hubs.longitude').first()
                    if (isDockedToHub) {
                      if (isDockedToHub.latitude) tripDetails.latitude = isDockedToHub.latitude
                      if (isDockedToHub.longitude) tripDetails.longitude = isDockedToHub.longitude
                    }
                  }
                  if (trip.bike_id && !activeBooking) {
                    if (
                      fleetDetails[0].skip_parking_image === 0 &&
                      !_.has(tripDetails, 'parking_image')
                    ) {
                      logger('Error: Image required but not provided')
                      return callback(errors.missingParameter(true), null)
                    }
                  }
                  if (trip.bike_id) {
                    trip.distance = tripHelper.getTripDistance(trip)
                    bikeHandler.getBikeWithLock(
                      _.pick(trip, 'bike_id'),
                      (error, bikes) => {
                        if (error) {
                          Sentry.captureException(error, { trip })
                          logger(
                            `Error: Getting bike with ID ${_.pick(
                              trip,
                              'bike_id'
                            )}`,
                            error
                          )
                          return callback(error, null)
                        }
                        const bike = bikes[0]
                        if (!tripDetails.latitude && !tripDetails.longitude) {
                          tripDetails = Object.assign(tripDetails, {
                            latitude: bike.latitude,
                            longitude: bike.longitude
                          })
                        }

                        _getTripAddress(
                          tripDetails.latitude,
                          tripDetails.longitude,
                          (error, address) => {
                            if (error) {
                              Sentry.captureException(error, { tripDetails })
                              logger(
                                `Error: Getting trip address for ${tripDetails.latitude},${tripDetails.longitude}`,
                                error
                              )
                              return callback(
                                errors.internalServer(false),
                                null
                              )
                            }
                            tripDetails.end_address =
                              address ||
                              `Unnamed Road ${tripDetails.latitude},${tripDetails.longitude}`
                            tripDetails.steps = [
                              [
                                tripDetails.latitude,
                                tripDetails.longitude,
                                moment().unix()
                              ]
                            ]
                            tripDetails.date_endtrip = moment().unix()

                            bikeHandler.checkForServiceDue(bike, (error) => {
                              if (error) {
                                Sentry.captureException(error, { tripDetails })
                                logger(
                                  `Error checking service due status for bike ${bike.bike_id}`,
                                  error.message || error
                                )
                                return callback(error, null)
                              }

                              let fee = 0.00
                              let outside
                              const parkingSettingsObject = {
                                currency: paymentHandler.getFleetCurrency(fleetDetails[0])
                              }

                              // Check parking restrictions for bikes with adapters to stations
                              if (bikeHasAdapter && !isDockedToHub) {
                                outside = true
                                if (fleetDetails[0].parking_area_restriction > 0) {
                                  parkingSettingsObject.not_allowed = true
                                  // Parking restriction set in fleet
                                  if (fleetDetails[0].type === fleetConstants.fleet_type.publicWithPayment ||
                                    fleetDetails[0].type === fleetConstants.fleet_type.privateWithPayment) {
                                    fee = fleetDetails[0].price_for_penalty_outside_parking
                                    parkingSettingsObject.fee = fee
                                  }
                                }
                                parkingSettingsObject.outside = outside
                                if (parkingSettingsObject.not_allowed && !theftTicket) {
                                  logger(
                                    `Cannot end a trip when not docked to a station ${bike.bike_id}`
                                  )
                                  return callback(
                                    errors.customError(
                                      'Cannot end a trip when not docked to a station',
                                      405,
                                      'InvalidParkingZone',
                                      true
                                    ),
                                    null
                                  )
                                }
                              }

                              fleetHandler.checkParkingFees(
                                Object.assign(
                                  { fleet_id: trip.fleet_id },
                                  _.pick(tripDetails, [
                                    'latitude',
                                    'longitude',
                                    'skip_parking_fee'
                                  ])
                                ),
                                async (error, parkingFees) => {
                                  if (error) {
                                    Sentry.captureException(error, {
                                      tripDetails
                                    })
                                    logger(
                                      `Error checking parking fees for bike ${bike.bike_id}`,
                                      error.message || error
                                    )
                                    return callback(error, null)
                                  }

                                  // Enforce parking rule by refusing to end the trip when the
                                  // ride is outside the parking zones and a parking area restriction is in place.
                                  if (parkingFees.not_allowed && !theftTicket) {
                                    logger(
                                      `Cannot end a trip when not within a parking zone for bike ${bike.bike_id}`
                                    )
                                    return callback(
                                      errors.customError(
                                        'Cannot end the trip when not within a parking zone',
                                        405,
                                        'InvalidParkingZone',
                                        true
                                      ),
                                      null
                                    )
                                  }

                                  const trx = await db.main.transaction()
                                  try {
                                    if (!theftTicket) {
                                      await checkForParkingViolation(
                                        Object.assign(
                                          _.pick(
                                            tripDetails,
                                            'trip_id',
                                            'parking_image'
                                          ),
                                          _.pick(
                                            bike,
                                            'bike_id',
                                            'lock_id',
                                            'fleet_id',
                                            'operator_id',
                                            'customer_id'
                                          )
                                        ),
                                        parkingFees.outside
                                      )
                                    }

                                    const fleetPaymentSettings =
                                      fleetDetails[0]

                                    const unchargedTrip = Object.assign(
                                      trip,
                                      { parking_fees: parkingFees },
                                      _.pick(tripDetails, [
                                        'latitude',
                                        'longitude',
                                        'skip_parking_fee',
                                        'pricing_option_id'
                                      ])
                                    )

                                    const tripObj = await _chargeForTrip({
                                      unchargedTrip,
                                      fleetPaymentSettings,
                                      requestParams: tripDetails
                                    })

                                    const updateTripDetails = _.pick(
                                      tripDetails,
                                      'trip_id',
                                      'steps',
                                      'end_address',
                                      'parking_image',
                                      'date_endtrip'
                                    )

                                    updateTrip(
                                      null,
                                      updateTripDetails,
                                      async (error) => {
                                        if (error) {
                                          Sentry.captureException(error, {
                                            updateTripDetails
                                          })
                                          logger(
                                            `Error: updating trip query with user id: ${tripDetails.trip_id}`,
                                            error
                                          )
                                          return callback(
                                            errors.internalServer(false),
                                            null
                                          )
                                        }

                                        logger(
                                          `Success:: Trip ${tripDetails.trip_id} updated successfully`
                                        )

                                        await processEndTripRequestToGPSService(
                                          bike,
                                          tripDetails,
                                          callback
                                        )
                                        const tapKeyController = await db
                                          .main('controllers')
                                          .where({ bike_id: trip.bike_id })
                                          .where({ vendor: 'Tap Key' })

                                        try {
                                          await reservationHelpers.endTripReservation(
                                            {
                                              trip: {
                                                user_id: trip.user_id,
                                                reservation_id:
                                                  trip.reservation_id,
                                                date_endtrip:
                                                  tripDetails.date_endtrip
                                              }
                                            },
                                            trx
                                          )
                                        } catch (error) {
                                          Sentry.captureException(error, {
                                            tripDetails
                                          })
                                          logger(
                                            `Error:: ending trip reservation for trip ${tripDetails.trip_id}, user ${trip.user_id}`
                                          )
                                          // TODO:  Handle this partial failure.
                                          return callback(error)
                                        }

                                        bookingHandler.updateBooking(
                                          trx,
                                          {
                                            status:
                                              tripConstants.booking.finished,
                                            cancelled_on: moment().unix()
                                          },
                                          _.pick(tripDetails, 'trip_id'),
                                          (error) => {
                                            if (error) {
                                              Sentry.captureException(error, {
                                                tripDetails
                                              })
                                              logger(
                                                `Error:: updating booking for trip ${tripDetails.trip_id}, user ${trip.user_id}`
                                              )
                                              return callback(
                                                errors.internalServer(false),
                                                null
                                              )
                                            }
                                            const bikeParams = createBikeParams(
                                              bike,
                                              tripObj,
                                              tripDetails,
                                              trips
                                            )
                                            bikeHandler.updateBike(
                                              trx,
                                              bikeParams,
                                              async (error) => {
                                                if (error) {
                                                  Sentry.captureException(
                                                    error,
                                                    { bikeParams }
                                                  )
                                                  logger(
                                                    `Error: Updating bike ${bikeParams.bike_id} on end trip ${tripDetails.trip_id}`
                                                  )
                                                  return callback(error, null)
                                                }
                                                await trx.commit()
                                                getTripsWithPaymentInfo(
                                                  {
                                                    trip_id:
                                                      tripDetails.trip_id
                                                  },
                                                  async (error, finalTrips) => {
                                                    if (error) {
                                                      Sentry.captureException(
                                                        error,
                                                        { tripDetails }
                                                      )
                                                      logger(
                                                        `Error: Getting payment info for trip ${tripDetails.trip_id}`
                                                      )
                                                      return callback(
                                                        error,
                                                        null
                                                      )
                                                    }
                                                    if (
                                                      tapKeyController.length &&
                                                      finalTrips[0] &&
                                                      finalTrips[0].total === 0
                                                    ) {
                                                      try {
                                                        const [controller] =
                                                          tapKeyController
                                                        const details = {
                                                          lockId:
                                                            controller.key,
                                                          fleetId:
                                                            trip.fleet_id
                                                        }
                                                        const {
                                                          passPhrase,
                                                          tokenIssuer,
                                                          idpClientId,
                                                          provider,
                                                          ownerAccountId,
                                                          authCodeClientSecret,
                                                          tapKeyPrivateKey,
                                                          lattisRefreshToken,
                                                          grantType,
                                                          loginUrl,
                                                          baseUrl,
                                                          redirectUri,
                                                          metadata,
                                                          operatorAccountId,
                                                          authCodeClientId
                                                        } = await fetchTapKeyConfiguration(
                                                          details
                                                        )
                                                        const tapKeyApiClient =
                                                          new TapKeyApiClient({
                                                            idpClientId:
                                                              idpClientId.Value,
                                                            grantType:
                                                              grantType.Value,
                                                            provider:
                                                              provider.Value,
                                                            loginUrl:
                                                              loginUrl.Value,
                                                            passPhrase:
                                                              passPhrase.Value,
                                                            tokenIssuer:
                                                              tokenIssuer.Value,
                                                            refreshToken:
                                                              metadata.refreshToken,
                                                            lattisRefreshToken:
                                                              lattisRefreshToken.Value,
                                                            accessToken:
                                                              metadata.accessToken,
                                                            baseUrl:
                                                              baseUrl.Value,
                                                            ownerAccountId:
                                                              ownerAccountId.Value,
                                                            operatorAccountId:
                                                              operatorAccountId.ownerAccountId,
                                                            authCodeClientId:
                                                              authCodeClientId.Value,
                                                            authCodeClientSecret:
                                                              authCodeClientSecret.Value,
                                                            tapKeyPrivateKey:
                                                              tapKeyPrivateKey.Value,
                                                            redirectUri:
                                                              redirectUri.Value
                                                          })
                                                        // make call to revoke grant
                                                        await tapKeyApiClient.revokeGrant(
                                                          {
                                                            userId:
                                                              trip.user_id.toString()
                                                          }
                                                        )
                                                        return callback(
                                                          null,
                                                          finalTrips[0]
                                                        )
                                                      } catch (error) {
                                                        Sentry.captureException(
                                                          error,
                                                          {
                                                            tripDetails,
                                                            tapKeyController,
                                                            trip
                                                          }
                                                        )
                                                        logger(
                                                          `Error: An error occurred while revoking grant for user id: ${trip.user_id}`
                                                        )
                                                        return callback(
                                                          error,
                                                          null
                                                        )
                                                      }
                                                    }
                                                    return callback(
                                                      null,
                                                      finalTrips[0]
                                                    )
                                                  }
                                                )
                                              }
                                            )
                                          }
                                        )
                                      }
                                    )
                                  } catch (error) {
                                    Sentry.captureException(error, {
                                      tripDetails
                                    })
                                    logger(
                                      'endtrip:: error ending trip',
                                      errors.errorWithMessage(error)
                                    )
                                    await trx.rollback(
                                      error.message ||
                                        error ||
                                        `An error occurred ending trip ${tripDetails.trip_id}`
                                    )
                                    return callback(error)
                                  }
                                }
                              )
                            })
                          }
                        )
                      }
                    )
                  } else if (trip.hub_id) {
                    trip.distance = 0
                    const hubInfo = await getHubById(trip.hub_id)
                    tripDetails = Object.assign(tripDetails, {
                      latitude: hubInfo.latitude,
                      longitude: hubInfo.longitude
                    })
                    _getTripAddress(
                      hubInfo.latitude,
                      hubInfo.longitude,
                      (error, address) => {
                        if (error) {
                          Sentry.captureException(error, {
                            tripDetails,
                            hubInfo
                          })
                          logger(
                            `Error: Getting trip address for ${tripDetails.latitude},${tripDetails.longitude}`,
                            error
                          )
                          return callback(errors.internalServer(false), null)
                        }
                        tripDetails.end_address =
                          address ||
                          `Unnamed Road ${hubInfo.latitude},${hubInfo.longitude}`
                        tripDetails.steps = []
                        tripDetails.date_endtrip = moment().unix()
                        fleetHandler.checkParkingFees(
                          Object.assign(
                            { fleet_id: trip.fleet_id },
                            _.pick(tripDetails, [
                              'latitude',
                              'longitude',
                              'skip_parking_fee'
                            ])
                          ),
                          async (error, parkingFees) => {
                            if (error) {
                              Sentry.captureException(error, { tripDetails })
                              logger(
                                `Error checking parking fees for hub ${hubInfo.hub_id}`,
                                error.message || error
                              )
                              return callback(error, null)
                            }
                            const trx = await db.main.transaction()
                            try {
                              const fleetPaymentSettings = fleetDetails[0]
                              const unchargedTrip = Object.assign(
                                trip,
                                { parking_fees: parkingFees },
                                _.pick(tripDetails, [
                                  'latitude',
                                  'longitude',
                                  'skip_parking_fee',
                                  'pricing_option_id'
                                ])
                              )

                              await _chargeForTrip({
                                unchargedTrip,
                                fleetPaymentSettings,
                                requestParams: tripDetails
                              })

                              const updateTripDetails = _.pick(
                                tripDetails,
                                'trip_id',
                                'steps',
                                'end_address',
                                'date_endtrip'
                              )

                              updateTrip(
                                null,
                                updateTripDetails,
                                async (error) => {
                                  if (error) {
                                    Sentry.captureException(error, {
                                      tripDetails
                                    })
                                    logger(
                                      `Error: updating trip query with user id: ${tripDetails.trip_id}`,
                                      error
                                    )
                                    return callback(
                                      errors.internalServer(false),
                                      null
                                    )
                                  }

                                  logger(
                                    `Success:: Trip ${tripDetails.trip_id} updated successfully`
                                  )
                                  try {
                                    await reservationHelpers.endTripReservation(
                                      {
                                        trip: {
                                          user_id: trip.user_id,
                                          reservation_id: trip.reservation_id,
                                          date_endtrip:
                                            tripDetails.date_endtrip
                                        }
                                      },
                                      trx
                                    )
                                  } catch (error) {
                                    Sentry.captureException(error, {
                                      tripDetails,
                                      trip
                                    })
                                    logger(
                                      `Error:: ending trip reservation for trip ${tripDetails.trip_id}, user ${trip.user_id}`
                                    )
                                    // TODO:  Handle this partial failure.
                                    return callback(error)
                                  }

                                  bookingHandler.updateBooking(
                                    trx,
                                    {
                                      status: tripConstants.booking.finished,
                                      cancelled_on: moment().unix()
                                    },
                                    _.pick(tripDetails, 'trip_id'),
                                    async (error) => {
                                      if (error) {
                                        Sentry.captureException(error, {
                                          tripDetails
                                        })
                                        logger(
                                          `Error:: updating booking for trip ${tripDetails.trip_id}, user ${trip.user_id}`
                                        )
                                        return callback(
                                          errors.internalServer(false),
                                          null
                                        )
                                      }
                                      await trx('hubs')
                                        .where({ hub_id: trip.hub_id })
                                        .update({
                                          status: 'Available',
                                          current_status: 'parked',
                                          current_trip_id: null,
                                          current_user_id: null
                                        })
                                      await trx('hubs_and_fleets')
                                        .where({ hub_uuid: hubInfo.hub_uuid })
                                        .update({ current_status: 'parked' })
                                      await trx.commit()
                                      getTripsWithPaymentInfo(
                                        { trip_id: tripDetails.trip_id },
                                        async (error, finalTrips) => {
                                          if (error) {
                                            Sentry.captureException(error, {
                                              tripDetails
                                            })
                                            logger(
                                              `Error: Getting payment info for trip ${tripDetails.trip_id}`
                                            )
                                            return callback(error, null)
                                          }
                                          return callback(null, finalTrips[0])
                                        }
                                      )
                                    }
                                  )
                                }
                              )
                            } catch (error) {
                              Sentry.captureException(error, { tripDetails })
                              logger(
                                'endtrip:: error ending trip',
                                errors.errorWithMessage(error)
                              )
                              await trx.rollback(
                                error.message ||
                                  error ||
                                  `An error occurred ending trip ${tripDetails.trip_id}`
                              )
                              return callback(error)
                            }
                          }
                        )
                      }
                    )
                  } else if (trip.port_id) {
                    const portInfo = await retrievePort({
                      portId: trip.port_id
                    })
                    const portHub = portInfo.hub
                    tripDetails = Object.assign(tripDetails, {
                      latitude: portHub.latitude,
                      longitude: portHub.longitude
                    })
                    _getTripAddress(
                      portHub.latitude,
                      portHub.longitude,
                      (error, address) => {
                        if (error) {
                          Sentry.captureException(error, {
                            tripDetails,
                            portInfo
                          })
                          logger(
                            `Error: Getting trip address for ${tripDetails.latitude},${tripDetails.longitude}`,
                            error
                          )
                          return callback(errors.internalServer(false), null)
                        }
                        tripDetails.end_address =
                          address ||
                          `Unnamed Road ${portHub.latitude},${portHub.longitude}`
                        tripDetails.date_endtrip = moment().unix()
                        fleetHandler.checkParkingFees(
                          Object.assign(
                            { fleet_id: trip.fleet_id },
                            _.pick(tripDetails, [
                              'latitude',
                              'longitude',
                              'skip_parking_fee'
                            ])
                          ),
                          async (error, parkingFees) => {
                            if (error) {
                              Sentry.captureException(error, { tripDetails })
                              logger(
                                `Error checking parking fees for port ${tripDetails.port_id}`,
                                error.message || error
                              )
                              return callback(error, null)
                            }
                            const trx = await db.main.transaction()
                            try {
                              const fleetPaymentSettings = fleetDetails[0]
                              const unchargedTrip = Object.assign(
                                trip,
                                { parking_fees: parkingFees },
                                _.pick(tripDetails, [
                                  'latitude',
                                  'longitude',
                                  'skip_parking_fee',
                                  'pricing_option_id'
                                ])
                              )

                              await _chargeForTrip({
                                unchargedTrip,
                                fleetPaymentSettings,
                                requestParams: tripDetails
                              })

                              const updateTripDetails = _.pick(
                                tripDetails,
                                'trip_id',
                                'end_address',
                                'date_endtrip'
                              )

                              updateTrip(
                                null,
                                updateTripDetails,
                                async (error) => {
                                  if (error) {
                                    Sentry.captureException(error, {
                                      tripDetails
                                    })
                                    logger(
                                      `Error: updating trip query with user id: ${tripDetails.trip_id}`,
                                      error
                                    )
                                    return callback(
                                      errors.internalServer(false),
                                      null
                                    )
                                  }

                                  logger(
                                    `Success:: Trip ${tripDetails.trip_id} updated successfully`
                                  )
                                  try {
                                    await reservationHelpers.endTripReservation(
                                      {
                                        trip: {
                                          user_id: trip.user_id,
                                          reservation_id: trip.reservation_id,
                                          date_endtrip:
                                            tripDetails.date_endtrip
                                        }
                                      },
                                      trx
                                    )
                                  } catch (error) {
                                    Sentry.captureException(error, {
                                      tripDetails,
                                      trip
                                    })
                                    logger(
                                      `Error:: ending trip reservation for trip ${tripDetails.trip_id}, user ${trip.user_id}`
                                    )
                                    // TODO:  Handle this partial failure.
                                    return callback(error)
                                  }

                                  bookingHandler.updateBooking(
                                    trx,
                                    {
                                      status: tripConstants.booking.finished,
                                      cancelled_on: moment().unix()
                                    },
                                    _.pick(tripDetails, 'trip_id'),
                                    async (error) => {
                                      if (error) {
                                        Sentry.captureException(error, {
                                          tripDetails
                                        })
                                        logger(
                                          `Error:: updating booking for trip ${tripDetails.trip_id}, user ${trip.user_id}`
                                        )
                                        return callback(
                                          errors.internalServer(false),
                                          null
                                        )
                                      }
                                      await trx('ports')
                                        .where({ port_id: trip.port_id })
                                        .update({
                                          status: 'Available',
                                          current_status: 'parked',
                                          current_trip_id: null,
                                          current_user_id: null
                                        })
                                      await trx.commit()
                                      getTripsWithPaymentInfo(
                                        { trip_id: tripDetails.trip_id },
                                        async (error, finalTrips) => {
                                          if (error) {
                                            Sentry.captureException(error, {
                                              tripDetails
                                            })
                                            logger(
                                              `Error: Getting payment info for trip ${tripDetails.trip_id}`
                                            )
                                            return callback(error, null)
                                          }
                                          return callback(null, finalTrips[0])
                                        }
                                      )
                                    }
                                  )
                                }
                              )
                            } catch (error) {
                              Sentry.captureException(error, { tripDetails })
                              logger(
                                'endtrip:: error ending trip',
                                errors.errorWithMessage(error)
                              )
                              await trx.rollback(
                                error.message ||
                                  error ||
                                  `An error occurred ending trip ${tripDetails.trip_id}`
                              )
                              return callback(error)
                            }
                          }
                        )
                      }
                    )
                  }
                }
              )
            }
          )
        } else {
          logger(
            `endtrip:: trip has an end address, ending without charging...`,
            {
              trip_id: trip.trip_id,
              fleet: trip.fleet_id
            }
          )
          getDurationCost(
            {
              type: fleetConstants.tripInfo.endTrip,
              fleet_id: trip.fleet_id,
              date_created: trip.date_created,
              date_endtrip: moment().unix(),
              trip
            },
            (error, tripsDurationCost) => {
              if (error) {
                Sentry.captureException(error, { trip })
                logger('Error: Failed to get duration and cost', trip.fleet_id)
                callback(error, null)
                return
              }
              trip.duration = Math.max(
                trip.date_endtrip - trip.date_created,
                0
              )
              trip.distance =
                trip.steps && trip.steps.length
                  ? tripHelper.getTripDistance(trip)
                  : 0
              let tripObj = Object.assign(trip, {
                currency: config.fleets.defaultCurrency.toUpperCase(),
                transaction_id: null,
                charge_for_duration: 0,
                penalty_fees: 0,
                deposit: 0,
                total: 0,
                over_usage_fees: 0,
                user_profile_id: null,
                card_id: null,
                date_charged: null
              })
              if (tripObj.steps) {
                tripObj.steps = JSON.parse(tripObj.steps)
              }
              if (parseInt(tripsDurationCost.amount) > 0) {
                getTripPaymentTransaction(
                  { trip_id: trip.trip_id },
                  (error, tripPaymentTransactions) => {
                    if (error) {
                      Sentry.captureException(error, { trip })
                      callback(error, null)
                      return
                    }
                    if (tripPaymentTransactions.length !== 0) {
                      trip.total =
                        tripPaymentTransactions[0].total -
                        tripPaymentTransactions[0].deposit
                      tripObj = Object.assign(trip, tripPaymentTransactions[0])
                    }
                    tripObj.first_lock_connect = trip.first_lock_connect > 0
                    callback(errors.noError(), tripObj)
                  }
                )
              } else {
                tripObj.first_lock_connect = trip.first_lock_connect > 0
                callback(errors.noError(), tripObj)
              }
            }
          )
        }
      }
    )
  })
}

const _processUnChargeableTrip = util.promisify(
  function _processUnChargeableTrip (trip, callback) {
    getDurationCost(
      {
        type: fleetConstants.tripInfo.endTrip,
        fleet_id: trip.fleet_id,
        date_created: trip.date_created,
        date_endtrip: moment().unix(),
        trip
      },
      (error, tripsDurationCost) => {
        if (error) {
          Sentry.captureException(error, { trip })
          logger('Error: Failed to get duration and cost', trip.fleet_id)
          callback(error, null)
          return
        }
        const updateTripDetails = {
          date_endtrip: trip.date_endtrip,
          duration: tripsDurationCost.duration
        }
        const updateTripQuery = queryCreator.updateSingle(
          dbConstants.tables.trips,
          updateTripDetails,
          { trip_id: trip.trip_id }
        )
        sqlPool.makeQuery(updateTripQuery, (error) => {
          if (error) {
            Sentry.captureException(error, { trip })
            logger(
              'Error: making update-trip query with user_id: ',
              trip.user_id,
              ':',
              updateTripQuery
            )

            return callback(errors.internalServer(false), null)
          }

          const tripPaymentTransactionsObj = {
            currency: config.fleets.defaultCurrency.toUpperCase(),
            transaction_id: null,
            charge_for_duration: 0,
            penalty_fees: 0,
            deposit: 0,
            total: 0,
            over_usage_fees: 0,
            user_profile_id: null,
            card_id: null,
            date_charged: null
          }

          trip = Object.assign(trip, tripPaymentTransactionsObj)

          return callback(null, trip)
        })
      }
    )
  }
)

/**
 * Given some fleet payment settings, determine if the fleet is correctly set up
 * to accept payments. When the fleet can accept payments, the function returns
 * the fleet payment settings object, otherwise returns `null`
 *
 * NOTE: This function mutates the fleet payment settings object by adding an
 * integrations object when the fleet is set up to pay with Mercado Pago.
 *
 * @param {object} fleetPaymentSettings Fleet payment settings
 * @returns {object|null} fleetPaymentsSettings
 */
const fleetAcceptsPayments = async (fleetPaymentSettings) => {
  if (
    payments.isStripeConnect(fleetPaymentSettings) &&
    fleetPaymentSettings.stripe_account_id
  ) {
    return fleetPaymentSettings
  } else if (payments.isMercadoPagoConnect(fleetPaymentSettings)) {
    const integration = await integrationsHandler.retrieveIntegration({
      fleet_id: fleetPaymentSettings.fleet_id,
      integration_type: payments.determinePaymentGateway(fleetPaymentSettings)
    })

    if (!integration) {
      return null
    }

    fleetPaymentSettings.integration = integration

    return fleetPaymentSettings
  }

  return null
}

const _validateProfilePaymentGateway = (profile, fleetPaymentSettings) => {
  if (profile.payment_gateway !== fleetPaymentSettings.payment_gateway) {
    throw errors.customError(
      `User cannot pay using the specified payment method. This fleet does not accept payments via ${profile.payment_gateway}.`,
      responseCodes.Conflict,
      'Conflict'
    )
  }
}

const getPaymentProfile = ({
  userPaymentProfiles,
  fleetPaymentSettings,
  preferredProfile = {}
}) => {
  let primaryPaymentProfile = {}

  if (preferredProfile.user_payment_profile_id) {
    const paymentProfile = userPaymentProfiles.find(
      (profile) =>
        Number(profile.id) === Number(preferredProfile.user_payment_profile_id)
    )

    if (!paymentProfile) {
      throw errors.customError(
        `User does not have the specified payment profile: ${preferredProfile.user_payment_profile_id}`,
        responseCodes.BadRequest,
        'BadRequest'
      )
    }

    _validateProfilePaymentGateway(paymentProfile, fleetPaymentSettings)

    return paymentProfile
  }

  if (userPaymentProfiles.length) {
    // eslint-disable-next-line eqeqeq
    primaryPaymentProfile = userPaymentProfiles.find((cc) => cc.is_primary == 1)

    if (!primaryPaymentProfile && userPaymentProfiles.length === 1) {
      // When the user has just a single card, assume that's the primary source
      primaryPaymentProfile = userPaymentProfiles[0]
    }

    // TODO: (waiyaki 27/04/2020) - Confirm if a user can have multiple
    // CCs without one set as the primary one.
    if (!(primaryPaymentProfile || {}).card_id) {
      throw errors.customError(
        'This user does not have a primary payment source.',
        responseCodes.Conflict,
        'Conflict'
      )
    }

    _validateProfilePaymentGateway(primaryPaymentProfile, fleetPaymentSettings)
  }

  return primaryPaymentProfile
}

function validateStripePayment (payment) {
  const valid = (
    payment &&
    (payment.status === 'processing' || payment.status === 'succeeded')
  )

  if (valid) {
    return { valid }
  }

  if (payment && payment.last_payment_error) {
    const lastError = payment.last_payment_error

    if (lastError.code === 'authentication_required') {
      const error = errors.customError(
        lastError.message,
        402,
        'PaymentFailure'
      )

      error.data = {
        error_code: 'authentication_required',
        payment_intent: {
          id: payment.id,
          client_secret: payment.client_secret,
          message: lastError.message
        }
      }
      Sentry.captureException(error, { payment })

      return { valid, error }
    }
  }

  return {
    valid,
    error: errors.customError(
      'Invalid payment provided',
      responseCodes.BadRequest,
      'BadRequest'
    )
  }
}

const performTripCharge = async (trip, fleetPaymentSettings, { reservation, requestParams = {} } = {}) => {
  return new Promise(async (resolve, reject) => {
    try {
      let charge = { id: null }

      const canCharge =
        fleetPaymentSettings.type ===
          fleetConstants.fleet_type.privateWithPayment ||
        fleetPaymentSettings.type ===
          fleetConstants.fleet_type.publicWithPayment

      if (!canCharge) {
        logger('endtrip:: processing unchargeable trip', {
          fleetPaymentSettingsType: fleetPaymentSettings.type,
          trip: trip.trip_id,
          user: trip.user_id,
          fleet: trip.fleet_id
        })

        const unchargedTrip = await _processUnChargeableTrip(trip)

        return resolve({
          charge,
          chargeable: canCharge,
          trip: unchargedTrip,
          error: null
        })
      }

      if (!fleetAcceptsPayments(fleetPaymentSettings)) {
        logger(
          `Error: Fleet ${fleetPaymentSettings.fleet_id} is not configured to accept payments.`
        )

        return resolve({
          trip,
          charge,
          error: errors.customError(
            'Fleet is not configured to accept payments.',
            responseCodes.BadRequest,
            'MissingPaymentGateway'
          )
        })
      }

      getTripPaymentTransaction(
        { trip_id: trip.trip_id },
        (error, [paymentTransaction]) => {
          if (error) {
            Sentry.captureException(error, { trip })
            return { trip, charge, error }
          }

          if (
            paymentTransaction &&
            paymentTransaction.status !== payments.STATUS.pending
          ) {
            trip.total =
              paymentTransaction.total -
              paymentTransaction.deposit

            return resolve({ trip, charge })
          }

          paymentHandler.getUserPaymentProfiles(
            { user_id: trip.user_id },
            (error, userCreditCards) => {
              if (error) {
                Sentry.captureException(error, { trip })
                logger(
                  'Error: Failed to end trip. Get user payment profile failed',
                  trip.user_id
                )
                return resolve({ trip, charge, error })
              }

              let dateCreated = trip.date_created
              let dateEndTrip = moment().unix()

              if (reservation) {
                dateCreated = moment.utc(reservation.reservation_start).unix()
                dateEndTrip = moment.utc(reservation.reservation_end).unix()
              }

              getDurationCost(
                {
                  type: fleetConstants.tripInfo.endTrip,
                  fleet_id: trip.fleet_id,
                  date_created: dateCreated,
                  date_endtrip: dateEndTrip,
                  trip
                },
                async (error, tripsDurationCost) => {
                  if (error) {
                    Sentry.captureException(error, { trip })
                    logger(
                      'Error: Failed to get duration and cost',
                      trip.fleet_id
                    )
                    return resolve({ trip, charge, error })
                  }

                  const tripDuration = tripsDurationCost.duration
                    ? parseFloat(tripsDurationCost.duration)
                    : 0
                  const chargeForDuration = tripsDurationCost.amount
                    ? parseFloat(tripsDurationCost.amount)
                    : 0.0

                  trip.parking_fees = trip.parking_fees || {}

                  trip.duration = Number.isFinite(tripDuration)
                    ? tripDuration
                    : 0.0
                  trip.charge_for_duration = Number.isFinite(chargeForDuration)
                    ? chargeForDuration
                    : 0.0

                  trip.deposit =
                    fleetPaymentSettings.ride_deposit === 'Yes'
                      ? fleetPaymentSettings.price_for_ride_deposit
                      : 0.0
                  trip.over_usage_fees = Number.isFinite(
                    tripsDurationCost.excess_fees
                  )
                    ? tripsDurationCost.excess_fees
                    : 0.0
                  trip.bike_unlock_fee = tripsDurationCost.bike_unlock_fee || 0
                  const {
                    subscription,
                    discount: membershipDiscount,
                    discountedAmount
                  } = await subscriptionHelpers.applyMembershipIncentive({
                    userId: trip.user_id,
                    fleetId: fleetPaymentSettings.fleet_id,
                    amount:
                      trip.charge_for_duration +
                      trip.over_usage_fees +
                      trip.bike_unlock_fee
                  })

                  const {
                    promotion,
                    discount: promoCodeDiscount,
                    discountedAmount: discountedDurationCharge
                  } = await applyPromotionDiscount({
                    userId: trip.user_id,
                    fleetId: trip.fleet_id,
                    amount: discountedAmount
                  })

                  const taxInfo = await subscriptionHelpers.calculateTaxes(trip.fleet_id, discountedDurationCharge)
                  if (taxInfo) {
                    trip.taxes = JSON.stringify(taxInfo.taxes)
                    trip.tax_sub_total = taxInfo.taxTotal
                  }

                  trip.penalty_fees = Number.isFinite(trip.parking_fees.fee)
                    ? trip.parking_fees.fee
                    : 0.0

                  trip.total = discountedDurationCharge + trip.penalty_fees + (trip.tax_sub_total || 0)
                  trip.membership_discount = membershipDiscount

                  if (promotion) {
                    trip.promo_code_discount = promoCodeDiscount
                  }

                  const stripeChargeAmount = conversions.roundOffPricing(
                    trip.total + trip.deposit
                  )

                  const shouldCharge = stripeChargeAmount > 0 && !requestParams.charge_id

                  if (!shouldCharge) {
                    logger(`endtrip:: will not charge, small charge amount`, {
                      trip_id: trip.trip_id,
                      fleet: trip.fleet_id,
                      user: trip.user_id,
                      charge_id: requestParams.charge_id,
                      stripeChargeAmount,
                      tripsDurationCost,
                      tripDuration,
                      chargeForDuration,
                      membershipDiscount,
                      promoCodeDiscount,
                      discountedAmount,
                      discountedDurationCharge,
                      taxes: (taxInfo && taxInfo.taxes) ? JSON.stringify(taxInfo.taxes) : null,
                      tax_sub_total: (taxInfo && taxInfo.taxTotal) || null
                    })
                  }

                  if (shouldCharge && userCreditCards.length === 0) {
                    logger(
                      'Error: Failed to end trip. User',
                      trip.user_id,
                      "doesn't have a credit card"
                    )

                    return resolve({
                      trip,
                      charge,
                      error: errors.stripePayment(false)
                    })
                  }

                  let paymentProfile

                  try {
                    paymentProfile = getPaymentProfile({
                      fleetPaymentSettings: fleetPaymentSettings,
                      userPaymentProfiles: userCreditCards,
                      preferredProfile: {
                        user_payment_profile_id:
                        requestParams.user_payment_profile_id
                      }
                    })
                  } catch (error) {
                    Sentry.captureException(error, { trip })
                    return resolve({ trip, charge, error })
                  }

                  const shouldCapture = paymentTransaction &&
                  paymentTransaction.status === payments.STATUS.pending

                  if (shouldCharge) {
                    try {
                      if (shouldCapture) {
                        charge = await payments.capture({
                          amount: stripeChargeAmount,
                          paymentIntentId: paymentTransaction.transaction_id,
                          fleetPaymentSettings
                        })
                      } else {
                        charge = await paymentHandler.chargeForTrip(
                          {
                            amount: stripeChargeAmount,
                            paymentProfile,
                            fleetPaymentSettings
                          },
                          { cardToken: requestParams.card_token }
                        )
                      }

                      trip.transaction_id = charge.id
                    } catch (error) {
                      Sentry.captureException(error, { trip })
                      logger('Failed to charge: ', errors.errorWithMessage(error))

                      const e = errors.customError(
                        `Payment error: ${error.message}`,
                        typeof error.code === 'string'
                          ? responseCodes.Conflict
                          : error.code,
                        'PaymentFailure'
                      )

                      e.data = error.data

                      return resolve({ trip, charge, error: e })
                    }
                  } else if (requestParams.charge_id) {
                    const payment = await payments.gateways.stripe.retrieve(
                      requestParams.charge_id,
                      {
                        connectedAccountId:
                          fleetPaymentSettings.stripe_account_id
                      }
                    )

                    const validateCharge = validateStripePayment(payment)

                    if (!validateCharge.valid) {
                      return resolve({
                        trip,
                        charge,
                        error: validateCharge.error
                      })
                    }

                    trip.transaction_id = payment.id
                  }

                  if (!shouldCharge && shouldCapture) {
                    charge = await payments.gateways.stripe
                      .cancel({
                        paymentIntentId: paymentTransaction.transaction_id,
                        fleetPaymentSettings
                      })
                      .catch((e) => {
                        Sentry.captureException(e, { trip })
                        logger(
                          'Error cancelling pre-authorized amount',
                          errors.errorWithMessage(e)
                        )

                        return { id: null }
                      })
                  }

                  if (paymentProfile.card_id) {
                    trip.user_profile_id = paymentProfile.stripe_net_profile_id
                    trip.card_id = paymentProfile.card_id
                  }

                  const columnsAndValues = Object.assign(
                    { transaction_id: charge.id },
                    _.pick(
                      trip,
                      'trip_id',
                      'fleet_id',
                      'charge_for_duration',
                      'membership_discount',
                      'user_profile_id',
                      'card_id',
                      'penalty_fees',
                      'deposit',
                      'over_usage_fees',
                      'bike_unlock_fee',
                      'promo_code_discount',
                      'taxes',
                      'tax_sub_total'
                    ),
                    {
                      currency: (charge.currency && charge.currency.toUpperCase()) || (tripsDurationCost.currency && tripsDurationCost.currency.toUpperCase()),
                      total: stripeChargeAmount,
                      date_charged: moment().unix(),
                      status: payments.STATUS.succeeded,
                      gateway_status: charge.status,
                      ...(shouldCapture
                        ? {
                          amount_captured: charge.amount_received / 100,
                          preauth_amount: charge.amount / 100
                        }
                        : { amount_captured: stripeChargeAmount })
                    }
                  )

                  if (reservation) {
                    columnsAndValues.reservation_id = reservation.reservation_id
                  }

                  if (promotion) {
                    await db
                      .main('promotion_users')
                      .where({
                        promotion_id: promotion.promotion_id,
                        user_id: trip.user_id
                      })
                      .update({ claimed_at: new Date() })

                    if (promotion.usage === 'multiple_unlimited' && !promotion.deactivated_at) {
                      // For unlimited promo codes, create a redemption clone of the just applied
                      // promo code so that the new redemption record may be used next time.
                      await db.main('promotion_users').insert({
                        promotion_id: promotion.promotion_id,
                        user_id: trip.user_id
                      })
                    }

                    columnsAndValues.promotion_id = promotion.promotion_id
                  }

                  if (subscription) {
                    columnsAndValues.membership_subscription_id = subscription.membership_subscription_id
                  }

                  try {
                    if (shouldCapture) {
                      await db
                        .main('trip_payment_transactions')
                        .where({ id: paymentTransaction.id })
                        .update(columnsAndValues)
                    } else {
                      await db
                        .main('trip_payment_transactions')
                        .insert(columnsAndValues)
                    }
                    return resolve({ trip, charge, tripsDurationCost })
                  } catch (error) {
                    Sentry.captureException(error, { trip })
                    logger(
                      'Error: inserting payment transaction query with',
                      columnsAndValues,
                      errors.errorWithMessage(error)
                    )

                    return resolve({
                      trip,
                      charge,
                      error: new Error('Error inserting payment transaction')
                    })
                  }
                }
              )
            }
          )
        }
      )
    } catch (error) {
      Sentry.captureException(error, { trip })
      return reject(error)
    }
  })
}

/**
 * To charge fees for the trip
 *
 * @param {Object} unchargedTrip - trip data
 * @param {Object} fleetDetails - fleet data
 * @param {Function} callback
 * @private
 */
const _chargeForTrip = util.promisify(
  function _chargeForTrip ({ unchargedTrip, fleetPaymentSettings, requestParams }, callback) {
    performTripCharge(unchargedTrip, fleetPaymentSettings, { requestParams })
      .then(({ trip, charge, tripsDurationCost = {}, error }) => {
        if (error) {
          Sentry.captureException(error, { trip })
          return callback(error)
        }

        const updateTripDetails = {
          date_endtrip: trip.date_endtrip,
          duration: tripsDurationCost.duration ? parseFloat(tripsDurationCost.duration) : 0
        }

        _updateTrips(updateTripDetails, { trip_id: trip.trip_id }, (error) => {
          if (error) {
            Sentry.captureException(error, { trip })
            logger('Error: making update-trip query with user_id: ', trip.user_id, ' with error:', error)
            return callback(errors.internalServer(false), null)
          }

          // Only attempt the refund logic below when the trip was
          // actually charged.
          if (!charge.id) {
            return callback(null, trip)
          }

          const comTripColumnAndValues = {
            user_id: trip.user_id
          }
          const queryTrip = queryCreator.selectWithAnd(dbConstants.tables.trips, null, comTripColumnAndValues)
          sqlPool.makeQuery(queryTrip, async (error, tripsrec) => {
            if (error) {
              Sentry.captureException(error, { trip })
              logger('Error: making trips query with ', comTripColumnAndValues, ' : ', queryTrip)
              return callback(errors.internalServer(false), null)
            }

            if (fleetPaymentSettings.ride_deposit !== fleetConstants.tableParams.yes) {
              return callback(null, trip)
            }

            if (fleetPaymentSettings.price_for_ride_deposit_type === fleetConstants.tableParams.onetime) {
              if (tripsrec.length > 0) {
                return callback(null, trip)
              }

              const userRefund = {
                user_id: trip.user_id,
                fleet_id: trip.fleet_id,
                trip_id: trip.trip_id,
                deposit_amount: fleetPaymentSettings.price_for_ride_deposit,
                currency: fleetPaymentSettings.currency,
                charge_id: charge.id,
                date_charged: moment().unix(),
                date_refunded: 0
              }

              try {
                await db.main(dbConstants.tables.user_refunded).insert(userRefund)
                return callback(null, trip)
              } catch (error) {
                Sentry.captureException(error, { trip })
                logger('Error: making user funded query with ', _.keys(userRefund))
                return callback(errors.internalServer(false), null)
              }
            } else {
              const userRefund = {
                user_id: trip.user_id,
                fleet_id: trip.fleet_id,
                trip_id: trip.trip_id,
                deposit_amount: fleetPaymentSettings.price_for_ride_deposit,
                currency: fleetPaymentSettings.currency,
                charge_id: charge.id,
                date_charged: moment().unix(),
                date_refunded: 0
              }

              try {
                await db.main(dbConstants.tables.user_refunded).insert(userRefund)
                return callback(null, trip)
              } catch (error) {
                Sentry.captureException(error, { trip })
                logger('Error: making user funded query with ', _.keys(userRefund))
                callback(errors.internalServer(false), null)
              }
            }
          })
        })
      })
      .catch(callback)
  }
)

const getDurationCost = (requestParam, callback) => {
  const initialCost = 0.0
  const currentUnixTimeStamp = moment().unix()

  let timePassed, totalMins, duration

  if (requestParam.type === fleetConstants.tripInfo.updateTrip) {
    timePassed = (currentUnixTimeStamp - requestParam.date_created) / 60
  } else {
    timePassed = (requestParam.date_endtrip - requestParam.date_created) / 60
  }

  duration = Math.max(
    (requestParam.date_endtrip || currentUnixTimeStamp) - requestParam.date_created,
    0
  )

  fleetHandler.getFleets({ fleet_id: requestParam.fleet_id }, async (error, [fleetDetails]) => {
    if (error) {
      Sentry.captureException(error, { fleet_id: requestParam.fleet_id })
      logger('Error: get trip duration cost. Failed to get fleet', requestParam.fleet_id)
      callback(errors.internalServer(false), null)
      return
    }

    if (!fleetDetails) {
      logger('Error: Failed to find fleet', requestParam.fleet_id)
      callback(errors.internalServer(false), null)
      return
    }

    const trip = requestParam.trip || {}
    const bikeUnlockFee = Number(fleetDetails.price_for_bike_unlock)

    let finalAmount = initialCost
    let excessFees = initialCost

    totalMins = timePassed

    if (trip.pricing_option_id) {
      try {
        const payload = await pricing.handlers.getDurationCost({
          pricingOptionId: trip.pricing_option_id,
          start: requestParam.date_created,
          end: requestParam.date_endtrip,
          duration
        })

        if (Number.isFinite(bikeUnlockFee)) {
          payload.bike_unlock_fee = bikeUnlockFee
        }

        payload.excess_fees = 0 // Pricing options re-up, no excess fees.

        return callback(null, payload)
      } catch (error) {
        Sentry.captureException(error, {
          pricingOptionId: trip.pricing_option_id
        })
        return callback(error)
      }
    }

    if (fleetDetails.price_type === fleetConstants.tableParams.hours) {
      totalMins = totalMins / tripConstants.totalMins.hour
    } else if (fleetDetails.price_type === fleetConstants.tableParams.days) {
      totalMins = totalMins / tripConstants.totalMins.day
    } else if (fleetDetails.price_type === fleetConstants.tableParams.weeks) {
      totalMins = totalMins / tripConstants.totalMins.week
    } else if (
      fleetDetails.price_type === fleetConstants.tableParams.months
    ) {
      totalMins = totalMins / tripConstants.totalMins.month
    }

    finalAmount = fleetDetails.price_for_membership !== '' && fleetDetails.price_type_value !== ''
      ? conversions.roundOffPricing(
        Number(Math.ceil(totalMins / fleetDetails.price_type_value)) *
              fleetDetails.price_for_membership
      )
      : 0

    if (fleetDetails.usage_surcharge === fleetConstants.tableParams.yes) {
      let timingAfter = tripConstants.totalMins.min

      if (fleetDetails.excess_usage_type_after_type === fleetConstants.tableParams.hours) {
        timingAfter = tripConstants.totalMins.hour
      } else if (fleetDetails.excess_usage_type_after_type === fleetConstants.tableParams.days) {
        timingAfter = tripConstants.totalMins.day
      } else if (fleetDetails.excess_usage_type_after_type === fleetConstants.tableParams.weeks) {
        timingAfter = tripConstants.totalMins.week
      } else if (fleetDetails.excess_usage_type_after_type === fleetConstants.tableParams.months) {
        timingAfter = tripConstants.totalMins.month
      }

      const excessFeesAfterMins = fleetDetails.excess_usage_type_after_value * timingAfter
      let timing = tripConstants.totalMins.min

      if (fleetDetails.excess_usage_type === fleetConstants.tableParams.hours) {
        timing = tripConstants.totalMins.hour
      } else if (fleetDetails.excess_usage_type === fleetConstants.tableParams.days) {
        timing = tripConstants.totalMins.day
      } else if (fleetDetails.excess_usage_type === fleetConstants.tableParams.weeks) {
        timing = tripConstants.totalMins.week
      } else if (fleetDetails.excess_usage_type === fleetConstants.tableParams.months) {
        timing = tripConstants.totalMins.month
      }

      const excessFeesTypeMins = fleetDetails.excess_usage_type_value * timing

      if (timePassed > excessFeesAfterMins) {
        const excessUsage = Number(
          Math.ceil((timePassed - excessFeesAfterMins) / excessFeesTypeMins)
        )
        excessFees = conversions.roundOffPricing(
          Number.isFinite(excessUsage)
            ? excessUsage * fleetDetails.excess_usage_fees
            : fleetDetails.excess_usage_fees
        )
      }
    }

    const currency = paymentHandler.getFleetCurrency(fleetDetails)

    const amount = Number.isFinite(finalAmount) ? finalAmount : initialCost
    const payload = {
      currency: currency,
      duration: parseFloat(duration),
      amount,
      excess_fees: Number.isFinite(excessFees) ? excessFees : 0
    }

    if (Number.isFinite(bikeUnlockFee)) {
      payload.bike_unlock_fee = bikeUnlockFee
    }

    return callback(null, payload)
  })
}

/**
 * To update trip with trip details
 *
 * @param {Object} tripDetails - Contains user and lock identifiers
 * @param {Function} callback
 */
const updateTrip = (trx, tripDetails, callback) => {
  getTrips({ trip_id: tripDetails.trip_id }, true, async (error, trips) => {
    if (error) {
      Sentry.captureException(error, { trip_id: tripDetails.trip_id })
      logger('Error:', tripDetails.trip_id, JSON.stringify(error))
      callback(errors.internalServer(false), null)
      return
    }
    if (trips.length === 0) {
      logger(
        'Error: While updating trip. No trip found with trip_id:',
        tripDetails.trip_id
      )
      callback(errors.resourceNotFound(false), null)
      return
    }
    const trip = trips[0]
    if (!trip || trip.date_endtrip) {
      logger('Error:', tripDetails.trip_id, 'Trip already ended')
      return callback(errors.customError(`Trip ${tripDetails.trip_id} has already ended`, responseCodes.ResourceNotFound, 'TripAlreadyEnded', true), null)
    }
    getDurationCost(
      {
        type: fleetConstants.tripInfo.updateTrip,
        fleet_id: trip.fleet_id,
        date_created: trip.date_created,
        date_endtrip: trip.date_endtrip,
        trip
      },
      async (error, tripsDurationCost) => {
        if (error) {
          Sentry.captureException(error, { trip_id: tripDetails.trip_id })
          logger('Error: Getting trip duration cost for:', tripDetails.trip_id)
          callback(errors.internalServer(false), null)
          return
        }
        if (trip.bike_id) {
          const steps = trip.steps
            ? Array.isArray(trip.steps)
              ? trip.steps
              : JSON.parse(trip.steps)
            : []
          let lastLockedLocation
          let lastUnlockedLocation
          const now = moment().unix()
          if (tripDetails.steps) {
            const stepsLen = tripDetails.steps.length
            for (let i = 0; i < stepsLen; i++) {
              if (tripDetails.steps[i].length > 3) {
                if (tripDetails.steps[i][3] === 1) {
                  lastLockedLocation = tripDetails.steps[i]
                } else if (tripDetails.steps[i][3] === 0) {
                  lastUnlockedLocation = tripDetails.steps[i]
                } else {
                  tripDetails.steps[i].pop()
                }
              }
            }
          } else if (tripDetails.latitude && tripDetails.longitude) {
            if (trip.date_created > now) {
              tripDetails.date_created = now
            }
            tripDetails.steps = [[tripDetails.latitude, tripDetails.longitude, now]]
            tripDetails.time_lock_connected = now
          }

          tripDetails.steps = tripDetails.steps ? JSON.stringify(steps.concat(tripDetails.steps)) : JSON.stringify(steps)

          try {
            tripDetails.steps = JSON.parse(tripDetails.steps)
            trip.steps = JSON.parse(trip.steps)
          } catch (error) {
            Sentry.captureException(error, { trip_id: tripDetails.trip_id })
            logger('Error: Parsing steps to JSON')
          }

          if (lastLockedLocation) {
            lastLockedLocation.pop()
            tripDetails = Object.assign(tripDetails, {
              last_locked_location: JSON.stringify(lastLockedLocation)
            })
          }
          if (lastUnlockedLocation) {
            lastUnlockedLocation.pop()
            tripDetails = Object.assign(tripDetails, {
              last_unlocked_location: JSON.stringify(lastUnlockedLocation)
            })
          }
        }

        const bikeUnlockFee = tripsDurationCost.bike_unlock_fee || 0
        const totalCharge =
          tripsDurationCost.amount +
          tripsDurationCost.excess_fees +
          bikeUnlockFee
        fleetHandler.getFleetMetaData(
          { fleet_id: trip.fleet_id },
          async (error, result) => {
            if (error) {
              Sentry.captureException(error, { tripDetails })
              logger('Error: getting fleets with fleet_id:', trip.fleet_id)
              return callback(error, null)
            }

            const chargeForDuration = Number.isFinite(totalCharge)
              ? conversions.roundOffPricing(totalCharge)
              : 0.0
            const {
              discount: membershipDiscount,
              discountedAmount
            } = await subscriptionHelpers.applyMembershipIncentive({
              userId: trip.user_id,
              fleetId: trip.fleet_id,
              amount: chargeForDuration
            })

            const {
              discount: promoCodeDiscount,
              discountedAmount: discountedDurationCharge
            } = await applyPromotionDiscount({
              userId: trip.user_id,
              fleetId: trip.fleet_id,
              amount: discountedAmount
            })

            const payload = {
              trip_id: trip.trip_id,
              duration: tripsDurationCost.duration,
              amount: chargeForDuration,
              membership_discount: membershipDiscount,
              promo_code_discount: promoCodeDiscount,
              charge_for_duration: discountedDurationCharge,
              currency: tripsDurationCost.currency && tripsDurationCost.currency.toUpperCase(),
              do_not_track_trip: result[0] && result[0].do_not_track_trip
            }

            if (trip.bike_id) payload.bike_id = trip.bike_id
            else if (trip.port_id) payload.port_id = trip.port_id
            else if (trip.hub_id) payload.hub_id = trip.hub_id

            if (trip.bike_id) { // bike battery level only applies to bikes not ports and hubs
              const bikeInfo = await bikeHandler.getVehicleInfo({bike_id: trip.bike_id})
              if (bikeInfo) payload.bike_battery_level = bikeInfo.bike_battery_level
            }

            if (trip.end_address || trip.date_endtrip) {
              trip.duration = Math.max(
                trip.date_endtrip - trip.date_created,
                0
              )

              payload.ended_trip = Object.assign(trip, {
                currency: config.fleets.defaultCurrency.toUpperCase(),
                transaction_id: null,
                charge_for_duration: 0,
                penalty_fees: 0,
                deposit: 0,
                total: 0,
                over_usage_fees: 0,
                user_profile_id: null,
                card_id: null,
                date_charged: null
              })
              if (parseInt(chargeForDuration) > 0) {
                getTripPaymentTransaction(
                  { trip_id: trip.trip_id },
                  (error, tripPaymentTransactions) => {
                    if (error) {
                      Sentry.captureException(error, { tripDetails })
                      callback(error, null)
                      return
                    }
                    if (tripPaymentTransactions.length !== 0) {
                      trip.total =
                          tripPaymentTransactions[0].total -
                          tripPaymentTransactions[0].deposit
                      payload.ended_trip = Object.assign(
                        trip,
                        tripPaymentTransactions[0]
                      )
                    }
                    callback(errors.noError(), payload)
                  }
                )
              } else {
                callback(errors.noError(), payload)
              }
            } else {
              const fieldsToUpdate = _.pick(
                tripDetails,
                'start_address',
                'end_address',
                'date_created',
                'steps',
                'first_lock_connect',
                'time_lock_connected',
                'parking_image',
                'last_locked_location',
                'last_unlocked_location',
                'date_endtrip',
                'device_token',
                'device_type',
                'taxes',
                'tax_sub_total'
              )
              if (trip.bike_id) fieldsToUpdate.steps = JSON.stringify(fieldsToUpdate.steps)
              else if (trip.port_id || trip.hub_id) fieldsToUpdate.steps = null // Hubs and ports don't need steps
              try {
                if (!trx) await db.main('trips').update(fieldsToUpdate).where({ trip_id: tripDetails.trip_id })
                else await trx('trips').update(fieldsToUpdate).where({ trip_id: tripDetails.trip_id })
                callback(null, payload)
              } catch (error) {
                Sentry.captureException(error, { tripDetails })
                return callback(errors.internalServer(false), null)
              }
            }
          }
        )
      }
    )
  })
}

/**
 * To get trip start and end address
 *
 * @param {Number} latitude - latitude of the location to get address
 * @param {Number} longitude - longitude of the location to get address
 * @param {Function} callback
 * @private
 */
const _getTripAddress = (latitude, longitude, callback) => {
  if (!latitude || !longitude) {
    logger(`latitude or longitude is invalid with values lat: ${latitude} and long: ${longitude} `)
    return callback(errors.noError(), null)
  }
  mapBoxHandler.getAddressInformationForCoordinate(
    {
      latitude: latitude,
      longitude: longitude
    },
    (error, address) => {
      if (error) {
        Sentry.captureException(error, { latitude, longitude })
        logger('Error: making trip-address query to MapBox')
        return callback(errors.internalServer(false), null)
      }
      const locationAddress = address
        ? (!!address.address && !!address.street)
          ? address.address + ' ' + address.street
          : ''
        : ''
      if (!(locationAddress.trim().length)) {
        googleMapsHandler.getAddressInformationForCoordinate({
          latitude: latitude,
          longitude: longitude
        }, (error, addressInfo) => {
          if (error) {
            Sentry.captureException(error, { latitude, longitude })
            logger('Error: Failed to get address from google maps API with error: ', error)
            return callback(errors.internalServer(false), null)
          }
          callback(null, addressInfo.street ? addressInfo.street : '')
        })
      } else {
        return callback(errors.noError(), locationAddress)
      }
    }
  )
}

/**
 * To update a trip rating
 *
 * @param {Object} tripDetails - Contains tripDetails to update rating
 */
const updateTripRating = async (tripDetails) => {
  try {
    await db.main('trips').update({ rating: tripDetails.rating, rating_shown: true }).where({ trip_id: tripDetails.trip_id })
    return { msg: 'Updated successfully' }
  } catch (error) {
    Sentry.captureException(error, { tripDetails })
    logger('Error: making update rating query with trip_id: ', tripDetails.trip_id, ':', error)
    throw error
  }
}

/**
 * End trip on theft report
 *
 * @param {Object} tripDetails - Contains tripDetails to update theft report
 * @param {Function} callback
 */
const endTripOnTheftReport = (tripDetails, callback) => {
  bookingHandler.updateBooking(
    null,
    {
      status: tripDetails.trip_id ? tripConstants.booking.finished : tripConstants.booking.cancelled,
      cancelled_on: moment().unix()
    },
    _.pick(tripDetails, 'booking_id'), (error) => {
      if (error) {
        Sentry.captureException(error, { tripDetails })
        logger('unable to update booking row for bike_id ', tripDetails.bike_id, ' on theft report')
        callback(errors.internalServer(false))
        return
      }
      if (tripDetails.trip_id) {
        updateTripForTheft({ trip_id: tripDetails.trip_id }, (error, trip) => {
          if (error) {
            Sentry.captureException(error, { tripDetails })
            logger('Error: making end-trip query with user id:', tripDetails.user_id)
            callback(errors.internalServer(false))
            return
          }
          callback(null)
        })
      } else {
        callback(null)
      }
    })
}
/**
 * To update trip with trip details for theft report
 *
 * @param {Object} tripDetails
 * @param {Function} callback
 */
const updateTripForTheft = (tripDetails, callback) => {
  getTrips({ trip_id: tripDetails.trip_id }, false, (error, trips) => {
    if (error) {
      Sentry.captureException(error, { tripDetails })
      logger('Error: Getting particular trip for:', tripDetails.trip_id)
      logger('Error:', JSON.stringify(error))
      callback(errors.internalServer(false))
      return
    }
    if (trips.length === 0) {
      logger('Error: Trip details not found for theft report')
      callback(errors.resourceNotFound(false))
      return
    }
    const updateTripQuery = queryCreator.updateSingle(
      dbConstants.tables.trips,
      { end_address: '', date_endtrip: moment().unix() },
      { trip_id: tripDetails.trip_id })
    sqlPool.makeQuery(updateTripQuery, (error) => {
      if (error) {
        Sentry.captureException(error, { tripDetails })
        logger('Error: making update-trip query on theft report ', updateTripQuery)
        callback(errors.internalServer(false))
        return
      }
      callback(null)
    })
  })
}

/**
 * To format trips objects
 *
 * @param {Array} trips
 * @param {Array} fleets
 * @param {Array} tripPayments - trips payment info
 */
const formatTripsWithPaymentInfo = (trips, fleets, tripPayments) => {
  const tripsLen = trips.length
  let trip
  let fleet
  let tripPayment
  const now = moment().unix()

  const defaultTripPaymentTransaction = {
    currency: config.fleets.defaultCurrency.toUpperCase(),
    transaction_id: null,
    charge_for_duration: 0,
    penalty_fees: 0,
    deposit: 0,
    total: 0,
    over_usage_fees: 0,
    user_profile_id: null,
    card_id: null,
    date_charged: null,
    type_card: null,
    cc_no: null,
    cc_type: null
  }

  for (let i = 0; i < tripsLen; i++) {
    trip = trips[i]
    trip.duration = trip.date_endtrip ? Math.max((trip.date_endtrip - trip.date_created), 0) : Math.max((now - trip.date_created), 0)
    if (!trip.steps) trip.steps = [[]]
    trip.distance = tripHelper.getTripDistance(trip)
    if (fleets && fleets.length !== 0) {
      fleet = _.findWhere(fleets, { fleet_id: trip.fleet_id })
      if (fleet) {
        delete fleet.date_created
        trip = Object.assign(trip, fleet)
      }
    }
    if (tripPayments && tripPayments.length !== 0) {
      tripPayment = _.findWhere(tripPayments, { trip_id: trip.trip_id })
      if (tripPayment) {
        tripPayment = Object.assign({ currency: paymentHandler.getFleetCurrency(tripPayment) },
          _.omit(tripPayment,
            'is_primary', 'stripe_net_profile_id', 'stripe_net_payment_id', 'cvc',
            'exp_month', 'exp_year', 'fingerprint', 'created_date', 'last_updated'))
      } else {
        tripPayment = defaultTripPaymentTransaction
      }
    } else {
      tripPayment = defaultTripPaymentTransaction
    }

    trip = Object.assign(trip, tripPayment)
    trip.skip_parking_image = trip.skip_parking_image === 1
    trip.first_lock_connect = trip.first_lock_connect === 1
    // eslint-disable-next-line eqeqeq, camelcase, no-undef
    let finalTotal = trip.total_refunded ? conversions.roundOffPricing(trip.total - trip.total_refunded) : trip.total - trip.deposit
    trip.total = finalTotal
    if (!trip.steps) trip.steps = null
    else {
      trip.steps = Array.isArray(trip.steps)
        ? trip.steps
        : JSON.parse(trip.steps)
    }
    trips[i] = trip
  }
  return trips
}

/**
 * To format trips objects for wire
 *
 * @param {Array} trips
 */
const formatTripsForWire = (trips) => {
  if (!Array.isArray(trips)) {
    trips = [trips]
  }
  const tripsLen = trips.length
  const newTrips = []
  let usedTrip = false
  let trip
  for (let i = 0; i < tripsLen; i++) {
    trip = trips[i]
    // For Payments fleet: Checking if the trip is cancelled before starting
    usedTrip = trip.date_endtrip ? (trip.date_endtrip > trip.date_created) : true
    if (usedTrip) {
      newTrips.push(trip)
    }
  }
  return newTrips
}

const sendTripEmail = async (trip, appDetails = { source: null }, lang) => {
  if (!['public', 'private'].includes(trip.type)) {
    return
  }

  const fleet = await db.main('fleet_metadata').where({fleet_id: trip.fleet_id}).select('fleet_timezone').first()

  const taxData = await db.main('trip_payment_transactions').where({ trip_id: trip.trip_id }).select('taxes', 'tax_sub_total').first()
  return new Promise((resolve, reject) => {
    return userHandler.getUserWithUserId(trip.user_id, (err, user) => {
      if (err) {
        return reject(err)
      }

      const data = {
        name: user.first_name + ' ' + user.last_name,
        email: user.email,
        subject: lang && lang === 'fr' ? `Merci d’avoir sécurisé votre vélo avec la ${trip.fleet_name}!` : `Thank you for securing your bike with the ${trip.fleet_name}!`,
        fleetName: trip.fleet_name,
        fleetLogo: trip.logo,
        charges: {
          currency:
            conversions.currencyCodeToSymbol(trip.currency) ||
            conversions.currencyCodeSymbolMap.USD,
          total: trip.total,
          membership_discount: trip.membership_discount,
          promo_code_discount: trip.promo_code_discount,
          duration: trip.charge_for_duration,
          penalty_fees: trip.penalty_fees,
          over_usage_fees: trip.over_usage_fees,
          bike_unlock_fee: trip.bike_unlock_fee,
          cc_type: trip.cc_type,
          cc_no: trip.cc_no && trip.cc_no.slice(-4),
          amount_captured: trip.amount_captured,
          preauth_amount: trip.preauth_amount,
          taxes: trip.taxes,
          tax_sub_total: trip.tax_sub_total
        },
        start: {
          date: moment
            .unix(trip.date_created)
            .tz(fleet.fleet_timezone)
            .format('MMMM DD, YYYY'),
          time: moment
            .unix(trip.date_created)
            .tz(fleet.fleet_timezone)
            .format('hh:mm a'),
          place: trip.start_address
        },
        end: {
          date: moment
            .unix(trip.date_endtrip)
            .tz(fleet.fleet_timezone)
            .format('MMMM DD, YYYY'),
          time: moment
            .unix(trip.date_endtrip)
            .tz(fleet.fleet_timezone)
            .format('hh:mm a'),
          place: trip.end_address
        },
        tripType: trip && trip.bike_id ? 'bike' : 'station',
        contact: {
          email: trip.contact_email,
          phone: trip.contact_phone
        }
      }
      if (taxData && taxData.taxes) {
        data.charges.tax_sub_total = `${data.charges.currency}${Number(taxData.tax_sub_total).toFixed(2)}`
        data.charges.taxes = taxData.taxes
      }
      if (lang && lang === 'fr') {
        const email = endTripEmailObjectFrench(data)
        mailHandler.sendMail(
          data.email,
          data.subject,
          email,
          null,
          appDetails.source,
          (err) => {
            if (err) {
              throw err
            }
            resolve(true)
          }
        )
      } else {
        const email = endTripEmailObject(data)
        mailHandler.sendMail(
          data.email,
          data.subject,
          email,
          null,
          appDetails.source,
          (err) => {
            if (err) {
              throw err
            }
            resolve(true)
          }
        )
      }
    })
  })
    .then((res) =>
      logger(`Success: Ride receipt email sent for trip ID ${trip.trip_id} `)
    )
    .catch((err) => {
      Sentry.captureException(err, { trip })
      logger('Error: could not send ride receipt email', err)
    })
}

const getStandardDeviceInfo = async ({bikeId, portId, hubId, hubType, fleetId}) => {
  if (bikeId) {
    const bike = await db.main('bikes').where({ bike_id: bikeId }).leftJoin('fleets', 'fleets.fleet_id', 'bikes.fleet_id').options({ nestTables: true }).first()
    return { bike: bike.bikes, fleet: bike.fleets }
  } else if (portId) {
    const port = await db.main('ports').where({port_id: portId}).first()
    const hub = await db.main('hubs').where({ uuid: port.hub_uuid }).first()
    const hubInfo = await getHubById(hub.hub_id)
    return hubInfo
  } else if (hubId) {
    // const whereClause = { hub_id: hubId, 'fleets.fleet_id': fleetId }
    // if (hubType) whereClause.type = hubType | 'parking_station'
    // const hub = await db.main('hubs_and_fleets').leftJoin('fleets', 'fleets.fleet_id', 'hubs_and_fleets.fleet_id').options({ nestTables: true }).where(whereClause).first()
    // delete hub.hubs_and_fleets.image // TODO: Get images from s3
    // return { hub: hub.hubs_and_fleets, fleet: hub.fleets }
    const hub = await getHubById(hubId)
    return hub
  }
}

const getTripInformation = ({userId, tripId}, cb) => {
  getTripsWithPaymentInfo({ user_id: userId, trip_id: tripId }, (error, trips) => {
    if (error) {
      Sentry.captureException(error, { userId, tripId })
      logger('Error: could not get trip with id: ', tripId, 'Failed with error', error)
      return cb(error, null)
    }
    if (trips.length === 0) {
      return cb(null, { trip: null })
    }
    let doNotTrackTrip = false
    fleetHandler.getFleetMetaData({ fleet_id: trips[0].fleet_id }, async (error, fleetDetails) => {
      if (error) {
        Sentry.captureException(error, { userId, tripId })
        logger('Error: getting fleets with fleet_id:', trips[0].fleet_id)
        return cb(error, null)
      }
      let trip = trips[0]
      let result = { trip }

      if (trip.bike_id && !result.bike) {
        const bike = await getStandardDeviceInfo({ bikeId: trip.bike_id, fleetId: trip.fleet_id })
        result = {...result, bike}
      }
      if (trip.hub_id && !result.hub) {
        const hub = await getStandardDeviceInfo({ hubId: trip.hub_id, fleetId: trip.fleet_id })
        result = {...result, hub}
      }
      if (trip.port_id && !result.port) {
        const hub = await getStandardDeviceInfo({ portId: trip.port_id, fleetId: trip.fleet_id })
        result = {...result, hub}
      }
      if (trip.reservation_end) {
        const reservation = await db.main('reservations')
          .where({ reservation_id: trip.reservation_id })
          .first()
        if (reservation) {
          trip.reservation_end = momentTimezone(trip.reservation_end)
            .tz(
              reservation.reservation_timezone
            ).utc().format()
          if (trip.reservation_start) {
            trip.reservation_start = momentTimezone(trip.reservation_start)
              .tz(
                reservation.reservation_timezone
              ).utc().format()
          }
        }
      }
      if (fleetDetails.length) {
        let info = fleetDetails[0]
        doNotTrackTrip = info.do_not_track_trip && info.do_not_track_trip === 1
        if (doNotTrackTrip) {
          return getDurationCost({
            type: fleetConstants.tripInfo.updateTrip,
            fleet_id: trip.fleet_id,
            date_created: trip.date_created,
            date_endtrip: trip.date_endtrip,
            trip
          }, async (error, cost) => {
            if (error) {
              Sentry.captureException(error, { userId, tripId })
              logger('Error: getting cost with trip_id:', trip.trip_id)
              return cb(error, null)
            }
            trip.do_not_track_trip = Boolean(doNotTrackTrip)
            trip.total = cost.amount
            result.trip = trip
            return cb(null, { ...result })
          })
        }
      }
      return cb(null, result)
    })
  })
}

module.exports = Object.assign(module.exports, {
  getDurationCost: getDurationCost,
  getTrips: getTrips,
  getTripsWithPaymentInfo: getTripsWithPaymentInfo,
  startTrip: startTrip,
  startReservationTrip,
  updateTrip: updateTrip,
  endTrip: endTrip,
  endTripOnTheftReport: endTripOnTheftReport,
  updateTripRating: updateTripRating,
  getTripPaymentTransaction: getTripPaymentTransaction,
  formatTripsForWire: formatTripsForWire,
  sendTripEmail: sendTripEmail,
  performTripCharge,
  fleetAcceptsPayments,
  startTripV2,
  getTripInformation,
  getStandardDeviceInfo,
  isUserOnActiveTrip,
  endTripWithCancelledOrElapsedReservation
})
