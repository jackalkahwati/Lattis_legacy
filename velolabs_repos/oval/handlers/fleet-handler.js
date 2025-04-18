'use strict'

const platform = require('@velo-labs/platform')
const queryCreator = platform.queryCreator
const sqlPool = platform.sqlPool
const config = require('./../config')
const errors = platform.errors
const logger = platform.logger
const dbConstants = platform.dbConstants
const encryptionHandler = platform.encryptionHandler
const parkingHandler = require('./parking-handler')
const paymentHandler = require('./payment-handler')
const pointHandler = platform.pointHandler
const fleetConstants = require('../constants/fleet-constants')
const _ = require('underscore')
const moment = require('moment')
const db = require('../db')
const dbHelpers = require('../helpers/db')
const {configureSentry} = require('../utils/configureSentry')

const Sentry = configureSentry()

const getFleets = async (
  { fleet_id: fleetId, includeMetadata = false, includePricingOptions = true },
  callback
) => {
  const qb = db
    .main('fleets')
    .whereIn('fleets.fleet_id', Array.isArray(fleetId) ? fleetId : [fleetId])
    .leftOuterJoin(
      'fleet_payment_settings',
      'fleet_payment_settings.fleet_id',
      'fleets.fleet_id'
    )

  if (includePricingOptions) {
    qb.leftOuterJoin('pricing_options', function () {
      this.on('pricing_options.fleet_id', '=', 'fleets.fleet_id').andOnNull(
        'pricing_options.deactivated_at'
      )
    })
  }

  if (includeMetadata) {
    qb.leftOuterJoin(
      'fleet_metadata',
      'fleet_metadata.fleet_id',
      'fleets.fleet_id'
    )
  }

  qb.options({ nestTables: true, typeCast: dbHelpers.castDataTypes }).select()

  try {
    const fleets = (await qb).reduce(
      // fleets will be duplicated as many times as there are duplicate related
      // entities. Cannot use `groupBy` in the db query, since then some pricing
      // options may be omitted. Manually deduplicate by fleet_id in this reduce.
      (
        fleets,
        {
          fleets: nextFleet,
          fleet_payment_settings: paymentSettings,
          fleet_metadata: metadata,
          pricing_options: pricingOption
        }
      ) => {
        const fleet = fleets[nextFleet.fleet_id] || nextFleet

        if ((pricingOption || {}).pricing_option_id) {
          // has a pricing option?
          if (fleet.pricing_options) {
            fleet.pricing_options.push(pricingOption)
          } else {
            fleet.pricing_options = [pricingOption]
          }
        }

        if (!(paymentSettings || {}).fleet_id) {
          // no payment settings?
          fleets[fleet.fleet_id] = fleet // replace (unnecessary due to mutation?).

          return fleets
        }

        const hasMeta = metadata && metadata.fleet_metadata_id

        const fleetWithPayment = Object.assign(
          {},
          fleet,
          paymentSettings,
          hasMeta ? metadata : {}
        )

        if (paymentSettings && paymentSettings.usage_surcharge === 'No') {
          fleetWithPayment.excess_usage_fees = 0.00
          fleetWithPayment.excess_usage_type_after_value = 0.00
          fleetWithPayment.excess_usage_type_value = 0.00
          fleetWithPayment.excess_usage_type = ''
          fleetWithPayment.excess_usage_type_after_type = ''
        } else {
          fleetWithPayment.excess_usage_fees = paymentSettings.excess_usage_fees
          fleetWithPayment.excess_usage_type_after_value = paymentSettings.excess_usage_type_after_value
          fleetWithPayment.excess_usage_type_value = paymentSettings.excess_usage_type_value
          fleetWithPayment.excess_usage_type = paymentSettings.excess_usage_type
          fleetWithPayment.excess_usage_type_after_type = paymentSettings.excess_usage_type_after_type
        }

        fleetWithPayment.enable_preauth = !!fleetWithPayment.enable_preauth

        fleets[fleet.fleet_id] = fleetWithPayment

        return fleets
      },
      {}
    )

    return callback(null, Object.values(fleets))
  } catch (error) {
    Sentry.captureException(error)
    logger('Error fetching fleets', errors.errorWithMessage(error))

    return callback(errors.internalServer(true))
  }
}

