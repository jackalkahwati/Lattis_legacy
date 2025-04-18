'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const query = platform.queryCreator
const errors = platform.errors
const sqlPool = platform.sqlPool
const _ = require('underscore')
const dbConstants = platform.dbConstants
const ticketConstants = require('./../constants/ticket-constants')
const maintenanceConstants = require('./../constants/maintenance-constants')
const bikeConstants = require('./../constants/bike-constants')
const platformConfig = platform.config
const bikeFleetHandler = require('./bike-fleet-handler')
const maintenanceHandler = require('./maintenance-handler')
const crashHandler = require('./crash-handler')
const theftHandler = require('./theft-handler')
const tripHandler = require('./trip-handler')
const alertHandler = require('./alerts-handler')
const operatorHandler = require('./operator-handler')
const passwordHandler = require('./../utils/password-handler')
const moment = require('moment')
const async = require('async')
const uploadHandler = require('./upload-handler')
const uploadConstants = require('./../constants/upload-constants')
const db = require('../db')
const Sentry = require('../utils/configureSentry')

/**
 * Used to get tickets.
 *
 * @param {Object} columnsAndValues - columnsAndValues for maintenance.
 * @param done - Callback
 */
const getTicket = (columnsAndValues, done) => {
  const getTicketsQuery = query.selectWithAnd(dbConstants.tables.tickets, null, columnsAndValues)
  sqlPool.makeQuery(getTicketsQuery, done)
}

/**
 * Used to add tickets in the tickets table.
 *
 * @param {Object} columnsAndValues - columnsAndValues for maintenance.
 * @param done - Callback
 */
const insertTicket = (columnsAndValues, done) => {
  const insertTicketQuery = query.insertSingle(dbConstants.tables.tickets, columnsAndValues)
  sqlPool.makeQuery(insertTicketQuery, done)
}

/**
 * Used to add tickets in the tickets table.
 *
 * @param {Object} requestParam
 * @param {Object} TicketPic
 * @param {String} url
 * @param done - Callback
 */

