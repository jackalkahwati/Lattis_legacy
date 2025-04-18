const express = require('express')

const {
  logger,
  errors,
  jsonResponse,
  responseCodes: codes
} = require('@velo-labs/platform')

const { schema, validator } = require('../validators')
const { statusCodeFromError } = require('../utils/responseCodes')

const { reservations } = require('../model_handlers/reservations')

const router = express.Router()

router.get(
  '/',
  validator.query(schema.reservations.listQuery),
  async (req, res) => {
    try {
      logger('Listing vehicle reservations ', { query: req.query })

      const response = await reservations.list(req.query)

      return jsonResponse(res, codes.OK, null, response)
    } catch (error) {
      logger(
        'Failed to list reservations',
        { query: req.query },
        'Error: ',
        errors.errorWithMessage(error)
      )

      return jsonResponse(
        res,
        statusCodeFromError(error),
        errors.formatErrorForWire(error)
      )
    }
  }
)

router.get(
  '/:reservation_id',
  validator.params(schema.reservations.retrieveParams),
  async (req, res) => {
    try {
      logger('Retrieving reservation', { params: req.params })

      const response = await reservations.retrieve(req.params)

      return jsonResponse(res, codes.OK, null, response)
    } catch (error) {
      logger(
        'Failed to retrieve reservation',
        { params: req.params },
        'Error: ',
        errors.errorWithMessage(error)
      )

      return jsonResponse(
        res,
        statusCodeFromError(error),
        errors.formatErrorForWire(error)
      )
    }
  }
)

module.exports = router
