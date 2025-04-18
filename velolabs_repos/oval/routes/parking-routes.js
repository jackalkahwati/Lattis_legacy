'use strict'

const express = require('express')
const router = express.Router()
const platform = require('@velo-labs/platform')
const logger = platform.logger
const ovalResponse = require('./../utils/oval-response')
const errors = platform.errors
const _ = require('underscore')
const parkingHandler = require('./../handlers/parking-handler')
const requestHelper = require('./../helpers/request-helper')

router.post('/get-parking-spots-for-fleet', function (req, res) {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: Some Parameters are missing in this request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, function (error, user) {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    parkingHandler.getParkingSpotsForFleet(req.body, function (error, parkingspots) {
      if (error) {
        logger('Error: processing parking spots:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      ovalResponse(res, errors.noError(), parkingspots)
    })
  })
})

router.post('/get-parking-zones-for-fleet', function (req, res) {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: Some Parameters are missing in this request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, function (error, user) {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    parkingHandler.getParkingZonesForFleet(req.body, function (error, parkingzones) {
      if (error) {
        logger('Error: processing parking zones:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), parkingzones)
    })
  })
})

module.exports = router