const createTicket = async (requestParam, TicketPic, url, done) => {
  const now = moment().unix()
  const ticketColumns = {
    operator_notes: requestParam.notes,
    date_created: now,
    status: ticketConstants.status.created,
    priority: requestParam.priority || ticketConstants.priority.medium,
    category: requestParam.category,
    fleet_id: requestParam.fleet_id,
    operator_id: requestParam.operator_id,
    customer_id: requestParam.customer_id,
    bike_id: requestParam.bike_id
  }
  if (requestParam.category !== maintenanceConstants.category.parkingOutsideGeofence) {
    let maintenanceStatus
    let bikeStatus
    let bikeMaintenanceStatus
    let bikeCurrentStatus
    if (requestParam.category === maintenanceConstants.category.damageReported) {
      maintenanceStatus = maintenanceConstants.category.damageReported
      bikeStatus = bikeConstants.status.suspended
      bikeMaintenanceStatus = bikeConstants.maintenance_status.fieldMaintenance
      bikeCurrentStatus = bikeConstants.current_status.damaged
    } else if (requestParam.category === maintenanceConstants.category.reportedTheft) {
      maintenanceStatus = maintenanceConstants.category.reportedTheft
      bikeStatus = bikeConstants.status.suspended
      bikeMaintenanceStatus = bikeConstants.maintenance_status.shopMaintenance
      bikeCurrentStatus = bikeConstants.current_status.reportedStolen
    } else if (requestParam.category === maintenanceConstants.category.serviceDue) {
      maintenanceStatus = maintenanceConstants.category.serviceDue
      bikeStatus = bikeConstants.status.suspended
      bikeMaintenanceStatus = bikeConstants.maintenance_status.shopMaintenance
      bikeCurrentStatus = bikeConstants.current_status.underMaintenance
    } else if (requestParam.category === maintenanceConstants.category.potentialTheft) {
      maintenanceStatus = maintenanceConstants.category.reportedTheft
      bikeStatus = bikeConstants.status.suspended
      bikeMaintenanceStatus = bikeConstants.maintenance_status.shopMaintenance
      bikeCurrentStatus = bikeConstants.current_status.reportedStolen
    } else if (requestParam.category === maintenanceConstants.category.depleted_battery) {
      maintenanceStatus = maintenanceConstants.category.maintenanceDue
      bikeStatus = bikeConstants.status.inactive
      bikeMaintenanceStatus = bikeConstants.maintenance_status.shopMaintenance
      bikeCurrentStatus = bikeConstants.current_status.underMaintenance
    }
    const maintenanceColumns = {
      status: maintenanceStatus,
      category: requestParam.category,
      date_created: now,
      service_start_date: now,
      bike_id: requestParam.bike_id,
      lock_id: requestParam.lock_id,
      fleet_id: requestParam.fleet_id,
      operator_id: requestParam.operator_id,
      customer_id: requestParam.customer_id,
      reported_by_user_id: requestParam.reported_by_user_id,
      reported_by_operator_id: requestParam.reported_by_operator_id
    }
    if (requestParam.category === ticketConstants.category.other) {
      insertTicket(_.extend(ticketColumns, { type_id: null, type: null }), (error) => {
        if (error) {
          Sentry.captureException(error, {requestParam})
          logger('Error Saving Ticket:' + error)
          done(errors.internalServer(true), null)
          return
        }
        alertHandler.sendEmailNotifications(requestParam, requestParam.category, (error) => {
          if (error) {
            Sentry.captureException(error, {requestParam})
            logger('Error updating Bike:', errors.errorWithMessage(error))
            done(null, null)
          }
          done(null, null)
        })
      })
    } else if (requestParam.category === maintenanceConstants.category.damageReported) {
      uploadHandler.uploadPic(TicketPic,
        uploadConstants.types.damage_report + '-' + requestParam.fleet_id + '-' + passwordHandler.randString(10),
        platformConfig.aws.s3.damageReports.bucket, (error, imageData) => {
          if (error) {
            Sentry.captureException(error, {requestParam})
            done(error, null)
            return
          }
          bikeFleetHandler.insertMaintenance(_.extend(maintenanceColumns, { operator_photo: imageData }), (error, status) => {
            if (error) {
              Sentry.captureException(error, {requestParam})
              logger('Error Saving maintenance:', errors.errorWithMessage(error))
              done(errors.internalServer(true), null)
              return
            }
            insertTicket(_.extend(ticketColumns, {
              type: ticketConstants.types.maintenance,
              type_id: status.insertId
            }), (error) => {
              if (error) {
                Sentry.captureException(error, {requestParam, ticketColumns})
                logger('Error Saving Ticket:' + error)
                done(errors.internalServer(true), null)
                return
              }
              bikeFleetHandler.updateBike({
                status: bikeStatus,
                current_status: bikeCurrentStatus,
                maintenance_status: bikeMaintenanceStatus
              }, { bike_id: requestParam.bike_id }, (error) => {
                if (error) {
                  Sentry.captureException(error, {requestParam, bikeStatus, bikeCurrentStatus, bikeMaintenanceStatus})
                  logger('Error updating Bike:', errors.errorWithMessage(error))
                  done(errors.internalServer(true), null)
                  return
                }
                alertHandler.sendEmailNotifications(requestParam, requestParam.category, (error) => {
                  if (error) {
                    logger('Error updating Bike:', errors.errorWithMessage(error))
                    return done(null, null)
                  }
                  done(null, null)
                })
              })
            })
          })
        })
    } else if (requestParam.category === maintenanceConstants.category.reportedTheft) {
      const theftObject = {
        confirmed: '1',
        lock_id: requestParam.lock_id,
        bike_id: requestParam.bike_id,
        date: moment().unix()
      }
      maintenanceHandler.insertTheft(theftObject, (error, theftStatus) => {
        if (error) {
          Sentry.captureException(error, {requestParam, theftObject})
          logger('Error: Failed to create ticket. Could not save new theft record with bike_id ', requestParam.bike_id,
            ' Failed with error:', error)
          done(errors.internalServer(false), null)
          return
        }
        insertTicket(_.extend(ticketColumns, {
          type: ticketConstants.types.theft,
          type_id: theftStatus.insertId
        }), (error, ticketData) => {
          if (error) {
            Sentry.captureException(error, {requestParam, ticketColumns})
            logger('Error Saving Ticket:' + error)
            done(errors.internalServer(true), null)
            return
          }
          bikeFleetHandler.updateBike({
            status: bikeStatus,
            current_status: bikeCurrentStatus,
            maintenance_status: bikeMaintenanceStatus
          }, { bike_id: requestParam.bike_id }, (error) => {
            if (error) {
              Sentry.captureException(error, {requestParam, bikeStatus, bikeCurrentStatus, bikeMaintenanceStatus})
              logger('Error updating Bike:', errors.errorWithMessage(error))
              done(errors.internalServer(true), null)
              return
            }
            alertHandler.sendEmailNotifications(requestParam, requestParam.category, (error) => {
              if (error) {
                Sentry.captureException(error, {requestParam})
                logger('Error updating Bike:', errors.errorWithMessage(error))
                return done(error, null)
              }
              done(null, ticketData)
            })
          })
        })
      })
    } else if (requestParam.category === maintenanceConstants.category.potentialTheft) {
      const theftObject = {
        confirmed: 0,
        lock_id: requestParam.lock_id,
        bike_id: requestParam.bike_id,
        date: moment().unix()
      }
      maintenanceHandler.insertTheft(theftObject, (error, theftStatus) => {
        if (error) {
          Sentry.captureException(error, {requestParam, theftObject})
          logger('Error: Failed to create ticket. Could not save new theft record with bike_id ', requestParam.bike_id,
            ' Failed with error:', error)
          done(errors.internalServer(false), null)
          return
        }
        insertTicket(_.extend(ticketColumns, {
          type: ticketConstants.types.theft,
          type_id: theftStatus.insertId
        }), (error, ticketData) => {
          if (error) {
            Sentry.captureException(error, {requestParam, ticketColumns})
            logger('Error Saving Ticket:' + error)
            done(errors.internalServer(true), null)
            return
          }

          alertHandler.sendEmailNotifications(requestParam, requestParam.category, (error) => {
            if (error) {
              Sentry.captureException(error, {requestParam})
              logger('Error updating Bike:', errors.errorWithMessage(error))
              return done(error, null)
            }
            done(null, ticketData)
          })
        })
      })
    } else if (requestParam.category === maintenanceConstants.category.lowBattery) {
      // Battery Low
      try {
        const bike = await db.main('bikes').where({bike_id: requestParam.bike_id}).select('bike_name').first()
        const ticketExists = await db.main('tickets').where({ category: ticketColumns.category, bike_id: ticketColumns.bike_id, status: 'created' }).first()
        ticketColumns.date_created = (requestParam.time / 1000) | 0
        ticketColumns.operator_notes = `Low battery (${requestParam.powerPercent}%) reported on ${bike.bike_name}`
        if (ticketExists) {
          let t = await db.main('tickets').update(ticketColumns).where({ category: ticketColumns.category, bike_id: ticketColumns.bike_id, status: 'created' })
          return done(null, t)
        }
        const [ticketId] = await db.main('tickets').insert(ticketColumns)
        const ticket = await db.main('tickets').where({ticket_id: ticketId}).first()
        done(null, ticket)
      } catch (error) {
        Sentry.captureException(error, {requestParam})
        logger('Error Saving Ticket:' + error)
        return done(error, null)
      }
    } else if (requestParam.category === maintenanceConstants.category.depleted_battery) {
      // Depleted Low
      try {
        const bike = await db.main('bikes').where({bike_id: requestParam.bike_id}).select('bike_name').first()
        const ticketExists = await db.main('tickets').where({ category: ticketColumns.category, bike_id: ticketColumns.bike_id, status: 'created' }).first()
        ticketColumns.date_created = (requestParam.time / 1000) | 0
        ticketColumns.operator_notes = `Depleted battery (${requestParam.powerPercent}%) reported on ${bike.bike_name}`
        if (ticketExists) {
          let t = await db.main('tickets').update(ticketColumns).where({ category: ticketColumns.category, bike_id: ticketColumns.bike_id, status: 'created' })
          return done(null, t)
        }
        const [ticketId] = await db.main('tickets').insert(ticketColumns)
        const ticket = await db.main('tickets').where({ticket_id: ticketId}).first()
        done(null, ticket)
      } catch (error) {
        Sentry.captureException(error, {requestParam})
        logger('Error Saving Ticket:' + error)
        return done(error, null)
      }
    } else {
      bikeFleetHandler.insertMaintenance(maintenanceColumns, (error, status) => {
        if (error) {
          Sentry.captureException(error, {requestParam, maintenanceColumns})
          logger('Error Saving maintenance:', errors.errorWithMessage(error))
          done(errors.internalServer(true), null)
          return
        }
        insertTicket(_.extend(ticketColumns, {
          type: ticketConstants.types.maintenance,
          type_id: status.insertId
        }), (error) => {
          if (error) {
            Sentry.captureException(error, {requestParam, ticketColumns})
            logger('Error Saving Ticket:', errors.errorWithMessage(error))
            done(errors.internalServer(true), null)
            return
          }
          bikeFleetHandler.updateBike({
            status: bikeStatus,
            current_status: bikeCurrentStatus,
            maintenance_status: bikeMaintenanceStatus
          }, { bike_id: requestParam.bike_id }, (error) => {
            if (error) {
              Sentry.captureException(error, {requestParam, bikeStatus, bikeCurrentStatus, bikeMaintenanceStatus})
              logger('Error updating Bike:', errors.errorWithMessage(error))
              done(errors.internalServer(true), null)
              return
            }
            alertHandler.sendEmailNotifications(requestParam, requestParam.category, (error) => {
              if (error) {
                Sentry.captureException(error, {requestParam})
                logger('Error updating Bike:', errors.errorWithMessage(error))
                return done(null, null)
              }
              done(null, null)
            })
          })
        })
      })
    }
  } else {
    uploadHandler.uploadPic(TicketPic,
      uploadConstants.types.ticket + '-' + requestParam.fleet_id + '-' + passwordHandler.randString(10),
      platformConfig.aws.s3.damageReports.bucket, (error, imageData) => {
        if (error) {
          Sentry.captureException(error, {requestParam})
          done(error, null)
          return
        }
        tripHandler.updateTrip({ parking_image: imageData }, { trip_id: requestParam.trip_id }, (error) => {
          if (error) {
            Sentry.captureException(error, {requestParam})
            logger('Error Saving Trip:', errors.errorWithMessage(error))
            done(errors.internalServer(true), null)
            return
          }
          insertTicket(_.extend(ticketColumns, {
            type: ticketConstants.types.trip,
            type_id: requestParam.trip_id
          }), (error) => {
            if (error) {
              Sentry.captureException(error, {requestParam, ticketColumns})
              logger('Error Saving Ticket:', errors.errorWithMessage(error))
              done(errors.internalServer(true), null)
              return
            }
            bikeFleetHandler.updateBike({ maintenance_status: bikeConstants.maintenance_status.parkingOutsideGeofence },
              { bike_id: requestParam.bike_id }, (error) => {
                if (error) {
                  Sentry.captureException(error, {requestParam, bikeConstants})
                  logger('Error Updating bike:', errors.errorWithMessage(error))
                  done(errors.internalServer(true), null)
                  return
                }
                alertHandler.sendEmailNotifications(requestParam, requestParam.category, (error) => {
                  if (error) {
                    Sentry.captureException(error, {requestParam})
                    logger('Error updating Bike:', errors.errorWithMessage(error))
                    return done(null, null)
                  }
                  done(null, null)
                })
              })
          })
        })
      })
  }
}

