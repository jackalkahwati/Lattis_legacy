const express = require('express')
const {
  jsonResponse,
  logger,
  errors,
  responseCodes: codes
} = require('@velo-labs/platform')

const { auth } = require('./middleware')
const { schema, validator } = require('./validators')
const responseCodes = require('../utils/responseCodes')
const { promotions } = require('../handlers/promotions')

const router = new express.Router()

router.use(auth.authenticate)

router.get('/', validator.query(schema.promotions.list), async (req, res) => {
  try {
    logger('Listing promotions', { user: req.user.user_id, query: req.query })

    const promos = await promotions.list({
      user: req.user,
      fleetId: req.query.fleet_id,
      includeClaimed: req.query.include_claimed
    })

    return jsonResponse(res, codes.OK, null, promos)
  } catch (error) {
    logger(
      'Failed to list promotions',
      { user: req.user.user_id, query: req.query },
      'Error:',
      errors.errorWithMessage(error)
    )

    return jsonResponse(
      res,
      responseCodes.statusCodeFromError(error),
      errors.formatErrorForWire(error)
    )
  }
})

router.patch(
  '/:promo_code/redeem',
  validator.params(schema.promotions.redeem),
  async (req, res) => {
    try {
      logger('Redeeming promo_code', { params: req.params })

      const promotion = await promotions.redeem({
        promoCode: req.params.promo_code,
        user: req.user
      })

      return jsonResponse(res, codes.OK, null, promotion)
    } catch (error) {
      logger(
        'Failed to redeem promo code',
        { user: req.user.user_id, params: req.params },
        'Error:',
        errors.errorWithMessage(error)
      )

      return jsonResponse(
        res,
        responseCodes.statusCodeFromError(error),
        errors.formatErrorForWire(error)
      )
    }
  }
)

module.exports = router
