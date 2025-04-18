'use strict'
// @ts-check

const platform = require('@velo-labs/platform')
const _ = require('underscore')
const moment = require('moment')
const logger = platform.logger
const pointHandler = platform.pointHandler
const sqlPool = platform.sqlPool
const query = platform.queryCreator
const responseCodes = platform.responseCodes
const dbConstants = platform.dbConstants
const errors = platform.errors
const db = require('./../db')
const config = require('./../config')
const tripConstants = require('./../constants/trip-constants')
const LockFormatter = require('./../helpers/lock-formatter')
const lockHandler = require('./lock-handler')
const bookingHandler = require('./booking-handler')
const fleetHandler = require('./../handlers/fleet-handler')
const parkingHandler = require('./../handlers/parking-handler')
const fleetConstants = require('../constants/fleet-constants')
const bikeConstants = require('./../constants/bike-constants')
const ticketConstants = require('./../constants/ticket-constants')
const maintenanceConstants = require('./../constants/maintenance-constants')
const privateAccountHandler = require('./../handlers/private-account-handler')
const ticketHandler = require('./../handlers/ticket-handler')
const maintenanceHandler = require('./../handlers/maintenance-handler')
const lockFormatter = new LockFormatter()
const util = require('util')
const { SegwayApiClient } = require('../helpers/segway-api')
const { ActonAPI, OmniAPI, OmniIoTApiClient, TeltonikaApiClient, OkaiAPIClient } = require('../helpers/acton-api')
const { GeotabApiClient } = require('../helpers/geotab-api')
const { KuhmuteAPI } = require('../helpers/kuhmute-api')
const { LinkaApiClient } = require('../helpers/linka')
const { fetchTapKeyConfiguration, TapKeyApiClient } = require('../helpers/tapkey-api')
const comoduleApi = require('../helpers/comodule-api')
const reservationHelpers = require('./reservation-handlers/helpers')
const { promotions } = require('./promotions')
const { getLatestDataForController, getDeviceConfig } = require('../helpers/nimbelink-api')
const fleetAccess = require('../helpers/manage-fleet-access')
const pricing = require('./pricing')
const { getParameterStoreValue } = require('../utils/parameterStore')
const controllerDetails = require('../utils/assign-controller-keys')
const { setUpApiUser, retrievePort, getHubById } = require('./hubs-handler')
const { DucktApiClient } = require('../helpers/duckt-api')
const { ParcelHiveApiClient } = require('../helpers/parcel-hive-api')
const { Kisi } = require('../helpers/kisi')
const { fetchSasConfig, SasApiClient } = require('../helpers/sas-api')
const Sentinel = require('../helpers/sentinel')
const Greenriders = require('../helpers/greenriders')
const { EdgeApiClient } = require('../helpers/edge-api')
const envConfig = require('../envConfig')
const { configureSentry } = require('../utils/configureSentry')

const Sentry = configureSentry()

const encryptionHandler = platform.encryptionHandler

const segwayApiClientForUs = new SegwayApiClient({
  baseUrl: envConfig('SEGWAY_API_URL'),
  clientId: envConfig('SEGWAY_CLIENT_ID'),
  clientSecret: envConfig('SEGWAY_CLIENT_SECRET')
})

const segwayApiClientForEu = new SegwayApiClient({
  baseUrl: envConfig('SEGWAY_EU_API_URL'),
  clientId: envConfig('SEGWAY_EU_CLIENT_ID'),
  clientSecret: envConfig('SEGWAY_EU_CLIENT_SECRET')
})

const actonFamily = {
  'ACTON': ActonAPI,
  'Omni IoT': OmniIoTApiClient,
  'Teltonika': TeltonikaApiClient,
  'Omni Lock': OmniAPI,
  'Okai': OkaiAPIClient
}

let GeotabApi
let LinkaApi
let DucktApi
let parcelHiveApiClient

/** This method queries the electric bikes table
 *
 * @param {Object} columnsAndValues - columns and values to choose
 * @param {Object} comparisonColumnAndValues - filter columns and values
 * @param {Function} callback
 */
const getElectricBikes = (columnsAndValues, comparisonColumnAndValues, callback) => {
  const electricBikesQuery = query.selectWithAnd(dbConstants.tables.electric_bikes, columnsAndValues, comparisonColumnAndValues)
  sqlPool.makeQuery(electricBikesQuery, callback)
}

/**
 * This method gets the user details from integrations
 *
 * @param {Object} bike - filter parameters
 */
const setUpGeotabApiUser = async (bike) => {
  let user = {}
  const geotabApiKeys = await db.main('integrations')
    .where('fleet_id', bike.fleet_id)
    .where('integration_type', 'geotab')
    .select()
  if (geotabApiKeys) {
    user.userName = geotabApiKeys[0].email
    user.password = encryptionHandler.decryptDbValue(geotabApiKeys[0].api_key)
    user.sessionId = geotabApiKeys[0].session_id
    user.serverName = JSON.parse(geotabApiKeys[0].metadata).server_name
    user.database = JSON.parse(geotabApiKeys[0].metadata).database
  }
  return user
}

/**
 * This method gets the user details from integrations
 *
 * @param {Object} bike - filter parameters
 */
const setUpLinkaApiClient = async (bike) => {
  let merchant = {}
  const [linkaApiKeys] = await db
    .main('integrations')
    .where('fleet_id', bike.fleet_id)
    .where('integration_type', 'linka')
    .select()
  if (!_.isEmpty(linkaApiKeys)) {
    merchant.baseUrl = envConfig('LINKA_API_URL')
    merchant.merchantUrl = envConfig('LINKA_MERCHANT_URL')
    merchant.secretKey = JSON.parse(linkaApiKeys.metadata).secretKey
    merchant.apiKey = JSON.parse(linkaApiKeys.metadata).apikey
  }
  return merchant
}

/** This method updates the electric bikes table
 *
 * @param {Object} columnsAndValues - columns and values to choose
 * @param {Object} comparisonColumnAndValues - filter columns and values
 * @param {Function} callback
 */
const updateElectricBike = async (columnsAndValues, comparisonColumnAndValues, callback) => {
  await db.main('bikes').update(columnsAndValues).where(comparisonColumnAndValues)
  callback(null)
  // const updateElectricBikesQuery = query.updateSingle(dbConstants.tables.electric_bikes, columnsAndValues, comparisonColumnAndValues)
  // sqlPool.makeQuery(updateElectricBikesQuery, callback)
}

/** This method updates the metadata
 *
 * @param {Object} metadata - includes battery levels of bike and lock
 * @param {Function} callback
 */
const updateMetadata = (metadata, callback, sentryObj) => {
  if (metadata.bike_id) {
    _updateBikeBattery(metadata, (error) => {
      if (error) {
        if (sentryObj) {
          sentryObj.captureException(error, {metadata})
        }
        callback(error)
        return
      }
      _updateLockMetadata(metadata, callback, sentryObj)
    }, sentryObj)
  } else {
    _updateLockMetadata(metadata, callback, sentryObj)
  }
}

/** This method updates bike metadata
 *
 * @param {Object} metadata - includes battery level of bike
 * @param {Function} callback
 * @private
 */
const _updateBikeBattery = (metadata, callback, sentryObj) => {
  if (Number.isFinite(metadata.bike_battery_level)) {
    getElectricBikes(null, { bike_id: metadata.bike_id }, (error, electricBikes) => {
      if (error) {
        if (sentryObj) {
          sentryObj.captureException(error, {metadata})
        }
        logger('Error: updating metadata. Failed to get bike details', error)
        callback(errors.internalServer(false))
        return
      }
      if (electricBikes.length === 0) {
        logger('No bikes available with', metadata.bike_id)
        callback(errors.internalServer(false))
        return
      }
      updateElectricBike({ bike_battery_level: metadata.bike_battery_level },
        { bike_id: metadata.bike_id }, (error) => {
          if (error) {
            if (sentryObj) {
              sentryObj.captureException(error, {metadata})
            }
            logger('Error: updating metadata. Failed to update bike battery level', error)
            callback(errors.internalServer(false))
            return
          }
          callback(null)
        })
    })
  } else {
    callback(null)
  }
}

/** This method updates lock metadata
 *
 * @param {Object} metadata - includes battery level of lock
 * @param {Function} callback
 * @private
 */
const _updateLockMetadata = (metadata, callback, sentryObj) => {
  if (Number.isFinite(metadata.lock_battery_level) || _.has(metadata, 'firmware_version') || _.has(metadata, 'shackle_jam')) {
    if (metadata.lock_id || metadata.mac_id) {
      const comparisonColumn = metadata.lock_id ? { lock_id: metadata.lock_id } : { mac_id: metadata.mac_id }
      lockHandler.getLocks(comparisonColumn, (error, locks) => {
        if (error) {
          if (sentryObj) {
            sentryObj.captureException(error, {comparisonColumn, locks})
          }
          logger('Error: updating metadata. Failed to get lock with', comparisonColumn)
          callback(errors.internalServer(false))
          return
        }
        if (locks.length === 0) {
          logger('Error: updating metadata. No locks available with', comparisonColumn)
          callback(errors.resourceNotFound(false))
          return
        }
        const lock = locks[0]
        if (metadata.user_id) {
          if (lock.user_id !== metadata.user_id) {
            logger('Error: updating metadata. The lock with', comparisonColumn,
              'It is not attached with the user', metadata.user_id)
            callback(errors.unauthorizedAccess(false))
            return
          }
        }
        const columnAndValues = {}
        if (Number.isFinite(metadata.lock_battery_level)) {
          let batteryLog = lock.battery_level_log ? JSON.parse(lock.battery_level_log) : []
          batteryLog.push([moment().unix(), metadata.lock_battery_level])
          batteryLog = batteryLog.length > config.lockBatteryLogLimit ? batteryLog.slice(-config.lockBatteryLogLimit) : batteryLog
          columnAndValues.battery_level = metadata.lock_battery_level
          columnAndValues.battery_level_log = JSON.stringify(batteryLog)
        }
        if (metadata.shackle_jam) {
          metadata.jam_counter = lock.jam_counter ? lock.jam_counter + 1 : 1
          columnAndValues.jam_counter = metadata.jam_counter
        }
        if (metadata.firmware_version) {
          columnAndValues.firmware_version = metadata.firmware_version
        }
        lockHandler.updateLock(columnAndValues, comparisonColumn, (error) => {
          if (error) {
            if (sentryObj) {
              sentryObj.captureException(error, {columnAndValues, comparisonColumn})
            }
            logger('Error: updating metadata. Failed to update lock battery level', error)
            callback(errors.internalServer(false))
            return
          }
          return callback(null)
        })
      })
    } else {
      getBikes({ bike_id: metadata.bike_id }, (error, bikes) => {
        if (error) {
          if (sentryObj) {
            sentryObj.captureException(error, {metadata})
          }
          logger('Error: updating metadata. Failed to update lock battery level', error)
          callback(errors.internalServer(false))
          return
        }
        if ((bikes.length === 0) && !bikes[0].lock_id) {
          logger('No bikes available with', metadata.bike_id)
          callback(errors.resourceNotFound(false))
          return
        }
        if (!bikes[0].lock_id) {
          callback(null)
          return
        }
        lockHandler.getLocks({ lock_id: bikes[0].lock_id }, (error, locks) => {
          if (error) {
            if (sentryObj) {
              sentryObj.captureException(error, {metadata, lock_id: bikes[0].lock_id})
            }
            logger('Error: updating metadata. Failed to get lock with', bikes[0].lock_id)
            callback(errors.internalServer(false))
            return
          }
          if (locks.length === 0) {
            logger('Error: updating metadata. No locks available with', bikes[0].lock_id)
            callback(errors.resourceNotFound(false))
            return
          }
          const lock = locks[0]
          const columnAndValues = {}
          if (Number.isFinite(metadata.lock_battery_level)) {
            let batteryLog = lock.battery_level_log ? JSON.parse(lock.battery_level_log) : []
            batteryLog.push([moment().unix(), metadata.lock_battery_level])
            batteryLog = batteryLog.length > config.lockBatteryLogLimit ? batteryLog.slice(-config.lockBatteryLogLimit) : batteryLog
            columnAndValues.battery_level = metadata.lock_battery_level
            columnAndValues.battery_level_log = JSON.stringify(batteryLog)
          }
          if (metadata.shackle_jam) {
            metadata.jam_counter = lock.jam_counter ? lock.jam_counter + 1 : 1
            columnAndValues.jam_counter = metadata.jam_counter
          }
          if (metadata.firmware_version) {
            columnAndValues.firmware_version = metadata.firmware_version
          }
          lockHandler.updateLock(columnAndValues,
            { lock_id: bikes[0].lock_id }, (error) => {
              if (error) {
                if (sentryObj) {
                  sentryObj.captureException(error, {columnAndValues, lock_id: bikes[0].lock_id})
                }
                logger('Error: updating metadata. Failed to update lock battery level', error)
                callback(errors.internalServer(false))
                return
              }
              callback(null)
            })
        })
      })
    }
  } else {
    callback(null)
  }
}

/** This method queries the bikes table
 *
 * @param {Object} filterParam - Identifier to filter the bikes, must send either lock_id or bike_id
 * @param {Function} callback
 */
const getBikesWithLock = async (filterParam, callback, sentryObj) => {
  let filterColumnAndValues = {}
  if (filterParam.bike_id) {
    filterColumnAndValues.bike_id = filterParam.bike_id
  } else if (filterParam.lock_id) {
    filterColumnAndValues.lock_id = filterParam.lock_id
  } else if (filterParam.qr_code_id) {
    const qrCodeInfo = await db.main('qr_codes').where({code: filterParam.qr_code_id, type: 'bike'}).first()
    if (qrCodeInfo) filterColumnAndValues.bike_id = qrCodeInfo.equipment_id
  }

  getBikes(filterColumnAndValues, (error, bikes) => {
    if (error) {
      if (sentryObj) {
        sentryObj.captureException(error, {filterColumnAndValues})
      }
      logger('Error: Failed to get bikes with', filterColumnAndValues)
      callback(errors.internalServer(false), null)
      return
    }
    if (bikes.length !== 0) {
      lockHandler.getLocks({ lock_id: _.uniq(_.pluck(bikes, 'lock_id')) }, (error, locks) => {
        if (error) {
          if (sentryObj) {
            sentryObj.captureException(error, {filterColumnAndValues, lock_id: _.uniq(_.pluck(bikes, 'lock_id'))})
          }
          callback(error, null)
          return
        }
        getBikeGroups({ bike_group_id: _.uniq(_.pluck(bikes, 'bike_group_id')) },
          (error, bikeGroups) => {
            if (error) {
              if (sentryObj) {
                sentryObj.captureException(error, {filterColumnAndValues, bike_group_id: _.uniq(_.pluck(bikes, 'bike_group_id'))})
              }
              callback(error, null)
              return
            }
            fleetHandler.getFleetsWithPaymentAndMetadata({ fleet_id: _.uniq(_.pluck(bikes, 'fleet_id')) }, (error, fleets) => {
              if (error) {
                if (sentryObj) {
                  sentryObj.captureException(error, {filterColumnAndValues, fleet_id: _.uniq(_.pluck(bikes, 'fleet_id'))})
                }
                logger('Error: While getting bikes. failed to get fleets with:', error)
                callback(errors.internalServer(false), null)
              }
              fleets = fleetHandler.formatFleetsForWire(fleets)
              if (fleets.length === 0) {
                logger('Error: fleets are not present in database in findBikes')
                callback(errors.resourceNotFound(false), null)
                return
              }
              parkingHandler.getParkingSpots({ fleet_id: _.uniq(_.pluck(bikes, 'fleet_id')) }, (error, parkingSpots) => {
                if (error) {
                  if (sentryObj) {
                    sentryObj.captureException(error, {filterColumnAndValues, fleet_id: _.uniq(_.pluck(bikes, 'fleet_id'))})
                  }
                  logger('Error: Failed to get parking spots', error)
                  callback(errors.internalServer(false), null)
                  return
                }
                fleetHandler.getCustomers({ customer_id: _.uniq(_.pluck(fleets, 'customer_id')) }, (error, customerDetails) => {
                  if (error) {
                    if (sentryObj) {
                      sentryObj.captureException(error, {filterColumnAndValues, customer_id: _.uniq(_.pluck(fleets, 'customer_id'))})
                    }
                    logger('Error: Failed to get customers with error:', error)
                    callback(error, null)
                    return
                  }
                  if (customerDetails.length === 0) {
                    logger('Error: Customers are not present in database in findBikes')
                    callback(errors.resourceNotFound(false), null)
                    return
                  }
                  getElectricBikes(null, { bike_id: _.uniq(_.pluck(bikes, 'bike_id')) }, async (error, electricBikes) => {
                    if (error) {
                      if (sentryObj) {
                        sentryObj.captureException(error, {filterColumnAndValues, bike_id: _.uniq(_.pluck(bikes, 'bike_id'))})
                      }
                      logger('Error: fetching bikes. Failed to get electric bikes with', _.uniq(_.pluck(bikes, 'bike_id')))
                      callback(errors.internalServer(false), null)
                      return
                    }
                    const controllers = await db.main('controllers').whereIn('bike_id', bikes.map(b => b.bike_id)).select()
                    callback(null, _formatBikeEntityForWire({ bikes, locks, electricBikes, bikeGroups, fleets, parkingSpots, customerDetails, controllers }))
                  })
                })
              })
            })
          })
      })
    } else {
      callback(null, bikes)
    }
  })
}

/**
 * This method will format all the bike entity for the clients
 *
 * @param {Array} bikes
 * @param {Array} locks
 * @param {Array} electricBikes
 * @param {Array} bikeGroups
 * @param {Array} fleets
 * @param {Array} parkingSpots
 * @param {Array} customers
 */