/**
 * Used to get tickets in the tickets table.
 *
 * @param {Object} properties - properties for ticket.
 * @param done - Callback
 */
const getTickets = (properties, done) => {
  const comparisonCol = { status: [ticketConstants.status.created, ticketConstants.status.assigned] }
  if (properties.resolved) {
    comparisonCol.status.push(ticketConstants.status.resolved)
  }
  if (properties.ticket_id) {
    comparisonCol.ticket_id = properties.ticket_id
  } else if (properties.fleet_id) {
    comparisonCol.fleet_id = properties.fleet_id
  } else if (properties.operator_id) {
    comparisonCol.operator_id = properties.operator_id
  } else if (properties.customer_id) {
    comparisonCol.customer_id = properties.customer_id
  }
  getTicket(comparisonCol, (error, tickets) => {
    if (error) {
      Sentry.captureException(error, {properties})
      logger('Error: getting tickets', errors.errorWithMessage(error))
      done(errors.internalServer(false), null)
      return
    }
    if (tickets.length === 0) {
      done(errors.noError(), tickets)
      return
    }
    bikeFleetHandler.getBikes({ bike_id: _.uniq(_.pluck(tickets, 'bike_id')) }, (error, bikes) => {
      if (error) {
        Sentry.captureException(error, {properties})
        logger('Error: getting tickets. Failed to get bikes with ', _.uniq(_.pluck(tickets, 'bike_id')), errors.errorWithMessage(error))
        done(error, null)
        return
      }
      const maintenanceTickets = []
      const crashTickets = []
      const theftTickets = []
      const tripTickets = []
      const otherTickets = []
      const ticketsLength = tickets.length
      let ticket
      let bike
      for (let i = 0; i < ticketsLength; i++) {
        ticket = tickets[i]
        if (ticket.bike_id) {
          bike = _.findWhere(bikes, { bike_id: ticket.bike_id })
          if (bike) {
            ticket = Object.assign(ticket,
              {
                bike_name: bike.bike_name,
                bike_status: bike.status,
                bike_current_status: bike.current_status,
                bike_maintenance_status: bike.maintenance_status,
                bike_distance_after_service: bike.distance_after_service
              })
          }
        }
        if (ticket.type === ticketConstants.types.maintenance) {
          maintenanceTickets.push(ticket)
        } else if (ticket.type === ticketConstants.types.crash) {
          crashTickets.push(ticket)
        } else if (ticket.type === ticketConstants.types.theft) {
          theftTickets.push(ticket)
        } else if (ticket.type === ticketConstants.types.trip) {
          tripTickets.push(ticket)
        } else {
          otherTickets.push({...ticket, ticket_category: ticket.category, ticket_status: ticket.status})
        }
      }
      const asyncTasks = {
        ticket_maintenance_data: (next) => {
          getMaintenanceDataForTicket(maintenanceTickets, next)
        },
        ticket_crash_data: (next) => {
          getCrashDataForTicket(crashTickets, next)
        },
        ticket_theft_data: (next) => {
          getTheftDataForTicket(theftTickets, next)
        },
        ticket_trip_data: (next) => {
          getTripDataForTicket(tripTickets, next)
        }
      }
      async.parallel(asyncTasks, (err, results) => {
        if (err) {
          return done(errors.internalServer(true), null)
        } else {
          const ticketResults = otherTickets.concat(results.ticket_maintenance_data, results.ticket_crash_data,
            results.ticket_theft_data, results.ticket_trip_data)
          done(null, ticketResults)
        }
      })
    })
  })
}

