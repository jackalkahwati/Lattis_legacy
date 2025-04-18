'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const query = platform.queryCreator
const sqlPool = platform.sqlPool
const moment = require('moment')
const errors = platform.errors
const S3Handler = platform.S3Handler
const config = platform.config
const encryptionHandler = platform.encryptionHandler
const responseCodes = platform.responseCodes
const operatorHandler = require('./operator-handler')
const fleetHandler = require('./fleet-handler')
const tripHandler = require('./../model_handlers/trip-handler')
const dbConstants = platform.dbConstants
const fleetConstants = require('./../constants/fleet-constants')
const operatorConstants = require('./../constants/operator-constants')
const _ = require('underscore')
const passwordHandler = require('./../utils/password-handler')
const localConfig = require('./../config')
const uploadConstants = require('./../constants/upload-constants')
const db = require('../db')
const { sendCreateGeofenceRequestToGPSService } = require('../utils/gpsService')
const Sentry = require('../utils/configureSentry')
const uploadFile = require('../utils/file-upload')

/**
 *   To get customerList for an operator
 *
 * @param {Number} fleetDetails - contains operator id and customer id
 * @param {Function} done - Callback function with error, data params
 */
const getCustomerList = (fleetDetails, done) => {
  const getCustomersQuery = query.joinWithAnd(
    dbConstants.tables.fleets,
    dbConstants.tables.customers,
    null,
    'customer_id',
    'customer_id',
    [fleetDetails.customer_id]
  )
  sqlPool.makeQuery(getCustomersQuery, (error, customersList) => {
    if (error) {
      Sentry.captureException(error, {getCustomersQuery})
      logger(error)
      done(error, null)
      return
    }
    _.each(customersList, (customer, index) => {
      customersList[index].country_code = parseInt(customer.country_code)
      customersList[index].phone_number = parseInt(customer.phone_number)
    })

    done(null, customersList)
  })
}

/**
 * This methods validates a new fleet object.
 *
 * @param {Object} customerData
 * @param {FileReader} customerImageData
 * @param {String} type
 * @returns {boolean}
 */
const validateNewFleet = (customerData, customerImageData, type) => {
  const isValid = (
    (_.has(customerData, 'operator_id') || _.has(customerData, 'customer_id') || _.has(customerData, 'fleet_id')) &&
    _.has(customerData, 'fleet_name') &&
    _.has(customerData, 'email') &&
    _.has(customerData, 'first_name') &&
    _.has(customerData, 'last_name') &&
    _.has(customerData, 'country_code') &&
    _.has(customerData, 'fleet_timezone')
  )
  if (type === 'existing_customer') {
    return isValid && (
      _.has(customerData, 'image_url')) && (_.has(customerData, 'city')) &&
      _.has(customerData, 'type')
  } else if (type === 'new_customer') {
    return isValid && (
      customerImageData ||
      _.has(customerData, 'image_url')) && _.has(customerData, 'city') && _.has(customerData, 'type')
  } else if (type === 'edit_fleet_info') {
    return isValid && (_.has(customerData, 'customer_name') && _.has(customerData, 'phone_number'))
  }
}

/**
 * This method inserts a customer for an operator
 *
 * @param {Object} columnsAndValues - Customer details to add
 * @param {Function} done - Callback function with error, data params
 */
const insertCustomer = (columnsAndValues, done) => {
  const insertCustomerQuery = query.insertSingle(dbConstants.tables.customers, columnsAndValues)
  sqlPool.makeQuery(insertCustomerQuery, done)
}

/**
 * This method inserts a fleet for an operator
 *
 * @param {Object} columnsAndValues - fleet Details to add
 * @param {Function} done - Callback funtion with error, data params
 * @private
 */
const _insertFleet = (columnsAndValues, done) => {
  const insertFleetsQuery = query.insertSingle(dbConstants.tables.fleets, columnsAndValues)
  sqlPool.makeQuery(insertFleetsQuery, done)
}

/**
 * This method inserts an address according to the type
 * @param {Object} columnsAndValues - Identifier to insert the datas inside the table
 * @param {Function} done
 * @private
 */
const _insertAddress = (columnsAndValues, done) => {
  const insertAddressQuery = query.insertSingle(dbConstants.tables.addresses, columnsAndValues)
  sqlPool.makeQuery(insertAddressQuery, done)
}

/*
 * Update the details of Address
 *
 * @param {Object} columnsToUpdate
 * @param {Object} comparisonColumnsAndValues
 * @param {Function} Callback with params {error, rows}
 */
const _updateAddress = (columnsAndValues, comparisonColumn, done) => {
  const updatePreferencesQuery = query.updateSingle(dbConstants.tables.addresses, columnsAndValues, comparisonColumn)
  sqlPool.makeQuery(updatePreferencesQuery, done)
}

/**
 * This method updates the address according of that type
 * @param {Object} columnsAndValues - Identifier to insert the datas inside the table
 * @param {Object} filterParam - Identifier to filter the data and update
 * @param {Function} done
 * @private
 */
const _updateFleet = (columnsAndValues, filterParam, done) => {
  const updateFleetsQuery = query.updateSingle(dbConstants.tables.fleets, columnsAndValues, filterParam)
  sqlPool.makeQuery(updateFleetsQuery, done)
}

const updateFleetProfile = (columnsAndValues, filterParam, done) => {
  const updateFleetsQuery = query.updateSingle(dbConstants.tables.fleet_payment_settings, columnsAndValues, filterParam)
  sqlPool.makeQuery(updateFleetsQuery, done)
}

/**
 * This method inserts an address according to the type
 * @param {Object} columnsAndValues - Identifier to insert the datas inside the table
 * @param {Function} done
 * @private
 */
const _insertFleetProfile = (columnsAndValues, done) => {
  const insertAddressQuery = query.insertSingle(dbConstants.tables.fleet_payment_settings, columnsAndValues)
  sqlPool.makeQuery(insertAddressQuery, done)
}

/**
 * Query to get all Trips list from trip table
 *
 * @param comparisonColumns - column to filter the trip
 * @param {Function} done - Callback with params {error, trips}
 */
const getFleetPaymentSettings = (comparisonColumns, done) => {
  const getFleetPaymentSettingsQuery = query.selectWithAnd(dbConstants.tables.fleet_payment_settings, null, comparisonColumns)
  sqlPool.makeQuery(getFleetPaymentSettingsQuery, done)
}

/*
 *
 * Used to checkFleetName whether the fleetName repeats in addFleet page.
 * @param {Function} Callback with params {error, fleets}
 */
const checkFleetName = (fleetName, done) => {
  const checkCustomerNameQuery = query.selectWithAnd(dbConstants.tables.fleets, null, { fleet_name: fleetName })
  sqlPool.makeQuery(checkCustomerNameQuery, (error, fleets) => {
    if (error) {
      Sentry.captureException(error, {fleetName})
      done(error, null)
      return
    }

    done(null, fleets)
  })
}

/*
 *
 * Used to upload multiple files with their file types
 * @param {Function} Callback with params {error, fleets}
 */
const fileUpload = (fileDetails, done) => {
  const s3Handler = new S3Handler()
  const fileLocations = []
  let bucket
  let file
  let fileName
  const filesLength = fileDetails.length
  for (let i = 0; i < filesLength; i++) {
    file = fileDetails[i]
    fileName = fileDetails[i].file_name ? fileDetails[i].file_name : file.file.originalFilename
    if (file.category === 'contract') {
      bucket = config.aws.s3.contracts.bucket
    } else {
      bucket = config.aws.s3.fleetTAndC.bucket
    }
    s3Handler.upload(file.file, fileName, bucket, file.file_type, (error, imageData) => {
      if (error) {
        Sentry.captureException(error, {fileName})
        logger('Error: uploading file', file.file, 'with type', file.file_type, 'to s3', errors.errorWithMessage(error))
        done(errors.internalServer(true), null)
        return
      }
      fileLocations.push({ file: file.file, file_location: imageData.Location })
      if (i === filesLength - 1) {
        done(errors.noError(), fileLocations)
      }
    })
  }
}

/*
 * Used to get fleet settings
 *
 * @param {Object} fleetDetails
 * @param {Function} Callback
 */
const getFleetSettings = (fleetDetails, done) => {
  const fleetMetadataQuery = query.selectWithAnd(dbConstants.tables.fleet_metadata, null, { fleet_id: fleetDetails.fleet_id })
  sqlPool.makeQuery(fleetMetadataQuery, (error, fleetMetadata) => {
    if (error) {
      Sentry.captureException(error, {fleetDetails})
      logger('Error: getting fleet settings with:', fleetDetails.fleet_id,
        'Failed to get fleet metadata', errors.errorWithMessage(error))
      done(errors.internalServer(false), null)
      return
    }
    let payload = []
    if (fleetMetadata.length > 0) {
      payload = Object.assign(fleetMetadata[0], {
        default_bike_booking_interval: config.bikeBookingExpiration,
        bike_booking_min_bound: localConfig.bike_booking_min_bound,
        bike_booking_max_bound: localConfig.bike_booking_max_bound
      })
      done(errors.noError(), payload)
    } else {
      done(errors.noError(), payload)
    }
  })
}