const _formatBikeEntityForWire = ({
  bikes, locks, electricBikes, bikeGroups, fleets, parkingSpots, customerDetails,
  controllers, reservationSettings, promotions
}) => {
  let bike
  let lock
  let eBike
  let bikeGroup
  let bikeFleet
  let customer
  bikes = _.uniq(bikes)
  const bikesLen = bikes.length

  let promotionsByFleet
  if (promotions) {
    promotionsByFleet = _.groupBy(promotions, 'fleet_id')
  }

  for (let i = 0; i < bikesLen; i++) {
    bike = bikes[i]
    if (!!locks && Array.isArray(locks)) {
      lock = _.findWhere(locks, { lock_id: bike.lock_id })
      if (lock) {
        delete lock.status
        bike = Object.assign(bike, lock)
      }
    }
    if (!!electricBikes && Array.isArray(electricBikes)) {
      eBike = _.findWhere(electricBikes, { bike_id: bike.bike_id })
      if (eBike) {
        bike = Object.assign(bike, eBike)
      }
    }
    if (!!bikeGroups && Array.isArray(bikeGroups)) {
      bikeGroup = _.findWhere(bikeGroups, { bike_group_id: bike.bike_group_id })
      if (bikeGroup) {
        bike = Object.assign(bike, bikeGroup)
      }
    }
    if (!!fleets && Array.isArray(fleets)) {
      bikeFleet = _.findWhere(fleets, { fleet_id: bike.fleet_id })
    }
    if (bikeFleet) {
      if (!!customerDetails && Array.isArray(customerDetails)) {
        customer = _.findWhere(customerDetails, { customer_id: bikeFleet.customer_id })
      }
      bike = Object.assign(bike, _.omit(bikeFleet, 'key', 't_and_c', 'logo', 'type', 'skip_parking_image'))
      bike.fleet_key = bikeFleet.key
      bike.fleet_t_and_c = bikeFleet.t_and_c
      bike.fleet_logo = bikeFleet.logo
      bike.fleet_type = bikeFleet.type
      bike.parking_area_restriction = bikeFleet.parking_area_restriction === 1
      bike.tariff = bikeFleet.type === fleetConstants.fleet_type.privateWithNoPayment
        ? fleetConstants.fleet_tariff.free : 0
      bike.fleet_bikes = (_.where(bikes, { fleet_id: bikeFleet.fleet_id })).length
      bike.fleet_parking_spots = (_.where(parkingSpots, { fleet_id: bikeFleet.fleet_id })).length
      bike.customer_name = customer ? customer.customer_name : ''
      bike.skip_parking_image = bikeFleet.skip_parking_image === 1
      bike.require_phone_number = bikeFleet.require_phone_number === 1
      bike.do_not_track_trip = bikeFleet.do_not_track_trip === 1
    }

    if (controllers) {
      bike.controllers = controllers.filter(controller =>
        controller.bike_id === bike.bike_id
      )
    }

    if (reservationSettings) {
      bike.reservation_settings = _.findWhere(
        reservationSettings,
        { fleet_id: bike.fleet_id }
      )
    }

    if (promotionsByFleet) {
      bike.promotions = promotionsByFleet[bike.fleet_id]
    }

    /* TODO: Soon we will have locks from other vendors, we will have to rewrite some of this logic to accommodate those changes */
    if (bikes[i] && bikes[i].lock_id !== null && bikes[i].fleet_type && bikes[i].fleet_key) {
      bikes[i] = bikes[i].mac_id ? lockFormatter.formatLockWithBikeForWire(bike) : lockFormatter.formatBikeForWire(bike)
    }
  }
  return bikes
}

/**
 * This method is to get all the bike
 *
 * @param {Object} filterParam - Identifier to filter the bikes
 * @param {Function} callback
 */
const getBikes = (filterParam, callback, sentryObj) => {
  let comparisonColumnAndValues = {}
  if (filterParam.bike_id) {
    comparisonColumnAndValues.bike_id = filterParam.bike_id
  }
  if (filterParam.lock_id) {
    comparisonColumnAndValues.lock_id = filterParam.lock_id
  }
  if (filterParam.fleet_id) {
    comparisonColumnAndValues.fleet_id = filterParam.fleet_id
  }
  if (filterParam.status) {
    comparisonColumnAndValues.status = filterParam.status
  }
  if (filterParam.current_status) {
    comparisonColumnAndValues.current_status = filterParam.current_status
  }
  if (filterParam.make) {
    comparisonColumnAndValues.make = filterParam.make
  }
  if (filterParam.model) {
    comparisonColumnAndValues.model = filterParam.model
  }
  if (filterParam.qr_code_id) {
    comparisonColumnAndValues.qr_code_id = filterParam.qr_code_id
  }

  const bikesQuery = query.selectWithAnd(dbConstants.tables.bikes, null, comparisonColumnAndValues)
  sqlPool.makeQuery(bikesQuery, (error, bikes) => {
    if (error) {
      if (sentryObj) {
        sentryObj.captureException(error, {bikes, bikesQuery})
      }
      logger('Error: Failed to get bikes with', comparisonColumnAndValues)
      callback(errors.internalServer(false), null)
      return
    }
    callback(errors.noError(), bikes)
  })
}

/** This method joins the bikes and locks table
 *
 * @param {Object} filterParam - Identifier to filter the bikes, must send either lock_id or bike_id
 * @param {Function} callback
 */
const getBikeWithLock = async (filterParam, callback, sentryObj) => {
  let where = null
  if (_.has(filterParam, 'bike_id')) {
    where = {
      'bikes.bike_id': filterParam.bike_id
    }
  }
  if (_.has(filterParam, 'qr_code_id')) {
    where = {
      'bikes.qr_code_id': filterParam.qr_code_id
    }
  }

  try {
    let bike = await db.main('bikes').first().where(where)
    if (bike.lock_id) {
      const lock = await db
        .main('locks')
        .first()
        .where({ lock_id: bike.lock_id })
      bike = Object.assign(bike, lock)
    } else {
      const controllers = await db
        .main('controllers')
        .select()
        .where({ bike_id: bike.bike_id })

      bike.controllers = controllers
    }
    callback(errors.noError(), [bike])
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, { filterParam })
    }
    logger('Error: Failed to get bikes with', JSON.stringify(where))
    callback(error, null)
  }
}

/**
 * This method gets bike information for a given point Coordinate
 *
 * @param {Object} searchParameters - latitude, longitude,numberOfBikes to be filtered ,distance in miles and fleet_id
 * @param {Function} callback
 */
const findBikes = (searchParameters, callback, sentryObj) => {
  const location = {
    latitude: parseFloat(searchParameters.latitude),
    longitude: parseFloat(searchParameters.longitude)
  }
  getBikes(
    {
      current_status: [
        bikeConstants.current_status.parked,
        bikeConstants.current_status.collect
      ]
    },
    (error, bikeData) => {
      if (error) {
        if (sentryObj) {
          sentryObj.captureException(error, { searchParameters })
        }
        logger('Error: Failed to get bikes with locks', error)
        callback(error, null)
        return
      }
      if (bikeData.length === 0) {
        logger('No Bikes available')
        callback(null, { bikes: bikeData })
        return
      }
      privateAccountHandler.filterPrivateFleetContent(
        bikeData,
        { user_id: searchParameters.user_id },
        (error, filteredBikes) => {
          if (error) {
            if (sentryObj) {
              sentryObj.captureException(error, { searchParameters })
            }
            callback(error, null)
            return
          }
          if (filteredBikes.length === 0) {
            logger('No Bikes available for the user')
            callback(null, { bikes: filteredBikes })
            return
          }
          filteredBikes = _.uniq(filteredBikes, 'bike_id')
          let nearByBikes = []
          const searchRange = searchParameters.bike_range
            ? searchParameters.bike_range
            : config.lattisSearchRange
          const filteredBikesLength = filteredBikes.length
          let bike
          for (let i = 0; i < filteredBikesLength; i++) {
            bike = filteredBikes[i]
            const lastPoint = {}
            lastPoint.latitude = bike.latitude
            lastPoint.longitude = bike.longitude
            const distance = pointHandler.distanceBetweenPoints(
              location,
              lastPoint
            )
            if (distance <= searchRange) {
              nearByBikes.push(bike)
            }
          }
          if (nearByBikes.length === 0) {
            logger('No Bikes available with nearby latitudes and longitudes')
            callback(null, { bikes: nearByBikes })
            return
          }

          formatBikesForWire(
            { bikes: nearByBikes, searchParameters },
            callback
          )
        }
      )
    }
  )
}

/**
 * This method gets bike information for a given point Coordinate
 *
 * @param {Object} searchParameters
 * @param {Function} callback
 */
const findBikesByName = (searchParameters, callback, sentryObj) => {
  getBikes(
    {
      current_status: [
        bikeConstants.current_status.parked,
        bikeConstants.current_status.collect
      ]
    },
    (error, bikeData) => {
      if (error) {
        if (sentryObj) {
          sentryObj.captureException(error, { searchParameters })
        }
        logger('Error: Failed to get bikes with locks', error)
        callback(error, null)
        return
      }
      if (bikeData.length === 0) {
        logger('No Bikes available')
        callback(null, { bikes: bikeData })
        return
      }
      privateAccountHandler.filterPrivateFleetContent(
        bikeData,
        { user_id: searchParameters.user_id },
        (error, filteredBikes) => {
          if (error) {
            if (sentryObj) {
              sentryObj.captureException(error, { searchParameters })
            }
            callback(error, null)
            return
          }
          if (filteredBikes.length === 0) {
            logger('No Bikes available for the user')
            callback(null, { bikes: filteredBikes })
            return
          }
          const bikeName = new RegExp(
            (searchParameters.bike_name || '').toLowerCase()
          )
          filteredBikes = _.uniq(filteredBikes, 'bike_id')
          let bikesByName = filteredBikes.filter(
            (bike) =>
              bike.bike_name && bike.bike_name.toLowerCase().match(bikeName)
          )
          if (!bikesByName.length) {
            logger('No Bikes available for the user')
            callback(null, { bikes: [] })
            return
          }

          formatBikesForWire(
            { bikes: bikesByName, searchParameters },
            callback
          )
        }
      )
    }
  )
}

const formatBikesForWire = ({ bikes, searchParameters }, callback, sentryObj) => {
  lockHandler.getLocks(
    { lock_id: _.uniq(_.pluck(bikes, 'lock_id')) },
    (error, locks) => {
      if (error) {
        if (sentryObj) {
          sentryObj.captureException(error, { searchParameters, bikes })
        }
        logger(
          'Error: Failed to get locks with',
          _.uniq(_.pluck(bikes, 'lock_id')),
          error
        )
        callback(error, null)
        return
      }
      bikes = _formatBikeEntityForWire({ bikes, locks })
      if (bikes.length === 0) {
        logger('No Bikes available with nearby latitudes and longitudes')
        callback(null, { bikes: [] })
        return
      }
      bookingHandler.getActiveBooking(
        { bike_id: _.uniq(_.pluck(bikes, 'bike_id')) },
        (error, bookingDetails) => {
          if (error) {
            if (sentryObj) {
              sentryObj.captureException(error, {
                searchParameters,
                bike_id: _.uniq(_.pluck(bikes, 'bike_id'))
              })
            }
            logger(
              'Error: Failed to get booking details with',
              bookingDetails.bike_id,
              ':',
              error
            )
            callback(errors.internalServer(false), null)
            return
          }
          if (bookingDetails) {
            const bookingLength = bookingDetails.length
            for (let index = 0; index < bookingLength; index++) {
              bikes = bikes.filter(
                (bike) => bike.bike_id !== bookingDetails[index].bike_id
              )
            }
          }
          if (bikes.length === 0) {
            logger('No Bikes available with nearby latitudes and longitudes')
            callback(null, { bikes: [] })
            return
          }

          fleetHandler.getFleetsWithPaymentAndMetadata(
            { fleet_id: _.uniq(_.pluck(bikes, 'fleet_id')) },
            (error, fleets) => {
              if (error) {
                if (sentryObj) {
                  sentryObj.captureException(error, {
                    searchParameters,
                    fleet_id: _.uniq(_.pluck(bikes, 'fleet_id'))
                  })
                }
                logger('Error: Failed to get fleets', error)
                callback(errors.internalServer(false), null)
                return
              }
              fleets = fleetHandler.formatFleetsForWire(fleets)
              parkingHandler.getParkingSpots(
                { fleet_id: _.uniq(_.pluck(bikes, 'fleet_id')) },
                (error, parkingSpots) => {
                  if (error) {
                    if (sentryObj) {
                      sentryObj.captureException(error, {
                        searchParameters,
                        fleet_id: _.uniq(_.pluck(bikes, 'fleet_id'))
                      })
                    }
                    logger('Error: Failed to get parking spots', error)
                    callback(errors.internalServer(false), null)
                    return
                  }
                  fleetHandler.getCustomers(
                    { customer_id: _.uniq(_.pluck(fleets, 'customer_id')) },
                    (error, customerDetails) => {
                      if (error) {
                        sentryObj.captureException(error, {
                          searchParameters,
                          customer_id: _.uniq(_.pluck(fleets, 'customer_id'))
                        })
                        logger(
                          'Error: Failed to get customers with error:',
                          error
                        )
                        callback(error, null)
                        return
                      }
                      if (customerDetails.length === 0) {
                        logger(
                          'Error: Customers are not present in database: formatPrivateAccountForWire'
                        )
                        callback(errors.resourceNotFound(false), null)
                        return
                      }
                      getBikeGroups(
                        {
                          bike_group_id: _.uniq(
                            _.pluck(bikes, 'bike_group_id')
                          )
                        },
                        (error, bikeGroups) => {
                          if (error) {
                            sentryObj.captureException(error, {
                              searchParameters,
                              bike_group_id: _.uniq(
                                _.pluck(bikes, 'bike_group_id')
                              )
                            })
                            callback(error, null)
                            return
                          }
                          getElectricBikes(
                            null,
                            { bike_id: _.uniq(_.pluck(bikes, 'bike_id')) },
                            (error, electricBikes) => {
                              if (error) {
                                sentryObj.captureException(error, {
                                  searchParameters,
                                  bike_id: _.uniq(_.pluck(bikes, 'bike_id'))
                                })
                                logger(
                                  'Error: fetching bikes. Failed to get electric bikes with',
                                  _.uniq(_.pluck(bikes, 'bike_id'))
                                )
                                callback(errors.internalServer(false), null)
                                return
                              }

                              const bikeIds = bikes.map((bike) => bike.bike_id)

                              db.main('controllers')
                                .select()
                                .whereIn('bike_id', bikeIds)
                                .then(async (controllers) => {
                                  const reservationSettings = await db
                                    .main('bikes')
                                    .whereIn('bikes.bike_id', bikeIds)
                                    .join('reservation_settings', function () {
                                      this.on(
                                        'reservation_settings.fleet_id',
                                        '=',
                                        'bikes.fleet_id'
                                      ).onNull(
                                        'reservation_settings.deactivation_date'
                                      )
                                    })
                                    .distinct('reservation_settings.*')
                                    .options({
                                      typeCast:
                                        reservationHelpers.castDataTypes
                                    })

                                  const promos = await promotions.list({
                                    user: { user_id: searchParameters.user_id }
                                  })

                                  callback(null, {
                                    bikes: _formatBikeEntityForWire({
                                      bikes,
                                      electricBikes,
                                      bikeGroups,
                                      fleets,
                                      parkingSpots,
                                      customerDetails,
                                      controllers,
                                      reservationSettings,
                                      promotions: promos
                                    })
                                  })
                                })
                                .catch((error) => {
                                  sentryObj.captureException(error, {
                                    searchParameters,
                                    bike_id: _.uniq(_.pluck(bikes, 'bike_id'))
                                  })
                                  callback(error, null)
                                })
                            }
                          )
                        }
                      )
                    }
                  )
                }
              )
            }
          )
        }
      )
    }
  )
}
/**
 * To get particular bike details
 *
 * @param {Object} properties - bike identifier
 * @param {Function} callback
 */
const getBikeDetails = async (properties, callback, sentryObj) => {
  if (!properties.bike_id && (properties.qr_code_id || properties.iot_qr_code)) {
    const qrCodeInfo = await db.main('qr_codes').where({ code: properties.iot_qr_code || properties.qr_code_id }).first()
    if (!qrCodeInfo) {
      callback(errors.customError('The provided QR code was not found in our system', responseCodes.ResourceNotFound, 'QRCodeNotFound', true), null)
      return
    }
    properties.bike_id = qrCodeInfo.equipment_id
  }
  getBikesWithLock(properties.bike_id ? { bike_id: properties.bike_id } : { qr_code_id: properties.qr_code_id },
    async (error, bikeDetails) => {
      if (error) {
        sentryObj.captureException(error, {bike_id: properties.bike_id, qr_code_id: properties.qr_code_id})
        logger('Error: getting bike details for bike with bike_id:', properties.bike_id, 'with error;', error)
        callback(error, null)
        return
      }
      if (bikeDetails.length === 0) {
        callback(errors.resourceNotFound(false), null)
        return
      }
      let bike = bikeDetails[0]
      if (bike.bike_battery_level) bike.bike_battery_level = parseInt(bike.bike_battery_level)
      if (bike.status !== 'active') {
        return callback({ message: 'You cannot use a bike that is not live', code: 422, name: 'ResourceNotAvailable' }, null)
      }
      if (bike.status === 'active' && bike.current_status === 'on_trip' && (properties.qr_code_id || properties.iot_qr_code)) {
        const currentTrip = await db.main('trips').where({ bike_id: bike.bike_id }).orderBy('trip_id', 'DESC').first()
        if (currentTrip.user_id !== properties.user_id) {
          return callback({ message: 'You cannot use a bike on trip', code: 404 }, null)
        }
      }

      // lets check if the fleet is accessible
      privateAccountHandler.getAccessibleFleets({ user_id: properties.user_id }, [bike.fleet_id], (error, fleets) => {
        if (error) {
          sentryObj.captureException(error, {user_id: properties.user_id, fleet_id: bike.fleet_id})
          logger('Error: getting bike details for bike with bike_id:', properties.bike_id,
            'Unable to get accessible fleets with error;', error)
          callback(error, null)
          return
        }
        if (fleets.length === 0) {
          logger('The user :', properties.user_id, 'does not have access to fleet', bike.fleet_id)
          callback(errors.customError(`The user ${properties.user_id}, does not have access to fleet ${bike.fleet_id}`, responseCodes.Unauthorized, 'UnauthorizedAccessToFleet', true), null)
          return
        }
        // lets check if the the bike is booked already
        bookingHandler.getActiveBooking({ bike_id: bike.bike_id }, async (error, bookingDetails) => {
          if (error) {
            sentryObj.captureException(error, {bike_id: bike.bike_id})
            logger('Error: Failed to get booking details with', bike.bike_id, ': ', error)
            callback(error, null)
            return
          }
          if (!!bookingDetails && bookingDetails.length && (bookingDetails[0].user_id !== properties.user_id)) {
            logger('The bike with bike_id:', bike.bike_id, ' is already booked')
            callback(errors.customError('Bike already has a active booking', responseCodes.MethodNotAllowed,
              'bikeAlreadyBooked', false), null)
            return
          }

          const promos = await promotions.list({
            user: { user_id: properties.user_id },
            fleetId: bike.fleet_id
          })

          if (promos.length) {
            bike.promotions = promos
          }

          bike.pricing_options = await pricing.handlers.list({ fleet_id: bike.fleet_id })

          await controllerDetails.processControllerKeys(bike.controllers)
          callback(errors.noError(), { bike: bike })
        })
      })
    })
}

