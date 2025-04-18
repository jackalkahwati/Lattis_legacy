'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const sqlPool = platform.sqlPool
const query = platform.queryCreator
const errors = platform.errors
const responseCodes = platform.responseCodes
const bikeHandler = require('./bike-handler')
const operatorHandler = require('./operator-handler')
const _ = require('underscore')
const moment = require('moment')
const config = require('./../config')
const dbConstants = require('./../constants/db-constants')
const tripConstants = require('./../constants/trip-constants')
const bikeConstants = require('./../constants/bike-constants')
const fleetHandler = require('./fleet-handler')
const tripHandler = require('./trip-handler')
const fleetConstants = require('./../constants/fleet-constants')
const db = require('../db')
const payments = require('./payments')
const { GATEWAYS } = require('./payments/gateways')
const pricing = require('./pricing')
const paymentHandler = require('./payment-handler')
const conversions = require('../utils/conversions')
const { retrievePort, getHubById } = require('./hubs-handler')
const {configureSentry} = require('../utils/configureSentry')

const Sentry = configureSentry()

/** * This method updates the bikes and booking table for cancelling a trip
 *
 * @param {Integer} bookingDetails - Identifier that contains booking details
 * @param {Function} callback
 */
const cancelBooking = (bookingDetails, callback) => {
  getActiveBooking({ user_id: bookingDetails.user_id, bike_id: bookingDetails.bike_id }, (error, activeBooking) => {
    if (error) {
      Sentry.captureException(error, {bookingDetails})
      logger('Error: Failed to get booking details with', bookingDetails.user_id, ': ', error)
      callback(error)
      return
    }
    if (activeBooking === null) {
      logger('the user with user_id:', bookingDetails.user_id, 'has no active booking for the bike with bike_id:', bookingDetails.bike_id)
      callback(errors.customError(`The user with id ${bookingDetails.user_id}, has no active booking for the bike with bike ${bookingDetails.bike_id}`, responseCodes.ResourceNotFound, 'BookingNotFound', true))
      return
    }
    const booking = activeBooking[0]
    const bookingColumn = {
      status: bookingDetails.lock_issue ? tripConstants.booking.cancelled_with_issue : tripConstants.booking.cancelled,
      cancelled_on: moment().unix()
    }
    // End the trip if relevant booking has trip started
    if (booking.trip_id) {
      tripHandler.endTrip(Object.assign(_.pick(bookingDetails, 'bike_damaged'), {
        trip_id: booking.trip_id,
        skip_parking_fee: true
      }), (error) => {
        if (error) {
          Sentry.captureException(error, {bookingDetails})
          logger('Error: cancel booking for user', bookingDetails.user_id, ' for the bike with bike_id:',
            bookingDetails.bike_id, 'Failed to end the trip', error)
          callback(error)
          return
        }
        updateBooking(null, bookingColumn, { booking_id: booking.booking_id }, callback)
      })
    } else {
      updateBooking(null, bookingColumn, { booking_id: booking.booking_id }, callback)
    }
    // })
  })
}

/** * This method updates the bikes and booking table for cancelling a trip
 *
 * @param {Integer} bookingDetails - Identifier that contains booking details
 * @param {Function} callback
 */
const cancelBookingV2 = (bookingDetails, bookingId, callback) => {
  let deviceId
  if (bookingDetails.bike_id) {
    deviceId = 'bike_id'
  } else if (bookingDetails.hub_id) {
    deviceId = 'hub_id'
  } else if (bookingDetails.port_id) {
    deviceId = 'port_id'
  }
  getActiveBooking({ user_id: bookingDetails.user_id, [deviceId]: bookingDetails[deviceId] }, (error, activeBooking) => {
    if (error) {
      Sentry.captureException(error, {bookingDetails})
      logger('Error: Failed to get booking details with', bookingDetails.user_id, ': ', error)
      callback(error)
      return
    }
    if (activeBooking === null) {
      logger('the user with user_id:', bookingDetails.user_id, `has no active booking for ${deviceId}:`, bookingDetails[deviceId])
      callback(errors.customError(`The user with id ${bookingDetails.user_id}, has no active booking for ${deviceId}:, ${bookingDetails[deviceId]}`, responseCodes.ResourceNotFound, 'BookingNotFound', true))
      return
    }
    const booking = activeBooking[0]
    const bookingColumn = {
      status: bookingDetails.lock_issue ? tripConstants.booking.cancelled_with_issue : tripConstants.booking.cancelled,
      cancelled_on: moment().unix()
    }
    // End the trip if relevant booking has trip started
    if (booking.trip_id) {
      tripHandler.endTrip(Object.assign(_.pick(bookingDetails, 'bike_damaged'), {
        trip_id: booking.trip_id,
        skip_parking_fee: true
      }), (error) => {
        if (error) {
          Sentry.captureException(error, {bookingDetails})
          logger('Error: cancel booking for user', bookingDetails.user_id, ` for the bike with ${deviceId}:`,
            bookingDetails[deviceId], 'Failed to end the trip', error)
          callback(error)
          return
        }
        updateBooking(null, bookingColumn, { booking_id: booking.booking_id }, callback)
      })
    } else {
      updateBooking(null, bookingColumn, { booking_id: booking.booking_id }, callback)
    }
  })
}