/*
 * Used to update fleet settings
 *
 * @param {Object} fleetDetails - Parameters to be updated
 * @param {Function} Callback
 */
const updateFleetSettings = (fleetDetails, done) => {
  const fleetMetadataQuery = query.selectWithAnd(dbConstants.tables.fleet_metadata, null, { fleet_id: fleetDetails.fleet_id })
  sqlPool.makeQuery(fleetMetadataQuery, (error, fleetMetaData) => {
    if (error) {
      Sentry.captureException(error, {fleetDetails})
      logger('Error: getting fleet metadata with:', fleetDetails.fleet_id, errors.errorWithMessage(error))
      done(errors.internalServer(false), null)
      return
    }
    if (fleetMetaData.length > 0) {
      fleetDetails.do_not_track_trip = !fleetDetails.do_not_track_trip
      const updateFleetMetadataSettingsQuery = query.updateSingle(dbConstants.tables.fleet_metadata,
        _.pick(fleetDetails, 'skip_parking_image', 'max_trip_length', 'bike_booking_interval',
          'fleet_timezone', 'bike_available_from', 'bike_available_till', 'do_not_track_trip', 'email_on_start_trip', 'require_phone_number', 'smart_docking_stations_enabled'),
        { fleet_id: fleetDetails.fleet_id })

      sqlPool.makeQuery(updateFleetMetadataSettingsQuery, (error) => {
        if (error) {
          Sentry.captureException(error, {fleetDetails})
          logger('Error: updating fleet settings with:', fleetDetails.fleet_id, ': ', errors.errorWithMessage(error))
          done(errors.internalServer(false), null)
          return
        }

        const fleetSettings = _.pick(fleetDetails, 'parking_area_restriction', 'skip_parking_image')
        if (fleetSettings.hasOwnProperty('parking_area_restriction')) {
          fleetSettings.parking_spot_restriction = fleetDetails.parking_area_restriction
          // TODO: Confirm with @Kimo Brian why these two lines were necessary
          // delete fleetSettings.parking_area_restriction
        }

        if (!_.isEmpty(fleetSettings)) {
          const updateFleetSettingsQuery = query.updateSingle(
            dbConstants.tables.fleets,
            fleetSettings,
            { fleet_id: fleetDetails.fleet_id }
          )

          sqlPool.makeQuery(updateFleetSettingsQuery, error => {
            if (error) {
              Sentry.captureException(error, {fleetDetails, updateFleetSettingsQuery})
              logger('Error: updating fleet settings with:', fleetDetails.fleet_id, ': ', errors.errorWithMessage(error))
              done(errors.internalServer(false), null)
            } else {
              done(null, null)
            }
          })
        } else {
          done(null, null)
        }
      })
    } else {
      insertFleetMetaData(fleetDetails, done)
    }
  })
}

