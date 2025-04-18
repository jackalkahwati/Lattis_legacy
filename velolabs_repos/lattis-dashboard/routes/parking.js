'use strict'

const platform = require('@velo-labs/platform')
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const errors = platform.errors
const logger = platform.logger
const express = require('express')
const router = express.Router()
const _ = require('underscore')
const parkingHandler = require('./../model_handlers/parking-handler')
const uploadFile = require('./../utils/file-upload')

/**
 * Used to Get Parking Zones to fleet
 *
 * The request body should include fleet_id or operator_id
 */
router.post('/get-parking-zones', (req, res) => {
  if (!_.has(req.body, 'fleet_id') && !_.has(req.body, 'operator_id') && !_.has(req.body, 'customer_id')) {
    logger('Error: failed to get parking-zones. Required parameter not sent')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  parkingHandler.getParkingZones(req.body, (error, parkingZones) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(), parkingZones)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), parkingZones)
  })
})

/**
 * Used to Edit Parking Zones for fleet
 *
 * The request body should include fleet_id ,operator_id/customer_id, type, zone, geometry and zone_id
 */
router.post('/edit-parking-zones', (req, res) => {
  if (!_.has(req.body, 'fleet_id') || (!_.has(req.body, 'operator_id') && !_.has(req.body, 'customer_id')) ||
        !_.has(req.body, 'type') || !_.has(req.body, 'geometry') || !_.has(req.body, 'parking_area_id') ||
        !_.has(req.body, 'name')) {
    logger('Error: failed to edit parking-zones. Required parameter not sent')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  parkingHandler.editParkingZones(req.body, (error, parkingZones) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), parkingZones)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), parkingZones)
  })
})

/**
 * Used to Edit Parking Spots for fleet
 *
 * The request body should include parking_spot_id with other details to be changed
 */
router.post('/edit-parking-spots', uploadFile.upload.single('pic'), (req, res) => {
  if (!parkingHandler.validateParkingSpots(req.body, req.file, false)) {
    logger('Error: Missing Parameters in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  parkingHandler.editParkingSpots(req.body, req.file, (error) => {
    if (error) {
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), null)
  })
})

/**
 * Used to Create Parking Zones for fleet
 *
 * The request body should include fleet_id ,operator_id/customer_id, type, geometry
 */
router.post('/create-parking-zones', (req, res) => {
  if (!_.has(req.body, 'fleet_id') || (!_.has(req.body, 'operator_id') && !_.has(req.body, 'customer_id')) ||
        !_.has(req.body, 'type') || !_.has(req.body, 'geometry') || !_.has(req.body, 'name')) {
    logger('Error: failed to create parking-zones. Required parameter not sent')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  parkingHandler.createParkingZones(req.body, (error, parkingZones) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), parkingZones)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), parkingZones)
  })
})

/**
 * Used to Get Parking Spots to fleet
 *
 * The request body should include fleet_id or operator_id
 */
router.post('/get-parking-spots', (req, res) => {
  if (!_.has(req.body, 'fleet_id') && !_.has(req.body, 'operator_id') && !_.has(req.body, 'customer_id')) {
    logger('Error: failed to get parking-zones. Required parameter not sent')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  parkingHandler.getParkingSpots(req.body, (error, parkingSpots) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), parkingSpots)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), parkingSpots)
  })
})

/**
 * Used to Create parking spots to the parking
 *
 * The request body should include the operator_id
 */
router.post('/create-parking-spots', uploadFile.upload.single('pic'), (req, res) => {
  if (!parkingHandler.validateParkingSpots(req.body, req.file, true)) {
    logger('Error: Missing Parameters in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }

  parkingHandler.createParkingSpots(req.body, req.file, (error, status) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to delete parking spots for a fleet
 *
 * The request body should include the operator_id
 */
router.post('/delete-parking-spots', (req, res) => {
  if (!_.has(req.body, 'parking_spot_id')) {
    logger('Error: Required Parameter missing')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }

  parkingHandler.deleteParkingSpots(req.body, (error) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), null)
  })
})

/**
 * Used to Delete Parking Zones for fleet
 *
 * The request body should include parking_area_id for a zone
 */
router.post('/delete-parking-zones', (req, res) => {
  if (!_.has(req.body, 'parking_area_id')) {
    logger('Error: Required parameter missing in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  parkingHandler.deleteParkingZones(req.body, (error, parkingZones) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(), parkingZones)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), parkingZones)
  })
})

/**
 * Used to upload Parking Spots for fleet
 *
 * The request body should include parking_area_id for a zone
 */
router.post('/upload-parking-spots', uploadFile.upload.single('spot_csv'), (req, res) => {
  if (!req.file ||
        !_.has(req.body, 'fleet_id') ||
        !_.has(req.body, 'operator_id') ||
        !_.has(req.body, 'customer_id')) {
    logger('Error: no member csv file sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  parkingHandler.parkingSpotCsvUpload(req.body, req.file, (error, status) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(), status)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to Get public Parking Spots to fleet
 *
 * The request body should include fleet_id or operator_id
 */
router.post('/get-public-parking-spots', (req, res) => {
  if (!_.has(req.body, 'fleet_id') && !_.has(req.body, 'operator_id') && !_.has(req.body, 'customer_id')) {
    logger('Error: failed to get parking-zones. Required parameter not sent')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  parkingHandler.getPublicParkingSpots(req.body, (error, parkingSpots) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), parkingSpots)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), parkingSpots)
  })
})

/**
 * Used to Get public Parking Spots to fleet
 *
 * The request body should include fleet_id or operator_id
 */
router.post('/set-parking-settings', (req, res) => {
  if (!_.has(req.body, 'fleet_id') && !_.has(req.body, 'show_parking_zones') && !_.has(req.body, 'show_parking_spots')) {
    logger('Error: failed to get parking-zones. Required parameter not sent')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  parkingHandler.setParkingSettings(req.body, (error, parkingSpots) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), parkingSpots)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), parkingSpots)
  })
})

module.exports = router