/**
 * To get particular bike/port/hub details
 *
 * @param {Object} properties - bike/port/hub identifier
 * @param {Function} callback
 */
const getRentalDetails = async (sentryObj, properties, callback) => {
  const qrCodeData = await db.main('qr_codes').where({ code: properties.qr_code }).first()
  if (!qrCodeData) {
    callback(errors.customError('A vehicle/port with that QR code not found', responseCodes.ResourceNotFound, 'GetBikeDetails'), null)
    return
  }
  if (qrCodeData) {
    if (qrCodeData.type === 'bike') properties.bike_id = qrCodeData.equipment_id
    else if (qrCodeData.type === 'port') properties.port_id = qrCodeData.equipment_id
    else if (qrCodeData.type === 'docking station' || qrCodeData.type === 'hub') properties.hub_id = qrCodeData.equipment_id
  }
  if (properties.port_id) {
    // How to handle a port booking
    try {
      const portInfo = await retrievePort({portId: +properties.port_id})
      if (!portInfo) {
        callback(errors.customError('A vehicle/port with that QR code not found', responseCodes.ResourceNotFound, 'MissingPortDetails', true), null)
        return
      }

      const hub = portInfo.hub

      if (hub.type !== 'parking_station') {
        callback(errors.customError('Only ports on hubs of type parking station can be rented', responseCodes.BadRequest, 'HubNotParkingStation'), null)
        return
      }

      if (portInfo.hub.status !== 'live') {
        throw errors.customError(
          'The hub holding this port is not live',
          responseCodes.ResourceNotFound,
          'ResourceNotFound',
          false
        )
      }

      if (portInfo.port_status !== 'Available') {
        throw errors.customError(
          'The port is not available for use',
          responseCodes.ResourceNotFound,
          'ResourceNotFound',
          false
        )
      }
      if (portInfo.port_current_status === 'on_trip') {
        throw errors.customError(
          'The port is used by another member',
          409,
          'ResourceNotAvailable',
          false
        )
      }

      const fleetId = portInfo.fleet.fleet_id
      portInfo.fleet_id = fleetId
      const {access} = await fleetAccess.manageFleetAccess({fleetId: fleetId, userId: properties.user_id})
      if (!access) {
        callback(errors.unauthorizedAccess(false), null)
        return
      }
      bookingHandler.getActiveBooking({ port_id: properties.port_id }, async (error, bookingDetails) => {
        if (error) {
          logger('Error: Failed to get booking details for port', properties.port_id, ': ', error)
          sentryObj.captureException(error, {user_id: properties.user_id, fleet_id: fleetId})
          callback(error, null)
          return
        }
        if (!!bookingDetails && bookingDetails.length && (bookingDetails[0].user_id !== properties.user_id)) {
          logger('The port with id:', properties.port_id, ' is already booked')
          callback(errors.customError('Port already has a active booking', responseCodes.MethodNotAllowed, 'bikeAlreadyBooked', false), null)
          return
        }

        const promos = await promotions.list({
          user: { user_id: properties.user_id },
          fleetId: fleetId
        })

        if (promos.length) {
          portInfo.promotions = promos
        }
        portInfo.pricing_options = await pricing.handlers.list({ fleet_id: fleetId })
        callback(errors.noError(), { port: portInfo })
      })
    } catch (error) {
      sentryObj.captureException(error, {port_id: properties.port_id})
      callback(errors.customError(
        error.message || 'An error occurred getting port details',
        error.code || responseCodes.ResourceNotFound,
        error.name || 'ResourceNotFound',
        true
      ), null)
    }
  } else if (properties.bike_id) {
    getBikesWithLock({ bike_id: properties.bike_id }, async (error, bikeDetails) => {
      const date = await db.main('reservations').where({
        bike_id: properties.bike_id,
        reservation_terminated: null
      }).max('created_at as latestDate').first()

      const bikeReservation = await db.main('reservations').where({
        bike_id: properties.bike_id,
        reservation_terminated: null,
        created_at: date.latestDate
      }).first() || {}

      if (error) {
        sentryObj.captureException(error, {bike_id: properties.bike_id})
        logger('Error: getting bike details for bike with bike_id:', properties.bike_id, 'with error;', error)
        callback(error, null)
        return
      }
      if (bikeDetails.length === 0) {
        callback(errors.resourceNotFound(false), null)
        return
      }
      let bike = bikeDetails[0]
      if (bike.bike_battery_level) bike.bike_battery_level = parseInt(bike.bike_battery_level)
      if (bike.status !== 'active') {
        return callback({ message: 'You cannot use a bike that is not live', code: 422, name: 'ResourceNotAvailable' }, null)
      }
      if (bike.status === 'active' && bike.current_status === 'on_trip' && (properties.qr_code_id || properties.iot_qr_code || properties.qr_code)) {
        const now = moment().unix()
        const currentTrip = await db.main('trips').where({bike_id: bike.bike_id}).andWhere(function () {
          this.whereNotNull('date_created')
          this.whereNull('end_address')
          this.whereNull('date_endtrip')
          this.where('date_created', '<=', now)
        }).first()
        if (currentTrip && currentTrip.user_id !== properties.user_id) {
          return callback({ message: 'You cannot use a bike on trip', code: 409 }, null)
        }
      }

      // let's check if the fleet is accessible iff it's a private fleet
      const {access} = await fleetAccess.manageFleetAccess({fleetId: bike.fleet_id, userId: properties.user_id})
      if (!access) {
        callback(errors.unauthorizedAccess(false), null)
        return
      }
      // let's check if the bike is booked already
      bookingHandler.getActiveBooking({ bike_id: bike.bike_id }, async (error, bookingDetails) => {
        if (error) {
          sentryObj.captureException(error, {bike_id: bike.bike_id})
          logger('Error: Failed to get booking details with', bike.bike_id, ': ', error)
          callback(error, null)
          return
        }
        if (!!bookingDetails && bookingDetails.length && (bookingDetails[0].user_id !== properties.user_id)) {
          logger('The bike with bike_id:', bike.bike_id, ' is already booked')
          callback(errors.customError('Bike already has a active booking', responseCodes.MethodNotAllowed,
            'bikeAlreadyBooked', false), null)
          return
        }

        const promos = await promotions.list({
          user: { user_id: properties.user_id },
          fleetId: bike.fleet_id
        })

        if (promos.length) {
          bike.promotions = promos
        }

        bike.pricing_options = await pricing.handlers.list({ fleet_id: bike.fleet_id })
        await controllerDetails.processControllerKeys(bike.controllers)
        bike.reservation = bikeReservation

        callback(errors.noError(), { bike: bike })
      })
    })
  } else if (properties.hub_id) {
    // How to handle a port booking
    try {
      const hubInfo = await getHubById(+properties.hub_id, true)
      if (!hubInfo) {
        callback(errors.customError('Could not get hub info with that QR code', responseCodes.ResourceNotFound, 'HubNotFound'), null)
        return
      }

      if (hubInfo.type !== 'parking_station') {
        callback(errors.customError('Only hubs of type parking station can be rented', responseCodes.BadRequest, 'HubNotParkingStation'), null)
        return
      }

      if (!(hubInfo.remote_hub_status === 'Available' && hubInfo.local_hub_status === 'live')) {
        throw errors.customError(
          'The hub is not available for use',
          responseCodes.ResourceNotFound,
          'ResourceNotFound',
          false
        )
      }

      const fleetId = hubInfo.fleet_id
      // let's check if the fleet is accessible
      privateAccountHandler.getAccessibleFleets({ user_id: properties.user_id }, [fleetId], (error, fleets) => {
        if (error) {
          sentryObj.captureException(error, {hub_id: properties.hub_id})
          logger('Error: getting fleet accessibility details for:', properties.bike_id, 'Unable to get accessible fleets with error;', error)
          callback(error, null)
          return
        }
        if (fleets.length === 0) {
          logger('The user :', properties.user_id, 'does not have access to fleet', fleetId)
          callback(errors.unauthorizedAccess(false), null)
          return
        }
        // let's check if the hub is booked already
        bookingHandler.getActiveBooking({ hub_id: properties.hub_id }, async (error, bookingDetails) => {
          if (error) {
            sentryObj.captureException(error, {hub_id: properties.hub_id})
            logger('Error: Failed to get booking details for port', properties.port_id, ': ', error)
            callback(error, null)
            return
          }
          if (!!bookingDetails && bookingDetails.length && (bookingDetails[0].user_id !== properties.user_id)) {
            logger('The hub with id:', properties.hub_id, ' is already booked')
            callback(errors.customError('Hub already has a active booking', responseCodes.MethodNotAllowed, 'bikeAlreadyBooked', false), null)
            return
          }

          const promos = await promotions.list({
            user: { user_id: properties.user_id },
            fleetId: fleetId
          })

          if (promos.length) {
            hubInfo.promotions = promos
          }
          hubInfo.pricing_options = await pricing.handlers.list({ fleet_id: fleetId })
          callback(errors.noError(), { hub: hubInfo })
        })
      })
    } catch (error) {
      sentryObj.captureException(error, {hub_id: properties.hub_id})
      throw errors.customError(
        'An error occured getting port details',
        responseCodes.ResourceNotFound,
        'ResourceNotFound',
        false
      )
    }
  }
}

const getVehicleInfo = async (bikeDetails, sentryObj) => {
  try {
    return await db.main.select().from('bikes').where({ bike_id: bikeDetails.bike_id }).first()
  } catch (error) {
    sentryObj.captureException(error, {bike_id: bikeDetails.bike_id})
    logger('Error:::', error)
    throw new Error('Failed to get vehicle info')
  }
}

/**
 * To update a bike with the properties
 *
 * @param {Object} properties - Contains the bike identifiers
 * @param {Function} callback
 */
const updateBike = async (trx, properties, callback, sentryObj) => {
  try {
    await (trx ? trx('bikes') : db.main('bikes')).update(_.omit(properties, 'fleet_id', 'bike_id')).where({ bike_id: properties.bike_id })
    callback(null, null)
  } catch (error) {
    sentryObj.captureException(error, {bike_id: properties.bike_id})
    logger('Error: making update bike query with bike_id: ', properties.bike_id)
    return callback(errors.internalServer(false), null)
  }
}

/**
 * This method formats a single lock with bike to be sent over the wire. Only properties that
 * are "safe" to be sent will be sent.
 *
 * @param {object} lock
 * @returns {object} formatted lock
 */
const formatLockWithBikeForWire = (lock) => {
  return lockFormatter.formatLockWithBikeForWire(lock)
}

/**
 * This method formats multiple lock with bikes to be sent over the wire. Only properties that
 * are "safe" to be sent will be sent.
 *
 * @param {Array} locks
 * @returns {Array} formatted locks
 */
const formatLockWithBikesForWire = (locks) => {
  return lockFormatter.formatLockWithBikesForWire(locks)
}

// FIXME: Temporary Method for testing purpose. Has to be removed on Production
const getBike = (comparisonColumn, callback, sentryObj) => {
  const getBikeQuery = query.selectWithAnd(dbConstants.tables.bikes, null, comparisonColumn)
  sqlPool.makeQuery(getBikeQuery, (error, bikes) => {
    if (error) {
      sentryObj.captureException(error, {comparisonColumn, getBikeQuery})
      logger('Error: making update bike query with: ', comparisonColumn, ':', getBikeQuery)
      callback(errors.internalServer(false), null)
      return
    }
    callback(null, bikes)
  })
}

/**
 * This method assigns lock to a bike
 *
 * @param {object} requestParam
 * @param {function} callback
 * @returns {object} status of lock
 */
const assignLock = async (requestParam, callback, sentryObj) => {
  const getLockWithLockIdAsync = util.promisify(lockHandler.getLockWithLockId)

  let bike
  let lock

  try {
    const assignedBike = await db.main('bikes')
      .where({ lock_id: requestParam.lock_id })
      .first()

    if (assignedBike) {
      logger('The lock:', requestParam.lock_id, ' is already registered with bike:', assignedBike.bike_id)
      return callback(errors.unauthorizedAccess(false), null)
    }

    lock = await getLockWithLockIdAsync(requestParam.lock_id)
    if (!lock) {
      logger('Invalid lock id')
      return callback(errors.resourceNotFound(false), null)
    }

    if (requestParam.qr_code_id) {
      bike = await db.main('bikes')
        .where({
          qr_code_id: requestParam.qr_code_id
        })
        .first()
    } else if (requestParam.iot_qr_code) {
      bike = await db.main('bikes')
        .join('controllers', 'bikes.bike_id', 'controllers.bike_id')
        .where({
          'controllers.qr_code': requestParam.iot_qr_code
        })
        .first('bikes.*')
    }

    if (bike && bike.lock_id) {
      logger(`The bike with qr_code_id ${requestParam.qr_code_id} is already assigned a lock`)
      return callback(errors.unauthorizedAccess(false), null)
    }

    // if there is no bike with the qr_code_id or iot_qr_code try to assign the lock to
    // a random bike with the received bike_group_id.
    if (!bike) {
      if (!_.has(requestParam, 'bike_group_id')) {
        logger('Error: Missing bike_group_id in assing-lock request')
        return callback(errors.missingParameter(false))
      }
      const assignableBikes = await await db.main('bikes')
        .where({
          lock_id: null,
          status: bikeConstants.status.inactive,
          current_status: bikeConstants.current_status.lockNotAssigned,
          fleet_id: lock.fleet_id,
          bike_group_id: requestParam.bike_group_id
        })
        .select()

      if (assignableBikes.length) {
        bike = _.sample(assignableBikes)
      }
    }
  } catch (error) {
    sentryObj.captureException(error, {lock_id: requestParam.lock_id})
    return callback(errors.internalServer(false), null)
  }

  if (!bike) {
    logger('There are no bikes in staging!')
    return callback(errors.resourceNotFound(false), null)
  }

  updateBike(null, _.extend(_.pick(requestParam, 'qr_code_id', 'bike_name', 'bike_battery_level'), {
    bike_id: bike.bike_id,
    lock_id: lock.lock_id,
    current_status: bikeConstants.current_status.lockAssigned
  }), (error) => {
    if (error) {
      sentryObj.captureException(error, {bike_id: bike.bike_id})
      callback(errors.internalServer(false), null)
      return
    }
    getBikes(_.pick(bike, 'bike_id'), (error, newBike) => {
      if (error) {
        sentryObj.captureException(error, {bike_id: bike.bike_id})
        callback(errors.internalServer(false), null)
        return
      }
      fleetHandler.getFleetKey(lock.fleet_id, (error, fleetKey) => {
        if (error) {
          sentryObj.captureException(error, {fleet_id: lock.fleet_id})
          callback(errors.internalServer(false), null)
          return
        }
        lock.fleet_key = fleetKey
        if (_.has(requestParam, 'e_bike_id')) {
          getElectricBikes(null, { e_bike_id: requestParam.e_bike_id }, (error, electricBikes) => {
            if (error) {
              sentryObj.captureException(error, {e_bike_id: requestParam.e_bike_id})
              logger('Error: Failed to get Electric bikes', errors.errorWithMessage(error))
              callback(errors.internalServer(false), null)
              return
            }
            if (electricBikes.length > 0) {
              logger('Error: This electric bike has been already assigned', requestParam.e_bike_id)
              callback(errors.unauthorizedAccess(false), null)
              return
            }
            updateElectricBike({
              e_bike_id: requestParam.e_bike_id
            },
            { bike_id: bike.bike_id },
            (error) => {
              if (error) {
                sentryObj.captureException(error, {e_bike_id: requestParam.e_bike_id})
                logger('Error: Failed to update Electric bikes', errors.errorWithMessage(error))
                callback(errors.internalServer(false), null)
                return
              }
              callback(errors.noError(), { lock: formatLockWithBikeForWire(_.extend(lock, newBike[0])) })
            })
          })
        } else {
          callback(errors.noError(), { lock: formatLockWithBikeForWire(_.extend(lock, newBike[0])) })
        }
      })
    })
  })
}

/**
 * This method gives the bikes
 *
 * @param {Object} comparisonColumn
 * @param {function} callback
 * @returns {Array} formatted locks
 */
const getBikesWithFleets = (comparisonColumn, callback, sentryObj) => {
  const getBikesQuery = query.selectWithAnd(dbConstants.tables.bikes, null, comparisonColumn)
  sqlPool.makeQuery(getBikesQuery, (error, bikes) => {
    if (error) {
      sentryObj.captureException(error, {comparisonColumn})
      logger('Error: making get bikes query with: ', comparisonColumn, ':', getBikesQuery)
      callback(errors.internalServer(false), null)
      return
    }
    callback(null, bikes)
  })
}

/**
 * This method unAssign lock to a bike
 *
 * @param {object} requestParam
 * @param {function} callback
 * @returns {object} formatted lock
 */
