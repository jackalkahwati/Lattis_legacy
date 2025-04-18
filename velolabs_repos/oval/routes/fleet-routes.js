'use strict'

const { logger, errors, jsonResponse } = require('@velo-labs/platform')
const express = require('express')

const _ = require('underscore')
const ovalResponse = require('./../utils/oval-response')
const fleetHandler = require('./../handlers/fleet-handler')
const bikeHandler = require('./../handlers/bike-handler')
const requestHelper = require('./../helpers/request-helper')
const { auth } = require('./middleware')
const { statusCodeFromError } = require('../utils/responseCodes')

const router = express.Router()

router.get('/', auth.authenticate, async (req, res) => {
  try {
    let fleets = await fleetHandler.list({ user: req.user })
    let appType = req.headers['user-agent']
    const whiteLabelFleetIds = appType ? await bikeHandler.getAppFleets(appType) : []

    // filter fleets if current app is whitelabel
    if (whiteLabelFleetIds.length) {
      fleets = fleets.filter(fleet => whiteLabelFleetIds.includes(fleet.fleet_id))
    }

    return ovalResponse(res, errors.noError(), fleets)
  } catch (error) {
    logger(
      'Failed to list fleets',
      { user: req.user.user_id },
      'Error: ',
      errors.errorWithMessage(error)
    )

    return jsonResponse(
      res,
      statusCodeFromError(error),
      errors.formatErrorForWire(error)
    )
  }
})

/**
 * Used to login for an operator
 *
 * The request body should include the username,password
 */
router.get('/get-fleets', (req, res) => {
  requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
    if (error) return
    fleetHandler.getFleetAssociations(_.pick(operator, 'operator_id'), (error, fleetAssociations) => {
      if (error) {
        logger('Error: failed to get fleets for', operator.operator_id, error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      if (fleetAssociations.length !== 0) {
        fleetHandler.getFleets({fleet_id: _.pluck(fleetAssociations, 'fleet_id')}, (error, fleets) => {
          if (error) {
            logger('Error: failed to get fleets for', _.pluck(fleetAssociations, 'fleet_id'), error)
            ovalResponse(res, errors.formatErrorForWire(error), null)
            return
          }
          for (let i = 0, len = fleets.length; i < len; i++) {
            fleets[i] = _.omit(fleets[i], 'key')
          }
          ovalResponse(res, errors.noError(), fleets)
        })
      } else {
        ovalResponse(res, errors.noError(), [])
      }
    })
  })
})

router.post('/check-parking-fee', function (req, res) {
  if (!_.has(req.body, 'latitude') || !_.has(req.body, 'longitude') || !_.has(req.body, 'fleet_id')) {
    logger('Error: Some Parameters are missing in this request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, function (error, user) {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    fleetHandler.checkParkingFees(req.body, function (error, uploadedUrl) {
      if (error) {
        logger('Error: Check parking fees:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), uploadedUrl)
    })
  })
})

router.post('/token', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: Some Parameters are missing in this request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  fleetHandler.getToken(req.body, function (error, token) {
    if (error) {
      logger('Error: Check parking fees:', error)
      ovalResponse(res, errors.formatErrorForWire(error), null)
      return
    }
    ovalResponse(res, errors.noError(), token)
  })
})

router.get('/fleet-of-token', (req, res) => {
  if (!_.has(req.headers, 'api-token')) {
    logger('Error: Some Parameters are missing in this request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  fleetHandler.validateFleetToken({api_token: req.header('api-token')}, (error, fleet) => {
    if (error) {
      logger('Error: unable to get fleet details with api token: ', req.header('api-token'))
      return ovalResponse(res, errors.formatErrorForWire(error), null)
    }
    ovalResponse(res, errors.noError(), fleet)
  })
})

router.get('/geofences', async (req, res) => {
  const authorized = await new Promise((resolve) => {
    requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
      req.sentryObj.configureScope(scope => {
        scope.setTag('user_id', user.user_id)
      })
      resolve(!error)
    })
  })
  if (!authorized) {
    return
  }

  if (!_.has(req.query, 'fleet_id')) {
    logger('Error: fleet_id is missing from this query params')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  try {
    const gofences = await fleetHandler.getFleetGeofences(req.query.fleet_id)
    ovalResponse(res, errors.noError(), gofences)
  } catch (error) {
    logger('Error: Failed to fetch geofences:', error)
    ovalResponse(res, errors.formatErrorForWire(error), null)
  }
})

module.exports = router
