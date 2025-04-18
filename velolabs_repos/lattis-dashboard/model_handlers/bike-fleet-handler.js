'use strict'

const platform = require('@velo-labs/platform')
const moment = require('moment')
const async = require('async')
const _ = require('underscore')
const jsonFile = require('jsonfile')
const logger = platform.logger
const query = platform.queryCreator
const sqlPool = platform.sqlPool
const S3Handler = platform.S3Handler
const errors = platform.errors
const platformConfig = platform.config
const dbConstants = platform.dbConstants
const tripUtil = require('./../utils/trip-util')
const fleetHandler = require('./fleet-handler')
const tripHandler = require('./trip-handler')
const maintenanceHandler = require('./maintenance-handler')
const operatorHandler = require('./operator-handler')
const ticketConstants = require('./../constants/ticket-constants')
const localConfig = require('./../config')
const bikeConstants = require('./../constants/bike-constants')
const maintenanceConstants = require('./../constants/maintenance-constants')
const ticketHandler = require('./ticket-handler')
const alertHandler = require('./alerts-handler')
const passwordHandler = require('./../utils/password-handler')
const uploadConstants = require('./../constants/upload-constants')
const db = require('../db')
const { SegwayApiClient } = require('../utils/segway-api')
const { ActonAPI, TeltonikaApiClient, OmniIoTApiClient, OmniAPI, OkaiAPIClient } = require('../utils/acton-api')
const { GeotabApiClient } = require('../utils/geotab-api')
const { KuhmuteApi } = require('../utils/kuhmute-api')
const { LinkaApiClient } = require('../utils/linka')
const { TapKeyApiClient } = require('../utils/tapkey-api')
const { DucktApiClient } = require('../utils/duckt-api')
const Sentinel = require('../utils/sentinel')
const AWS = require('aws-sdk')
const { v4: uuidv4 } = require('uuid')
const uploadFile = require('./../utils/file-upload')

const { Kisi } = require('../utils/kisi')

const { getParameterStoreValuesByPath, getParameterStoreValue } = require('../utils/parameterStore')

const { updateBikeStatus, createVehicleAndController } = require('../utils/gpsService')
const {
  getLatestDataForController,
  updateDeviceConfig
} = require('../utils/nimbelink-api')
const {
  getAllUserScouts,
  getCurrentScoutStatus
} = require('../utils/scout-api')
const comoduleApi = require('../utils/comodule-api')
const encryptionHandler = platform.encryptionHandler
const { executeCommand, client } = require('../utils/grow')
const randomGenerator = require('../utils/random-code-generator')
const { responseCodes } = require('@velo-labs/platform')
const envConfig = require('../envConfig')
const { EdgeApiClient } = require('../utils/edge-api')

const Sentry = require('../utils/configureSentry')

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

const environment = process.env.CURRENT_ENVIRONMENT
const ssmClient = new AWS.SSM({
  region: 'us-west-1'
})

const actonFamily = {
  'ACTON': ActonAPI,
  'Omni IoT': OmniIoTApiClient,
  'Teltonika': TeltonikaApiClient,
  'Okai': OkaiAPIClient
}

let GeotabApi
let LinkaApi
let DucktApi

/**
 * This method gets electric bike types
 *
 * @param {Object} columnsAndValues - filter parameters
 * @param {Function} done - Callback function
 */
const getElectricBikes = (columnsAndValues, done) => {
  const electricBikesQuery = query.selectWithAnd(
    dbConstants.tables.electric_bikes,
    null,
    columnsAndValues
  )
  sqlPool.makeQuery(electricBikesQuery, (error, bikeGroups) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: Getting electric bike.')
      done(errors.internalServer(false), null)
      return
    }
    done(errors.noError(), bikeGroups)
  })
}

/**
 * This method gets the user details from integrations
 *
 * @param {Object} bike - filter parameters
 */
const setUpGeotabApiUser = async (bike) => {
  let user = {}
  const geotabApiKeys = await db
    .main('integrations')
    .where({ 'fleet_id': bike.fleet_id, 'integration_type': 'geotab' })
    .first()
  if (geotabApiKeys) {
    user.userName = geotabApiKeys.email
    user.password = encryptionHandler.decryptDbValue(geotabApiKeys.api_key)
    user.sessionId = geotabApiKeys.session_id
    user.serverName = JSON.parse(geotabApiKeys.metadata).server_name
    user.database = JSON.parse(geotabApiKeys.metadata).database
  }
  return user
}

/**
 * This method gets the user details from integrations
 *
 * @param {Object} bike - filter parameters
 * @param {string} integrationType - filter parameters
 */
const setUpApiUser = async (bike, integrationType) => {
  let user = {}
  const apiKeys = await db
    .main('integrations')
    .where('fleet_id', bike.fleet_id)
    .where('integration_type', integrationType)
    .select()
  if (apiKeys.length) {
    switch (integrationType) {
      case 'duckt':
        user.userName = apiKeys[0].email
        user.password = encryptionHandler.decryptDbValue(apiKeys[0].api_key)
        return user
      case 'ParcelHive':
        user.accessToken = JSON.parse(apiKeys[0].metadata).accessToken
        return user
      default:
        return {}
    }
  }
}

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

/**
 * This method gets electric bike types
 *
 * @param {Number} customerId - customer identifier
 * @param {Function} done - Callback function
 */
const getElectricBikeTypes = (customerId, done) => {
  getBikeGroups({ customer_id: customerId }, (error, bikeGroups) => {
    if (error) {
      Sentry.captureException(error, {customerId})
      logger('Error: Getting electric bike. Failed to get bike-groups')
      done(errors.internalServer(false), null)
      return
    }

    const groupsWithUniqueName = bikeGroups.map((group) => {
      const nameSuffix = group.iot_module_type
        ? ` (with ${group.iot_module_type} IoT)`
        : ''
      const uniqueGroupName = `${group.make} - ${group.model}${nameSuffix}`
      return {
        ...group,
        pre_defined_bike: uniqueGroupName,
        pre_defined_bike_title: uniqueGroupName
      }
    })

    const uniqueGroups = _.uniq(
      groupsWithUniqueName,
      (group) => group.pre_defined_bike
    )

    done(errors.noError(), { pre_defined_bikes: uniqueGroups })
  })
}

/**
 * This method gets bike information for a given bike id.
 *
 * @param {Number} bikeId - bike identifier
 * @param {Function} done - Callback function
 */
const getBikesWithBikeId = (bikeId, done) => {
  getBike('bike_id', bikeId, done)
}

/**
 * This method gets bike information for a given bike id.
 *
 * @param {Number} fleetId - fleet identifier
 * @param {Function} done - Callback function
 */
const getBikesForFleet = (fleetId, done) => {
  _makeBikeQuery('fleet_id', fleetId, done)
}

/**
 * This methods gets all bikes for a given customer identifier.
 *
 * @param {Number} customerId - A customer identifier
 * @param {Function} done - Callback function
 */
const getBikesForCustomer = (customerId, done) => {
  _makeBikeQuery('customer_id', customerId, done)
}

/**
 * This methods gets all bikes for a given operator identifier.
 *
 * @param {Number} operatorId - A operator identifier
 * @param {Function} done - Callback function
 */
const getBikesForOperator = (operatorId, done) => {
  _makeBikeQuery('operator_id', operatorId, done)
}

/*
 * Queries the database for bike objects for the given input parameters.
 *
 * @param {String} filterColumn - Column to filter on when querying the db
 * @param {String} || {Array} filterValues - Values to filter for the given filterColumn
 * @param done
 * @private
 */
const _makeBikeQuery = (filterColumn, filterValues, done) => {
  const bikesQuery = query.joinWithAnd(
    'locks',
    ['bikes'],
    null,
    'lock_id',
    filterColumn,
    [filterValues]
  )
  sqlPool.makeQuery(bikesQuery, (error, bikes) => {
    if (error) {
      Sentry.captureException(error, {bikesQuery})
      logger('Error: making bikes query:', bikesQuery, 'with error', error)
      done(errors.internalServer(false), null)
      return
    }

    getInactiveBikes(
      filterColumn,
      filterValues,
      async (error, inactiveBikes) => {
        if (error) {
          Sentry.captureException(error, {filterColumn, filterValues})
          done(error, null)
          return
        }
        if (inactiveBikes && inactiveBikes.length > 0) {
          bikes = bikes.concat(inactiveBikes)
        }

        // Bikes with controller
        const controllers = await db
          .main('controllers')
          .whereNotNull('bike_id')
          .select()

        const bikesWithController = await db
          .main('bikes')
          .whereIn(
            'bike_id',
            _.uniq(controllers.map((controller) => controller.bike_id))
          )
          .andWhere({
            [filterColumn]: filterValues
          })
          .select()

        // This is a hack to append bikes with controllers that were not included by
        // the legacy implementation while avoiding duplication. Ideally, we should be
        // able to make better sql to achieve this in one go but that requires refactoring
        // without breaking legacy behaviour.
        for (const bikeWithController of bikesWithController) {
          let bike = bikes.find(
            (b) => b.bike_id === bikeWithController.bike_id
          )
          if (!bike) {
            bikes.push(bikeWithController)
            bike = bikeWithController
          }
          bike.controllers = controllers.filter(
            (controller) => controller.bike_id === bike.bike_id
          )
        }

        bikes = await Promise.all(bikes.map(async bike => {
          const qrCode = await db.main('qr_codes').where({'equipment_id': bike.bike_id, type: 'bike'}).select('code').first()
          return qrCode ? {...bike, qr_code_id: qrCode.code} : bike
        }))

        if (bikes.length !== 0) {
          getElectricBikes(
            { bike_id: _.pluck(bikes, 'bike_id') },
            (error, electricBikes) => {
              if (error) {
                Sentry.captureException(error, { bike_id: _.pluck(bikes, 'bike_id') })
                done(error, null)
                return
              }
              let eBike
              for (let i = 0, bikesLen = bikes.length; i < bikesLen; i++) {
                eBike = _.findWhere(electricBikes, {
                  bike_id: bikes[i].bike_id
                })
                if (eBike) {
                  delete eBike.bike_battery_level
                  bikes[i] = Object.assign(bikes[i], eBike)
                }
              }
              getLastLocationOfBikes(_formatLocksForWire(bikes), done)
            }
          )
        } else {
          done(errors.noError(), bikes)
        }
      }
    )
  })
}

const getInactiveBikes = (filterColumn, filterValue, done) => {
  if (filterColumn === 'fleet_id') {
    const lockLessBikesQuery = query.selectWithAnd('bikes', null, {
      fleet_id: filterValue,
      status: 'inactive',
      current_status: 'lock_not_assigned'
    })
    sqlPool.makeQuery(lockLessBikesQuery, (error, lockeLessBikes) => {
      if (error) {
        Sentry.captureException(error, {lockLessBikesQuery})
        logger('Error: making lockLessBikes query:', lockLessBikesQuery)
        done(errors.internalServer(false), null)
        return
      }
      done(errors.noError(), lockeLessBikes)
    })
  } else {
    const comparisonColumnAndValues =
      filterColumn === 'customer_id'
        ? { customer_id: filterValue }
        : { operator_id: filterValue }
    fleetHandler.getFleetList(comparisonColumnAndValues, (error, fleets) => {
      if (error) {
        Sentry.captureException(error, {comparisonColumnAndValues})
        done(error, null)
        return
      }
      if (!fleets || fleets.length === 0) {
        return done(errors.noError(), null)
      } else {
        const fleetID = []
        _.each(fleets, (fleet) => {
          fleetID.push(fleet.fleet_id)
        })
        const lockLessBikesQuery = query.selectWithAnd('bikes', null, {
          fleet_id: fleetID,
          status: 'inactive',
          current_status: 'lock_not_assigned'
        })
        sqlPool.makeQuery(lockLessBikesQuery, (error, lockeLessBikes) => {
          if (error) {
            Sentry.captureException(error, {lockLessBikesQuery})
            logger('Error: making lockLessBikes query:', lockLessBikesQuery)
            done(errors.internalServer(false), null)
            return
          }
          done(errors.noError(), lockeLessBikes)
        })
      }
    })
  }
}

/*
 * To fetch the bike details.
 *
 * @param {String} filterColumn - Column to filter on when querying the db
 * @param {String} || {Array} filterValues - Values to filter for the given filterColumn
 * @param done
 * @private
 */
const getBike = (filterColumn, filterValues, done) => {
  if (!Array.isArray(filterValues)) {
    filterValues = [filterValues]
  }
  const bikesQuery = query.joinWithAnd(
    dbConstants.tables.bikes,
    [dbConstants.tables.locks],
    null,
    'lock_id',
    filterColumn,
    filterValues
  )
  sqlPool.makeQuery(bikesQuery, (error, bikes) => {
    if (error) {
      Sentry.captureException(error, {bikesQuery})
      logger('Error: making bikes query:', bikesQuery, 'with error', error)
      done(errors.internalServer(false), null)
      return
    }
    const bikesLength = bikes.length
    if (bikesLength === 0) {
      done(errors.noError(), bikes)
      return
    }
    getElectricBikes(
      { bike_id: _.pluck(bikes, 'bike_id') },
      (error, electricBikes) => {
        if (error) {
          Sentry.captureException(error, { bike_id: _.pluck(bikes, 'bike_id') })
          done(error, null)
          return
        }
        let eBike
        for (let i = 0; i < bikesLength; i++) {
          eBike = _.findWhere(electricBikes, { bike_id: bikes[i].bike_id })
          if (eBike) {
            bikes[i] = Object.assign(bikes[i], eBike)
          }
        }
        getLastLocationOfBikes(_formatLocksForWire(bikes), done)
      }
    )
  })
}

/**
 * This method gets bike data.
 *
 * @param {Object} columnsAndValues
 * @param {Function} done - Callback function
 */
const getBikes = (columnsAndValues, done) => {
  const bikesQuery = query.selectWithAnd(
    dbConstants.tables.bikes,
    null,
    columnsAndValues
  )
  sqlPool.makeQuery(bikesQuery, (error, bikes) => {
    if (error) {
      Sentry.captureException(error, {bikesQuery})
      logger('Error: making get bikes query:', bikesQuery)
      done(errors.internalServer(false), null)
      return
    }
    done(errors.noError(), bikes)
  })
}

/**
 * This method gets maintenance information for a given bike id.
 *
 * @param {Array || Number} bikeId - bike identifier
 * @param {Function} done - Callback function
 */
const _getMaintenanceRecordsForBikes = (bikeId, done) => {
  _makeMaintenanceQuery('bike_id', bikeId, done)
}

/**
 * This method gets maintenance information for a given fleet id.
 *
 * @param {Number} fleetId - Fleet identifier
 * @param {Function} done - Callback function
 */
const getMaintenanceRecordsForFleet = (fleetId, done) => {
  _makeMaintenanceQuery('fleet_id', fleetId, done)
}

/**
 * This methods gets all maintenance records for a given customer identifier.
 *
 * @param {Number} customerId - A customer identifier
 * @param {Function} done - Callback function
 */
const getMaintenanceRecordsForCustomer = (customerId, done) => {
  _makeMaintenanceQuery('customer_id', customerId, done)
}

/**
 * This methods gets all maintenance records for a given operator identifier.
 *
 * @param {Number} operatorId - A operator identifier
 * @param {Function} done - Callback function
 */
const getMaintenanceRecordsForOperator = (operatorId, done) => {
  _makeMaintenanceQuery('operator_id', operatorId, done)
}

/*
 * Queries the database for maintenance objects for the given input parameters.
 *
 * @param {String} filterColumn - Column to filter on when querying the db
 * @param {Array} || {Array} filterValues - Values to filter for the given filterColumn
 * @param done
 * @private
 */
const _makeMaintenanceQuery = (filterColumn, filterValues, done) => {
  const maintenanceQuery = query.selectWithAnd('maintenance', null, {
    [filterColumn]: filterValues
  })

  sqlPool.makeQuery(maintenanceQuery, (error, maintenanceData) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: making Maintenance query:', maintenanceQuery)
      done(errors.internalServer(false), null)
      return
    }
    done(errors.noError(), maintenanceData)
  })
}

const updateBikeInformation = async (bikeId) => {
  try {
    const ctrl = await db
      .main('controllers')
      .join('bikes', 'bikes.bike_id', 'controllers.bike_id')
      .where({ 'controllers.bike_id': bikeId }).first()
    const EdgeClient = new EdgeApiClient(ctrl.fleet_id)
    switch (ctrl.vendor) {
      case 'Segway': {
        await updateSegwayBikeStatus(segwayApiClientForUs, bikeId, ctrl)
        break
      }
      case 'Segway IoT EU': {
        await updateSegwayBikeStatus(segwayApiClientForEu, bikeId, ctrl)
        break
      }
      case 'Edge': {
        await updateEdgeBikeStatus(EdgeClient, bikeId, ctrl)
        break
      }
      default: {
        break
      }
    }
  } catch (error) {
    Sentry.captureException(error)
    logger(`Error updating bike info for bike ${bikeId}`)
  }
}

/**
 * Used to get all the bike fleet details.
 *
 * @param {Object} request - Column to get request parameters
 * @param done - Callback
 * @Private
 */
const getBikeFleetData = async (request, done) => {
  let asyncTasks
  if (_.has(request, 'bike_id')) {
    await updateBikeInformation(request.bike_id)
    asyncTasks = {
      bike_data: (next) => {
        getBikesWithBikeId(request.bike_id, next)
      },
      maintenance: (next) => {
        _getMaintenanceRecordsForBikes(request.bike_id, next)
      }
    }
  } else if (_.has(request, 'fleet_id')) {
    asyncTasks = {
      bike_data: (next) => {
        getBikesForFleet(request.fleet_id, next)
      },
      maintenance: (next) => {
        getMaintenanceRecordsForFleet(request.fleet_id, next)
      }
    }
  } else if (_.has(request, 'customer_id')) {
    asyncTasks = {
      bike_data: (next) => {
        getBikesForCustomer(request.customer_id, next)
      },
      maintenance: (next) => {
        getMaintenanceRecordsForCustomer(request.customer_id, next)
      }
    }
  } else {
    asyncTasks = {
      bike_data: (next) => {
        getBikesForOperator(request.operator_id, next)
      },
      maintenance: (next) => {
        getMaintenanceRecordsForOperator(request.operator_id, next)
      }
    }
  }
  let bikeFleetInfo = {
    bike_data: [],
    maintenance: []
  }
  async.parallel(asyncTasks, async (err, results) => {
    if (err) {
      done(err, null)
    } else {
      const bikeData = await Promise.all(results.bike_data.map(async bike => {
        if (bike.current_trip_id) {
          bike.currentTrip = await db.main('trips')
            .where({ trip_id: bike.current_trip_id })
            .select('trip_id', 'steps', 'start_address', 'end_address', 'date_created', 'date_endtrip', 'user_id', 'bike_id', 'port_id', 'hub_id', 'fleet_id')
            .first()
        }
        if (bike.current_user_id && bike.last_user_id) {
          const user = await db.users('users')
            .where({ user_id: bike.current_user_id })
            .select('user_id', 'email', 'first_name', 'last_name', 'username', 'phone_number').first()
          bike.currentUser = user
          bike.lastUser = user
        } else if (!bike.current_user_id && bike.last_user_id) {
          const user = await db.users('users')
            .where({ user_id: bike.last_user_id })
            .select('user_id', 'email', 'first_name', 'last_name', 'username', 'phone_number').first()
            .first()
          bike.currentUser = null
          bike.lastUser = user
        }
        return bike
      }))

      results.bike_data = bikeData

      for (let bike of results.bike_data) {
        const bikeId = bike.bike_id
        const qrCodeInfo = await db.main('qr_codes').where({equipment_id: +bikeId, type: 'bike'}).first()
        if (qrCodeInfo) {
          bike.qr_code_id = qrCodeInfo.code
          break
        }
      }
      bikeFleetInfo.bike_data = results.bike_data
      bikeFleetInfo.maintenance = results.maintenance
      if (bikeFleetInfo.bike_data.length !== 0) {
        getBikeGroupsForBike(bikeFleetInfo.bike_data, (error, bikesData) => {
          if (error) {
            Sentry.captureException(error)
            done(error, null)
            return
          }
          bikeFleetInfo.bike_data = bikesData
          done(null, bikeFleetInfo)
        })
      } else {
        done(null, bikeFleetInfo)
      }
    }
  })
}