const unAssignLock = (requestParam, callback, sentryObj) => {
  lockHandler.getLockWithLockId(requestParam.lock_id, (error, lock) => {
    if (error) {
      sentryObj.captureException(error, {lock_id: requestParam.lock_id})
      callback(errors.internalServer(false), null)
      return
    }
    getBikes(_.pick(lock, 'lock_id'), (error, bikes) => {
      if (error) {
        sentryObj.captureException(error, {lock_id: requestParam.lock_id})
        callback(error, null)
        return
      }
      if (bikes.length === 0) {
        logger('The lock is not assigned to any bike!')
        callback(errors.resourceNotFound(false), null)
        return
      }
      updateBike(null, {
        bike_id: bikes[0].bike_id,
        lock_id: null,
        status: bikeConstants.status.inactive,
        current_status: bikeConstants.current_status.lockNotAssigned
      }, (error) => {
        if (error) {
          sentryObj.captureException(error, {bike_id: bikes[0].bike_id})
          callback(error, null)
          return
        }
        fleetHandler.getFleetKey(lock.fleet_id, (error, fleetKey) => {
          if (error) {
            sentryObj.captureException(error, {fleet_id: lock.fleet_id})
            callback(errors.internalServer(false), null)
          }
          callback(errors.noError(), { lock: formatLockWithBikeForWire(_.extend(lock, { fleet_key: fleetKey })) })
        })
      })
    })
  })
}

/**
 * This method get all the bike groups
 *
 * @param {object} comparisonColumns
 * @param {function} callback
 */
const getBikeGroups = (comparisonColumns, callback, sentryObj) => {
  let bikeGroupsQuery = query.selectWithAnd(dbConstants.tables.bike_group, null, comparisonColumns)
  sqlPool.makeQuery(bikeGroupsQuery, (error, bikeGroups) => {
    if (error) {
      sentryObj.captureException(error, {comparisonColumns})
      logger('Error: making get bike groups query with: ', comparisonColumns, ':', bikeGroupsQuery)
      callback(errors.internalServer(false), null)
      return
    }
    callback(null, bikeGroups)
  })
}

/**
 * This method get all staging bikes
 *
 * @param {object} reqParam
 * @param {function} callback
 */

const getStagingBikeGroups = (reqParam, callback, sentryObj) => {
  let comparisonColumnsAndValues = {
    fleet_id: reqParam.fleet_id,
    status: bikeConstants.status.inactive,
    current_status: bikeConstants.current_status.lockNotAssigned
  }

  getBikes(comparisonColumnsAndValues, (error, bikes) => {
    if (error) {
      sentryObj.captureException(error, {comparisonColumnsAndValues})
      callback(error, null)
      return
    }

    let groupedBikes = _.map(_.groupBy(bikes, (bike) => {
      return bike.bike_group_id
    }), (grouped) => {
      return grouped[0]
    })
    let bikeGroupIds = []
    _.each(groupedBikes, (groupedBikes) => {
      bikeGroupIds.push(groupedBikes.bike_group_id)
    })
    getBikeGroups({ fleet_id: reqParam.fleet_id }, (error, bikeGroups) => {
      if (error) {
        sentryObj.captureException(error, {fleet_id: reqParam.fleet_id})
        callback(error, null)
        return
      }
      let stagingBikeGroups = []
      _.each(bikeGroups, (bikeGroup) => {
        _.each(bikeGroupIds, (groupId) => {
          if (bikeGroup.bike_group_id === groupId) {
            stagingBikeGroups.push(bikeGroup)
          }
        })
      })
      callback(null, stagingBikeGroups)
    })
  })
}

/**
 * This method changes the bike status
 *
 * @param {object} requestParam
 * @param {function} callback
 */
const changeBikeStatus = (requestParam, callback, sentryObj) => {
  let status
  let currentStatus
  let maintenanceStatus
  bookingHandler.getActiveBooking(_.pick(requestParam, 'bike_id'), (error, activeBooking) => {
    if (error) {
      sentryObj.captureException(error, {bike_id: requestParam.bike_id})
      callback(error, null)
      return
    }

    if (activeBooking) {
      logger('Trip is active for this bike_id', _.pick(requestParam, 'bike_id'))
      callback(errors.unauthorizedAccess(false))
      return
    }

    if (requestParam.to === 'live') {
      status = bikeConstants.status.active
      currentStatus = bikeConstants.current_status.parked
      maintenanceStatus = null
    } else if (requestParam.to === 'staging') {
      status = bikeConstants.status.inactive
      currentStatus = bikeConstants.current_status.lockAssigned
      maintenanceStatus = bikeConstants.maintenance_status.shopMaintenance
    } else if (requestParam.to === 'out_of_service') {
      status = bikeConstants.status.suspended
      currentStatus = bikeConstants.current_status.underMaintenance
      maintenanceStatus = bikeConstants.maintenance_status.shopMaintenance
    } else if (requestParam.to === 'archive') {
      if (!_.has(requestParam, 'category')) {
        logger('Missing parameter category in Request')
        callback(errors.missingParameter(false), null)
        return
      } else {
        status = bikeConstants.status.deleted
        maintenanceStatus = null
        if (requestParam.category === bikeConstants.current_status.totalLoss) {
          currentStatus = bikeConstants.current_status.totalLoss
        } else if (requestParam.category === 'defleet') {
          currentStatus = bikeConstants.current_status.defleet
        } else if (requestParam.category === bikeConstants.current_status.stolen) {
          currentStatus = bikeConstants.current_status.stolen
        }
      }
    }
    updateBike(null, {
      bike_id: requestParam.bike_id,
      status: status,
      current_status: currentStatus,
      maintenance_status: maintenanceStatus,
      latitude: requestParam.latitude,
      longitude: requestParam.longitude
    }, (error) => {
      if (error) {
        sentryObj.captureException(error, {bike_id: requestParam.bike_id})
        callback(error, null)
        return
      }
      callback(null, null)
    })
  })
}

/**
 * This method checks if the bike crossed maintenance due
 *
 * @param {object} bike - Details of the bike
 * @param {function} callback
 */
const checkForServiceDue = (bike, callback, sentryObj) => {
  getBikeGroups(_.pick(bike, 'bike_group_id'), (error, bikeGroups) => {
    if (error) {
      sentryObj.captureException(error, {bike_group_id: bike.bike_group_id})
      callback(error)
      return
    }
    if (bikeGroups[0].maintenance_schedule <= bike.distance_after_service) {
      maintenanceHandler.getMaintenance({
        status: maintenanceConstants.status.maintenanceDue,
        category: maintenanceConstants.category.serviceDue,
        bike_id: bike.bike_id
      }, (error, maintenance) => {
        if (error) {
          sentryObj.captureException(error, {bike_id: bike.bike_id})
          logger('Error: checking maintenance due. Failed to get maintenance', error)
          callback(errors.internalServer(false))
          return
        }
        maintenance = _.where(maintenance, { service_end_date: null })
        bike = Object.assign(bike, { category: maintenanceConstants.category.serviceDue })
        if (maintenance.length !== 0) {
          ticketHandler.getTicket({
            type: ticketConstants.types.maintenance,
            type_id: _.pluck(maintenance, 'maintenance_id'),
            status: [ticketConstants.status.created, ticketConstants.status.assigned]
          }, (error, tickets) => {
            if (error) {
              sentryObj.captureException(error, {bike_id: bike.bike_id})
              logger('Error: checking maintenance due. Failed to get tickets', error)
              callback(errors.internalServer(false))
              return
            }
            if (tickets.length !== 0) {
              callback(errors.noError())
              return
            }
            ticketHandler.createTicket(bike, callback)
          })
        } else {
          ticketHandler.createTicket(bike, callback)
        }
      })
    } else {
      callback(errors.noError())
    }
  })
}

/**
 * This method gives all the nearest and available bikes
 *
 * @param {object} searchParameters
 * @param {function} callback
 */
const searchBikes = (searchParameters, callback, sentryObj) => {
  const location = {
    latitude: parseFloat(searchParameters.latitude),
    longitude: parseFloat(searchParameters.longitude),
    bike_range: config.lattisNearestRange
  }
  findBikes(
    Object.assign(searchParameters, { bike_range: config.lattisNearestRange }),
    (error, bikeData) => {
      if (error) {
        sentryObj.captureException(error, {
          latitude: searchParameters.latitude,
          longitude: searchParameters.longitude,
          bike_range: config.lattisNearestRange
        })
        callback(error, null)
        return
      }
      let nearestBikes = []
      let availableBikes = []
      if (bikeData.bikes.length === 0) {
        logger('There is no service in this location')
        return callback(null, { nearest: [], available: [] })
      } else {
        _.each(bikeData.bikes, (bike) => {
          const lastPoint = {}
          lastPoint.latitude = bike.latitude
          lastPoint.longitude = bike.longitude
          const distance = pointHandler.distanceBetweenPoints(
            location,
            lastPoint
          )
          if (distance <= config.lattisSearchRange) {
            nearestBikes.push(bike)
          } else {
            availableBikes.push(bike)
          }
        })
        callback(null, {
          nearest: nearestBikes,
          available: nearestBikes.length > 0 ? [] : availableBikes
        })
      }
    }
  )
}

const searchBikesByCoordinates = (searchParameters, callback, sentryObj) => {
  var latLon = searchParameters.ne.replace(/[()]/g, '').split(',')
  const ne = {
    latitude: parseFloat(latLon[0]),
    longitude: parseFloat(latLon[1])
  }
  latLon = searchParameters.sw.replace(/[()]/g, '').split(',')
  const sw = {
    latitude: parseFloat(latLon[0]),
    longitude: parseFloat(latLon[1])
  }
  var midPoint = {
    latitude: sw.latitude + (ne.latitude - sw.latitude) / 2,
    longitude: sw.longitude + (ne.longitude - sw.longitude) / 2
  }
  const bikeRange = pointHandler.distanceBetweenPoints(midPoint, ne)
  midPoint.user_id = searchParameters.user_id
  midPoint.bike_range = bikeRange
  findBikes(midPoint, async (error, bikeData) => {
    if (error) {
      sentryObj.captureException(error, {
        latitude: midPoint.latitude,
        longitude: midPoint.longitude,
        bike_range: midPoint.bike_range
      })
      callback(error, null)
      return
    }
    if (bikeData.bikes.length === 0) {
      logger('There is no service in this location')
      return callback(null, [])
    } else {
      let bikeList = bikeData.bikes.filter(
        (bike) =>
          bike.longitude <= ne.longitude &&
          bike.longitude >= sw.longitude &&
          bike.latitude <= ne.latitude &&
          bike.latitude >= sw.latitude
      )
      bikeList = await Promise.all(
        bikeList.map(async (bike) => {
          const qrCode = await db
            .main('qr_codes')
            .where({ equipment_id: bike.bike_id, type: 'bike' })
            .first()
          return Object.assign(bike, {
            qr_code: (qrCode && qrCode.code) || null
          })
        })
      )
      callback(null, bikeList)
    }
  })
}
/**
 * This method get the lock info with the qr_code_id
 *
 * @param {object} requestParam
 * @param {function} callback
 */
const getLockByQrCode = (requestParam, callback, sentryObj) => {
  getBikeWithLock(requestParam, (error, bikeInfo) => {
    if (error) {
      sentryObj.captureException(error, { qr_code_id: requestParam.qr_code_id })
      logger('Error: Getting lock by qr code.', errors.errorWithMessage(error))
      callback(errors.internalServer(false), null)
      return
    }
    fleetHandler.getFleetKey(bikeInfo[0].fleet_id, (error, fleetKey) => {
      if (error) {
        sentryObj.captureException(error, { qr_code_id: requestParam.qr_code_id })
        callback(errors.internalServer(false), null)
        return
      }
      callback(errors.noError(), {
        bike: formatLockWithBikeForWire(
          _.extend(bikeInfo[0], { fleet_key: fleetKey })
        )
      })
    })
  })
}

/**
 * This method used to change the qr_code_id and bike_name
 *
 * @param {object} requestParam
 * @param {function} callback
 */
const changeLabel = (requestParam, callback, sentryObj) => {
  getBikes(_.pick(requestParam, 'qr_code_id'), (error, bikeWithQRCode) => {
    if (error) {
      sentryObj.captureException(error, { qr_code_id: requestParam.qr_code_id })
      logger(
        'Error: Changing bike label. Unable to get bike by QR code.',
        errors.errorWithMessage(error)
      )
      callback(errors.internalServer(false), null)
    }
    if (bikeWithQRCode.length !== 0) {
      logger(
        'The QR code is already registered with bike:',
        bikeWithQRCode[0].bike_id
      )
      callback(errors.unauthorizedAccess(false), null)
      return
    }
    updateBike(
      _.pick(requestParam, 'bike_id', 'bike_name', 'qr_code_id'),
      (error) => {
        if (error) {
          sentryObj.captureException(error, {
            qr_code_id: requestParam.qr_code_id
          })
          callback(errors.internalServer(false), null)
          return
        }
        getBikes(_.pick(requestParam, 'bike_id'), (error, bike) => {
          if (error) {
            sentryObj.captureException(error, { bike_id: requestParam.bike_id })
            callback(errors.internalServer(false), null)
          }
          if (bike[0].lock_id) {
            lockHandler.getLockWithLockId(bike[0].lock_id, (error, lock) => {
              if (error) {
                sentryObj.captureException(error, {
                  lock_id: requestParam.lock_id
                })
                callback(errors.internalServer(false), null)
                return
              }
              fleetHandler.getFleetKey(bike[0].fleet_id, (error, fleetKey) => {
                if (error) {
                  sentryObj.captureException(error, {
                    lock_id: requestParam.lock_id
                  })
                  callback(errors.internalServer(false), null)
                }
                bike[0].fleet_key = fleetKey
                callback(errors.noError(), {
                  lock: formatLockWithBikeForWire(_.extend(lock, bike[0]))
                })
              })
            })
          } else {
            db.main('controllers')
              .select()
              .where({ bike_id: requestParam.bike_id })
              .then((controllers) => {
                callback(errors.noError(), { controllers })
              })
              .catch((error) => {
                sentryObj.captureException(error, {
                  bike_id: requestParam.bike_id
                })
                callback(errors.internalServer(false), null)
              })
          }
        })
      }
    )
  })
}

const checkForBikeIssues = (bikeDetails, callback, sentryObj) => {
  logger('Check bike for details: bikeDetails', bikeDetails)
  bookingHandler.getBookingDetails(
    {
      bike_id: bikeDetails.bike_id,
      status: tripConstants.booking.cancelled_with_issue
    },
    (error, bookings) => {
      if (error) {
        sentryObj.captureException(error, { bike_id: bikeDetails.bike_id })
        logger(
          'Error: checking for bike issues. Failed to get all the bookings for bike with',
          bikeDetails.bike_id,
          error
        )
        callback(errors.internalServer(false))
        return
      }
      // Let's take the last 5 booking records for the bike
      bookings = bookings.slice(bookings.length - 5, bookings.length)
      bookings = _.filter(bookings, (bikeBooking) => {
        return bikeBooking.trip_id !== null
      })
      logger('Check bike for details: booking', bookings, bookings.length)
      if (bookings.length === 0) {
        maintenanceHandler.getMaintenance(
          {
            status: maintenanceConstants.status.issueDetected,
            category: maintenanceConstants.category.issueDetected,
            bike_id: bikeDetails.bike_id
          },
          (error, maintenance) => {
            if (error) {
              sentryObj.captureException(error, { bike_id: bikeDetails.bike_id })
              logger(
                'Error: checking for bike issues. Failed to get maintenance',
                error
              )
              callback(errors.internalServer(false))
              return
            }
            maintenance = _.where(maintenance, { service_end_date: null })
            const ticketDetails = Object.assign(
              _.pick(
                bikeDetails,
                'bike_id',
                'lock_id',
                'user_id',
                'fleet_id',
                'operator_id',
                'customer_id'
              ),
              {
                category: maintenanceConstants.category.issueDetected,
                maintenance_notes:
                  'The bike is facing problem in lock connection'
              }
            )
            logger('Check bike for details: ticket details', ticketDetails)
            logger(
              'Check bike for details: maintainance',
              maintenance,
              maintenance.length
            )
            if (maintenance.length !== 0) {
              ticketHandler.getTicket(
                {
                  type: ticketConstants.types.maintenance,
                  type_id: _.pluck(maintenance, 'maintenance_id'),
                  status: [
                    ticketConstants.status.created,
                    ticketConstants.status.assigned
                  ]
                },
                (error, tickets) => {
                  if (error) {
                    if (sentryObj) {
                      sentryObj.captureException(error, {
                        bike_id: bikeDetails.bike_id
                      })
                    }
                    logger(
                      'Error: checking for bike issues. Failed to get tickets',
                      error
                    )
                    callback(errors.internalServer(false))
                    return
                  }
                  if (tickets.length > 0) {
                    callback(errors.noError())
                    return
                  }
                  ticketHandler.createTicket(ticketDetails, callback)
                }
              )
            } else {
              ticketHandler.createTicket(ticketDetails, callback)
            }
          }
        )
      } else {
        callback(errors.noError())
      }
    }
  )
}

/**
 * Similar to assignLock with controller_id instead of lock_id
 */
