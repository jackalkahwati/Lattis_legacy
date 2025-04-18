'use strict'

const { logger, errors } = require('@velo-labs/platform')
const express = require('express')
const _ = require('underscore')

const tripHandler = require('./../handlers/trip-handler')
const requestHelper = require('./../helpers/request-helper')
const ovalResponse = require('./../utils/oval-response')
const { getAppDetails } = require('../utils/routes')
const responseCodes = require('@velo-labs/platform/helpers/response-codes')

const router = express.Router()
const routerV2 = express.Router()

const {configureSentry} = require('../utils/configureSentry')

const Sentry = configureSentry()

/**
 * To get all the trips for given identifier
 *
 * The request body can include any one of the following: trip_id, bike_id,
 * fleet_id, customer_id, operator_id to filter trips
 */
router.get('/get-trips', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) {
      Sentry.captureException(error, req.body)
      return
    }
    req.body.user_id = user.user_id
    logger('get trips request', req.body)

    tripHandler.getTripsWithPaymentInfo(req.body, (error, trips) => {
      if (error) {
        Sentry.captureException(error, req.body)
        logger(
          'Error: failed to get trips for request parameter:',
          req.body,
          'Failed with error',
          error
        )
        ovalResponse(res, errors.internalServer(true), null)
        return
      }
      ovalResponse(res, errors.noError(), tripHandler.formatTripsForWire(trips))
    })
  })
})

/**
 * To start the trip
 *
 * The request body should include bike_id and steps(to get start location)
 */
router.post('/start-trip', (req, res) => {
  if (!_.has(req.body, 'bike_id') || !_.has(req.body, 'latitude') || !_.has(req.body, 'longitude')) {
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) {
      Sentry.captureException(error, req.body)
      return
    }
    req.body.user_id = user.user_id
    req.body.email = user.email
    req.body.phone_number = user.phone_number
    logger('Start trip', req.body)
    // This endpoint is called when a user starts trip by scan so first_lock_connect=1
    req.body.first_lock_connect = 1
    tripHandler.startTrip(req.body, (error, trip) => {
      if (error) {
        Sentry.captureException(error, req.body)
        logger('Error: could not start trip with id: ', req.body.user_id, 'Failed with error', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      trip.do_not_track_trip = Boolean(trip.do_not_track_trip)
      ovalResponse(res, errors.noError(), trip)
    })
  })
})

/**
 * To start the trip
 *
 * The request body should include bike_id and steps(to get start location)
 */
routerV2.post('/start-trip', (req, res) => {
  if (((!_.has(req.body, 'bike_id') || !_.has(req.body, 'latitude') || !_.has(req.body, 'longitude')) && !_.has(req.body, 'hub_id') && !_.has(req.body, 'port_id'))) {
    ovalResponse(res, errors.customError('Provide a bike_id with coordinates or port_id or hub hub_id', responseCodes.BadRequest, 'MissingParameter', true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) {
      Sentry.captureException(error, req.body, user)
      return
    }
    req.body.user_id = user.user_id
    req.body.email = user.email
    req.body.phone_number = user.phone_number
    logger('Start trip', req.body)
    // This endpoint is called when a user starts trip by scan so first_lock_connect=1
    req.body.first_lock_connect = 1
    tripHandler.startTripV2(req.body, (error, trip) => {
      if (error) {
        Sentry.captureException(error, req.body)
        logger('Error: could not start trip with id: ', req.body.user_id, 'Failed with error', error)
        ovalResponse(res, errors.customError(error.message || `Error: could not start trip with id: ${req.body.user_id}`, responseCodes.InternalServer, 'StartTripFailed', true), null)
        return
      }
      trip.do_not_track_trip = Boolean(trip.do_not_track_trip)
      ovalResponse(res, errors.noError(), trip)
    })
  })
})

/**
 * To end the trip
 *
 * The request body should include trip_id, latitude and longitude
 */
