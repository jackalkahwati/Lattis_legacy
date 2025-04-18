'use strict'

const platform = require('@velo-labs/platform')
const queryCreator = platform.queryCreator
const sqlPool = platform.sqlPool
const logger = platform.logger
const errors = platform.errors
const _ = require('underscore')
const dbConstants = platform.dbConstants
const ticketConstants = require('./../constants/ticket-constants')
const bikeConstants = require('./../constants/bike-constants')
const maintenanceConstants = require('./../constants/maintenance-constants')
const maintenanceHandler = require('./maintenance-handler')
const bikeHandler = require('./../handlers/bike-handler')
const tripHandler = require('./../handlers/trip-handler')
const operatorHandler = require('./../handlers/operator-handler')
const moment = require('moment')
const alertsHandler = require('./alert-handler')
const { configureSentry } = require('../utils/configureSentry')

const Sentry = configureSentry()

/**
 * To get all the tickets for the given parameter
 *
 * @param {Object} comparisonColumnAndValue - parameter to filter tickets
 * @param {Function} callback
 */
const getTicket = (comparisonColumnAndValue, callback) => {
  const maintenanceQueryForTicket = queryCreator.selectWithAnd(
    dbConstants.tables.tickets,
    null,
    comparisonColumnAndValue
  )
  sqlPool.makeQuery(maintenanceQueryForTicket, callback)
}

/**
 * To update the tickets
 *
 * @param {Object} columnsAndValues - columns to update
 * @param {Object} comparisonColumnAndValue - parameter to compare tickets
 * @param {Function} callback
 * @private
 */
const _updateTicket = (
  columnsAndValues,
  comparisonColumnAndValue,
  callback
) => {
  const maintenanceQueryForTicket = queryCreator.updateSingle(
    dbConstants.tables.tickets,
    columnsAndValues,
    comparisonColumnAndValue
  )
  sqlPool.makeQuery(maintenanceQueryForTicket, callback)
}

/**
 * This methods fetches tickets that are associated with an request identifier.
 *
 * @param {Object} requestParam - Request parameter for identifier.
 * @param {Function} callback - callback
 */
const getTickets = (requestParam, callback) => {
  let comparisonValues = {
    status: [ticketConstants.status.created, ticketConstants.status.assigned]
  }
  if (requestParam.fleet_id) {
    comparisonValues.fleet_id = requestParam.fleet_id
  } else {
    comparisonValues.operator_id = requestParam.operator_id
  }
  _getTickets(comparisonValues, (error, tickets) => {
    if (error) {
      Sentry.captureException(error, { comparisonValues })
      logger('Error: getting tickets', errors.errorWithMessage(error))
      callback(errors.internalServer(false), null)
      return
    }
    const ticketsLength = tickets.length
    const maintenanceTickets = []
    const crashTickets = []
    const theftTickets = []
    const tripTickets = []
    const otherTickets = []
    let ticket
    for (let i = 0; i < ticketsLength; i++) {
      ticket = tickets[i]
      if (ticket.type === ticketConstants.types.maintenance) {
        maintenanceTickets.push(ticket)
      } else if (ticket.type === ticketConstants.types.crash) {
        crashTickets.push(ticket)
      } else if (ticket.type === ticketConstants.types.theft) {
        theftTickets.push(ticket)
      } else if (ticket.type === ticketConstants.types.trip) {
        tripTickets.push(ticket)
      } else {
        otherTickets.push(ticket)
      }
    }
    getMaintenanceDataForTicket(
      maintenanceTickets,
      (error, maintenanceTicketData) => {
        if (error) {
          Sentry.captureException(error, { maintenanceTickets })
          logger('Error: getting tickets', errors.errorWithMessage(error))
          callback(errors.internalServer(false), null)
        }
        getCrashDataForTicket(crashTickets, (error, crashTicketData) => {
          if (error) {
            Sentry.captureException(error, { crashTickets })
            logger('Error: getting tickets', errors.errorWithMessage(error))
            callback(errors.internalServer(false), null)
          }
          getTheftDataForTicket(theftTickets, (error, theftTicketData) => {
            if (error) {
              Sentry.captureException(error, { theftTickets })
              logger('Error: getting tickets', errors.errorWithMessage(error))
              callback(errors.internalServer(false), null)
            }
            getTripDataForTicket(tripTickets, (error, tripTicketData) => {
              if (error) {
                Sentry.captureException(error, { tripTickets })
                logger(
                  'Error: getting tickets',
                  errors.errorWithMessage(error)
                )
                callback(errors.internalServer(false), null)
              }
              for (let i = 0; i < otherTickets.length; i++) {
                otherTickets[i] = _formatTicket(otherTickets[i], {}, {}, {})
              }
              const ticketResults = otherTickets.concat(
                maintenanceTicketData,
                crashTicketData,
                theftTicketData,
                tripTicketData
              )
              callback(errors.noError(), ticketResults)
            })
          })
        })
      }
    )
  })
}