const assignControllerToRandomBike = async (params, sentryObj) => {
  try {
    const controller = await db.main('controllers').first().where({
      controller_id: params.controller_id
    })
    if (!controller) {
      logger('The controller:', params.controller_id, ' does not exist')
      throw errors.resourceNotFound(false)
    }
    if (controller.bike_id) {
      logger(
        'The controller:',
        controller.controller_id,
        ' is already registered with bike:',
        controller.bike_id
      )
      throw errors.unauthorizedAccess(false)
    }

    const bikeWithQRCode = await db.main('bikes').first().where({
      qr_code_id: params.qr_code_id
    })
    if (bikeWithQRCode) {
      logger(
        'The QR code:',
        params.qr_code_id,
        'is already registered for bike:',
        bikeWithQRCode.bike_id
      )
      throw errors.unauthorizedAccess(false)
    }

    const assignableBikes = await db
      .main('bikes')
      .select('bikes.*', 'controllers.controller_id')
      .leftOuterJoin('controllers', 'controllers.bike_id', 'bikes.bike_id')
      .where({
        'bikes.fleet_id': controller.fleet_id,
        'bikes.bike_group_id': params.bike_group_id,
        'bikes.status': bikeConstants.status.inactive,
        'bikes.current_status': bikeConstants.current_status.lockNotAssigned,
        'bikes.lock_id': null,
        'controllers.controller_id': null
      })

    if (!assignableBikes.length) {
      logger('there are no bikes in staging!')
      throw errors.resourceNotFound(false)
    }

    const bikeToAssign = _.sample(assignableBikes)

    // Update controller and bike status atomically
    await db.main.transaction(async (trx) => {
      await trx('bikes').where({ bike_id: bikeToAssign.bike_id }).update({
        qr_code_id: params.qr_code_id,
        bike_name: params.bike_name,
        bike_battery_level: params.bike_battery_level,
        current_status: bikeConstants.current_status.controllerAssigned
      })
      await trx('controllers')
        .where({ controller_id: controller.controller_id })
        .update({ bike_id: bikeToAssign.bike_id })
    })

    // Should include an equivalent to lines 744-767 here. But that logic in
    // the assingLock funtion would throw an error every time because the
    // electric_bikes table doesn't have an e_bike_id column. Therefore, it
    // has been skipped here

    // Return the updated bike with all it's assigned controllers
    const [assignedBike, controllers] = await Promise.all([
      db.main('bikes').first().where({ bike_id: bikeToAssign.bike_id }),
      db.main('controllers').select().where({ bike_id: bikeToAssign.bike_id })
    ])

    assignedBike.controllers = controllers
    return assignedBike
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, { controller_id: params.controller_id })
    }
    if (error.constructor.name === 'LattisError') {
      throw error
    }
    logger(`Error: Failed to assign controller: ${error.message}`)
    throw errors.internalServer(false)
  }
}

const assignController = async ({ bikeId, controllerId }, sentryObj) => {
  try {
    // Update controller and bike status
    await db.main.transaction(async (trx) => {
      const bikesUpdated = await trx('bikes')
        .where({ bike_id: bikeId })
        .update({
          current_status: bikeConstants.current_status.controllerAssigned
        })
      if (bikesUpdated === 0) {
        logger(
          `Error: Failed to assign controller: Bike ${bikeId} does not exist`
        )
        throw errors.invalidParameter(false)
      }
      const controllersUpdated = await trx('controllers')
        .where({
          controller_id: controllerId,
          bike_id: null
        })
        .update({ bike_id: bikeId })
      if (controllersUpdated === 0) {
        logger(
          `Error: Failed to assign controller: Controller ${controllerId} does not exist or is already assigned to a bike`
        )
        throw errors.invalidParameter(false)
      }
    })

    // Return the updated bike with all it's assigned controllers
    const [bike, controllers] = await Promise.all([
      db.main('bikes').first().where({ bike_id: bikeId }),
      db.main('controllers').select().where({ bike_id: bikeId })
    ])

    bike.controllers = controllers

    return bike
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, {
        bike_id: bikeId,
        controller_id: controllerId
      })
    }
    if (error.constructor.name === 'LattisError') {
      throw error
    }
    logger(`Error: Failed to update controllers: ${error.message}`)
    if (error.code === 'ER_NO_REFERENCED_ROW_2') {
      throw errors.invalidParameter(false)
    }
    throw errors.internalServer(false)
  }
}

const shouldUpdateLocation = async (bikeId, sentryObj) => {
  let updateLocation = true
  try {
    const vehicle = await db.main('bikes').where({ bike_id: bikeId }).first()
    if (vehicle) {
      const isDockedToHub = await db
        .main('ports')
        .where({ vehicle_uuid: vehicle.bike_uuid })
        .first()
      if (isDockedToHub) updateLocation = false
    }
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, { bike_id: bikeId })
    }
    logger(`Error: Failed to check if bike is docked to hub: ${error.message}`)
  }
  return updateLocation
}

/**
 * Get the status (location, battery level) of the bike from Segway API
 * and update those details in the database
 *
 * @param {number} bikeId id of the bike to update
 */
const updateSegwayBikeStatus = async (segwayApi, bikeId, controllerKey, sentryObj) => {
  try {
    const controller = await db
      .main('controllers')
      .where({ key: controllerKey, device_type: 'iot' })
      .first()

    let shouldUpdateVehicleAndCtrlLocation
    if (bikeId) await shouldUpdateLocation(bikeId)
    if (!controller) {
      logger(
        `Warn: Unable to update status for segway equipment ${controllerKey}`
      )
      return
    }

    const status = await segwayApi.getVehicleInformation(controller.key)

    const positionUpdates = {
      latitude: status.latitude,
      longitude: status.longitude
    }

    let bikeUpdates = { bike_battery_level: status.powerPercent }
    let ctrlUpdates = {
      battery_level: status.powerPercent,
      gps_log_time: status.gpsUtcTime
    }
    if (shouldUpdateVehicleAndCtrlLocation) {
      bikeUpdates = { ...bikeUpdates, ...positionUpdates }
      ctrlUpdates = { ...ctrlUpdates, ...positionUpdates }
    }

    await db.main.transaction(async (trx) => {
      if (bikeId) {
        await trx('bikes').where('bike_id', bikeId).update(bikeUpdates)
      }

      await trx('controllers')
        .where('controller_id', controller.controller_id)
        .update(ctrlUpdates)
    })
    logger(`Info: Updated status for segway bike ${bikeId}`)
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, { controller_id: controllerKey })
    }
    // the bike status update can fail silently since it doesn't block the request
    logger(`Warn: Unable to update status for segway bike ${bikeId}: ${error}`)
  }
}

/**
 * Get the status (location) of the bike from Geotab API
 * and update those details in the database
 *
 * @param {number} bikeId id of the bike to update
 * @param {number} deviceId id of the geotab device
 * @param {object}  controller of the bike to update
 */
const updateGeotabBikeStatus = async (bikeId, controller, sentryObj) => {
  try {
    const shouldUpdateVehicleAndCtrlLocation = await shouldUpdateLocation(
      bikeId
    )
    const deviceLocation = await GeotabApi.getCurrentLocation(controller)
    const location = {
      latitude: deviceLocation[0].latitude,
      longitude: deviceLocation[0].longitude
    }
    await db.main.transaction(async (trx) => {
      let ctrlUpdates = { gps_log_time: deviceLocation[0].dateTime }
      if (shouldUpdateVehicleAndCtrlLocation) {
        await trx('bikes').where('bike_id', bikeId).update(location)
        ctrlUpdates = { ...ctrlUpdates, ...location }
      }

      await trx('controllers')
        .where('controller_id', controller.controller_id)
        .update(ctrlUpdates)
    })
    logger(`Info: Updated status for bike ${bikeId}`)
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, { controller_id: controller.controller_id })
    }
    // the bike status update can fail silently since it doesn't block the request
    logger(`Warn: Unable to update status for bike ${bikeId}: ${error}`)
    throw errors.customError(
      `Failed to update location for bike ${bikeId}`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      false
    )
  }
}

/**
 * Get the status (location) of the bike from Linka API
 * and update those details in the database
 *
 * @param {number} bikeId id of the bike to update
 * @param {object}  controller of the bike to update
 */
const updateLinkaBikeStatus = async (bikeId, controller, sentryObj) => {
  try {
    const deviceLocation = await LinkaApi.getCurrentLocation(controller.key)
    const location = {
      latitude: deviceLocation[0].latitude,
      longitude: deviceLocation[0].longitude
    }
    const shouldUpdateVehicleAndCtrlLocation = await shouldUpdateLocation(
      bikeId
    )
    await db.main.transaction(async (trx) => {
      let ctrlUpdates = { gps_log_time: deviceLocation.modifiedAt }
      if (shouldUpdateVehicleAndCtrlLocation) {
        await trx('bikes').where('bike_id', bikeId).update(location)
        ctrlUpdates = { ...ctrlUpdates, ...location }
      }

      await trx('controllers')
        .where('controller_id', controller.controller_id)
        .update(ctrlUpdates)
    })
    logger(`Info: Updated status for bike ${bikeId}`)
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, { controller_id: controller.controller_id })
    }
    // the bike status update can fail silently since it doesn't block the request
    logger(`Warn: Unable to update status for bike ${bikeId}: ${error}`)
    throw errors.customError(
      `Failed to update location for bike ${bikeId}`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      false
    )
  }
}

/**
 * Get the location and battery level of the bike from COMODULE API
 * and update those details in the database
 *
 * @param {number} bikeId id of the bike to update
 */
const updateComoduleSegwayEs4BikeStatus = async (bikeId, sentryObj) => {
  try {
    const shouldUpdateVehicleAndCtrlLocation = await shouldUpdateLocation(
      bikeId
    )
    const controller = await db
      .main('controllers')
      .where({ bike_id: bikeId, device_type: 'iot' })
      .first()

    if (!controller) {
      logger(
        `Warn: Unable to update status for COMODULE Ninebot ES4 bike ${bikeId}: controller not found`
      )
      return
    }

    const vehicleState = await comoduleApi.getVehicleState(controller.key)
    const location = {
      latitude: vehicleState.gpsLatitude,
      longitude: vehicleState.gpsLongitude
    }
    await db.main.transaction(async (trx) => {
      let bikeUpdates = {
        bike_battery_level: vehicleState.vehicleBatteryPercentage
      }
      let ctrlUpdates = {
        battery_level: vehicleState.moduleBatteryPercentage,
        gps_log_time: vehicleState.lastUpdate
      }
      if (shouldUpdateVehicleAndCtrlLocation) {
        bikeUpdates = { ...bikeUpdates, ...location }
        ctrlUpdates = { ...ctrlUpdates, ...location }
      }
      await trx('bikes').where('bike_id', bikeId).update(bikeUpdates)

      await trx('controllers')
        .where('controller_id', controller.controller_id)
        .update(ctrlUpdates)
    })
    logger(`Info: Updated status for segway bike ${bikeId}`)
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error)
    }
    // the bike status update can fail silently since it doesn't block the request
    logger(`Warn: Unable to update status for segway bike ${bikeId}: ${error}`)
  }
}

/**
 * Get the status (location, battery level) of the bike from Acton API
 * and update those details in the database
 *
 * @param {number} bikeId id of the bike to update
 */
const updateActonBikeStatus = async (bikeId, sentryObj) => {
  try {
    const shouldUpdateVehicleAndCtrlLocation = await shouldUpdateLocation(bikeId)
    const controller = await db.main('controllers')
      .where({ bike_id: bikeId, device_type: 'iot' })
      .first()

    if (!controller) {
      logger(`Warn: Unable to update status for segway bike ${bikeId}: controller not found`)
      return
    }

    const clientApi = actonFamily[controller.vendor]

    if (!clientApi) return

    const status = await clientApi.getCurrentStatus(controller.key)
    const location = {
      latitude: status.lat,
      longitude: status.lon
    }
    await db.main.transaction(async trx => {
      let bikeUpdates = { bike_battery_level: status.extendedData.vehicleBattery || status.extendedData.scooterBattery }
      let ctrlUpdates = {
        battery_level: status.iotBattery,
        gps_log_time: status.gpsTimestamp
      }
      if (shouldUpdateVehicleAndCtrlLocation) {
        bikeUpdates = { ...bikeUpdates, ...location }
        ctrlUpdates = { ...ctrlUpdates, ...location }
      }
      await trx('bikes')
        .where('bike_id', bikeId)
        .update(bikeUpdates)

      await trx('controllers')
        .where('controller_id', controller.controller_id)
        .update(ctrlUpdates)
    })
    logger(`Info: Updated status for ${controller.vendor} bike ${bikeId}`)
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error)
    }
    // the bike status update can fail silently since it doesn't block the request
    logger(`Warn: Unable to update status for bike ${bikeId}: ${error}`)
  }
}

const updateKisiLockStatus = async (controller, sentryObj) => {
  try {
    const status = await new Kisi().fetchLock(controller.fleet_id, controller.key)
    await db.main.transaction(async (trx) => {
      let ctrlUpdates
      const location = {
        latitude: status.latitude || status.place.latitude,
        longitude: status.longitude || status.place.longitude
      }
      ctrlUpdates = location

      if (ctrlUpdates) {
        await trx('controllers')
          .where('controller_id', controller.controller_id)
          .update(ctrlUpdates)
      }
    })
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, {controller_id: controller.controller_id})
    }
    // the bike status update can fail silently since it doesn't block the request
    logger(`Warn: Unable to update status for Kisi lock: ${error}`)
  }
}

/**
 * Get the status (location, battery level) of the Nimbelink tracker/controller
 * and update those details in the database
 *
 * @param {number} bikeId id of the bike to update
 */
const updateNimbelinkTrackerStatus = async (controllerId, sentryObj) => {
  try {
    const controller = await db.main('controllers')
      .where('controller_id', controllerId)
      .first()

    const shouldUpdateVehicleAndCtrlLocation = await shouldUpdateLocation(controller.bike_id)

    if (!controller) {
      logger(`Warn: Nimbelink tracker with ID${controllerId}: not found`)
      return
    }

    const vehicleState = await getLatestDataForController(controller.key)
    if (!vehicleState) {
      throw errors.customError(
        'Controller status undefined',
        responseCodes.BadRequest,
        'BadRequest',
        false
      )
    }
    const lastLocation = vehicleState.data.loc.pop()
    const location = {
      latitude: lastLocation.lat,
      longitude: lastLocation.lon
    }
    await db.main.transaction(async trx => {
      let bikeUpdates = { battery_level: vehicleState.data.deviceStatus[0].estBattPct, gps_log_time: lastLocation.ts }
      if (shouldUpdateVehicleAndCtrlLocation) {
        bikeUpdates = { ...bikeUpdates, ...location }
      }
      await trx('controllers')
        .where('controller_id', controllerId)
        .update(bikeUpdates)
    })
    logger(`Info: Updated status for Nimbelink tracker with ID: ${controllerId}`)
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, {controller_id: controllerId})
    }
    // the bike status update can fail silently since it doesn't block the request
    logger(`Warn: Unable to update status for Nimbelink tracker with ID: ${controllerId}: ${error}`)
  }
}

const updateEdgeBikeStatus = async (EdgeApi, bikeId, controllerKey) => {
  try {
    const edgeInfo = await EdgeApi.getVehicleInformation(controllerKey)
    if (edgeInfo) {
      const { powerPercent, latitude, longitude } = edgeInfo
      await db.main('bikes').update({ bike_battery_level: powerPercent, latitude, longitude }).where({ bike_id: bikeId })
      await db.main('controllers').update({ battery_level: powerPercent, latitude, longitude }).where({ bike_id: bikeId })
    }
  } catch (error) {
    logger(`Error: An error occurred updating edge bike status ${error.message}`)
  }
}

/**
 * Used to get the data for a geotab integration
 * @param {object} fleetId
 * @param done - Callback
 */
const getGeotabApiKey = async (fleetId, sentryObj) => {
  try {
    const geotabApiKeys = await db.main('integrations')
      .where('fleet_id', fleetId.fleet_id)
      .where('integration_type', 'geotab')
      .select()
    if (geotabApiKeys) {
      return geotabApiKeys
    } else {
      throw errors.customError(
        `Geotab keys for ${fleetId} not found.`,
        platform.responseCodes.ResourceNotFound,
        'NotFound',
        true
      )
    }
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, {fleet_id: fleetId})
    }
    logger(`Error: Cannot get credentials for Geotab integration : ${error.message}`)
    throw errors.customError(
      error.message || `No Geotab integration available for fleet: ${error.message}`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

const lockBike = async (bikeId, sentryObj) => {
  try {
    const controller = await db.main('controllers')
      .where({ bike_id: bikeId, device_type: 'iot' })
      .first()

    const date = await db.main('trips').where({bike_id: bikeId}).max('date_created as latestDate').first()
    const lastTrip = await db.main('trips').where({bike_id: bikeId, date_created: date.latestDate}).first()
    const lastUserId = lastTrip ? lastTrip.user_id : ''

    if (!lastUserId) {
      Sentry.captureException('No user found for bike')
    }

    if (!controller) {
      logger(`Error: Failed to lock bike ${bikeId}, no controller assigned`)
      throw errors.customError(
        'Bike cannot be locked via API',
        responseCodes.BadRequest,
        'BadRequest',
        false
      )
    }

    switch (controller.vendor) {
      case 'Segway':
        const segwayLockResponse = await segwayApiClientForUs.lock(controller.key)
        // Do not await, run the update without blocking the request
        updateSegwayBikeStatus(segwayApiClientForUs, bikeId, controller.key, sentryObj)
        return segwayLockResponse

      case 'Segway IoT EU':
        const segwayEULockResponse = await segwayApiClientForEu.lock(controller.key, sentryObj)
        // Do not await, run the update without blocking the request
        updateSegwayBikeStatus(segwayApiClientForUs, bikeId, controller.key)
        return segwayEULockResponse

      case 'COMODULE Ninebot ES4':
        const comoduleLockResponse = await comoduleApi.lock(controller.key)
        // Do not await, run the update without blocking the request
        updateComoduleSegwayEs4BikeStatus(bikeId)
        return comoduleLockResponse

      // For acton vehicles it's start-unlock and stop-lock
      case 'ACTON':
      case 'Omni IoT':
      case 'Okai':
      case 'Teltonika':
        // Stop and lock vehicle
        let clientApi = actonFamily[controller.vendor]
        const lockResponse = await clientApi.stopVehicle(controller.key)
        updateActonBikeStatus(bikeId, sentryObj)
        return lockResponse

      case 'Geotab IoT':
        const geotabUser = await setUpGeotabApiUser(controller)
        if (geotabUser) {
          GeotabApi = new GeotabApiClient({
            password: geotabUser.password,
            userName: geotabUser.userName,
            sessionId: geotabUser.sessionId,
            database: geotabUser.database,
            serverName: geotabUser.serverName
          })
        }
        const geotabLockResponse = await GeotabApi.lock(controller)
        const [integrationData] = await getGeotabApiKey({ fleet_id: controller.fleet_id }, sentryObj)
        let metadata = JSON.parse(integrationData.metadata)
        metadata.bike_locked = true
        try {
          await db.main('integrations')
            .update({ metadata: JSON.stringify(metadata) })
            .where({ fleet_id: controller.fleet_id, integration_type: 'geotab' })
        } catch (error) {
          logger(`Warn: Unable to update lock status for Geotab with controller ${controller.key}: ${error}`)
          throw errors.customError(
            `Unable to update lock status for Geotab with id ${controller.key}`,
            platform.responseCodes.InternalServer,
            'InternalServer',
            false
          )
        }
        const [message] = geotabLockResponse
        await updateGeotabBikeStatus(bikeId, controller)
        return message

      case 'Linka IoT':
        const linkaMerchant = await setUpLinkaApiClient(controller)
        if (!_.isEmpty(linkaMerchant)) {
          LinkaApi = new LinkaApiClient({
            baseUrl: linkaMerchant.baseUrl,
            merchantUrl: linkaMerchant.merchantUrl,
            apiKey: linkaMerchant.apiKey,
            secretKey: linkaMerchant.secretKey
          })
        }
        const linkaResponse = await LinkaApi.lock(controller.key)
        await updateLinkaBikeStatus(bikeId, controller, sentryObj)
        return linkaResponse
      case 'Manual Lock':
        logger(`Error: Failed to lock bike ${bikeId}, Dumb lock is not a controller`)
        throw errors.customError(
          'Bike cannot be locked via API',
          responseCodes.BadRequest,
          'BadRequest',
          false
        )
      case 'Edge':
        const EdgeApiHandler = new EdgeApiClient(controller.fleet_id, lastUserId)
        updateEdgeBikeStatus(EdgeApiHandler, bikeId, controller.key)
        const bike = await db.main('bikes').where({ bike_id: bikeId }).first()
        const edgeVehicleInfo = await EdgeApiHandler.getVehicleInformation(controller.key)
        if (edgeVehicleInfo.mode === 'MoM') {
          throw new Error('You can not operate the vehicle in MoM mode')
        }
        let res
        if (bike.current_status === 'on_trip') {
          if (edgeVehicleInfo.trip) {
            res = await EdgeApiHandler.pauseTrip(edgeVehicleInfo.trip.id)
          } else {
            throw new Error('The Edge Vehicle is not in trip mode')
          }
        } else {
          throw new Error('You cannot lock a vehicle without a trip')
        }
        return res
      default:
        logger(`Error: Failed to lock bike ${bikeId}, ${controller.vendor} API not supported`)
        throw errors.customError(
          'Bike cannot be locked via API',
          responseCodes.BadRequest,
          'BadRequest',
          false
        )
    }
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, {bike_id: bikeId})
    }
    if (error.constructor.name === 'LattisError') {
      throw error
    }
    logger(`Error: Failed to lock bike, ${error.response ? error.response.data.message : error.message}`)
    throw errors.internalServer(false)
  }
}