/**
 * Used to get crashData for ticket
 *
 * @param {Array} tickets - tickets to get crash data for.
 * @param done - Callback
 */
const getCrashDataForTicket = (tickets, done) => {
  const ticketsLength = tickets.length
  if (ticketsLength !== 0) {
    crashHandler.getCrash({ crash_id: _.pluck(tickets, 'type_id') }, (error, crashData) => {
      if (error) {
        Sentry.captureException(error, {tickets})
        done(errors.internalServer(false), null)
        return
      }
      operatorHandler.getOperator({ operator_id: _.pluck(tickets, 'assignee') }, (error, operators) => {
        if (error) {
          Sentry.captureException(error, {tickets})
          logger('Error getting operators with operator id', _.pluck(tickets, 'assignee'), error)
          done(error, null)
          return
        }
        let ticket
        let crash
        let operator
        for (let i = 0; i < ticketsLength; i++) {
          ticket = tickets[i]
          crash = _.findWhere(crashData, { crash_id: ticket.type_id })
          if (ticket.assignee) {
            operator = _.findWhere(operators, { operator_id: ticket.assignee })
            if (operator) {
              ticket = Object.assign(ticket,
                { assignee_name: operator.first_name + ' ' + operator.last_name })
            }
          }
          if (crash) {
            ticket = Object.assign({ ticket_status: ticket.status, ticket_category: ticket.category },
              _.omit(ticket, 'status', 'category'))
            tickets[i] = _formatTicket(ticket, crash)
          }
        }
        done(errors.noError(), tickets)
      })
    })
  } else {
    done(errors.noError(), tickets)
  }
}

