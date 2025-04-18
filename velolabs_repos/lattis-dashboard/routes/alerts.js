'use strict'

const platform = require('@velo-labs/platform')
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const express = require('express')
const router = express.Router()
const alertsHandler = require('./../model_handlers/alerts-handler')

/**
 * Gets all alerts in the database to return to user.
 *
 * TODO: update this route as it is currently only set up for testing purposes.
 */
router.post('/all-alerts', function (req, res) {
  alertsHandler.getAllAlerts(function (error, alerts) {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, error, null)
      return
    }

    jsonResponse(res, responseCodes.OK, null, alerts)
  })
})

module.exports = router
