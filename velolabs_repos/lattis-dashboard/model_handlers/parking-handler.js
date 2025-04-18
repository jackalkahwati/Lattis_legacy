'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const query = platform.queryCreator
const sqlPool = platform.sqlPool
const _ = require('underscore')
const S3Handler = platform.S3Handler
const errors = platform.errors
const platformConfig = platform.config
const config = require('./../config')
const dbConstants = platform.dbConstants
const fleetHandler = require('./fleet-handler')
const passwordHandler = require('./../utils/password-handler')
const uploadConstants = require('./../constants/upload-constants')
const db = require('../db')
const Sentry = require('../utils/configureSentry')

/**
 * To get comparison columns and values
 *
 * @param {Object} comparingFields - Incoming fields for comparison.
 * @private
 */
const _comparisonColumnAndValue = (comparingFields) => {
  let comparisonColumnAndValue = {}

  if (_.has(comparingFields, 'parking_spot_id')) {
    comparisonColumnAndValue.parking_spot_id = comparingFields.parking_spot_id
  }
  if (_.has(comparingFields, 'fleet_id')) {
    comparisonColumnAndValue.fleet_id = comparingFields.fleet_id
  }
  if (_.has(comparingFields, 'operator_id')) {
    comparisonColumnAndValue.operator_id = comparingFields.operator_id
  }
  if (_.has(comparingFields, 'customer_id')) {
    comparisonColumnAndValue.customer_id = comparingFields.customer_id
  }
  return comparisonColumnAndValue
}

/**
 * This methods fetches parking-zones that are associated with an request identifier.
 *
 * @param {Object} parkingDetails - Request parameter for identifier.
 * @param {Function} done
 */
const getParkingZones = (parkingDetails, done) => {
  const getParkingZonesQuery = query.selectWithAnd('parking_areas', null, _comparisonColumnAndValue(parkingDetails))
  sqlPool.makeQuery(getParkingZonesQuery, (error, parkingZones) => {
    if (error) {
      Sentry.captureException(error, {parkingDetails})
      logger('Error: making parkingZones query with ', _.keys(_comparisonColumnAndValue(parkingDetails)), ':', getParkingZonesQuery)
      done(errors.internalServer(true), null)
      return
    }
    done(null, parkingZones)
  })
}

/**
 * This methods updates parking-zones that are associated with a request identifier.
 *
 * @param {Object} parkingDetails - Request parameter for identifier that are associated with editing
 * a parking zone
 * @param {Function} done
 */
const editParkingZones = (parkingDetails, done) => {
  const columnsAndValues = {
    'type': parkingDetails.type,
    'geometry': JSON.stringify(parkingDetails.geometry),
    'fleet_id': parkingDetails.fleet_id,
    'name': parkingDetails.name,
    'zone': parkingDetails.zone,
    'operator_id': parkingDetails.operator_id,
    'customer_id': parkingDetails.customer_id
  }
  const editParkingZonesQuery = query.updateSingle('parking_areas', columnsAndValues, {'parking_area_id': parkingDetails.parking_area_id})
  sqlPool.makeQuery(editParkingZonesQuery, (error, parkingZones) => {
    if (error) {
      logger('Error: Updating parkingZones query with ', _.keys(_comparisonColumnAndValue(parkingDetails)), ':', editParkingZonesQuery)
      done(errors.internalServer(true), null)
      return
    }
    done(null, parkingZones)
  })
}

/**
 * This methods deletes parking-zones that are associated with a request identifier.
 *
 * @param {Object} parkingDetails - Request parameter for identifier that are associated with deleting
 * a parking zone
 * @param {Function} done
 */
const deleteParkingZones = (parkingDetails, done) => {
  if (!Array.isArray(parkingDetails.parking_area_id)) {
    parkingDetails.parking_area_id = [parkingDetails.parking_area_id]
  }
  _.each(parkingDetails.parking_area_id, (id, index) => {
    const deleteParkingZoneQuery = query.deleteSingle(dbConstants.tables.parking_areas, {parking_area_id: id})
    sqlPool.makeQuery(deleteParkingZoneQuery, (error) => {
      if (error) {
        Sentry.captureException(error, {parkingDetails})
        logger('Error: Deleting parking Zones for a particular fleet')
        done(errors.internalServer(true), null)
        return
      }
      if (index === parkingDetails.parking_area_id.length - 1) {
        done(errors.noError(), null)
      }
    })
  })
}

