'use strict'

const fs = require('fs')

const _ = require('underscore')
const express = require('express')
const { responseCodes, jsonResponse, errors, logger } = require('@velo-labs/platform')

const fleetHandler = require('../../model_handlers/fleet-handler')
const gpsService = require('../../utils/gpsService')
const { validator, schema } = require('../../validators')
const uploadFile = require('../../utils/file-upload')

const router = express.Router()

/**
 * Used to get customerList to display on the addFleet page.
 *
 * The request body should include the operatorId and the
 * List of customers that the operator has specified.
 */
router.post('/customer-list', (req, res) => {
  if (!_.has(req.body, 'operator_id')) {
    logger('Error: no operator id sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.getCustomerList(req.body.operator_id, (error, customerList) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), customerList)
  })
})

/**
 * Used to details of a particular fleet
 * The request body should include the
 * fleet_id.
 */
router.post('/get-fleet-by-id', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Some Parameters not passed to server')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  fleetHandler.getFleetWithFleetId(req.body.fleet_id, (error, fleet) => {
    if (error) {
      logger(error)
      jsonResponse(
        res,
        responseCodes.InternalServer,
        errors.internalServer(true),
        null
      )
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), fleet)
  })
})

/**
 * Used to get on-call details of operator
 * The request body should include the
 * fleet_id that the operator has specified.
 */
router.post('/get-on-call-operator', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: Missing Parameter')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.getOnCallOperatorDetails(req.body, (error, operatorList) => {
    if (error) {
      logger('Error: retrieving on-call operator with', error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), operatorList)
  })
})

/**
 * Used to add domain details of fleet for either operator / customer
 * The request body should include the
 * fleet_id, operator_id or customer_id that the operator/customer has specified.
 */
router.post('/add-domain', (req, res) => {
  if (!_.has(req.body, 'fleet_id') || (!_.has(req.body, 'operator_id') && !_.has(req.body, 'customer_id')) ||
    req.body.domain_name.length === 0) {
    logger('Error: Some Parameters are missing in this request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.saveDomainDetailsForFleet(req.body, (error) => {
    if (error) {
      logger('Error: Saving Domain name for fleet with fleet_id', req.body.fleet_id, ':', error)
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), null)
  })
})

/**
 * Used to add domain details of fleet for either operator / customer
 * The request body should include the
 * fleet_id, operator_id or customer_id that the operator/customer has specified.
 */
router.post('/get-domain', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: Some Parameters are missing in this request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.getDomains(req.body, (error, operatorList) => {
    if (error) {
      logger('Error: Fetching Domain name for fleet with fleet_id', req.body.fleet_id, ':', error)
      jsonResponse(res, responseCodes.InternalServer, error, null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), operatorList)
  })
})

/**
 * Used to delete domain of fleet for either operator / customer
 * The request body should include the
 * fleet_id, operator_id or customer_id that the operator/customer has specified.
 */
router.post('/delete-domain', (req, res) => {
  if (!_.has(req.body, 'domain_id') || !_.has(req.body, 'fleet_id')) {
    logger('Error: Some Parameters are missing in this request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.deleteDomainForFleet(req.body, (error) => {
    if (error) {
      logger('Error: Deleting Domain name for fleet with domain_id', req.body.domain_id, ':', error)
      jsonResponse(res, responseCodes.InternalServer, error, null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), null)
  })
})

/**
 * Used to update on-call details of operator
 * The request body should include the operatorId and the
 * List of customers that the operator has specified.
 */
router.post('/update-on-call-operator', (req, res) => {
  if (!_.has(req.body, 'fleet_id') || !_.has(req.body, 'operator_id')) {
    logger('Error: Missing Parameter')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.UpdateOnCallOperatorDetails(req.body, (error) => {
    if (error) {
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), null)
  })
})

