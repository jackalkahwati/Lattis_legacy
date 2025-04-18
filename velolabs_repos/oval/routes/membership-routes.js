const express = require('express')
const platform = require('@velo-labs/platform')

const { auth } = require('./middleware')
const { schema, validator } = require('./validators')
const responseCodes = require('../utils/responseCodes')
const { memberships } = require('../handlers/fleet-membership-handlers')
const bikeHandler = require('../handlers/bike-handler')

const router = new express.Router()

const { jsonResponse, logger, errors, responseCodes: codes } = platform

router.use(auth.authenticate)

router.post(
  '/:membership_id/subscribe',
  validator.params(schema.fleetMemberships.memberships.subscribe),
  async (req, res) => {
    try {
      logger('Subscribing to fleet membership:', {
        user: req.user.user_id,
        params: req.params
      })

      const subscription = await memberships.subscribe({
        params: req.params,
        user: req.user
      })

      return jsonResponse(res, codes.EntryCreated, null, subscription)
    } catch (error) {
      logger(
        'Failed to subscribe user to fleet membership plan',
        { user: req.user.user_id, body: req.body },
        'Error:',
        errors.errorWithMessage(error)
      )
      if (error.status === 409) {
        return jsonResponse(
          res,
          409,
          errors.customError('Card expired', 409, 'CardExpired', true)
        )
      } else {
        return jsonResponse(
          res,
          responseCodes.statusCodeFromError(error),
          errors.formatErrorForWire(error)
        )
      }
    }
  }
)

router.patch(
  '/:membership_id/unsubscribe',
  validator.params(schema.fleetMemberships.memberships.unsubscribe),
  async (req, res) => {
    try {
      logger('Unsubscribing from subscription', { params: req.params })

      const subscription = await memberships.unsubscribe({
        params: req.params,
        user: req.user
      })

      return jsonResponse(res, codes.OK, null, subscription)
    } catch (error) {
      logger(
        'Failed to unsubscribe user from subscription',
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

router.get(
  '/:membership_id',
  validator.params(schema.fleetMemberships.memberships.retrieve),
  async (req, res) => {
    try {
      logger('Retrieving fleet membership plan:', {
        user: req.user.user_id,
        params: req.params
      })

      const membership = await memberships.retrieve({
        params: req.params,
        user: req.user
      })

      return jsonResponse(res, codes.OK, null, membership)
    } catch (error) {
      logger(
        'Failed to retrieve fleet membership plan',
        { user: req.user.user_id, body: req.body },
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
  '/',
  validator.query(schema.fleetMemberships.memberships.list),
  async (req, res) => {
    let fleetMemberships
    const appType = req.headers['user-agent']
    const whiteLabelFleetIds = appType ? await bikeHandler.getAppFleets(appType) : []
    try {
      logger('Listing memberships', { query: req.query, user: req.user.id })

      const membershipsList = await memberships.list({
        params: req.query,
        user: req.user
      })

      if (whiteLabelFleetIds.length && membershipsList.length) {
        fleetMemberships = membershipsList.filter(member => whiteLabelFleetIds.includes(member.fleet.fleet_id))
      }

      const membeships = fleetMemberships || membershipsList
      return jsonResponse(res, codes.OK, null, membeships)
    } catch (error) {
      logger(
        'Failed to list memberships',
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

module.exports = router