// Check active bookings
const checkActiveBooking = (bookingInfo, filterId, deviceInfo, isPaymentFleet, callback) => {
  // lets check if the user has already booked a bike
  getActiveBooking(_.pick(bookingInfo, 'user_id'), (error, bookedUser) => {
    if (error) {
      Sentry.captureException(error, {bookingInfo})
      logger('Error: Failed to get booking details with', bookingInfo.user_id, ': ', error)
      callback(error, null)
      return
    }
    if (bookedUser !== null) {
      logger('the user with id', bookingInfo.user_id, ' has an active booking')
      callback(errors.customError('The user has an active booking', responseCodes.MethodNotAllowed, 'ActiveBookingError', true), bookedUser[0])
      return
    }

    let bookingFor
    if (filterId === 'bike_id') bookingFor = 'bike'
    else if (filterId === 'hub_id') bookingFor = 'hub'
    else if (filterId === 'port_id') bookingFor = 'port'

    // lets check if the the bike/port/hub is booked already
    getActiveBooking(_.pick(bookingInfo, filterId), (error, bookingDetails) => {
      if (error) {
        Sentry.captureException(error, {bookingInfo})
        logger('Error: Failed to get booking details with', bookingInfo[filterId], ': ', error)
        callback(error, null)
        return
      }
      if (bookingDetails !== null) {
        logger(`The ${bookingFor} with ${filterId}:`, bookingInfo[filterId], ' is already booked')
        return callback(errors.unauthorizedAccess(false), null)
      } else {
        const columnsAndValues = {
          booked_on: moment().unix(),
          user_id: bookingInfo.user_id,
          lock_id: bookingInfo.lock_id || null,
          [filterId]: deviceInfo[filterId],
          status: tripConstants.booking.active,
          operator_id: bookingInfo.operator_id,
          customer_id: bookingInfo.customer_id,
          fleet_id: deviceInfo.fleet_id,
          device_type: bookingFor
        }
        fleetHandler.getFleetsWithPaymentAndMetadata({ fleet_id: [deviceInfo.fleet_id] }, async (error, fleets) => {
          if (error) {
            Sentry.captureException(error, {bookingInfo})
            logger('Error: Failed to create booking for', deviceInfo[filterId],
              'Failed to get fleetsWithMetadata:', error)
            callback(error, null)
            return
          }

          if (bookingInfo.pricing_option_id) {
            try {
              const pricingOption = await pricing.handlers.retrieve({
                id: bookingInfo.pricing_option_id,
                fleetId: deviceInfo.fleet_id
              })

              if (!pricingOption) {
                throw errors.customError(
                  `Invalid pricing option: ${bookingInfo.pricing_option_id}`,
                  responseCodes.BadRequest,
                  'BadRequest'
                )
              }
            } catch (error) {
              Sentry.captureException(error, {bookingInfo})
              return callback(error)
            }
          }

          let preAuthPayment
          let fleetInfo = fleets[0]

          if (isPaymentFleet) {
            try {
              preAuthPayment = await preAuthorize({
                userId: bookingInfo.user_id,
                fleetPaymentSettings: fleetInfo
              })
            } catch (error) {
              Sentry.captureException(error, {bookingInfo})
              return callback(error)
            }
          }

          _insertBookingQuery(columnsAndValues, async (error, res) => {
            if (error) {
              Sentry.captureException(error, {columnsAndValues})
              logger('Error: failed to save booking data. Failed with error:', error)
              callback(error, null)
            }
            const insertedBookingId = res.insertId
            const booking = await db.main('booking').where({booking_id: insertedBookingId}).select('booking_id', 'bike_id', 'port_id', 'hub_id', 'device_type').first() || {}
            let bookingExpiration = config.bikeBookingExpiration
            if ((fleets.length !== 0) && fleets[0].bike_booking_interval) {
              bookingExpiration = fleets[0].bike_booking_interval
            }
            const payload = {
              booked_on: columnsAndValues.booked_on,
              expires_in: bookingExpiration,
              support_phone: config.lattisSupportPhoneNumber,
              on_call_operator: null,
              ...booking
            }
            // To send support phone number for help section
            operatorHandler.getOnCallOperator(bookingInfo.fleet_id, (error, onCallOperator) => {
              if (error) {
                Sentry.captureException(error, {bookingInfo})
                callback(errors.internalServer(false), null)
                return
              }
              if (onCallOperator) {
                payload.on_call_operator = onCallOperator.phone_number
                  ? String(onCallOperator.phone_number)
                  : null
              }
              if (isPaymentFleet) {
                tripHandler.startTripV2({
                  [filterId]: bookingInfo[filterId],
                  user_id: bookingInfo.user_id,
                  phone_number: bookingInfo.phone_number,
                  email: bookingInfo.email,
                  trip_start_time: payload.booked_on + payload.expires_in,
                  is_payment_fleet: isPaymentFleet,
                  device_token: bookingInfo.device_token || null,
                  pricing_option_id: bookingInfo.pricing_option_id,
                  device_type: bookingFor
                }, async (error, trip) => {
                  if (error) {
                    Sentry.captureException(error, {bookingInfo})
                    return callback(error, null)
                  }

                  if (preAuthPayment) {
                    await insertPreAuthPayment({
                      trip,
                      payment: preAuthPayment,
                      fleetPaymentSettings: fleetInfo
                    })
                  }

                  callback(null, Object.assign(payload, {
                    trip_id: trip.trip_id,
                    do_not_track_trip: trip.do_not_track_trip
                  }))
                })
              } else {
                callback(null, payload)
              }
            })
          })
        }
        )
      }
    }
    )
  })
}