const updateFleetLogo = async (fleetDetails, uploadedImageDetails) => {
  try {
    fleetDetails.path = uploadedImageDetails.path
    const bucket = config.aws.s3.fleetLogos.bucket
    const { link } = await uploadFile.uploadImageFile(fleetDetails,
      uploadConstants.types.bike +
      '-' +
      fleetDetails.fleetId +
      '-' +
      passwordHandler.randString(10),
      bucket,
      uploadedImageDetails.mimetype)
    await db.main('fleets').update({ logo: link }).where({fleet_id: fleetDetails.fleetId})
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

/*
 * Used to update fleet settings
 *
 * @param {Object} fleetDetails - Parameters to be updated
 * @param {Function} Callback
 */
const updateFleetPaymentSettings = (fleetDetails, done) => {
  const fleetPaymentQuery = query.selectWithAnd(dbConstants.tables.fleet_payment_settings, null, { fleet_id: fleetDetails.fleet_id })
  sqlPool.makeQuery(fleetPaymentQuery, (error, fleetPayment) => {
    if (error) {
      Sentry.captureException(error, {fleetDetails})
      logger('Error: getting fleet metadata with:', fleetDetails.fleet_id, errors.errorWithMessage(error))
      done(errors.internalServer(false), null)
      return
    }
    if (fleetPayment.length > 0) {
      const updateFleetSettingsQuery = query.updateSingle(dbConstants.tables.fleet_payment_settings,
        _.pick(fleetDetails, 'currency'),
        { fleet_id: fleetDetails.fleet_id })
      sqlPool.makeQuery(updateFleetSettingsQuery, (error) => {
        if (error) {
          Sentry.captureException(error, {fleetDetails, updateFleetSettingsQuery})
          logger('Error: updating fleet settings with:', fleetDetails.fleet_id, ': ', errors.errorWithMessage(error))
          done(errors.internalServer(false), null)
          return
        }
        done(null, null)
      })
    } else {
      _insertFleetProfile(_.pick(fleetDetails, 'fleet_id', 'currency'), done)
    }
  })
}

const insertFleetMetaData = (fleetDetails, done) => {
  const fleetMetaQuery = query.insertSingle(dbConstants.tables.fleet_metadata,
    _.pick(fleetDetails, 'fleet_id', 'skip_parking_image', 'max_trip_length', 'bike_booking_interval',
      'fleet_timezone', 'bike_available_from', 'bike_available_till', 'do_not_track_trip'))
  sqlPool.makeQuery(fleetMetaQuery, (error) => {
    if (error) {
      Sentry.captureException(error, {fleetDetails, fleetMetaQuery})
      logger('Error: inserting fleet metadata with:', fleetDetails.fleet_id, errors.errorWithMessage(error))
      done(errors.internalServer(false))
      return
    }
    done(null, null)
  })
}

/*
 *
 * Used to checkCustomerName whether the customerName repeats in addFleet page.
 * @param {customerDetails} requestData - object to filter the columns and values
 * @param {Function} Callback with params {error, customers}
 */
const checkCustomerName = (customerDetails, done) => {
  const checkCustomerNameQuery = query.selectWithAnd(dbConstants.tables.customers, null, {
    customer_name: customerDetails.customer_name
  })
  sqlPool.makeQuery(checkCustomerNameQuery, (error, customers) => {
    if (error) {
      Sentry.captureException(error, {customerDetails, checkCustomerNameQuery})
      logger('Error: getting customer_name for customers with:', customerDetails.customer_name, ': ', error)
      done(errors.internalServer(true), null)
      return
    }
    done(errors.noError(), customers)
  })
}

/**
 * Queries the database for Fleets and customers for the given input parameters.
 *
 * @param {object} requestData - object to filter the columns and values
 * @param done
 * @private
 */
const getFleetsWithCustomers = (requestData, done) => {
  operatorHandler.getOperator(null, { operator_id: requestData.operator_id }, (error, operator) => {
    if (error) {
      Sentry.captureException(error, {requestData})
      logger('Error: getting fleets with customers. Failed to get operator', requestData.operator_id, 'with error:', error)
      done(errors.internalServer(false), null)
      return
    }
    if (operator[0].acl === operatorConstants.acl.superAdmin || operator[0].acl === operatorConstants.acl.supportAdmin) {
      _getAllFleetsWithCustomers((error, fleets) => {
        if (error) {
          Sentry.captureException(error, {requestData})
          logger('Error: getting fleets with customers. Failed to get all the fleets with customers for operator',
            requestData.operator_id, 'with error:', error)
          done(errors.internalServer(false), null)
          return
        }
        done(null, fleets)
      })
    } else {
      getFleetAssociations({ operator_id: requestData.operator_id }, (error, fleetAssociations) => {
        if (error) {
          Sentry.captureException(error, {requestData, operator_id: requestData.operator_id})
          logger('Error: getting fleets with customers. Failed to get fleet associations for operator',
            requestData.operator_id, 'with error:', error)
          done(errors.internalServer(false), null)
          return
        }
        if (fleetAssociations.length === 0) {
          done(errors.noError(), fleetAssociations)
          return
        }
        _getFleets({ fleet_id: _.uniq(_.pluck(fleetAssociations, 'fleet_id')) }, (error, fleets) => {
          if (error) {
            Sentry.captureException(error, {requestData, fleet_id: _.uniq(_.pluck(fleetAssociations, 'fleet_id'))})
            logger('Error: getting fleets with customers. Failed to get fleets with',
              _.uniq(_.pluck(fleetAssociations, 'fleet_id')), 'with error:', error)
            done(errors.internalServer(false), null)
            return
          }
          const fleetsLength = fleets.length
          if (fleetsLength === 0) {
            done(errors.noError(), fleets)
            return
          }
          _getCustomers({ customer_id: _.uniq(_.pluck(fleets, 'customer_id')) }, (error, customers) => {
            if (error) {
              Sentry.captureException(error, {requestData, customer_id: _.uniq(_.pluck(fleets, 'customer_id'))})
              logger('Error getting customers with customer_id', _.uniq(_.pluck(fleets, 'customer_id')), error)
              done(error, null)
            }
            let fleet
            let customer
            for (let i = 0; i < fleetsLength; i++) {
              fleet = fleets[i]
              customer = _.findWhere(customers, { customer_id: fleet.customer_id })
              if (customer) {
                fleets[i] = Object.assign(fleet, { contact_country_code: fleet.country_code }, customer)
              }
            }
            done(null, fleets)
          })
        })
      })
    }
  })
}

/*
 * Used to get Fleet Statistics to display on the dashboard page.
 *
 * @param {Function} Callback with params {error, fleets}
 */
const getFleetStatistics = (requestParam, done) => {
  const fleetStats = {}
  if (_.has(requestParam, 'operator_id')) {
    tripHandler.getTrips(_.pick(requestParam, 'operator_id'), (error, tripsData) => {
      if (error) {
        Sentry.captureException(error, {requestParam})
        logger('Error: Failed to get fleet statistics. Unable to get trips with operator: ', requestParam.operator_id)
        return done(error, null)
      }
      fleetStats.trips_data = tripsData
      done(null, fleetStats)
    })
  } else if (_.has(requestParam, 'customer_id')) {
    tripHandler.getTrips(_.pick(requestParam, 'customer_id'), (error, tripsData) => {
      if (error) {
        Sentry.captureException(error, {requestParam})
        logger('Error: Failed to get fleet statistics. Unable to get trips with operator: ', requestParam.operator_id)
        return done(error, null)
      }
      fleetStats.trips_data = tripsData
      done(null, fleetStats)
    })
  } else {
    tripHandler.getTrips(_.pick(requestParam, 'fleet_id'), (error, tripsData) => {
      if (error) {
        Sentry.captureException(error, {requestParam})
        logger('Error: Failed to get fleet statistics. Unable to get trips with operator: ', requestParam.operator_id)
        return done(error, null)
      }
      fleetStats.trips_data = tripsData
      done(null, fleetStats)
    })
  }
}

/**
 * Query to get customer list from customer table
 *
 * @param comparisonColumns - column to filter the customer
 * @param {Function} done - Callback with params {error, customers}
 */
const _getCustomers = (comparisonColumns, done) => {
  const getCustomersQuery = query.selectWithAnd('customers', null, comparisonColumns)
  sqlPool.makeQuery(getCustomersQuery, done)
}

/**
 * Query to get join and get all fleets with customers
 *
 * @param {Function} done - Callback with params {error, customers}
 */
const _getAllFleetsWithCustomers = (done) => {
  const getAllFleetsWithCustomersQuery = query.join('fleets', 'customers', [], 'customer_id', null, null)
  sqlPool.makeQuery(getAllFleetsWithCustomersQuery, done)
}

/**
 * Used to get all the fleet and customer details for the given operator.
 *
 * @param {Object} operatorId - operator identifier
 * @param {function} done - Callback function
 * @Private
 */
const getOperatorData = (operatorId, done) => {
  const payload = {
    fleet_data: [],
    customer_data: []
  }
  operatorHandler.getOperator(null, { operator_id: operatorId }, (error, operator) => {
    if (error) {
      Sentry.captureException(error, {operatorId})
      logger('Error getting operator.', errors.errorWithMessage(error))
      done(errors.internalServer(false), null)
      return
    }
    let comparisonColumn = null
    if ((operator[0].acl !== operatorConstants.acl.superAdmin) && (operator[0].acl !== operatorConstants.acl.supportAdmin)) {
      comparisonColumn = { operator_id: operatorId }
    }
    getFleetAssociations(comparisonColumn, (error, fleetAssociations) => {
      if (error) {
        Sentry.captureException(error, {operatorId})
        logger('Error: getting fleet associations', comparisonColumn, ': ', error)
        done(errors.internalServer(false), null)
        return
      }
      if (fleetAssociations.length === 0) {
        _getCustomers({ customer_id: operator[0].customer_id }, (error, customers) => {
          if (error) {
            Sentry.captureException(error, {operatorId})
            logger('Error: getting customers with', operator[0].customer_id + ': ', error)
            done(errors.internalServer(false), null)
            return
          }
          payload.customer_data = customers
          done(null, payload)
        })
      } else {
        getFleetsWithPayment({ fleet_id: _.uniq(_.pluck(fleetAssociations, 'fleet_id')) }, (error, fleets) => {
          if (error) {
            Sentry.captureException(error, {operatorId, fleet_id: _.uniq(_.pluck(fleetAssociations, 'fleet_id'))})
            logger('Error: getting fleets with', _.uniq(_.pluck(fleetAssociations, 'fleet_id')), ': ', error)
            done(errors.internalServer(false), null)
            return
          }
          if (fleets.length === 0) {
            done(null, payload)
            return
          }
          getFleetMetaData({ fleet_id: _.uniq(_.pluck(fleetAssociations, 'fleet_id')) }, (error, fleetMetaData) => {
            if (error) {
              Sentry.captureException(error, {operatorId, fleet_id: _.uniq(_.pluck(fleetAssociations, 'fleet_id'))})
              logger('Error: getting customers with', _.pluck(fleets, 'customer_id'), ': ', error)
              done(errors.internalServer(false), null)
              return
            }
            const fleetsWithMetadata = _.map(fleets, (fleet) => {
              const metaData = _.findWhere(fleetMetaData, { fleet_id: fleet.fleet_id })
              return metaData ? Object.assign(fleet, metaData) : fleet
            })
            const fleetCustomers = _.pluck(fleetsWithMetadata, 'customer_id').concat([operator[0].customer_id])
            _getCustomers({ customer_id: _.uniq(fleetCustomers) }, (error, customers) => {
              if (error) {
                Sentry.captureException(error, {operatorId, fleet_id: _.uniq(_.pluck(fleetAssociations, 'fleet_id'))})
                logger('Error: getting customers with', _.pluck(fleets, 'customer_id'), ': ', error)
                done(errors.internalServer(false), null)
                return
              }
              if (customers.length === 0) {
                payload.fleet_data = fleets
                done(null, payload)
                return
              }
              let fleetCustomer
              let operatorAccess
              let fleet
              for (let i = 0, fleetLength = fleets.length; i < fleetLength; i++) {
                fleet = fleets[i]
                operatorAccess = _.findWhere(fleetAssociations, {
                  fleet_id: fleet.fleet_id,
                  operator_id: operatorId
                })
                delete fleet.key
                fleet.fleet_date_created = fleet.date_created
                if (!fleetCustomer || fleetCustomer.customer_id !== fleet.customer_id) {
                  fleetCustomer = _.findWhere(customers, { customer_id: fleet.customer_id })
                }
                if (operatorAccess) {
                  fleet = Object.assign(fleet, operatorAccess)
                }
                fleets[i] = Object.assign(fleet, fleetCustomer)
              }
              payload.fleet_data = fleets
              payload.customer_data = customers
              done(null, payload)
            })
          })
        })
      }
    })
  })
}

/**
 *   To get fleetList for an operator/customer
 *
 * @param {Number} fleetDetails - operator_id/ customer_id, fleet_id
 * @param {Function} done - Callback function with error, data params
 */
const getFleetList = (fleetDetails, done) => {
  getFleetsWithCustomers(fleetDetails, (error, fleets) => {
    if (error) {
      Sentry.captureException(error, {fleetDetails})
      done(error, null)
      return
    }
    if (!fleets || fleets.length === 0) {
      done(null, fleets)
      return
    }
    const fleetsLength = fleets.length
    getFleetMetaData({ fleet_id: _.uniq(_.pluck(fleets, 'fleet_id')) }, (error, metadata) => {
      if (error) {
        Sentry.captureException(error, {fleetDetails})
        logger('Error: getting fleet list. Failed to get fleet metadata with', _.uniq(_.pluck(fleets, 'fleet_id')),
          'with error', error)
        done(errors.internalServer(false), null)
        return
      }
      getFleetPaymentSettings({ fleet_id: _.uniq(_.pluck(fleets, 'fleet_id')) }, (error, fleetPayments) => {
        if (error) {
          Sentry.captureException(error, {fleetDetails})
          logger('Error: getting fleet list. Failed to get fleet Payments with', _.uniq(_.pluck(fleets, 'fleet_id')),
            'with error', error)
          done(errors.internalServer(false), null)
          return
        }
        getAddresses(null, (error, addresses) => {
          if (error) {
            Sentry.captureException(error, {fleetDetails})
            logger('Error: getting fleet list. Failed to get fleet address with', _.uniq(_.pluck(fleets, 'fleet_id')),
              'with error', error)
            done(errors.internalServer(false), null)
            return
          }
          let fleet
          let address
          let fleetMetadata
          let fleetPayment
          for (let i = 0; i < fleetsLength; i++) {
            fleet = fleets[i]
            address = _.findWhere(addresses, { type: 'fleet', type_id: fleet.fleet_id })
            fleetMetadata = _.findWhere(metadata, { fleet_id: fleet.fleet_id })
            fleetPayment = _.findWhere(fleetPayments, { fleet_id: fleet.fleet_id })
            address = address || {}
            fleetMetadata = fleetMetadata || {}
            fleetPayment = fleetPayment || {}
            fleets[i] = Object.assign(fleet, fleetMetadata, _.pick(address, 'state', 'country', 'postal_code', 'city'), _.pick(fleetPayment, 'currency'))
          }
          done(null, fleets)
        })
      })
    })
  })
}

/* This method updates the details of an existing Fleet
   *
   * @param {Object} customerDetails - Identifier for the customer details to get created
   * @param {Object} customerImageDetails - Identifier for customer fleet logo and contract file to get created
   * @param {Function} callback
   */
const updateFleetDetails = async (customerDetails, customerImageDetails, done) => {
  _getFleets({ fleet_name: customerDetails.fleet_name }, (err, fleets) => {
    const duplicateName = fleets.filter(fleet => fleet.fleet_id !== Number(customerDetails.fleet_id)).length > 0
    if (err) {
      logger('Error: Updating fleet details')
      return
    }

    if (duplicateName) {
      logger('Error; Duplicate fleet name')
      done({ code: 'duplicate', message: 'Duplicate fleet name' })
      return
    }
    if (customerDetails.type.includes('public')) {
      deleteDomain({ fleet_id: customerDetails.fleet_id }, (error) => {
        if (error) {
          Sentry.captureException(error, {fleetId: customerDetails.fleet_id})
        } else {
          return (null)
        }
      })
    }
    let weblink = customerDetails.fleet_weblink
    if ((weblink.trim() === '') || (weblink.trim().toUpperCase() === 'NULL')) {
      weblink = null
    }
    _updateFleet(Object.assign(_.pick(customerDetails, 'fleet_name', 'operator_id', 'contract_file', 'country_code', 'distance_preference'),
      {
        contact_first_name: customerDetails.first_name,
        contact_last_name: customerDetails.last_name,
        contact_phone: customerDetails.phone_number,
        contact_email: customerDetails.email,
        type: customerDetails.type,
        contact_web_link: weblink
      }), { fleet_id: customerDetails.fleet_id },
    (error) => {
      if (error) {
        Sentry.captureException(error, {fleetId: customerDetails.fleet_id})
        logger('Error: making update fleet query:', errors.errorWithMessage(error))
        done(errors.internalServer(false), null)
        return
      }
      updateFleetMetaData(_.pick(customerDetails, 'fleet_timezone'), _.pick(customerDetails, 'fleet_id'), (error) => {
        if (error) {
          Sentry.captureException(error, {customerDetails})
          logger('Error: making update fleet query:', errors.errorWithMessage(error))
          done(errors.internalServer(false), null)
          return
        }
        if (_.has(customerDetails, 'currency')) {
          updateFleetPaymentSettings(customerDetails, (error) => {
            if (error) {
              Sentry.captureException(error, {customerDetails})
              return done(error, null)
            }
            done(null, null)
          })
        } else {
          done(null, null)
        }
      })
    })
  })
}

/* This method inserts the details of fleet associations
   *
   * @param {Object} fleetData - Identifier for the fleet details to get created
   * @param {Function} callback
   * @private
   */
const _insertFleetAssociations = (fleetData, done) => {
  const insertFleetAssociationsForOperator = query.insertSingle(dbConstants.tables.fleet_associations, fleetData)
  sqlPool.makeQuery(insertFleetAssociationsForOperator, (error) => {
    if (error) {
      Sentry.captureException(error, {fleetData})
      logger('Error: making insert fleet associations query:', errors.errorWithMessage(error))
      done(errors.internalServer(false), null)
      return
    }
    done(errors.noError(), null)
  })
}

/* This method updates the details of fleet associations
   *
   * @param {Object} columnsAndValues - columns to be updated
   * @param {Object} comparisonColumn - columns to compare
   * @param {Function} done - callback
   */
const _updateFleetAssociations = (columnsAndValues, comparisonColumn, done) => {
  const fleetAssociationsQuery = query.updateSingle(dbConstants.tables.fleet_associations,
    columnsAndValues,
    comparisonColumn)
  sqlPool.makeQuery(fleetAssociationsQuery, (error) => {
    if (error) {
      Sentry.captureException(error, {columnsAndValues, comparisonColumn, fleetAssociationsQuery})
      done(errors.internalServer(false))
      return
    }
    done(null)
  })
}

/* This method  is to create new fleet
   *
   * @param {Object} fleetDetails - fleet details to create new fleet
   * @param {Object} customerImageDetails - Identifier for customer fleet logo and contract file to get created
   * @param {Function} done - callback
   */
const insertFleetDetails = (fleetDetails, customerImageDetails, done) => {
  if (!_.has(fleetDetails, 'customer_id')) {
    insertCustomer(_.extend(_.pick(fleetDetails, 'customer_name')), (error, customerStatus) => {
      if (error) {
        Sentry.captureException(error, {fleetDetails})
        logger('Error: making insert customer query with customer_id:', fleetDetails.operator_id)
        done(errors.internalServer(false), null)
        return
      }
      fleetDetails.customer_id = customerStatus.insertId
      _insertNewFleet(fleetDetails, customerImageDetails, done)
    })
  } else {
    _insertNewFleet(fleetDetails, customerImageDetails, done)
  }
}

/* This method is to insert new fleet
   *
   * @param {Object} fleetDetails - fleet details to create new fleet
   * @param {Object} customerImageDetails - Identifier for customer fleet logo and contract file to get created
   * @param {Function} done - callback
   */
const _insertNewFleet = (fleetDetails, customerImageDetails, done) => {
  const fileDetails = []
  fileDetails.push({
    file: customerImageDetails,
    file_type: customerImageDetails.mimetype,
    file_name: uploadConstants.types.fleet_logo + '-' + fleetDetails.fleet_name,
    category: 'logo'
  })
  if (customerImageDetails.contract_file) {
    fileDetails.push({
      file: customerImageDetails.contract_file,
      file_type: customerImageDetails.contract_file.type,
      file_name: uploadConstants.types.fleet_contract + '-' + fleetDetails.fleet_name,
      category: 'contract'
    })
  }
  if (customerImageDetails.t_and_c) {
    fileDetails.push({
      file: customerImageDetails.t_and_c,
      file_type: customerImageDetails.t_and_c.type,
      file_name: uploadConstants.types.fleet_t_and_c + '-' + fleetDetails.fleet_name,
      category: 't_and_c'
    })
  }
  fileUpload(fileDetails, (error, fileLocations) => {
    if (error) {
      Sentry.captureException(error, {fileDetails})
      logger('Error: uploading file to s3', errors.errorWithMessage(error))
      return done(errors.internalServer(true), null)
    }
    _.each(fileLocations, (file) => {
      if (file.file.fieldName === 'fleet_logo') {
        fleetDetails.logo = file.file_location
      } else if (file.file.fieldName === 't_and_c') {
        fleetDetails.t_and_c = file.file_location
        fleetDetails.t_and_c_date_created = moment().unix()
      } else {
        fleetDetails.contract_file = file.file_location
      }
    })
    _insertFleet(_.extend(_.pick(fleetDetails, 'fleet_name', 'operator_id', 'logo', 'contract_file', 'country_code', 'distance_preference'),
      {
        contact_first_name: fleetDetails.first_name,
        contact_last_name: fleetDetails.last_name,
        contact_phone: fleetDetails.phone_number,
        contact_email: fleetDetails.email,
        date_created: moment().unix(),
        customer_id: fleetDetails.customer_id,
        type: fleetDetails.type,
        key: encryptionHandler.encryptDbValue(passwordHandler.randString(10)),
        parking_area_restriction: 0
      }), (error, fleetStatus) => {
      if (error) {
        Sentry.captureException(error, {fleetDetails})
        logger('Error: making insert fleet query:', errors.errorWithMessage(error))
        done(errors.internalServer(false), null)
        return
      }
      insertFleetMetadata({
        fleet_id: fleetStatus.insertId,
        fleet_timezone: fleetDetails.fleet_timezone
      }, (error) => {
        if (error) {
          Sentry.captureException(error, {fleetDetails})
          logger('Error: making insert fleet metadata query:', errors.errorWithMessage(error))
          done(errors.internalServer(false), null)
          return
        }
        _insertAddress(_.extend(_.pick(fleetDetails, 'address1', 'address2', 'city', 'state', 'postal_code', 'country'),
          { type: 'fleet', type_id: fleetStatus.insertId }), (error) => {
          if (error) {
            Sentry.captureException(error, {fleetDetails})
            logger('Error: making update address Query with address_id: ', fleetDetails.address_id)
            done(errors.internalServer(false), null)
            return
          }
          const columnsAndValues = {
            acl: fleetConstants.acl.admin,
            operator_id: fleetDetails.operator_id,
            fleet_id: fleetStatus.insertId,
            customer_id: fleetDetails.customer_id
          }
          _insertFleetAssociations(columnsAndValues, (error) => {
            if (error) {
              Sentry.captureException(error, {columnsAndValues})
              logger('Error: making insert fleet associations query:', errors.errorWithMessage(error))
              done(errors.internalServer(false), null)
              return
            }
            if (_.has(fleetDetails, 'currency')) {
              const insertParams = {
                fleet_id: fleetStatus.insertId,
                currency: fleetDetails.currency
              }
              updateFleetProfile(insertParams, { fleet_id: fleetStatus.insertId }, (error) => {
                if (error) {
                  Sentry.captureException(error, {insertParams})
                  logger('Error: updating fleet profile query:', errors.errorWithMessage(error))
                  done(errors.internalServer(false), null)
                  return
                }
                done(errors.noError(), null)
              })
            } else {
              done(errors.noError(), null)
            }
          })
        })
      })
    })
  })
}

/* This method updates the on Call details of Operator
   *
   * @param {Object} operatorDetails - Details of the Operator for whom the details needs to be updated
   * @param {Function} callback - done
   */
const UpdateOnCallOperatorDetails = (operatorDetails, done) => {
  operatorHandler.getOperator(null, { operator_id: operatorDetails.operator_id }, (error, operator) => {
    if (error) {
      Sentry.captureException(error, {operatorDetails})
      logger('Error updating on-call operator with', operatorDetails.operator_id, 'for fleet',
        operatorDetails.fleet_id, 'Failed to get operator')
      done(error, null)
      return
    }
    if (operator.length !== 0) {
      if (operator[0].phone_number) {
        _updateFleetAssociations({ on_call: 0 },
          { fleet_id: operatorDetails.fleet_id, on_call: 1 }, (error) => {
            if (error) {
              Sentry.captureException(error, {operatorDetails})
              logger('Error updating on-call operator with', operatorDetails.operator_id, 'for fleet',
                operatorDetails.fleet_id)
              done(error, null)
              return
            }
            _updateFleetAssociations({ on_call: 1 },
              {
                fleet_id: operatorDetails.fleet_id,
                operator_id: operatorDetails.operator_id
              }, (error) => {
                if (error) {
                  Sentry.captureException(error, {operatorDetails})
                  logger('Error updating on-call operator with', operatorDetails.operator_id, 'for fleet',
                    operatorDetails.fleet_id)
                  done(error, null)
                  return
                }
                done(null)
              })
          })
      } else {
        done(errors.customError('Operator does not have phone number',
          responseCodes.Forbidden,
          'operatorNotEligible', false), null)
      }
    } else {
      done(errors.resourceNotFound(false), null)
    }
  })
}

const UpdateACL = (role, operator, done) => {
  _updateFleetAssociations({ acl: role.value }, operator, (error) => {
    if (error) {
      Sentry.captureException(error, {role, operator})
      logger('Error updating on-call operator with', operator.operator_id, 'for fleet', operator.fleet_id, 'Failed to get operator')
      done(error, null)
      return
    }
    done(null)
  })
}

/* This method retrieves the on Call details of Operator
   *
   * @param {Object} operatorDetails - Details of the Operator for whom the details needs to be fetched
   * @param {Function} callback - done
   */
const getOnCallOperatorDetails = (operatorDetails, done) => {
  getFleetAssociations({ fleet_id: operatorDetails.fleet_id }, (error, fleetAssociations) => {
    if (error) {
      Sentry.captureException(error, {operatorDetails})
      logger('Error: retrieving on-call operator with fleet_id:', operatorDetails.fleet_id)
      done(errors.internalServer(false), null)
      return
    }
    if (fleetAssociations.length !== 0) {
      operatorHandler.getOperator(null, { operator_id: _.pluck(fleetAssociations, 'operator_id') }, (error, operators) => {
        if (error) {
          Sentry.captureException(error, {operatorDetails})
          logger('Error: retrieving on-call operator with fleet_id:', operatorDetails.fleet_id)
          done(errors.internalServer(false), null)
          return
        }
        for (let i = 0, len = fleetAssociations.length; i < len; i++) {
          const fleetOperator = _.findWhere(operators, { operator_id: fleetAssociations[i].operator_id })
          if (fleetOperator) {
            fleetOperator.operator_acl = fleetOperator.acl
            delete fleetOperator.acl
            Object.assign(fleetAssociations[i], operatorHandler.formatOperatorForWire(fleetOperator))
          }
        }
        done(null, fleetAssociations)
      })
    } else {
      done(null, fleetAssociations)
    }
  })
}

/* This method inserts fleet associations for multiple columns and values
   *
   * @param {Object} operatorDetails - Details of the Operator for whom the fleet associations needs to be inserted
   * @param {Function} callback - done
   */
const insertAssociationForMultipleFleets = (columns, values, done) => {
  const insertAssociationsForMultipleFleetsQuery = query.insertMultiple(
    dbConstants.tables.fleet_associations,
    columns,
    values
  )
  sqlPool.makeQuery(insertAssociationsForMultipleFleetsQuery, (error) => {
    if (error) {
      Sentry.captureException(error, {columns, values})
      logger('Error: making insert Associations query:', insertAssociationsForMultipleFleetsQuery)
      done(errors.internalServer(true), null)
      return
    }
    done(errors.noError(), null)
  })
}

/*
   * Retrieves the details of fleet associations of the operator
   *
   * @param {Object} comparisonColumnsAndValues
   * @param {Function} Callback with params {error, rows}
   */
const getFleetAssociations = (comparisonColumn, done) => {
  const getFleetAssociationsQuery = query.selectWithAnd(dbConstants.tables.fleet_associations, null, comparisonColumn)
  sqlPool.makeQuery(getFleetAssociationsQuery, done)
}

/*
   * Retrieves the details of fleet associations of the operator
   *
   * @param {Object} comparisonColumnsAndValues
   * @param {Function} Callback with params {error, rows}
   */
const getAddresses = (comparisonColumn, done) => {
  const getAddressesQuery = query.selectWithAnd(dbConstants.tables.addresses, null, comparisonColumn)
  sqlPool.makeQuery(getAddressesQuery, done)
}

/* This method gets the Domain Details of operator/ customer
   *
   * @param {Object} operatorDetails - contains fleet_id, domain_name, operator_id and customer_id
   * @param {Function} callback - done
   */
const getDomains = (operatorDetails, done) => {
  const comparisonColumn = {}
  if (operatorDetails.fleet_id) {
    comparisonColumn.fleet_id = operatorDetails.fleet_id
  }
  if (operatorDetails.operator_id) {
    comparisonColumn.operator_id = operatorDetails.operator_id
  }
  if (operatorDetails.customer_id) {
    comparisonColumn.customer_id = operatorDetails.customer_id
  }
  if (operatorDetails.domain_name) {
    comparisonColumn.domain_name = operatorDetails.domain_name
  }
  if (operatorDetails.domain_id) {
    comparisonColumn.domain_id = operatorDetails.domain_id
  }

  const getDomainQuery = query.selectWithAnd(dbConstants.tables.domains, null, comparisonColumn)
  sqlPool.makeQuery(getDomainQuery, (error, domains) => {
    if (error) {
      Sentry.captureException(error, {operatorDetails})
      logger('Error: fetching domain_name:  for', operatorDetails.fleet_id, ': ', error)
      done(errors.internalServer(false), null)
      return
    }
    return done(null, domains)
  })
}

/* This method saves the Domain Details of operator/ customer
   *
   * @param {Object} operatorDetails - contains fleet_id, domain_name, operator_id and customer_id
   * @param {Function} callback - done
   */
const saveDomainDetailsForFleet = (operatorDetails, done) => {
  getDomains(operatorDetails, async (error, domains) => {
    if (error) {
      Sentry.captureException(error, {operatorDetails})
      logger('Error: fetching domain_name: ', operatorDetails.domain_name, 'for', operatorDetails.fleet_id, ': ', error)
      done(errors.internalServer(false), null)
      return
    }
    if (domains && domains.length > 0) {
      logger('Error: Domain domain_name: ', operatorDetails.domain_name, 'for', operatorDetails.fleet_id, 'already exist: ', error)
      done(errors.customError('Domain name already exists for this fleet!', responseCodes.Conflict, 'DomainAlreadyExists', false), null)
      return
    }

    if (!Array.isArray(operatorDetails.domain_name)) {
      operatorDetails.domain_name = [operatorDetails.domain_name]
    }
    const domainList = []
    _.each(operatorDetails.domain_name, (domainName, index) => {
      const columnsAndValues = {
        fleet_id: operatorDetails.fleet_id,
        domain_name: domainName,
        operator_id: operatorDetails.operator_id,
        customer_id: operatorDetails.customer_id
      }
      domainList.push(columnsAndValues)

      const insertDomainQuery = query.insertSingle(dbConstants.tables.domains, columnsAndValues)
      sqlPool.makeQuery(insertDomainQuery, (error) => {
        if (error) {
          Sentry.captureException(error, {operatorDetails, insertDomainQuery})
          logger('Error: inserting domain_name: ', operatorDetails.domain_name, 'for', operatorDetails.fleet_id, ': ', error)
          done(errors.internalServer(false), null)
        }
      })
    })
    try {
      if (domainList.length) {
        const promises = domainList.map(async (domain) => {
          const usersToBeGrantedAccess = await db.users('users')
            .where('email', 'like', `%${domain.domain_name}`)
            .where('user_type', 'lattis')
          const existingUsers = await db.users('private_fleet_users')
            .where('email', 'like', `%${domain.domain_name}`)
            .where('fleet_id', `${domain.fleet_id}`)
          return {'allUsers': usersToBeGrantedAccess, 'existingUsers': existingUsers}
        })
        const [allData] = await Promise.all(promises).then(response => response.map(res => { return res }))
        if (allData && allData.existingUsers.length) {
          // grant available users permission over here
          allData.existingUsers.forEach(async (user) => {
            await db.users('private_fleet_users')
              .update({access: 1, access_mode: 'domain', updated_at: moment().unix()})
              .where({email: user.email})
          })
        }
        if (allData.allUsers.length) {
          const existingUserEmails = _.pluck(allData.existingUsers, 'email')
          if (allData.allUsers.length) {
            allData.allUsers.forEach(async (member) => {
              if (!existingUserEmails.includes(member.email)) {
                await db.users('private_fleet_users')
                  .insert({
                    email: member.email,
                    user_id: member.user_id,
                    fleet_id: operatorDetails.fleet_id,
                    verified: 0,
                    access: 1,
                    access_mode: 'domain',
                    updated_at: moment().unix()
                  })
              }
            })
          }
        }
        return done(error, null)
      } else {
        return done(error, null)
      }
    } catch (error) {
      Sentry.captureException(error, {operatorDetails})
      logger('Error occurred when updating domain users:::', error)
      throw new Error(`Failed to update domain users: ${error}`)
    }
  })
}

/* This method deletes the Domain Details of operator/ customer
   *
   * @param {Object} operatorDetails - contains
   * @param {Function} callback - done
   */
const deleteDomainForFleet = (operatorDetails, done) => {
  fleetHandler.getDomains({ domain_id: operatorDetails.domain_id }, async (error, domains) => {
    if (error) {
      Sentry.captureException(error, {operatorDetails})
      done(error, null)
      return
    }
    if (domains.length) {
      try {
        const promises = domains.map(async (domain) => {
          const targetMembers = await db.users('private_fleet_users')
            .where({ fleet_id: operatorDetails.fleet_id })
            .where({access: 1})
            .where('email', 'like', `%${domain.domain_name}`)
          return targetMembers
        })
        const [membersToLoseAccess] = await Promise.all(promises).then(response => response.map(res => { return res }))
        if (membersToLoseAccess.length) {
          membersToLoseAccess.forEach(async (member) => {
            await db.users('private_fleet_users').update({access: 0, verified: 0})
              .where({fleet_id: operatorDetails.fleet_id})
              .where({email: member.email})
          })
          done(null, null)
        } else {
          done(null, null)
        }
        domains.forEach(async (domain) => {
          await db.main('domains').delete()
            .where({domain_name: domain.domain_name})
            .where({fleet_id: operatorDetails.fleet_id})
        })
      } catch (error) {
        Sentry.captureException(error, {operatorDetails})
        logger('Error occurred when updating domain users:::', error)
        throw new Error(`Failed to update domain users: ${error}`)
      }
    } else {
      done(null, null)
    }
  })
}

/* This method gets the fleets with fleetAssociations
   *
   * @param {Object} operatorDetails - contains
   * @param {Function} callback - done
   */
const getFleetsWithFleetAssociations = (comparisonColumn, filterColumn, filterValues, adminOnly, done) => {
  let fleetWithFleetAssociationsQuery = `SELECT ${comparisonColumn} FROM ${dbConstants.tables.fleet_associations} WHERE ${filterColumn} IN (${filterValues})`
  if (adminOnly) {
    fleetWithFleetAssociationsQuery += ' AND (acl = \'admin\' OR acl = \'normal_admin\')'
  }
  fleetWithFleetAssociationsQuery = `SELECT * FROM ${dbConstants.tables.fleets} WHERE ${comparisonColumn} IN (${fleetWithFleetAssociationsQuery})`
  sqlPool.makeQuery({ lattis_main: fleetWithFleetAssociationsQuery }, (error, fleetDetails) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: getting fleets with fleetAssociations', filterColumn[0], ': ', error)
      done(errors.internalServer(false), null)
      return
    }
    if (fleetDetails.length === 0) {
      done(null, fleetDetails)
      return
    }
    /* const filters = filterValues.map(f => Number(f)) */
    const fleets = _formatFleetsForWire(fleetDetails)
    done(null, fleets)
  })
}