/**
 * This methods creates parking-zones that are associated with parking Details.
 *
 * @param {Object} parkingDetails - This Parameter holds information about parking Details that are associated
 * with creating a parking zone
 * @param {Function} done
 */
const createParkingZones = async (parkingDetails, done) => {
  const fleetParkingDetails = await db.main('parking_areas')
    .where('parking_areas.fleet_id', parkingDetails.fleet_id)
    .join('reservation_settings', function () {
      this
        .on('reservation_settings.fleet_id', '=', 'parking_areas.fleet_id')
        .onNull('reservation_settings.deactivation_date')
    })
    .first()
    .options({ nestTables: true })
    .select()

  if (fleetParkingDetails) {
    logger(`Cannot create additional parking spots for fleet ${parkingDetails.fleet_id} with reservations enabled`)

    return done(errors.customError(
      'This fleet has reservations enabled and additional parking spot cannot be added.',
      platform.responseCodes.Conflict,
      'Conflict'
    ))
  }

  const columnsAndValues = {
    'name': parkingDetails.name,
    'type': parkingDetails.type,
    'zone': parkingDetails.zone,
    'geometry': JSON.stringify(parkingDetails.geometry),
    'fleet_id': parkingDetails.fleet_id,
    'operator_id': parkingDetails.operator_id,
    'customer_id': parkingDetails.customer_id
  }
  const createParkingZonesQuery = query.insertSingle('parking_areas', columnsAndValues)
  sqlPool.makeQuery(createParkingZonesQuery, (error, parkingZones) => {
    if (error) {
      Sentry.captureException(error, {parkingDetails})
      logger('Error: inserting parkingZones query with ', _.keys(_comparisonColumnAndValue(parkingDetails)), ':', createParkingZonesQuery)
      done(errors.internalServer(true), null)
      return
    }
    done(null, parkingZones)
  })
}

/**
 * This methods fetches parking-spots that are associated with an request identifier.
 *
 * @param {Object} parkingDetails - Request parameter for identifier.
 * @param {Function} done
 */
const getParkingSpots = (parkingDetails, done) => {
  const getParkingSpotsQuery = query.selectWithAnd('parking_spots', null, _comparisonColumnAndValue(parkingDetails))
  sqlPool.makeQuery(getParkingSpotsQuery, (error, parkingSpots) => {
    if (error) {
      Sentry.captureException(error, {parkingDetails})
      logger('Error: making parkingSpots query with ', _.keys(_comparisonColumnAndValue(parkingDetails)), ':', getParkingSpotsQuery)
      done(errors.internalServer(true), null)
      return
    }
    let finalParkingSpots = _.filter(parkingSpots, (spot) => {
      return spot.type !== 'public'
    })
    done(null, finalParkingSpots)
  })
}

/**
 * This method inserts a parkingSpot for an operator/customer
 *
 * @param {Object} parkingSpotDetail - Contains the details of parking spot to be inserted in table
 * @param {Object} parkingSpotImage - Contains the details of parkingSpotImage to be inserted in s3 and table
 * @param {Function} done - Callback function with error, data params
 */
