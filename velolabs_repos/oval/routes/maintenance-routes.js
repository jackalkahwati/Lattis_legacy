'use strict'

const express = require('express')
const router = express.Router()
const _ = require('underscore')
const platform = require('@velo-labs/platform')
const logger = platform.logger
const ovalResponse = require('./../utils/oval-response')
const errors = platform.errors
const maintenanceHandler = require('./../handlers/maintenance-handler')
const requestHelper = require('./../helpers/request-helper')

/**
 * Used to upload damage report when a bike undergoes maintenance before/after a trip
 *
 * The request body should include the image
 */
router.post('/upload-damage-report', (req, res) => {
  if (!_.has(req.body, 'bike_id') || !_.has(req.body, 'fleet_id') || !_.has(req.body, 'operator_id')) {
    logger('Failed to upload damage report. Missing parameter in request body.')
    res.send(errors.missingParameter(true))
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error) => {
    if (error) return
    maintenanceHandler.damageReportUploads(req.body, (error, imageUrl) => {
      if (error) {
        res.send(error)
        return
      }
      res.send(imageUrl)
    })
  })
})

/**
 * Used to create damage report when a bike undergoes maintenance before/after a trip
 *
 * The request body should include the damage_type, maintenance_notes, bike_id, operator_id and fleet_id
 */
router.post('/create-damage-report', (req, res) => {
  if (!_.has(req.body, 'image') || !_.has(req.body, 'notes') ||
    !_.has(req.body, 'bike_id') || (!_.has(req.body, 'category') && !_.has(req.body, 'damage_type'))) {
    logger('Error: Some Parameters not sent in request: maintenanceRoutes')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    req.body.damage_type = req.body.category
    req.body.category = 'damage_reported'
    maintenanceHandler.createDamageReport(req.body, (error) => {
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), null)
    })
  })
})

/**
 * Used to report theft
 *
 * The request body should include the bike_id
 */
router.put('/report-bike-theft', (req, res) => {
  if (!_.has(req.body, 'bike_id')) {
    logger('Error: Some Parameters not sent in request: Report-Bike-Theft routes')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    maintenanceHandler.theftReported(req.body, (error) => {
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), null)
    })
  })
})

module.exports = router
