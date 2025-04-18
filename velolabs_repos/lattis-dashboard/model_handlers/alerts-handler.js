'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const query = platform.queryCreator
const sqlPool = platform.sqlPool
const _ = require('underscore')
const errors = platform.errors
let jsonFile = require('jsonfile')
let path = require('path')
const dbConstants = platform.dbConstants
const fleetHandler = require('./fleet-handler')
const operatorHandler = require('./operator-handler')
const ticketConstants = require('./../constants/ticket-constants')
const Sentry = require('../utils/configureSentry')

const mailContent = platform.mailContent
const mailHandler = platform.mailHandler

const getAllAlerts = (done) => {
  const pathName = path.join(__dirname, '../files/alerts.json')
  jsonFile.readFile(pathName, (error, data) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: could not read alerts json file at path:', pathName)
      return done(error, null)
    }
    done(null, data)
  })
}

/** * This method gets the details of the alerts from the alerts table
 *
 * @param {Object} columnsAndValues - Identifier to filter the alerts table
 * @param {Function} done - callback
 */
const getAlerts = (columnsAndValues, done) => {
  const getAlertsQuery = query.selectWithAnd(dbConstants.tables.alerts, null, columnsAndValues)
  sqlPool.makeQuery(getAlertsQuery, done)
}

/** * This method sends email notifications to the operators
 *
 * @param {Object} requestParam - Identifier from the request
 * @param {Object} category - category of maintenance
 * @param {Function} done
 */
const sendEmailNotifications = (requestParam, category, done) => {
  fleetHandler.getFleetsWithFleetAssociations('operator_id', 'fleet_id', [requestParam.fleet_id], false, (error, fleetAssociations) => {
    if (error) {
      Sentry.captureException(error, {requestParam, category})
      return done(error, null)
    }
    if (fleetAssociations.length === 0) {
      logger('Error: No operator has access to fleet', requestParam.fleet_id)
      return done(errors.resourceNotFound(false), null)
    }
    const operatorIds = _.pluck(fleetAssociations, 'operator_id')
    getAlerts({operator_id: operatorIds}, (error, alerts) => {
      if (error) {
        Sentry.captureException(error, {requestParam, category})
        logger('Error: Getting Alerts with the operator id:', operatorIds, errors.errorWithMessage(error))
        return done(errors.internalServer(false), null)
      }
      const operatorList = []
      let alert
      if (alerts.length !== 0) {
        const operatorsLength = operatorIds.length
        for (let i = 0; i < operatorsLength; i++) {
          alert = _.findWhere(alerts, {operator_id: operatorIds[i]})
          if (alert) {
            const allTicket = alert.all_tickets > 0
            const theftReported = alert.theft_is_reported > 0 && (category === ticketConstants.category.reportedTheft)
            const potentialTheft = alert.theft_is_reported > 0 && (category === ticketConstants.category.potentialTheft)
            const damageReported = alert.damage_is_reported > 0 && (category === ticketConstants.category.damageReported)
            const maintenanceDue = alert.maintenance_due > 0 && (category === ticketConstants.category.maintenanceDue)
            const serviceDue = alert.maintenance_due > 0 && (category === ticketConstants.category.serviceDue)
            const ellipseBatteryLow = alert.ellipse_battery_low > 0 && (category === ticketConstants.category.maintenance_due)
            const bikeParkedOutsideZone = alert.bike_is_parked_outside_zone > 0 && (category === ticketConstants.category.parkingOutsideGeofence)
            const bikeParkedOutsideSpot = alert.bike_is_parked_outside_spot > 0 && (category === ticketConstants.category.parkingOutsideGeofence)
            if (allTicket || (theftReported || damageReported || maintenanceDue || ellipseBatteryLow || bikeParkedOutsideZone || bikeParkedOutsideSpot || serviceDue || potentialTheft)) {
              operatorList.push(operatorIds[i])
            }
          }
        }
      }
      if (operatorList.length === 0) {
        return done(null, null)
      }
      operatorHandler.getOperator(null, {operator_id: operatorList}, (error, operatorData) => {
        if (error) {
          Sentry.captureException(error, {requestParam, category})
          logger('Error: Getting alerts with operator_id:', operatorList, errors.errorWithMessage(error))
          return done(error, null)
        }
        const operatorsLength = operatorData.length
        let operator
        let mailObject = {}
        for (let i = 0; i < operatorsLength; i++) {
          operator = operatorData[i]
          if (category === ticketConstants.category.reportedTheft) {
            mailObject = mailContent.theftReportedTicket(operator.first_name, fleetAssociations[0].fleet_name)
          } else if (category === ticketConstants.category.potentialTheft) {
            mailObject = mailContent.potentialTheftTicket(operator.first_name, fleetAssociations[0].fleet_name)
          } else {
            mailObject = mailContent.allActivityTicket(operator.first_name, fleetAssociations[0].fleet_name)
          }
          mailHandler.sendMail(operator.email, mailObject.subject, mailObject.content, null, null, (error) => {
            if (error) {
              Sentry.captureException(error, {requestParam, category})
              logger('Error: Failed to send mail for createTicket to the recipient with email', operator.email, error)
              return done(error, null)
            }
            if (i >= operatorsLength - 1) {
              return done(errors.noError(), null)
            }
          })
        }
      })
    })
  })
}

module.exports = {
  getAllAlerts: getAllAlerts,
  getAlerts: getAlerts,
  sendEmailNotifications: sendEmailNotifications
}