const getFleetAssociations = (fleetAssociationDetail, callback) => {
  const query = queryCreator.selectWithAnd(dbConstants.tables.fleet_associations, null, fleetAssociationDetail)
  sqlPool.makeQuery(query, (error, fleetAssociations) => {
    if (error) {
      Sentry.captureException(error, {query})
      logger('Error: failed to fetch fleet associations with:', fleetAssociationDetail, 'with error:', error)
      callback(errors.internalServer(false), null)
      return
    }
    callback(null, fleetAssociations)
  })
}

const getFleetPaymentSettings = (columns, comparison, callback) => {
  const fleetPaymentSettingsQuery = queryCreator.selectWithAnd(dbConstants.tables.fleet_payment_settings, columns, comparison)
  sqlPool.makeQuery(fleetPaymentSettingsQuery, callback)
}

const getCustomers = (customerDetails, callback) => {
  const getCustomersQuery = queryCreator.selectWithAnd('customers', null, customerDetails)
  sqlPool.makeQuery(getCustomersQuery, (error, customers) => {
    if (error) {
      Sentry.captureException(error, {query: getCustomersQuery})
      logger('Error: failed to fetch customers with:', customerDetails, 'with error:', error)
      callback(errors.internalServer(true), null)
      return
    }
    callback(errors.noError(), customers)
  })
}

const formatFleetsForWire = (fleets) => {
  const formattedFleets = []
  _.each(fleets, (fleet) => {
    formattedFleets.push(formatFleetForWire(fleet))
  })
  return formattedFleets
}

const formatFleetForWire = (fleet) => {
  fleet.key = fleet.key ? encryptionHandler.decryptDbValue(fleet.key) : fleet.key
  return fleet
}

const getFleetKey = (fleetId, callback) => {
  getFleets({ fleet_id: fleetId }, (error, fleets) => {
    if (error) {
      Sentry.captureException(error)
      callback(error, null)
    }
    callback(errors.noError(), encryptionHandler.decryptDbValue(fleets[0].key))
  })
}

/*
 * Fee Calculation for parking spots and zones
 * @param {Object} searchParams of the users location
 * @param {Function} Callback with params {error, rows}
 */
const checkParkingFees = (searchParams, callback) => {
  let notAllowed = false
  let outside = false
  let fee = 0.00
  // Skip parking fee if not necessary
  if (searchParams.skip_parking_fee) {
    callback(errors.noError(), { fee: fee, outside: outside, not_allowed: notAllowed })
    return
  }
  getFleets({ fleet_id: searchParams.fleet_id }, (error, fleet) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: checking parking fee. Failed to get fleets with fleet_id:',
        searchParams.fleet_id, errors.errorWithMessage(error))
      callback(error, null)
      return
    }
    const location = {
      latitude: parseFloat(searchParams.latitude),
      longitude: parseFloat(searchParams.longitude)
    }
    parkingHandler.getParkingZonesForFleet({ fleet_id: searchParams.fleet_id }, (error, zones) => {
      if (error) {
        Sentry.captureException(error)
        logger('Error: checking parking fee. Failed to get parking zones with fleet_id:',
          searchParams.fleet_id, errors.errorWithMessage(error))
        callback(error, null)
        return
      }
      parkingHandler.getParkingSpots({ fleet_id: searchParams.fleet_id }, (error, parkingSpotsData) => {
        if (error) {
          Sentry.captureException(error)
          logger('Error: checking parking fee. Failed to get parking zones with fleet_id:',
            searchParams.fleet_id, errors.errorWithMessage(error))
          callback(error, null)
          return
        }

        // Lets check the parking area restriction
        if (!fleet[0].parking_area_restriction || fleet[0].parking_area_restriction === 0) {
          notAllowed = false
        } else if (fleet[0].parking_area_restriction > 0) {
          notAllowed = true
        }
        // Lets check if parking zones have setup that fleet
        if (zones.parking_zones.length === 0 && parkingSpotsData.length === 0) {
          fee = 0.00
          outside = false
          notAllowed = false
        } else {
          // Lets Check if user is inside any of the zones location
          const parkingZones = _checkZoneWithinBounds(zones.parking_zones, location)
          if (parkingZones.length === 0) {
            // Lets Check if user is inside any of the spots
            const parkingSpots = _checkSpotsWithinBounds(parkingSpotsData, location)
            if (parkingSpots.length > 0) {
              outside = false
              fee = 0.00
            } else {
              outside = true
              if (fleet[0].type === fleetConstants.fleet_type.publicWithPayment ||
                fleet[0].type === fleetConstants.fleet_type.privateWithPayment) {
                fee = fleet[0].price_for_penalty_outside_parking
              }
            }
          } else {
            outside = false
            fee = 0.00
          }
        }
        const parkingFees = {
          currency: paymentHandler.getFleetCurrency(fleet[0]),
          fee: fee ? parseFloat(fee.toFixed(2)) : 0.00,
          outside: outside,
          not_allowed: notAllowed && outside
        }
        callback(null, parkingFees)
      })
    })
  })
}

