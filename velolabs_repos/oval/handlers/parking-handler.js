'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const errors = platform.errors
const queryCreator = platform.queryCreator
const sqlPool = platform.sqlPool
const dbConstants = require('./../constants/db-constants')
const _ = require('underscore')
const fleetHandler = require('./fleet-handler')
const { configureSentry } = require('../utils/configureSentry')

const Sentry = configureSentry()

/**
 * The method retrieves parking spots
 *
 * @param {Object} comparisonColumnAndValue
 * @param {function} callback
 */
const getParkingSpots = (comparisonColumnAndValue, callback) => {
  const query = queryCreator.selectWithAnd(
    dbConstants.tables.parking_spots,
    null,
    comparisonColumnAndValue
  )
  sqlPool.makeQuery(query, callback)
}

/**
 * The method retrieves parking spots for fleets
 *
 * @param {Object} requestParam
 * @param {function} callback
 */
const getParkingSpotsForFleet = (requestParam, callback) => {
  fleetHandler.getFleetMetaData(
    { fleet_id: requestParam.fleet_id },
    (error, fleetMetaData) => {
      if (error) {
        Sentry.captureException(error, { requestParam })
        logger(
          'Error: could not get fleet Meta data Failed with error:',
          error
        )
        callback(errors.internalServer(false), null)
        return
      }
      if (
        fleetMetaData.length === 0 ||
        (_.has(fleetMetaData[0], 'show_parking_spots') &&
          fleetMetaData[0].show_parking_spots > 0)
      ) {
        getParkingSpots(
          { fleet_id: requestParam.fleet_id },
          (error, parkingSpots) => {
            if (error) {
              Sentry.captureException(error, { requestParam })
              logger(
                'Error: could not get parkingSpots Failed with error:',
                error
              )
              callback(errors.internalServer(false), null)
              return
            }
            callback(null, { parking_spots: parkingSpots })
          }
        )
      } else {
        callback(null, { parking_spots: [] })
      }
    }
  )
}

/**
 * The method retrieves parking Zones
 *
 * @param {Object} comparisonColumnAndValue
 * @param {function} callback
 */
const getParkingZones = (comparisonColumnAndValue, callback) => {
  const query = queryCreator.selectWithAnd(
    dbConstants.tables.parking_areas,
    null,
    comparisonColumnAndValue
  )
  sqlPool.makeQuery(query, callback)
}

/**
 * The method retrieves parking Zones for fleets
 *
 * @param {Object} requestParam
 * @param {function} callback
 */
const getParkingZonesForFleet = (requestParam, callback) => {
  fleetHandler.getFleetMetaData(
    { fleet_id: requestParam.fleet_id },
    (error, fleetMetaData) => {
      if (error) {
        Sentry.captureException(error, { requestParam })
        logger(
          'Error: could not get fleet Meta data Failed with error:',
          error
        )
        callback(errors.internalServer(false), null)
        return
      }
      if (
        fleetMetaData.length === 0 ||
        (_.has(fleetMetaData[0], 'show_parking_zones') &&
          fleetMetaData[0].show_parking_zones > 0)
      ) {
        getParkingZones(
          { fleet_id: requestParam.fleet_id },
          (error, parkingZones) => {
            if (error) {
              Sentry.captureException(error, { requestParam })
              logger(
                'Error: could not get parkingZones Failed with error:',
                error
              )
              callback(errors.internalServer(false), null)
              return
            }
            _.each(parkingZones, (parkingZone, index) => {
              parkingZones[index].geometry = JSON.parse(parkingZone.geometry)
            })
            callback(null, { parking_zones: parkingZones })
          }
        )
      } else {
        callback(null, { parking_zones: [] })
      }
    }
  )
}

module.exports = Object.assign(module.exports, {
  getParkingSpots: getParkingSpots,
  getParkingSpotsForFleet: getParkingSpotsForFleet,
  getParkingZonesForFleet: getParkingZonesForFleet
})