const createParkingSpots = async (parkingSpotDetail, parkingSpotImage, done) => {
  const fleetParkingDetails = await db.main('parking_spots')
    .where('parking_spots.fleet_id', parkingSpotDetail.fleet_id)
    .join('reservation_settings', function () {
      this
        .on('reservation_settings.fleet_id', '=', 'parking_spots.fleet_id')
        .onNull('reservation_settings.deactivation_date')
    })
    .first()
    .options({ nestTables: true })
    .select()

  if (fleetParkingDetails) {
    logger(`Cannot create additional parking spots for fleet ${parkingSpotDetail.fleet_id} with reservations enabled`)

    return done(errors.customError(
      'This fleet has reservations enabled and additional parking spot cannot be added.',
      platform.responseCodes.Conflict,
      'Conflict'
    ))
  }

  const s3Handler = new S3Handler()
  s3Handler.upload(
    parkingSpotImage,
    uploadConstants.types.parking_spot + '-' + parkingSpotDetail.fleet_id + passwordHandler.randString(10),
    platformConfig.aws.s3.parkingSpots.bucket,
    parkingSpotImage.mimetype, (error, imageData) => {
      if (error) {
        Sentry.captureException(error)
        logger('Error: uploading parking spot image to s3', errors.errorWithMessage(error))
        done(errors.internalServer(true), null)
        return
      }

      const columnsAndValues = {
        name: parkingSpotDetail.name,
        description: parkingSpotDetail.description,
        latitude: parkingSpotDetail.latitude,
        longitude: parkingSpotDetail.longitude,
        fleet_id: parkingSpotDetail.fleet_id,
        type: parkingSpotDetail.type,
        pic: imageData.Location
      }

      if (_.has(parkingSpotDetail, 'operator_id')) {
        columnsAndValues.operator_id = parkingSpotDetail.operator_id
      }
      if (_.has(parkingSpotDetail, 'customer_id')) {
        columnsAndValues.customer_id = parkingSpotDetail.customer_id
      }
      if (_.has(parkingSpotDetail, 'parking_area_id')) {
        columnsAndValues.parking_area_id = parkingSpotDetail.parking_area_id
      }
      const insertParkingSpotsQuery = query.insertSingle('parking_spots', columnsAndValues)
      sqlPool.makeQuery(insertParkingSpotsQuery, (error, status) => {
        if (error) {
          Sentry.captureException(error, {parkingSpotDetail})
          logger('Error: saving new parking spots with query', insertParkingSpotsQuery,
            'error:', errors.errorWithMessage(error))
          done(errors.internalServer(true), null)
          return
        }
        done(null, status)
      })
    })
}

/**
 * This method inserts a parkingSpot for an operator/customer
 *
 * @param {Object} parkingSpotDetails - Contains the details of parking spot to be inserted in table
 * @param {Object} parkingSpotImage - Contains the details of parkingSpotImage to be inserted in s3 and table
 * @param {Function} done - Callback
 */
const editParkingSpots = (parkingSpotDetails, parkingSpotImage, done) => {
  if (!parkingSpotDetails.pic_link) {
    getParkingSpots(_.pick(parkingSpotDetails, 'parking_spot_id'), (error, parkingSpots) => {
      if (error) {
        Sentry.captureException(error, {parkingSpotDetails})
        done(error)
      }
      const s3Handler = new S3Handler()
      const fileName = parkingSpots[0].pic.toString().split(config.s3path +
                platformConfig.aws.s3.parkingSpots.bucket + '/')
      s3Handler.deleteFile(fileName[1], platformConfig.aws.s3.parkingSpots.bucket, (error) => {
        if (error) {
          Sentry.captureException(error, {parkingSpotDetails})
          logger("Error: Failed to delete parking spot's previous image:", fileName, 'from bucket', platformConfig.aws.s3.parkingSpots.bucket)
        }
        s3Handler.upload(
          parkingSpotImage,
          uploadConstants.types.parking_spot + '-' + parkingSpotDetails.fleet_id + passwordHandler.randString(10),
          platformConfig.aws.s3.parkingSpots.bucket,
          parkingSpotImage.mimetype, (error, imageData) => {
            if (error) {
              logger('Error: Uploading parking spot image to s3', errors.errorWithMessage(error))
              done(errors.internalServer(true), null)
              return
            }
            parkingSpotDetails.pic_link = imageData.Location
            _updateParkingSpot(parkingSpotDetails, done)
          })
      })
    })
  } else {
    _updateParkingSpot(parkingSpotDetails, done)
  }
}

/**
 * This method inserts a parkingSpot for an operator/customer
 *
 * @param {Object} parkingSpotDetails - Contains the details of parking spot
 * @param {Function} done - Callback
 * @private
 */