/* This method is used to get all the fleet details

   * @param {Object} comparisonValues - contains Values to be compared
   * @param {Function} callback - done
   */
const _getFleets = (comparisonValues, done) => {
  const fleetQuery = query.selectWithAnd(dbConstants.tables.fleets, null, comparisonValues)
  sqlPool.makeQuery(fleetQuery, (error, fleets) => {
    if (error) {
      Sentry.captureException(error, {comparisonValues})
      logger('Error: making fleets query with ', comparisonValues, fleetQuery)
      done(error, null)
      return
    }
    done(errors.noError(), _formatFleetsForWire(fleets))
  })
}

/* This method is used to get all the fleets with payment settings

   * @param {Object} comparisonValues - contains Values to be compared
   * @param {Function} callback - done
   */
const getFleetsWithPayment = (comparisonValues, done) => {
  const filterColumnAndValues = {}
  if (_.has(comparisonValues, 'fleet_id')) {
    filterColumnAndValues.fleet_id = comparisonValues.fleet_id
  }
  const fleetQuery = query.selectWithAnd(dbConstants.tables.fleets, null, filterColumnAndValues)
  sqlPool.makeQuery(fleetQuery, (error, fleets) => {
    if (error) {
      Sentry.captureException(error, {comparisonValues, fleetQuery})
      logger('Error: making fleets query with ', fleetQuery)
      done(error, null)
      return
    }
    const fleetsLen = fleets.length
    if (fleetsLen === 0) {
      done(errors.noError(), fleets)
      return
    }
    const fleetPaymentsQuery = query.selectWithAnd(dbConstants.tables.fleet_payment_settings, null,
      { fleet_id: _.uniq(_.pluck(fleets, 'fleet_id')) })
    sqlPool.makeQuery(fleetPaymentsQuery, (error, fleetPayments) => {
      if (error) {
        Sentry.captureException(error, {comparisonValues, fleetPaymentsQuery})
        logger('Error: making fleets payment settings query with ', fleetPayments)
        done(error, null)
        return
      }
      let fleetPaymentSetting
      for (let i = 0; i < fleetsLen; i++) {
        fleetPaymentSetting = _.findWhere(fleetPayments, { fleet_id: fleets[i].fleet_id })
        if (fleetPaymentSetting) {
          fleets[i] = Object.assign(fleets[i], fleetPaymentSetting)
        }
      }
      done(errors.noError(), _formatFleetsForWire(fleets))
    })
  })
}

