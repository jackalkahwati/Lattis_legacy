const express = require('express')

const {
  logger,
  errors,
  jsonResponse,
  responseCodes: codes
} = require('@velo-labs/platform')

const { schema, validator } = require('../validators')
const { promotions } = require('../model_handlers/promotions')
const { statusCodeFromError } = require('../utils/responseCodes')

const router = express.Router()

router.post(
  '/',
  validator.body(schema.promotions.createPromotionSchema),
  async (req, res) => {
    try {
      logger('Creating promotion:', req.body)

      const response = await promotions.create(req.body)

      return jsonResponse(res, codes.OK, null, response)
    } catch (error) {
      logger(
        'Failed to create promotion',
        req.body,
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
  '/',
  validator.query(schema.promotions.listPromotionsSchema),
  async (req, res) => {
    try {
      logger('Listing promotions:', req.query)

      const response = await promotions.list(req.query)

      return jsonResponse(res, codes.OK, null, response)
    } catch (error) {
      logger(
        'Failed to list promotions',
        req.query,
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

router.patch(
  '/:promotion_id/deactivate',
  validator.params(schema.promotions.deactivatePromotionSchema),
  async (req, res) => {
    try {
      logger('Deactivating promotion:', req.params)

      const response = await promotions.deactivate(req.params)

      return jsonResponse(res, codes.OK, null, response)
    } catch (error) {
      logger(
        'Failed to deactivate promotion',
        req.params,
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