const _updateParkingSpot = (parkingSpotDetails, done) => {
  const columnsAndValues = {
    name: parkingSpotDetails.name,
    description: parkingSpotDetails.description,
    latitude: parkingSpotDetails.latitude,
    longitude: parkingSpotDetails.longitude,
    type: parkingSpotDetails.type,
    pic: parkingSpotDetails.pic_link
  }
  if (_.has(parkingSpotDetails, 'parking_area_id')) {
    columnsAndValues.parking_area_id = parkingSpotDetails.parking_area_id
  }
  if (_.has(parkingSpotDetails, 'capacity')) {
    columnsAndValues.capacity = parkingSpotDetails.capacity
  }
  let insertParkingSpotsQuery = query.updateSingle(dbConstants.tables.parking_spots,
    columnsAndValues,
    {parking_spot_id: parkingSpotDetails.parking_spot_id})
  sqlPool.makeQuery(insertParkingSpotsQuery, (error) => {
    if (error) {
      Sentry.captureException(error, {parkingSpotDetails})
      logger('Error: updating parking spots with query', insertParkingSpotsQuery,
        'error:', errors.errorWithMessage(error))
      done(errors.internalServer(false))
      return
    }
    done(null)
  })
}

/**
 * This methods validates a parking spot object.
 *
 * @param {Object} spotDetails
 * @param {file} files
 * @param {boolean} isNew
 * @returns {boolean}
 */
let validateParkingSpots = (spotDetails, pic, isNew) => {
  return isNew ? (
    (_.has(spotDetails, 'operator_id') || _.has(spotDetails, 'customer_id')) &&
        _.has(spotDetails, 'fleet_id') &&
        _.has(spotDetails, 'latitude') &&
        _.has(spotDetails, 'longitude') &&
        _.has(spotDetails, 'name') &&
        _.has(spotDetails, 'description') &&
        _.has(spotDetails, 'type') &&
        pic
  ) : (
    _.has(spotDetails, 'parking_spot_id') &&
        _.has(spotDetails, 'latitude') &&
        _.has(spotDetails, 'longitude') &&
        _.has(spotDetails, 'name') &&
        _.has(spotDetails, 'description') &&
        _.has(spotDetails, 'type') &&
        (pic || _.has(spotDetails, 'pic_link'))
  )
}

/**
 * This methods deletes parking-spots that are associated with an request identifier.
 *
 * @param {Object} parkingDetails - Request parameter for identifier.
 * @param {Function} done
 */
const deleteParkingSpots = (parkingDetails, done) => {
  if (!Array.isArray(parkingDetails.parking_spot_id)) {
    parkingDetails.parking_spot_id = [parkingDetails.parking_spot_id]
  }
  _.each(parkingDetails.parking_spot_id, (id, index) => {
    const deleteParkingSpotsQuery = query.deleteSingle(dbConstants.tables.parking_spots, {parking_spot_id: id})
    sqlPool.makeQuery(deleteParkingSpotsQuery, (error) => {
      if (error) {
        Sentry.captureException(error, {parkingDetails})
        logger('Error: delete parking spots with error', errors.errorWithMessage(error))
        done(errors.internalServer(true), null)
        return
      }
      if (index === parkingDetails.parking_spot_id.length - 1) {
        done(errors.noError(), null)
      }
    })
  })
}

/**
 * This methods uploads parking-spots that are associated with an request identifier.
 *
 * @param {Object} requestParam - Request parameter for identifier.
 * @param {file} csvFile - csv file path.
 * @param {Function} done
 */
const parkingSpotCsvUpload = (requestParam, csvFile, done) => {
  let columns = ['name', 'description', 'type', 'latitude', 'longitude', 'capacity']
  config.parseCsvUsingColumns(csvFile, columns, (error, csvRecords) => {
    if (error) {
      Sentry.captureException(error, {requestParam})
      logger('Error: Unable to parse CSV columns')
      done(error, null)
    }
    let valueArray = []
    _.each(csvRecords, (record, index) => {
      valueArray.push([record.name, record.description, record.type, record.latitude, record.longitude, record.capacity])
      valueArray[index] = [...valueArray[index], requestParam.operator_id, requestParam.customer_id, requestParam.fleet_id]
    })

    const insertMultipleQuery = query.insertMultiple(dbConstants.tables.parking_spots,
      [...columns, 'operator_id', 'customer_id', 'fleet_id'], valueArray)
    sqlPool.makeQuery(insertMultipleQuery, (error) => {
      if (error) {
        Sentry.captureException(error, {requestParam})
        logger('Error: inserting spots with error', errors.errorWithMessage(error))
        return done(errors.internalServer(true), null)
      }
      done(null, null)
    })
  })
}

