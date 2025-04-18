'use-strict'

const platform = require('@velo-labs/platform')
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const errors = platform.errors
const logger = platform.logger
const express = require('express')
const router = express.Router()
const taxHandler = require('../model_handlers/tax-handler')
const {
  fleetMemberships: memberships
} = require('../model_handlers/fleet-memberships')
const _ = require('underscore')

router.post('/addTax', (req, res) => {
  taxHandler.createTax(req.body).then((tax) => {
    jsonResponse(res, responseCodes.OK, errors.noError(), tax)
  }).catch((error) => {
    if (error) {
      logger('Error: failed to save tax with error', error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
    }
  })
})

/**
 * Used to get a Tax Details .
 *
 * The request body should include the fleet_id
 *  that the operator has specified.
 */
router.post('/getTax', (req, res) => {
  if (!_.has(req.body.data, 'fleet_id')) {
    logger('Error: Some Parameters are missing in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
  }
  taxHandler.getTaxWithFleetId(req.body.data.fleet_id).then((tax) => {
    if (tax) {
      jsonResponse(res, responseCodes.OK, errors.noError(), tax)
    }
  }).catch((error) => {
    if (error) {
      logger('Error: failed to get Tax with error', error)
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
    }
  })
})

/**
 * Used to delete a particular Tax Details .
 *
 * The request body should include the taxId
 *  that the operator has specified.
 */
router.post('/deactivateTax', (req, res) => {
  if (!_.has(req.body.data, 'taxId')) {
    logger('Error: Some Parameters are missing in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
  }
  taxHandler.deactivateTax(req.body.data.taxId).then((tax) => {
    if (tax) {
      jsonResponse(res, responseCodes.OK, errors.noError(), tax)
    }
  }).catch((error) => {
    if (error) {
      logger('Error: failed to get Tax with error', error)
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
    }
  })
})

/**
 * Used to update a particular Tax Details .
 *
 * The request body should include the taxId
 *  that the operator has specified.
 */
router.post('/updateTax', (req, res) => {
  if (!_.has(req.body, 'tax_id')) {
    logger('Error: Some Parameters are missing in request(tax_id)')
    return jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
  }
  if (!_.has(req.body, 'tax_name')) {
    logger('Error: Some Parameters are missing in request(tax_name)')
    return jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
  }
  if (!_.has(req.body, 'tax_percent')) {
    logger('Error: Some Parameters are missing in request(tax_percent)')
    return jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
  }
  taxHandler.updateTax(req.body).then(async (tax) => {
    if (tax && tax.fleet_id) {
      await memberships.list({ 'fleet_id': tax.fleet_id }).then(async membershipList => {
        if (membershipList) {
          let status = false
          let err
          for (const member of membershipList) {
            let data = { 'membership_incentive': member.membership_incentive,
              'membership_price': member.membership_price,
              'membership_price_currency': member.membership_price_currency,
              'payment_frequency': member.payment_frequency}
            await memberships.update({ 'fleet_membership_id': member.fleet_membership_id }, data).then(membership => {
              status = true
            }).catch((error) => {
              err = error
              status = false
            })
          }
          if (status) {
            jsonResponse(res, responseCodes.OK, errors.noError(), tax)
          } else {
            logger('Error: failed to get calculate Tax with in fleet membership error', err)
            jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(err), null)
          }
        }
      }).catch((error) => {
        logger('Error: failed to get Tax with error', error)
        jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
      })
    }
  }).catch((error) => {
    if (error) {
      logger('Error: failed to get Tax with error', error)
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
    }
  })
})

module.exports = router