/** * This method creates a booking record in the booking table
 *
 * @param {Integer} bookingInfo - Identifier for the booking details to get created
 * @param {Function} callback
 */
const createBooking = (bookingInfo, callback) => {
  bikeHandler.getBikesWithLock({ bike_id: bookingInfo.bike_id }, async (error, [bookedBike]) => {
    if (error || bookedBike === null) {
      Sentry.captureException(error, {bookingInfo})
      logger('Error: Failed to get bike details with', bookingInfo.bike_id, ': ', error)
      return callback(error, null)
    }

    /*
    const activeTrip = await tripHandler.isUserOnActiveTrip(bookingInfo.user_id)
    if (activeTrip) {
      try {
        await tripHandler.endTripWithCancelledOrElapsedReservation(activeTrip)
      } catch (error) {
        return callback(errors.customError(error.message || `You already have a scheduled reservation with the vehicle`, responseCodes.Conflict, 'ErrorCancellingScheduledReservation', true), null)
      }
    }
    */
    const isPaymentFleet = (bookedBike.fleet_type === fleetConstants.fleet_type.privateWithPayment ||
      bookedBike.fleet_type === fleetConstants.fleet_type.publicWithPayment)

    // lets check if private & public fleet type users connect with stripe account or not
    if (isPaymentFleet) {
      const acceptsPayments = await tripHandler.fleetAcceptsPayments(bookedBike)

      if (!acceptsPayments) {
        logger(
          `Error: Fleet ${bookedBike.fleet_id} is not configured to accept payments.`
        )

        return callback(
          errors.customError(
            'Fleet is not configured to accept payments.',
            responseCodes.Conflict,
            'MissingPaymentGateway'
          ),
          null
        )
      }
    }

    // lets check if the bike is available to book
    if (![bikeConstants.current_status.parked, bikeConstants.current_status.collect].includes(bookedBike.current_status)) {
      logger('The bike with bike_id:', bookingInfo.bike_id, ' is not available')
      callback(errors.customError(`The bike with bike_id:, ${bookingInfo.bike_id}, is not available`, responseCodes.MethodNotAllowed, 'BikeNotAvailable'), null)
      return
    }

    if (error) {
      Sentry.captureException(error, {bookedBike})
      logger('Error: getting fleets with fleet_id:', bookedBike.fleet_id)
      return callback(error, null)
    }

    if (bookedBike.require_phone_number) {
      if (!bookingInfo.phone_number) {
        logger('Info: The user does not have a phone number which is required for this fleet')
        return callback(errors.customError('Phone number required for this fleet', responseCodes.Conflict, 'Phone number required', true), null)
      }
    }

    if (payments.isMercadoPagoConnect(bookedBike)) {
      const userPaymentProfile = await db
        .main('user_payment_profiles')
        .where({
          user_id: bookingInfo.user_id,
          payment_gateway: GATEWAYS.mercadopago,
          source_fleet_id: bookedBike.fleet_id
        })
        .first()

      if (!userPaymentProfile) {
        return callback(
          errors.customError(
            'This user does not have a payment method that can pay into this fleet',
            responseCodes.Conflict,
            'Conflict'
          )
        )
      }
    }

    _updateBikeLocation(bookingInfo, (error) => {
      if (error) {
        Sentry.captureException(error, {bookingInfo})
        return callback(error, null)
      }

      // lets check if the user has already booked a bike
      getActiveBooking(_.pick(bookingInfo, 'user_id'), (error, bookedUser) => {
        if (error) {
          Sentry.captureException(error, {bookingInfo})
          logger('Error: Failed to get booking details with', bookingInfo.user_id, ': ', error)
          callback(error, null)
          return
        }
        if (bookedUser !== null) {
          logger('the user with user_id:', bookingInfo.user_id, ' has an active booking')
          callback(errors.customError('The user has an active booking', responseCodes.MethodNotAllowed, 'ActiveBookingError', true), bookedUser)
          return
        }

        // lets check if the the bike is booked already
        getActiveBooking(_.pick(bookingInfo, 'bike_id'), (error, bookingDetails) => {
          if (error) {
            Sentry.captureException(error, {bookingInfo})
            logger('Error: Failed to get booking details with', bookingInfo.bike_id, ': ', error)
            callback(error, null)
            return
          }
          if (bookingDetails !== null) {
            logger('The bike with bike_id:', bookingInfo.bike_id, ' is already booked')
            return callback(errors.unauthorizedAccess(false), null)
          } else {
            const columnsAndValues = {
              booked_on: moment().unix(),
              user_id: bookingInfo.user_id,
              lock_id: bookedBike.lock_id,
              bike_id: bookedBike.bike_id,
              status: tripConstants.booking.active,
              operator_id: bookedBike.operator_id,
              customer_id: bookedBike.customer_id,
              fleet_id: bookedBike.fleet_id
            }
            fleetHandler.getFleetsWithPaymentAndMetadata({ fleet_id: [bookedBike.fleet_id] }, async (error, fleets) => {
              if (error) {
                Sentry.captureException(error, {bookingInfo})
                logger('Error: Failed to create booking for', bookingInfo.bike_id,
                  'Failed to get fleetsWithMetadata:', error)
                callback(error, null)
                return
              }

              if (bookingInfo.pricing_option_id) {
                try {
                  const pricingOption = await pricing.handlers.retrieve({
                    id: bookingInfo.pricing_option_id,
                    fleetId: bookedBike.fleet_id
                  })

                  if (!pricingOption) {
                    throw errors.customError(
                      `Invalid pricing option: ${bookingInfo.pricing_option_id}`,
                      responseCodes.BadRequest,
                      'BadRequest'
                    )
                  }
                } catch (error) {
                  Sentry.captureException(error, {bookingInfo})
                  return callback(error)
                }
              }

              let preAuthPayment
              let fleetInfo = fleets[0]

              if (isPaymentFleet) {
                try {
                  preAuthPayment = await preAuthorize({
                    userId: bookingInfo.user_id,
                    fleetPaymentSettings: fleetInfo
                  })
                } catch (error) {
                  Sentry.captureException(error, {bookingInfo})
                  return callback(error)
                }
              }

              _insertBookingQuery(columnsAndValues, (error) => {
                if (error) {
                  Sentry.captureException(error, {bookingInfo, columnsAndValues})
                  logger('Error: failed to save booking data. Failed with error:', error)
                  callback(error, null)
                }
                let bookingExpiration = config.bikeBookingExpiration
                if ((fleets.length !== 0) && fleets[0].bike_booking_interval) {
                  bookingExpiration = fleets[0].bike_booking_interval
                }
                const payload = {
                  booked_on: columnsAndValues.booked_on,
                  expires_in: bookingExpiration,
                  support_phone: config.lattisSupportPhoneNumber,
                  on_call_operator: null
                }
                // To send support phone number for help section
                operatorHandler.getOnCallOperator(bookedBike.fleet_id, (error, onCallOperator) => {
                  if (error) {
                    Sentry.captureException(error, {bookingInfo, bookedBike})
                    callback(errors.internalServer(false), null)
                    return
                  }
                  if (onCallOperator) {
                    payload.on_call_operator = onCallOperator.phone_number
                      ? String(onCallOperator.phone_number)
                      : null
                  }
                  if (isPaymentFleet) {
                    tripHandler.startTrip({
                      bike_id: bookingInfo.bike_id,
                      user_id: bookingInfo.user_id,
                      phone_number: bookingInfo.phone_number,
                      email: bookingInfo.email,
                      trip_start_time: payload.booked_on + payload.expires_in,
                      is_payment_fleet: isPaymentFleet,
                      device_token: bookingInfo.device_token || null,
                      pricing_option_id: bookingInfo.pricing_option_id
                    }, async (error, trip) => {
                      if (error) {
                        Sentry.captureException(error, {bookingInfo, bookedBike})
                        return callback(error, null)
                      }

                      if (preAuthPayment) {
                        await insertPreAuthPayment({
                          trip,
                          payment: preAuthPayment,
                          fleetPaymentSettings: fleetInfo
                        })
                      }

                      callback(null, Object.assign(payload, {
                        trip_id: trip.trip_id,
                        do_not_track_trip: trip.do_not_track_trip
                      }))
                    })
                  } else {
                    callback(null, payload)
                  }
                })
              })
            }
            )
          }
        }
        )
      })
    })
  })
}

