'use strict'
const platform = require('@velo-labs/platform')
const sqlPool = platform.sqlPool
const query = platform.queryCreator
const errors = platform.errors
const logger = platform.logger
const dbConstants = platform.dbConstants
const mailContent = platform.mailContent
const mailHandler = platform.mailHandler
const _ = require('underscore')
const ticketConstants = require('./../constants/ticket-constants')
const fleetHandler = require('./fleet-handler')
const operatorHandler = require('./operator-handler')
const {configureSentry} = require('../utils/configureSentry')

const Sentry = configureSentry()

/** * This method gets the details of the alerts from the alerts table
 *
 * @param {Object} columnsAndValues - Identifier to filter the alerts table
 * @param {Function} callback
 */
const getAlerts = (columnsAndValues, callback) => {
  const getAlertsQuery = query.selectWithAnd(dbConstants.tables.alerts, null, columnsAndValues)
  sqlPool.makeQuery(getAlertsQuery, callback)
}

/** * This method sends email notifications to the operators
 *
 * @param {Object} requestParam - Identifier from the request
 * @param {Object} category - category of maintenance
 * @param {Function} callback
 */
const sendEmailNotifications = (requestParam, category, callback) => {
  fleetHandler.getFleetsWithFleetAssociations('fleet_id', [requestParam.fleet_id], (error, fleetAssociations) => {
    if (error) {
      Sentry.captureException(error, {requestParam, category})
      callback(error, null)
      return
    }
    if (fleetAssociations.length === 0) {
      logger('Error: No operator has access to fleet', requestParam.fleet_id)
      callback(errors.resourceNotFound(false), null)
      return
    }
    const operators = _.pluck(fleetAssociations, 'operator_id')
    getAlerts({ operator_id: operators }, (error, alerts) => {
      if (error) {
        Sentry.captureException(error, { operators })

        logger('Error: Getting Alerts with the operator id:', requestParam.operator_id, errors.errorWithMessage(error))
        callback(errors.internalServer(false), null)
        return
      }
      const operatorList = []
      let alert
      if (alerts.length !== 0) {
        const operatorsLength = operators.length
        for (let i = 0; i < operatorsLength; i++) {
          alert = _.findWhere(alerts, { operator_id: operators[i] })
          if (alert) {
            const allTicket = alert.all_tickets > 0
            const theftReported = alert.theft_is_reported > 0 && (category === ticketConstants.category.reportedTheft)
            const damageReported = alert.damage_is_reported > 0 && (category === ticketConstants.category.damageReported)
            const maintenanceDue = alert.maintenance_due > 0 && (category === ticketConstants.category.maintenanceDue)
            const serviceDue = alert.maintenance_due > 0 && (category === ticketConstants.category.serviceDue)
            const ellipseBatteryLow = alert.ellipse_battery_low > 0 && (category === ticketConstants.category.maintenance_due)
            const bikeParkedOutsideZone = alert.bike_is_parked_outside_zone > 0 && (category === ticketConstants.category.parkingOutsideGeofence)
            const bikeParkedOutsideSpot = alert.bike_is_parked_outside_spot > 0 && (category === ticketConstants.category.parkingOutsideGeofence)
            if (allTicket || (theftReported || damageReported || maintenanceDue || ellipseBatteryLow || bikeParkedOutsideZone || bikeParkedOutsideSpot || serviceDue)) {
              operatorList.push(operators[i])
            }
          }
        }
      }
      if (operatorList.length === 0) {
        return callback(null, null)
      }
      operatorHandler.getOperator(null, { operator_id: operatorList }, (error, operatorData) => {
        if (error) {
          Sentry.captureException(error, { operatorList })
          logger('Error: Getting alerts with operator_id:', requestParam.operator_id, errors.errorWithMessage(error))
          callback(error, null)
          return
        }
        let operator
        let mailObject = {}
        const operatorsLength = operatorData.length
        for (let i = 0; i < operatorsLength; i++) {
          operator = operatorData[i]
          if (category === ticketConstants.category.reportedTheft) {
            mailObject = mailContent.theftReportedTicket(operator.first_name, fleetAssociations[0].fleet_name)
          } else {
            mailObject = mailContent.allActivityTicket(operator.first_name, fleetAssociations[0].fleet_name)
          }
          logger('Info: Sending email to', operator.email)
          mailHandler.sendMail(operator.email, mailObject.subject, mailObject.content, null, null, (error) => {
            if (error) {
              Sentry.captureException(error, { operator })
              logger('Error: Failed to send mail for createTicket to the recipient with email', operator.email, error)
              return callback(error, null)
            }
            if (i >= operatorsLength - 1) {
              return callback(errors.noError(), null)
            }
          })
        }
      })
    })
  })
}

module.exports = {
  getAlerts: getAlerts,
  sendEmailNotifications: sendEmailNotifications
}