/**
 * Used to get crashData for ticket
 *
 * @param {Object} crashTickets - requestParam for ticket.
 * @param callback - Callback
 */
const getCrashDataForTicket = (crashTickets, callback) => {
  const crashTicketsLength = crashTickets.length
  if (crashTicketsLength === 0) {
    return callback(errors.noError(), crashTickets)
  }
  const crashQuery = queryCreator.selectWithAnd(
    dbConstants.tables.crashes,
    null,
    { crash_id: _.uniq(_.pluck(crashTickets, 'type_id')) }
  )
  sqlPool.makeQuery(crashQuery, (error, crashData) => {
    if (error) {
      Sentry.captureException(error, { crashTickets, crashQuery })
      callback(errors.internalServer(false), null)
      return
    }
    for (let i = 0; i < crashTicketsLength; i++) {
      crashTickets[i] = _formatTicket(
        crashTickets[i],
        _.findWhere(crashData, { crash_id: crashTickets[i].type_id }),
        {},
        {}
      )
    }
    callback(errors.noError(), crashTickets)
  })
}

/**
 * Used to get tripData for ticket
 *
 * @param {Object} tripTickets - requestParam for ticket.
 * @param callback - Callback
 */
const getTripDataForTicket = (tripTickets, callback) => {
  const tripTicketsLength = tripTickets.length
  if (tripTicketsLength === 0) {
    return callback(errors.noError(), tripTickets)
  }
  const tripQuery = queryCreator.selectWithAnd(dbConstants.tables.trips, null, {
    trip_id: _.uniq(_.pluck(tripTickets, 'type_id'))
  })
  sqlPool.makeQuery(tripQuery, (error, tripData) => {
    if (error) {
      Sentry.captureException(error, { tripTickets, tripQuery })
      callback(errors.internalServer(false), null)
      return
    }
    let bikeIds = []
    let lockIds = []
    let bikeComparisonColumns = []
    let lockComparisonColumns = []
    _.each(tripData, (trip) => {
      bikeIds.push(trip.bike_id)
      bikeComparisonColumns.push('bike_id')
      lockComparisonColumns.push('lock_id')
      lockIds.push(trip.lock_id)
    })

    if (
      !bikeComparisonColumns.length ||
      !bikeIds.length ||
      !lockComparisonColumns.length ||
      !lockIds.length
    ) {
      callback(null, [])
      return
    }
    const bikesQuery = queryCreator.selectWithOr(
      dbConstants.tables.bikes,
      [],
      bikeComparisonColumns,
      bikeIds
    )
    sqlPool.makeQuery(bikesQuery, (error, bikesData) => {
      if (error) {
        Sentry.captureException(error, { tripTickets, bikesQuery })
        callback(errors.internalServer(true), null)
        return
      }
      const locksQuery = queryCreator.selectWithOr(
        dbConstants.tables.locks,
        [],
        lockComparisonColumns,
        lockIds
      )
      sqlPool.makeQuery(locksQuery, (error, locksData) => {
        if (error) {
          Sentry.captureException(error, { tripTickets, locksQuery })
          callback(errors.internalServer(true), null)
          return
        }
        let tripTicket
        let trip
        let bike
        let lock
        for (let i = 0; i < tripTicketsLength; i++) {
          bike = null
          lock = null
          tripTicket = tripTickets[i]
          trip = _.findWhere(tripData, { trip_id: tripTicket.type_id })
          if (trip) {
            bike = _.findWhere(bikesData, { bike_id: trip.bike_id })
            lock = _.findWhere(locksData, { lock_id: trip.lock_id })
            if (bike && !lock) {
              lock = _.findWhere(locksData, { lock_id: bike.lock_id })
            }
          }
          tripTickets[i] = _formatTicket(tripTicket, trip, bike, lock)
        }
        callback(errors.noError(), tripTickets)
      })
    })
  })
}