/**
   * To create stripe fleet profile
   * @param {Object} requestParam
   * @param {Function} done - Callback
   */
const createFleetPaymentProfile = (requestParam, done) => {
  _getFleets({ fleet_id: requestParam.fleet_id }, (error, fleet) => {
    if (error) {
      Sentry.captureException(error, {requestParam})
      logger('Error: Sorry can\'t get details of fleet', error)
      done(errors.internalServer(true), null)
      return
    }
    if (fleet.length !== 0) {
      const stripe = require('stripe')(config.stripe.apiKey)
      stripe.customers.create({
        email: fleet[0].contact_email
      }).then((customer) => {
        stripe.customers.createSource(customer.id, {
          source: {
            object: 'card',
            exp_month: requestParam.exp_month,
            exp_year: requestParam.exp_year,
            number: requestParam.cc_no,
            cvc: requestParam.cvc,
            name: fleet[0].fleet_name
          }
        }, function (err, source) {
          if (err) {
            logger('Error: Sorry can\'t connect with stripe', err)
            return done(errors.invalidCreditCard(false), null)
          } else {
            getCards(null, {
              fleet_id: requestParam.fleet_id,
              operator_id: requestParam.operator_id
            }, function (error, fleetsProfile) {
              if (error) {
                Sentry.captureException(error, {requestParam})
                logger('Error: not able to add card')
                done(errors.internalServer(false), null)
                return
              }
              if (fleetsProfile.length === 0) {
                const columnsAndValues = {
                  operator_id: requestParam.operator_id,
                  stripe_profile_id: customer.id,
                  cc_no: requestParam.cc_no.replace(/.(?=.{4})/g, 'x'),
                  exp_month: requestParam.exp_month,
                  exp_year: requestParam.exp_year,
                  type_card: fleetConstants.cardinfo.type_card,
                  card_id: source.id,
                  fleet_id: requestParam.fleet_id,
                  created_date: moment().unix(),
                  fingerprint: source.fingerprint,
                  cc_type: source.brand
                }
                _insertCards(columnsAndValues, function (error) {
                  if (error) {
                    Sentry.captureException(error, {requestParam})
                    logger('Error: not able to add card')
                    done(errors.internalServer(false), null)
                    return
                  }
                  done(errors.noError(), null)
                })
              } else {
                if (source.fingerprint !== fleetsProfile[0].fingerprint) {
                  stripe.customers.deleteCard(
                    fleetsProfile[0].stripe_profile_id,
                    fleetsProfile[0].card_id,
                    function (err) {
                      if (err) {
                        logger('Error: Sorry can\'t connect with stripe', err)
                        done(errors.invalidCreditCard(false), null)
                      } else {
                        const columnsAndValues = {
                          operator_id: requestParam.operator_id,
                          stripe_profile_id: customer.id,
                          cc_no: requestParam.cc_no.replace(/.(?=.{4})/g, 'x'),
                          exp_month: requestParam.exp_month,
                          exp_year: requestParam.exp_year,
                          type_card: fleetConstants.cardinfo.type_card,
                          card_id: source.id,
                          fleet_id: requestParam.fleet_id,
                          created_date: moment().unix(),
                          fingerprint: source.fingerprint,
                          cc_type: source.brand
                        }

                        const updatePaymentProfile = query.updateSingle(dbConstants.tables.operator_payment_profiles, columnsAndValues, {
                          fleet_id: requestParam.fleet_id,
                          operator_id: requestParam.operator_id
                        })
                        sqlPool.makeQuery(updatePaymentProfile, (error) => {
                          if (error) {
                            Sentry.captureException(error, {requestParam})
                            logger('Error: updating fleet paymemt profile with fleet_id:', requestParam.fleet_id)
                            done(error, null)
                          }
                        })
                        done(errors.noError(), null)
                      }
                    }
                  )
                } else {
                  const columnsAndValues = {
                    operator_id: requestParam.operator_id,
                    stripe_profile_id: customer.id,
                    cc_no: requestParam.cc_no.replace(/.(?=.{4})/g, 'x'),
                    exp_month: requestParam.exp_month,
                    exp_year: requestParam.exp_year,
                    type_card: fleetConstants.cardinfo.type_card,
                    card_id: source.id,
                    fleet_id: requestParam.fleet_id,
                    created_date: moment().unix(),
                    fingerprint: source.fingerprint,
                    cc_type: source.brand
                  }

                  const updatePaymentProfile = query.updateSingle(dbConstants.tables.operator_payment_profiles, columnsAndValues, {
                    fleet_id: requestParam.fleet_id,
                    operator_id: requestParam.operator_id
                  })
                  sqlPool.makeQuery(updatePaymentProfile, (error) => {
                    if (error) {
                      Sentry.captureException(error, {requestParam})
                      logger('Error: updating fleet paymemt profile with fleet_id:', requestParam.fleet_id)
                      done(error, null)
                    }
                  })
                  done(errors.noError(), null)
                }
              }
            })
          }
        })
      }).catch(function (err) {
        logger('Error: Sorry can\'t connect with stripe', err)
        return done(errors.invalidCreditCard(false), null)
      })
    }
  })
}