router.post('/end-trip', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    if (!_.has(req.body, 'trip_id') || !_.has(req.body, 'latitude') || !_.has(req.body, 'longitude')) {
      ovalResponse(res, errors.missingParameter(true), null)
      return
    }
    logger('End ride request body', {
      ...req.body,
      card_token: req.body.card_token
        ? req.body.card_token.slice(0, 4) + '...'
        : undefined
    })
    tripHandler.endTrip(req.body, async (error, tripResponse) => {
      if (error) {
        error.code = (error.name === 'MissingPaymentGateway') ? 411 : error.code
        logger('Error: could not end trip with id: ', req.body.trip_id, 'Failed with error', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      tripResponse.do_not_track_trip = Boolean(tripResponse.do_not_track_trip)
      tripResponse.first_lock_connect = Boolean(tripResponse.first_lock_connect)
      if (tripResponse && tripResponse.steps && tripResponse.steps.length) {
        for (let i = 0; i < tripResponse.steps.length; i++) {
          if (tripResponse.steps[i].length) {
            for (let j = 0; j < tripResponse.steps[i].length; j++) {
              if (tripResponse.steps[i][j]) {
                tripResponse.steps[i][j] = parseFloat(tripResponse.steps[i][j])
              }
            }
          }
        }
      }
      ovalResponse(res, errors.noError(), tripResponse)
      try {
        const appDetails = await getAppDetails(req)
        await tripHandler.sendTripEmail(tripResponse, appDetails, user && user.language_preference)
      } catch (error) {
        logger('error sending ride receipt trip email:', error)
      }
    })
  })
})

/**
 * To update the trip
 *
 * The request body should include trip_id and steps
 */
router.post('/update-trip', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    if (!_.has(req.body, 'trip_id') || !_.has(req.body, 'steps')) {
      ovalResponse(res, errors.missingParameter(true), null)
      return
    }
    tripHandler.updateTrip(null, req.body, (error, tripResponse) => {
      if (error) {
        return ovalResponse(res, errors.customError(error.message || 'An error occurred updating the trip', error.code || responseCodes.BadRequest, error.name || 'UpdatingTripError', true), null)
      }
      if (!tripResponse) {
        return ovalResponse(res, errors.customError('An active trip was not found. The trip might be ended', responseCodes.ResourceNotFound, 'ActiveTripNotFound', true), null)
      }
      if (tripResponse.ended_trip) tripResponse.ended_trip['first_lock_connect'] = !!tripResponse.ended_trip['first_lock_connect']
      tripResponse.do_not_track_trip = !!tripResponse.do_not_track_trip
      if (error) {
        logger('Error: could not update trip with id: ', req.body.trip_id, 'Failed with error', error)
        return ovalResponse(res, errors.customError(error.message || 'An error occurred updating the trip', error.code || responseCodes.BadRequest, error.name || 'UpdatingTripError', true), null)
      }
      tripResponse.do_not_track_trip = Boolean(tripResponse.do_not_track_trip)
      ovalResponse(res, errors.noError(), tripResponse)
    })
  })
})

/**
 * To get the trip details
 *
 * The request body should include trip_id
 */
router.post('/get-trip-details', (req, res) => {
  if (!_.has(req.body, 'trip_id')) {
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    tripHandler.getTripInformation({ userId: user.user_id, tripId: req.body.trip_id }, (e, tripResponse) => {
      if (e) return ovalResponse(res, errors.customError(e.message || e || 'An error occurred getting trip details', responseCodes.InternalServer, 'ErrorGettingTripDetails', true), null)
      if (!tripResponse || (tripResponse && !tripResponse.trip)) {
        return ovalResponse(res, errors.customError(`The trip with Id ${req.body.trip_id} was not found or has ended`, responseCodes.ResourceNotFound, 'TripNotFound', true), null)
      }
      if (tripResponse && tripResponse.trip.steps && tripResponse.trip.steps.length) {
        for (let i = 0; i < tripResponse.trip.steps.length; i++) {
          if (tripResponse.trip.steps[i].length) {
            for (let j = 0; j < tripResponse.trip.steps[i].length; j++) {
              if (tripResponse.trip.steps[i][j]) {
                tripResponse.trip.steps[i][j] = parseFloat(tripResponse.trip.steps[i][j])
              }
            }
          }
        }
      }
      return ovalResponse(res, errors.noError(), tripResponse)
    })
  })
})

/**
 * Used to update a trip rating for a trip
 *
 * The request body should include the trip_id, rating
 */
router.post('/update-rating', (req, res) => {
  if (!_.has(req.body, 'trip_id') || !_.has(req.body, 'rating')) {
    logger('Error: Some Parameters are missing in this request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, async (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    try {
      await tripHandler.updateTripRating(req.body)
      ovalResponse(res, errors.noError(), {msg: 'Rating updared successfully'})
    } catch (error) {
      ovalResponse(res, errors.customError(error.message || 'An error occurred updating trip rating', responseCodes.InternalServer, 'FailedRatingUpdate', true))
    }
  })
})

module.exports = {router, v2Router: routerV2}
