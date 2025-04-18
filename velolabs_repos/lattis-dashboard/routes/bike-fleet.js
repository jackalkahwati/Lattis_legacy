'use strict'

const platform = require('@velo-labs/platform')
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const errors = platform.errors
const logger = platform.logger
const express = require('express')
const router = express.Router()
const _ = require('underscore')
const bikeFleetHandler = require('./../model_handlers/bike-fleet-handler')
const config = platform.config
const db = require('../db')
const { executeCommand, client } = require('../utils/grow')
const { grinIoTDefaultSettings } = require('../constants/bike-constants')
const uploadFile = require('./../utils/file-upload')

/**
 * Used to get cumulative trip statistics for a fleet
 *
 * The request body should include the bike details and image
 */
router.post('/pre-defined-bikes', (req, res) => {
  if (!_.has(req.body, 'customer_id')) {
    logger('Error: No Parameters sent in request')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  bikeFleetHandler.getElectricBikeTypes(
    req.body.customer_id,
    (error, cumulativeTripData) => {
      if (error) {
        jsonResponse(
          res,
          responseCodes.InternalServer,
          errors.internalServer(true),
          null
        )
        return
      }
      jsonResponse(res, responseCodes.OK, errors.noError(), cumulativeTripData)
    }
  )
})

/**
 * Used to get cumulative trip statistics for a fleet
 *
 * The request body should include the bike details and image
 */
router.post('/cumulative-trip-data', (req, res) => {
  if (
    !_.has(req.body, 'fleet_id') &&
    !_.has(req.body, 'operator_id') &&
    !_.has(req.body, 'customer_id')
  ) {
    logger('Error: No Parameters sent in request')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }

  bikeFleetHandler.cumulativeTripStatistics(
    req.body,
    (error, cumulativeTripData) => {
      if (error) {
        jsonResponse(
          res,
          responseCodes.InternalServer,
          errors.internalServer(true),
          null
        )
        return
      }
      jsonResponse(res, responseCodes.OK, errors.noError(), cumulativeTripData)
    }
  )
})

/**
 * Used to Add bike details to the bike fleet
 *
 * The request body should include the bike details and image
 */
router.post('/add-bike-details', uploadFile.upload.single('pic'), (req, res) => {
  if (!bikeFleetHandler.validateNewBikes(req.body)) {
    logger('Error: No Parameters sent in request')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  bikeFleetHandler.insertBikeDetails(req.body, req.file, (error) => {
    if (error) {
      jsonResponse(
        res,
        responseCodes.InternalServer,
        errors.internalServer(true),
        null
      )
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), null)
  })
})

/**
 * Used to get bike details for the bike fleet
 *
 * The request body should include the fleet_id/operator_id/customer_id/fleet_id
 */
router.post('/get-bike-image-and-description', (req, res) => {
  if (
    !_.has(req.body, 'fleet_id') &&
    !_.has(req.body, 'operator_id') &&
    !_.has(req.body, 'customer_id')
  ) {
    logger('Error: No Parameters sent in request')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  bikeFleetHandler.getBikeImageAndDescription(req.body, (error, bikes) => {
    if (error) {
      jsonResponse(
        res,
        responseCodes.InternalServer,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), bikes)
  })
})

/**
 * Used to get Parameters to for all bikes in the bike fleet
 *
 * The request body should include operator_id or customer_id or fleet_id.
 */
router.post('/get-bike-fleet-data', (req, res) => {
  if (
    !_.has(req.body, 'operator_id') &&
    !_.has(req.body, 'customer_id') &&
    !_.has(req.body, 'fleet_id') &&
    !_.has(req.body, 'bike_id')
  ) {
    logger('Parameters not sent to server')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  bikeFleetHandler.getBikeFleetData(req.body, (error, bikeData) => {
    if (error) {
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), bikeData)
  })
})

// Better route to just get bike details
router.get('/get-bike-details/:bikeId', async (req, res) => {
  try {
    const bikeData = await bikeFleetHandler.getBikeDetails(req.params.bikeId)
    return jsonResponse(res, responseCodes.OK, errors.noError(), bikeData)
  } catch (error) {
    jsonResponse(res, responseCodes.InternalServer, errors.customError(
      'An error occurred getting bike details',
      responseCodes.InternalServer,
      'BikeDetailsError',
      true
    ), null)
  }
})

/**
 * Used to get maintenance Categories for sending Suspended Bikes to Workshop
 *
 * res - Response of list of maintenance categories.
 */
router.get('/maintenance-categories', (req, res) => {
  bikeFleetHandler.getMaintenanceCategories((error, maintenanceCategories) => {
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
    jsonResponse(
      res,
      responseCodes.OK,
      errors.noError(),
      maintenanceCategories
    )
  })
})

/**
 * Used to send suspended bikes to workshop
 *
 * The request body should include the operator_id, bike_id, maintenance_notes,maintenance_category and the
 * list of bikes to be suspended that the admin has specified.
 */
router.post('/send-to-service', uploadFile.upload.single('operator_photo'), (req, res) => {
  if (
    !_.has(req.body, 'operator_id') ||
    !_.has(req.body, 'bike_id') ||
    !_.has(req.body, 'notes') ||
    !_.has(req.body, 'fleet_id') ||
    !_.has(req.body, 'category')
  ) {
    logger('Error: no parameters sent in request')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }

  const url = config.mailer.baseUrl ? config.mailer.baseUrl : ''
  bikeFleetHandler.sendToService(
    req.body,
    req.file,
    url,
    (error, returnStatus) => {
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
      jsonResponse(res, responseCodes.OK, errors.noError(), returnStatus)
    }
  )
})

/**
 * Used to delete change the status of the bike in bikeFleet page
 *
 * The request body should include the operator_id,bike_id
 */
router.post('/send-to-archive', (req, res) => {
  try {
    if (
      !_.has(req.body, 'operator_id') ||
      !_.has(req.body, 'bikes') ||
      !_.has(req.body, 'status')
    ) {
      logger('Error:Some Parameters are missing in request')
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.missingParameter(true),
        null
      )
      return
    }
    bikeFleetHandler.sendToArchive(req.body, (error) => {
      if (error) {
        logger(error)
        jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
        return
      }
      jsonResponse(res, responseCodes.OK, errors.noError(), null)
    })
  } catch (err) {
    logger('Error: sending bike to archive', err)
    jsonResponse(
      res,
      err.code ? err.code : responseCodes.InternalServer,
      errors.internalServer(true),
      null
    )
  }
})

/**
 * Used to return a bike to active for the bikeFleet page
 *
 * The request body should include the operator_id,bike_id
 */
router.post('/return-to-active-fleet', (req, res) => {
  if (!_.has(req.body, 'bikes')) {
    logger('Error:Some Parameters are missing in request')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }

  bikeFleetHandler.returnToActiveFleet(req.body, (error) => {
    if (error) {
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), null)
  })
})

