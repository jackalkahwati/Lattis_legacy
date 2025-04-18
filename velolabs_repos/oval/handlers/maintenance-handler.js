'use strict'

const platform = require('@velo-labs/platform')
const queryCreator = platform.queryCreator
const sqlPool = platform.sqlPool
const logger = platform.logger
const errors = platform.errors
const moment = require('moment')
const bikeConstants = require('./../constants/bike-constants')
const dbConstants = require('./../constants/db-constants')
const ticketConstants = require('./../constants/ticket-constants')
const maintenanceConstants = require('./../constants/maintenance-constants')
const tripConstants = require('./../constants/trip-constants')
const bikeHandler = require('./bike-handler')
const ticketHandler = require('./../handlers/ticket-handler')
const _ = require('underscore')
const tripHandler = require('./../handlers/trip-handler')
const bookingHandler = require('./../handlers/booking-handler')
const { configureSentry } = require('../utils/configureSentry')

const Sentry = configureSentry()

/**
 * The method saves a maintenance record to the database for a given user
 *
 * @param {Object} maintenanceObjects Objects with key/value pairs
 * @param {Function} callback
 */
const saveMaintenance = (maintenanceObjects, callback) => {
  const query = queryCreator.insertSingle(
    dbConstants.tables.maintenance,
    maintenanceObjects
  )
  sqlPool.makeQuery(query, callback)
}

/**
 * The method saves a maintenance record to the database for a given user
 *
 * @param {Object} columnsAndValues - columns to be updated
 * @param {Object} comparisonColumns - comparison parameter
 * @param {Function} callback
 */
const updateMaintenance = (columnsAndValues, comparisonColumns, callback) => {
  const query = queryCreator.updateSingle(
    dbConstants.tables.maintenance,
    columnsAndValues,
    comparisonColumns
  )
  sqlPool.makeQuery(query, callback)
}

/**
 * To get maintenance records
 *
 * @param {Object} maintenanceDetails - Details of the maintenance
 * @param {Function} callback
 */
const getMaintenance = (maintenanceDetails, callback) => {
  const query = queryCreator.selectWithAnd(
    dbConstants.tables.maintenance,
    null,
    maintenanceDetails
  )
  sqlPool.makeQuery(query, callback)
}

/**
 * The method Saves the Damage Report into maintenance database
 *
 * @param {Object}  maintenanceDetails - contains the details of the maintenance to be saved in the database
 * @param {Function} callback
 */
const createDamageReport = (maintenanceDetails, callback) => {
  logger('Info: Create Damage report with details', maintenanceDetails)
  getMaintenance(
    {
      bike_id: maintenanceDetails.bike_id,
      status: maintenanceConstants.category.damageReported
    },
    (error, maintenanceRows) => {
      if (error) {
        Sentry.captureException(error, { maintenanceDetails })
        logger(
          'Error: could not save new maintenance record. Unable to get maintenance with bike_id:',
          maintenanceDetails.bike_id,
          'Failed with error:',
          error
        )
        return callback(errors.internalServer(false))
      }
      const activeMaintenance = maintenanceRows.filter(
        (maintenanceRow) => maintenanceRow.service_end_date === null
      )
      if (activeMaintenance.length === 0) {
        // getting bike with the bike_id
        bikeHandler.getBikesWithLock(
          { bike_id: maintenanceDetails.bike_id },
          (error, bikes) => {
            if (error) {
              Sentry.captureException(error, { maintenanceDetails })
              logger(
                'Error: could not save new maintenance record. Unable to get bike with bike_id:',
                maintenanceDetails.bike_id,
                'Failed with error:',
                error
              )
              return callback(errors.internalServer(false))
            }

            let category
            if (
              maintenanceDetails.category ===
                maintenanceConstants.category.damageReported ||
              maintenanceDetails.category ===
                maintenanceConstants.category.issueDetected ||
              maintenanceDetails.category ===
                maintenanceConstants.category.parkedOutsideGeofence ||
              maintenanceDetails.category ===
                maintenanceConstants.category.reportedTheft ||
              maintenanceDetails.category ===
                maintenanceConstants.category.serviceDue
            ) {
              category = maintenanceDetails.category
            } else {
              category = maintenanceConstants.category.damageReported
            }

            const ticketProp = {
              status: maintenanceConstants.status.damageReported,
              category: category,
              maintenance_category: maintenanceDetails.category
                ? maintenanceDetails.category
                : maintenanceDetails.damage_type,
              user_photo: maintenanceDetails.image,
              rider_notes: maintenanceDetails.notes,
              bike_id: bikes[0].bike_id,
              lock_id: bikes[0].lock_id,
              fleet_id: bikes[0].fleet_id,
              customer_id: bikes[0].customer_id,
              reported_by_user_id: maintenanceDetails.user_id,
              trip_id: maintenanceDetails.trip_id,
              date_created: moment().unix(),
              service_start_date: moment().unix()
            }

            ticketHandler.createTicket(ticketProp, (error) => {
              if (error) {
                Sentry.captureException(error, {
                  maintenanceDetails,
                  ticketProp
                })
                logger(
                  'Error: could not create tickets for the user with user_id',
                  maintenanceDetails.user_id,
                  ' with ticket parameters: ',
                  ticketProp,
                  'Failed with error',
                  error
                )
                callback(errors.internalServer(false))
                return
              }
              callback(errors.noError())
            })
          }
        )
      } else {
        // Check if there's an active booking for the bike
        bookingHandler.getActiveBooking(
          { bike_id: maintenanceDetails.bike_id },
          (error, bookingDetails) => {
            if (error) {
              Sentry.captureException(error, { maintenanceDetails })
              logger(
                'Error: Saving maintenance record. Failed to get booking details with bike_id',
                maintenanceDetails.bike_id,
                'with error:',
                error
              )
              return callback(error)
            }
            if (!bookingDetails) {
              // updating the bike with the bike_id
              bikeHandler.updateBike(
                null,
                {
                  bike_id: maintenanceDetails.bike_id,
                  status: bikeConstants.status.suspended,
                  current_status: bikeConstants.current_status.damaged,
                  maintenance_status:
                    bikeConstants.maintenance_status.shopMaintenance
                },
                (error) => {
                  if (error) {
                    Sentry.captureException(error, { maintenanceDetails })
                    logger(
                      'Error: could not save new maintenance record. Unable to update bike with bike_id',
                      maintenanceDetails.bike_id,
                      'Failed with error:',
                      error
                    )
                    return callback(errors.internalServer(false))
                  }
                  callback(errors.noError())
                }
              )
            } else {
              callback(errors.noError())
            }
          }
        )
      }
    }
  )
}

