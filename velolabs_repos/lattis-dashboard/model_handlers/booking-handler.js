'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const sqlPool = platform.sqlPool
const query = platform.queryCreator
const errors = platform.errors
const _ = require('underscore')
const dbConstants = platform.dbConstants
const Sentry = require('../utils/configureSentry')

/** * This function is to update the booking details
 *
 * @param {Object} bookingDetails - Booking details to be updated
 * @param {Object} filterParam - Filter column and values to select booking row
 * @param {Function} callback
 */
const updateBooking = (bookingDetails, filterParam, callback) => {
  const updateBookingQuery = query.updateSingle(
    dbConstants.tables.booking,
    _.omit(bookingDetails, 'booking_id', 'user_id', 'fleet_id', 'customer_id', 'operator_id'),
    filterParam)
  sqlPool.makeQuery(updateBookingQuery, (error, updateStatus) => {
    if (error) {
      Sentry.captureException(error, {bookingDetails, filterParam, updateBookingQuery})
      logger('Error: Failed to update booking with', filterParam, ':', error)
      callback(errors.internalServer(false), null)
      return
    }
    callback(errors.noError(), updateStatus.insertId)
  })
}

module.exports = {
  updateBooking: updateBooking
}