const lockEquipment = async (controller, sentryObj) => {
  try {
    if (!controller) {
      logger(`Error: Failed to lock equipment with key ${controller.key}, no controller assigned`)
      throw errors.customError(
        'Equipment cannot be locked via API',
        responseCodes.BadRequest,
        'BadRequest',
        false
      )
    }

    switch (controller.vendor) {
      case 'Segway':
        const segwayLockResponse = await segwayApiClientForUs.lock(controller.key)
        // Do not await, run the update without blocking the request
        if (controller.bike_id) {
          updateSegwayBikeStatus(segwayApiClientForUs, controller.bike_id, controller.key, sentryObj)
        } else updateSegwayBikeStatus(segwayApiClientForUs, undefined, controller.key, sentryObj)
        return segwayLockResponse

      case 'Segway IoT EU':
        const segwayEULockResponse = await segwayApiClientForEu.lock(controller.key)
        // Do not await, run the update without blocking the request
        if (controller.bike_id) {
          updateSegwayBikeStatus(segwayApiClientForUs, controller.bike_id, controller.key, sentryObj)
        } else updateSegwayBikeStatus(segwayApiClientForUs, undefined, controller.key, sentryObj)
        return segwayEULockResponse

      case 'COMODULE Ninebot ES4':
        const comoduleLockResponse = await comoduleApi.lock(controller.key)
        // Do not await, run the update without blocking the request
        if (controller.bike_id) updateComoduleSegwayEs4BikeStatus(controller.bike_id)
        return comoduleLockResponse

      // For acton vehicles it's start-unlock and stop-lock
      case 'ACTON':
      case 'Omni IoT':
      case 'Teltonika':
      case 'Okai':
        // Stop and lock vehicle
        let clientApi = actonFamily[controller.vendor]
        const lockResponse = await clientApi.stopVehicle(controller.key)
        if (controller.bike_id) updateActonBikeStatus(controller.bike_id)
        return lockResponse

      case 'Geotab IoT':
        const geotabUser = await setUpGeotabApiUser(controller)
        if (geotabUser) {
          GeotabApi = new GeotabApiClient({
            password: geotabUser.password,
            userName: geotabUser.userName,
            sessionId: geotabUser.sessionId,
            database: geotabUser.database,
            serverName: geotabUser.serverName
          })
        }
        const geotabLockResponse = await GeotabApi.lock(controller)
        const [integrationData] = await getGeotabApiKey({ fleet_id: controller.fleet_id }, sentryObj)
        let metadata = JSON.parse(integrationData.metadata)
        metadata.bike_locked = true
        try {
          await db.main('integrations')
            .update({ metadata: JSON.stringify(metadata) })
            .where({ fleet_id: controller.fleet_id, integration_type: 'geotab' })
        } catch (error) {
          if (sentryObj) {
            sentryObj.captureException(error, {bike_id: controller.fleet_id})
          }
          logger(`Warn: Unable to update lock status for Geotab with controller ${controller.key}: ${error}`)
          throw errors.customError(
            `Unable to update lock status for Geotab with id ${controller.key}`,
            platform.responseCodes.InternalServer,
            'InternalServer',
            false
          )
        }
        const [message] = geotabLockResponse
        if (controller.bike_id) updateGeotabBikeStatus(controller.bike_id, controller)
        return message

      case 'Linka IoT':
        const linkaMerchant = await setUpLinkaApiClient(controller)
        if (!_.isEmpty(linkaMerchant)) {
          LinkaApi = new LinkaApiClient({
            baseUrl: linkaMerchant.baseUrl,
            merchantUrl: linkaMerchant.merchantUrl,
            apiKey: linkaMerchant.apiKey,
            secretKey: linkaMerchant.secretKey
          })
        }
        const linkaResponse = await LinkaApi.lock(controller.key)
        if (controller.bike_id) await updateLinkaBikeStatus(controller.bike_id, controller, sentryObj)
        return linkaResponse
      case 'Manual Lock':
        logger(`Error: Failed to lock equipment , Dumb lock is not a controller`)
        throw errors.customError(
          'Bike cannot be locked via API',
          responseCodes.BadRequest,
          'BadRequest',
          false
        )
      case 'Edge':
        let lastUserId = null
        if (controller.bike_id) {
          const date = await db.main('trips').where({bike_id: controller.bike_id}).max('date_created as latestDate').first()
          const lastTrip = await db.main('trips').where({bike_id: controller.bike_id, date_created: date.latestDate}).first()
          lastUserId = lastTrip ? lastTrip.user_id : ''

          if (!lastUserId) {
            Sentry.captureException('No user found for bike')
          }
        }
        const EdgeApiHandler = new EdgeApiClient(controller.fleet_id, lastUserId)
        const bikeId = controller.bike_id
        updateEdgeBikeStatus(EdgeApiHandler, bikeId, controller.key)
        const bike = await db.main('bikes').where({ bike_id: bikeId }).first()
        let res
        if (bike.current_status === 'on_trip') {
          const edgeVehicleInfo = await EdgeApiHandler.getVehicleInformation(controller.key)
          if (edgeVehicleInfo.trip) {
            res = await EdgeApiHandler.pauseTrip(edgeVehicleInfo.trip.id)
          } else {
            throw new Error('The Edge Vehicle is not in trip mode')
          }
        } else {
          throw new Error('You cannot lock a vehicle without a trip')
        }
        return res
      default:
        logger(`Error: Failed to lock equipment ${controller.key}, ${controller.vendor} API not supported`)
        throw errors.customError(
          'Equipmemnt cannot be locked via API',
          responseCodes.BadRequest,
          'BadRequest',
          false
        )
    }
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, {bike_id: controller.bike_id})
    }
    if (error.constructor.name === 'LattisError') {
      throw error
    }
    logger(`Error: Failed to lock equipmment, ${error.response ? error.response.data.message : error.message}`)
    throw errors.internalServer(false)
  }
}

const unlockBike = async (bikeId, controllerKey, sentryObj) => {
  const bike = await db.main('bikes')
    .where({ bike_id: bikeId })
    .first()
  let controller
  if (controllerKey) {
    controller = await db.main('controllers')
      .where({ key: controllerKey })
      .first()
  }
  const date = await db.main('trips').where({bike_id: bikeId}).max('date_created as latestDate').first()
  const lastTrip = await db.main('trips').where({bike_id: bikeId, date_created: date.latestDate}).first()
  const lastUserId = lastTrip ? lastTrip.user_id : ''

  if (!lastUserId) {
    Sentry.captureException('No user found for bike')
  }

  const bikeUUID = bike.bike_uuid
  const dockedToHub = await db.main('ports')
    .join('hubs', 'ports.hub_uuid', 'hubs.uuid')
    .select('ports.port_id as portId', 'ports.hub_uuid as hubUUID', 'ports.vehicle_uuid as vehicleUUID', 'ports.status as portStatus', 'ports.number as portNumber', 'hubs.status as hubStatus')
    .where({ 'ports.vehicle_uuid': bikeUUID }).first()

  const controllerType = controller && controller.device_type
  if (controllerType === 'adapter') {
    if (!dockedToHub) return { type: 'adapter', msg: 'This vehicle is not docked' }
    switch (controller.vendor) {
      case 'Kuhmute':
        if (dockedToHub && dockedToHub.hubStatus === 'Available') {
          try {
            const KuhmuteApiClient = new KuhmuteAPI(bike.fleet_id)
            const res = await KuhmuteApiClient.unlockVehicle(bikeUUID)
            if (res && res.charge && !res.charge.docked) { // After unlocking, docked should be false
              return { type: 'adapter', msg: res }
            } else {
              throw errors.customError(
                `Could not undock vehicle ${bikeUUID} from Hub`,
                platform.responseCodes.InternalServer,
                'Integration Error',
                false
              )
            }
          } catch (error) {
            if (sentryObj) {
              sentryObj.captureException(error, {bike_id: bikeUUID})
            }
            logger('Error undocking vehicle from Kuhmute Hub')
            throw errors.customError(
              `Error undocking vehicle ${bikeId} from Hub`,
              platform.responseCodes.InternalServer,
              'Integration Error',
              false
            )
          }
        }
        break
      case 'Duckt':
        if (dockedToHub && dockedToHub.hubStatus === 'Available') {
          try {
            const ducktUser = await setUpApiUser(bike, 'duckt')
            const { Parameter: baseURL } = await getParameterStoreValue('DUCKT/URLS/BASE_URL')
            const { Parameter: callbackUrl } = await getParameterStoreValue('DUCKT/URLS/CALL_BACK_URL')
            if (!_.isEmpty(ducktUser)) {
              DucktApi = new DucktApiClient({
                baseUrl: baseURL.Value,
                password: ducktUser.password,
                userName: ducktUser.userName,
                callbackUrl: callbackUrl.Value
              })
            }
            let res = await DucktApi.unlock(bike)
            if (res) {
              await db.main('ports').update({ status: 'Available', vehicle_uuid: null }).where({ port_id: dockedToHub.portId })
              // After unlocking, docked should be false
              const port = await db
                .main('ports')
                .select('*')
                .where({ port_id: dockedToHub.portId })
                .first()
              return {
                type: 'adapter',
                msg: {
                  port,
                  apiResponse: res
                }
              }
            } else {
              throw errors.customError(
                `Could not undock vehicle ${bikeUUID} from Hub`,
                platform.responseCodes.InternalServer,
                'Integration Error',
                false
              )
            }
          } catch (error) {
            if (sentryObj) {
              sentryObj.captureException(error, {bike_id: bikeUUID})
            }
            logger('Error undocking vehicle from duckt Hub')
            throw errors.customError(
              `Error undocking vehicle ${bikeId} from Hub`,
              platform.responseCodes.InternalServer,
              'Integration Error',
              false
            )
          }
        }
        break
      case 'dck-mob-e':
        return {
          type: 'adapter',
          msg: `It's a dumb adapter. No unlock functionality`
        }
      default:
        logger(`Error undocking vehicle ${bikeId} from Hub. Adapter not supported`)
        throw errors.customError(
          `Error undocking vehicle ${bikeId} from Hub. Adapter not supported`,
          platform.responseCodes.InternalServer,
          'Integration Error',
          false
        )
    }
  }
  try {
    if (!controller) {
      logger(`Error: Failed to unlock bike ${bikeId}, no controller assigned`)
      throw errors.customError(
        'Bike cannot be unlocked via API',
        responseCodes.BadRequest,
        'BadRequest',
        false
      )
    }

    const segwayIntegration = await fleetHandler.getFleetIntegrations(controller.fleet_id, 'segway')
    let segwaySettings
    if (segwayIntegration.length) {
      segwaySettings = segwayIntegration[0].metadata
    }

    switch (controller.vendor) {
      case 'dck-mob-e': {
        try {
          const controllerOnvehicle = await db.main('controllers').where({ bike_id: bike.bike_id }).first()
          const response = await new Greenriders().unlockStation(controller.key, controllerOnvehicle.key)
          return response
        } catch (error) {
          logger('Error undocking vehicle from Greenriders station')
          throw errors.customError(
            `Error undocking vehicle ${bikeId} from Greenrdiers station`,
            platform.responseCodes.InternalServer,
            'IntegrationError',
            true
          )
        }
      }
      case 'Segway':
        const segwayUnlockResponse = await segwayApiClientForUs.unlock(controller.key)
        if (segwaySettings) {
          await segwayApiClientForUs.toggleLights(segwaySettings.controlType, controller.key)
          await segwayApiClientForUs.setSpeedMode(segwaySettings.speedMode ? segwaySettings.speedMode : 2, controller.key)
          await segwayApiClientForUs.setMaximumSpeedLimit(segwaySettings.segwayMaximumSpeedLimit, controller.key)
        }
        // Do not await, run the update without blocking the request
        updateSegwayBikeStatus(segwayApiClientForUs, bikeId, controllerKey, sentryObj)
        return segwayUnlockResponse

      case 'Segway IoT EU':
        const segwayEuUnlockResponse = await segwayApiClientForEu.unlock(controller.key)
        if (segwaySettings) {
          await segwayApiClientForEu.toggleLights(segwaySettings.controlType, controller.key)
          await segwayApiClientForEu.setSpeedMode(segwaySettings.speedMode ? segwaySettings.speedMode : 2, controller.key)
          await segwayApiClientForEu.setMaximumSpeedLimit(segwaySettings.segwayMaximumSpeedLimit, controller.key)
        }
        // Do not await, run the update without blocking the request
        updateSegwayBikeStatus(segwayApiClientForEu, bikeId, controllerKey, sentryObj)
        return segwayEuUnlockResponse

      case 'COMODULE Ninebot ES4':
        const comoduleUnlockResponse = await comoduleApi.unlock(controller.key)
        // Do not await, run the update without blocking the request
        updateComoduleSegwayEs4BikeStatus(bikeId, controllerKey)
        return comoduleUnlockResponse

      case 'ACTON':
      case 'Omni Lock':
      case 'Omni IoT':
      case 'Teltonika':
        const clientApi = actonFamily[controller.vendor]
        const unlockResponse = clientApi.startVehicle(controller.key)
        updateActonBikeStatus(bikeId, controllerKey)
        return unlockResponse
      case 'Geotab IoT':
        const geotabUser = await setUpGeotabApiUser(controller)
        if (geotabUser) {
          GeotabApi = new GeotabApiClient({
            password: geotabUser.password,
            userName: geotabUser.userName,
            sessionId: geotabUser.sessionId,
            database: geotabUser.database,
            serverName: geotabUser.serverName
          })
        }
        const geotabLockResponse = await GeotabApi.unlock(controller)
        const [integrationData] = await getGeotabApiKey({ fleet_id: controller.fleet_id }, sentryObj)
        let metadata = JSON.parse(integrationData.metadata)
        metadata.bike_locked = false
        try {
          await db.main('integrations')
            .update({ metadata: JSON.stringify(metadata) })
            .where({ fleet_id: controller.fleet_id, integration_type: 'geotab' })
        } catch (error) {
          if (sentryObj) {
            sentryObj.captureException(error, {fleet_id: controller.fleet_id})
          }
          logger(`Warn: Unable to update lock status for Geotab with controller ${controller.key}: ${error}`)
          throw errors.customError(
            `Unable to update lock status for Geotab with id ${controller.key}`,
            platform.responseCodes.InternalServer,
            'InternalServer',
            false
          )
        }
        const [message] = geotabLockResponse
        await updateGeotabBikeStatus(bikeId, controller)
        return message
      case 'Linka IoT':
        const linkaMerchant = await setUpLinkaApiClient(bike)
        if (!_.isEmpty(linkaMerchant)) {
          LinkaApi = new LinkaApiClient({
            baseUrl: linkaMerchant.baseUrl,
            merchantUrl: linkaMerchant.merchantUrl,
            apiKey: linkaMerchant.apiKey,
            secretKey: linkaMerchant.secretKey
          })
        }
        const lockResponse = await LinkaApi.unlock(controller.key)
        await updateLinkaBikeStatus(bikeId, controller, sentryObj)
        return lockResponse

      case 'Kisi': {
        const unlockResponse = await new Kisi().unlock(controller.fleet_id, controller.key)
        if (unlockResponse.error) { throw new Error(unlockResponse.error) }
        await updateKisiLockStatus(controller, sentryObj)
        return unlockResponse
      }
      case 'Sentinel': {
        const sentinelClient = new Sentinel()
        const lockStatus = await sentinelClient.fetchLockStatus(controller.key)
        const coordinates = lockStatus.body.geoPoint
        const voltage = lockStatus.body.voltagePercentage
        db.main('controllers').update({...coordinates, battery_level: voltage}).where({key: controller.key})
        db.main('bikes').update({...coordinates, bike_battery_level: voltage}).where({bike_id: controller.bike_id})
        if (lockStatus && lockStatus.code === '000000') {
          const workMode = lockStatus.body.workMode
          if (workMode === 1) {
            throw errors.customError(
              `The lock is in offline mode. Please tap the lock and retry`,
              platform.responseCodes.InternalServer,
              'LockOffline',
              false
            )
          } else {
            const unlockResponse = await sentinelClient.unlock(controller.key)
            if (unlockResponse.code !== '000000') {
              throw errors.customError(
                `Unable to unlock the lock with id ${controller.key}`,
                platform.responseCodes.InternalServer,
                'InternalServer',
                false
              )
            } else if (unlockResponse.code === '000000' && unlockResponse.body) {
              return unlockResponse
            } else if (unlockResponse.code === '000000' && unlockResponse.body === false) {
              throw errors.customError(
                `Unable to unlock the lock with id ${controller.key}`,
                platform.responseCodes.InternalServer,
                'InternalServer',
                false
              )
            }
          }
        }
        return lockStatus
      }
      case 'Edge':
        try {
          let res
          const EdgeApiHandler = new EdgeApiClient(controller.fleet_id, lastUserId)
          if (bike.current_status === 'on_trip') {
            const edgeVehicleInfo = await EdgeApiHandler.getVehicleInformation(controller.key)
            if (edgeVehicleInfo.mode === 'MoM') {
              throw new Error('You can not operate the vehicle in MoM mode')
            }
            if (edgeVehicleInfo.trip) {
              res = await EdgeApiHandler.resumeTrip(edgeVehicleInfo.trip)
            } else {
              throw new Error('The Edge Vehicle is not in trip mode')
            }
          } else {
            throw new Error('You cannot unlock a vehicle without a trip')
          }
          return res
        } catch (error) {
          if (sentryObj) {
            sentryObj.captureException(error, {bike_id: controller.bike_id})
          }
          logger(`Error unlocking Edge vehicle ${controller.bike_id}`)
          throw errors.customError(
            error.message || `Error unlocking Edge vehicle`,
            platform.responseCodes.InternalServer,
            'Integration Error',
            false
          )
        }
      case 'Manual Lock':
        logger(`Error: Failed to unlock bike ${bikeId}, Dumb lock is not a controller`)
        throw errors.customError(
          'Bike cannot be locked via API',
          responseCodes.BadRequest,
          'BadRequest',
          false
        )
      default:
        logger(`Error: Failed to unlock bike ${bikeId}, ${controller.vendor} API not supported`)
        throw errors.customError(
          errors.message || 'Bike cannot be unlocked via API',
          responseCodes.BadRequest,
          'BadRequest',
          false
        )
    }
  } catch (error) {
    logger(`Error: Failed to lock bike, ${error.response ? error.response.data.message : error.message}`)
    throw error
  }
}

