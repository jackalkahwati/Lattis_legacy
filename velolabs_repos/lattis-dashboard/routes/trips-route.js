'use strict'

const platform = require('@velo-labs/platform')
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const tripHelper = platform.tripHelper
const errors = platform.errors
const logger = platform.logger
const express = require('express')
const router = express.Router()
const _ = require('underscore')
const tripHandler = require('./../model_handlers/trip-handler')
const axios = require('axios')
const { generateToken } = require('../utils/auth')
const authorization = require('../utils/authentication/authorization')
const envConfig = require('../envConfig')

const GPS_SERVICE_URL = envConfig('GPS_TRACKING_SERVICE')

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
    logger('Error getting trips from GPS service')
    logger('Error:::', error.message || error.response.data.message)
    return null
  }
}

const commonAuthorizers = [
  {
    getResourceIdFromRequest: (req) => req.body.fleet_id,
    authorizingFn: authorization.fleets.hasFleetAccess
  },
  {
    getResourceIdFromRequest: (req) => req.body.trip_id,
    authorizingFn: authorization.trips.hasTripAccess
  }
]

const getTripsAuthorizers = [
  ...commonAuthorizers,
  {
    getResourceIdFromRequest: req => req.body.bike_id,
    authorizingFn: authorization.bikes.hasBikeAccess
  },
  {
    getResourceIdFromRequest: req => req.body.port_id,
    authorizingFn: () => ({ authorized: true })
  }
]

router.post('/get-trips',
  authorization.authorize(getTripsAuthorizers, {
    unauthorized: res => jsonResponse(res, responseCodes.OK, null, [])
  }),
  function (req, res) {
    if (
      !_.has(req.body, 'trip_id') &&
      !_.has(req.body, 'user_id') &&
      !_.has(req.body, 'bike_id') &&
      !_.has(req.body, 'fleet_id') &&
      !_.has(req.body, 'customer_id') &&
      !_.has(req.body, 'operator_id') &&
      !_.has(req.body, 'port_id')
    ) {
      logger('Error: failed to get trips. Required parameter not sent')
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.missingParameter(true),
        null
      )
      return
    }
    tripHandler.getTrips(req.body, async (error, data) => {
      let trips = data.trips || []
      let pagination = data.pagination || {
        page: 1,
        per_page: 10,
        total: 0
      }
      if (error) {
        logger(
          'Error: failed to get trips for request parameter:',
          req.body,
          'Failed with error',
          error
        )
        jsonResponse(
          res,
          responseCodes.InternalServer,
          errors.formatErrorForWire(error),
          null
        )
        return
      }
      const token = generateToken()
      const tripIds = []

      for (let trip of trips) {
        if (trip.vendor && ['Segway', 'Segway IoT EU', 'Grow', 'Geotab IoT', 'ACTON', 'Omni IoT', 'Teltonika', 'Okai', 'Sentinel'].includes(trip.vendor)
        ) {
          if (trip.date_created > 1613080737) {
          // Only make requests for trips after this date to avoid unnecessary requests
            tripIds.push(trip.trip_id)
          }
        }
      }
      let tripsObject = {}
      try {
        const tripsInfo = await getTripsByIds(tripIds, token)
        if (tripsInfo && tripsInfo.payload) {
          for (let i = 0; i < tripsInfo.payload.length; i++) {
            const trip = tripsInfo.payload[i]
            const distance = (trip.steps && Array.isArray(trip.steps)) ? tripHelper.getTripDistance(trip).toFixed(2) : 0
            tripsObject[trip.trip_id] = {
              distance,
              steps: trip.steps,
              end_address: trip.end_address,
              date_endtrip: trip.date_endtrip,
              trip_id: trip.trip_id,
              bike_id: trip.bike_id,
              start_address: trip.start_address,
              date_created: trip.date_created,
              trip_distance: distance,
              manualEndTrip: trip.manualEndTrip
            }
          }
        }

        trips = trips.map((trip) => {
          if (tripsObject[trip.trip_id]) {
            let tripInObject = tripsObject[trip.trip_id]
            let distance
            const endAddress = trip.date_endtrip ? tripInObject.end_address || trip.end_address || 'Unnamed Road' : trip.end_address
            // Trips that were fixed because of some issues. The steps might not be accurate
            if (tripInObject.manualEndTrip || !tripInObject.steps.length > 0) distance = tripHelper.getTripDistance(trip).toFixed(2)
            else if (tripInObject.steps && Array.isArray(tripInObject.steps) && tripInObject.steps.length > 0) {
              distance = tripHelper.getTripDistance(tripInObject).toFixed(2)
            }
            return {
              ...trip,
              ...tripsObject[trip.trip_id],
              date_endtrip: tripsObject[trip.trip_id].date_endtrip || trip.date_endtrip,
              distance,
              end_address: endAddress
            }
          }
          return trip
        })
      } catch (error) {
        logger('Error getting trips info from GPS Service', error)
        console.log('Error::', error)
      }

      jsonResponse(res, responseCodes.OK, errors.noError(), {trips, pagination})
    })
  })

router.use(
  authorization.authorize(
    commonAuthorizers,
    {
      unauthorized: (res) =>
        res.status(responseCodes.ResourceNotFound).send({
          message: 'Not found'
        })
    }
  )
)

router.post('/end-trip', function (req, res) {
  if (
    !_.has(req.body, 'trip_id') ||
    !_.has(req.body, 'to') ||
    !_.has(req.body, 'fleet_id')
  ) {
    logger('Error: failed to end trip. Required parameter not sent')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  req.body.acl = JSON.parse(req.body.acl)[0]
  tripHandler.endTrip(req.body, function (error, trip) {
    if (error) {
      logger(
        'Error: failed to end trip for request parameter:',
        req.body,
        'Failed with error',
        errors.errorWithMessage(error)
      )
      jsonResponse(
        res,
        responseCodes.InternalServer,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), trip)
  })
})

router.post('/end-trip-port', function (req, res) {
  if (
    !_.has(req.body, 'trip_id') ||
    !_.has(req.body, 'to') ||
    !_.has(req.body, 'fleet_id')
  ) {
    logger('Error: failed to end trip. Required parameter not sent')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  req.body.acl = JSON.parse(req.body.acl)[0]
  tripHandler.endTripPort(req.body, function (error, trip) {
    if (error) {
      logger(
        'Error: failed to end trip for request parameter:',
        req.body,
        'Failed with error',
        errors.errorWithMessage(error)
      )
      jsonResponse(
        res,
        responseCodes.InternalServer,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), trip)
  })
})

router.post('/trip-analytics', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: failed to get trip analytics. Required parameter not sent')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  tripHandler.tripAnalytics(req.body, (error, trip) => {
    if (error) {
      logger(
        'Error: failed to get trips for request parameter:',
        req.body,
        'Failed with error',
        error
      )
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), trip)
  })
})

router.post('/refund-trip', (req, res) => {
  if (!_.has(req.body, 'trip_id')) {
    logger(
      'Error: Failed to refund trip. Required parameter `trip_id` is not set.'
    )

    return jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
  }

  return tripHandler
    .refundTrip(req.body)
    .then((refund) =>
      jsonResponse(res, responseCodes.OK, errors.noError(), refund)
    )
    .catch((error) =>
      jsonResponse(
        res,
        error.code || responseCodes.InternalServer,
        errors.formatErrorForWire(error),
        null
      )
    )
})

module.exports = router
