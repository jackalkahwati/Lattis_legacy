'use strict'

const platform = require('@velo-labs/platform')
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const errors = platform.errors
const logger = platform.logger
const express = require('express')
const router = express.Router()
const _ = require('underscore')
const paymentHandler = require('./../model_handlers/payment-handler')
const { validator, schema } = require('../validators')

router.post('/save-stripe-connect-info', (req, res) => {
  if (!_.has(req.body, 'fleet_id') || !_.has(req.body, 'code')) {
    logger('Parameter Missing : code or fleet_id not being sent to Server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  paymentHandler.saveStripeConnectInfo(req.body, (error, status) => {
    if (error) {
      return jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

router.post(
  '/connect-mercado-pago',
  validator.body(schema.payments.connectMercadoPago),
  async (req, res) => {
    try {
      logger('Connecting Mercado Pago', { fleet_id: req.body.fleet_id })
      const response = await paymentHandler.connectMercadoPago(req.body)

      return jsonResponse(res, responseCodes.OK, null, response)
    } catch (error) {
      logger('Failed to connect to Mercado Pago', errors.errorWithMessage(error))
      return jsonResponse(res, responseCodes.BadRequest, errors.formatErrorForWire(error))
    }
  }
)

module.exports = router
