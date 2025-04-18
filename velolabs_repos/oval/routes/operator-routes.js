'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const errors = platform.errors
const express = require('express')
const router = express.Router()
const _ = require('underscore')
const ovalResponse = require('./../utils/oval-response')
const lockHandler = require('./../handlers/lock-handler')
const operatorHandler = require('./../handlers/operator-handler')
const requestHelper = require('./../helpers/request-helper')

router.post('/add-lock', (req, res) => {
  if ((!_.has(req.body, 'bike_id') &&
        (!_.has(req.body, 'bike_name') || !_.has(req.body, 'bike_battery_level'))) ||
        !_.has(req.body, 'mac_id')) {
    logger('Could not add adding lock. Missing the required parameter')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  lockHandler.addLock(req.body, (error) => {
    if (error) {
      logger('Error: failed to update lock data', error)
      ovalResponse(res, errors.internalServer(true), null)
      return
    }
    ovalResponse(res, errors.noError(), null)
  })
})

/**
 * Used to login for an operator
 *
 * The request body should include the username,password
 */
router.post('/login', (req, res) => {
  if ((!_.has(req.body, 'username') || (!_.has(req.body, 'password')))) {
    logger('There are no Parameters')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  operatorHandler.login(req.body, (error, operator) => {
    if (error) {
      logger('Error: failed to update lock data', error)
      ovalResponse(res, errors.formatErrorForWire(error), null)
      return
    }
    ovalResponse(res, errors.noError(), operator)
  })
})

/**
 * Used to get an operator
 *
 * The request body should include the fleet_id
 */
router.post('/get-operator', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Some Parameters not passed to server')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
    if (error) return
    req.body.operator_id = operator.operator_id
    operatorHandler.getOperatorsByFleets(req.body, (error, operators) => {
      if (error) {
        logger(error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), operators)
    })
  })
})

module.exports = router