/** * This method creates a booking record in the booking table
 *
 * @param {Integer} bookingInfo - Identifier for the booking details to get created
 * @param {Function} callback
 */
const createBookingV2 = async (bookingInfo, callback) => {
  /*
  const activeTrip = await tripHandler.isUserOnActiveTrip(bookingInfo.user_id)
  if (activeTrip) {
    try {
      await tripHandler.endTripWithCancelledOrElapsedReservation(activeTrip)
    } catch (error) {
      return callback(errors.customError(error.message || `You already have a scheduled reservation with the vehicle`, responseCodes.Conflict, 'ErrorCancellingScheduledReservation', true), null)
    }
  }
  */
  switch (bookingInfo.bookingType) {
    case 'port': {
      try {
        const port = await retrievePort({portId: bookingInfo.port_id})
        const hub = port.hub
        if (!hub) throw new Error('This port does not have a hub')
        if (hub && hub.type !== 'parking_station') throw new Error('You can only book/rent a port on a parking station hub')
        const fleet = port.fleet
        port.fleet_id = fleet.fleet_id
        if (!fleet) throw new Error('Port does not have a fleet')
        const isPaymentFleet = [fleetConstants.fleet_type.privateWithPayment, fleetConstants.fleet_type.publicWithPayment].includes(fleet.type)
        const fleetPaymentSettings = await db.main('fleet_payment_settings').where({ fleet_id: fleet.fleet_id }).first()
        // lets check if private & public fleet type users connect with stripe account or not
        if (isPaymentFleet) {
          const acceptsPayments = await tripHandler.fleetAcceptsPayments(fleetPaymentSettings)
          if (!acceptsPayments) {
            logger(
              `Error: Fleet ${fleet.fleet_id} is not configured to accept payments.`
            )
            return callback(
              errors.customError(
                'Fleet is not configured to accept payments.',
                responseCodes.Conflict,
                'MissingPaymentGateway'
              ),
              null
            )
          }
        }

        // lets check if the port is available to book
        if (!(port.port_status === 'Available' && port.hub.status === 'live' && port.port_current_status === 'parked')) {
          logger(`The port with port_id: ${port.port_id} is not available`)
          callback(errors.formatErrorForWire({ message: `The port with port_id:${port.port_id} is not available`, code: responseCodes.InvalidRequest }), null)
          return
        }

        if (fleet.require_phone_number) {
          if (!bookingInfo.phone_number) {
            logger('Info: The user does not have a phone number which is required for this fleet')
            return callback(errors.customError('Phone number required for this fleet', responseCodes.Conflict, 'Phone number required'), null)
          }
        }

        // TODO: Check if payment is MercadoPago
        checkActiveBooking(bookingInfo, 'port_id', port, isPaymentFleet, (error, booking) => {
          if (error) {
            Sentry.captureException(error, {bookingInfo, port})
            return callback(error, booking)
          }
          callback(null, booking)
        })
      } catch (error) {
        Sentry.captureException(error, {bookingInfo})
        logger('Error: Failed to get port details with', bookingInfo.port_id, ': ', error)
        return callback(error, null)
      }
      break
    }
    case 'hub':
      try {
        const hub = await getHubById(bookingInfo.hub_id, true)
        if (!hub) throw new Error('The hub is not Available or live')
        if (hub && hub.type !== 'parking_station') throw new Error('You can only book/rent a parking station hub')
        const fleet = hub.fleet
        if (!fleet) throw new Error('Port does not have a fleet')
        const isPaymentFleet = [fleetConstants.fleet_type.privateWithPayment, fleetConstants.fleet_type.publicWithPayment].includes(fleet.type)
        const fleetPaymentSettings = await db.main('fleet_payment_settings').where({ fleet_id: fleet.fleet_id }).first()
        // lets check if private & public fleet type users connect with stripe account or not
        if (isPaymentFleet) {
          const acceptsPayments = await tripHandler.fleetAcceptsPayments(fleetPaymentSettings)
          if (!acceptsPayments) {
            logger(
              `Error: Fleet ${fleet.fleet_id} is not configured to accept payments.`
            )
            return callback(
              errors.customError(
                'Fleet is not configured to accept payments.',
                responseCodes.Conflict,
                'MissingPaymentGateway'
              ),
              null
            )
          }
        }
        // lets check if the hub is available to book
        if (!(hub.remote_hub_status === 'Available' && hub.local_hub_status === 'live')) {
          logger('The hub with hub Id:', hub.hub_id, ' is either not available or not live')
          callback(errors.formatErrorForWire({ message: `The hub with hub Id:, ${hub.hub_id}, is not available` }), null)
          return
        }

        if (fleet.require_phone_number) {
          if (!bookingInfo.phone_number) {
            logger('Info: The user does not have a phone number which is required for this fleet')
            return callback(errors.customError('Phone number required for this fleet', responseCodes.Conflict, 'Phone number required'), null)
          }
        }

        // TODO: Check if payment is MercadoPago
        checkActiveBooking(bookingInfo, 'hub_id', hub, isPaymentFleet, (error, booking) => {
          if (error) {
            Sentry.captureException(error, {bookingInfo, hub})
            return callback(error, booking)
          }
          return callback(null, booking)
        })
      } catch (error) {
        Sentry.captureException(error, {bookingInfo})
        logger('Error: Failed to get port details with', bookingInfo.hub_id, ': ', error)
        return callback(error, null)
      }
      break
    case 'bike':
      bikeHandler.getBikesWithLock({ bike_id: bookingInfo.bike_id }, async (error, [bookedBike]) => {
        if (error || !bookedBike) {
          Sentry.captureException(error, {bookingInfo})
          logger('Error: Failed to get bike details with', bookingInfo.bike_id, ': ', error)
          return callback(errors.customError((error && error.message) || `Could not find bike with Id ${bookingInfo.bike_id}`, error ? responseCodes.InternalServer : responseCodes.ResourceNotFound, 'BikeNotFound', true), null)
        }

        const isPaymentFleet = (bookedBike.fleet_type === fleetConstants.fleet_type.privateWithPayment ||
          bookedBike.fleet_type === fleetConstants.fleet_type.publicWithPayment)

        // lets check if private & public fleet type users connect with stripe account or not
        if (isPaymentFleet) {
          const acceptsPayments = await tripHandler.fleetAcceptsPayments(bookedBike)

          if (!acceptsPayments) {
            logger(
              `Error: Fleet ${bookedBike.fleet_id} is not configured to accept payments.`
            )

            return callback(
              errors.customError(
                'Fleet is not configured to accept payments.',
                responseCodes.Conflict,
                'MissingPaymentGateway'
              ),
              null
            )
          }
        }

        // lets check if the bike is available to book
        if (![bikeConstants.current_status.parked, bikeConstants.current_status.collect].includes(bookedBike.current_status)) {
          logger('The bike with bike_id:', bookingInfo.bike_id, ' is not available')
          callback(errors.formatErrorForWire({ message: `The bike with bike_id:, ${bookingInfo.bike_id}, is not available`, code: responseCodes.InvalidRequest }), null)
          return
        }

        if (error) {
          Sentry.captureException(error, {bookingInfo, bookedBike})
          logger('Error: getting fleets with fleet_id:', bookedBike.fleet_id)
          return callback(error, null)
        }

        if (bookedBike.require_phone_number) {
          if (!bookingInfo.phone_number) {
            logger('Info: The user does not have a phone number which is required for this fleet')
            return callback(errors.customError('Phone number required for this fleet', responseCodes.Conflict, 'Phone number required'), null)
          }
        }

        if (payments.isMercadoPagoConnect(bookedBike)) {
          const userPaymentProfile = await db
            .main('user_payment_profiles')
            .where({
              user_id: bookingInfo.user_id,
              payment_gateway: GATEWAYS.mercadopago,
              source_fleet_id: bookedBike.fleet_id
            })
            .first()

          if (!userPaymentProfile) {
            return callback(
              errors.customError(
                'This user does not have a payment method that can pay into this fleet',
                responseCodes.Conflict,
                'Conflict'
              )
            )
          }
        }

        _updateBikeLocation(bookingInfo, (error) => {
          if (error) {
            Sentry.captureException(error, {bookingInfo, bookedBike})
            return callback(error, null)
          }

          // lets check if the user has already booked a bike
          getActiveBooking(_.pick(bookingInfo, 'user_id'), (error, bookedUser) => {
            if (error) {
              Sentry.captureException(error, {bookingInfo, bookedBike})
              logger('Error: Failed to get booking details with', bookingInfo.user_id, ': ', error)
              callback(error, null)
              return
            }
            if (bookedUser !== null) {
              logger('the user with user_id:', bookingInfo.user_id, ' has an active booking')
              callback(errors.customError('The user has an active booking', responseCodes.MethodNotAllowed, 'ActiveBookingError', true), bookedUser)
              return
            }

            // lets check if the the bike is booked already
            getActiveBooking(_.pick(bookingInfo, 'bike_id'), (error, bookingDetails) => {
              if (error) {
                Sentry.captureException(error, {bookingInfo, bookedBike})
                logger('Error: Failed to get booking details with', bookingInfo.bike_id, ': ', error)
                callback(error, null)
                return
              }
              if (bookingDetails !== null) {
                logger('The bike with bike_id:', bookingInfo.bike_id, ' is already booked')
                return callback(errors.unauthorizedAccess(false), null)
              } else {
                const columnsAndValues = {
                  booked_on: moment().unix(),
                  user_id: bookingInfo.user_id,
                  lock_id: bookedBike.lock_id,
                  bike_id: bookedBike.bike_id,
                  status: tripConstants.booking.active,
                  operator_id: bookedBike.operator_id,
                  customer_id: bookedBike.customer_id,
                  fleet_id: bookedBike.fleet_id
                }
                fleetHandler.getFleetsWithPaymentAndMetadata({ fleet_id: [bookedBike.fleet_id] }, async (error, fleets) => {
                  if (error) {
                    Sentry.captureException(error, {bookingInfo, bookedBike})
                    logger('Error: Failed to create booking for', bookingInfo.bike_id,
                      'Failed to get fleetsWithMetadata:', error)
                    callback(error, null)
                    return
                  }

                  if (bookingInfo.pricing_option_id) {
                    try {
                      const pricingOption = await pricing.handlers.retrieve({
                        id: bookingInfo.pricing_option_id,
                        fleetId: bookedBike.fleet_id
                      })

                      if (!pricingOption) {
                        throw errors.customError(
                          `Invalid pricing option: ${bookingInfo.pricing_option_id}`,
                          responseCodes.BadRequest,
                          'BadRequest'
                        )
                      }
                    } catch (error) {
                      Sentry.captureException(error, {bookingInfo, bookedBike})
                      return callback(error)
                    }
                  }

                  let preAuthPayment
                  let fleetInfo = fleets[0]

                  if (isPaymentFleet) {
                    try {
                      preAuthPayment = await preAuthorize({
                        userId: bookingInfo.user_id,
                        fleetPaymentSettings: fleetInfo
                      })
                    } catch (error) {
                      Sentry.captureException(error, {bookingInfo, bookedBike})
                      return callback(error)
                    }
                  }

                  _insertBookingQuery(columnsAndValues, async (error, res) => {
                    if (error) {
                      Sentry.captureException(error, {bookingInfo, bookedBike})
                      logger('Error: failed to save booking data. Failed with error:', error)
                      callback(error, null)
                    }
                    const insertedBookingId = res.insertId
                    const booking = await db.main('booking').where({booking_id: insertedBookingId}).select('booking_id', 'bike_id', 'port_id', 'hub_id', 'device_type').first() || {}
                    let bookingExpiration = config.bikeBookingExpiration
                    if ((fleets.length !== 0) && fleets[0].bike_booking_interval) {
                      bookingExpiration = fleets[0].bike_booking_interval
                    }
                    const payload = {
                      booked_on: columnsAndValues.booked_on,
                      expires_in: bookingExpiration,
                      support_phone: config.lattisSupportPhoneNumber,
                      on_call_operator: null,
                      ...booking
                    }
                    // To send support phone number for help section
                    operatorHandler.getOnCallOperator(bookedBike.fleet_id, (error, onCallOperator) => {
                      if (error) {
                        Sentry.captureException(error, {bookingInfo, bookedBike})
                        callback(errors.internalServer(false), null)
                        return
                      }
                      if (onCallOperator) {
                        payload.on_call_operator = onCallOperator.phone_number
                          ? String(onCallOperator.phone_number)
                          : null
                      }
                      if (isPaymentFleet) {
                        tripHandler.startTripV2({
                          bike_id: bookingInfo.bike_id,
                          user_id: bookingInfo.user_id,
                          phone_number: bookingInfo.phone_number,
                          email: bookingInfo.email,
                          trip_start_time: payload.booked_on + payload.expires_in,
                          is_payment_fleet: isPaymentFleet,
                          device_token: bookingInfo.device_token || null,
                          pricing_option_id: bookingInfo.pricing_option_id,
                          device_type: 'bike'
                        }, async (error, trip) => {
                          if (error) {
                            Sentry.captureException(error, {bookingInfo, bookedBike})
                            return callback(error, null)
                          }

                          if (preAuthPayment) {
                            await insertPreAuthPayment({
                              trip,
                              payment: preAuthPayment,
                              fleetPaymentSettings: fleetInfo
                            })
                          }

                          callback(null, Object.assign(payload, {
                            trip_id: trip.trip_id,
                            do_not_track_trip: trip.do_not_track_trip
                          }))
                        })
                      } else {
                        callback(null, payload)
                      }
                    })
                  })
                }
                )
              }
            }
            )
          })
        })
      })
      break
    default:
      logger('Error: This device is not supported for booking')
      return callback(errors.customError('Support for device unavailable', responseCodes.InternalServer, 'UnsupportedDevice'), null)
  }
}