router.post('/update-acl', (req, res) => {
  if (!_.has(req.body, 'fleet_id') || !_.has(req.body, 'operator_id') || !_.has(req.body, 'role')) {
    logger('Error: Missing Parameter')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  const operator = {
    fleet_id: req.body.fleet_id,
    operator_id: req.body.operator_id
  }

  fleetHandler.UpdateACL(req.body.role, operator, (error) => {
    if (error) {
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), null)
  })
})

/**
 * Used to checkFleetName to display on the addFleet page.
 *
 * The request body should include the operatorId,FleetName and the
 * List of customers that the operator has specified.
 */
router.post('/check-fleet-name', (req, res) => {
  if (!_.has(req.body, 'operator_id') || !_.has(req.body, 'fleet_name')) {
    logger('Error: Some Parameters are missing in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.checkFleetName(req.body.fleet_name, (error, fleets) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), fleets)
  })
})

/**
 * Used to checkCustomerName to display on the addFleet page.
 *
 * The request body should include the operatorId,customerName,etc
 */
router.post('/check-customer-name', (req, res) => {
  if (!_.has(req.body, 'customer_name')) {
    logger('Error: Some Parameters are missing in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.checkCustomerName(req.body, (error, customers) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), customers)
  })
})

/**
 * Used to store values for an existing customer in the addFleet page.
 *
 * The request body should include the operatorId,title,firstName,lastName,countryCode,phoneNumber,email,address
 *  that the operator has specified.
 */
