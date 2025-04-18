const express = require('express')
const platform = require('@velo-labs/platform')

const { auth } = require('./middleware')
const { schema, validator } = require('./validators')
const responseCodes = require('../utils/responseCodes')
const { subscriptions } = require('../handlers/fleet-membership-handlers')

const router = new express.Router()

const { jsonResponse, logger, errors, responseCodes: codes } = platform

router.use(auth.authenticate)

router.get(
  '/',
  validator.query(schema.fleetMemberships.subscriptions.list),
  async (req, res) => {
    try {
      logger('Listing subscriptions', { query: req.query })

      const subscription = await subscriptions.list({
        params: req.query,
        user: req.user
      })

      return jsonResponse(res, codes.OK, null, subscription)
    } catch (error) {
      logger(
        'Failed to list subscriptions',
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
  }
)

router.get(
  '/:subscription_id',
  validator.params(schema.fleetMemberships.subscriptions.retrieve),
  async (req, res) => {
    try {
      logger('Retrieving subscription', { params: req.params })

      const subscription = await subscriptions.retrieve({
        params: req.params,
        user: req.user
      })

      return jsonResponse(res, codes.OK, null, subscription)
    } catch (error) {
      logger(
        'Failed to retrieve subscription',
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