async function preAuthorize ({ fleetPaymentSettings, userId }) {
  if (!fleetPaymentSettings.enable_preauth) {
    return null
  }

  if (!fleetPaymentSettings.preauth_amount) {
    throw errors.customError(
      'Fleet is missing a preauth amount',
      responseCodes.Conflict,
      'Conflict'
    )
  }

  logger('Pre-authorizing', {
    userId,
    fleetPaymentSettings: {
      preauth_amount: fleetPaymentSettings.preauth_amount,
      stripe_account_id: fleetPaymentSettings.stripe_account_id
    },
    amount: conversions.roundOffPricing(
      Number(fleetPaymentSettings.preauth_amount)
    )
  })

  return paymentHandler.authorizePayment({
    userId,
    fleetPaymentSettings: fleetPaymentSettings,
    amount: conversions.roundOffPricing(
      Number(fleetPaymentSettings.preauth_amount)
    )
  }).catch(error => {
    logger('Failed to pre-authorize: ', errors.errorWithMessage(error))

    const e = errors.customError(
      `Payment error: ${error.message}`,
      typeof error.code === 'string'
        ? responseCodes.Conflict
        : error.code,
      'PaymentFailure'
    )

    return Promise.reject(e)
  })
}

async function insertPreAuthPayment ({ payment, trip, fleetPaymentSettings }) {
  const data = {
    trip_id: trip.trip_id,
    fleet_id: fleetPaymentSettings.fleet_id,
    user_profile_id: payment.customer,
    charge_for_duration: 0,
    penalty_fees: 0,
    deposit: 0,
    total: Number(fleetPaymentSettings.preauth_amount),
    over_usage_fees: 0,
    transaction_id: payment.id,
    status: payments.STATUS.pending,
    gateway_status: payment.status,
    currency: payment.currency
  }

  return db.main('trip_payment_transactions').insert(data)
}