/*
   * Insert new billing address for fleet
   * @param {Object} requestParam
   * @param {Function} Callback with params {error, rows}
   */
const createFleetBillingAddress = (requestParam, done) => {
  _insertAddress(_.extend(_.pick(requestParam, 'address1', 'address2', 'city', 'state', 'postal_code', 'country'),
    { type: 'fleet_billing', type_id: requestParam.fleet_id }), (error) => {
    if (error) {
      Sentry.captureException(error, {requestParam})
      logger('Error: not able to create billing address for fleet')
      done(errors.internalServer(false), null)
      return
    }
    done(errors.noError(), null)
  })
}

/*
   * Retrieves the details of saved address for fleet
   *
   * @param {Object} fleet_id
   * @param {Function} Callback with params {error, rows}
   */

const getFleetBillingAddress = (fleetId, done) => {
  getAddresses({ type_id: fleetId, type: 'fleet_billing' }, (error, fleetAddresses) => {
    if (error) {
      Sentry.captureException(error, {fleetId})
      logger('Error: getting addresses', ': ', error)
      return done(errors.internalServer(false), null)
    }
    done(errors.noError(), fleetAddresses)
  })
}

/*
   * Retrieves the details of saved address for fleet
   *
   * @param {Object} fleet_id
   * @param {Function} Callback with params {error, rows}
   */

