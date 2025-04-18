'use strict'

const express = require('express')
const router = express.Router()
const _ = require('underscore')
const platform = require('@velo-labs/platform')
const logger = platform.logger
const ovalResponse = require('./../utils/oval-response')
const errors = platform.errors
const requestHelper = require('./../helpers/request-helper')
const userHandler = require('./../handlers/user-handler')

/**
 * Used to get information to display in an apps help section
 *
 * The request headers should include the user-agent http header
 */
router.get('/info', (req, res) => {
  if (!_.has(req.headers, 'user-agent')) {
    logger('Error: No Parameters sent in request: getApplicationData')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    let appType = req.headers['user-agent']
    userHandler.getFleetInfo(_.pick(user, 'user_id'), (error, payload) => {
      if (error) {
        logger('Error: getting fleet history for user with user_id:', user.user_id,
          'with error:', errors.errorWithMessage(error))
        return
      }

      if (payload && payload.fleet) {
        const appInfo = {
          email: payload.fleet.contact_email,
          phone_number: payload.fleet.contact_phone,
          weblink: payload.fleet.contact_web_link
        }
        ovalResponse(res, errors.noError(), appInfo)
      } else {
        userHandler.getApplicationData(appType, (error, appInfo) => {
          if (error) {
            ovalResponse(res, errors.formatErrorForWire(error), null)
            return
          }
          ovalResponse(res, errors.noError(), appInfo)
        })
      }
    })
  })
})

module.exports = router