/** * This method gets the details of the booking from the booking table
 *
 * @param {Object} columnsAndValues - Identifier to filter the booking table
 * @param {Function} callback
 */
const getBookingDetails = (columnsAndValues, callback) => {
  const bookingQuery = query.selectWithAnd(dbConstants.tables.booking, null, columnsAndValues)
  sqlPool.makeQuery(bookingQuery, callback)
}

/** * This method inserts the booking in the booking table
 *
 * @param {Object} columnsAndValues - Identifier to insert the datas inside the table
 * @param {Function} callback
 * @private
 */
const _insertBookingQuery = (columnsAndValues, callback) => {
  const insertBookingQuery = query.insertSingle(dbConstants.tables.booking, columnsAndValues)
  sqlPool.makeQuery(insertBookingQuery, callback)
}

/** * This function is to update the booking details
 *
 * @param {Object} bookingDetails - Booking details to be updated
 * @param {Object} filterParam - Filter column and values to select booking row
 * @param {Function} callback
 */
const updateBooking = async (trx, bookingDetails, filterParam, callback) => {
  try {
    await (trx ? trx('booking') : db.main('booking')).update(_.omit(bookingDetails, 'booking_id', 'user_id', 'fleet_id', 'customer_id', 'operator_id')).where(filterParam)
    callback(errors.noError())
  } catch (error) {
    Sentry.captureException(error, {bookingDetails, filterParam})
    logger('Error: Failed to update booking with', filterParam, ':', error)
    callback(errors.internalServer(false))
  }
}