/**
 * Used to get MaintenanceData for ticket
 *
 * @param {Object} maintenanceTickets - requestParam for ticket.
 * @param callback - Callback
 */
const getMaintenanceDataForTicket = (maintenanceTickets, callback) => {
  const maintenanceTicketsLength = maintenanceTickets.length
  if (maintenanceTicketsLength === 0) {
    return callback(errors.noError(), maintenanceTickets)
  }
  const maintenanceQuery = queryCreator.selectWithAnd(
    dbConstants.tables.maintenance,
    null,
    { maintenance_id: _.uniq(_.pluck(maintenanceTickets, 'type_id')) }
  )
  sqlPool.makeQuery(maintenanceQuery, (error, maintenanceData) => {
    if (error) {
      Sentry.captureException(error, { maintenanceTickets, maintenanceQuery })
      logger(
        'Error: getting maintenance rows for tickets',
        errors.errorWithMessage(error)
      )
      callback(errors.internalServer(false), null)
      return
    }
    const bikeComparisonColumns = []
    const bikeComparisonValues = []
    const lockComparisonColumns = []
    const lockComparisonValues = []
    _.each(maintenanceData, (maintenance) => {
      bikeComparisonColumns.push(['bike_id'])
      lockComparisonColumns.push(['lock_id'])
      lockComparisonValues.push(maintenance.lock_id)
      bikeComparisonValues.push(maintenance.bike_id)
    })
    const bikesQuery = queryCreator.selectWithOr(
      dbConstants.tables.bikes,
      [],
      bikeComparisonColumns,
      bikeComparisonValues
    )
    sqlPool.makeQuery(bikesQuery, (error, bikeData) => {
      if (error) {
        Sentry.captureException(error, { maintenanceTickets, bikesQuery })
        logger('Error: getting bikes', errors.errorWithMessage(error))
        callback(errors.internalServer(true), null)
        return
      }
      const locksQuery = queryCreator.selectWithOr(
        dbConstants.tables.locks,
        [],
        lockComparisonColumns,
        lockComparisonValues
      )
      sqlPool.makeQuery(locksQuery, (error, lockData) => {
        if (error) {
          Sentry.captureException(error, { maintenanceTickets, locksQuery })
          logger('Error: getting locks', errors.errorWithMessage(error))
          callback(errors.internalServer(true), null)
          return
        }
        let lock
        let bike
        let maintenance
        let maintenanceTicket
        for (let i = 0; i < maintenanceTicketsLength; i++) {
          lock = null
          maintenanceTicket = maintenanceTickets[i]
          maintenance = _.findWhere(maintenanceData, {
            maintenance_id: maintenanceTicket.type_id
          })
          if (maintenance) {
            maintenance.maintenance_status = maintenance.status
            if (maintenance.lock_id) {
              lock = _.findWhere(lockData, { lock_id: maintenance.lock_id })
            }
            bike = _.findWhere(bikeData, {
              bike_id: maintenanceTicket.bike_id
            })
            if (bike && !lock) {
              lock = _.findWhere(lockData, { lock_id: bike.lock_id })
            }
          }
          maintenanceTickets[i] = _formatTicket(
            maintenanceTicket,
            maintenance,
            bike,
            lock
          )
        }
        callback(errors.noError(), maintenanceTickets)
      })
    })
  })
}

/**
 * Used to get theftData for ticket
 *
 * @param {Object} theftTickets - requestParam for ticket.
 * @param callback - Callback
 */