const unlockEquipment = async (controller, sentryObj) => {
  if (!controller) {
    logger(`Error: Failed to unlock equipment with Id ${controller.controller_id}`)
    throw errors.customError(
      'Bike cannot be unlocked via API',
      responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
  let bike
  let bikeId
  let lastUserId
  if (controller.bike_id) {
    bikeId = controller.bike_id
    bike = await db.main('bikes').where({ bike_id: bikeId }).first()

    const date = await db.main('trips').where({bike_id: bikeId}).max('date_created as latestDate').first()
    const lastTrip = await db.main('trips').where({bike_id: bikeId, date_created: date.latestDate}).first()
    lastUserId = lastTrip ? lastTrip.user_id : ''

    if (!lastUserId) {
      Sentry.captureException('No user found for bike')
    }
  }
  if (controller.bike_id && controller.device_type === 'adapter') {
    const bikeUUID = bike.bike_uuid
    const dockedToHub = await db.main('ports')
      .join('hubs', 'ports.hub_uuid', 'hubs.uuid')
      .select('ports.port_id as portId', 'ports.hub_uuid as hubUUID', 'ports.vehicle_uuid as vehicleUUID', 'ports.status as portStatus', 'ports.number as portNumber', 'hubs.status as hubStatus')
      .where({ 'ports.vehicle_uuid': bikeUUID }).first()
    if (!dockedToHub) return { type: 'adapter', msg: 'This vehicle is not docked' }
    switch (controller.vendor) {
      case 'Kuhmute':
        if (dockedToHub && dockedToHub.hubStatus === 'Available') {
          try {
            const KuhmuteApiClient = new KuhmuteAPI(bike.fleet_id)
            const res = await KuhmuteApiClient.unlockVehicle(bikeUUID)
            if (res && res.charge && !res.charge.docked) { // After unlocking, docked should be false
              return { type: 'adapter', msg: res }
            } else {
              throw errors.customError(
                `Could not undock vehicle ${bikeUUID} from Hub`,
                platform.responseCodes.InternalServer,
                'Integration Error',
                false
              )
            }
          } catch (error) {
            if (sentryObj) sentryObj.captureException(error, {fleet_id: bike.fleet_id})
            logger('Error undocking vehicle from Kuhmute Hub')
            throw errors.customError(
              `Error undocking vehicle ${bikeId} from Hub`,
              platform.responseCodes.InternalServer,
              'Integration Error',
              false
            )
          }
        }
        break
      case 'Duckt':
        if (dockedToHub && dockedToHub.hubStatus === 'Available') {
          try {
            const ducktUser = await setUpApiUser(bike, 'duckt')
            const { Parameter: baseURL } = await getParameterStoreValue('DUCKT/URLS/BASE_URL')
            const { Parameter: callbackUrl } = await getParameterStoreValue('DUCKT/URLS/CALL_BACK_URL')
            if (!_.isEmpty(ducktUser)) {
              DucktApi = new DucktApiClient({
                baseUrl: baseURL.Value,
                password: ducktUser.password,
                userName: ducktUser.userName,
                callbackUrl: callbackUrl.Value
              })
            }
            let res = await DucktApi.unlock(bike)
            if (res) {
              await db.main('ports').update({ status: 'Available', vehicle_uuid: null }).where({ port_id: dockedToHub.portId })
              // After unlocking, docked should be false
              const port = await db
                .main('ports')
                .select('*')
                .where({ port_id: dockedToHub.portId })
                .first()
              return {
                type: 'adapter',
                msg: {
                  port,
                  apiResponse: res
                }
              }
            } else {
              throw errors.customError(
                `Could not undock vehicle ${bikeUUID} from Hub`,
                platform.responseCodes.InternalServer,
                'Integration Error',
                false
              )
            }
          } catch (error) {
            if (sentryObj) {
              sentryObj.captureException(error, {fleet_id: bike.fleet_id})
            }
            logger('Error undocking vehicle from duckt Hub')
            throw errors.customError(
              `Error undocking vehicle ${bikeId} from Hub`,
              platform.responseCodes.InternalServer,
              'Integration Error',
              false
            )
          }
        }
        break
      default:
        logger(`Error undocking vehicle ${bikeId} from Hub. Adapter not supported`)
        throw errors.customError(
          `Error undocking vehicle ${bikeId} from Hub. Adapter not supported`,
          platform.responseCodes.InternalServer,
          'Integration Error',
          false
        )
    }
  }

  try {
    const segwayIntegration = await fleetHandler.getFleetIntegrations(controller.fleet_id, 'segway')
    let segwaySettings
    if (segwayIntegration.length) {
      segwaySettings = segwayIntegration[0].metadata
    }

    switch (controller.vendor) {
      case 'Segway':
        const segwayUnlockResponse = await segwayApiClientForUs.unlock(controller.key)
        if (segwaySettings) {
          await segwayApiClientForUs.toggleLights(segwaySettings.controlType, controller.key)
          await segwayApiClientForUs.setSpeedMode(segwaySettings.speedMode ? segwaySettings.speedMode : 2, controller.key)
          await segwayApiClientForUs.setMaximumSpeedLimit(segwaySettings.segwayMaximumSpeedLimit, controller.key)
        }
        // Do not await, run the update without blocking the request
        if (controller.bike_id) {
          updateSegwayBikeStatus(segwayApiClientForUs, controller.bike_id, controller.key, sentryObj)
        } else {
          updateSegwayBikeStatus(segwayApiClientForUs, undefined, controller.key, sentryObj)
        }
        return segwayUnlockResponse

      case 'Segway IoT EU':
        const segwayEuUnlockResponse = await segwayApiClientForEu.unlock(controller.key)
        if (segwaySettings) {
          await segwayApiClientForEu.toggleLights(segwaySettings.controlType, controller.key)
          await segwayApiClientForEu.setSpeedMode(segwaySettings.speedMode ? segwaySettings.speedMode : 2, controller.key)
          await segwayApiClientForEu.setMaximumSpeedLimit(segwaySettings.segwayMaximumSpeedLimit, controller.key)
        }
        // Do not await, run the update without blocking the request
        if (controller.bike_id) {
          updateSegwayBikeStatus(segwayApiClientForUs, controller.bike_id, controller.key, sentryObj)
        } else {
          updateSegwayBikeStatus(segwayApiClientForUs, undefined, controller.key, sentryObj)
        }
        return segwayEuUnlockResponse

      case 'COMODULE Ninebot ES4':
        const comoduleUnlockResponse = await comoduleApi.unlock(controller.key)
        // Do not await, run the update without blocking the request
        updateComoduleSegwayEs4BikeStatus(controller.bike_id, controller.key)
        return comoduleUnlockResponse

      case 'ACTON':
      case 'Omni Lock':
      case 'Omni IoT':
      case 'Teltonika':
      case 'Okai':
        const clientApi = actonFamily[controller.vendor]
        const unlockResponse = clientApi.startVehicle(controller.key)
        updateActonBikeStatus(controller.bike_id, controller.key)
        return unlockResponse
      case 'Geotab IoT':
        const geotabUser = await setUpGeotabApiUser(controller)
        if (geotabUser) {
          GeotabApi = new GeotabApiClient({
            password: geotabUser.password,
            userName: geotabUser.userName,
            sessionId: geotabUser.sessionId,
            database: geotabUser.database,
            serverName: geotabUser.serverName
          })
        }
        const geotabLockResponse = await GeotabApi.unlock(controller)
        const [integrationData] = await getGeotabApiKey({ fleet_id: controller.fleet_id }, sentryObj)
        let metadata = JSON.parse(integrationData.metadata)
        metadata.bike_locked = false
        try {
          await db.main('integrations')
            .update({ metadata: JSON.stringify(metadata) })
            .where({ fleet_id: controller.fleet_id, integration_type: 'geotab' })
        } catch (error) {
          if (sentryObj) {
            sentryObj.captureException(error, {fleet_id: controller.fleet_id})
          }
          logger(`Warn: Unable to update lock status for Geotab with controller ${controller.key}: ${error}`)
          throw errors.customError(
            `Unable to update lock status for Geotab with id ${controller.key}`,
            platform.responseCodes.InternalServer,
            'InternalServer',
            false
          )
        }
        const [message] = geotabLockResponse
        await updateGeotabBikeStatus(controller.bike_id, controller)
        return message
      case 'Linka IoT':
        const linkaMerchant = await setUpLinkaApiClient(bike)
        if (!_.isEmpty(linkaMerchant)) {
          LinkaApi = new LinkaApiClient({
            baseUrl: linkaMerchant.baseUrl,
            merchantUrl: linkaMerchant.merchantUrl,
            apiKey: linkaMerchant.apiKey,
            secretKey: linkaMerchant.secretKey
          })
        }
        const lockResponse = await LinkaApi.unlock(controller.key)
        await updateLinkaBikeStatus(controller.bike_id, controller, sentryObj)
        return lockResponse

      case 'Kisi': {
        const unlockResponse = await new Kisi().unlock(controller.fleet_id, controller.key)
        if (unlockResponse.error) { throw new Error(unlockResponse.error) }
        updateKisiLockStatus(controller, sentryObj)
        return unlockResponse
      }
      case 'Sentinel': {
        const sentinelClient = new Sentinel()
        const lockStatus = await sentinelClient.fetchLockStatus(controller.key)
        const coordinates = lockStatus.body.geoPoint
        const voltage = lockStatus.body.voltagePercentage
        db.main('controllers').update({...coordinates, battery_level: voltage}).where({key: controller.key})
        if (controller.bike_id) db.main('bikes').update({...coordinates, bike_battery_level: voltage}).where({bike_id: controller.bike_id})
        if (lockStatus && lockStatus.code === '000000') {
          const workMode = lockStatus.body.workMode
          if (workMode === 1) {
            throw errors.customError(
              `The lock is in offline mode. Please tap the lock and retry`,
              platform.responseCodes.InternalServer,
              'LockOffline',
              false
            )
          } else {
            const unlockResponse = await sentinelClient.unlock(controller.key)
            if (unlockResponse.code !== '000000') {
              throw errors.customError(
                `Unable to unlock the lock with id ${controller.key}`,
                platform.responseCodes.InternalServer,
                'InternalServer',
                false
              )
            } else if (unlockResponse.code === '000000' && unlockResponse.body) {
              return unlockResponse
            } else if (unlockResponse.code === '000000' && unlockResponse.body === false) {
              throw errors.customError(
                `Unable to unlock the lock with id ${controller.key}`,
                platform.responseCodes.InternalServer,
                'InternalServer',
                false
              )
            }
          }
        }
        return lockStatus
      }
      case 'ParcelHive':
        const user = await setUpApiUser(controller, controller.vendor)
        parcelHiveApiClient = new ParcelHiveApiClient({
          accessToken: user.accessToken,
          lockerId: JSON.parse(controller.metadata).lockerId,
          boxId: JSON.parse(controller.metadata).boxId
        })
        const parcelHiveResponse = await parcelHiveApiClient.open(controller)
        return parcelHiveResponse
      case 'Manual Lock':
        logger(`Error: Failed to unlock vehicle, Dumb lock is not a controller`)
        throw errors.customError(
          'Bike cannot be locked via API',
          responseCodes.BadRequest,
          'BadRequest',
          false
        )
      case 'Edge':
        const EdgeApiHandler = new EdgeApiClient(controller.fleet_id, lastUserId)
        updateEdgeBikeStatus(EdgeApiHandler, bike.bike_id, controller.key)
        let res
        if (bike && bike.current_status === 'on_trip') {
          const edgeVehicleInfo = await EdgeApiHandler.getVehicleInformation(controller.key)
          if (edgeVehicleInfo.mode === 'MoM') {
            throw new Error('You can not operate the vehicle in MoM mode')
          }
          if (edgeVehicleInfo.trip) {
            res = await EdgeApiHandler.resumeTrip(edgeVehicleInfo.trip.id)
          } else if (!edgeVehicleInfo.trip) {
            res = await EdgeApiHandler.startTrip(controller.key || edgeVehicleInfo.id)
            const edgeTripId = res.trip.id
            if (bike.current_trip_id) {
              db.main('trips').update({ edge_trip_id: edgeTripId }).where({ trip_id: bike.current_trip_id })
            }
          }
        } else {
          throw new Error('You cannot lock a vehicle without a trip')
        }
        return res
      default:
        logger(`Error: Failed to unlock equipment, ${controller.controller_id} API not supported`)
        throw errors.customError(
          'Equipment cannot be unlocked via API',
          responseCodes.BadRequest,
          'BadRequest',
          false
        )
    }
  } catch (error) {
    if (sentryObj) sentryObj.captureException(error, {fleet_id: controller.fleet_id})
    logger(`Error: Failed to unlock equipment, ${error.response ? error.response.data.message : error.message}`)
    throw error
  }
}

const lockBikeAsUser = async (bikeId, userId, sentryObj) => {
  try {
    const trip = await db.main('trips')
      .where({
        bike_id: bikeId,
        user_id: userId,
        date_endtrip: null
      })
      .first()

    if (!trip) {
      logger(`Error: Failed to lock bike ${bikeId}, user ${userId} is not using it`)
      throw errors.customError('User cannot lock bike they are not using', responseCodes.Unauthorized, false)
    }

    return await lockBike(bikeId)
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, {bike_id: bikeId})
    }
    logger(`Error: Failed to lock bike, ${error}`)
    throw errors.customError(
      error.message || `Failed to lock bike ${bikeId}`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      false
    )
  }
}

const lockEquipmentAsUser = async (userId, controller, sentryObj) => {
  try {
    const whereClause = {}
    if (controller.bike_id) whereClause.bike_id = controller.bike_id
    else if (controller.port_id) whereClause.port_id = controller.port_id
    else if (controller.hub_id) whereClause.hub_id = controller.hub_id
    const tripWhereClause = { user_id: userId, date_endtrip: null, ...whereClause }
    const trip = await db.main('trips').where(tripWhereClause).first()

    if (!trip) {
      logger(`Error: Failed to lock equipment, user ${userId} is not using it`)
      throw errors.customError('User cannot lock equipment they are not using', responseCodes.Unauthorized, false)
    }

    return await lockEquipment(controller)
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, {fleet_id: controller.fleet_id})
    }
    logger(`Error: Failed to lock equipment, ${error}`)
    throw errors.customError(
      error.message || `Failed to lock equipment ${controller.controller_id}`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      false
    )
  }
}

const unlockBikeAsUser = async (bikeId, userId, controllerKey, sentryObj) => {
  try {
    const trip = await db.main('trips')
      .where({
        bike_id: bikeId,
        user_id: userId,
        date_endtrip: null
      })
      .first()

    // For free fleets
    const noPaymentFleet = await db.main.select('bikes.bike_id', 'bikes.bike_name', 'fleets.fleet_id', 'fleets.fleet_name', 'fleets.type').from('bikes').join('fleets', 'bikes.fleet_id', 'fleets.fleet_id').whereIn('fleets.type', [fleetConstants.fleet_type.privateWithNoPayment, fleetConstants.fleet_type.publicWithNoPayment]).andWhere('bikes.bike_id', bikeId).first()
    let activeBooking
    if (noPaymentFleet) {
      activeBooking = await db.main('booking').where({ 'bike_id': bikeId, 'user_id': userId, 'status': tripConstants.booking.active })
    }

    if (!trip && !(noPaymentFleet && activeBooking)) {
      logger(`Error: Failed to unlock bike ${bikeId}, ${userId} is not using it`)
      throw errors.customError('User cannot unlock bike they are not using', responseCodes.Unauthorized, false)
    }

    return await unlockBike(bikeId, controllerKey)
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, {bike_id: bikeId})
    }
    logger(`Error: Failed to unlock bike, ${error}`)
    throw errors.customError(
      error.message || `Failed to unlock bike ${bikeId}`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      false
    )
  }
}