const updateFleetBillingAddress = (requestParam, done) => {
  const columnsAndValues = {
    address1: requestParam.address1,
    address2: requestParam.address2,
    city: requestParam.city,
    state: requestParam.state,
    postal_code: requestParam.postal_code,
    country: requestParam.country
  }
  _updateAddress(columnsAndValues, { address_id: requestParam.address_id }, (error) => {
    if (error) {
      Sentry.captureException(error, {requestParam})
      logger('Error: not able to update billing address for fleet')
      done(errors.internalServer(false), null)
      return
    }
    done(errors.noError(), null)
  })
}

/*
   * Retrieves the details of saved cards for fleet
   *
   * @param {Object} columnsToSelect
   * @param {Object} comparisonColumnsAndValues
   * @param {Function} Callback with params {error, rows}
   */
const getCards = (columnsToSelect, comparisonColumnsAndValues, done) => {
  const operatorQuery = query.selectWithAnd(dbConstants.tables.operator_payment_profiles, columnsToSelect, comparisonColumnsAndValues)
  sqlPool.makeQuery(operatorQuery, done)
}

/*
   * Retrieves the details of saved cards for fleet
   *
   * @param {Object} columnsAndValues
   */

const _insertCards = function (columnsAndValues, done) {
  const insertCardQuery = query.insertSingle(dbConstants.tables.operator_payment_profiles, columnsAndValues)
  sqlPool.makeQuery(insertCardQuery, done)
}

/*
   * Retrieves the details of saved cards for fleet
   *
   * @param {Object} columnsToSelect
   * @param {Object} comparisonColumnsAndValues
   * @param {Function} Callback with params {error, rows}
   */
const getFleetInvoice = (columnsToSelect, comparisonColumnsAndValues, done) => {
  const operatorQuery = query.selectWithAnd(dbConstants.tables.operator_payment_invoice, columnsToSelect, comparisonColumnsAndValues)
  sqlPool.makeQuery(operatorQuery, done)
}

/*
   * Insert blling of fleet by hour/day and penalty fees
   * @param {Object} requestParam
   * @param {Function} Callback with params {error, rows}
   */