/** This method gets the details of the booking from the booking table
 *
 * @param {Object} filterParam - Identifier to get active booking
 * @param {Function} callback
 */
const getActiveBooking = async (filterParam, callback) => {
  const comparisonColumnAndValues = { status: tripConstants.booking.active }
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
  if (filterParam.trip_id) {
    comparisonColumnAndValues.trip_id = filterParam.trip_id
  }
  if (filterParam.port_id) {
    comparisonColumnAndValues.port_id = filterParam.port_id
  }
  if (filterParam.hub_id) {
    comparisonColumnAndValues.hub_id = filterParam.hub_id
  }
  if (filterParam.booking_id) {
    comparisonColumnAndValues.booking_id = filterParam.booking_id
  }

  const bookingQuery = query.selectWithAnd(dbConstants.tables.booking, null, comparisonColumnAndValues)

  sqlPool.makeQuery(bookingQuery, async (error, bookings) => {
    if (error) {
      Sentry.captureException(error, {filterParam, bookingQuery, bookings})
      logger('Error: Failed to get active booking with', filterParam, ':', error)
      callback(errors.internalServer(false), null)
      return
    }
    if (bookings.length === 0) {
      return callback(errors.noError(), null)
    }
    const activeBookings = []
    for (let booking of bookings) {
      const fleetMeta = await db.main('fleet_metadata').where({ fleet_id: booking.fleet_id }).select('fleet_id', 'bike_booking_interval').first()
      let bookingExpiration = config.bikeBookingExpiration
      if (fleetMeta && fleetMeta.bike_booking_interval) {
        bookingExpiration = fleetMeta.bike_booking_interval
      }
      booking.bike_booking_expiration = bookingExpiration

      if (((booking.booked_on + bookingExpiration) >= moment().unix()) || !!booking.trip_id) {
        if (booking.trip_id) {
          const trip = await db.main('trips').where({trip_id: booking.trip_id}).select('first_lock_connect').first()
          if (trip.first_lock_connect) continue // Trip was started by user
        }
        activeBookings.push(booking)
      } else if (booking.booked_on + bookingExpiration < moment().unix() && !booking.trip_id) {
        // If the booking is expired and there is no trip started, we will end it
        await db.main('booking').where({ booking_id: booking.booking_id }).update({ status: tripConstants.booking.cancelled, cancelled_on: moment().unix() })
      }
    }
    callback(errors.noError(), activeBookings.length > 0 ? activeBookings : null)
  })
}

/**
 * To update the bike location in case bike booked by QR code scanning
 *
 * @param {Object} properties - bike details to update
 * @param {Function} callback
 * @private
 */
const _updateBikeLocation = (properties, callback) => {
  if (properties.by_scan) {
    bikeHandler.updateBike(null, _.pick(properties, 'bike_id', 'latitude', 'longitude'), callback)
  } else {
    callback(errors.noError())
  }
}

module.exports = Object.assign(module.exports, {
  createBooking: createBooking,
  cancelBooking: cancelBooking,
  getBookingDetails: getBookingDetails,
  updateBooking: updateBooking,
  getActiveBooking: getActiveBooking,
  createBookingV2,
  cancelBookingV2
})