const getTheftDataForTicket = (theftTickets, callback) => {
  const theftTicketsLength = theftTickets.length
  if (theftTicketsLength === 0) {
    return callback(errors.noError(), theftTickets)
  }
  const theftQuery = queryCreator.selectWithAnd(
    dbConstants.tables.thefts,
    null,
    { theft_id: _.uniq(_.pluck(theftTickets, 'type_id')) }
  )
  sqlPool.makeQuery(theftQuery, (error, theftData) => {
    if (error) {
      Sentry.captureException(error, { theftTickets, theftQuery })
      logger('Error: getting theft', errors.errorWithMessage(error))
      return callback(errors.internalServer(false), null)
    }
    const bikesQuery = queryCreator.selectWithOr(
      dbConstants.tables.bikes,
      [],
      ['bike_id'],
      _.uniq(_.pluck(theftData, 'bike_id'))
    )
    sqlPool.makeQuery(bikesQuery, (error, bikeData) => {
      if (error) {
        Sentry.captureException(error, { theftTickets, bikesQuery })
        logger('Error: getting bikes', errors.errorWithMessage(error))
        return callback(errors.internalServer(true), null)
      }
      const locksQuery = queryCreator.selectWithOr(
        dbConstants.tables.locks,
        [],
        ['lock_id'],
        _.uniq(_.pluck(bikeData, 'lock_id'))
      )
      sqlPool.makeQuery(locksQuery, (error, lockData) => {
        if (error) {
          Sentry.captureException(error, { theftTickets, locksQuery })
          logger('Error: getting locks', errors.errorWithMessage(error))
          return callback(errors.internalServer(true), null)
        }
        let bike
        let lock
        let theft
        let theftTicket
        for (let i = 0; i < theftTicketsLength; i++) {
          lock = null
          bike = null
          theftTicket = theftTickets[i]
          theft = _.findWhere(theftData, { theft_id: theftTicket.type_id })
          if (theft) {
            bike = _.findWhere(bikeData, { bike_id: theftTicket.bike_id })
            if (bike) {
              lock = _.findWhere(lockData, { lock_id: bike.lock_id })
            }
          }
          theftTickets[i] = _formatTicket(theftTicket, theft, bike, lock)
        }
        callback(errors.noError(), theftTickets)
      })
    })
  })
}

/**
 * To format ticket for wire
 *
 * @param {Object} ticket - ticket element
 * @param {Object} typeElement - element of specific type
 * @param {Object} bike - bike element
 * @param {Object} lock - lock element
 * @returns {Object} formatted ticket
 */
const _formatTicket = (ticket, typeElement, bike, lock) => {
  ticket = Object.assign(ticket, {
    ticket_status: ticket.status,
    ticket_date_created: ticket.date_created
  })
  delete ticket.status
  if (bike) {
    bike = Object.assign(bike, {
      bike_status: bike.status,
      bike_current_status: bike.current_status,
      bike_date_created: bike.date_created
    })
  }
  if (lock) {
    lock.lock_name = lock.name
    delete lock.fleet_id
  }
  typeElement = typeElement || {}
  bike = bike || {}
  lock = lock || {}
  return _.omit(Object.assign(ticket, typeElement, bike, lock),
    'key', 'name', 'user_id', 'status', 'mac_id', 'date_created', 'battery_level_log', 'jam_counter')
}

/**
 * This methods will insert ticket with given columnAndValues.
 *
 * @param {Object} columnsAndValues - Parameters to create ticket.
 * @param {Function} callback
 */
const insertTicket = (columnsAndValues, callback) => {
  let ticketsQuery = queryCreator.insertSingle(dbConstants.tables.tickets, columnsAndValues)
  sqlPool.makeQuery(ticketsQuery, callback)
}

/**
 * This methods will create ticket with given columnAndValues.
 *
 * @param {Object} requestParam - Parameters to create ticket.
 * @param {Function} callback
 */