/** @private
 * The method checks the parking zones within Bounds
 * @param {Object} zones
 * @param {Object} location
 */
const _checkZoneWithinBounds = (zones, location) => {
  const nearByZones = []
  _.each(zones, (zone) => {
    if (zone.type === 'polygon' || zone.type === 'rectangle') {
      const zones = []
      _.each(zone.geometry, (polygon) => {
        const points = []
        points.push(polygon.longitude, polygon.latitude)
        zones.push(points)
      })
      const withInBounds = pointHandler.checkWithinBounds(zones, [location.longitude, location.latitude])
      if (withInBounds) {
        nearByZones.push(zone)
      }
    } else if (zone.type === 'circle') {
      const centerPoint = { latitude: zone.geometry[0].latitude, longitude: zone.geometry[0].longitude }
      const withInBound = _withinCircle(location, centerPoint, zone.geometry[0].radius)
      if (withInBound) {
        nearByZones.push(zone)
      }
    }
  })
  return nearByZones
}

/** @private
 * The method checks the parking spots within Bounds
 * @param {Object} parkingSpots
 * @param {Object} location
 */
const _checkSpotsWithinBounds = (parkingSpots, location) => {
  const nearBySpots = []
  _.each(parkingSpots, (spot) => {
    const lastPoint = {}
    lastPoint.latitude = spot.latitude
    lastPoint.longitude = spot.longitude
    const distance = pointHandler.distanceBetweenPoints(location, lastPoint)
    if (distance <= config.parkingSpotRange) {
      nearBySpots.push(spot)
      return true
    }
  })
  return nearBySpots
}

/**
 * The method fetched fleets with its metadata
 *
 * @param {Object} fleetDetails
 * @param {Function} callback
 */
const getFleetsWithMetadata = (fleetDetails, callback) => {
  const query = queryCreator.joinWithAnd(dbConstants.tables.fleets, dbConstants.tables.fleet_metadata,
    null, 'fleet_id', 'fleet_id', fleetDetails.fleet_id)
  sqlPool.makeQuery(query, (error, fleets) => {
    if (error) {
      Sentry.captureException(error, {query: query})
      logger('Error: failed to fetch fleetsWithMetadata with:', fleetDetails, 'with error:', error)
      callback(errors.internalServer(false), null)
      return
    }
    callback(null, fleets)
  })
}

/**
 * The method fetched fleets with its payment data and metadata
 *
 * @param {Object} fleetDetails
 * @param {Function} callback
 */
const getFleetsWithPaymentAndMetadata = (fleetDetails, callback) => {
  getFleets({ ...fleetDetails, includeMetadata: true }, (error, fleets) => {
    if (error) {
      Sentry.captureException(error)
      callback(error, null)
      return
    }

    callback(null, fleets)
  })
}

/** @private
 * The method checks the point inside the circle or not
 *
 * @param {Object} location
 * @param {Object} centerPoint
 * @param {Number} radius
 *
 */
const _withinCircle = (location, centerPoint, radius) => {
  const withInCircle = false
  const distance = pointHandler.distanceBetweenPoints(location, centerPoint)
  if (distance > radius) {
    return withInCircle
  }
  return !withInCircle
}

const getFleetsWithFleetAssociations = (filterColumn, filterValues, callback) => {
  const getFleetsWithFleetAssociationsQuery = queryCreator.joinWithAnd(dbConstants.tables.fleets,
    dbConstants.tables.fleet_associations,
    null,
    'fleet_id',
    filterColumn,
    filterValues
  )
  sqlPool.makeQuery(getFleetsWithFleetAssociationsQuery, callback)
}

/**
 * The method fetches fleets with its metadata
 *
 * @param {Object} comparisonValues
 * @param {Function} callback
 */
const getFleetMetaData = (comparisonValues, callback) => {
  const getFleetMetaDataQuery = queryCreator.selectWithAnd(dbConstants.tables.fleet_metadata, null, comparisonValues)
  sqlPool.makeQuery(getFleetMetaDataQuery, callback)
}

/**
 * The method gets or creates the token for the fleet
 *
 * @param {Object} requestData
 * @param {Function} callback
 */