/**
 * Used to get all the cumulative Trip Statistics
 *
 * @param {Object} tripDetails - Parameter to get Cumulative trip parameters
 * like fleet_id
 * @param done - Callback
 */
const cumulativeTripStatistics = (tripDetails, done) => {
  let asyncTasks
  if (_.has(tripDetails, 'fleet_id')) {
    asyncTasks = {
      cumulative_bike_fleet_data: (next) => {
        getBikesForFleet(tripDetails.fleet_id, next)
      },
      cumulative_trip_fleet_data: (next) => {
        tripHandler.getAllTrips(tripDetails, next)
      },
      cumulative_ticket_data: (next) => {
        ticketHandler.getTicket(
          {
            fleet_id: tripDetails.fleet_id,
            status: [
              ticketConstants.status.created,
              ticketConstants.status.assigned
            ]
          },
          next
        )
      }
    }
  } else if (_.has(tripDetails, 'customer_id')) {
    asyncTasks = {
      cumulative_bike_fleet_data: (next) => {
        getBikesForCustomer(tripDetails.customer_id, next)
      },
      cumulative_trip_fleet_data: (next) => {
        tripHandler.getAllTrips(tripDetails, next)
      },
      cumulative_ticket_data: (next) => {
        ticketHandler.getTicket(
          {
            customer_id: tripDetails.customer_id,
            status: [
              ticketConstants.status.created,
              ticketConstants.status.assigned
            ]
          },
          next
        )
      }
    }
  } else {
    asyncTasks = {
      cumulative_bike_fleet_data: (next) => {
        getBikesForOperator(tripDetails.operator_id, next)
      },
      cumulative_trip_fleet_data: (next) => {
        tripHandler.getAllTrips(tripDetails, next)
      },
      cumulative_ticket_data: (next) => {
        ticketHandler.getTicket(
          {
            operator_id: tripDetails.operator_id,
            status: [
              ticketConstants.status.created,
              ticketConstants.status.assigned
            ]
          },
          next
        )
      }
    }
  }
  async.parallel(asyncTasks, (err, results) => {
    if (err) {
      done(err, null)
    } else {
      const cumulativeData = {
        bike_data: {
          till_last_month: 0,
          till_now: 0
        },
        bike_out_of_service: {
          till_yesterday: 0,
          till_now: 0
        },
        trip_data: {
          till_yesterday: 0,
          till_now: 0
        },
        ticket_data: {
          from_yesterday: 0,
          till_now:
            results.cumulative_ticket_data === null
              ? 0
              : results.cumulative_ticket_data.length
        }
      }

      /* Cumulative Trip data calculations */
      const tripsLength = results.cumulative_trip_fleet_data.length
      let trip
      for (let i = 0; i < tripsLength; i++) {
        trip = tripUtil.parseTripString(results.cumulative_trip_fleet_data[i])

        if (trip.steps && !!trip.steps.length) {
          // Let's check if the trip was taken yesterday
          if (
            trip.steps[0][2] >= moment().subtract(1, 'days').unix() &&
            trip.steps[trip.steps.length - 1][2] <=
            moment().subtract(1, 'days').unix()
          ) {
            cumulativeData.trip_data.till_yesterday++
          }
          // Let's check if the trip is active now
          if (trip.end_address === null && !trip.date_endtrip && trip.steps[0][2] <= moment().unix()) {
            cumulativeData.trip_data.till_now++
          }
        }
      }

      // Let's return if the bikes are within last month
      results.cumulative_bike_fleet_data_till_last_month = _.filter(
        results.cumulative_bike_fleet_data,
        (bikeFleet) => {
          return bikeFleet.date_created < moment().subtract(1, 'month').unix()
        }
      )

      results.cumulative_bike_fleet_data_deleted = _.filter(
        results.cumulative_bike_fleet_data,
        (bikeFleet) => {
          return bikeFleet.status === 'deleted'
        }
      )

      results.cumulative_ticket_data_from_yesterday = _.filter(
        results.cumulative_ticket_data,
        (ticket) => {
          return (
            ticket.date_created >= moment().subtract(1, 'days').unix() &&
            ticket.date_created <= moment().unix()
          )
        }
      )

      _getLastMaintenanceForBikes(
        _.pluck(results.cumulative_bike_fleet_data, 'bike_id'),
        (error, bikeMaintenance) => {
          if (error) {
            Sentry.captureException(error, {bike_id: _.pluck(results.cumulative_bike_fleet_data, 'bike_id')})
            return done(error, null)
          }
          // Let's return if the bikes which are under maintenance are within end service date
          results.cumulative_bikes_out_of_service_till_yesterday = _.filter(
            bikeMaintenance,
            (bikeFleet) => {
              return (
                bikeFleet.service_end_date !== null &&
                bikeFleet.service_end_date <=
                moment().subtract(1, 'days').unix()
              )
            }
          )

          // Let's return if the bikes which are in suspended state
          results.cumulative_bikes_out_of_service_till_now = _.filter(
            results.cumulative_bike_fleet_data,
            (bikeFleet) => {
              return bikeFleet.status === bikeConstants.status.suspended
            }
          )

          cumulativeData.bike_data.till_last_month =
            (results.cumulative_bike_fleet_data_till_last_month === null
              ? 0
              : results.cumulative_bike_fleet_data_till_last_month.length) -
            (results.cumulative_bike_fleet_data_deleted === null
              ? 0
              : results.cumulative_bike_fleet_data_deleted.length)

          cumulativeData.bike_data.till_now =
            (results.cumulative_bike_fleet_data === null
              ? 0
              : results.cumulative_bike_fleet_data.length) -
            (results.cumulative_bike_fleet_data_deleted === null
              ? 0
              : results.cumulative_bike_fleet_data_deleted.length)

          cumulativeData.bike_out_of_service.till_yesterday =
            results.cumulative_bikes_out_of_service_till_yesterday === null
              ? 0
              : results.cumulative_bikes_out_of_service_till_yesterday.length

          cumulativeData.bike_out_of_service.till_now =
            results.cumulative_bikes_out_of_service_till_now === null
              ? 0
              : results.cumulative_bikes_out_of_service_till_now.length

          cumulativeData.ticket_data.from_yesterday =
            results.cumulative_ticket_data_from_yesterday === null
              ? 0
              : results.cumulative_ticket_data_from_yesterday.length

          const bikeFleetInfo = {
            cumulative_bike_fleet_data: cumulativeData.bike_data,
            cumulative_bikes_out_of_service: cumulativeData.bike_out_of_service,
            cumulative_trip_fleet_data: cumulativeData.trip_data,
            cumulative_ticket_data: cumulativeData.ticket_data
          }

          done(null, bikeFleetInfo)
        }
      )
    }
  })
}

/**
 * Used to get last maintenance record for the bikes
 *
 * @param {Array} bikesId - bikes identifiers
 * @param {Function} done - callback
 * @private
 */
const _getLastMaintenanceForBikes = (bikesId, done) => {
  if (bikesId.length === 0) {
    done(errors.noError(), null)
  } else {
    _getMaintenanceRecordsForBikes(bikesId, (error, maintenanceDetails) => {
      if (error) {
        Sentry.captureException(error, {bikesId})
        done(error, null)
        return
      }
      let lastMaintenance = []
      _.each(bikesId, (bikeId) => {
        const maintenanceForBike = _.where(maintenanceDetails, {
          bike_id: bikeId
        })
        if (maintenanceForBike.length > 0) {
          lastMaintenance.push(
            _.max(maintenanceForBike, _.property('service_start_date'))
          )
        }
      })
      done(null, lastMaintenance)
    })
  }
}

/**
 *
 * Used to get BikeGroups of bikes
 * @param {Object} bikes - bikes to be attached with bike groups
 * @param {Function} done - callback
 */
const getBikeGroupsForBike = (bikes, done) => {
  if (bikes.length !== 0) {
    const bikeGroupQuery = query.selectWithAnd(
      dbConstants.tables.bike_group,
      null,
      { bike_group_id: _.pluck(bikes, 'bike_group_id') }
    )
    sqlPool.makeQuery(bikeGroupQuery, (error, bikeGroups) => {
      if (error) {
        Sentry.captureException(error, {bikeGroupQuery})
        logger('Error: getting Bike groups query:', bikeGroupQuery, error)
        done(errors.internalServer(false), null)
        return
      }
      for (let i = 0, len = bikes.length; i < len; i++) {
        for (let j = 0, length = bikeGroups.length; j < length; j++) {
          if (bikes[i].bike_group_id === bikeGroups[j].bike_group_id) {
            bikes[i] = _.extend(
              bikes[i],
              _.omit(bikeGroups[j], 'pre_defined_bike_id')
            )
          }
        }
      }
      done(null, bikes)
    })
  } else {
    done(null, bikes)
  }
}

/**
 *
 * Used to get BikeGroups
 * @param {Object} columnAndValues - filter parameters
 * @param {Function} done - callback
 */
const getBikeGroups = (columnAndValues, done) => {
  const bikeGroupQuery = query.selectWithAnd(
    dbConstants.tables.bike_group,
    null,
    columnAndValues
  )
  sqlPool.makeQuery(bikeGroupQuery, (error, bikeGroups) => {
    if (error) {
      Sentry.captureException(error, {bikeGroupQuery})
      logger('Error: getting Bike groups query:', bikeGroupQuery, error)
      done(errors.internalServer(false), null)
      return
    }
    done(null, bikeGroups)
  })
}

/**
 * This methods validates a new bike object.
 *
 * @param {Object} bikeDetails
 * @returns {boolean}
 */
const validateNewBikes = (bikeDetails) => {
  return (
    _.has(bikeDetails, 'make') ||
    _.has(bikeDetails, 'model') ||
    _.has(bikeDetails, 'type') ||
    _.has(bikeDetails, 'description') ||
    _.has(bikeDetails, 'bikes_to_be_added') ||
    _.has(bikeDetails, 'fleet_id') ||
    _.has(bikeDetails, 'maintenance_schedule') ||
    _.has(bikeDetails, 'customer_id') ||
    _.has(bikeDetails, 'operator_id')
  )
}

/**
 *
 * Used to insert bikeData based on bikeDetails
 * @param {Object} bikeDetails - Parameter to get all the bike details like make,
 * model,type, description , etc
 * @param {Function} done - callback
 */
const addBikeData = (bikeDetails, done) => {
  const bikesInserted = []
  for (let i = 0; i < bikeDetails.bikes_to_be_added; i++) {
    const columnsAndValues = {
      status: bikeConstants.status.inactive,
      current_status: bikeConstants.current_status.lockNotAssigned,
      date_created: moment().unix(),
      fleet_id: bikeDetails.fleet_id,
      bike_name: '',
      bike_group_id: bikeDetails.bike_group_id,
      bike_uuid: uuidv4()
    }
    const insertBikesQuery = query.insertSingle(
      dbConstants.tables.bikes,
      columnsAndValues
    )
    sqlPool.makeQuery(insertBikesQuery, (error, status) => {
      if (error) {
        Sentry.captureException(error, {insertBikesQuery})
        logger('Error: making insert bikes query:', insertBikesQuery)
        done(error)
        return
      }
      bikesInserted.push(status.insertId)
      if (i === bikeDetails.bikes_to_be_added - 1) {
        if (bikeDetails.type === bikeConstants.types.electric) {
          const columns = ['bike_id', 'pre_defined_model']
          let values = []
          for (let j = 0, len = bikesInserted.length; j < len; j++) {
            values.push([bikesInserted[j], bikeDetails.pre_defined_bike])
          }
          const insertBikesQuery = query.insertMultiple(
            dbConstants.tables.electric_bikes,
            columns,
            values
          )
          sqlPool.makeQuery(insertBikesQuery, (error) => {
            if (error) {
              Sentry.captureException(error, {insertBikesQuery})
              logger('Error: making insert bikes query:', insertBikesQuery)
              done(errors.internalServer(false))
              return
            }
            return done(null)
          })
        } else {
          return done(null)
        }
      }
    })
  }
}

/**
 *
 * Used to insert details of bikes based on number of bikes to be added
 * and bikeImage
 * @param {Object} bikeDetails - Parameter to get all the bike details like make,
 * model,type, description
 * @param {Object} bikeDetailsImage - Parameter to insert the bikeImage in our database
 * @param {Function} done - callback
 */
const insertBikeDetails = (bikeDetails, bikeDetailsImage, done) => {
  if (bikeDetails.type === bikeConstants.types.electric) {
    operatorHandler.getCustomers(
      { customer_id: bikeDetails.customer_id },
      (error, customers) => {
        if (error) {
          Sentry.captureException(error, {bikeDetails})
          logger('Error: Inserting bikes. Failed to get customers')
          done(errors.internalServer(false))
          return
        }
        _addNewBikes(bikeDetails, bikeDetailsImage, done)
        /* Commenting this validation we have different electric bikes, not just hyenatek
             if (customers[0].pre_defined_bikes && JSON.parse(customers[0].pre_defined_bikes).includes(bikeDetails.pre_defined_bike)) {
             _addNewBikes(bikeDetails, bikeDetailsImage, done);
             } else {
             done(errors.unauthorizedAccess(false));
             } */
      }
    )
  } else {
    _addNewBikes(bikeDetails, bikeDetailsImage, done)
  }
}

const fixNullsInObject = (object) => {
  for (const [key, value] of Object.entries(object)) {
    if (
      typeof value === 'string' &&
      (value.trim() === '' || value.trim().toUpperCase() === 'NULL')
    ) {
      object[key] = null
    }
  }
  return object
}

/**
 *
 * Used to add new bikes records
 * @param {Object} bikeDetails - Parameter to get all the bike details like make,
 * model,type, description
 * @param {Object} bikeDetailsImage - Parameter to insert the bikeImage in our database
 * @param {Function} done - callback
 */
const _addNewBikes = (bikeDetails, bikeDetailsImage, done) => {
  const s3Handler = new S3Handler()
  let bikeGroupDetails
  if (bikeDetailsImage) {
    s3Handler.upload(
      bikeDetailsImage,
      uploadConstants.types.bike +
      '-' +
      bikeDetails.fleet_id +
      '-' +
      passwordHandler.randString(10),
      platformConfig.aws.s3.bikeImages.bucket,
      bikeDetailsImage.mimetype,
      (error, imageData) => {
        if (error) {
          Sentry.captureException(error, {bikeDetailsImage})
          logger(
            'Error: uploading bike image to s3',
            errors.errorWithMessage(error)
          )
          done(errors.internalServer(true))
          return
        }

        bikeGroupDetails = fixNullsInObject({
          make: bikeDetails.make,
          model: bikeDetails.model,
          description: bikeDetails.description,
          pic: imageData.Location,
          date_created: moment().unix(),
          type: bikeDetails.type,
          maintenance_schedule: bikeDetails.maintenance_schedule,
          customer_id: bikeDetails.customer_id,
          operator_id: bikeDetails.operator_id,
          fleet_id: bikeDetails.fleet_id,
          iot_module_type: bikeDetails.iot_module_type
        })
        _addBikeGroups({ ...bikeGroupDetails }, (error, status) => {
          if (error) {
            Sentry.captureException(error, {bikeGroupDetails})
            logger('Error: inserting bike groups', error)
            done(error)
            return
          }
          addBikeData(
            _.extend(bikeDetails, { bike_group_id: status.insertId }),
            (error) => {
              if (error) {
                Sentry.captureException(error, {bikeDetails})
                logger('Error: inserting bikes', error)
                done(error)
                return
              }
              done(null)
            }
          )
        })
      }
    )
  } else if (bikeDetails.bike_image) {
    bikeGroupDetails = fixNullsInObject({
      make: bikeDetails.make,
      model: bikeDetails.model,
      description: bikeDetails.description,
      pic: bikeDetails.bike_image,
      date_created: moment().unix(),
      type: bikeDetails.type,
      maintenance_schedule: bikeDetails.maintenance_schedule,
      customer_id: bikeDetails.customer_id,
      operator_id: bikeDetails.operator_id,
      fleet_id: bikeDetails.fleet_id,
      iot_module_type: bikeDetails.iot_module_type
    })
    _addBikeGroups({ ...bikeGroupDetails }, (error, status) => {
      if (error) {
        Sentry.captureException(error, {bikeGroupDetails})
        logger('Error: inserting bike groups', error)
        done(error)
        return
      }
      addBikeData(
        _.extend(bikeDetails, { bike_group_id: status.insertId }),
        (error) => {
          if (error) {
            Sentry.captureException(error, {bikeDetails})
            logger('Error: inserting bikes', error)
            done(error)
            return
          }
          done(null)
        }
      )
    })
  } else {
    bikeGroupDetails = fixNullsInObject({
      make: bikeDetails.make,
      model: bikeDetails.model,
      description: bikeDetails.description,
      pic: localConfig.default_bike_image_url,
      date_created: moment().unix(),
      type: bikeDetails.type,
      maintenance_schedule: bikeDetails.maintenance_schedule,
      customer_id: bikeDetails.customer_id,
      operator_id: bikeDetails.operator_id,
      fleet_id: bikeDetails.fleet_id,
      iot_module_type: bikeDetails.iot_module_type
    })

    _addBikeGroups({ ...bikeGroupDetails }, (error, status) => {
      if (error) {
        Sentry.captureException(error, {bikeGroupDetails})
        logger('Error: inserting bike groups', error)
        done(error)
        return
      }
      addBikeData(
        _.extend(bikeDetails, { bike_group_id: status.insertId }),
        (error) => {
          if (error) {
            Sentry.captureException(error, {bikeDetails})
            logger('Error: inserting bikes', error)
            done(error)
            return
          }
          done(null)
        }
      )
    })
  }
}

/**
 *
 * Used to get details of bikes based on number of bikes to be added
 * and bikeImage
 * @param {Object} bikeDetails - Parameter to get all the bike details
 * @param {Function} done - Callback
 */
const getBikeImageAndDescription = (bikeDetails, done) => {
  let filterColumn
  let filterValue
  if (_.has(bikeDetails, 'fleet_id')) {
    filterColumn = 'fleet_id'
    filterValue = [bikeDetails.fleet_id]
  } else if (_.has(bikeDetails, 'operator_id')) {
    filterColumn = 'operator_id'
    filterValue = [bikeDetails.operator_id]
  } else if (_.has(bikeDetails, 'customer_id')) {
    filterColumn = 'customer_id'
    filterValue = [bikeDetails.customer_id]
  }
  _makeBikeQuery(filterColumn, filterValue, (error, bikes) => {
    if (error) {
      Sentry.captureException(error, {bikeDetails, filterColumn, filterValue})
      logger(
        'Error: making get bike Details query: getBikeImageAndDescription',
        error
      )
      done(errors.internalServer(false), null)
      return
    }
    if (bikes.length === 0) {
      logger(
        'Error: no bikes available in the database: getBikeImageAndDescription',
        error
      )
      done(errors.noError(), null)
      return
    }
    const bikeGroupQuery = query.join(
      dbConstants.tables.bikes,
      [dbConstants.tables.bike_group],
      null,
      'bike_group_id',
      null,
      null
    )
    sqlPool.makeQuery(bikeGroupQuery, (error, bikeGroups) => {
      if (error) {
        Sentry.captureException(error, {bikeGroupQuery})
        logger(
          'Error: making get bike Group Details query: getBikeImageAndDescription',
          error
        )
        done(errors.internalServer(false), null)
        return
      }
      if (bikeGroups.length === 0) {
        logger(
          'Error: no bike Groups available in the database: getBikeImageAndDescription',
          error
        )
        done(errors.noError(), null)
        return
      }
      let bikeGroupList = []
      _.each(bikes, (bike) => {
        _.each(bikeGroups, (bikeGroup) => {
          if (bike.bike_group_id === bikeGroup.bike_group_id) {
            bikeGroupList.push(bikeGroup)
          }
        })
      })

      bikeGroupList = _.map(bikeGroupList, (bike) => {
        return _.pick(bike, 'description', 'pic', 'date_created')
      })
      bikeGroupList = _.filter(bikeGroupList, (bike) => {
        return bike.description !== null || bike.pic !== null
      })
      const uniqBikeImages = _.uniq(bikeGroupList, 'pic')
      const uniqBikeDescriptions = _.uniq(bikeGroupList, 'description')
      let bikeDescriptions = []
      let bikeImages = []
      _.each(uniqBikeDescriptions, (bike) => {
        bikeDescriptions.push({
          description: bike.description,
          date: bike.date_created
        })
      })
      _.each(uniqBikeImages, (bike) => {
        bikeImages.push({ image: bike.pic, date: bike.date_created })
      })
      bikeDescriptions = bikeDescriptions.filter((bikeDescription) => {
        return bikeDescription !== null
      })
      bikeImages = bikeImages.filter((bikeImage) => {
        return bikeImage !== null
      })
      const bikeList = {
        bike_descriptions: bikeDescriptions,
        bike_images: bikeImages
      }
      done(errors.noError(), bikeList)
    })
  })
}