/**
 * This methods gets public parking spots that are associated with an request identifier.
 *
 * @param {Object} reqParam - Request parameter for identifier.
 * @param {Function} done
 */
const getPublicParkingSpots = (reqParam, done) => {
  const getParkingSpotsQuery = query.selectWithAnd('parking_spots', null, _.extend(_comparisonColumnAndValue(reqParam), {type: 'public'}))
  sqlPool.makeQuery(getParkingSpotsQuery, (error, parkingSpots) => {
    if (error) {
      Sentry.captureException(error, {reqParam})
      logger('Error: making parkingSpots query with ', _.keys(_comparisonColumnAndValue(reqParam)), ':',
        getParkingSpotsQuery, error)
      done(errors.internalServer(true), null)
      return
    }
    done(null, parkingSpots)
  })
}

/**
 * This methods updates parking settings.
 *
 * @param {Object} reqParam - Request parameter for identifier.
 * @param {Function} done
 */
const setParkingSettings = (reqParam, done) => {
  fleetHandler.getFleetMetaData({fleet_id: reqParam.fleet_id}, (error, fleets) => {
    if (error) {
      Sentry.captureException(error, {reqParam})
      logger('Error: getting fleets with fleet_id', reqParam.fleet_id, ':', error)
      done(errors.internalServer(true), null)
      return
    }

    if (fleets.length === 0) {
      const columnsAndValues = {
        fleet_id: reqParam.fleet_id
      }
      if (Number.isFinite(reqParam.show_parking_zones)) {
        columnsAndValues.show_parking_zones = reqParam.show_parking_zones
      }
      if (Number.isFinite(reqParam.show_parking_spots)) {
        columnsAndValues.show_parking_spots = reqParam.show_parking_spots
      }

      fleetHandler.insertFleetMetadata(columnsAndValues, (error) => {
        if (error) {
          Sentry.captureException(error, {reqParam})
          logger('Error: inserting fleet meta data with error', errors.errorWithMessage(error))
          done(errors.internalServer(true), null)
          return
        }
        done(null, null)
      })
    } else {
      let columnsAndValues = {
        show_parking_zones: Number.isFinite(reqParam.show_parking_zones) ? reqParam.show_parking_zones : fleets[0].show_parking_zones,
        show_parking_spots: Number.isFinite(reqParam.show_parking_spots) ? reqParam.show_parking_spots : fleets[0].show_parking_spots,
        fleet_id: reqParam.fleet_id
      }

      fleetHandler.updateFleetMetaData(_.pick(columnsAndValues, 'show_parking_zones', 'show_parking_spots'), _.pick(columnsAndValues, 'fleet_id'), (error) => {
        if (error) {
          Sentry.captureException(error, {reqParam})
          logger('Error: updating fleet meta data with error', errors.errorWithMessage(error))
          done(errors.internalServer(true), null)
          return
        }
        done(null, null)
      })
    }
  })
}

module.exports = {
  getParkingZones: getParkingZones,
  getParkingSpots: getParkingSpots,
  editParkingZones: editParkingZones,
  createParkingZones: createParkingZones,
  createParkingSpots: createParkingSpots,
  editParkingSpots: editParkingSpots,
  validateParkingSpots: validateParkingSpots,
  deleteParkingSpots: deleteParkingSpots,
  deleteParkingZones: deleteParkingZones,
  parkingSpotCsvUpload: parkingSpotCsvUpload,
  getPublicParkingSpots: getPublicParkingSpots,
  setParkingSettings: setParkingSettings
}
