'use strict'

const express = require('express')
const router = express.Router()
const _ = require('underscore')

const platform = require('@velo-labs/platform')
const logger = platform.logger
const ovalResponse = require('./../utils/oval-response')
const errors = platform.errors
const requestHelper = require('./../helpers/request-helper')
// const userHandler = require('./../handlers/user-handler')
const bikeHandler = require('./../handlers/bike-handler')
const db = require('./../db')

/**
 * Used to fetch accessToken
 *
 */
router.get('/credentials/:lockId', (req, res) => {
  if (!_.has(req.query, 'fleetId')) {
    logger('Error: Some parameters are missing in request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  const details = req.query
  details.lockId = req.params.lockId
  requestHelper.userAuthorizationValidator(req, res, true, async (error, user) => {
    if (error) return
    try {
      const fleet = await db.main('fleets')
        .where({ fleet_id: req.query.fleetId })
        .first()
      const isPublic = fleet && fleet.type.startsWith('public')
      details.userId = user.user_id.toString()
      const privateFleetUser = await db.users('private_fleet_users')
        .where({
          user_id: user.user_id,
          fleet_id: req.query.fleetId
        }).first()
      const userHasAccess = !!(privateFleetUser && privateFleetUser.verified)
      if (!isPublic && !userHasAccess) {
        ovalResponse(res, errors.unauthorizedAccess(true), null)
        return
      }
      const accessToken = await bikeHandler.getTapkeyCredentials(details)
      ovalResponse(res, errors.noError(), accessToken)
    } catch (error) {
      console.error('Failed to get access Token')
      ovalResponse(res, errors.formatErrorForWire(error), null)
    }
  })
})

module.exports = router