const billingCycleFleet = async (requestParam, done) => {
  const columnsAndValues = {
    price_for_membership: requestParam.price_for_membership,
    price_type_value: requestParam.price_type_value,
    price_type: requestParam.price_type,
    usage_surcharge: requestParam.usage_surcharge,
    excess_usage_fees: requestParam.excess_usage_fees,
    excess_usage_type_value: requestParam.excess_usage_type_value,
    excess_usage_type: requestParam.excess_usage_type,
    excess_usage_type_after_value: requestParam.excess_usage_type_after_value,
    excess_usage_type_after_type: requestParam.excess_usage_type_after_type,
    ride_deposit: requestParam.ride_deposit,
    price_for_ride_deposit_type: requestParam.price_for_ride_deposit_type,
    refund_criteria: requestParam.refund_criteria,
    refund_criteria_value: requestParam.refund_criteria_value,
    price_for_penalty_outside_parking: requestParam.price_for_penalty_outside_parking,
    price_for_penalty_outside_parking_below_battery_charge: requestParam.price_for_penalty_outside_parking_below_battery_charge,
    price_for_ride_deposit: requestParam.price_for_ride_deposit,
    price_for_forget_plugin: requestParam.price_for_forget_plugin,
    price_for_bike_unlock: requestParam.price_for_bike_unlock,
    price_for_reservation_late_return: requestParam.price_for_reservation_late_return,
    enable_preauth: requestParam.enable_preauth,
    preauth_amount: requestParam.preauth_amount || 0
  }

  if (
    requestParam.enable_preauth &&
    (!requestParam.preauth_amount || Number(requestParam.preauth_amount) === 0)
  ) {
    return done(
      errors.customError(
        'An amount to pre-authorize is required',
        responseCodes.BadRequest,
        'BadRequest'
      )
    )
  }

  updateFleetProfile(columnsAndValues, { fleet_id: requestParam.fleet_id }, (error) => {
    if (error) {
      Sentry.captureException(error, {requestParam})
      logger('Error: making update fleet query:', errors.errorWithMessage(error))
      done(errors.internalServer(false), null)
      return
    }
    done(null, null)
  })
}

/*
   * get fleets with fleet id
   * @param {Integer} fleetId
   * @param {Function} Callback with params {error, rows}
   */
const getFleetWithFleetId = (fleetId, done) => {
  const fleetQuery = query.selectWithAnd(dbConstants.tables.fleets, [], { fleet_id: fleetId })
  sqlPool.makeQuery(fleetQuery, (error, fleet) => {
    if (error) {
      Sentry.captureException(error, {fleetId})
      logger('Error: get Fleet With Fleet id:', errors.errorWithMessage(error))
      done(errors.internalServer(false), null)
      return
    }
    done(null, _formatFleetForWire(fleet[0]))
  })
}

/**
   * The method fetches fleets with its metadata
   *
   * @param {Object} comparisonValues
   * @param {Function} callback
   */
const getFleetMetaData = (comparisonValues, callback) => {
  const getFleetMetaDataQuery = query.selectWithAnd(dbConstants.tables.fleet_metadata, null, comparisonValues)
  sqlPool.makeQuery(getFleetMetaDataQuery, callback)
}

/**
   * The method inserts in fleet metadata
   *
   * @param {Object} columnsAndValues
   * @param {Function} callback
   */
const insertFleetMetadata = (columnsAndValues, callback) => {
  const insertFleetMetaData = query.insertSingle(dbConstants.tables.fleet_metadata, columnsAndValues)
  sqlPool.makeQuery(insertFleetMetaData, callback)
}

/*
   * The method updates in fleet metadata
   *
   * @param {Object} columnsAndValues
   * @param {Function} callback
   */
const updateFleetMetaData = (columnsAndValues, filterParam, done) => {
  const updateFleetsQuery = query.updateSingle(dbConstants.tables.fleet_metadata, columnsAndValues, filterParam)
  sqlPool.makeQuery(updateFleetsQuery, done)
}

/* This method formats all Fleets
   * @param {Object} fleets
   * @private
   */
const _formatFleetsForWire = (fleets) => {
  if (!Array.isArray(fleets)) {
    fleets = [fleets]
  }
  const fleetsLength = fleets.length
  for (let i = 0; i < fleetsLength; i++) {
    fleets[i] = _formatFleetForWire(fleets[i])
  }
  return fleets
}

/* This method formats a fleet
   * @param {Object} fleet
   * @private
   */
const _formatFleetForWire = (fleet) => {
  return _.omit(fleet, 'key')
}

const deleteDomain = (comparisonColumn, done) => {
  const deleteDomainQuery = query.deleteSingle(dbConstants.tables.domains, comparisonColumn)
  sqlPool.makeQuery(deleteDomainQuery, (error) => {
    if (error) {
      Sentry.captureException(error, {comparisonColumn})
      logger('Error: deleting domains with: ', comparisonColumn, ' and error: ', error)
      return done(errors.internalServer(false), null)
    }
    done(null, null)
  })
}

/* const deletePrivateFleetUsers = (comparisonColumn, done) => {
  const deleteDomainQuery = query.deleteSingle(dbConstants.tables.private_fleet_users, comparisonColumn)
  sqlPool.makeQuery(deleteDomainQuery, (error) => {
    if (error) {
      logger('Error: deleting users with: ', comparisonColumn, ' and error: ', error)
      return done(errors.internalServer(false), null)
    }
    done(null, null)
  })
} */

const createGeofence = async (geofence) => {
  const [insertedId] = await db.main('geofences').insert({...geofence, status: true})
  const geoData = { ...geofence, geofence_id: insertedId, status: true }
  await sendCreateGeofenceRequestToGPSService(geoData)
  return db.main('geofences').first().where({ geofence_id: insertedId })
}

const getGeofences = async (fleetId) => {
  try {
    const geofences = await db.main.select().from('geofences').where({ fleet_id: fleetId })
    const enabled = await db.main('fleet_metadata').select('fleet_id', 'geofence_enabled').where({fleet_id: fleetId}).first()
    return { geofences, enabled }
  } catch (error) {
    Sentry.captureException(error, {fleetId})
    logger('Error:::', error)
    throw new Error('Failed to get geofences')
  }
}

const updateGeofence = async (geofenceId, update) => {
  const updated = await db.main('geofences')
    .where({ geofence_id: geofenceId })
    .update(update)
  if (!updated) {
    throw errors.invalidParameter(false)
  }
  return db.main('geofences').first().where({ geofence_id: geofenceId })
}

const updateGeofencesStatus = async (fleetId, status) => {
  const updated = await db.main('geofences')
    .where({ fleet_id: fleetId })
    .update({ status })
  const updateFleet = await db
    .main('fleet_metadata')
    .where({ fleet_id: fleetId })
    .update({ geofence_enabled: status })
  if (!updated || !updateFleet) {
    throw errors.invalidParameter(false)
  }
  const fleetMeta = await db.main('fleet_metadata').where({ fleet_id: fleetId }).select('fleet_id', 'geofence_enabled').first()
  return fleetMeta
}

const deleteGeofence = async (geofenceId) => {
  const deleted = await db.main('geofences')
    .where({ geofence_id: geofenceId })
    .delete()
  if (!deleted) {
    throw errors.invalidParameter(false)
  }
  return db.main('geofences').first().where({ geofence_id: geofenceId })
}

module.exports = Object.assign(module.exports, {
  getCustomerList: getCustomerList,
  validateNewFleet: validateNewFleet,
  checkFleetName: checkFleetName,
  checkCustomerName: checkCustomerName,
  getOperatorData: getOperatorData,
  getFleetStatistics: getFleetStatistics,
  insertAssociationForMultipleFleets: insertAssociationForMultipleFleets,
  getFleetList: getFleetList,
  getFleetsWithPayment: getFleetsWithPayment,
  updateFleetDetails: updateFleetDetails,
  insertFleetDetails: insertFleetDetails,
  UpdateOnCallOperatorDetails: UpdateOnCallOperatorDetails,
  UpdateACL: UpdateACL,
  getOnCallOperatorDetails: getOnCallOperatorDetails,
  getFleetAssociations: getFleetAssociations,
  saveDomainDetailsForFleet: saveDomainDetailsForFleet,
  deleteDomainForFleet: deleteDomainForFleet,
  getDomains: getDomains,
  getFleetsWithFleetAssociations: getFleetsWithFleetAssociations,
  createFleetPaymentProfile: createFleetPaymentProfile,
  createFleetBillingAddress: createFleetBillingAddress,
  getFleetBillingAddress: getFleetBillingAddress,
  getCards: getCards,
  getFleetInvoice: getFleetInvoice,
  billingCycleFleet: billingCycleFleet,
  insertCustomer: insertCustomer,
  updateFleetBillingAddress: updateFleetBillingAddress,
  updateFleetProfile: updateFleetProfile,
  getFleetSettings: getFleetSettings,
  updateFleetSettings: updateFleetSettings,
  getFleetWithFleetId: getFleetWithFleetId,
  getFleetMetaData: getFleetMetaData,
  insertFleetMetadata: insertFleetMetadata,
  updateFleetMetaData: updateFleetMetaData,
  createGeofence,
  getGeofences,
  updateGeofence,
  deleteGeofence,
  updateGeofencesStatus,
  updateFleetLogo
})
