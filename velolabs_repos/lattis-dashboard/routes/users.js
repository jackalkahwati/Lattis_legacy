'use strict'

const platform = require('@velo-labs/platform')
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const errors = platform.errors
const logger = platform.logger
const express = require('express')
const router = express.Router()
const usersHandler = require('./../model_handlers/users-handler')
const moment = require('moment')
const fleetConstants = require('./../constants/fleet-constants')
const platformConfig = platform.config
const query = platform.queryCreator
const dbConstants = platform.dbConstants
const sqlPool = platform.sqlPool
const async = require('async')

/**
 * Gets all users in the database to return to user.
 *
 * The request body should include the fleet_id
 */
router.post('/get-user-data', (req, res) => {
  usersHandler.getUserData(req.body.fleet_id, (error, users) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }

    jsonResponse(res, responseCodes.OK, errors.noError(), users)
  })
})

router.post('/user-refunds', (req, res) => {
  const userRefundQuery = query.joinWithAnd(dbConstants.tables.user_refunded, dbConstants.tables.fleet_payment_settings,
    null, 'fleet_id', 'date_refunded', [0])
  sqlPool.makeQuery(userRefundQuery, (error, refundList) => {
    if (error) {
      logger('Error: failed to fetch user refund with:', userRefundQuery,
        'with error:', errors.formatErrorForWire(error))
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    if (refundList.length > 0) {
      let currentUnixTimestamp = moment().unix()
      async.forEachSeries(refundList, (refund, callbackRefund) => {
        let timeDiff = currentUnixTimestamp - refund.date_charged
        let timeDiffInUnit = 0
        if (refund.refund_criteria_value === fleetConstants.tableParams.mins) {
          timeDiffInUnit = (timeDiff / 60)
        } else if (refund.refund_criteria_value === fleetConstants.tableParams.hours) {
          timeDiffInUnit = (timeDiff / (60 * 60))
        } else if (refund.refund_criteria_value === fleetConstants.tableParams.days) {
          timeDiffInUnit = (timeDiff / (60 * 60 * 24))
        } else if (refund.refund_criteria_value === fleetConstants.tableParams.weeks) {
          timeDiffInUnit = (timeDiff / (60 * 60 * 24 * 7))
        } else if (refund.refund_criteria_value === fleetConstants.tableParams.months) {
          timeDiffInUnit = (timeDiff / (60 * 60 * 24 * 30))
        }
        if (timeDiffInUnit > refund.refund_criteria) {
          let stripe = require('stripe')(platformConfig.stripe.apiKey)
          stripe.refunds.create({
            charge: refund.charge_id,
            amount: (refund.deposit_amount * 100)
          }, (error) => {
            if (error) {
              callbackRefund()
            } else {
              let requestParams = {
                refund_id: refund.refund_id,
                date_refunded: currentUnixTimestamp
              }
              usersHandler.updateUserRefund(requestParams, (error) => {
                if (error) {
                  logger('Error: Updating user refund with param: ', requestParams, ' failed with error: ', error)
                  callbackRefund()
                }
                callbackRefund()
              })
            }
          })
        } else {
          callbackRefund()
        }
      }, (error) => {
        if (error) {
          logger('Error: updating user refunds failed with error: ', error)
        }
        console.log('done')
      })
    }
  })
})

module.exports = router