/**
 * Get the list of maintenance Categories for Suspended Bikes
 *
 * @param done - Callback
 * @Private
 */
const getMaintenanceCategories = (done) => {
  jsonFile.readFile(
    localConfig.filePaths.maintenanceCategory,
    (error, maintenanceCategory) => {
      if (error) {
        Sentry.captureException(error)
        logger('Error: getting maintenance Categories')
        done(errors.internalServer(false), null)
        return
      }
      done(null, maintenanceCategory)
    }
  )
}

/**
 * Sends Suspended Bikes to Workshop on bike fleet
 *
 * @param {object} requestData to the suspended bikes
 * @param imageDetails
 * @param url
 * @param done
 * @Private
 */
const sendToService = (requestData, imageDetails, url, done) => {
  const now = moment().unix()
  tripHandler.getActiveBooking(
    { bike_id: requestData.bike_id },
    (error, booking) => {
      if (error) {
        Sentry.captureException(error, {requestData, imageDetails, url})
        logger(
          'Error: get Bikes using bike_id:' + requestData.bike_id,
          'with error:',
          errors.errorWithMessage(error)
        )
        done(errors.internalServer(false), null)
        return
      }

      if (booking) {
        logger('the bike_id' + requestData.bike_id + 'is already active')
        done(errors.resourceNotFound(false), null)
        return
      }
      let maintenanceColumnsAndValues
      if (requestData.type === 'dashboard_end_ride') {
        maintenanceColumnsAndValues = {
          service_start_date: now,
          date_created: now,
          status: maintenanceConstants.status.serviceDue,
          category: requestData.category,
          lock_id: requestData.lock_id,
          bike_id: requestData.bike_id,
          fleet_id: requestData.fleet_id,
          operator_id: requestData.operator_id,
          reported_by_operator_id: requestData.operator_id
        }
      } else {
        maintenanceColumnsAndValues = {
          service_start_date: now,
          date_created: now,
          status: maintenanceConstants.status.inWorkshop,
          category: requestData.category,
          lock_id: requestData.lock_id,
          bike_id: requestData.bike_id,
          fleet_id: requestData.fleet_id,
          operator_id: requestData.operator_id,
          reported_by_operator_id: requestData.operator_id
        }
      }

      const bikesColumnsAndTargetColumns = {
        status: bikeConstants.status.suspended,
        current_status: bikeConstants.current_status.underMaintenance,
        maintenance_status: bikeConstants.maintenance_status.shopMaintenance
      }

      const ticketColumnsAndValues = {
        category: requestData.category,
        operator_notes: requestData.notes,
        type: ticketConstants.types.maintenance,
        fleet_id: requestData.fleet_id,
        operator_id: requestData.operator_id,
        date_created: now,
        status: ticketConstants.status.created,
        customer_id: requestData.customer_id,
        bike_id: requestData.bike_data
      }

      _imageUpload(
        imageDetails,
        uploadConstants.types.damage_report +
        '-' +
        requestData.bike_id +
        '-' +
        passwordHandler.randString(10),
        (error, imageData) => {
          if (error) {
            Sentry.captureException(error, {imageDetails, url})
            done(errors.internalServer(true), null)
            return
          }
          let operatorPhoto = imageData ? imageData.Location : null
          insertMaintenance(
            _.extend(maintenanceColumnsAndValues, {
              operator_photo: operatorPhoto
            }),
            (error, status) => {
              if (error) {
                logger('Error: making insert maintenance query:', error)
                done(error, null)
                return
              }
              ticketHandler.insertTicket(
                _.extend(ticketColumnsAndValues, { type_id: status.insertId }),
                (error) => {
                  if (error) {
                    Sentry.captureException(error, {imageDetails, url, ticketColumnsAndValues})
                    logger('Error: making insert ticket query:', error)
                    done(error, null)
                    return
                  }
                  updateBike(
                    bikesColumnsAndTargetColumns,
                    { bike_id: requestData.bike_id },
                    (error) => {
                      if (error) {
                        Sentry.captureException(error, {imageDetails, url, bikesColumnsAndTargetColumns})
                        logger('Error: making update bikes query:', error)
                        done(error, null)
                        return
                      }
                      alertHandler.sendEmailNotifications(
                        requestData,
                        requestData.category,
                        (error) => {
                          if (error) {
                            Sentry.captureException(error, { requestData })
                            logger('Error: making update bikes query:', error)
                            done(error, null)
                            return
                          }
                          done(null, null)
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
 * Used to update the status of the bike to deleted.
 *
 * @param {Object} requestData - Request parameters includes bike_id of the bike to be deleted.
 * @param done - Callback
 */
const sendToArchive = (requestData, done) => {
  const columnsAndValues = {
    status: bikeConstants.status.deleted,
    current_status: requestData.status
  }

  tripHandler.getActiveBooking(
    { bike_id: requestData.bikes },
    (error, tripBooking) => {
      if (error) {
        Sentry.captureException(error, {requestData})
        logger(
          'Error: getting Active booking from trip',
          errors.errorWithMessage(error)
        )
        done(errors.internalServer(false))
        return
      }

      if (tripBooking) {
        logger(
          'There is a trip active for the following bikes',
          requestData.bikes
        )
        done(errors.unauthorizedAccess(false))
        return
      }

      ticketHandler.getTicket(
        {
          bike_id: requestData.bikes,
          status: [
            ticketConstants.status.created,
            ticketConstants.status.assigned
          ]
        },
        (error, tickets) => {
          if (error) {
            Sentry.captureException(error, {requestData})
            logger(
              'Error: Moving bike to live. Failed to get maintenance details.',
              errors.errorWithMessage(error)
            )
            done(errors.internalServer(false))
            return
          }
          if (tickets.length > 0) {
            ticketHandler.resolveTicket(
              { ticket_id: _.pluck(tickets, 'ticket_id') },
              (error) => {
                if (error) {
                  Sentry.captureException(error, {tickets, requestData})
                  logger(
                    'Error: Moving bike to live. Failed to resolve ticket.',
                    errors.errorWithMessage(error)
                  )
                  done(errors.internalServer(false))
                  return
                }
                updateBikes(
                  columnsAndValues,
                  { bike_id: requestData.bikes },
                  (error) => {
                    if (error) {
                      Sentry.captureException(error, {requestData})
                      logger('Error: making update bikes query:')
                      done(errors.internalServer(false))
                      return
                    }
                    done(null)
                  }
                )
              }
            )
          } else {
            updateBikes(
              columnsAndValues,
              { bike_id: requestData.bikes },
              (error) => {
                if (error) {
                  Sentry.captureException(error)
                  logger('Error: making update bikes query:')
                  done(errors.internalServer(false))
                  return
                }
                done(null)
              }
            )
          }
        }
      )
    }
  )
}

/**
 * Query to update the status of the bike to be moved to active status.
 *
 * @param {Object} columnsAndValues - to be updated
 * @param {Object} targetColumnsAndValues - to be compared in the Query
 * @param {Function} done - Callback
 */
const updateBikes = async (columnsAndValues, targetColumnsAndValues, done) => {
  const activeBikeFleetQuery = query.updateSingle(
    dbConstants.tables.bikes,
    columnsAndValues,
    targetColumnsAndValues
  )
  sqlPool.makeQuery(activeBikeFleetQuery, done)
  await updateBikeStatus(targetColumnsAndValues, columnsAndValues)
}

/**
 * Used to update the status of the bike to be moved to active status.
 *
 * @param {Array} bikes - Bikes to be moved to active status.
 * @param done - Callback
 */
const returnToActiveFleet = ({ bikes, current_status: currentStatus = null }, done) => {
  if (!Array.isArray(bikes)) {
    bikes = [bikes]
  }
  const columnsAndValues = {
    status: bikeConstants.status.active,
    current_status: currentStatus || bikeConstants.current_status.parked
  }
  getBikes({ bike_id: bikes }, (error, dbBikes) => {
    if (error) {
      Sentry.captureException(error, {bikes})
      logger(
        'Error: Failed to return bike to live. Unable to get bikes:',
        bikes
      )
      done(error)
      return
    }
    const serviceBikes = _.pluck(
      _.filter(dbBikes, (dbBike) => {
        return (
          (dbBike.status === bikeConstants.status.suspended ||
            dbBike.status === bikeConstants.status.deleted) &&
          (dbBike.current_status ===
            bikeConstants.current_status.underMaintenance ||
            dbBike.current_status === bikeConstants.current_status.damaged)
        )
      }),
      'bike_id'
    )
    const stolenBikes = _.pluck(
      _.filter(dbBikes, (dbBike) => {
        return (
          (dbBike.status === bikeConstants.status.suspended ||
            dbBike.status === bikeConstants.status.deleted) &&
          (dbBike.current_status === bikeConstants.current_status.stolen ||
            dbBike.current_status ===
            bikeConstants.current_status.reportedStolen)
        )
      }),
      'bike_id'
    )
    if (serviceBikes.length !== 0) {
      moveServiceBikesToLive(serviceBikes, done)
    } else if (stolenBikes.length !== 0) {
      moveStolenBikesToLive(stolenBikes, done)
    } else if (bikes.length !== stolenBikes.length + serviceBikes.length) {
      const bikesOnTrip = _.pluck(
        _.filter(dbBikes, {
          current_status: bikeConstants.current_status.onTrip
        }),
        'bike_id'
      )
      const maintenanceBikes = stolenBikes.concat(serviceBikes)
      const bikesToLive = _.difference(
        _.difference(_.pluck(dbBikes, 'bike_id'), bikesOnTrip),
        maintenanceBikes
      )
      if (bikesToLive.length !== 0) {
        updateBikes(columnsAndValues, { bike_id: bikesToLive }, (error) => {
          if (error) {
            Sentry.captureException(error, {bikesToLive})
            logger('Error: making update bikes query:')
            return done(errors.internalServer(false))
          }
          done(null)
        })
      } else {
        done(null)
      }
    }
  })
}

/**
 * Used to move maintenance bikes to live
 *
 * @param {Object} serviceBikes - bikes to be sent to live
 * @param done - Callback
 */
const moveServiceBikesToLive = (serviceBikes, done) => {
  const columnsAndValues = {
    status: bikeConstants.status.active,
    current_status: bikeConstants.current_status.parked
  }
  maintenanceHandler.getMaintenance(
    { bike_id: serviceBikes },
    (error, maintenance) => {
      if (error) {
        Sentry.captureException(error, {serviceBikes})
        logger(
          'Error: Failed to move service bikes to live. Unable to get maintenance for bikes:',
          serviceBikes
        )
        done(error)
        return
      }
      maintenance =
        maintenance.length !== 0
          ? _.filter(maintenance, { service_end_date: null })
          : maintenance
      if (maintenance.length > 0) {
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
              Sentry.captureException(error, {serviceBikes})
              logger(
                'Error: Moving bike to live. Failed to get maintenance details.',
                errors.errorWithMessage(error)
              )
              done(errors.internalServer(false))
              return
            }
            updateBikes(
              columnsAndValues,
              { bike_id: serviceBikes },
              (error) => {
                if (error) {
                  Sentry.captureException(error, {serviceBikes})
                  logger(
                    'Error: Moving bike to live. Failed to make update bikes query:'
                  )
                  done(errors.internalServer(false))
                  return
                }
                done(null)
              }
            )
          }
        )
      } else {
        updateBikes(columnsAndValues, { bike_id: serviceBikes }, (error) => {
          if (error) {
            Sentry.captureException(error, {serviceBikes})
            logger(
              'Error: Moving bike to live. Failed to make update bikes query:'
            )
            done(errors.internalServer(false))
            return
          }
          done(null)
        })
      }
    }
  )
}

/**
 * Used to move stolen bikes to live
 *
 * @param {Object} stolenBikes - bikes to be sent to live
 * @param done - Callback
 */
const moveStolenBikesToLive = (stolenBikes, done) => {
  const columnsAndValues = {
    status: bikeConstants.status.active,
    current_status: bikeConstants.current_status.parked
  }
  ticketHandler.getTicket(
    {
      bike_id: stolenBikes,
      status: [ticketConstants.status.created, ticketConstants.status.assigned]
    },
    (error, tickets) => {
      if (error) {
        Sentry.captureException(error, {stolenBikes})
        logger(
          'Error: Moving bike to live. Failed to get maintenance details.',
          errors.errorWithMessage(error)
        )
        done(errors.internalServer(false))
        return
      }
      if (tickets.length > 0) {
        ticketHandler.resolveTicket(
          { ticket_id: _.pluck(tickets, 'ticket_id') },
          (error) => {
            if (error) {
              Sentry.captureException(error, {stolenBikes})
              logger(
                'Error: Moving bike to live. Failed to resolve ticket.',
                errors.errorWithMessage(error)
              )
              done(errors.internalServer(false))
              return
            }
            updateBikes(columnsAndValues, { bike_id: stolenBikes }, (error) => {
              if (error) {
                logger('Error: making update bikes query:')
                done(errors.internalServer(false))
                return
              }
              done(null)
            })
          }
        )
      } else {
        updateBikes(columnsAndValues, { bike_id: stolenBikes }, (error) => {
          if (error) {
            Sentry.captureException(error, {stolenBikes})
            logger('Error: making update bikes query:')
            done(errors.internalServer(false))
            return
          }
          done(null)
        })
      }
    }
  )
}

/**
 * Used to add bike groups in the bike grouping table.
 *
 * @param {Object} columnsAndValues - columnsAndValues for maintenance.
 * @param done - Callback
 */
let _addBikeGroups = (columnsAndValues, done) => {
  const insertBikeGroupQuery = query.insertSingle(
    dbConstants.tables.bike_group,
    columnsAndValues
  )
  sqlPool.makeQuery(insertBikeGroupQuery, done)
}

/**
 * Used to insert maintenance data in the maintenance table.
 *
 * @param {Object} columnsAndValues - columnsAndValues for maintenance.
 * @param done - Callback
 */
let insertMaintenance = (columnsAndValues, done) => {
  const insertMaintenanceQuery = query.insertSingle(
    dbConstants.tables.maintenance,
    columnsAndValues
  )
  sqlPool.makeQuery(insertMaintenanceQuery, done)
}

/**
 * Used to updates maintenance data in the maintenance table.
 *
 * @param {Object} columnsAndValues - columnsAndValues for maintenance.
 * @param {Object} comparisonColumns - comparisonColumns for maintenance.
 * @param done - Callback
 */
let updateMaintenance = (columnsAndValues, comparisonColumns, done) => {
  const updateMaintenanceQuery = query.updateSingle(
    dbConstants.tables.maintenance,
    columnsAndValues,
    comparisonColumns
  )
  sqlPool.makeQuery(updateMaintenanceQuery, done)
}
/**
 * Used to update the status of the bike to be moved to inactive status.
 *
 * @param {Array} requestParam - Contains Bikes to be moved to active status.
 * @param done - Callback
 */
const sendToStaging = (requestParam, done) => {
  const columnsAndValues = {
    status: requestParam.status || bikeConstants.status.inactive,
    current_status: requestParam.current_status || bikeConstants.current_status.lockAssigned
  }
  _.each(requestParam.bikes, (bikeId, index) => {
    updateBike(columnsAndValues, { bike_id: bikeId }, (error) => {
      if (error) {
        Sentry.captureException(error, {bikeId})
        logger('Error: making update bikes query:', error)
        done(errors.internalServer(false), null)
        return
      }
      if (index === requestParam.bikes.length - 1) {
        return done(errors.noError(), null)
      }
    })
  })
}

/**
 * Used to update the status of the bike
 *
 * @param {Object} columnsAndValues - Columns and values to update the bike Status.
 * @param {Object} comparisonColumn - comparisonColumn Bikes to compare the bikes.
 * @param done - Callback
 */
const updateBike = async (columnsAndValues, comparisonColumn, done) => {
  const updateBikeQuery = query.updateSingle(
    dbConstants.tables.bikes,
    columnsAndValues,
    comparisonColumn
  )
  sqlPool.makeQuery(updateBikeQuery, done)
  await updateBikeStatus(comparisonColumn, columnsAndValues)
}

/**
 * Used to update the bikeGroup table
 *
 * @param {Object} columnsAndValues - Columns and values to update the bike Status.
 * @param {Object} comparisonColumn - comparisonColumn Bikes to compare the bikes.
 * @param done - Callback
 */
const updateBikeGroup = (columnsAndValues, comparisonColumn, done) => {
  const updateBikeGroupQuery = query.updateSingle(
    dbConstants.tables.bike_group,
    columnsAndValues,
    comparisonColumn
  )
  sqlPool.makeQuery(updateBikeGroupQuery, done)
}

/**
 * Used to update the bikeGroup table
 *
 * @param {Object} bikeGroupDetails  - contains bike_group_id and maintenance_schedule to update the bikes
 * @param done - Callback
 */
const updateMaintenanceSchedule = (bikeGroupDetails, done) => {
  _.each(bikeGroupDetails.bikes, (bikeId, index) => {
    let comparisonColumnAndValue = {
      fleet_id: bikeGroupDetails.fleet_id,
      bike_group_id: bikeId
    }
    if (bikeGroupDetails.operator_id) {
      comparisonColumnAndValue.operator_id = bikeGroupDetails.operator_id
    }
    if (bikeGroupDetails.customer_id) {
      comparisonColumnAndValue.customer_id = bikeGroupDetails.customer_id
    }
    const columnsAndValues = {
      maintenance_schedule: bikeGroupDetails.maintenance_schedule[index]
    }
    updateBikeGroup(columnsAndValues, comparisonColumnAndValue, (error) => {
      if (error) {
        Sentry.captureException(error, {bikeId})
        logger(
          'Error: Updating MaintenanceSchedule for bike: updateMaintenanceSchedule',
          error
        )
        done(errors.internalServer(false), null)
        return
      }
      if (index === bikeGroupDetails.bikes.length - 1) {
        return done(errors.noError(), null)
      }
    })
  })
}

/**
 * Used to get the data for a group of bikes
 * @param {Object} bikeGroupDetails  - contains operator_id / customer_id
 * @param done - Callback
 */
const getBikeGroupData = (bikeGroupDetails, done) => {
  const bikeGroupQuery = query.selectWithAnd(
    dbConstants.tables.bike_group,
    null,
    { fleet_id: bikeGroupDetails.fleet_id }
  )
  sqlPool.makeQuery(bikeGroupQuery, (error, bikeGroupData) => {
    if (error) {
      Sentry.captureException(error, {bikeGroupDetails, bikeGroupQuery})
      logger('Error: Fetching BikeGroup Data: getBikeGroupData', error)
      done(errors.internalServer(false), null)
      return
    }
    done(errors.noError(), bikeGroupData)
  })
}

/**
 * Used to get the data for the activity log
 * @param {Object} bikeId  - bike identifier
 * @param done - Callback
 */
const activityLog = (bikeId, done) => {
  let activityLogQuery = query.customQuery(
    'lattis_main',
    'CALL proc_bike_feed(' + bikeId + ')'
  )
  sqlPool.makeQuery(activityLogQuery, (error, activityLogData) => {
    if (error) {
      logger('Error: Fetching activityLogData: activityLogData', error)
      done(errors.internalServer(false), null)
      return
    }
    done(errors.noError(), activityLogData)
  })
}

const _imageUpload = (imageDetails, imageName, done) => {
  const s3Handler = new S3Handler()
  if (imageDetails) {
    s3Handler.upload(
      imageDetails,
      imageName,
      platformConfig.aws.s3.damageReports.bucket,
      imageDetails.mimetype,
      (error, imageData) => {
        if (error) {
          Sentry.captureException(error, {imageDetails, imageName})
          logger(
            'Error: uploading bike image to s3',
            errors.errorWithMessage(error)
          )
          done(errors.internalServer(true), null)
          return
        }
        done(null, imageData)
      }
    )
  } else {
    done(null, null)
  }
}

/* This method formats all locks
 * @param {Object} lockWithBike
 * @private
 */
const _formatLocksForWire = (lockData) => {
  if (!Array.isArray(lockData)) {
    lockData = [lockData]
  }
  const lockDataLength = lockData.length
  for (let i = 0; i < lockDataLength; i++) {
    lockData[i] = _formatLockForWire(lockData[i])
  }
  return lockData
}

/**
 * This method formats all locks
 *
 * @param {Object} lock
 * @private
 */
const _formatLockForWire = (lock) => {
  return _.omit(lock, 'key', 'mac_id')
}

/**
 * To get last parked location if current location is invalid
 *
 * @param {Array} bikes
 * @param {Function} done - callback
 */
const getLastLocationOfBikes = (bikes, done) => {
  const bikesLength = bikes.length
  const bikesNeedLocation = _.filter(bikes, (bike) => {
    return (
      bike.latitude < -90 ||
      bike.latitude > 90 ||
      bike.longitude < -180 ||
      bike.longitude > 180
    )
  })
  _getLastLocation(bikesNeedLocation, (error, bikesWithLocation) => {
    if (error) {
      Sentry.captureException(error, {bikes})
      return done(error, null)
    }
    let newBike
    for (let j = 0; j < bikesLength; j++) {
      newBike = _.findWhere(bikesWithLocation, { bike_id: bikes[j].bike_id })
      if (newBike) {
        bikes[j] = Object.assign(
          bikes[j],
          _.pick(newBike, 'latitude', 'longitude')
        )
      }
    }
    done(errors.noError(), bikes)
  })
}

/**
 * To get last parked location for the bikes from the last trip
 *
 * @param {Array} bikes
 * @param {Function} done - callback
 * @private
 */
const _getLastLocation = (bikes, done) => {
  const bikesLength = bikes.length
  if (bikesLength === 0) {
    return done(errors.noError(), bikes)
  }
  tripHandler.getAllTrips(
    { bike_id: _.uniq(_.pluck(bikes, 'bike_id')) },
    (error, data) => {
      let { trips } = data
      if (error) {
        Sentry.captureException(error, {bikes})
        return done(errors.internalServer(false), null)
      }
      let bikeTrips
      let lastTrip
      for (let i = 0; i < bikesLength; i++) {
        bikeTrips = trips.filter((trip) => trip.bike_id === bikes[i].bike_id)
        if (bikeTrips.length) {
          lastTrip = _.max(bikeTrips, 'trip_id')
          if (lastTrip) {
            lastTrip = tripUtil.parseTripString(lastTrip)
            if (lastTrip.steps && lastTrip.steps.length) {
              bikes[i] = Object.assign(bikes[i], {
                latitude: lastTrip.steps[lastTrip.steps.length - 1][0],
                longitude: lastTrip.steps[lastTrip.steps.length - 1][1]
              })
            }
          }
        }
      }
      done(errors.noError(), bikes)
    }
  )
}

const updateIotCase = async (bike, params) => {
  const controller = await db
    .main('controllers')
    .join('bikes', 'bikes.bike_id', 'controllers.bike_id')
    .where({ 'controllers.key': params.iot_key })
    .first()
  // Cannot assign controller if already assigned to other vehicle
  if (controller && bike.bike_id !== controller.bike_id) {
    throw errors.customError(
      `The controller is already assigned to vehicle ID ${controller.bike_id}:${controller.bike_name}`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
  const moduleType = params.iot_module_type // If there was no controller then use the param supplied(adding a ctrl)
  const bikeGroupId = bike.bike_group_id
  if (moduleType) {
    // TODO: Is this necessary. How do we handle it with multiple modules on one bike
    updateBikeGroup(
      { iot_module_type: moduleType },
      { bike_group_id: bikeGroupId },
      (error) => {
        if (error) {
          Sentry.captureException(error, {bike, params})
          logger('Error: updating bike group for bike', error)
          throw errors.customError(
            `Error: updating bike group for bike`,
            platform.responseCodes.BadRequest,
            'BadRequest',
            false
          )
        }
      }
    )
  } else {
    throw new Error('Module/IoT type is required')
  }

  switch (moduleType) {
    case 'Segway':
      return updateSegwayIot(segwayApiClientForUs, bike, params)

    case 'Segway IoT EU':
      return updateSegwayIot(segwayApiClientForEu, bike, params)

    case 'COMODULE Ninebot ES4':
      return updateComoduleSegwayEs4Iot(bike, params)

    case 'ACTON':
    case 'Omni IoT':
    case 'Teltonika':
    case 'Okai':
      return updateActonFamilyIoT(bike, params, moduleType)

    case 'Nimbelink':
      return updateNimbelinkTracker(bike, params)

    case 'ScoutIOT':
      return updateScoutIoT(bike, params)
    case 'Geotab IoT':
      return updateGeotabIoT(bike, params)
    case 'Manual Lock':
      return updateManualLock(bike, params)

    case 'Grow':
      return updateGrowIoT(bike, params)
    case 'Linka IoT':
      return updateLinkaIoT(bike, params)
    case 'Tap Key':
      return updateTapKeyIoT(bike, params)
    case 'Duckt':
      return updateDucktIoT(bike, params)
    case 'Kisi':
      return updateKisiIoT(bike, params)
    case 'Sas':
      return updateSasIot(bike, params)
    case 'Sentinel':
      return updateSentinelIoT(bike, params)
    case 'dck-mob-e':
      if (params.operation === 'adding') {
        await db.main('controllers').insert({
          key: params.iot_key,
          vendor: params.iot_module_type,
          bike_id: bike.bike_id,
          fleet_id: bike.fleet_id,
          device_type: 'adapter',
          equipment_type: 'bike'
        })
      }
      return {message: 'Device key added successfully'}
    case 'Edge':
      return updateEdgeIoT(bike, params)
    case null:
      logger(`Error: ${bike.make} ${bike.model} doesn't support IoT`)
      throw errors.customError(
        `${bike.make} ${bike.model} bikes do not support IoT`,
        platform.responseCodes.BadRequest,
        'BadRequest',
        false
      )

    default:
      logger(`Error: ${moduleType} IoT is not supported`)
      throw errors.customError(
        `${moduleType} IoT is not supported`,
        platform.responseCodes.BadRequest,
        'BadRequest',
        false
      )
  }
}

const integrationTypes = {
  'Segway': 'segway',
  'Segway IoT EU': 'segway',
  'Grow': 'grow',
  'Geotab IoT': 'geotab',
  'ACTON': 'acton',
  'Omni Lock': 'Omni Lock',
  'Omni IoT': 'Omni IoT',
  'Teltonika': 'Teltonika',
  'Okai': 'Okai',
  'Sentinel': 'Sentinel',
  'Edge': 'Edge'
}

const updateIot = async (bikeId, params, skipLiveCheck) => {
  if (params.iot_key) params.iot_key = params.iot_key.trim()
  const bike = await db
    .main('bikes')
    .join('bike_group', 'bikes.bike_group_id', 'bike_group.bike_group_id')
    .where({ bike_id: bikeId })
    .first()

  if (!bike) {
    throw errors.customError(
      `Bike ${bikeId} not found`,
      platform.responseCodes.ResourceNotFound,
      'ResourceNotFound',
      false
    )
  }

  if (params.bike_name) {
    await db.main.transaction(async (trx) => {
      await trx('bikes')
        .where({ bike_id: bike.bike_id })
        .update({
          bike_name: params.bike_name || bike.bike_name
        })
    })
  }
  if (bike.status === 'active' && !skipLiveCheck) {
    throw errors.customError(
      'Cannot update vehicle while it is live, move to staging and then try again.',
      platform.responseCodes.Forbidden,
      'Forbidden',
      false
    )
  }
  const ctrl = await updateIotCase(bike, params)

  try {
    // some bikes have more than 1  module
    if (ctrl && ['Segway', 'Segway IoT EU', 'Grow', 'Geotab IoT', 'ACTON', 'Omni IoT', 'Teltonika', 'Okai', 'Sentinel', 'Edge'].includes(ctrl.vendor)) {
      // Make a request to create the vehicle in GPS service
      const result = await db.main('bikes').where({ bike_id: ctrl.bike_id }).leftJoin('fleet_metadata', 'fleet_metadata.fleet_id', 'bikes.fleet_id').first()
      const integration = await db.main('integrations').where({
        fleet_id: ctrl.fleet_id, integration_type: integrationTypes[ctrl.vendor]
      }).first()
      let meta = { integrationType: ctrl.vendor }
      if (integration) {
        let info = {}
        const integrationMeta = integration.metadata && JSON.parse(integration.metadata)
        if (integrationMeta) info = integrationMeta
        if (integration.email) info.userName = integration.email
        if (integration.api_key) info.apiKey = integration.api_key
        if (integration.session_id) info.sessionId = integration.session_id
        meta = {
          ...meta,
          info
        }
        meta = _.pick(meta, _.identity)
      }

      const info = {
        bikeAndCtrl: {
          bike_id: ctrl.bike_id,
          bike_name: result.bike_name,
          status: result.current_status,
          mainStatus: result.status,
          controller_id: ctrl.controller_id,
          fleet_id: ctrl.fleet_id,
          controller_key: ctrl.key,
          device_type: ctrl.device_type,
          vendor: ctrl.vendor,
          make: ctrl.make,
          battery: result.bike_battery_level,
          batteryIot: ctrl.battery_level,
          position: {
            latitude: result.latitude,
            longitude: result.longitude
          }
        },
        fleet: {
          fleet_id: result.fleet_id,
          geofenseEnabled: !!result.geofence_enabled,
          doNotTrackTrips: !!result.do_not_track_trip,
          meta
        }
      }
      await createVehicleAndController(info)
    }
  } catch (error) {
    Sentry.captureException(error)
    logger(`Error: Creating vehicle in GPS service ${bikeId}:${ctrl.key}`)
  }
  return ctrl
}

// Convenient function to create/update vehicles in the GPS service
const updateBikesInGPSService = async (fleetId) => {
  try {
    const bikesInFleets = await db.main('bikes').where({ 'bikes.fleet_id': fleetId })
      .rightJoin('controllers', 'controllers.bike_id', 'bikes.bike_id')
      .select('bikes.bike_id', 'bikes.status', 'current_status', 'bike_name', 'device_type', 'key', 'bikes.latitude', 'bikes.longitude', 'controller_id', 'vendor', 'make', 'bike_battery_level', 'battery_level', 'geofence_enabled', 'bikes.fleet_id', 'do_not_track_trip')
      .leftJoin('fleet_metadata', 'fleet_metadata.fleet_id', 'bikes.fleet_id')

    const createVehiclesPromises = []
    for (let bikesInFleet of bikesInFleets) {
      const integration = await db.main('integrations').where({
        fleet_id: fleetId, integration_type: integrationTypes[bikesInFleet.vendor]
      }).first()
      let meta = { geofenceEnabled: !!bikesInFleet.geofence_enabled }
      if (integration) {
        let info = {}
        const integrationMeta = integration.metadata && JSON.parse(integration.metadata)
        if (integrationMeta) info = integrationMeta
        if (integration.email) info.userName = integration.email
        meta = {
          ...meta,
          fleetId: integration.fleet_id,
          apiKey: integration.api_key,
          sessionId: integration.session_id,
          info
        }
        meta = _.pick(meta, _.identity)
      }

      let geotabInfo = {}
      if (bikesInFleet.vendor === 'Geotab IoT') {
        const integrations = await db.main('integrations').where({ fleet_id: bikesInFleet.fleet_id, integration_type: 'geotab' }).first()

        geotabInfo.apiKey = integrations.api_key
        const info = JSON.parse(integrations.metadata)
        geotabInfo.info = info
      }
      const info = {
        bikeAndCtrl: {
          bike_id: bikesInFleet.bike_id,
          bike_name: bikesInFleet.bike_name,
          status: bikesInFleet.current_status,
          mainStatus: bikesInFleet.status,
          controller_id: bikesInFleet.controller_id,
          fleet_id: bikesInFleet.fleet_id,
          controller_key: bikesInFleet.key,
          device_type: bikesInFleet.device_type,
          vendor: bikesInFleet.vendor,
          make: bikesInFleet.make,
          battery: bikesInFleet.bike_battery_level,
          batteryIot: bikesInFleet.battery_level,
          position: {
            latitude: bikesInFleet.latitude,
            longitude: bikesInFleet.longitude
          }
        },
        fleet: {
          fleet_id: bikesInFleet.fleet_id,
          geofenseEnabled: !!bikesInFleet.geofence_enabled,
          doNotTrackTrips: !!bikesInFleet.do_not_track_trip,
          meta
        }
      }
      createVehiclesPromises.push(createVehicleAndController(info))
    }
    await Promise.all(createVehiclesPromises)
  } catch (error) {
    Sentry.captureException(error)
  }
}

const saveQRCode = async (equipmentId, qrCode, equipmentType) => {
  try {
    const data = {
      code: qrCode,
      type: equipmentType,
      equipment_id: +equipmentId
    }

    const isQRCodeSet = await db.main('qr_codes').where({ code: qrCode }).first()

    // Upsert operation
    if (isQRCodeSet && isQRCodeSet.equipment_id !== +equipmentId) {
      throw new Error('The QR code already exists')
    } else if (!isQRCodeSet) {
      const isDeviceInQRCodeTable = await db.main('qr_codes').where({ equipment_id: +equipmentId }).first()
      if (isDeviceInQRCodeTable) await db.main('qr_codes').update(data).where({ type: equipmentType, equipment_id: +equipmentId })
      else await db.main('qr_codes').insert(data)
    }
    const savedData = await db.main('qr_codes').where({ equipment_id: +equipmentId }).first()
    return savedData
  } catch (error) {
    throw errors.customError(error.message || 'An error occurred saving the QR code', responseCodes.InternalServer, 'ErrorSavingQRCode', true)
  }
}

const updateBikeDetails = async (bikeId, params) => {
  const bike = await db
    .main('bikes')
    .join('bike_group', 'bikes.bike_group_id', 'bike_group.bike_group_id')
    .where({ bike_id: bikeId })
    .first()

  if (!bike) {
    throw errors.customError(
      `Bike ${bikeId} not found`,
      platform.responseCodes.ResourceNotFound,
      'ResourceNotFound',
      false
    )
  }

  if (bike.status === 'active') {
    throw errors.customError(
      'Cannot update vehicle while it is live, move to staging and then try again.',
      platform.responseCodes.Forbidden,
      'Forbidden',
      false
    )
  }
  const bikeUpdates = {
    bike_name: params.bike_name || bike.bike_name
  }

  let qrData
  if (params.qr_code) {
    qrData = await saveQRCode(bikeId, params.qr_code, params.equipmentType)
  }

  await db.main.transaction(async (trx) => {
    await trx('bikes')
      .where({ bike_id: bike.bike_id })
      .update(bikeUpdates)
  })
  const ctrls = await db.main('controllers').where({ bike_id: bike.bike_id })
  const bikeInfo = await db.main('bikes').where({ 'bikes.bike_id': bikeId }).first()
  bikeInfo.controllers = ctrls
  if (qrData) bikeInfo.qrCode = qrData.code
  return bikeInfo
}

const shouldUpdateLocation = async (bikeId) => {
  let updateLocation = true
  try {
    const vehicle = await db.main('bikes').where({ bike_id: bikeId }).first()
    if (vehicle) {
      const isDockedToHub = await db.main('ports').where({ vehicle_uuid: vehicle.bike_uuid }).first()
      if (isDockedToHub) updateLocation = false
    } else updateLocation = false
  } catch (error) {
    Sentry.captureException(error)
    logger(`Error: Failed to check if bike is docked to hub: ${error.message}`)
  }
  return updateLocation
}

/**
 * Get the status (location, battery level) of the bike from Acton API
 * and update those details in the database
 *
 * @param {number} bikeId id of the bike to update
 * @param {object} controller
 * @param {object} client Acton, Omni(Lock), Omni IoT and Teltonika share the same API so re-using makes sense
 */
const updateActonBikeStatus = async (bikeId, controller, client) => {
  try {
    const status = await client.getCurrentStatus(controller.key)
    const updateLocation = await shouldUpdateLocation(bikeId)

    let bikeUpdates = { bike_battery_level: status.extendedData.vehicleBattery || status.extendedData.scooterBattery }
    let ctrlUpdates = { battery_level: status.iotBattery, gps_log_time: status.gpsTimestamp }
    if (updateLocation) {
      bikeUpdates = { ...bikeUpdates, latitude: status.lat, longitude: status.lon }
      ctrlUpdates = { ...ctrlUpdates, latitude: status.lat, longitude: status.lon }
    }
    await db.main.transaction(async (trx) => {
      await trx('bikes')
        .where('bike_id', bikeId)
        .update(bikeUpdates)

      await trx('controllers')
        .where('controller_id', controller.controller_id)
        .update(ctrlUpdates)
    })
    logger(`Info: Updated status for ACTON bike ${bikeId}`)
  } catch (error) {
    Sentry.captureException(error, {bikeId})
    // the bike status update can fail silently since it doesn't block the request
    logger(`Warn: Unable to update status for ACTON bike ${bikeId}: ${error}`)
  }
}

/**
 * Get the status (location, battery level) of the bike from Segway API
 * and update those details in the database
 *
 * @param {number} bikeId id of the bike to update
 */
const updateSegwayBikeStatus = async (segwayApi, bikeId, controller) => {
  try {
    const status = await segwayApi.getVehicleInformation(controller.key)
    const updateLocation = await shouldUpdateLocation(bikeId)

    await db.main.transaction(async (trx) => {
      let ctrlUpdates = {
        battery_level: status.powerPercent,
        gps_log_time: status.gpsUtcTime
      }
      let bikeUpdates = { bike_battery_level: status.powerPercent }
      if (updateLocation) {
        ctrlUpdates = { ...ctrlUpdates, latitude: status.latitude, longitude: status.longitude }
        bikeUpdates = { ...bikeUpdates, latitude: status.latitude, longitude: status.longitude }
      }

      await trx('bikes').where('bike_id', bikeId).update(bikeUpdates)

      await trx('controllers')
        .where('controller_id', controller.controller_id)
        .update(ctrlUpdates)
    })
    logger(`Info: Updated status for segway bike ${bikeId}`)
  } catch (error) {
    Sentry.captureException(error, {bikeId})
    // the bike status update can fail silently since it doesn't block the request
    logger(`Warn: Unable to update status for segway bike ${bikeId}: ${error}`)
  }
}

const updateEdgeBikeStatus = async (EdgeApi, bikeId, controller) => {
  try {
    const status = await EdgeApi.getVehicleInformation(controller.key)
    const updateLocation = await shouldUpdateLocation(bikeId)

    await db.main.transaction(async (trx) => {
      let ctrlUpdates = {
        battery_level: status.powerPercent,
        gps_log_time: status.gpsUtcTime
      }
      let bikeUpdates = { bike_battery_level: status.powerPercent }
      if (updateLocation) {
        ctrlUpdates = { ...ctrlUpdates, latitude: status.latitude, longitude: status.longitude }
        bikeUpdates = { ...bikeUpdates, latitude: status.latitude, longitude: status.longitude }
      }

      await trx('bikes').where('bike_id', bikeId).update(bikeUpdates)

      await trx('controllers')
        .where('controller_id', controller.controller_id)
        .update(ctrlUpdates)
    })
    logger(`Info: Updated status for segway bike ${bikeId}`)
  } catch (error) {
    Sentry.captureException(error, {bikeId})
    // the bike status update can fail silently since it doesn't block the request
    logger(`Warn: Unable to update status for Edge bike ${bikeId}: ${error}`)
  }
}

const updateKisiLockStatus = async (bikeId, controller) => {
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
          .update(location)
      }
    })
    logger(`Info: Updated status for segway bike ${bikeId}`)
  } catch (error) {
    Sentry.captureException(error, {bikeId})
    logger(`Warn: Unable to update status for Kisi lock: ${error}`)
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
const updateGeotabBikeStatus = async (bikeId, controller) => {
  try {
    const deviceLocation = await GeotabApi.getCurrentLocation(controller)
    const updateLocation = await shouldUpdateLocation(bikeId)
    await db.main.transaction(async (trx) => {
      const location = {
        latitude: deviceLocation[0].latitude,
        longitude: deviceLocation[0].longitude
      }

      let ctrlUpdates = {
        gps_log_time: deviceLocation[0].dateTime
      }
      if (updateLocation) {
        ctrlUpdates = { ...ctrlUpdates, ...location }
        await trx('bikes').where('bike_id', bikeId).update(location)
      }

      await trx('controllers')
        .where('controller_id', controller.controller_id)
        .update(ctrlUpdates)
    })
    logger(`Info: Updated status for bike ${bikeId}`)
  } catch (error) {
    Sentry.captureException(error, {bikeId})
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
 * Get the status (location) of the bike from Liinka API
 * and update those details in the database
 *
 * @param {number} bikeId id of the bike to update
 * @param {object}  controller of the bike to update
 */
const updateLinkaBikeStatus = async (bikeId, controller) => {
  try {
    const deviceLocation = await LinkaApi.getCurrentLocation(controller.key)
    const location = {
      latitude: deviceLocation.latitude,
      longitude: deviceLocation.longitude
    }

    const updateLocation = await shouldUpdateLocation(bikeId)
    let dateTime = new Date(deviceLocation.modifiedAt)
    dateTime = dateTime.toISOString().replace('Z', '').replace('T', '')
    await db.main.transaction(async (trx) => {
      let controllerUpdates = { gps_log_time: dateTime }
      if (updateLocation) {
        await trx('bikes').where('bike_id', bikeId).update(location)
        controllerUpdates = { ...controllerUpdates, ...location }
      }

      await trx('controllers')
        .where('controller_id', controller.controller_id)
        .update(controllerUpdates)
    })
    logger(`Info: Updated status for bike ${bikeId}`)
  } catch (error) {
    Sentry.captureException(error, {bikeId})
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
const updateComoduleSegwayEs4BikeStatus = async (bikeId, controller) => {
  try {
    const vehicleState = await comoduleApi.getVehicleState(controller.key)
    const updateLocation = await shouldUpdateLocation(bikeId)

    let bikeUpdates = {
      bike_battery_level: vehicleState.vehicleBatteryPercentage
    }
    let ctrlUpdates = {
      battery_level: vehicleState.moduleBatteryPercentage,
      gps_log_time: vehicleState.lastUpdate
    }
    if (updateLocation) {
      bikeUpdates = { ...bikeUpdates, latitude: vehicleState.gpsLatitude, longitude: vehicleState.gpsLongitude }
      ctrlUpdates = { ...ctrlUpdates, latitude: vehicleState.gpsLatitude, longitude: vehicleState.gpsLongitude }
    }
    await db.main.transaction(async (trx) => {
      await trx('bikes').where('bike_id', bikeId).update(bikeUpdates)

      await trx('controllers')
        .where('controller_id', controller.controller_id)
        .update(ctrlUpdates)
    })
    logger(`Info: Updated status for segway bike ${bikeId}`)
  } catch (error) {
    Sentry.captureException(error, {bikeId})
    // the bike status update can fail silently since it doesn't block the request
    logger(`Warn: Unable to update status for segway bike ${bikeId}: ${error}`)
  }
}

// For locking or unlocking bike depending on the "action" param
const lockOrUnlockBike = async (bikeIdOrUUID, action, hubType = undefined) => {
  const bike = await db
    .main('bikes')
    .join('bike_group', 'bikes.bike_group_id', 'bike_group.bike_group_id')
    // Bypass a bug where sql casts `8dbbdbndnnd...` to number `8`. Hopefully a bike_id !== bike_uuid ever
    .where({
      bike_id: isNaN(Number(bikeIdOrUUID)) ? 99999999999999 : bikeIdOrUUID
    })
    .orWhere({ bike_uuid: bikeIdOrUUID })
    .first()

  if (!bike) {
    throw errors.customError(
      `Bike ${bikeIdOrUUID} not found`,
      platform.responseCodes.ResourceNotFound,
      'ResourceNotFound',
      false
    )
  }

  const bikeId = bike.bike_id

  if (hubType && ['duckt', 'kuhmute'].includes(hubType)) {
    switch (hubType) {
      // Meaning it was a UUID in the param and not a bikeId
      case 'duckt':
        // duckt Adapters allow just unlock
        if (action === 'lock') {
          throw errors.customError(
            `Forbidden operation on a duckt Adapter with bikeID ${bikeId}`,
            platform.responseCodes.InternalServer,
            'Integration Error',
            false
          )
        } else {
          const bikeUUID = bike.bike_uuid
          const dockedToDuckt = await db
            .main('ports')
            .join('hubs', 'ports.hub_uuid', 'hubs.uuid')
            .select(
              'ports.port_id as portId',
              'ports.hub_uuid as hubUUID',
              'ports.vehicle_uuid as vehicleUUID',
              'ports.status as portStatus',
              'ports.number as portNumber',
              'hubs.status as hubStatus'
            )
            .where({ 'ports.vehicle_uuid': bikeUUID })
            .first()
          if (dockedToDuckt && dockedToDuckt.hubStatus === 'Available') {
            try {
              const ducktUser = await setUpApiUser(bike, 'duckt')
              if (!_.isEmpty(ducktUser)) {
                const { Parameter: baseURL } = await getParameterStoreValue('DUCKT/URLS/BASE_URL')
                const { Parameter: callbackUrl } = await getParameterStoreValue('DUCKT/URLS/CALL_BACK_URL')
                DucktApi = new DucktApiClient({
                  baseUrl: baseURL.Value,
                  password: ducktUser.password,
                  userName: ducktUser.userName,
                  callbackUrl: callbackUrl.Value
                })
              }
              let res = await DucktApi.unlock(bike)
              if (res) {
                await db.main('ports').update({ status: 'Available', vehicle_uuid: null, current_status: 'parked' }).where({ port_id: dockedToDuckt.portId })
                // After unlocking, docked should be false
                const port = await db
                  .main('ports')
                  .select('*')
                  .where({ port_id: dockedToDuckt.portId })
                  .first()
                return {
                  port,
                  apiResponse: res
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
              Sentry.captureException(error, {bikeId})
              logger('Error undocking vehicle from duckt Hub')
              throw errors.customError(
                `Error undocking vehicle ${bikeId} from Hub`,
                platform.responseCodes.InternalServer,
                'Integration Error',
                false
              )
            }
          }
        }
        break
      case 'kuhmute':
        // TODO: Handle Kuhumute as a case
        if (action === 'lock') {
          throw errors.customError(
            `Forbidden operation lock on Kuhmute with bikeID ${bikeId}`,
            platform.responseCodes.InternalServer,
            'Integration Error',
            false
          )
        } else if (action === 'unlock') {
          const bikeUUID = bike.bike_uuid
          const dockedToKuhmute = await db
            .main('ports')
            .join('hubs', 'ports.hub_uuid', 'hubs.uuid')
            .select(
              'ports.port_id as portId',
              'ports.hub_uuid as hubUUID',
              'ports.vehicle_uuid as vehicleUUID',
              'ports.status as portStatus',
              'ports.number as portNumber',
              'hubs.status as hubStatus'
            )
            .where({ 'ports.vehicle_uuid': bikeUUID })
            .first()
          if (dockedToKuhmute && dockedToKuhmute.hubStatus === 'Available') {
            try {
              const KuhmuteApiClient = new KuhmuteApi(bike.fleet_id)
              let res = await KuhmuteApiClient.unlockVehicle(bikeUUID)
              res = res && JSON.parse(res)
              if (res && res.uuid && res.charge && !res.charge.docked) {
                // After unlocking, docked should be false
                const port = await db
                  .main('ports')
                  .select('*')
                  .where({ port_id: dockedToKuhmute.portId })
                  .first()
                return {
                  port,
                  apiResponse: res
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
              Sentry.captureException(error, {bikeId})
              logger('Error undocking vehicle from Kuhmute Hub')
              throw errors.customError(
                `Error undocking vehicle ${bikeId} from Hub`,
                platform.responseCodes.InternalServer,
                'Integration Error',
                false
              )
            }
          }
        }
        break
      default:
        throw errors.customError(
          `Operation not supported for adapter with bikeID ${bikeId}`,
          platform.responseCodes.InternalServer,
          'Integration Error',
          false
        )
    }
  }

  const controllers = await db
    .main('controllers')
    .join('bikes', 'bikes.bike_id', 'controllers.bike_id')
    .where({ 'controllers.bike_id': bikeId })
  // For vehicles with multiple controllers, only pick the controller with remote lock capability i.e an IoT
  const controllerWithRemoteLock = controllers.find((ctrl) =>
    ['iot'].includes(ctrl.device_type)
  )
  const controller = controllerWithRemoteLock

  if (action === 'unlock') {
    const bikeUUID = bike.bike_uuid
    const dockedToKuhmute = db
      .main('ports')
      .join('hubs', 'ports.hub_uuid', 'hubs.uuid')
      .select(
        'ports.hub_uuid as hubUUID',
        'ports.vehicle_uuid as vehicleUUID',
        'ports.status as portStatus',
        'ports.number as portNumber',
        'hubs.status as hubStatus'
      )
      .where({ 'ports.vehicle_uuid': bikeUUID })
    if (
      dockedToKuhmute &&
      dockedToKuhmute.portStatus === 'Available' &&
      dockedToKuhmute.hubStatus === 'Available'
    ) {
      try {
        const KuhmuteApiClient = new KuhmuteApi(bike.fleet_id)
        const res = await KuhmuteApiClient.unlockVehicle(bikeUUID)
        if (res && res.charge && !res.charge.docked) {
          // After unlocking, docked should be false
          return res
        } else {
          throw errors.customError(
            `Could not undock vehicle ${bikeUUID} from Hub`,
            platform.responseCodes.InternalServer,
            'Integration Error',
            false
          )
        }
      } catch (error) {
        Sentry.captureException(error, {bikeId})
        logger('Error undocking vehicle from Kuhmute Hub')
        throw errors.customError(
          `Error undocking vehicle ${bikeId} from Hub`,
          platform.responseCodes.InternalServer,
          'Integration Error',
          false
        )
      }
    }
  }

  if (!controller) {
    throw errors.customError(
      `No controller with remote lock functionality found for ${bike.bike_name || bikeId
      }`,
      platform.responseCodes.ResourceNotFound,
      'ResourceNotFound',
      false
    )
  }

  const integrations = await getFleetIntegrations(controller.fleet_id)
  const segwayIntegration = integrations.filter(
    (integration) => integration.integration_type === 'segway'
  )
  let segwaySettings
  if (segwayIntegration.length) {
    segwaySettings = segwayIntegration[0].metadata
  }

  let lockResponse
  let unlockResponse
  // This is more reliable to go around multiple controllers
  switch (controller.vendor) {
    case 'Segway':
      if (action === 'lock') {
        if (segwaySettings) {
          await segwayApiClientForUs.toggleLights(
            0,
            controller.key
          )
        }
        lockResponse = await segwayApiClientForUs.lock(controller.key)
        updateSegwayBikeStatus(segwayApiClientForUs, bikeId, controller)
        return lockResponse
      } else if (action === 'unlock') {
        if (segwaySettings) {
          await segwayApiClientForUs.toggleLights(
            segwaySettings.controlType,
            controller.key
          )
          await segwayApiClientForUs.setSpeedMode(
            segwaySettings.speedMode ? segwaySettings.speedMode : 2,
            controller.key
          )
          await segwayApiClientForUs.setMaximumSpeedLimit(
            segwaySettings.segwayMaximumSpeedLimit,
            controller.key
          )
        }
        unlockResponse = segwayApiClientForUs.unlock(controller.key)
        updateSegwayBikeStatus(segwayApiClientForUs, bikeId, controller)
        return unlockResponse
      }
      break

    case 'Segway IoT EU':
      if (action === 'lock') {
        if (segwaySettings) {
          await segwayApiClientForEu.toggleLights(
            0,
            controller.key
          )
        }
        lockResponse = await segwayApiClientForEu.lock(controller.key)
        updateSegwayBikeStatus(segwayApiClientForEu, bikeId, controller)
        return lockResponse
      } else if (action === 'unlock') {
        if (segwaySettings) {
          await segwayApiClientForEu.toggleLights(
            segwaySettings.controlType,
            controller.key
          )
          await segwayApiClientForEu.setSpeedMode(
            segwaySettings.speedMode ? segwaySettings.speedMode : 2,
            controller.key
          )
          await segwayApiClientForEu.setMaximumSpeedLimit(
            segwaySettings.segwayMaximumSpeedLimit,
            controller.key
          )
        }
        unlockResponse = segwayApiClientForEu.unlock(controller.key)
        updateSegwayBikeStatus(segwayApiClientForEu, bikeId, controller)
        return unlockResponse
      }
      break

    case 'Geotab IoT':
      const geotabUser = await setUpGeotabApiUser(bike)
      if (!_.isEmpty(geotabUser)) {
        GeotabApi = new GeotabApiClient({
          password: geotabUser.password,
          userName: geotabUser.userName,
          sessionId: geotabUser.sessionId,
          database: geotabUser.database,
          serverName: geotabUser.serverName
        })
      }
      if (action === 'lock') {
        const lockResponse = await GeotabApi.lock(controller)
        const [integrationData] = await getGeotabApiKey({
          fleet_id: controller.fleet_id
        })
        let metadata = JSON.parse(integrationData.metadata)
        try {
          await db
            .main('integrations')
            .update({ metadata: JSON.stringify(metadata) })
            .where({ fleet_id: controller.fleet_id, integration_type: 'geotab' })
        } catch (error) {
          Sentry.captureException(error, {bikeId, fleet_id: controller.fleet_id})
          logger(
            `Warn: Unable to update lock status for Geotab with controller ${controller.key}: ${error}`
          )
          throw errors.customError(
            `Unable to update lock status for Geotab with id ${controller.key}`,
            platform.responseCodes.InternalServer,
            'InternalServer',
            false
          )
        }
        const [message] = lockResponse
        await updateGeotabBikeStatus(bikeId, controller)
        return message
      } else if (action === 'unlock') {
        const lockResponse = await GeotabApi.unlock(controller)
        const [integrationData] = await getGeotabApiKey({
          fleet_id: controller.fleet_id,
          integration_type: 'geotab'
        })
        let metadata = JSON.parse(integrationData.metadata)
        try {
          await db
            .main('integrations')
            .update({ metadata: JSON.stringify(metadata) })
            .where({ fleet_id: controller.fleet_id, integration_type: 'geotab', bikeId })
        } catch (error) {
          Sentry.captureException(error, {bikeId, fleet_id: controller.fleet_id})
          logger(
            `Warn: Unable to update lock status for Geotab with controller ${controller.key}: ${error}`
          )
          throw errors.customError(
            `Unable to update lock status for Geotab with id ${controller.key}`,
            platform.responseCodes.InternalServer,
            'InternalServer',
            false
          )
        }
        const [message] = lockResponse
        await updateGeotabBikeStatus(bikeId, controller)
        return message
      }
      break

    case 'COMODULE Ninebot ES4':
      if (action === 'lock') {
        lockResponse = await comoduleApi.lock(controller.key)
        updateComoduleSegwayEs4BikeStatus(bikeId, controller)
        return lockResponse
      } else if (action === 'unlock') {
        unlockResponse = comoduleApi.unlock(controller.key)
        updateComoduleSegwayEs4BikeStatus(bikeId, controller)
        return unlockResponse
      }
      break

    // For acton vehicles it's start-unlock and stop-lock
    case 'ACTON':
    case 'Omni IoT':
    case 'Teltonika':
    case 'Omni Lock':
    case 'Okai':
      let client = actonFamily[controller.vendor]
      if (!client) throw new Error(`ACTON family vendor ${controller.vendor} not supported`)
      if (action === 'lock') {
        // Stop and lock vehicle
        lockResponse = await client.stopVehicle(controller.key)
        updateActonBikeStatus(bikeId, controller, client)
        return lockResponse
      } else if (action === 'unlock') {
        if (controller.vendor === 'Omni Lock') client = OmniAPI
        unlockResponse = await client.startVehicle(controller.key)
        updateActonBikeStatus(bikeId, controller, client)
        return unlockResponse
      }
      break

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
      if (action === 'lock') {
        lockResponse = await LinkaApi.lock(controller.key)
        await updateLinkaBikeStatus(bikeId, controller)
        return lockResponse
      } else if (action === 'unlock') {
        unlockResponse = await LinkaApi.unlock(controller.key)
        await updateLinkaBikeStatus(bikeId, controller)
        return unlockResponse
      }
      break
    case 'Kisi':
      if (action === 'lock') {
        logger(`Error: ${controller.vendor} controller does not support lock action`)
        throw errors.customError(
          `${controller.vendor} controller does not support lock action`,
          platform.responseCodes.BadRequest,
          'BadRequest',
          false
        )
      } else if (action === 'unlock') {
        unlockResponse = await new Kisi().unlock(controller.fleet_id, controller.key)
        if (unlockResponse.error) { throw new Error(unlockResponse.error) }
        await updateKisiLockStatus(bikeId, controller)
        return unlockResponse
      }
      break
    case 'Sentinel':
      if (action === 'lock') {
        logger(`Error: ${controller.vendor} controller does not support lock action`)
        throw errors.customError(
          `${controller.vendor} controller does not support lock action`,
          platform.responseCodes.BadRequest,
          'BadRequest',
          false
        )
      } else if (action === 'unlock') {
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
            unlockResponse = await sentinelClient.unlock(controller.key)
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
      break
    case 'Edge':
      const EdgeClient = new EdgeApiClient(controller.fleet_id)
      if (action === 'lock') {
        let result = await EdgeClient.lock(controller.key)
        return result
      } else if (action === 'unlock') {
        unlockResponse = await EdgeClient.unlock(controller.key)
        if (unlockResponse.error) { throw new Error(unlockResponse.error) }
        await updateEdgeBikeStatus(EdgeClient, bikeId, controller)
        return unlockResponse
      }
      break
    default:
      logger(`Error: ${controller.vendor} controller is not supported`)
      throw errors.customError(
        `${controller.vendor} controller is not supported`,
        platform.responseCodes.BadRequest,
        'BadRequest',
        false
      )
  }
}

// open battery cover for Segway ProMax
const openCover = async (bikeId) => {
  const bike = await db
    .main('bikes')
    .join('bike_group', 'bikes.bike_group_id', 'bike_group.bike_group_id')
    .where({ bike_id: bikeId })
    .first()

  if (!bike) {
    throw errors.customError(
      `Bike ${bikeId} not found`,
      platform.responseCodes.ResourceNotFound,
      'ResourceNotFound',
      false
    )
  }
  const controllers = await db
    .main('controllers')
    .join('bikes', 'bikes.bike_id', 'controllers.bike_id')
    .where({ 'controllers.bike_id': bike.bike_id })

  // For vehicles with multiple controllers, only pick the controller with remote lock capability i.e an IoT
  const controller = controllers.find((ctrl) =>
    ['iot'].includes(ctrl.device_type)
  )
  if (!controller) {
    throw errors.customError(
      `No controller with remote lock functionality found for ${bike.bike_name || bike.bike_id
      }`,
      platform.responseCodes.ResourceNotFound,
      'ResourceNotFound',
      false
    )
  }
  let openResponse
  switch (controller.vendor) {
    case 'Segway':
      openResponse = await segwayApiClientForUs.openCover(controller.key)
      return openResponse

    case 'Segway IoT EU':
      openResponse = await segwayApiClientForEu.openCover(controller.key)
      return openResponse
  }
}

// turn off bike lights on ending trip
const setBikeLights = async (controlType, controller) => {
  let lights
  try {
    if (controller.vendor === 'Segway') {
      lights = await segwayApiClientForUs.toggleLights(
        controlType,
        controller.key
      )
    } else {
      lights = await segwayApiClientForEu.toggleLights(
        controlType,
        controller.key
      )
    }
    return lights
  } catch (error) {
    Sentry.captureException(error)
    logger(`Error: unable to toggle lights for segway: ${error}`)
    throw error
  }
}

const updateSegwayIot = async (segwayApi, bike, params) => {
  let status
  try {
    status = await getStatusFromVendor(params.iot_module_type, params.iot_key)
  } catch (error) {
    Sentry.captureException(error)
    logger(
      `Error: failed to get vehicle status from ${params.iot_module_type} API: ${error.message}`
    )
    throw errors.customError(
      `Failed to get vehicle status from ${params.iot_module_type} API, the key may be invalid`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }

  const controller = await db
    .main('controllers')
    .where({
      bike_id: bike.bike_id,
      device_type: 'iot'
    })
    .first()

  try {
    const insertedId = await db.main.transaction(async (trx) => {
      const bikeUpdates = {
        current_status: bikeConstants.current_status.controllerAssigned,
        ...status.location,
        ...status.bike
      }
      await trx('bikes')
        .where({ bike_id: bike.bike_id })
        .update(bikeUpdates)

      const ctrlUpdates = {
        key: params.iot_key,
        ...status.location,
        ...status.controller
      }

      if (params.qr_code) {
        ctrlUpdates.qr_code = params.qr_code
      }
      if (controller) {
        await trx('controllers')
          .where({ controller_id: controller.controller_id })
          .update(ctrlUpdates)
      } else {
        const [id] = await trx('controllers').insert({
          device_type: 'iot',
          key: params.iot_key,
          qr_code: params.qr_code,
          vendor: params.iot_module_type,
          make: bike.make,
          model: bike.model,
          fleet_id: bike.fleet_id,
          bike_id: bike.bike_id,
          ...status.location,
          ...status.controller
        })
        return id
      }
    })
    return db
      .main('controllers')
      .where({
        controller_id: controller ? controller.controller_id : insertedId
      })
      .first()
  } catch (error) {
    Sentry.captureException(error)
    if (error.code === 'ER_DUP_ENTRY') {
      throw errors.customError(
        `Key is already assigned to another vehicle`,
        platform.responseCodes.Conflict,
        'Conflict',
        false
      )
    }
    throw error
  }
}

const updateKisiIoT = async (bike, params) => {
  try {
    const lockStatus = await new Kisi().fetchLock(bike.fleet_id, params.iot_key)
    if (!lockStatus) throw new Error('Failed to fetch Kisi lock status')
    const controller = await db.main('controllers').where({bike_id: bike.bike_id, key: params.iot_key}).first()
    const insertedId = await db.main.transaction(async (trx) => {
      const bikeUpdates = {
        current_status: bikeConstants.current_status.controllerAssigned,
        latitude: lockStatus.place.latitude,
        longitude: lockStatus.place.longitude
      }

      if (params.bike_name) bikeUpdates.bike_name = params.bike_name

      await trx('bikes')
        .where({ bike_id: bike.bike_id })
        .update(bikeUpdates)

      if (controller) {
        await trx('controllers')
          .where({ controller_id: controller.controller_id })
          .update({
            key: params.iot_key,
            latitude: lockStatus.place.latitude,
            longitude: lockStatus.place.longitude
          })
      } else {
        const [id] = await trx('controllers').insert({
          device_type: 'iot',
          key: params.iot_key,
          vendor: params.iot_module_type,
          make: bike.make,
          model: bike.model,
          fleet_id: bike.fleet_id,
          bike_id: bike.bike_id,
          latitude: lockStatus.place.latitude,
          longitude: lockStatus.place.longitude
        })
        return id
      }
    })
    return db
      .main('controllers')
      .where({
        controller_id: controller ? controller.controller_id : insertedId
      })
  } catch (error) {
    Sentry.captureException(error)
    logger(
      error.message || `Error: Failed to update vehicle equipment`
    )
    throw errors.customError(
      error.message || `Failed to update vehicle equipment`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}
const updateSasIot = async (bike, params) => {
  try {
    const controller = await db.main('controllers').where({bike_id: bike.bike_id, key: params.iot_key}).first()
    const insertedId = await db.main.transaction(async (trx) => {
      const bikeUpdates = {
        current_status: bikeConstants.current_status.lockAssigned
      }

      if (params.bike_name) bikeUpdates.bike_name = params.bike_name

      await trx('bikes')
        .where({ bike_id: bike.bike_id })
        .update(bikeUpdates)

      if (controller) {
        await trx('controllers')
          .where({ controller_id: controller.controller_id })
          .update({
            key: params.iot_key
          })
      } else {
        const [id] = await trx('controllers').insert({
          device_type: 'lock',
          key: params.iot_key,
          vendor: params.iot_module_type,
          make: bike.make,
          model: bike.model,
          fleet_id: bike.fleet_id,
          bike_id: bike.bike_id
        })
        return id
      }
    })
    return db
      .main('controllers')
      .where({
        controller_id: controller ? controller.controller_id : insertedId
      })
  } catch (error) {
    Sentry.captureException(error)
    logger(
      error.message || `Error: Failed to update vehicle equipment`
    )
    throw errors.customError(
      error.message || `Failed to update vehicle equipment`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

const updateSentinelIoT = async (bike, params) => {
  try {
    const lockStatus = await new Sentinel().fetchLockStatus(params.iot_key)
    if (!lockStatus) throw new Error('Failed to fetch Sentinel lock status')
    const controller = await db.main('controllers').where({bike_id: bike.bike_id, key: params.iot_key}).first()
    const latitude = lockStatus.body.geoPoint.latitude
    const longitude = lockStatus.body.geoPoint.longitude
    const voltage = lockStatus.body.voltagePercentage
    const insertedId = await db.main.transaction(async (trx) => {
      const bikeUpdates = {
        current_status: bikeConstants.current_status.controllerAssigned,
        latitude,
        longitude,
        bike_battery_level: voltage
      }

      if (params.bike_name) bikeUpdates.bike_name = params.bike_name

      await trx('bikes')
        .where({ bike_id: bike.bike_id })
        .update(bikeUpdates)
      if (controller) {
        await trx('controllers')
          .where({ controller_id: controller.controller_id })
          .update({
            key: params.iot_key,
            latitude,
            longitude
          })
      } else {
        const [id] = await trx('controllers').insert({
          device_type: 'iot',
          key: params.iot_key,
          vendor: params.iot_module_type,
          make: bike.make,
          model: bike.model,
          fleet_id: bike.fleet_id,
          bike_id: bike.bike_id,
          latitude,
          longitude,
          battery_level: voltage
        })
        return id
      }
    })
    return db
      .main('controllers')
      .where({
        controller_id: controller ? controller.controller_id : insertedId
      }).first()
  } catch (error) {
    Sentry.captureException(error)
    logger(
      error.message || `Error: Failed to update vehicle equipment`
    )
    throw errors.customError(
      error.message || `Failed to update vehicle equipment`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

const updateEdgeIoT = async (bike, params) => {
  try {
    const EdgeClient = new EdgeApiClient(bike.fleet_id)
    const lockStatus = await EdgeClient.getVehicleInformation(params.iot_key)
    if (!lockStatus) throw new Error('Failed to fetch Edge vehicle status')
    const controller = await db.main('controllers').where({bike_id: bike.bike_id, key: params.iot_key}).first()
    const latitude = lockStatus.latitude
    const longitude = lockStatus.longitude
    const voltage = lockStatus.powerPercent
    const insertedId = await db.main.transaction(async (trx) => {
      const bikeUpdates = {
        current_status: bikeConstants.current_status.controllerAssigned,
        latitude,
        longitude,
        bike_battery_level: voltage
      }

      if (params.bike_name) bikeUpdates.bike_name = params.bike_name

      await trx('bikes')
        .where({ bike_id: bike.bike_id })
        .update(bikeUpdates)
      if (controller) {
        await trx('controllers')
          .where({ controller_id: controller.controller_id })
          .update({
            key: params.iot_key,
            latitude,
            longitude
          })
      } else {
        const [id] = await trx('controllers').insert({
          device_type: 'iot',
          key: params.iot_key,
          vendor: params.iot_module_type,
          make: bike.make,
          model: bike.model,
          fleet_id: bike.fleet_id,
          bike_id: bike.bike_id,
          latitude,
          longitude,
          battery_level: voltage
        })
        return id
      }
    })
    return db
      .main('controllers')
      .where({
        controller_id: controller ? controller.controller_id : insertedId
      }).first()
  } catch (error) {
    Sentry.captureException(error)
    logger(
      error.message || `Error: Failed to update vehicle equipment`
    )
    throw errors.customError(
      error.message || `Failed to update vehicle equipment`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

const generateGrowCommands = (fleetIntegrationSettings) => {
  const growConstants = bikeConstants.grinIoTDefaultSettings
  const heartBeatIntervalCommand = `39,${growConstants.lockedHeartBeatInterval}`
  const commands = [heartBeatIntervalCommand]
  if (fleetIntegrationSettings) {
    const speedLimitCommand = `27,${fleetIntegrationSettings.growMaximumSpeed ||
      growConstants.growMaximumSpeed
    }`
    const driveModeCommand = `6,${fleetIntegrationSettings.scooterDriveMode ||
      growConstants.scooterDriveMode
    }`
    const brakeModeCommand = `8,${fleetIntegrationSettings.scooterBrakeMode ||
      growConstants.scooterBrakeMode
    }`
    commands.push(...[speedLimitCommand, driveModeCommand, brakeModeCommand])
  } else {
    commands.push(
      ...[
        `27,${growConstants.growMaximumSpeed}`,
        `6,${growConstants.scooterDriveMode}`,
        `8,${growConstants.scooterBrakeMode}`
      ]
    )
  }
  return commands
}

const getAndApplyGrowSettings = async (deviceId) => {
  try {
    const ctrl = await db.main('controllers').where({ key: deviceId }).first()
    if (ctrl) {
      const growDetails = { fleetId: ctrl.fleet_id }
      applyGrowSettings(growDetails, deviceId)
    }
  } catch (error) {
    Sentry.captureException(error)
    logger(`Error applying Grow settings on ${deviceId}`)
  }
}

// Called when adding a Grow IoT to a bike
const updateGrowIoT = async (bike, params) => {
  const controller = await db
    .main('controllers')
    .where({
      bike_id: bike.bike_id,
      device_type: 'iot'
    })
    .first()
  try {
    const insertedId = await db.main.transaction(async (trx) => {
      await trx('bikes')
        .where({ bike_id: bike.bike_id })
        .update({
          bike_name:
            params.bike_name || bike.bike_name || `Segway ${params.qr_code}`,
          current_status: bikeConstants.current_status.controllerAssigned
        })
      if (controller) {
        await trx('controllers')
          .where({ controller_id: controller.controller_id })
          .update({
            key: params.iot_key,
            qr_code: params.qr_code
          })
      } else {
        const [id] = await trx('controllers').insert({
          device_type: 'iot',
          key: params.iot_key,
          qr_code: params.qr_code,
          vendor: params.iot_module_type,
          make: bike.make,
          model: bike.model,
          fleet_id: bike.fleet_id,
          bike_id: bike.bike_id
        })
        return id
      }
    })
    client.subscribe(`s/a/17/${params.iot_key}`)
    client.subscribe(`s/h/17/${params.iot_key}`)
    let fleetIntegrationSettings = await db
      .main('integrations')
      .where({ fleet_id: bike.fleet_id, integration_type: 'grow' })
      .select('metadata')
      .first()
    fleetIntegrationSettings =
      fleetIntegrationSettings && JSON.parse(fleetIntegrationSettings.metadata)
    const commands = generateGrowCommands(fleetIntegrationSettings)
    // Apply fleet settings to Grow IoT
    for (let command of commands) {
      executeCommand(`s/c/${params.iot_key}`, command, (err, message) => {
        if (err) {
          logger(`Error executing Grow Command ${params.key}`)
        }
      })
    }
    return db
      .main('controllers')
      .where({
        controller_id: controller ? controller.controller_id : insertedId
      })
      .first()
  } catch (error) {
    Sentry.captureException(error)
    if (error.code === 'ER_DUP_ENTRY') {
      throw errors.customError(
        `Key is already assigned to another vehicle`,
        platform.responseCodes.Conflict,
        'Conflict',
        false
      )
    }
    throw error
  }
}

const applyGrowSettings = async (growDetails, deviceId) => {
  let controllerKeys = []
  if (deviceId) controllerKeys = [deviceId]
  else {
    const controllers = await db
      .main('controllers')
      .where({ fleet_id: growDetails.fleetId, vendor: 'Grow' })
      .select('key')
    controllerKeys = controllers.map((item) => item.key)
  }
  let fleetIntegrationSettings = await db
    .main('integrations')
    .where({ fleet_id: growDetails.fleetId, integration_type: 'grow' })
    .select('metadata')
    .first()
  fleetIntegrationSettings =
    fleetIntegrationSettings &&
    JSON.parse(fleetIntegrationSettings.metadata)
  const commands = generateGrowCommands(fleetIntegrationSettings)
  // Apply fleet settings to Grow IoT
  for (let key of controllerKeys) {
    for (let command of commands) {
      executeCommand(`s/c/${key}`, command, (err, message) => {
        if (err) {
          logger(`Error executing Grow Command ${key}`)
        }
      })
    }
  }
}

const applyACTONSettings = async (actonDetails, vendor) => { // Vendor can be ACTON or Omni
  let controllers = await db
    .main('controllers')
    .where({ fleet_id: actonDetails.fleetId, vendor })
    .select('key', 'vendor')
  controllers = controllers.map((item) => ([item.key, item.vendor]))

  let fleetIntegrationSettings = await db
    .main('integrations')
    .where({ fleet_id: actonDetails.fleetId, integration_type: vendor })
    .select('metadata')
    .first()
  fleetIntegrationSettings =
    fleetIntegrationSettings &&
    JSON.parse(fleetIntegrationSettings.metadata)
  const speedLimit = fleetIntegrationSettings.maximumSpeedLimit || 20
  // Apply fleet settings to ACTON/Omni IoT/Teltonika vehicle
  for (let ctrl of controllers) {
    const client = actonFamily[ctrl.vendor]
    if (client) client.setSpeedLimit(ctrl[0], speedLimit)
  }
}

const updateGrowFleetSettings = async (growDetails) => {
  try {
    if (growDetails.apply && !growDetails.default) {
      delete growDetails.apply
      // Updating settings
      const where = { fleet_id: growDetails.fleetId, integration_type: 'grow' }
      await db
        .main('integrations')
        .update({ metadata: JSON.stringify(growDetails) })
        .where(where)
      const updated = await db
        .main('integrations')
        .where(where)
        .select(
          'integrations_id',
          'fleet_id',
          'email',
          'integration_type',
          'metadata'
        )
        .first()
      applyGrowSettings(growDetails)
      return { ...updated, metadata: JSON.parse(updated.metadata) }
    }
    if (growDetails.default) delete growDetails.default
    const checkForDuplicate = await db
      .main('integrations')
      .where({ integration_type: 'grow', fleet_id: growDetails.fleetId })
      .first()
    if (checkForDuplicate) {
      throw new Error(
        `The fleet(${growDetails.fleetId}) already has a Grow integration`
      )
    }
    const data = {
      fleet_id: growDetails.fleetId,
      integration_type: 'grow',
      metadata: JSON.stringify(growDetails)
    }
    const results = await db.main('integrations').insert(data)
    applyGrowSettings(growDetails)

    const inserted = await db
      .main('integrations')
      .where({ integrations_id: results[0] })
      .select(
        'integrations_id',
        'fleet_id',
        'email',
        'integration_type',
        'metadata'
      )
      .first()
    return { ...inserted, metadata: JSON.parse(inserted.metadata) }
  } catch (error) {
    Sentry.captureException(error)
    throw new Error(
      errors.customError(
        error.message || `Error saving Grow settings`,
        platform.responseCodes.InternalServer,
        'Conflict',
        false
      )
    )
  }
}

/**
 * Apply sound setting to all ACTONs, Omni IoT and Teltonika in a fleet async
 *
 */
const updateACTONFleetSettings = async (actonDetails) => {
  try {
    if (actonDetails.apply && !actonDetails.default) {
      delete actonDetails.apply
      // Updating settings
      const where = { fleet_id: actonDetails.fleetId, integration_type: 'ACTON' }
      await db
        .main('integrations')
        .update({ metadata: JSON.stringify(actonDetails) })
        .where(where)
      const updated = await db
        .main('integrations')
        .where(where)
        .select(
          'integrations_id',
          'fleet_id',
          'email',
          'integration_type',
          'metadata'
        )
        .first()
      applyACTONSettings(actonDetails, actonDetails.vendor)
      return { ...updated, metadata: JSON.parse(updated.metadata) }
    }
    if (actonDetails.default) delete actonDetails.default
    const checkForDuplicate = await db
      .main('integrations')
      .where({ integration_type: actonDetails.vendor, fleet_id: actonDetails.fleetId })
      .first()
    if (checkForDuplicate) {
      throw new Error(
        `The fleet(${actonDetails.fleetId}) already has an ACTON integration`
      )
    }
    const data = {
      fleet_id: actonDetails.fleetId,
      integration_type: actonDetails.vendor,
      metadata: JSON.stringify(actonDetails)
    }
    const results = await db.main('integrations').insert(data)
    applyACTONSettings(actonDetails, actonDetails.vendor)

    const inserted = await db
      .main('integrations')
      .where({ integrations_id: results[0] })
      .select(
        'integrations_id',
        'fleet_id',
        'email',
        'integration_type',
        'metadata'
      )
      .first()
    return { ...inserted, metadata: JSON.parse(inserted.metadata) }
  } catch (error) {
    Sentry.captureException(error)
    throw new Error(
      errors.customError(
        error.message || `Error saving ${actonDetails.vendor} settings`,
        platform.responseCodes.InternalServer,
        'Conflict',
        false
      )
    )
  }
}

/**
 * Apply sound setting to all segways in a fleet async
 *
 * @param {*} fleetId
 * @param {*} mode 0 →no setting, 1→off, 2→on
 */
const applySegwaySoundSettings = async (fleetId, mode) => {
  try {
    const controllers = await db.main('controllers')
      .whereIn('vendor', ['Segway', 'Segway IoT EU'])
      .andWhere({ fleet_id: fleetId })
      .select('controller_id', 'vendor', 'key', 'fleet_id')

    const soundUpdates = controllers.map(async ctrl => {
      if (ctrl.vendor === 'Segway') await segwayApiClientForUs.setSound(ctrl.key, mode)
      else if (ctrl.vendor === 'Segway IoT EU') await segwayApiClientForEu.setSound(ctrl.key, mode)
    })
    await Promise.all(soundUpdates)
  } catch (error) {
    Sentry.captureException(error)
    logger(`Error setting sound on...`)
  }
}

const saveIntegrationSettings = async (settings) => {
  try {
    switch (settings.integrationType) {
      case 'kisi':
        const isKisiAdded = await db.main('integrations').where({ fleet_id: settings.fleetId, integration_type: settings.integrationType }).first()
        if (isKisiAdded) throw new Error('Kisi integration already exists for this fleet')
        await db.main('integrations').insert({ fleet_id: settings.fleetId, integration_type: settings.integrationType, api_key: settings.apiKey })
        const integration = await db.main('integrations').where({ integration_type: settings.integrationType, fleet_id: settings.fleetId }).select('integrations_id as id', 'fleet_id as fleetId', 'email', 'integration_type as integrationType').first()
        return integration
      case 'Edge':
        const isEdgeAdded = await db.main('integrations').where({ fleet_id: settings.fleetId, integration_type: settings.integrationType }).first()
        if (isEdgeAdded) throw new Error('Edge integration already exists for this fleet')
        await db.main('integrations').insert({ fleet_id: settings.fleetId, integration_type: settings.integrationType, api_key: settings.apiKey })
        const integrationEdge = await db.main('integrations').where({ integration_type: settings.integrationType, fleet_id: settings.fleetId }).select('integrations_id as id', 'fleet_id as fleetId', 'email', 'integration_type as integrationType').first()
        return integrationEdge
      default:
        throw new Error(`An unknown integration type ${settings.integrationType} submitted`)
    }
  } catch (error) {
    Sentry.captureException(error)
    throw new Error(`An error occurred saving the integration ${settings.integrationType}`)
  }
}

const updateSegwayFleetSettings = async (segwayDetails) => {
  try {
    const speedLimits = {}
    speedLimits['segwayMaximumSpeedLimit'] =
      segwayDetails.segwayMaximumSpeedLimit
    if (segwayDetails.apply) {
      const where = {
        fleet_id: segwayDetails.fleetId,
        integration_type: 'segway'
      }
      await db
        .main('integrations')
        .update({ metadata: JSON.stringify(segwayDetails) })
        .where(where)
      if (segwayDetails.soundMode) applySegwaySoundSettings(segwayDetails.fleetId, segwayDetails.soundMode)
      return await db
        .main('integrations')
        .where(where)
        .select(
          'integrations_id',
          'fleet_id',
          'email',
          'integration_type',
          'metadata'
        )
        .first()
    }
    const checkForDuplicate = await db
      .main('integrations')
      .where({ integration_type: 'segway', fleet_id: segwayDetails.fleetId })
      .first()
    if (checkForDuplicate) {
      throw new Error(
        `The fleet(${segwayDetails.fleetId}) already has a Segway integration`
      )
    }
    const data = {
      fleet_id: segwayDetails.fleetId,
      integration_type: 'segway',
      metadata: JSON.stringify(segwayDetails)
    }
    const results = await db.main('integrations').insert(data)
    if (segwayDetails.soundMode) applySegwaySoundSettings(segwayDetails.fleetId, segwayDetails.soundMode)
    return await db
      .main('integrations')
      .where({ integrations_id: results[0] })
      .select(
        'integrations_id',
        'fleet_id',
        'email',
        'integration_type',
        'metadata'
      )
      .first()
  } catch (error) {
    Sentry.captureException(error)
    throw new Error(
      errors.customError(
        error.message || `Error saving Segway settings`,
        platform.responseCodes.InternalServer,
        'Conflict',
        false
      )
    )
  }
}

/**
 *
 * @param {string} vendor Vendor
 * @param {string} key Unique key for vehicle (varies by vendor)
 *
 * Get the status of the vehicle from the vendor and return it in
 * a normalized format:
 *  {
      location: {
        latitude: ...,
        longitude: ...
      },
      bike: {
        bike_battery_level: ...
      },
      controller: {
        battery_level: ...,
        gps_log_time: ...,
        fw_version: ...
      }
    }
 */
const getStatusFromVendor = async (vendor, key) => {
  switch (vendor) {
    case 'Segway':
      return getStatusFromSegway(segwayApiClientForUs, key)

    case 'Segway IoT EU':
      return getStatusFromSegway(segwayApiClientForEu, key)

    case 'COMODULE Ninebot ES4':
      return getStatusFromComoduleNinebotEs4(key)

    default:
      logger(`Error: ${vendor} IoT is not supported`)
      throw errors.customError(
        `${vendor} IoT is not supported`,
        platform.responseCodes.BadRequest,
        'BadRequest',
        false
      )
  }
}

const getStatusFromSegway = async (apiClient, imei) => {
  const status = await apiClient.getVehicleInformation(imei)
  const normalizedStatus = {
    location: {
      latitude: status.latitude,
      longitude: status.longitude
    },
    bike: {
      bike_battery_level: status.powerPercent
    },
    controller: {
      battery_level: status.powerPercent,
      gps_log_time: status.gpsUtcTime
    }
  }

  try {
    const firmware = await apiClient.getFirmwareVersion(imei)
    if (firmware) {
      normalizedStatus.controller.fw_version = firmware.iotVersion
    }
  } catch (error) {
    Sentry.captureException(error)
    // this can fail if the vehicle is offline but that shouldn't prevent the update
  }

  return normalizedStatus
}

const getStatusFromComoduleNinebotEs4 = async (imei) => {
  const vehicleState = await comoduleApi.getVehicleState(imei)
  return {
    location: {
      latitude: vehicleState.gpsLatitude,
      longitude: vehicleState.gpsLongitude
    },
    bike: {
      bike_battery_level: vehicleState.vehicleBatteryPercentage
    },
    controller: {
      battery_level: vehicleState.moduleBatteryPercentage,
      gps_log_time: vehicleState.lastUpdate,
      fw_version: vehicleState.firmwareVersion
    }
  }
}

const updateComoduleSegwayEs4Iot = async (bike, params) => {
  let vehicleState
  try {
    vehicleState = await comoduleApi.getVehicleState(params.iot_key)
  } catch (error) {
    Sentry.captureException(error, {bike, params})
    logger(
      `Error: failed to validate IoT key with COMODULE API: ${error.message}`
    )
    throw errors.customError(
      `Failed to validate IoT key with COMODULE API, the key may be invalid`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }

  const controller = await db
    .main('controllers')
    .where({
      bike_id: bike.bike_id,
      device_type: 'iot'
    })
    .first()

  const insertedId = await db.main.transaction(async (trx) => {
    await trx('bikes')
      .where({ bike_id: bike.bike_id })
      .update({
        bike_name:
          params.bike_name || bike.bike_name || `Ninebot ES4 ${params.qr_code}`,
        qr_code_id: params.qr_code,
        current_status: bikeConstants.current_status.controllerAssigned,
        latitude: vehicleState.gpsLatitude,
        longitude: vehicleState.gpsLongitude,
        bike_battery_level: vehicleState.vehicleBatteryPercentage
      })

    if (controller) {
      const controllerUpdate = {
        key: params.iot_key,
        latitude: vehicleState.gpsLatitude,
        longitude: vehicleState.gpsLongitude,
        battery_level: vehicleState.moduleBatteryPercentage,
        gps_log_time: vehicleState.lastUpdate,
        fw_version: vehicleState.firmwareVersion
      }
      await trx('controllers')
        .where({ controller_id: controller.controller_id })
        .update(controllerUpdate)
    } else {
      const newController = {
        device_type: 'iot',
        vendor: params.iot_module_type,
        key: params.iot_key,
        make: bike.make,
        model: bike.model,
        fleet_id: bike.fleet_id,
        bike_id: bike.bike_id,
        latitude: vehicleState.gpsLatitude,
        longitude: vehicleState.gpsLongitude,
        battery_level: vehicleState.moduleBatteryPercentage,
        gps_log_time: vehicleState.lastUpdate,
        fw_version: vehicleState.firmwareVersions
      }
      const [id] = await trx('controllers').insert(newController)
      return id
    }
  })

  return db
    .main('controllers')
    .where({
      controller_id: controller ? controller.controller_id : insertedId
    })
    .first()
}

const updateActonFamilyIoT = async (bike, params, vendor) => {
  const client = actonFamily[vendor]
  if (!client) throw new Error(`ACTON family device ${vendor} not supported`)
  let vehicleState
  try {
    vehicleState = await client.getCurrentStatus(params.iot_key) // IMEI
  } catch (error) {
    Sentry.captureException(error, {bike, params, vendor})
    logger(`Error: failed to get ${vendor} vehicle status: ${error.message}`)
    logger(`Error: updateActonFamilyIoT: ${JSON.stringify(error)}`)
    throw errors.customError(
      error.message,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }

  const controller = await db
    .main('controllers')
    .where({
      bike_id: bike.bike_id,
      device_type: 'iot'
    })
    .first()

  const insertedId = await db.main.transaction(async (trx) => {
    await trx('bikes')
      .where({ bike_id: bike.bike_id })
      .update({
        bike_name:
          params.bike_name || bike.bike_name || `${vendor} ${bike.bike_id}`,
        qr_code_id: params.qr_code,
        current_status: bikeConstants.current_status.controllerAssigned,
        latitude: (vehicleState && vehicleState.lat) || 0,
        longitude: vehicleState && vehicleState.lon,
        bike_battery_level:
          (vehicleState &&
            vehicleState.extendedData &&
            vehicleState.extendedData.scooterBattery) ||
          0
      })

    if (controller) {
      const controllerUpdate = {
        key: params.iot_key,
        latitude: vehicleState.lat,
        longitude: vehicleState.lon,
        battery_level: vehicleState.iotBattery,
        gps_log_time: vehicleState.gpsTimestamp
      }
      await trx('controllers')
        .where({ controller_id: controller.controller_id })
        .update(controllerUpdate)
    } else {
      const newController = {
        device_type: 'iot',
        vendor: params.iot_module_type,
        key: params.iot_key,
        make: bike.make,
        model: bike.model,
        fleet_id: bike.fleet_id,
        bike_id: bike.bike_id,
        latitude: vehicleState.lat,
        longitude: vehicleState.lon,
        battery_level: (vehicleState && vehicleState.extendedData && vehicleState.extendedData.scooterBattery) || 0,
        gps_log_time: vehicleState.gpsTimestamp
      }
      const [id] = await trx('controllers').insert(newController)
      return id
    }
  })

  return db
    .main('controllers')
    .where({
      controller_id: controller ? controller.controller_id : insertedId
    })
    .first()
}

const updateNimbelinkConfig = async (SERIAL) => {
  const config = {
    config: {
      accelerometer: {
        enabled: true,
        measurement_period: 20,
        mode: 2,
        tracking: {
          connect_on_movement_start: true,
          connect_on_movement_end: true
        }
      },
      location: {
        enabled: true,
        sampling_period: 300
      },
      network: {
        period: 3600
      },
      temperature: {
        enabled: false
      }
    }
  }
  const newConfig = await updateDeviceConfig(config, SERIAL)
  return newConfig
}

const updateNimbelinkTracker = async (bike, params) => {
  try {
    // Update config every time a new Nimbelink is added/updated
    await updateNimbelinkConfig(params.iot_key.toLowerCase())
    const trackerState = await getLatestDataForController(
      params.iot_key.toLowerCase()
    ) // serial_number
    if (trackerState === 'No new status updates') {
      // The PubSub queus had been flushed and no new messages have been added so no need for controller or location updates
      updateBikes(
        {
          bike_name:
            params.bike_name || bike.bike_name || `Nimbelink ${params.qr_code}`,
          qr_code_id: params.qr_code,
          current_status: bikeConstants.current_status.controllerAssigned
        },
        { bike_id: bike.bike_id },
        (error) => {
          if (error) {
            Sentry.captureException(error, {bike, params})
            logger(`Error: Failed to update Nimbelink bike: ${error.message}`)
            throw errors.customError(
              `Failed to update Nimbelink bike: ${error.message}`,
              platform.responseCodes.BadRequest,
              'BadRequest',
              false
            )
          }
        }
      )
      if (params.operation === 'adding') {
        const ctrl = await db
          .main('controllers')
          .where({
            key: params.iot_key.toLowerCase()
          })
          .first()
        if (bike.bike_id === ctrl.bike_id) {
          logger(`The controller is already assigned to this bike`)
          throw errors.customError(
            `The controller is already assigned to this bike`,
            platform.responseCodes.BadRequest,
            'BadRequest',
            false
          )
        } else if (bike.bike_id === ctrl.bike_id) {
          logger(`The controller is already assigned to another bike`)
          throw errors.customError(
            `The controller is already assigned to another bike`,
            platform.responseCodes.BadRequest,
            'BadRequest',
            false
          )
        }
        const newController = _.omit(
          {
            device_type: 'tracker',
            vendor: params.iot_module_type,
            key: params.iot_key.toLowerCase(),
            make: bike.make,
            model: bike.model,
            fleet_id: bike.fleet_id,
            bike_id: bike.bike_id
          },
          (value) => !value
        )
        const insertController = query.insertSingle(
          dbConstants.tables.controllers,
          newController
        )
        sqlPool.makeQuery(insertController, (err) => {
          if (err) {
            logger(`Error: Failed to update Nimbelink tracker: ${err.message}`)
            throw errors.customError(
              `Failed to update Nimbelink tracker`,
              platform.responseCodes.BadRequest,
              'BadRequest',
              false
            )
          } else {
            const { id } = insertController
            return db.main('controllers').where({ controller_id: id }).first()
          }
        })
      }
    }
    const lastLocation =
      trackerState && trackerState.data && trackerState.data.loc.pop()
    const latitude = lastLocation && lastLocation.lat
    const longitude = lastLocation && lastLocation.lon
    const gpsLogTime = lastLocation && lastLocation.ts
    const batteryLevel =
      trackerState &&
      trackerState.data &&
      trackerState.data.deviceStatus &&
      trackerState.data.deviceStatus[0] &&
      trackerState.data.deviceStatus[0].estBattPct
    const controller = await db
      .main('controllers')
      .where({
        bike_id: bike.bike_id,
        device_type: 'tracker'
      })
      .first()

    const insertedId = await db.main.transaction(async (trx) => {
      await trx('bikes')
        .where({ bike_id: bike.bike_id })
        .update(
          _.omit({
            bike_name:
              params.bike_name ||
              bike.bike_name ||
              `Nimbelink ${params.qr_code}`,
            qr_code_id: params.qr_code,
            current_status: bikeConstants.current_status.controllerAssigned,
            latitude,
            longitude
          }),
          (value) => !value
        )

      if (controller) {
        const controllerUpdate = _.omit(
          {
            key: params.iot_key.toLowerCase(),
            latitude,
            longitude,
            battery_level: batteryLevel,
            gps_log_time: gpsLogTime
          },
          (value) => !value
        )
        await trx('controllers')
          .where({ controller_id: controller.controller_id })
          .update(controllerUpdate)
      } else {
        const newController = _.omit(
          {
            device_type: 'tracker',
            vendor: params.iot_module_type,
            key: params.iot_key.toLowerCase(),
            make: bike.make,
            model: bike.model,
            fleet_id: bike.fleet_id,
            bike_id: bike.bike_id,
            latitude,
            longitude,
            battery_level: batteryLevel,
            gps_log_time: gpsLogTime
          },
          (value) => !value
        )
        const [id] = await trx('controllers').insert(newController)
        return id
      }
    })

    return db
      .main('controllers')
      .where({
        controller_id: controller ? controller.controller_id : insertedId
      })
      .first()
  } catch (error) {
    Sentry.captureException(error)
    logger(`Error: Failed to update Nimbelink tracker: ${error.message}`)
    throw errors.customError(
      `Failed to update Nimbelink tracker: ${error.message}`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

/**
 * Used to get the data for a group of bikes
 * @param {number} fleetId
 * @param done - Callback
 */
const getScoutApiKey = async (fleetId) => {
  try {
    const scoutApiKeys = await db
      .main('integrations')
      .where('fleet_id', fleetId.fleet_id)
      .where('integration_type', 'scout')
      .select()
    if (scoutApiKeys) {
      return scoutApiKeys
    } else {
      throw errors.customError(
        `Scout keys for ${fleetId} not found.`,
        platform.responseCodes.ResourceNotFound,
        'NotFound',
        true
      )
    }
  } catch (error) {
    Sentry.captureException(error, {fleetId})
    logger(
      `Error: Cannot get credentials for scout integration : ${error.message}`
    )
    throw errors.customError(
      error.message ||
      `No scout integration available for fleet: ${error.message}`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

/**
 * Used to get the data for a geotab integration
 * @param {object} fleetId
 * @param done - Callback
 */
const getGeotabApiKey = async (fleetId) => {
  try {
    const geotabApiKeys = await db
      .main('integrations')
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
    Sentry.captureException(error, {fleetId})
    logger(
      `Error: Cannot get credentials for Geotab integration : ${error.message}`
    )
    throw errors.customError(
      error.message ||
      `No Geotab integration available for fleet: ${error.message}`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

const updateScoutIoT = async (bike, params) => {
  const user = await db
    .main('integrations')
    .where({ fleet_id: bike.fleet_id })
    .first()
  let trackerState
  try {
    // lets first get the controller_id before we can use it to make subsequent queries.
    const allScouts = await getAllUserScouts(user)
    const recursiveFilter = function (scout) {
      return scout.attrs.imeihash === params.iot_key
    }
    let targetScout = _.filter(allScouts.objects, recursiveFilter)
    let scoutId = targetScout[0]['id']
    trackerState = await getCurrentScoutStatus(user, scoutId) // device_id
  } catch (error) {
    Sentry.captureException(error, {bike, params})
    logger(
      `Error: failed to validate IoT key with Scout  IoT: ${error.message}`
    )
    throw errors.customError(
      `Failed to validate IoT key with Scout IoT, the key may be invalid`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }

  const controller = await db
    .main('controllers')
    .where({
      bike_id: bike.bike_id,
      device_type: 'tracker'
    })
    .first()

  const insertedId = await db.main.transaction(async (trx) => {
    await trx('bikes')
      .where({ bike_id: bike.bike_id })
      .update({
        bike_name: params.bike_name || bike.bike_name,
        qr_code_id: params.qr_code,
        current_status: bikeConstants.current_status.controllerAssigned,
        latitude: trackerState.objects[0].lat,
        longitude: trackerState.objects[0].lng
      })
    if (controller) {
      const controllerUpdate = {
        key: params.iot_key,
        latitude: trackerState.objects[0].lat,
        longitude: trackerState.objects[0].lng,
        battery_level: trackerState.objects[0].battery,
        gps_log_time: trackerState.objects[0].record_time
      }
      await trx('controllers')
        .where({ controller_id: controller.controller_id })
        .update(controllerUpdate)
    } else {
      const newController = {
        device_type: 'tracker',
        vendor: params.iot_module_type,
        key: params.iot_key,
        make: bike.make,
        model: bike.model,
        fleet_id: bike.fleet_id,
        bike_id: bike.bike_id,
        latitude: trackerState.objects[0].lat,
        longitude: trackerState.objects[0].lng,
        battery_level: trackerState.objects[0].battery,
        gps_log_time: trackerState.objects[0].record_time
      }
      const [id] = await trx('controllers').insert(newController)
      return id
    }
  })

  return db
    .main('controllers')
    .where({
      controller_id: controller ? controller.controller_id : insertedId
    })
    .first()
}

const updateManualLock = async (bike, params) => {
  try {
    params.key = params.iot_key
    const controller = await db.main('controllers')
      .where({
        bike_id: bike.bike_id,
        device_type: 'lock'
      })
      .first()

    const insertedId = await db.main.transaction(async trx => {
      await trx('bikes')
        .where({ bike_id: bike.bike_id })
        .update({
          bike_name: params.bike_name || bike.bike_name,
          qr_code_id: params.qr_code,
          current_status: bikeConstants.current_status.controllerAssigned
        })
      if (controller) {
        const controllerUpdate = {
          metadata: JSON.stringify({key: params.iot_key})
        }
        await trx('controllers')
          .where({ controller_id: controller.controller_id })
          .update(controllerUpdate)
      } else {
        const newController = {
          device_type: 'lock',
          vendor: params.iot_module_type,
          metadata: JSON.stringify({key: params.iot_key}),
          key: randomGenerator(),
          make: bike.make,
          model: bike.model,
          fleet_id: bike.fleet_id,
          bike_id: bike.bike_id
        }
        const [id] = await trx('controllers').insert(newController)
        return id
      }
    })

    const targetController = await db.main('controllers')
      .where({ controller_id: controller ? controller.controller_id : insertedId }).first()
    return targetController
  } catch (error) {
    Sentry.captureException(error)
    throw errors.customError(
      `Failed to update controller, ${error.message}`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      false
    )
  }
}

const updateGeotabIoT = async (bike, params) => {
  const geotabUser = await setUpGeotabApiUser(bike)
  if (!_.isEmpty(geotabUser)) {
    GeotabApi = new GeotabApiClient({
      password: geotabUser.password,
      userName: geotabUser.userName,
      sessionId: geotabUser.sessionId,
      database: geotabUser.database,
      serverName: geotabUser.serverName
    })
    params.key = params.iot_key
    params.fleet_id = bike.fleet_id
    const [currentLocation] = await GeotabApi.getCurrentLocation(params)
    const controller = await db
      .main('controllers')
      .where({
        bike_id: bike.bike_id,
        device_type: 'iot'
      })
      .first()

    const insertedId = await db.main.transaction(async (trx) => {
      await trx('bikes')
        .where({ bike_id: bike.bike_id })
        .update({
          bike_name: params.bike_name || bike.bike_name,
          qr_code_id: params.qr_code,
          current_status: bikeConstants.current_status.controllerAssigned,
          latitude: currentLocation.latitude,
          longitude: currentLocation.longitude
        })
      if (controller) {
        const controllerUpdate = {
          key: params.iot_key,
          latitude: currentLocation.latitude,
          longitude: currentLocation.longitude,
          gps_log_time: currentLocation.dateTime
        }
        await trx('controllers')
          .where({ controller_id: controller.controller_id })
          .update(controllerUpdate)
      } else {
        const newController = {
          device_type: 'iot',
          vendor: params.iot_module_type,
          key: params.iot_key,
          make: bike.make,
          model: bike.model,
          fleet_id: bike.fleet_id,
          bike_id: bike.bike_id,
          latitude: currentLocation.latitude,
          longitude: currentLocation.longitude,
          gps_log_time: currentLocation.dateTime
        }
        const [id] = await trx('controllers').insert(newController)
        return id
      }
    })

    return db
      .main('controllers')
      .where({
        controller_id: controller ? controller.controller_id : insertedId
      })
      .first()
  }
}

const updateLinkaIoT = async (bike, params) => {
  const linkaMerchant = await setUpLinkaApiClient(bike)
  if (!_.isEmpty(linkaMerchant)) {
    LinkaApi = new LinkaApiClient({
      baseUrl: linkaMerchant.baseUrl,
      merchantUrl: linkaMerchant.merchantUrl,
      apiKey: linkaMerchant.apiKey,
      secretKey: linkaMerchant.secretKey
    })

    params.key = params.iot_key
    const currentLocation = await LinkaApi.getCurrentLocation(params.key)
    let datetime = new Date(currentLocation.modifiedAt)
    datetime = datetime.toISOString().replace('Z', '').replace('T', '')
    const controller = await db
      .main('controllers')
      .where({
        bike_id: bike.bike_id,
        device_type: 'iot'
      })
      .first()

    const insertedId = await db.main.transaction(async (trx) => {
      await trx('bikes')
        .where({ bike_id: bike.bike_id })
        .update({
          bike_name: params.bike_name || bike.bike_name,
          qr_code_id: params.qr_code,
          current_status: bikeConstants.current_status.controllerAssigned,
          latitude: currentLocation.latitude,
          longitude: currentLocation.longitude
        })
      if (controller) {
        const controllerUpdate = {
          key: params.iot_key,
          latitude: currentLocation.latitude,
          longitude: currentLocation.longitude,
          gps_log_time: datetime
        }
        await trx('controllers')
          .where({ controller_id: controller.controller_id })
          .update(controllerUpdate)
      } else {
        const newController = {
          device_type: 'iot',
          vendor: params.iot_module_type,
          key: params.iot_key,
          make: bike.make,
          model: bike.model,
          fleet_id: bike.fleet_id,
          bike_id: bike.bike_id,
          latitude: currentLocation.latitude,
          longitude: currentLocation.longitude,
          gps_log_time: datetime
        }
        const [id] = await trx('controllers').insert(newController)
        return id
      }
    })

    return db
      .main('controllers')
      .where({
        controller_id: controller ? controller.controller_id : insertedId
      })
      .first()
  }
}

const getAllParameters = async (path, integration = null) => {
  if (integration === 'tapkey') {
    path = `${environment}/TAP_KEY/${path}`
  }
  try {
    const parameters = await ssmClient.getParametersByPath({
      Path: `/env/${path}`,
      Recursive: true
    }).promise()
    parameters.Parameters.push({ Name: 'environment', Value: environment })
    return parameters
  } catch (error) {
    Sentry.captureException(error)
    throw error
  }
}

const updateTapKeyIoT = async (bike, params) => {
  params.key = params.iot_key

  const controller = await db
    .main('controllers')
    .where({
      bike_id: bike.bike_id,
      device_type: 'lock'
    })
    .first()

  const insertedId = await db.main.transaction(async (trx) => {
    await trx('bikes')
      .where({ bike_id: bike.bike_id })
      .update({
        bike_name: params.bike_name || bike.bike_name,
        qr_code_id: params.qr_code,
        current_status: bikeConstants.current_status.controllerAssigned
      })
    if (controller) {
      const controllerUpdate = {
        key: params.iot_key
      }
      await trx('controllers')
        .where({ controller_id: controller.controller_id })
        .update(controllerUpdate)
    } else {
      const newController = {
        device_type: 'lock',
        vendor: params.iot_module_type,
        key: params.iot_key,
        make: bike.make,
        model: bike.model,
        fleet_id: bike.fleet_id,
        bike_id: bike.bike_id
      }
      const [id] = await trx('controllers').insert(newController)
      return id
    }
  })
  const lock = await db
    .main('controllers')
    .where({
      controller_id: controller ? controller.controller_id : insertedId
    })
    .first()
  const [tapKeyDetails] = await getTapkeyLockId('id', lock.key, lock.fleet_id)
  lock.key = tapKeyDetails.title
  return lock
}
const updateDucktIoT = async (bike, params) => {
  const ducktUser = await setUpApiUser(bike, 'duckt')

  if (!_.isEmpty(ducktUser)) {
    const { Parameter: baseURL } = await getParameterStoreValue('DUCKT/URLS/BASE_URL')
    const { Parameter: callbackUrl } = await getParameterStoreValue('DUCKT/URLS/CALL_BACK_URL')
    DucktApi = new DucktApiClient({
      baseUrl: baseURL.Value,
      password: ducktUser.password,
      userName: ducktUser.userName,
      callbackUrl: callbackUrl.Value
    })

    params.key = params.iot_key
    if (bike.type === 'regular') {
      bike.vehicleType = 'BICYCLE'
    } else {
      bike.vehicleType = 'SCOOTER'
    }
    await DucktApi.addAdapter(params.key, bike)
    const controller = await db
      .main('controllers')
      .where({
        bike_id: bike.bike_id,
        device_type: 'adapter'
      })
      .first()

    const insertedId = await db.main.transaction(async (trx) => {
      await trx('bikes')
        .where({ bike_id: bike.bike_id })
        .update({
          bike_name: params.bike_name || bike.bike_name,
          qr_code_id: params.qr_code,
          current_status: bikeConstants.current_status.controllerAssigned
        })
      if (controller) {
        const controllerUpdate = {
          key: params.iot_key
        }
        await trx('controllers')
          .where({ controller_id: controller.controller_id })
          .update(controllerUpdate)
      } else {
        const newController = {
          device_type: 'adapter',
          vendor: params.iot_module_type,
          key: params.iot_key,
          make: bike.make,
          model: bike.model,
          fleet_id: bike.fleet_id,
          bike_id: bike.bike_id,
          status: 'active'
        }
        const [id] = await trx('controllers').insert(newController)
        return id
      }
    })

    return db
      .main('controllers')
      .where({
        controller_id: controller ? controller.controller_id : insertedId
      })
      .first()
  }
}

const getFleetIntegrations = async (fleetId) => {
  try {
    const integrations = await db
      .main('integrations')
      .where({ fleet_id: fleetId })
      .select(
        'integrations_id',
        'fleet_id',
        'email',
        'integration_type',
        'metadata'
      )
    return integrations.map((d) => {
      if (d.metadata) return { ...d, metadata: JSON.parse(d.metadata) }
      return { ...d, metadata: null }
    })
  } catch (error) {
    Sentry.captureException(error)
    logger(
      `Error: failed to retrieve fleet integrations: ${error.message}`
    )
    throw errors.customError(
      `Failed to retrieve fleet integrations`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

const getLinkaCommandStatus = async (commandId) => {
  try {
    return await LinkaApi.getCurrentStatus(commandId)
  } catch (error) {
    Sentry.captureException(error, {commandId})
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

const getBikeTickets = async (bikeId) => {
  try {
    let tickets = await db.main('tickets').where({ bike_id: bikeId })
    const operatorIds = [...new Set(tickets.map(ticket => ticket.operator_id))].filter(opId => opId)
    let operators = await db.users('operators').whereIn('operator_id', operatorIds).select('operator_id', 'email', db.users.raw('CONCAT(first_name, \' \', last_name) as "displayName"'))
    const operatorsMap = {}
    for (let op of operators) {
      operatorsMap[op.operator_id] = op
    }
    tickets = tickets.map(ticket => {
      let ticketCopy = { ...ticket }
      if (ticket.operator_id) ticketCopy = { ...ticketCopy, operator: operatorsMap[`${ticket.operator_id}`] }
      return ticketCopy
    })
    return tickets
  } catch (error) {
    Sentry.captureException(error, {bikeId})
    logger('An error occurred getting tickets and operators')
  }
}

const deleteFleetDomains = async (fleetDetails) => {
  try {
    const deleteResponse = await db.main('domains').delete()
      .where({fleet_id: fleetDetails.fleetId})
    return deleteResponse
  } catch (error) {
    Sentry.captureException(error, {fleetDetails})
    throw errors.customError(
      `An error occurred while revoking fleet users access`,
      platform.responseCodes.ResourceNotFound,
      'InternalServerError',
      false
    )
  }
}

const updateBikeImage = async (bikeDetails, uploadedImageDetails) => {
  bikeDetails.path = uploadedImageDetails.path
  try {
    const { link } = await uploadFile.uploadImageFile(bikeDetails,
      uploadConstants.types.bike +
      '-' +
      bikeDetails.fleetId +
      '-' +
      passwordHandler.randString(10),
      platformConfig.aws.s3.bikeImages.bucket,
      uploadedImageDetails.mimetype)
    const targetGroup = await db.main('bikes').where({bike_id: bikeDetails.bikeId, fleet_id: bikeDetails.fleetId}).first()
    await db.main('bike_group').update({ pic: link }).where({bike_group_id: targetGroup.bike_group_id})
    return {link: link}
  } catch (error) {
    Sentry.captureException(error)
    throw errors.customError(
      `An error occurred while updating image`,
      platform.responseCodes.ResourceNotFound,
      'InternalServerError',
      false
    )
  }
}
const updateBikeDescription = async (bikeDetails) => {
  try {
    const targetGroup = await db.main('bikes').where({bike_id: bikeDetails.bikeId}).first()
    await db.main('bike_group').update({ description: bikeDetails.description }).where({bike_group_id: targetGroup.bike_group_id})
  } catch (error) {
    Sentry.captureException(error)
    throw errors.customError(
      `An error occurred while updating bike description`,
      platform.responseCodes.ResourceNotFound,
      'InternalServerError',
      false
    )
  }
}

const deleteController = async (controller) => {
  const controllerType = ['iot', 'tracker', 'adapter'].includes(controller.device_type) ? 'smart' : 'lock'
  try {
    const bike = await db.main('bikes').where({bike_id: controller.bike_id}).first()
    // check so we don't delete the controller of an active bike
    if (bike.status === 'active') {
      throw errors.customError(
        'Cannot update vehicle while it is live, move to staging and then try again.',
        platform.responseCodes.Forbidden,
        'Forbidden',
        false
      )
    }

    // check the number of controllers that bike is assigned to.
    const controllers = await db.main('controllers').where({bike_id: controller.bike_id})
    await db.main.transaction(async (trx) => {
      // if bike had only one controller we need to set it to lock_not_assigned
      if (controllers && controllers.length <= 1) {
        await trx('bikes').update({current_status: 'lock_not_assigned'}).where({bike_id: controller.bike_id})
      }
      if (!controllers.length && controller.lock_id) {
        await trx('bikes').update({current_status: 'lock_not_assigned', lock_id: null}).where({bike_id: bike.bike_id})
        await trx('locks').delete().where({lock_id: controller.lock_id})
      }
      if (controllers.length && controller.lock_id) {
        await trx('locks').delete().where({lock_id: controller.lock_id})
      }
      if (controllerType === 'smart') {
        await trx('controllers').delete().where({key: controller.key})
      }
      if (controllerType === 'lock') {
        // Manual lock, and Sas are registered as a controllers
        if (['Manual Lock', 'Sas'].includes(controller.vendor)) {
          await trx('controllers').delete().where({controller_id: controller.controller_id})
        }
      }
    })

    const remainingControllers = await db.main('controllers').where({bike_id: controller.bike_id})
    // TODO: We need to reset the iot_module_type column in bike_group, the catch is that a bike_group_id
    //  might be shared by more than one bike (because we can add bikes in bulk)
    return {controllers: remainingControllers}
  } catch (error) {
    Sentry.captureException(error, {controller})
    logger(
      `Error: An error occurred deleting controller: ${error.message}`
    )
    throw errors.customError(
      `Failed to delete controller, ${error.message}`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      false
    )
  }
}

const getTapkeyLockId = async (filterEntity, filter, fleetId) => {
  try {
    const { Parameters: tapkeyUrls } = await getParameterStoreValuesByPath('TAP_KEY/URLS')
    const { Parameters: tapkeyKeys } = await getParameterStoreValuesByPath('TAP_KEY/KEYS')
    const urlName = `/env/${environment}/TAP_KEY/URLS`
    const keyName = `/env/${environment}/TAP_KEY/KEYS`

    const passPhrase = tapkeyKeys.find(key => key.Name === `${keyName}/TAP_KEY_PASSPHRASE`)
    const tokenIssuer = tapkeyKeys.find(key => key.Name === `${keyName}/TAP_KEY_TOKEN_ISSUER`)
    const idpClientId = tapkeyKeys.find(url => url.Name === `${keyName}/IDP_CLIENT_ID`)
    const provider = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_ID_PROVIDER`)
    const ownerAccountId = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_OWNER_ACCOUNT_ID`)
    const authCodeClientId = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_AUTH_CODE_CLIENT_ID`)
    const authCodeClientSecret = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_AUTH_CODE_CLIENT_SECRET`)
    const tapKeyPrivateKey = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_PRIVATE_KEY`)

    const grantType = tapkeyUrls.find(url => url.Name === `${urlName}/TAP_KEY_GRANT_TYPE`)
    const loginUrl = tapkeyUrls.find(url => url.Name === `${urlName}/LOGIN_URL`)
    const baseUrl = tapkeyUrls.find(url => url.Name === `${urlName}/BASE_URL`)
    const redirectUri = tapkeyUrls.find(url => url.Name === `${urlName}/REDIRECT_URI`)

    const integration = await db.main('integrations').where({ fleet_id: fleetId, integration_type: 'tapkey' }).select('metadata').first()
    let metadata = integration && integration.metadata
    if (metadata) {
      metadata = JSON.parse(metadata)
    } else {
      throw new Error('Important info is missing to setup Tapkey connection')
    }

    const tapKeyApiClient = new TapKeyApiClient({
      idpClientId: idpClientId.Value,
      grantType: grantType.Value,
      provider: provider.Value,
      loginUrl: loginUrl.Value,
      passPhrase: passPhrase.Value,
      tokenIssuer: tokenIssuer.Value,
      refreshToken: metadata.refreshToken,
      accessToken: metadata.accessToken,
      baseUrl: baseUrl.Value,
      ownerAccountId: ownerAccountId.Value,
      authCodeClientId: authCodeClientId.Value,
      authCodeClientSecret: authCodeClientSecret.Value,
      tapKeyPrivateKey: tapKeyPrivateKey.Value,
      redirectUri: redirectUri.Value
    })
    return await tapKeyApiClient.getLocksByFilter(filterEntity, filter)
  } catch (error) {
    Sentry.captureException(error, {filterEntity, filter, fleetId})
    logger(
      `Error: failed to get tapkey lock: ${error.message}`
    )
    throw errors.customError(
      `Failed to fetch lock`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

const getFleetData = async (fleetDetails) => {
  try {
    const [fleet] = await db.main('fleets').where({fleet_id: fleetDetails.fleetId})
    return fleet
  } catch (error) {
    Sentry.captureException(error, {fleetDetails})
    throw errors.customError(
      `Failed to retrieve fleet data`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

const getBikeDetails = async (bikeId) => {
  const bike = await db.main('bikes').where({ 'bikes.bike_id': +bikeId })
    .leftJoin('controllers', 'controllers.bike_id', 'bikes.bike_id')
    .leftJoin('fleets', 'fleets.fleet_id', 'bikes.fleet_id')
    .leftJoin('qr_codes', 'qr_codes.equipment_id', 'bikes.bike_id')
    .leftJoin('trips', 'trips.trip_id', 'bikes.current_trip_id')
    .options({ nestTables: true })
    .first()

  if (bike.trips) {
    if (bike.trips.trip_id) {
      bike.currentTrip = bike.trips
    }
    delete bike.trips
  }
  if (bike.current_user_id && bike.last_user_id) {
    const user = await db.users('users').where({ user_id: bike.current_user_id }).first()
    bike.currentUser = user
    bike.lastUser = user
  } else if (!bike.current_user_id && bike.last_user_id) {
    const user = await db.users('users').where({ user_id: bike.last_user_id }).first()
    bike.currentUser = null
    bike.lastUser = user
  }

  let bikeInfo = {}
  if (bike) {
    if (bike.bikes) {
      bikeInfo = bike.bikes
      if (bikeInfo.bike_uuid) {
        const isDocked = await db.main('ports').where({ vehicle_uuid: bikeInfo.bike_uuid }).first()
        if (isDocked) bikeInfo.isDocked = isDocked.uuid
      }
      if (bikeInfo.lock_id) {
        const lock = await db.main('locks').where({lock_id: bikeInfo.lock_id}).first()
        if (lock) bikeInfo.lock = lock
      }
    }
    if (bike.controllers) {
      bikeInfo.controllerData = bike.controllers
    }
    if (bike.fleets) {
      bikeInfo.fleetData = bike.fleets
    }
    if (bike.qr_codes) {
      bikeInfo.qrCodeData = bike.qr_codes
    }
  }
  return bikeInfo
}

module.exports = Object.assign(module.exports, {
  validateNewBikes: validateNewBikes,
  getElectricBikes: getElectricBikes,
  getElectricBikeTypes: getElectricBikeTypes,
  getBikeFleetData: getBikeFleetData,
  getBikesForFleet: getBikesForFleet,
  getBikesForCustomer: getBikesForCustomer,
  getBikesForOperator: getBikesForOperator,
  getMaintenanceRecordsForFleet: getMaintenanceRecordsForFleet,
  getMaintenanceRecordsForCustomer: getMaintenanceRecordsForCustomer,
  getMaintenanceRecordsForOperator: getMaintenanceRecordsForOperator,
  getMaintenanceCategories: getMaintenanceCategories,
  sendToService: sendToService,
  returnToActiveFleet: returnToActiveFleet,
  insertBikeDetails: insertBikeDetails,
  getBikeImageAndDescription: getBikeImageAndDescription,
  cumulativeTripStatistics: cumulativeTripStatistics,
  sendToArchive: sendToArchive,
  sendToStaging: sendToStaging,
  updateBike: updateBike,
  updateMaintenanceSchedule: updateMaintenanceSchedule,
  getBikeGroupData: getBikeGroupData,
  updateMaintenance: updateMaintenance,
  insertMaintenance: insertMaintenance,
  getScoutApiKey,
  getGeotabApiKey,
  updateBikeDetails,
  activityLog: activityLog,
  getBikes: getBikes,
  updateIot,
  lockOrUnlockBike,
  openCover,
  updateGrowFleetSettings,
  getFleetIntegrations,
  updateSegwayFleetSettings,
  setBikeLights,
  getLinkaCommandStatus,
  updateACTONFleetSettings,
  updateBikesInGPSService,
  getAndApplyGrowSettings,
  getAllParameters,
  getBikeTickets,
  getTapkeyLockId,
  saveIntegrationSettings,
  deleteFleetDomains,
  getFleetData,
  deleteController,
  updateKisiLockStatus,
  updateBikeImage,
  updateBikeDescription,
  updateSegwayBikeStatus,
  segwayApiClientForUs,
  segwayApiClientForEu,
  getBikeDetails,
  setUpApiUser
})
