const express = require('express')
const router = express.Router()
const hubsHandler = require('../handlers/hubs-handler')
const requestHelper = require('./../helpers/request-helper')
const { errors, logger, responseCodes } = require('@velo-labs/platform')
const ovalResponse = require('./../utils/oval-response')
const _ = require('underscore')

router.get('/parking', async (req, res) => {
  if (!(_.has(req.query, 'lat') && _.has(req.query, 'lon') && _.has(req.query, 'bikeId'))) {
    logger('Error: No Parameters sent in request: getAdjacentHubs')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, async (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    const { lat, lon, bikeId } = req.query
    try {
      const hubs = await hubsHandler.getHubParkingSpots(parseFloat(lat), parseFloat(lon), parseInt(bikeId))
      ovalResponse(res, errors.noError(), hubs)
    } catch (error) {
      ovalResponse(res, errors.formatErrorForWire(error), null)
    }
  })
})

router.get('/', async (req, res) => {
  if (!_.has(req.query, 'sw') || !_.has(req.query, 'ne')) {
    logger('Error: No Parameters sent in request: getAdjacentHubs')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, async (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    try {
      const hubs = await hubsHandler.getAdjacentHubs(req.query)
      ovalResponse(res, errors.noError(), hubs)
    } catch (error) {
      ovalResponse(res, errors.formatErrorForWire(error), null)
    }
  })
})

router.post('/:uuid/undock', async (req, res) => {
  if (!_.has(req.params, 'uuid')) {
    logger('Error: No Parameters sent in request: undock bike from hub')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, async (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    try {
      const hubType = req.body.hub_type || req.params.hub_type
      const hubs = await hubsHandler.undockVehicle(req.params, hubType)
      ovalResponse(res, errors.noError(), hubs)
    } catch (error) {
      ovalResponse(res, errors.formatErrorForWire(error), null)
    }
  })
})

router.post('/custom/port/:port_uuid/unlock', (req, res) => {
  try {
    if (!_.has(req.params, 'port_uuid')) {
      logger('Error: No Parameters sent in request: unlock port')
      ovalResponse(res, errors.customError('Missing a port_uuid', responseCodes.ResourceNotFound, 'MissingParameter', true), null)
      return
    }
    requestHelper.userAuthorizationValidator(req, res, true, async (error, user) => {
      if (error) return
      req.sentryObj.configureScope(scope => {
        scope.setTag('user_id', user.user_id)
      })
      try {
        const portUUID = req.params.port_uuid
        const unlockResponse = await hubsHandler.unlockCustomPorts(portUUID)
        ovalResponse(res, errors.noError(), unlockResponse)
      } catch (error) {
        ovalResponse(res, errors.customError(error.message || 'An error occurred unlocking the port', error.code || responseCodes.InternalServer, error.name || 'ErrorUnlockingHub', true), null)
      }
    })
  } catch (error) {
    ovalResponse(res, errors.customError(error.message || 'An error occurred unlocking the port', error.code || responseCodes.InternalServer, error.name || 'ErrorUnlockingHub', true), null)
  }
})

router.post('/custom/hub/:hub_uuid/unlock', (req, res) => {
  try {
    if (!_.has(req.params, 'hub_uuid')) {
      logger('Error: No Parameters sent in request: unlock hub')
      ovalResponse(res, errors.customError('Missing a hub_uuid', responseCodes.ResourceNotFound, 'MissingParameter', true), null)
      return
    }
    requestHelper.userAuthorizationValidator(req, res, true, async (error, user) => {
      if (error) return
      req.sentryObj.configureScope(scope => {
        scope.setTag('user_id', user.user_id)
      })
      try {
        const hubUUID = req.params.hub_uuid
        const unlockResponse = await hubsHandler.unlockCustomHubs(hubUUID)
        ovalResponse(res, errors.noError(), unlockResponse)
      } catch (error) {
        ovalResponse(res, errors.customError(error.message || 'An error occurred unlocking the hub', error.code || responseCodes.InternalServer, error.name || 'ErrorUnlockingHub', true), null)
      }
    })
  } catch (error) {
    ovalResponse(res, errors.customError(error.message || 'An error occurred unlocking the hub', error.code || responseCodes.InternalServer, error.name || 'ErrorUnlockingHub', true), null)
  }
})

module.exports = router