router.post('/update-fleet', (req, res) => {
  if (!fleetHandler.validateNewFleet(req.body, req.files, 'existing_customer')) {
    logger('Error: Some Parameters are missing in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }

  fleetHandler.updateFleetDetails(req.body, req.files, (error, status) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to edit existing fleet details in the fleetInfo page.
 *
 * The request body should include the operatorId,title,firstName,lastName,countryCode,phoneNumber,email,address
 *  that the operator has specified.
 */
router.post('/edit-fleet-info', (req, res) => {
  if (!fleetHandler.validateNewFleet(req.body, req.files, 'edit_fleet_info')) {
    logger('Error: Some Parameters are missing in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }

  fleetHandler.updateFleetDetails(req.body, req.files, (error, status) => {
    if (error) {
      if (error.message === 'Duplicate fleet name') {
        jsonResponse(res, responseCodes.Conflict, errors.errorWithMessage(error), null)
      } else {
        jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      }
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to update a fleets profile picture.
 *
 * The request body should include uploaded image file
 *  that the operator provided.
 */
router.post('/update-logo', uploadFile.upload.single('pic'), async (req, res) => {
  try {
    const updateResponse = await fleetHandler.updateFleetLogo(req.body, req.file)
    jsonResponse(res, responseCodes.OK, errors.noError(), updateResponse)
  } catch (error) {
    logger(`Failed to update fleet profile picture: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      errors.internalServer(true),
      null
    )
  }
})

/**
 * Used to store values for a new customer in the addFleet page.
 *
 * The request body should include the operatorId,title,customerName,firstName,lastName,countryCode,
 * phoneNumber,email,address and the operator has specified.
 */
router.post('/add-fleet', uploadFile.upload.single('fleet_logo'), (req, res) => {
  if (!fleetHandler.validateNewFleet(req.body, req.file, 'new_customer')) {
    logger('Error: Some Parameters are missing in new customer request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.insertFleetDetails(req.body, req.file, (error, status) => {
    if (error) {
      logger('Error inserting customer:', error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to update fleet settings
 *
 * The request body should include the fleet_id
 */
router.post('/update-fleet-settings', (requestParam, response) => {
  if (!_.has(requestParam.body, 'fleet_id')) {
    logger('Error: Required parameters not sent in request')
    jsonResponse(response, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.updateFleetSettings(requestParam.body, (error) => {
    if (error) {
      logger(error)
      jsonResponse(response, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(response, responseCodes.OK, errors.noError(), null)
  })
})

/**
 * Used to get fleet settings
 *
 * The request body should include the fleet_id
 */
router.post('/get-fleet-settings', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: Required parameters not sent in request')
    return jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
  }
  fleetHandler.getFleetSettings(req.body, (error, fleetDetail) => {
    if (error) {
      return jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), fleetDetail)
  })
})

/**
 * Used to get fleet Statistics data for the Dashboard page
 *
 * The request body should include the operatorId
 */
router.post('/fleet-statistics', (requestParam, res) => {
  if (!_.has(requestParam.body, 'operator_id') && !_.has(requestParam.body, 'customer_id') && !_.has(requestParam.body, 'fleet_id')) {
    logger('Error: Required parameters not sent in request')
    return jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
  }

  fleetHandler.getFleetStatistics(requestParam.body, (error, trips) => {
    if (error) {
      return jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), trips)
  })
})

/**
 * Used to get all the dashboard data includes fleets, geofences and its location.
 *
 * The request body should include the operatorId and the
 * region that the operator wants to search.
 */
router.post('/get-operator-data', (requestParam, response) => {
  if (!_.has(requestParam.body, 'operator_id')) {
    logger('Error: Some Parameters are missing in request')
    jsonResponse(response, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }

  fleetHandler.getOperatorData(requestParam.body.operator_id, (error, fleetInfo) => {
    if (error) {
      logger(error)
      jsonResponse(response, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(response, responseCodes.OK, errors.noError(), fleetInfo)
  })
})

/**
 * Used to get fleetList to display on the addFleet page.
 *
 * The request body should include the operator_id and the
 * List of fleets that the operator has specified.
 */
router.post('/fleet-list', (req, res) => {
  if (!_.has(req.body, 'operator_id')) {
    logger('Error: Missing parameter in the request for fleet-list route')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.getFleetList(req.body, (error, fleetList) => {
    if (error) {
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), fleetList)
  })
})

/**
 * Used to get dummy alert list data.
 *
 */
router.get('/get-alert-list', (req, res) => {
  fs.readFile('./files/alerts.json', (err, data) => {
    if (err) {
      console.log(err)
    } else {
      const obj = JSON.parse(data)
      res.send(obj)
    }
  })
})

/**
 * Used to create stripe profile to each fleet linked to operator
 *
 * The request body should include the operator_id , fleet_id , exp_month , exp_year , cc_no ,cvc
 */

router.post('/create-fleet-payment-profile', (req, res) => {
  if (!_.has(req.body, 'operator_id') || !_.has(req.body, 'fleet_id') || !_.has(req.body, 'exp_month') || !_.has(req.body, 'exp_year') || !_.has(req.body, 'cvc') || !_.has(req.body, 'cc_no')) {
    logger('Parameter Missing : operator_id not being sent to Server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }

  fleetHandler.createFleetPaymentProfile(req.body, (error, status) => {
    if (error) {
      logger(error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to get account status of fleet
 *
 * The request body should include the fleet_id
 */

router.post('/get-account-status', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Parameter Missing : fleet_id is missing')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.getFleetsWithPayment({ fleet_id: req.body.fleet_id }, (error, status) => {
    if (error) {
      logger(error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to create billing address of each fleet
 *
 * The request body should include the operator_id , fleet_id , exp_month , exp_year , cc_no ,cvc
 */

router.post('/create-fleet-billing-address', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Parameter Missing : fleet_id is missing')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.createFleetBillingAddress(req.body, (error, status) => {
    if (error) {
      logger('Billingaddress : error in creating billing address')
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to get billing address of each fleet
 *
 * The request body should include the  fleet_id
 */
router.post('/get-fleet-billing-address', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Parameter Missing : fleet_id is missing')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.getFleetBillingAddress(req.body.fleet_id, (error, status) => {
    if (error) {
      logger(error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to update billing address of each fleet
 *
 * The request body should include the  fleet_id
 */
router.post('/update-fleet-billing-address', (req, res) => {
  if (!_.has(req.body, 'address_id')) {
    logger('Parameter Missing : address_id is missing')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.updateFleetBillingAddress(req.body, (error, status) => {
    if (error) {
      logger(error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to get billing cc card of  fleet
 *
 * The request body should include the  fleet_id
 */
router.post('/get-cards', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Parameter Missing : fleet_id not being sent to Server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.getCards(null, { fleet_id: req.body.fleet_id }, (error, status) => {
    if (error) {
      logger(error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to get billing cc card of  fleet
 *
 * The request body should include the  fleet_id
 */
router.post('/edit-cards', (req, res) => {
  if (!_.has(req.body, 'fleet_id') || !_.has(req.body, 'card_id')) {
    logger('Parameter Missing : fleet_id not being sent to Server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  fleetHandler.updateCards(req.body, function (error, status) {
    if (error) {
      logger(error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to get fleet invoice links
 *
 * The request body should include the  fleet_id
 */

router.post('/get-fleet-invoice', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Parameter Missing : fleet_id not being sent to Server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  const columnsToSelect = [
    'link_to_statement',
    'link_to_invoice',
    'invoice_due_date',
    'created_date',
    'payment_status',
    'amount'
  ]
  fleetHandler.getFleetInvoice(columnsToSelect, { fleet_id: req.body.fleet_id }, (error, status) => {
    if (error) {
      logger(error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

router.post(
  '/billing-cycle-fleet',
  validator.body(schema.fleetPaymentSettings.fleetPaymentSettings),
  (req, res) => {
    if (!_.has(req.body, 'fleet_id')) {
      logger('Parameter Missing : fleet_id not being sent to Server')
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.missingParameter(true),
        null
      )
      return
    }
    fleetHandler.billingCycleFleet(req.body, (error, status) => {
      if (error) {
        logger(error)
        jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
        return
      }
      jsonResponse(res, responseCodes.OK, errors.noError(), status)
    })
  }
)

router.post('/create-geofence', async (req, res) => {
  const params = ['fleet_id', 'name', 'geometry']
  for (const param of params) {
    if (!_.has(req.body, param)) {
      logger('Error: failed to create geofence. Required parameter not sent')
      jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
      return
    }
  }
  try {
    const geofence = await fleetHandler.createGeofence(_.pick(req.body, params))
    jsonResponse(res, responseCodes.OK, errors.noError(), geofence)
  } catch (error) {
    jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
  }
})

router.get('/geofences', async (req, res) => {
  if (!_.has(req.query, 'fleet_id')) {
    logger('Error: failed to create geofence. Required parameter not sent')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  try {
    const geofences = await fleetHandler.getGeofences(req.query.fleet_id)
    jsonResponse(res, responseCodes.OK, errors.noError(), geofences)
  } catch (error) {
    jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
  }
})

router.patch('/geofences/status/:fleetId', async (req, res) => {
  try {
    const update = await fleetHandler.updateGeofencesStatus(req.params.fleetId, req.body.status)
    await gpsService.sendUpdateGeofencesRequestToGPSService(req.params.fleetId, req.body.status)
    jsonResponse(res, responseCodes.OK, errors.noError(), update)
  } catch (error) {
    jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
  }
})

router.patch('/geofences/:geofenceId', async (req, res) => {
  const updatableParams = ['name', 'geometry']
  const hasUpdatableParams = updatableParams.some(param => !!req.body[param])
  if (!hasUpdatableParams) {
    logger('Error: failed to create geofence. Required parameter not sent')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  try {
    const geofences = await fleetHandler.updateGeofence(req.params.geofenceId, _.pick(req.body, updatableParams))
    await gpsService.sendUpdateGeofenceRequestToGPSService(req.params.geofenceId, _.pick(req.body, updatableParams))
    jsonResponse(res, responseCodes.OK, errors.noError(), geofences)
  } catch (error) {
    jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
  }
})

router.delete('/geofences/:geofenceId', async (req, res) => {
  try {
    const geofences = await fleetHandler.deleteGeofence(req.params.geofenceId)
    await gpsService.sendDeleteGeofenceRequestToGPSService(req.params.geofenceId)
    jsonResponse(res, responseCodes.OK, errors.noError(), geofences)
  } catch (error) {
    console.log(error)
    jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
  }
})

module.exports = router
