const express = require('express')
const router = express.Router()
const db = require('../db')
const platform = require('@velo-labs/platform')
const logger = platform.logger
const ticketHandler = require('./../model_handlers/ticket-handler')
const config = platform.config
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const errors = platform.errors
const sendPushNotification = require('../utils/firebase-messaging')
const { authWithAPIKey } = require('../utils/auth')
const _ = require('underscore')
const { sendUpdateTicketRequestToGPSService } = require('../utils/gpsService')
const uploadFile = require('./../utils/file-upload')

// Remove keys with undefined or null values
function cleanObject (obj) {
  for (const propName in obj) {
    if (obj[propName] === null || obj[propName] === undefined) {
      delete obj[propName]
    }
  }
  return obj
}

// Create ticket from GPS service
router.post('/create-ticket', authWithAPIKey, uploadFile.upload.single('photo'), (req, res) => {
  if (!ticketHandler.validateTicket(req.body)) {
    logger('Error: Some Parameters are missing in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }

  const url = config.mailer.baseUrl ? config.mailer.baseUrl : ''

  ticketHandler.createTicket(req.body, req.file, url, (error, ticket) => {
    if (error) {
      logger('Error: failed to create Ticket with error', error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), ticket)
  })
})

// resolve ticket from GPS service
router.post('/resolve-ticket', authWithAPIKey, (req, res) => {
  if (!_.has(req.body, 'ticket_id')) {
    logger('Error: Some Parameters are missing in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  ticketHandler.resolveTicket(req.body, async (error) => {
    if (error) {
      logger('Error: failed to resolve ticket with error', error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    try {
      await sendUpdateTicketRequestToGPSService(req.body.ticket_id)
      jsonResponse(res, responseCodes.OK, errors.noError(), null)
    } catch (error) {
      jsonResponse(res, responseCodes.InternalServer, error.message || errors.internalServer(true), null)
    }
  })
})

// Route to handle and send notifications on Omni is manually locked
router.post('/omni-lock', authWithAPIKey, async (req, res) => {
  try {
    let data = req.body[0] // Will be at index 0
    res.status(200).json({ 'msg': 'Request received...' })
    if (!data) return
    if (data && data.ignitionOn) return // Do nothing on unlocking
    const ctrl = await db.main('controllers').where({ key: data.IMEI }).first()
    if (ctrl) {
      const trip = await db.main('trips').where({ bike_id: ctrl.bike_id }).orderBy('trip_id', 'DESC').first()
      if (trip && trip.date_endtrip) {
        logger('The vehicle has no ongoing trips.')
        return
      }
      if (trip.device_token) {
        const message = {
          data: {
            trip_id: `${trip.trip_id}`,
            action: 'omni_vehicle_locked_manually'
          }
        }

        const options = {
          'contentAvailable': true,
          'priority': 'high'
        }
        sendPushNotification(trip.device_token, message, options)
        logger(`Info::PN send for Omni lock for trip ${trip.trip_id}`)
      }
    }
  } catch (error) {
    res.status(500).json({ msg: 'An error occurred implementing manual lock on omni' })
  }
})

router.patch('/bikes', authWithAPIKey, async (req, res, next) => {
  try {
    let data = req.body.data
    const updates = []
    for (let record of data) {
      const position = record.position
      let bikeUpdates = {
        bike_battery_level: record.bikeBattery
      }
      bikeUpdates = cleanObject(bikeUpdates)
      let controllerUpdates = {
        gps_log_time: record.statusUtcTime,
        battery_level: record.batteryIoT
      }
      controllerUpdates = cleanObject(controllerUpdates)
      const vehicle = await db.main('bikes').where({ bike_id: record.bikeId }).first()
      if (vehicle) {
        const vehicleUUID = vehicle.bike_uuid
        if (vehicleUUID) {
          const isVehicleDocked = await db.main('ports').where({ vehicle_uuid: vehicleUUID }).first()
          if (!isVehicleDocked) { // If vehicle is not docked, update it's position otherwise, let it keep using the hub position
            bikeUpdates = { ...bikeUpdates, ...position }
            controllerUpdates = { ...controllerUpdates, ...position }
          }
        } else {
          bikeUpdates = { ...bikeUpdates, ...position }
          controllerUpdates = { ...controllerUpdates, ...position }
        }
      }
      bikeUpdates = cleanObject(bikeUpdates)
      controllerUpdates = cleanObject(controllerUpdates)
      if (bikeUpdates.gpsUtcTime) delete bikeUpdates.gpsUtcTime
      if (controllerUpdates.gpsUtcTime) delete controllerUpdates.gpsUtcTime
      if (Object.keys(bikeUpdates).length) {
        const updateBike = db.main('bikes').update(bikeUpdates).where({ bike_id: record.bikeId })
        updates.push(updateBike)
      }
      if (Object.keys(controllerUpdates).length) {
        const updateController = db.main('controllers').update(controllerUpdates).where({ key: record.controllerKey })
        updates.push(updateController)
      }
    }
    Promise.all(updates).then(() => {
      console.log('We are done here updating bikes')
    })
    res.json({ msg: 'Updates in Progress...' })
  } catch (error) {
    logger('Error::', error.message || error)
    res.status(500).json({ error: true, msg: error.message || 'Oops! Something failed!!' })
  }
})

module.exports = router