/**
 * Used to get tripData for ticket
 *
 * @param {Array} tickets - tickets to get trip data for.
 * @param done - Callback
 */
const getTripDataForTicket = (tickets, done) => {
  const ticketsLength = tickets.length
  if (ticketsLength > 0) {
    tripHandler.getAllTrips({ trip_id: _.pluck(tickets, 'trip_id') }, (error, data) => {
      let tripData = data.trips
      if (error) {
        Sentry.captureException(error, {tickets})
        done(errors.internalServer(false), null)
        return
      }
      operatorHandler.getOperator(null, { operator_id: _.uniq(_.pluck(tickets, 'assignee')) }, (error, operators) => {
        if (error) {
          Sentry.captureException(error, {tickets})
          logger('Error: getting operator for tickets with', error)
          done(errors.internalServer(false), null)
          return
        }
        let ticket
        let trip
        let operator
        for (let i = 0; i < ticketsLength; i++) {
          ticket = tickets[i]
          trip = _.findWhere(tripData, { trip_id: ticket.trip_id })
          if (ticket.assignee) {
            operator = _.findWhere(operators, { operator_id: ticket.assignee })
            if (operator) {
              ticket = Object.assign(ticket,
                { assignee_name: operator.first_name + ' ' + operator.last_name })
            }
          }
          if (trip) {
            ticket = Object.assign({ ticket_status: ticket.status, ticket_category: ticket.category },
              _.omit(ticket, 'status', 'category'))
            tickets[i] = _formatTicket(ticket, _.omit(trip, 'steps'))
          }
        }
        done(errors.noError(), tickets)
      })
    })
  } else {
    done(errors.noError(), tickets)
  }
}

