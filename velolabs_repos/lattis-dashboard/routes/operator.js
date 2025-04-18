'use strict'

const platform = require('@velo-labs/platform')
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const errors = platform.errors
const logger = platform.logger
const config = platform.config
const express = require('express')
const router = express.Router()
const _ = require('underscore')
const operatorHandler = require('./../model_handlers/operator-handler')

router.post('/check-operator', (req, res) => {
  if (!_.has(req.body, 'email') || !_.has(req.body, 'operator_id')) {
    logger('Parameter Missing : operator_id not being sent to Server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  operatorHandler.checkOperator(req.body, (error, status) => {
    if (error) {
      logger(error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

router.post('/create-operator', (req, res) => {
  if (!operatorHandler.validateOperator(req.body)) {
    logger('Some Parameters not passed to server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  const url = config.mailer.baseUrl ? config.mailer.baseUrl + '#' + '/' + 'password-reset'
    : req.protocol + '://' + req.get('host') + '/' + '#' + '/' + 'password-reset'
  operatorHandler.createOperator(req.body, url, (error, status) => {
    if (error) {
      logger(error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

router.post('/delete-operator', (req, res) => {
  if (!_.has(req.body, 'operator_id') || !_.has(req.body, 'fleet_id')) {
    logger('Some Parameters not passed to server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  operatorHandler.deleteOperator(req.body, (error, status) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

router.post('/get-preferences', (req, res) => {
  if (!_.has(req.body, 'operator_id')) {
    logger('Some Parameters not passed to server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  operatorHandler.getPreferences(req.body, (error, preferences) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), preferences)
  })
})

router.post('/set-preferences', (req, res) => {
  if (!operatorHandler.validatePreferences(req.body)) {
    logger('Some Parameters not passed to server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  operatorHandler.setPreferences(req.body, (error, status) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

router.post('/create-customer-and-operator', (req, res) => {
  if (!_.has(req.body, 'customer_name') && !_.has(req.body, 'first_name') &&
        !_.has(req.body, 'last_name') && !_.has(req.body, 'email') && !_.has(req.body, 'phone_number')) {
    logger('Some Parameters not passed to server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }

  const url = config.mailer.baseUrl ? config.mailer.baseUrl + '#' + '/' + 'password-reset'
    : req.protocol + '://' + req.get('host') + '/' + '#' + '/' + 'password-reset'

  operatorHandler.createCustomerAndOperator(req.body, url, (error) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), null)
  })
})

router.post('/get-operator', (req, res) => {
  if (!_.has(req.body, 'operator_id')) {
    logger('Parameter Missing : operator_id not being sent to Server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  operatorHandler.getOperator(null, {operator_id: req.body.operator_id}, (error, status) => {
    if (error) {
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

module.exports = router