/**
 * Used to delete change the status of the bike in bikeFleet page
 *
 * The request body should include the operator_id,bike_id
 */
router.post('/send-to-staging', (req, res) => {
  if (!_.has(req.body, 'operator_id') || !_.has(req.body, 'bikes')) {
    logger('Error:Some Parameters are missing in request')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  bikeFleetHandler.sendToStaging(req.body, (error, updatedStatus) => {
    if (error) {
      logger(error)
      jsonResponse(
        res,
        responseCodes.InternalServer,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), updatedStatus)
  })
})

/**
 * Used to fetch data for a group of bikes
 *
 * The request body should include the operator_id,bike_group_id, customer_id
 */
router.post('/get-bike-group-data', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error:Some Parameters are missing in request')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  bikeFleetHandler.getBikeGroupData(req.body, (error, bikeGroups) => {
    if (error) {
      logger(error)
      jsonResponse(
        res,
        responseCodes.InternalServer,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), bikeGroups)
  })
})

/**
 * Used to update data for a group of bikes
 *
 * The request body should include the operator_id,bike_group_id, customer_id
 */
router.post('/update-bike-group-data', (req, res) => {
  if (
    !_.has(req.body, 'bikes') ||
    !_.has(req.body, 'maintenance_schedule') ||
    !_.has(req.body, 'fleet_id')
  ) {
    logger('Error:Some Parameters are missing in request')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  bikeFleetHandler.updateMaintenanceSchedule(
    req.body,
    (error, updatedStatus) => {
      if (error) {
        logger(error)
        jsonResponse(
          res,
          responseCodes.InternalServer,
          errors.formatErrorForWire(error),
          null
        )
        return
      }
      jsonResponse(res, responseCodes.OK, errors.noError(), updatedStatus)
    }
  )
})

/**
 * Used to get the API key after logging into scout integration.
 *
 * The request body should include the operator_id,bike_group_id, customer_id
 */
router.post('/get-scout-api-key', async (req, res) => {
  try {
    if (!_.has(req.body, 'fleet_id')) {
      logger('Error:Some Parameters are missing in request')
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.missingParameter(true),
        null
      )
      return
    }

    const integrationDetails = await bikeFleetHandler.getScoutApiKey(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), integrationDetails)
  } catch (error) {
    if (error) {
      logger(error)
      jsonResponse(
        res,
        responseCodes.InternalServer,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    logger(`Could not fetch key ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      errors.internalServer(true),
      null
    )
  }
})

/**
 * Used to get the API key after logging into geotab integration.
 *
 * The request body should include the fleet_id
 */
router.post('/get-geotab-api-key', async (req, res) => {
  try {
    if (!_.has(req.body, 'fleet_id')) {
      logger('Error:Some Parameters are missing in request')
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.missingParameter(true),
        null
      )
      return
    }

    const integrationDetails = await bikeFleetHandler.getGeotabApiKey(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), integrationDetails)
  } catch (error) {
    if (error) {
      logger(error)
      jsonResponse(
        res,
        responseCodes.InternalServer,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    logger(`Could not fetch key ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      errors.internalServer(true),
      null
    )
  }
})

/**
 * Used to get activity log
 *
 * The request body should include the operator_id,bike_group_id, customer_id
 */
router.post('/activity-log', (req, res) => {
  if (!_.has(req.body, 'bike_id')) {
    logger('Error:Some Parameters are missing in request')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  bikeFleetHandler.activityLog(req.body.bike_id, (error, activityLog) => {
    if (error) {
      logger(error)
      jsonResponse(
        res,
        responseCodes.InternalServer,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), activityLog)
  })
})

// For lock and unlock depending on action
router.post('/:bikeId/iot/lock', async (req, res) => {
  try {
    if (!_.has(req.params, 'bikeId') && !_.has(req.body, 'action')) {
      logger('Error: some required parameters are missing')
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.missingParameter(true),
        null
      )
      return
    }
    let longPollData
    let action = req.body.action
    let hubType = req.body.hubType
    const controller = await db
      .main('controllers')
      .where({ bike_id: req.params.bikeId })
      .select('vendor', 'key')
      .first()
    if (controller && controller.vendor === 'Grow') {
      if (action === 'lock') {
        const commandCode = 1
        const commandValue = 1
        const command = `${commandCode},${commandValue}`
        client.on('message', function (topic, payload, pkt) {
          if (action === 'lock') {
            longPollData = Buffer.from(payload).toString()
            longPollData = JSON.parse(longPollData)
            const responseCommand = longPollData && longPollData.command
            const responseCode = longPollData && longPollData.response
            if (res.headersSent) return // Don't respond twice
            if (responseCode && [-1, -2, -3].includes(responseCode)) {
              const response = processResponseCodes(responseCode)
              jsonResponse(
                res,
                responseCodes.BadRequest,
                { message: `Error locking Grow device:${response}` },
                null
              )
            } else {
              if (commandCode === responseCommand) {
                action = undefined
                longPollData = undefined
                return jsonResponse(res, responseCodes.OK, errors.noError(), {
                  message: `Grow device ${controller.key} locked successfully`
                })
              }
            }
          }
        })
        executeCommand(`s/c/${controller.key}`, `39,${grinIoTDefaultSettings.lockedHeartBeatInterval}`, (err, message) => {
          if (err) {
            logger(`Error setting Grow heartbeat interval on lock ${controller.key}`)
          }
        })
        executeCommand(`s/c/${controller.key}`, command, (err, message) => {
          if (err) {
            logger(`Error locking Grow ${controller.key}`)
            return jsonResponse(
              res,
              responseCodes.InternalServer,
              `Unable to unlock Grow device ${controller.key}`,
              null
            )
          }
        })
      } else if (action === 'unlock') {
        const commandCode = 1
        const commandValue = 0
        const command = `${commandCode},${commandValue}`
        client.on('message', function (topic, payload, pkt) {
          longPollData = Buffer.from(payload).toString()
          longPollData = JSON.parse(longPollData)
          const responseCommand = longPollData && longPollData.command
          const responseCode = longPollData && longPollData.response
          if (res.headersSent) return // Don't respond twice
          if (responseCode && [-1, -2, -3].includes(responseCode)) {
            const response = processResponseCodes(responseCode)
            jsonResponse(
              res,
              responseCodes.BadRequest,
              { message: `Error locking Grow device:${response}` },
              null
            )
          } else {
            if (commandCode === responseCommand) {
              longPollData = undefined
              jsonResponse(res, responseCodes.OK, errors.noError(), {
                message: `Grow device ${controller.key} unlocked successfully`
              })
            }
          }
        })
        executeCommand(`s/c/${controller.key}`, `39,${grinIoTDefaultSettings.heartBeatInterval}`, (err, message) => {
          if (err) {
            logger(`Error setting Grow heartbeat interval on unlock ${controller.key}`)
          }
        })
        executeCommand(`s/c/${controller.key}`, command, (err, message) => {
          if (err) {
            logger(`Error unlocking Grow ${controller.key}`)
            return jsonResponse(
              res,
              responseCodes.InternalServer,
              `Unable to unlock Grow device ${controller.key}`,
              null
            )
          }
        })
      }
    } else {
      const iot = await bikeFleetHandler.lockOrUnlockBike(
        req.params.bikeId,
        action,
        hubType,
        req.body.state
      )
      jsonResponse(res, responseCodes.OK, errors.noError(), iot)
    }
  } catch (error) {
    if (error.constructor.name === 'LattisError') {
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    logger(`Failed to lock bike: ${error.message}`)
    const payload = error.message ? { message: error.message } : errors.internalServer(true)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      payload,
      true
    )
  }
})

router.post('/:bikeId/iot/cover', async (req, res) => {
  try {
    if (!_.has(req.params, 'bikeId')) {
      logger('Error: some required parameters are missing')
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.missingParameter(true),
        null
      )
      return
    }
    const iot = await bikeFleetHandler.openCover(req.params.bikeId)
    jsonResponse(res, responseCodes.OK, errors.noError(), iot)
  } catch (error) {
    if (error.constructor.name === 'LattisError') {
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    logger(`Failed to open cover: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      errors.internalServer(true),
      null
    )
  }
})

const processResponseCodes = (responseCode) => {
  switch (responseCode) {
    case 0:
      return 'Success'
    case -1:
      return 'Processing Error'
    case -2:
      return 'Parameter Error'
    case -3:
      return 'Unknown Command Error'
    default:
      return 'Unknown response code'
  }
}

router.post('/:bikeId/iot', async (req, res) => {
  try {
    // TODO: Rework on this QR code thing
    // if (!_.has(req.body, 'iot_key') || !_.has(req.body, 'qr_code')) {
    if (!_.has(req.body, 'iot_key')) {
      logger('Error: some required parameters are missing')
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.missingParameter(true),
        null
      )
      return
    }
    const controller = await db
      .main('controllers')
      .where({ key: req.body.iot_key })
      .first()
    if (controller && +controller.bike_id !== +req.params.bikeId) {
      throw errors.customError(
        `The controller is already assigned to vehicle ID ${controller.bike_id}`,
        platform.responseCodes.BadRequest,
        'BadRequest',
        false
      )
    }
    let longPollData
    const isEdittingQRCode = req.body.isEditingQRCode
    const isEdittingControllerKey = req.body.isEdittingControllerKey
    const isAddingControllerKey = req.body.isAddingControllerKey
    const moduleType = req.body.iot_module_type
    const autogenerateQRCode = req.body.autogenerateQRCode && isEdittingQRCode
    const clientIotKey = req.body.iot_key
    const clientTopic = `s/a/17/${clientIotKey}`
    if (
      moduleType === 'Grow' &&
      (autogenerateQRCode || isEdittingControllerKey || isAddingControllerKey)
    ) {
      client.subscribe(clientTopic)
      const command = 222
      const timeout = setTimeout(() => {
        if (res.headersSent) return
        return jsonResponse(
          res,
          responseCodes.BadRequest,
          { message: `Took too long to get IoT response` },
          null
        )
      }, 5000)
      client.on('message', async function (topic, payload, pkt) {
        longPollData = Buffer.from(payload).toString()
        longPollData = JSON.parse(longPollData)
        const responseCommand = longPollData.command
        const commandResponse = longPollData.response

        if (res.headersSent) return // Don't respond twice
        if (commandResponse && [-1, -2, -3].includes(commandResponse)) {
          const response = processResponseCodes(commandResponse)
          jsonResponse(
            res,
            responseCodes.BadRequest,
            { message: `Error connecting Grow IoT:${response}` },
            null
          )
        } else if (clientTopic === topic && responseCommand === command) {
          clearTimeout(timeout)
          if (autogenerateQRCode) req.body.qr_code = longPollData.QR && longPollData.QR === 'FFFF' ? null : longPollData.QR // 'FFFF' is the default invalid QR code. Don't use it.
          const iot = await bikeFleetHandler.updateIot(
            req.params.bikeId,
            _.pick(req.body, [
              'iot_key',
              'bike_name',
              'qr_code',
              'iot_module_type', // Generally module_type/controller type since it can be a tracker too
              'operation' // either 'adding' a new ctrl or 'updating' an existing ctrl on a bike
            ]),
            true // Skip live status check for auto-generated QR code
          )
          longPollData = undefined
          if (res.headersSent) return
          jsonResponse(res, responseCodes.OK, errors.noError(), iot)
        }
      })
      executeCommand(`s/c/${req.body.iot_key}`, `${command}`, (err, message) => {
        if (err) {
          logger(`Error adding Grow device ${req.body.iot_key}`)
          jsonResponse(
            res,
            responseCodes.InternalServer,
            { message: `Unable to add Grin IoT ${req.body.iot_key}` },
            null
          )
        }
      })
    } else {
      const iot = await bikeFleetHandler.updateIot(req.params.bikeId, req.body)
      jsonResponse(res, responseCodes.OK, errors.noError(), iot)
    }
  } catch (error) {
    if (error.constructor.name === 'LattisError') {
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    logger(`Failed to update bike IoT details: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      error.message || errors.internalServer(true),
      null
    )
  }
})

router.post('/:bikeId/bikeDetails', async (req, res) => {
  try {
    if (!_.has(req.body, 'bike_name') || !_.has(req.body, 'qr_code')) {
      logger('Error: some required parameters are missing')
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.missingParameter(true),
        null
      )
      return
    }
    const bike = await bikeFleetHandler.updateBikeDetails(req.params.bikeId, req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), bike)
  } catch (error) {
    if (error.constructor.name === 'LattisError') {
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    logger(`Failed to update vehicle details: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      error || errors.internalServer(true),
      null
    )
  }
})

router.get('/tapkey-key/lock-id', async (req, res) => {
  try {
    const filter = req.query.filter
    const fleetId = req.query.fleetId
    const filterEntity = req.query.filterEntity
    const response = await bikeFleetHandler.getTapkeyLockId(filterEntity, filter, fleetId)
    jsonResponse(res, responseCodes.OK, errors.noError(), response)
  } catch (error) {
    logger(`Error getting lock details: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      error.message || errors.internalServer(true),
      null
    )
  }
})
router.get('/fleet-data/:fleetId', async (req, res) => {
  try {
    const fleetData = await bikeFleetHandler.getFleetData(req.params)
    jsonResponse(res, responseCodes.OK, errors.noError(), fleetData)
  } catch (error) {
    logger(`An error occurred fetching fleet data: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      errors.internalServer(true),
      null
    )
  }
})

router.post('/delete-fleet-domains', async (req, res) => {
  if (!_.has(req.body, 'fleetId')) {
    logger('Error: some parameters are missing in the request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  try {
    const memberAccessResponse = await bikeFleetHandler.deleteFleetDomains(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), memberAccessResponse)
  } catch (error) {
    logger(`Failed to delete fleet domains: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      errors.internalServer(true),
      null
    )
  }
})
router.post('/update-image', uploadFile.upload.single('pic'), async (req, res) => {
  try {
    const updateResponse = await bikeFleetHandler.updateBikeImage(req.body, req.file)
    jsonResponse(res, responseCodes.OK, errors.noError(), updateResponse)
  } catch (error) {
    logger(`Failed to update bike image: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      errors.internalServer(true),
      null
    )
  }
})

router.post('/update-description', async (req, res) => {
  try {
    const updateResponse = await bikeFleetHandler.updateBikeDescription(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), updateResponse)
  } catch (error) {
    logger(`Failed to update bike description: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      errors.internalServer(true),
      null
    )
  }
})