/**
 * Used to get MaintenanceData for ticket
 *
 * @param {Array} tickets - tickets to get maintenance data for.
 * @param done - Callback
 */
const getMaintenanceDataForTicket = (tickets, done) => {
  const ticketsLength = tickets.length
  if (ticketsLength > 0) {
    maintenanceHandler.getMaintenance({ maintenance_id: _.pluck(tickets, 'type_id') }, (error, maintenanceData) => {
      if (error) {
        Sentry.captureException(error, {tickets})
        logger('Error: getting maintenance for tickets with', error)
        done(errors.internalServer(false), null)
        return
      }
      const operators = _.pluck(maintenanceData, 'reported_by_operator_id').concat((_.pluck(tickets, 'assignee')))
      operatorHandler.getOperator(null, { operator_id: _.uniq(operators) }, (error, operators) => {
        if (error) {
          Sentry.captureException(error, {tickets})
          logger('Error: getting operator for tickets with', error)
          done(errors.internalServer(false), null)
          return
        }
        let operator
        let ticket
        let maintenance
        for (let i = 0; i < ticketsLength; i++) {
          ticket = tickets[i]
          if (ticket.assignee) {
            operator = _.findWhere(operators, { operator_id: ticket.assignee })
            if (operator) {
              ticket = Object.assign(ticket,
                { assignee_name: operator.first_name + ' ' + operator.last_name })
            }
          }
          maintenance = _.findWhere(maintenanceData, { maintenance_id: ticket.type_id })
          if (maintenance) {
            operator = _.findWhere(operators, { operator_id: maintenance.reported_by_operator_id })
            ticket = Object.assign({
              ticket_status: ticket.status, ticket_category: ticket.category
            },
            _.omit(ticket, 'status', 'category'))
            if (operator) {
              maintenance = Object.assign(maintenance,
                {
                  reported_by_operator_name: operator.first_name.charAt(0).toUpperCase() + operator.first_name.slice(1) +
                    ' ' + operator.last_name.charAt(0).toUpperCase() + operator.last_name.slice(1)
                })
            }
            tickets[i] = _formatTicket(ticket, maintenance)
          }
        }
        done(errors.noError(), tickets)
      })
    })
  } else {
    done(errors.noError(), tickets)
  }
}