const unlockEquipmentAsUser = async (userId, controller, sentryObj) => {
  try {
    const whereClause = {}
    if (controller.bike_id) whereClause.bike_id = controller.bike_id
    else if (controller.port_id) whereClause.port_id = controller.port_id
    else if (controller.hub_id) whereClause.hub_id = controller.hub_id
    const tripWhereClause = { user_id: userId, date_endtrip: null, ...whereClause }
    const trip = await db.main('trips').where(tripWhereClause).first()

    // For free fleets
    const fleetId = controller.fleet_id
    const noPaymentFleet = await db.main.select('fleets.fleet_id', 'fleets.fleet_name', 'fleets.type').from('fleets').whereIn('fleets.type', [fleetConstants.fleet_type.privateWithNoPayment, fleetConstants.fleet_type.publicWithNoPayment]).andWhere('fleets.fleet_id', fleetId).first()
    let activeBooking
    if (noPaymentFleet) {
      activeBooking = await db.main('booking').where({ ...whereClause, 'user_id': userId, 'status': tripConstants.booking.active })
    }

    if (!trip && !(noPaymentFleet && activeBooking)) {
      logger(`Error: Failed to unlock the equipment ${controller.controller_id}, ${userId} is not using it`)
      throw errors.customError('User cannot unlock equipment they are not using', responseCodes.Unauthorized, true)
    }

    return await unlockEquipment(controller, sentryObj)
  } catch (error) {
    if (sentryObj) sentryObj.captureException(error, {fleet_id: controller.fleet_id})
    throw errors.customError(
      error.message || `Failed to unlock equipment ${controller.controller_id}`,
      error.code || platform.responseCodes.InternalServer,
      error.name || 'InternalServerError',
      true
    )
  }
}

const setBikeLights = async (controlType, controller, sentryObj) => {
  let lights
  try {
    if (controller.vendor === 'Segway') {
      lights = await segwayApiClientForUs.toggleLights(controlType, controller.key)
    } else {
      lights = await segwayApiClientForEu.toggleLights(controlType, controller.key)
    }
    return lights
  } catch (error) {
    sentryObj.captureException(error, {controller_id: controller.controller_id})
    logger(`Error: unable to toggle lights for segway: ${error}`)
    throw error
  }
}

const getAppFleets = async (appType, sentryObj) => {
  try {
    const appFleets = await db.main('whitelabel_applications')
      .where({ app_type: appType })
      .innerJoin('fleet_metadata', 'fleet_metadata.whitelabel_id', 'whitelabel_applications.app_id')
      .select('fleet_id')

    return appFleets.map(record => record.fleet_id)
  } catch (error) {
    sentryObj.captureException(error, {app_type: appType})
    logger(`Error: Failed to get fleet_ids for that application: ${error}`)
    throw error
  }
}

const getLockCode = async (bikeId, sentryObj) => {
  try {
    const lockDetails = await db.main('controllers')
      .where({ bike_id: bikeId, vendor: 'Manual Lock' })
      .first()

    // this part ensures backward compatibility for locks that have not been migrated to use metadata as oposed to key
    const code = lockDetails.metadata ? JSON.parse(lockDetails.metadata).key : lockDetails.key
    return { lockCode: code }
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, { bikeId })
    }
    logger(`Error: Failed to get fleet_ids for that application: ${error}`)
    throw error
  }
}

const getTrackerStatus = async (controllerId, sentryObj) => {
  try {
    const controller = await db.main('controllers')
      .where({ controller_id: controllerId })
      .first()

    if (!controller) {
      throw errors.customError('Controller does not exist', responseCodes.ResourceNotFound, 'BadRequest', false)
    }

    switch (controller.vendor) {
      case 'Nimbelink': {
        const vehicleState = await getLatestDataForController(controller.key.toLowerCase())
        if (!vehicleState) {
          throw errors.customError(
            'Controller status undefined. Probable timeout',
            platform.responseCodes.InternalServer,
            'Device Error',
            false
          )
        }
        const lastLocation = vehicleState.data.loc.pop()
        updateNimbelinkTrackerStatus(controllerId, sentryObj)
        return {
          latitude: lastLocation.lat,
          longitude: lastLocation.lon,
          signal: vehicleState.data.deviceStatus[0].signal,
          network: vehicleState.data.deviceStatus[0].network,
          battery_percent: vehicleState.data.deviceStatus[0].estBattPct,
          last_update_time: lastLocation.ts
        }
      }

      default:
        throw errors.customError(`Unknown IoT vendor: ${controller.vendor}`, responseCodes.BadRequest, 'BadRequest', false)
    }
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, {controllerId})
    }
    if (error.constructor.name === 'LattisError') {
      logger(`Error: Failed to get controller status: ${error.message}`)
      throw error
    }
    logger(`Error: Failed to get controller status: ${error.response ? error.response.data.message : error.message}`)
    throw errors.internalServer(false)
  }
}

const getIotStatus = async (bikeId = null, userId, controllerKey, fleetId = null, sentryObj) => {
  try {
    let bikeAndFleet
    let fleet
    let lastUserId
    if (bikeId) {
      bikeAndFleet = await db.main('bikes')
        .join('fleets', 'bikes.fleet_id', 'fleets.fleet_id')
        .where({ bike_id: bikeId })
        .first()

      if (!bikeAndFleet) throw Error(`Bike with ID ${bikeId} not found`)

      const date = await db.main('trips').where({bike_id: bikeId}).max('date_created as latestDate').first()
      const lastTrip = await db.main('trips').where({bike_id: bikeId, date_created: date.latestDate}).first()
      lastUserId = lastTrip ? lastTrip.user_id : ''

      if (!lastUserId) {
        Sentry.captureException('No user found for bike')
      }
    }

    if (fleetId) {
      fleet = await db.main('fleets').where({fleet_id: fleetId}).first()
    }

    const isPublic = (bikeAndFleet && bikeAndFleet.type.startsWith('public')) || (fleet && fleet.type.startsWith('public'))

    const privateFleetUser = await db.users('private_fleet_users')
      .where({
        user_id: userId,
        fleet_id: (bikeAndFleet && bikeAndFleet.fleet_id) || fleetId
      }).first()

    const userHasAccess = !!(privateFleetUser && privateFleetUser.access)

    if (!isPublic && !userHasAccess) {
      throw errors.customError('User does not have access to that fleet', responseCodes.Unauthorized, false)
    }

    let controller
    if (controllerKey) {
      controller = await db.main('controllers')
        .where({ key: controllerKey })
        .first()
    } else {
      controller = await db.main('controllers')
        .where({ bike_id: bikeId, device_type: 'iot' })
        .first()
    }

    if (!controller) {
      throw errors.customError('Bike IoT not configured', responseCodes.ResourceNotFound, 'BadRequest', false)
    }

    const normalizeIoTStatus = status => ({
      latitude: status.latitude,
      longitude: status.longitude,
      online: status.online,
      locked: status.locked,
      battery_percent: parseInt(Number(status.powerPercent).toFixed()) || 0,
      bike_battery_percent: parseInt(Number(status.powerPercent).toFixed()) || 0,
      last_update_time: status.gpsUtcTime
    })

    switch (controller.vendor) {
      case 'Segway': {
        const status = await segwayApiClientForUs.getVehicleInformation(controller.key)
        updateSegwayBikeStatus(segwayApiClientForUs, bikeId, controller.key, sentryObj)
        const normalized = normalizeIoTStatus(status)
        return normalized
      }

      case 'Segway IoT EU': {
        const status = await segwayApiClientForEu.getVehicleInformation(controller.key, sentryObj)
        updateSegwayBikeStatus(segwayApiClientForEu, bikeId, controller.key, sentryObj)
        const normalized = normalizeIoTStatus(status)
        return normalized
      }
      case 'Edge': {
        const EdgeApiHandler = new EdgeApiClient(controller.fleet_id, lastUserId)
        const result = await EdgeApiHandler.getVehicleInformation(controller.key)
        const status = normalizeIoTStatus(result)
        return status
      }

      case 'ACTON':
      case 'Omni IoT':
      case 'Teltonika':
      case 'Okai':
      case 'Omni Lock': {
        const clientApi = actonFamily[controller.vendor]
        const vehicleState = await clientApi.getCurrentStatus(controller.key)
        updateActonBikeStatus(bikeId)
        const result = {
          latitude: vehicleState.lat,
          longitude: vehicleState.lon,
          online: vehicleState.ignitionOn,
          locked: !vehicleState.ignitionOn,
          battery_percent: vehicleState.iotBattery,
          bike_battery_percent: vehicleState.extendedData.vehicleBattery || vehicleState.extendedData.scooterBattery,
          last_update_time: vehicleState.gpsTimestamp
        }
        const status = normalizeIoTStatus(result)
        return status
      }

      case 'COMODULE Ninebot ES4': {
        const vehicleState = await comoduleApi.getVehicleState(controller.key)
        const result = {
          latitude: vehicleState.gpsLatitude,
          longitude: vehicleState.gpsLongitude,
          online: vehicleState.modulePowerOn,
          locked: !vehicleState.vehiclePowerOn,
          battery_percent: vehicleState.moduleBatteryPercentage,
          bike_battery_percent: vehicleState.vehicleBatteryPercentage,
          last_update_time: vehicleState.lastUpdate
        }
        const status = normalizeIoTStatus(result)
        return status
      }

      case 'Geotab IoT': {
        const geotabUser = await setUpGeotabApiUser(controller)
        if (geotabUser) {
          GeotabApi = new GeotabApiClient({
            password: geotabUser.password,
            userName: geotabUser.userName,
            sessionId: geotabUser.sessionId,
            database: geotabUser.database,
            serverName: geotabUser.serverName
          })
        }
        const [currentStatus] = await GeotabApi.getCurrentStatus(controller)
        const [integrationDetails] = await getGeotabApiKey({ fleet_id: controller.fleet_id }, sentryObj)
        const metadata = JSON.parse(integrationDetails.metadata)
        return {
          latitude: currentStatus.latitude,
          longitude: currentStatus.longitude,
          locked: metadata.bike_locked === undefined ? true : metadata.bike_locked,
          last_update_time: currentStatus.dateTime
        }
      }

      case 'Nimbelink':
        await getDeviceConfig(controller.key.toLowerCase())
        return getTrackerStatus(controller.id, sentryObj)

      case 'Linka IoT':
        const linkaMerchant = await setUpLinkaApiClient(controller)
        if (!_.isEmpty(linkaMerchant)) {
          LinkaApi = new LinkaApiClient({
            baseUrl: linkaMerchant.baseUrl,
            merchantUrl: linkaMerchant.merchantUrl,
            apiKey: linkaMerchant.apiKey,
            secretKey: linkaMerchant.secretKey
          })
        }

        const currentLinkaStatus = await LinkaApi.getCurrentLocation(controller.key)
        let isLinkaLocked = false
        if (currentLinkaStatus.lock_state) {
          isLinkaLocked = currentLinkaStatus.lock_state === 'Locked'
        }
        return {
          latitude: currentLinkaStatus.latitude,
          longitude: currentLinkaStatus.longitude,
          locked: isLinkaLocked,
          last_update_time: currentLinkaStatus.modifiedAt
        }

      case 'Kisi':
        const lockStatus = await new Kisi().fetchLock(controller.fleet_id, controller.key)
        if (lockStatus.error) throw new Error(lockStatus.error)
        updateKisiLockStatus(controller, sentryObj)
        return {
          latitude: lockStatus.latitude || lockStatus.place.latitude,
          longitude: lockStatus.longitude || lockStatus.place.longitude,
          online: lockStatus.online,
          locked: !lockStatus.unlocked,
          last_update_time: lockStatus.updated_at
        }
      case 'Sentinel':
        const { body: sentinelStatus } = await new Sentinel().fetchLockStatus(controllerKey)
        if (!sentinelStatus) throw errors.customError(`Sentinel status not found`, responseCodes.ResourceNotFound, 'MissingApiStatus', true)
        return {
          latitude: sentinelStatus.geoPoint.latitude,
          longitude: sentinelStatus.geoPoint.longitude,
          online: Boolean(sentinelStatus.connetctionStatus),
          locked: Boolean(sentinelStatus.lockStatus),
          last_update_time: sentinelStatus.lastReceivedDataTime
        }
      case 'ParcelHive':
        const lockerId = JSON.parse(controller.metadata).lockerId
        const boxId = JSON.parse(controller.metadata).boxId
        const user = await setUpApiUser(controller, controller.vendor)
        parcelHiveApiClient = new ParcelHiveApiClient({
          accessToken: user.accessToken,
          lockerId: lockerId,
          boxId: boxId
        })
        const locker = await parcelHiveApiClient.getLocker(lockerId, controller)
        const box = await parcelHiveApiClient.getBox(lockerId, boxId, controller)
        const coordinates = locker && locker.locker_location_gps.split(',')
        return {
          latitude: coordinates[0],
          longitude: coordinates[1],
          online: box.box_active === '1',
          locked: box.box_is_opened === '0',
          last_update_time: moment().unix()
        }
      default:
        throw errors.customError(`Unknown IoT vendor: ${controller.vendor}`, responseCodes.BadRequest, 'BadRequest', true)
    }
  } catch (error) {
    if (sentryObj) sentryObj.captureException(error)
    logger(`Error: Failed to get bike status: ${error.response ? error.response.data.message : error.message}`)
    throw errors.customError(
      error.message || 'Failed to get bike status',
      platform.responseCodes.InternalServer,
      'DeviceError',
      true
    )
  }
}

const getLinkaCommandStatus = async (commandId, bikeId, sentryObj) => {
  const controller = await db.main('controllers')
    .where({ bike_id: bikeId })
    .first()
  const linkaMerchant = await setUpLinkaApiClient(controller)
  if (!_.isEmpty(linkaMerchant)) {
    LinkaApi = new LinkaApiClient({
      baseUrl: linkaMerchant.baseUrl,
      merchantUrl: linkaMerchant.merchantUrl,
      apiKey: linkaMerchant.apiKey,
      secretKey: linkaMerchant.secretKey
    })
  }
  try {
    return await LinkaApi.getCurrentStatus(commandId)
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, {commandId, bikeId})
    }
    logger(
      `Error: failed to fetch status of Linka command: ${error.message}`
    )
    throw errors.customError(
      `Failed to retrieve command status`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

const getTapkeyCredentials = async (details, sentryObj) => {
  try {
    const {
      passPhrase,
      tokenIssuer,
      idpClientId,
      provider,
      ownerAccountId,
      authCodeClientSecret,
      tapKeyPrivateKey,
      lattisRefreshToken,
      grantType,
      loginUrl,
      baseUrl,
      redirectUri,
      metadata,
      operatorAccountId,
      authCodeClientId
    } = await fetchTapKeyConfiguration(details)
    const tapKeyApiClient = new TapKeyApiClient({
      idpClientId: idpClientId.Value,
      grantType: grantType.Value,
      provider: provider.Value,
      loginUrl: loginUrl.Value,
      passPhrase: passPhrase.Value,
      tokenIssuer: tokenIssuer.Value,
      refreshToken: metadata.refreshToken,
      lattisRefreshToken: lattisRefreshToken.Value,
      accessToken: metadata.accessToken,
      baseUrl: baseUrl.Value,
      ownerAccountId: ownerAccountId.Value,
      operatorAccountId: operatorAccountId.ownerAccountId,
      authCodeClientId: authCodeClientId.Value,
      authCodeClientSecret: authCodeClientSecret.Value,
      tapKeyPrivateKey: tapKeyPrivateKey.Value,
      redirectUri: redirectUri.Value
    })
    return await tapKeyApiClient.getCredentials(details)
  } catch (error) {
    if (sentryObj) {
      sentryObj.captureException(error, {details})
    }
    throw errors.customError(
      `Failed to retrieve accessToken: ${error}`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}
const getSasCredentials = async (details, sentryObj) => {
  try {
    const {
      clientId,
      secretKey,
      baseUrl,
      domainName,
      userId,
      accessRights
    } = await fetchSasConfig(details)
    const sasClient = new SasApiClient({
      clientId,
      secretKey,
      baseUrl,
      domainName,
      userId,
      accessRights
    })
    return await sasClient.getCredentials(details)
  } catch (error) {
    sentryObj.captureException(error, {details})
    throw errors.customError(
      `Failed to retrieve accessToken: ${error}`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

module.exports = Object.assign(module.exports, {
  getElectricBikes: getElectricBikes,
  updateElectricBike: updateElectricBike,
  updateMetadata: updateMetadata,
  findBikes: findBikes,
  getBikesWithLock: getBikesWithLock,
  getBikeWithLock: getBikeWithLock,
  getBike: getBike,
  getBikes: getBikes,
  getBikeDetails: getBikeDetails,
  updateBike: updateBike,
  formatLockWithBikeForWire: formatLockWithBikeForWire,
  formatLockWithBikesForWire: formatLockWithBikesForWire,
  getBikesWithFleets: getBikesWithFleets,
  assignLock: assignLock,
  unAssignLock: unAssignLock,
  getBikeGroups: getBikeGroups,
  getStagingBikeGroups: getStagingBikeGroups,
  changeBikeStatus: changeBikeStatus,
  searchBikes: searchBikes,
  searchBikesByCoordinates: searchBikesByCoordinates,
  checkForServiceDue: checkForServiceDue,
  getLockByQrCode: getLockByQrCode,
  changeLabel: changeLabel,
  checkForBikeIssues: checkForBikeIssues,
  assignController,
  assignControllerToRandomBike,
  lockBike,
  getAppFleets,
  unlockBike,
  lockBikeAsUser,
  unlockBikeAsUser,
  getIotStatus,
  setBikeLights,
  getLockCode,
  getLinkaCommandStatus,
  getVehicleInfo,
  getTapkeyCredentials,
  getSasCredentials,
  findBikesByName,
  updateKisiLockStatus,
  getRentalDetails,
  unlockEquipmentAsUser,
  lockEquipmentAsUser
})
