const express = require('express')
const router = express.Router()
const hubsHandler = require('../handlers/hubs-handler')
const bikeHandler = require('./../handlers/bike-handler')
const requestHelper = require('./../helpers/request-helper')
const { errors, logger } = require('@velo-labs/platform')
const ovalResponse = require('./../utils/oval-response')
const _ = require('underscore')
const util = require('util')
const responseCodes = require('@velo-labs/platform/helpers/response-codes')
const db = require('./../db')
const searchBikesByCoordinates = util.promisify(
  bikeHandler.searchBikesByCoordinates
)

router.get('/', async (req, res) => {
  if (!_.has(req.query, 'sw') || !_.has(req.query, 'ne')) {
    logger('Error: No Parameters sent in request: get all rentals')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(
    req,
    res,
    true,
    async (error, user) => {
      if (error) return
      const appType = req.headers['user-agent']
      if (!appType) {
        return ovalResponse(
          res,
          errors.customError(
            'User agent must be provided',
            responseCodes.BadRequest,
            'InvalidUserAgent',
            true
          ),
          null
        )
      }
      let fleetBikes
      const whiteLabelFleetIds = appType
        ? await bikeHandler.getAppFleets(appType)
        : []
      if (!whiteLabelFleetIds.length && appType !== 'lattis') {
        return ovalResponse(
          res,
          errors.customError(
            'Invalid user agent/app type',
            responseCodes.ResourceNotFound,
            'InvalidUserAgent',
            true
          ),
          null
        )
      }
      try {
        req.query.user_id = user.user_id
        req.sentryObj.configureScope(scope => {
          scope.setTag('user_id', user.user_id)
        })
        const hubs = await hubsHandler.getAdjacentHubs(
          req.query,
          whiteLabelFleetIds
        )
        const allBikes = await searchBikesByCoordinates(req.query)
        if (whiteLabelFleetIds.length && allBikes.length) {
          fleetBikes = allBikes.filter((bike) =>
            whiteLabelFleetIds.includes(bike.fleet_id)
          )
        }
        let bikes = fleetBikes || allBikes
        bikes = bikes.map((bike) => {
          if (bike.bike_battery_level) {
            bike.bike_battery_level = parseInt(bike.bike_battery_level)
          }

          if (bike.controllers) {
            bike.controllers.map((controller) => {
              if (controller.vendor === 'Manual Lock') {
                const metadata = controller.metadata
                controller.key = metadata
                  ? JSON.parse(metadata).key
                  : controller.key
              }
            })
          }
          return bike
        })

        const addReservation = async (bike) => {
          const date = await db
            .main('reservations')
            .where({
              bike_id: bike.bike_id,
              reservation_terminated: null
            })
            .max('created_at as latestDate')
            .first()

          const bikeReservation =
            (await db
              .main('reservations')
              .where({
                bike_id: bike.bike_id,
                reservation_terminated: null,
                created_at: date.latestDate
              })
              .first()) || null

          bike.reservation = bikeReservation
          return bike
        }

        bikes = await Promise.all(bikes.map(addReservation))
        const response = { hubs, bikes }
        ovalResponse(res, errors.noError(), response)
      } catch (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
      }
    }
  )
})

router.get('/search', async (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    req.body.bike_name = req.query.bike_name
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    bikeHandler.findBikesByName(req.body, (error, bikes) => {
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      if (bikes && bikes.bikes && Array.isArray(bikes.bikes)) {
        bikes = bikes.bikes.map(bike => {
          if (bike.bike_battery_level) {
            bike.bike_battery_level = parseInt(bike.bike_battery_level)
            if (bike.controllers) {
              bike.controllers.map(controller => {
                if (controller.vendor === 'Manual Lock') {
                  const metadata = controller.metadata
                  controller.key = metadata ? JSON.parse(metadata).key : controller.key
                }
              })
            }
          }
          return bike
        })
      }
      ovalResponse(res, errors.noError(), bikes)
    })
  })
})

router.get('/find', async (req, res) => {
  const qrCode = req.query.qr_code
  if (!qrCode) {
    ovalResponse(res, errors.customError('Provide a qr_code as a query string', responseCodes.BadRequest, 'RequiresQRode'), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    req.body.qr_code = qrCode
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    bikeHandler.getRentalDetails(req.sentryObj, req.body, (error, bikeDetails) => {
      if (error) {
        return ovalResponse(res, errors.customError(error.message || 'An error occurred getting rental details', error.code || responseCodes.BadRequest, error.name || 'ErrorGettingRentalDetails', true), null)
      }
      return ovalResponse(res, errors.noError(), bikeDetails)
    })
  })
})

module.exports = router