/**
 * Used to get theftData for ticket
 *
 * @param {Array} tickets - tickets to get theft data for.
 * @param done - Callback
 */
const getTheftDataForTicket = (tickets, done) => {
  const ticketsLength = tickets.length
  let operator
  if (tickets.length > 0) {
    operatorHandler.getOperator(null, { operator_id: _.uniq(_.pluck(tickets, 'operator_id')) }, (error, operators) => {
      if (error) {
        Sentry.captureException(error, {tickets})
        logger('Error: getting operator for tickets with', error)
        done(errors.internalServer(false), null)
        return
      }
      theftHandler.getTheft({ theft_id: _.pluck(tickets, 'type_id') }, (error, theftData) => {
        if (error) {
          Sentry.captureException(error, {tickets})
          done(errors.internalServer(false), null)
          return
        }
        let ticket
        let theft
        for (let i = 0; i < ticketsLength; i++) {
          ticket = tickets[i]
          theft = _.findWhere(theftData, { theft_id: ticket.type_id })
          if (ticket.assignee) {
            operator = _.findWhere(operators, { operator_id: ticket.assignee })
            if (operator) {
              ticket = Object.assign(ticket,
                { assignee_name: operator.first_name + ' ' + operator.last_name })
            }
          }
          if (theft) {
            operator = _.findWhere(operators, { operator_id: ticket.operator_id })
            if (operator) {
              ticket = Object.assign({ ticket_status: ticket.status, ticket_category: ticket.category },
                ({
                  operator_name: operator.first_name.charAt(0).toUpperCase() + operator.first_name.slice(1) +
                    ' ' + operator.last_name.charAt(0).toUpperCase() + operator.last_name.slice(1)
                }),
                _.omit(ticket, 'status', 'category'))
            } else {
              ticket = Object.assign({ ticket_status: ticket.status, ticket_category: ticket.category },
                _.omit(ticket, 'status', 'category'))
            }
            tickets[i] = _formatTicket(ticket, theft)
          }
        }
        done(errors.noError(), tickets)
      })
    })
  } else {
    done(errors.noError(), tickets)
  }
}

const _formatTicket = (ticket, otherDetails) => {
  return Object.assign(ticket, { ticket_date_created: ticket.date_created }, _.omit(otherDetails, 'trip_id'))
}

const validateTicket = (ticketData) => {
  const isValid = (
    _.has(ticketData, 'category') &&
    _.has(ticketData, 'fleet_id') &&
    _.has(ticketData, 'customer_id') &&
    _.has(ticketData, 'operator_id') &&
    _.has(ticketData, 'bike_id') &&
    _.has(ticketData, 'notes')
  )

  if (isValid && ticketData.category === maintenanceConstants.category.parkingOutsideGeofence) {
    return isValid && _.has(ticketData, 'trip_id')
  } else if ((ticketData.category === ticketConstants.category.serviceDue) ||
    (ticketData.category === ticketConstants.category.reportedTheft)) {
    return isValid
  } else if (ticketData.category === ticketConstants.category.damageReported) {
    return isValid && (_.has(ticketData, 'reported_by_user_id') ||
      _.has(ticketData, 'reported_by_operator_id'))
  } else {
    return isValid
  }
}