/**
 * The method Saves the Damage Report into maintenance database
 *
 * @param {Object}  theftDetails - Stolen reporting bike identification
 * @param {Function} callback
 */
const theftReported = (theftDetails, callback) => {
  logger('Info: Create theft report with details', theftDetails)
  bikeHandler.getBikesWithLock({ bike_id: theftDetails.bike_id }, (error, bikes) => {
    if (error) {
      Sentry.captureException(error, { theftDetails })
      logger(
        'Error: could not fetch bike record. bike_id :',
        theftDetails.bike_id,
        theftDetails.bike_id,
        'Failed with error:',
        error
      )
      return callback(error)
    }
    bookingHandler.getBookingDetails(
      {
        bike_id: bikes[0].bike_id,
        status: tripConstants.booking.active
      },
      (error, booking) => {
        if (error) {
          Sentry.captureException(error, { theftDetails })
          logger(
            'Error: Fetching booking details bike_id ',
            bikes[0].bike_id,
            ' with error:',
            error
          )
          return callback(errors.internalServer(false))
        }
        if (booking.length === 0) {
          logger(
            'Error: Booking details not found for bike_id',
            bikes[0].bike_id,
            'on theft report with error:',
            error
          )
          return callback(errors.resourceNotFound(false))
        }
        tripHandler.endTripOnTheftReport(
          _.pick(booking[0], 'booking_id', 'trip_id'),
          (error) => {
            if (error) {
              Sentry.captureException(error, { theftDetails })
              logger(
                'Error: could not update the trip end while theft report :',
                bikes[0].bike_id,
                'Failed with error:',
                error
              )
              return callback(error)
            }
            const ticketObject = {
              type: ticketConstants.types.theft,
              name: ticketConstants.category.reportedTheft,
              status: ticketConstants.status.created,
              category: ticketConstants.category.reportedTheft,
              date_created: moment().unix(),
              fleet_id: bikes[0].fleet_id,
              customer_id: bikes[0].customer_id,
              bike_id: bikes[0].bike_id,
              lock_id: bikes[0].lock_id,
              user_id: theftDetails.user_id,
              trip_id: theftDetails.trip_id
            }
            ticketHandler.createTicket(ticketObject, (error) => {
              if (error) {
                Sentry.captureException(error, { theftDetails, ticketObject })
                logger(
                  'Error: could not create tickets for the user with user_id',
                  ticketObject.user_id,
                  ' with ticket parameters: ',
                  ticketObject,
                  'Failed with error',
                  error
                )
                callback(errors.internalServer(false))
                return
              }
              callback(errors.noError())
            })
          }
        )
      }
    )
  })
}

/**
 * The method saves a theft record to the database for a given user
 *
 * @param {Object} theftObject Objects with key/value pairs
 * @param {Function} callback
 */
const insertTheftDetails = (theftObject, callback) => {
  let query = queryCreator.insertSingle(dbConstants.tables.thefts, theftObject)
  sqlPool.makeQuery(query, callback)
}

module.exports = Object.assign(module.exports, {
  createDamageReport: createDamageReport,
  saveMaintenance: saveMaintenance,
  updateMaintenance: updateMaintenance,
  getMaintenance: getMaintenance,
  theftReported: theftReported,
  insertTheftDetails: insertTheftDetails
})