const getToken = (requestData, callback) => {
  _getSdkData({ fleet_id: requestData.fleet_id }, (error, sdkData) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: failed to fetch sdk details with fleet_id:', requestData.fleet_id, 'with error:', error)
      callback(errors.internalServer(false), null)
      return
    }

    if (sdkData.length === 0) {
      _getFleets({ fleet_id: requestData.fleet_id }, (error, fleets) => {
        if (error) {
          Sentry.captureException(error)
          logger('Error: failed to fetch sdk details with fleet_id:', requestData.fleet_id, 'with error:', error)
          callback(errors.internalServer(false), null)
          return
        }

        const sdkParams = {
          fleet_id: fleets[0].fleet_id,
          api_token: _newApiToken(fleets[0].type, fleets[0].key),
          date_created: moment().unix(),
          date_updated: moment().unix(),
          active: 1
        }
        _insertSdkData(sdkParams, (error) => {
          if (error) {
            Sentry.captureException(error)
            logger('Error: failed to fetch sdk details with fleet_id:', requestData.fleet_id, 'with error:', error)
            callback(errors.internalServer(false), null)
            return
          }
          callback(null, sdkParams)
        })
      })
    } else {
      callback(null, sdkData[0])
    }
  })
}

/**
 * The method creates the hash of apiToken
 *
 * @param {String} fleetType
 * @param {String} fleetKey
 * @private
 */
const _newApiToken = (fleetType, fleetkey) => {
  return encryptionHandler.sha256Hash(fleetType + moment().unix().toString() + fleetkey)
}

/**
 * The method gets the token for the fleet
 *
 * @param {Object} comparisonValues
 * @param {Function} callback
 * @private
 */
const _getSdkData = (comparisonValues, callback) => {
  const getSdkDataQuery = queryCreator.selectWithAnd(dbConstants.tables.sdk, null, comparisonValues)
  sqlPool.makeQuery(getSdkDataQuery, callback)
}

/** * This method gets the fleet details in the fleets table
 *
 * @param {Object} comparisonColumns - Identifier to insert the datas inside the table
 * @param {Function} callback
 * @private
 */
const _getFleets = (comparisonColumns, callback) => {
  const getFleetsQuery = queryCreator.selectWithAnd(dbConstants.tables.fleets, null, comparisonColumns)
  sqlPool.makeQuery(getFleetsQuery, callback)
}

/** * This method gets the fleet in the sdk table
 *
 * @param {Object} columnsAndValues - Identifier to insert the datas inside the table
 * @param {Function} callback
 * @private
 */
const _insertSdkData = (columnsAndValues, callback) => {
  const insertSdkDataQuery = queryCreator.insertSingle(dbConstants.tables.sdk, columnsAndValues)
  sqlPool.makeQuery(insertSdkDataQuery, callback)
}

/** * This method Validates the fleet token
 *
 * @param {Object} params - Identifier to get and validate sdk data
 * @param {Function} callback
 * @private
 */
const validateFleetToken = (params, callback) => {
  _getSdkData({ api_token: params.api_token }, (error, data) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: failed to fetch sdk details with apiToken:', params.api_token, 'with error:', error)
      callback(errors.internalServer(false), null)
      return
    }
    if (data.length === 0) {
      logger('Failed to authenticate api token:', params.api_token, 'api token invalid')
      callback(errors.tokenInvalid(false))
    } else if (data[0].active === 0) {
      logger('api_Token:', params.api_token, 'is not verified')
      callback(errors.unauthorizedAccess(false))
    } else {
      _getFleets({ fleet_id: data[0].fleet_id }, (error, fleets) => {
        if (error) {
          Sentry.captureException(error)
          logger('Error: failed to fetch fleets:', data[0].fleet_id, 'with error:', error)
          callback(errors.internalServer(false), null)
          return
        }
        const fleet = fleets[0]
        fleet.fleet_key = encryptionHandler.decryptDbValue(fleet.key)
        callback(null, fleet)
      })
    }
  })
}

const getFleetGeofences = async (fleetId) => {
  const geofenceStatus = await db.main('fleet_metadata').select('fleet_id', 'geofence_enabled').where({ fleet_id: fleetId }).first()
  const geofenceEnabled = !!geofenceStatus.geofence_enabled
  if (geofenceEnabled) {
    const geofences = await db.main.select().from('geofences').where({ fleet_id: fleetId })
    return geofences
  } else return []
}