router.post('/delete-controller', async (req, res) => {
  if (!_.has(req.body, 'key') || !_.has(req.body, 'bike_id')) {
    logger('Error: some parameters are missing in the request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  try {
    const deleteResponse = await bikeFleetHandler.deleteController(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), deleteResponse)
  } catch (error) {
    logger(`Failed to delete controller: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.Forbidden,
      error.message || errors.internalServer(true),
      error
    )
  }
})

router.post('/grow/settings', async (req, res) => {
  try {
    const growDetails = req.body.growDetails
    const response = await bikeFleetHandler.updateGrowFleetSettings(
      growDetails
    )
    jsonResponse(res, responseCodes.OK, errors.noError(), response)
  } catch (error) {
    if (error.constructor.name === 'LattisError') {
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    logger(`Error saving Grow settings: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      error.message || errors.internalServer(true),
      null
    )
  }
})

router.post('/segway/settings', async (req, res) => {
  try {
    const segwayDetails = req.body.segwayDetails
    const response = await bikeFleetHandler.updateSegwayFleetSettings(
      segwayDetails
    )
    jsonResponse(res, responseCodes.OK, errors.noError(), response)
  } catch (error) {
    if (error.constructor.name === 'LattisError') {
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    logger(`Error saving Grow settings: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      error.message || errors.internalServer(true),
      null
    )
  }
})

router.post('/acton/settings', async (req, res) => {
  try {
    const actonDetails = req.body.ACTONDetails
    const response = await bikeFleetHandler.updateACTONFleetSettings(
      actonDetails
    )
    jsonResponse(res, responseCodes.OK, errors.noError(), response)
  } catch (error) {
    if (error.constructor.name === 'LattisError') {
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    logger(`Error saving ACTON settings: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      error.message || errors.internalServer(true),
      null
    )
  }
})

// Unify saving of integration settings to one route
router.post('/settings', async (req, res) => {
  try {
    const settings = req.body.settings
    const response = await bikeFleetHandler.saveIntegrationSettings(settings)
    jsonResponse(res, responseCodes.OK, errors.noError(), response)
  } catch (error) {
    logger(`Error saving integration settings: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      error.message || errors.internalServer(true),
      null
    )
  }
})

// Get the command status of a linka command (lock / unlock)
router.get('/command-status/:commandId', async (req, res) => {
  try {
    if (!_.has(req.params, 'commandId')) {
      logger('Error: commandId is required')
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.missingParameter(true),
        null
      )
      return
    }
    const commandId = req.params.commandId
    const commandStatus = await bikeFleetHandler.getLinkaCommandStatus(commandId)
    jsonResponse(res, responseCodes.OK, errors.noError(), commandStatus)
  } catch (error) {
    if (error.constructor.name === 'LattisError') {
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    logger(`Error fetching command status: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      error.message || errors.internalServer(true),
      null
    )
  }
})

// Get all integrations for a fleet
router.get('/integrations/:fleetId', async (req, res) => {
  try {
    if (!_.has(req.params, 'fleetId')) {
      logger('Error: some required parameters are missing(fleetId)')
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.missingParameter(true),
        null
      )
      return
    }
    const fleetId = req.params.fleetId
    const response = await bikeFleetHandler.getFleetIntegrations(fleetId)
    jsonResponse(res, responseCodes.OK, errors.noError(), response)
  } catch (error) {
    if (error.constructor.name === 'LattisError') {
      jsonResponse(
        res,
        responseCodes.BadRequest,
        errors.formatErrorForWire(error),
        null
      )
      return
    }
    logger(`Error getting fleeting integrations: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      error.message || errors.internalServer(true),
      null
    )
  }
})

module.exports = router