/**
 * Used to assign tickets to an operator
 *
 * @param {Object} ticketData - requestParam for ticket.
 * @param done - Callback
 */
const assignTicket = (ticketData, done) => {
  const columnsAndValues = {
    status: ticketConstants.status.assigned,
    assignee: ticketData.assignee,
    date_assigned: moment().unix()
  }
  const assignTicketsQuery = query.updateSingle(dbConstants.tables.tickets, columnsAndValues,
    {
      ticket_id: ticketData.ticket_id
    })
  sqlPool.makeQuery(assignTicketsQuery, (error) => {
    if (error) {
      Sentry.captureException(error, {ticketData, assignTicketsQuery})
      logger('Error assigning Tickets to an operator:', ticketData.assignee, error)
      done(errors.internalServer(true), null)
      return
    }
    done(errors.noError(), null)
  })
}

/**
 * Used to resolve a ticket
 *
 * @param {Object} ticketDetails - ticket identifier.
 * @param done - Callback
 */
const resolveTicket = (ticketDetails, done) => {
  if (!Array.isArray(ticketDetails.ticket_id)) {
    ticketDetails.ticket_id = [ticketDetails.ticket_id]
  }
  getTicket({ ticket_id: ticketDetails.ticket_id }, (error, tickets) => {
    if (error) {
      Sentry.captureException(error, {ticketDetails})
      logger('Error: Resolving ticket with ticket_id:', ticketDetails.ticket_id,
        'Unable to get ticket', errors.errorWithMessage(error))
      return done(errors.internalServer(false))
    }
    if (!tickets.length) {
      return done(errors.noError())
    }
    const resolveTicketsQuery = query.updateSingle(dbConstants.tables.tickets,
      {
        status: ticketConstants.status.resolved,
        date_resolved: moment().unix()
      },
      { ticket_id: _.pluck(tickets, 'ticket_id') })
    sqlPool.makeQuery(resolveTicketsQuery, (error) => {
      if (error) {
        Sentry.captureException(error, {ticketDetails, resolveTicketsQuery})
        logger('Error: Resolving ticket with ticket_id:', tickets.ticket_id, errors.errorWithMessage(error))
        done(errors.internalServer(false))
        return
      }
      const maintenanceTickets = _.filter(tickets, { type: ticketConstants.types.maintenance })
      if (!maintenanceTickets.length) {
        return done(errors.noError())
      }
      maintenanceHandler.updateMaintenance({ service_end_date: moment().unix() },
        { maintenance_id: _.pluck(maintenanceTickets, 'type_id') }, (error) => {
          if (error) {
            Sentry.captureException(error, {ticketDetails, resolveTicketsQuery})
            logger('Error: Resolving ticket with ticket_id:', maintenanceTickets.ticket_id,
              'Unable to update maintenance', errors.errorWithMessage(error))
            return done(errors.internalServer(false))
          }
          bikeFleetHandler.updateBike({ distance_after_service: 0, maintenance_status: null },
            { bike_id: _.pluck(maintenanceTickets, 'bike_id') }, (error) => {
              if (error) {
                Sentry.captureException(error, {ticketDetails, resolveTicketsQuery})
                logger('Error: Resolving ticket with ticket_id:', maintenanceTickets.ticket_id,
                  'Unable to update bike with bike_id', maintenanceTickets.bike_id, errors.errorWithMessage(error))
                return done(errors.internalServer(false))
              }
              done(errors.noError())
            })
        })
    })
  })
}

module.exports = Object.assign(module.exports, {
  getTicket: getTicket,
  insertTicket: insertTicket,
  createTicket: createTicket,
  getTickets: getTickets,
  validateTicket: validateTicket,
  assignTicket: assignTicket,
  resolveTicket: resolveTicket
})