const getFleetIntegrations = async (fleetId, vendor) => {
  try {
    let whereClause = { fleet_id: fleetId }
    if (vendor) whereClause = {...whereClause, integration_type: vendor}
    const integrations = await db.main('integrations')
      .where(whereClause)
      .select('integrations_id', 'fleet_id', 'email', 'integration_type', 'metadata')
    return integrations.map(d => {
      if (d.metadata) return { ...d, metadata: JSON.parse(d.metadata) }
    })
  } catch (error) {
    Sentry.captureException(error)
    logger(`Error: failed to validate IoT key with Scout  IoT: ${error.message}`)
    throw errors.customError(
      'Failed to retrieve fleet integrations',
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

const list = async ({ user }) => {
  const accessiblePrivateFleets = await db
    .users(dbConstants.tables.private_fleet_users)
    .where({ user_id: user.user_id, access: 1, verified: 1 })
    .select('fleet_id')

  const fleetsBuilder = db
    .main('fleets')
    .whereIn('fleets.type', [
      fleetConstants.fleet_type.publicWithPayment,
      fleetConstants.fleet_type.publicWithNoPayment
    ])

  if (accessiblePrivateFleets.length) {
    fleetsBuilder.orWhereIn(
      'fleets.fleet_id',
      accessiblePrivateFleets.map(f => f.fleet_id)
    )
  }

  fleetsBuilder
    .join('addresses', function () {
      this
        .on('addresses.type', '=', db.main.raw('?', ['fleet']))
        .andOn('addresses.type_id', '=', 'fleets.fleet_id')
    })
    .join('reservation_settings', 'reservation_settings.fleet_id', 'fleets.fleet_id')
    .leftOuterJoin('fleet_payment_settings', 'fleet_payment_settings.fleet_id', 'fleets.fleet_id')
    .leftOuterJoin('pricing_options', function () {
      this.on('pricing_options.fleet_id', '=', 'fleets.fleet_id').andOnNull(
        'pricing_options.deactivated_at'
      )
    })
    .options({ nestTables: true, typeCast: dbHelpers.castDataTypes })
    .distinct()

  const fleets = await fleetsBuilder

  return Object.values(
    fleets.reduce(
      (
        fleets,
        {
          fleets: fleet,
          addresses: address,
          reservation_settings: rs,
          fleet_payment_settings: ps,
          pricing_options: pricingOption
        }
      ) => {
        const current = fleets[fleet.fleet_id]
        if (current) {
          if (pricingOption.pricing_option_id) {
            if (current.pricing_options) {
              current.pricing_options.push(pricingOption)
            } else {
              current.pricing_options = [pricingOption]
            }
          }

          if (current.fleet_payment_settings && current.fleet_payment_settings.usage_surcharge === 'No') {
            current.fleet_payment_settings.excess_usage_fees = 0.00
          }
          current.fleet_payment_settings = {...current.fleet_payment_settings, enable_preauth: !!current.enable_preauth}
          if (['private_no_payment', 'public_no_payment'].includes(current.type)) delete current.fleet_payment_settings
          fleets[current.fleet_id] = current
        } else {
          fleet.address = address
          fleet.reservation_settings = rs
          ps = { ...ps, enable_preauth: !!ps.enable_preauth }
          fleet.fleet_payment_settings = ps

          if (pricingOption.pricing_option_id) {
            fleet.pricing_options = [pricingOption]
          }

          if (['private_no_payment', 'public_no_payment'].includes(fleet.type)) delete fleet.fleet_payment_settings

          fleets[fleet.fleet_id] = fleet
        }

        return fleets
      },
      {}
    )
  )
}

module.exports = Object.assign(module.exports, {
  getFleets: getFleets,
  getFleetsWithMetadata: getFleetsWithMetadata,
  getFleetAssociations: getFleetAssociations,
  getCustomers: getCustomers,
  formatFleetForWire: formatFleetForWire,
  formatFleetsForWire: formatFleetsForWire,
  getFleetKey: getFleetKey,
  checkParkingFees: checkParkingFees,
  getFleetsWithFleetAssociations: getFleetsWithFleetAssociations,
  getFleetMetaData: getFleetMetaData,
  getFleetsWithPaymentAndMetadata: getFleetsWithPaymentAndMetadata,
  getFleetPaymentSettings: getFleetPaymentSettings,
  getToken: getToken,
  validateFleetToken: validateFleetToken,
  getFleetGeofences,
  getFleetIntegrations,
  list
})