const createTicket = (requestParam, callback) => {
  const now = moment().unix()
  const ticketColumns = {
    date_created: now,
    status: ticketConstants.status.created,
    priority: requestParam.priority || ticketConstants.priority.medium,
    ticket_status: ticketConstants.ticket_status.open,
    category: requestParam.category,
    bike_id: requestParam.bike_id,
    fleet_id: requestParam.fleet_id,
    trip_id: requestParam.trip_id,
    customer_id: requestParam.customer_id,
    rider_notes: requestParam.rider_notes || null,
    operator_notes: requestParam.operator_notes || null,
    maintenance_notes: requestParam.maintenance_notes || null
  }
  if (requestParam.operator_id) {
    ticketColumns.operator_id = requestParam.operator_id
  }
  if (
    requestParam.category !== ticketConstants.category.parkingOutsideGeofence
  ) {
    if (
      requestParam.category === ticketConstants.category.damageReported ||
      requestParam.category === ticketConstants.category.serviceDue ||
      requestParam.category === ticketConstants.category.issueDetected
    ) {
      const maintenanceColumns = {
        category: requestParam.maintenance_category
          ? requestParam.maintenance_category
          : requestParam.category,
        user_photo: requestParam.user_photo || null,
        operator_photo: requestParam.operator_photo || null,
        bike_id: requestParam.bike_id,
        lock_id: requestParam.lock_id,
        fleet_id: requestParam.fleet_id,
        customer_id: requestParam.customer_id,
        service_start_date: now,
        date_created: now,
        reported_by_user_id: requestParam.reported_by_user_id,
        trip_id: requestParam.trip_id
      }
      if (requestParam.operator_id) {
        maintenanceColumns.operator_id = requestParam.operator_id
      }
      let maintenanceStatus; // eslint-disable-line
      if (
        requestParam.category === maintenanceConstants.category.damageReported
      ) {
        maintenanceColumns.status =
          maintenanceConstants.category.damageReported
        maintenanceStatus = maintenanceConstants.category.damageReported
        if (requestParam.reported_by_operator_id) {
          maintenanceColumns.reported_by_operator_id =
            requestParam.reported_by_operator_id
        }
      } else if (
        requestParam.category === maintenanceConstants.category.serviceDue
      ) {
        maintenanceColumns.status = maintenanceConstants.status.maintenanceDue
        maintenanceStatus = maintenanceConstants.category.serviceDue
      } else if (
        requestParam.category === maintenanceConstants.category.issueDetected
      ) {
        maintenanceColumns.status = maintenanceConstants.status.issueDetected
        maintenanceStatus = maintenanceConstants.category.issueDetected
      }

      maintenanceHandler.saveMaintenance(
        maintenanceColumns,
        (error, status) => {
          if (error) {
            Sentry.captureException(error, { maintenanceColumns })
            logger(
              'Error: Creating ticket. Unable to save maintenance record:',
              errors.errorWithMessage(error)
            )
            callback(error)
            return
          }
          insertTicket(
            _.extend(ticketColumns, {
              type: ticketConstants.types.maintenance,
              type_id: status.insertId
            }),
            (error) => {
              if (error) {
                Sentry.captureException(error, { ticketColumns })
                logger(
                  'Error: Creating ticket. Unable to insert ticket record:',
                  errors.errorWithMessage(error)
                )
                callback(error)
                return
              }
              tripHandler.getTrips(
                { bike_id: requestParam.bike_id },
                true,
                (error, trips) => {
                  if (error) {
                    Sentry.captureException(error, {
                      bike_id: requestParam.bike_id
                    })
                    logger(
                      'Error: Creating ticket. Unable to update bike record:',
                      errors.errorWithMessage(error)
                    )
                    callback(error)
                    return
                  }
                  const activeTrip = trips.find((trip) => !trip.date_endtrip)
                  if (!activeTrip) {
                    bikeHandler.updateBike(
                      null,
                      {
                        maintenance_status:
                          bikeConstants.maintenance_status.fieldMaintenance,
                        bike_id: requestParam.bike_id,
                        status: bikeConstants.status.suspended,
                        current_status: bikeConstants.current_status.damaged
                      },
                      (error) => {
                        if (error) {
                          Sentry.captureException(error, {
                            bike_id: requestParam.bike_id
                          })
                          logger(
                            'Error: Creating ticket. Unable to update bike record:',
                            errors.errorWithMessage(error)
                          )
                          callback(error)
                          return
                        }
                        alertsHandler.sendEmailNotifications(
                          _.pick(requestParam, 'operator_id', 'fleet_id'),
                          requestParam.category,
                          (error) => {
                            if (error) {
                              Sentry.captureException(error, { requestParam })
                              callback(null)
                              return
                            }
                            callback(null)
                          }
                        )
                      }
                    )
                  } else {
                    alertsHandler.sendEmailNotifications(
                      _.pick(requestParam, 'operator_id', 'fleet_id'),
                      requestParam.category,
                      (error) => {
                        if (error) {
                          Sentry.captureException(error, { requestParam })
                          callback(null)
                          return
                        }
                        callback(null)
                      }
                    )
                  }
                }
              )
            }
          )
        }
      )
    } else if (
      requestParam.category === ticketConstants.category.reportedTheft
    ) {
      const theftObject = {
        confirmed: '1',
        bike_id: requestParam.bike_id,
        lock_id: requestParam.lock_id,
        user_id: requestParam.user_id,
        trip_id: requestParam.trip_id,
        date: moment().unix()
      }
      maintenanceHandler.insertTheftDetails(
        theftObject,
        (error, theftStatus) => {
          if (error) {
            Sentry.captureException(error, { theftObject })
            logger(
              'Error: Creating ticket. Unable to save theft record with bike_id ',
              requestParam.bike_id,
              errors.errorWithMessage(error)
            )
            callback(errors.internalServer(false))
            return
          }
          insertTicket(
            _.extend(ticketColumns, {
              type: ticketConstants.types.theft,
              type_id: theftStatus.insertId
            }),
            (error) => {
              if (error) {
                Sentry.captureException(error, { ticketColumns })
                logger(
                  'Error: Creating ticket. Unable to insert ticket record:',
                  errors.errorWithMessage(error)
                )
                callback(error)
                return
              }

              bikeHandler.updateBike(
                null,
                {
                  bike_id: requestParam.bike_id,
                  maintenance_status:
                    maintenanceConstants.category.reportedTheft,
                  status: bikeConstants.status.suspended,
                  current_status: bikeConstants.current_status.reportedStolen
                },
                (error) => {
                  if (error) {
                    Sentry.captureException(error, {
                      bike_id: requestParam.bike_id
                    })
                    logger(
                      'Error: Creating ticket. Unable to update bike record:',
                      errors.errorWithMessage(error)
                    )
                    callback(error)
                    return
                  }
                  alertsHandler.sendEmailNotifications(
                    { fleet_id: requestParam.fleet_id },
                    requestParam.category,
                    (error) => {
                      if (error) {
                        Sentry.captureException(error, { requestParam })
                        callback(null)
                        return
                      }
                      callback(null)
                    }
                  )
                }
              )
            }
          )
        }
      )
    } else {
      logger(
        'Error: Creating ticket. Wrong ticket category',
        requestParam.category
      )
      callback(errors.missingParameter(false))
    }
  } else {
    tripHandler.getTrips(
      { bike_id: requestParam.bike_id },
      false,
      (error, trips) => {
        if (error) {
          Sentry.captureException(error, { bike_id: requestParam.bike_id })
          logger('Error Getting trips:' + error)
          callback(error)
          return
        }
        let trip = trips.find((trip) => trip.trip_id === requestParam.trip_id)
        tripHandler.updateTrip(
          null,
          Object.assign(_.pick(requestParam, 'parking_image'), {
            trip_id: trip.trip_id
          }),
          (error) => {
            if (error) {
              Sentry.captureException(error, { requestParam })
              logger('Error Saving Trip:' + error)
              callback(error)
            }
            insertTicket(
              _.extend(ticketColumns, {
                type: ticketConstants.types.trip,
                type_id: trip.trip_id
              }),
              (error) => {
                if (error) {
                  Sentry.captureException(error, { ticketColumns })
                  logger('Error Saving Ticket:' + error)
                  callback(error)
                  return
                }
                bikeHandler.updateBike(
                  null,
                  {
                    maintenance_status:
                      bikeConstants.maintenance_status.parkedOutsideGeofence,
                    bike_id: requestParam.bike_id
                  },
                  (error) => {
                    if (error) {
                      Sentry.captureException(error, {
                        bike_id: requestParam.bike_id
                      })
                      logger('Error Updating bike:' + error)
                      callback(error)
                      return
                    }
                    alertsHandler.sendEmailNotifications(
                      { fleet_id: requestParam.fleet_id },
                      requestParam.category,
                      (error) => {
                        if (error) {
                          Sentry.captureException(error, { requestParam })
                          callback(null)
                          return
                        }
                        callback(null)
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
}

/**
 * This methods validates a new ticket object.
 *
 * @param {Object} ticketData
 * @returns {boolean}
 */
const validateTicket = (ticketData) => {
  return (
    _.has(ticketData, 'fleet_id') &&
    _.has(ticketData, 'bike_id') &&
    _.has(ticketData, 'lock_id') &&
    _.has(ticketData, 'category') &&
    (_.has(ticketData, 'maintenance_notes') ||
      _.has(ticketData, 'operator_notes'))
  )
}

/**
 * This methods will resolve ticket with given columnAndValues.
 *
 * @param {Object} requestParam - Parameters to resolve ticket.
 * @param {Function} callback
 */
const resolveTicket = (requestParam, callback) => {
  if (!Array.isArray(requestParam.ticket_id)) {
    requestParam.ticket_id = [requestParam.ticket_id]
  }
  const now = moment().unix()
  getTicket({ ticket_id: _.uniq(requestParam.ticket_id) }, (error, tickets) => {
    if (error) {
      Sentry.captureException(error, { ticket_id: requestParam.ticket_id })
      logger(
        'Error: Resolving ticket with ticket_id:',
        requestParam.ticket_id,
        error
      )
      callback(errors.internalServer(false))
      return
    }
    const ticketsLength = tickets.length
    if (ticketsLength === 0) {
      callback(errors.noError())
      return
    }
    const columnsAndValues = {
      date_resolved: now,
      status: ticketConstants.status.resolved,
      ticket_status: ticketConstants.ticket_status.closed
    }
    _updateTicket(
      columnsAndValues,
      { ticket_id: _.uniq(_.pluck(tickets, 'ticket_id')) },
      (error) => {
        if (error) {
          Sentry.captureException(error, { columnsAndValues })
          logger(
            'Error: Resolving ticket with ticket_id:',
            _.uniq(_.pluck(tickets, 'ticket_id')),
            error
          )
          callback(errors.internalServer(false))
          return
        }
        const maintenanceTickets = _.filter(tickets, {
          type: ticketConstants.types.maintenance
        })
        if (maintenanceTickets.length === 0) {
          callback(errors.noError())
          return
        }
        maintenanceHandler.updateMaintenance(
          { service_end_date: now },
          { maintenance_id: _.uniq(_.pluck(maintenanceTickets, 'type_id')) },
          (error) => {
            if (error) {
              Sentry.captureException(error, {
                maintenance_id: _.uniq(_.pluck(maintenanceTickets, 'type_id'))
              })
              logger(
                'Error: Resolving ticket with ticket_id:',
                _.uniq(_.pluck(tickets, 'ticket_id')),
                'Unable to update maintenance',
                errors.errorWithMessage(error)
              )
              callback(errors.internalServer(false))
              return
            }
            callback(errors.noError())
          }
        )
      }
    )
  })
}

/**
 * This methods will assign the ticket to the operator.
 *
 * @param {Object} ticketInfo - Ticket and operator identifiers.
 * @param {Function} callback
 */
const assignTicket = (ticketInfo, callback) => {
  _getTickets(_.pick(ticketInfo, 'ticket_id'), (error, tickets) => {
    if (error) {
      Sentry.captureException(error, { ticket_id: ticketInfo.ticket_id })
      logger(
        'Error: Resolving ticket with ticket_id:',
        ticketInfo.ticket_id,
        'Unable to get ticket with error: ',
        error
      )
      return callback(errors.internalServer(true), null)
    }
    operatorHandler.getOperatorsByFleets(
      _.pick(tickets[0], 'fleet_id'),
      (error, operators) => {
        if (error) {
          Sentry.captureException(error, { fleet_id: tickets[0].fleet_id })
          logger(
            'Error: Resolving ticket with ticket_id:',
            ticketInfo.ticket_id,
            'Unable to get fleet operators with error: ',
            error
          )
          return callback(errors.internalServer(true), null)
        }
        if (!_.findWhere(operators, { operator_id: ticketInfo.operator_id })) {
          logger(
            'The operator ',
            ticketInfo.operator_id,
            ' does not have access to the fleet',
            tickets[0].fleet_id
          )
          callback(errors.unauthorizedAccess(false), null)
          return
        }
        const columnsAndValues = {
          assignee: ticketInfo.operator_id,
          status: ticketConstants.status.assigned,
          date_assigned: moment().unix()
        }
        if (!Array.isArray(ticketInfo.ticket_id)) {
          ticketInfo.ticket_id = [ticketInfo.ticket_id]
        }
        _.each(ticketInfo.ticket_id, (ticketId, index) => {
          const updateTicketQuery = queryCreator.updateSingle(
            dbConstants.tables.tickets,
            columnsAndValues,
            { ticket_id: ticketId }
          )
          sqlPool.makeQuery(updateTicketQuery, (error) => {
            if (error) {
              Sentry.captureException(error, { ticketId })
              logger(
                'Error: Resolving ticket with ticket_id:',
                ticketInfo.ticket_id,
                error
              )
              return callback(errors.internalServer(true), null)
            }
            if (index === ticketInfo.ticket_id.length - 1) {
              return callback(null, null)
            }
          })
        })
      }
    )
  })
}

/**
 * This methods will update ticket status (open / closed / inprogress) with given columnAndValues.
 *
 * @param {Object} requestParam - Parameters to update the ticket.
 * @param {Function} callback
 */
const statusUpdate = (requestParam, callback) => {
  // eslint-disable-next-line camelcase
  const {ticket_id, fleet_id, operator_id, ticket_status} = requestParam
  if (ticketConstants.ticket_status.hasOwnProperty(ticket_status)) {
    getTicket({ticket_id, operator_id, fleet_id}, (error, tickets) => {
      if (error) {
        Sentry.captureException(error, {ticket_id})
        logger(
          'Error: Updating ticket status with ticket_id:',
          ticket_id,
          error
        )
        callback(errors.internalServer(false))
        return
      }
      const ticketsLength = tickets.length
      if (ticketsLength === 0) {
        callback(errors.noError())
        return
      }
      const columnsAndValues = {
        status: ticket_status
      }
      _updateTicket(
        columnsAndValues,
        {ticket_id},
        (error) => {
          if (error) {
            Sentry.captureException(error, {columnsAndValues})
            logger(
              'Error: Updating ticket status with ticket_id:',
              _.uniq(_.pluck(tickets, 'ticket_id')),
              error
            )
            callback(errors.internalServer(false))
          } else {
            callback(errors.noError())
          }
        }
      )
    })
  } else {
    logger(
      'Error: Passed unknown ticket status with ticket_id:',
      _.uniq(_.pluck(requestParam, 'ticket_id'))
    )
    callback(errors.internalServer(false))
  }
}

/**

/**
 * This methods will get ticket by given params.
 *
 * @param {Object} columnAndValues - Ticket and operator identifiers.
 * @param {Function} callback
 * @private
 */
const _getTickets = (columnAndValues, callback) => {
  const getTicketsQuery = queryCreator.selectWithAnd(dbConstants.tables.tickets, null, columnAndValues)
  sqlPool.makeQuery(getTicketsQuery, callback)
}

module.exports = Object.assign(module.exports, {
  insertTicket: insertTicket,
  getTicket: getTicket,
  getTickets: getTickets,
  createTicket: createTicket,
  validateTicket: validateTicket,
  resolveTicket: resolveTicket,
  assignTicket: assignTicket,
  statusUpdate: statusUpdate
})
