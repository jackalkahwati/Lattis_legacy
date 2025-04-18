'use strict'

const platform = require('@velo-labs/platform')
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const errors = platform.errors
const logger = platform.logger
const express = require('express')
const router = express.Router()
const _ = require('underscore')
const reportsHandler = require('./../model_handlers/reports-handler')

/**
 * Used to get fleetUtilisation to display the reports on dashboard page.
 *
 * The request body should include the operator_id and month
 * that the operator has specified.
 */
router.post('/fleet-utilization', async (req, res) => {
  if (!_.has(req.body, 'fleetId') || !_.has(req.body, 'month') || !_.has(req.body, 'year')) {
    logger('Error: missing parameter in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  try {
    const tripsReport = await reportsHandler.generateFleetUtilizationReport(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), tripsReport)
  } catch (error) {
    logger(error)
    jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
  }
})

/**
 * Used to get reports of member for an operator
 *
 * The request body should include the fleet_id and month that an operator has specified
 */
router.post('/member-reports', (req, res) => {
  if (!_.has(req.body, 'fleet_id') || !_.has(req.body, 'month') || !_.has(req.body, 'year')) {
    logger('Error: missing parameter in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  reportsHandler.memberReports(req.body, (error, updateStatus) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), updateStatus)
  })
})

/*
 * Used to generate trip reports
 */
router.post('/trips', async (req, res) => {
  if (!_.has(req.body, 'fleetId') || !_.has(req.body, 'month') || !_.has(req.body, 'year')) {
    logger('Error: missing parameter in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  try {
    const tripsReport = await reportsHandler.generateTripsReport(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), tripsReport)
  } catch (error) {
    logger(error)
    jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
  }
})

/**
 * Used to get inventory to display the reports on dashboard page.
 *
 * The request body should include the operator_id and month
 * that the operator has specified.
 */
router.post('/inventory-reports', (req, res) => {
  if (!_.has(req.body, 'fleet_id') || !_.has(req.body, 'month') || !_.has(req.body, 'year')) {
    logger('Error: no operator id sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  reportsHandler.inventoryReportsUtilisation(req.body, (error, fleetList) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), fleetList)
  })
})

/**
 * Used to get maintenanceReportUtilisation to display the reports on dashboard page.
 *
 * The request body should include the operator_id and month
 * that the operator has specified.
 */
router.post('/maintenance-utilisation', (req, res) => {
  if (!_.has(req.body, 'fleet_id') || !_.has(req.body, 'month') || !_.has(req.body, 'year')) {
    logger('Error: Missing Parameters')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  reportsHandler.maintenanceReportsUtilisation(req.body, (error, fleetList) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), fleetList)
  })
})

/**
 * Used to get Revenue Reports to display the reports on dashboard page.
 *
 * The request body should include the operator_id and month
 * that the operator has specified.
 */
router.post('/revenue-reports', function (req, res) {
  if (!_.has(req.body, 'fleet_id') || !_.has(req.body, 'month') || !_.has(req.body, 'year')) {
    logger('Error: no operator id/month sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  reportsHandler.revenueReports(req.body, function (error, fleetList) {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), fleetList)
  })
})

/**
 * Used to get trip Reports to display the reports on dashboard page.
 *
 * The request body should include the operator_id and month
 * that the operator has specified.
 */
router.post('/location-reports', function (req, res) {
  if (!_.has(req.body, 'fleet_id') || !_.has(req.body, 'month') || !_.has(req.body, 'year')) {
    logger('Error: no operator id/month sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  reportsHandler.locationReports(req.body, function (error, fleetList) {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), fleetList)
  })
})

module.exports = router
