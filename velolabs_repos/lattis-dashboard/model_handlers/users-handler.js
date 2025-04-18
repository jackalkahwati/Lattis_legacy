'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const query = platform.queryCreator
const sqlPool = platform.sqlPool
const errors = platform.errors
const dbConstants = platform.dbConstants
let async = require('async')
let _ = require('underscore')
const Sentry = require('../utils/configureSentry')

/*
 * Used to get users with params
 * @param {comparisonColumns} - Object
 * @param {comparisonValues} - Object
 * @param {Function} done - returns {error, profileInfo}
 * @private
 */
const getUsers = (comparison, done) => {
  const usersQuery = query.selectWithAnd(dbConstants.tables.users, null, comparison)
  sqlPool.makeQuery(usersQuery, (err, users) => {
    if (err) {
      return done(err, users)
    }

    const results = users.map(user => _.omit(user, 'password', 'rest_token', 'refresh_token'))

    return done(null, results)
  })
}

const updateUserRefund = (userRefundDetails, callback) => {
  const updateRefundQuery = query.updateSingle(
    dbConstants.tables.user_refunded,
    {date_refunded: userRefundDetails.date_refunded},
    {refund_id: userRefundDetails.refund_id})
  sqlPool.makeQuery(updateRefundQuery, (error) => {
    if (error) {
      Sentry.captureException(error, {updateRefundQuery})
      logger('Error: making update-refund query with refund_id: ', userRefundDetails.refund_id, ':', updateRefundQuery)
      return callback(errors.internalServer(false), null)
    }
    callback(null, null)
  })
}

/**
 * Used to get all users data with Params(fleetId, etc)
 * @param {Object} fleetId - fleetId
 * @param {Function} done - returns {error, profileInfo}
 */
const getUserData = function (fleetId, done) {
  let asyncTasks = {
    trips: function (next) {
      getTrips(fleetId, next)
    },
    users: function (next) {
      getAllUsers(next)
    }
  }
  async.parallel(asyncTasks, function (err, results) {
    if (err) {
      done(err, null)
    } else {
      let usersData = []
      let uniqueTrips = _.uniq(results.trips, function (trip) {
        return trip.user_id
      })
      _.each(uniqueTrips, function (uniqueTrip) {
        _.each(results.users, function (user) {
          if (uniqueTrip.user_id === user.user_id) {
            usersData.push(user)
          }
        })
      })
      results.users = usersData
      done(null, results)
    }
  })
}

/**
 *
 * Used to get trips of fleetId.
 *
 * @param {Object} fleetId - get Trips based on the fleetId
 * @param {Function} done - returns {error, done}
 */
const getTrips = function (fleetId, done) {
  const tripsQuery = query.selectWithAnd('trips', null, {fleet_id: fleetId})
  sqlPool.makeQuery(tripsQuery, function (error, trips) {
    if (error) {
      Sentry.captureException(error, {tripsQuery})
      logger('Error: making trips query by operator id:', tripsQuery)
      done(error, null)
      return
    }

    done(null, trips)
  })
}

/**
 *
 * Used to get all users .
 *
 * @param {Function} done - returns {error, done}
 * @Private
 */
const getAllUsers = function (done) {
  const usersQuery = query.selectWithAnd('users', null, null)
  sqlPool.makeQuery(usersQuery, function (error, users) {
    if (error) {
      Sentry.captureException(error, {usersQuery})
      logger('Error: making users query:', usersQuery)
      done(error, null)
      return
    }

    done(null, users)
  })
}

/**
 * Updates user profile information
 *
 * @param {Object} userDetails - user information
 * @param {Function} done - Callback function with error and status
 */
const updateUserProfile = function (userDetails, done) {
  let columnsAndValues = {
    first_name: userDetails.first_name,
    last_name: userDetails.last_name,
    country_code: userDetails.country_code,
    phone_number: userDetails.phone_number,
    email: userDetails.email
  }
  const updateUserProfileQuery = query.updateSingle(dbConstants.tables.users, columnsAndValues, {user_id: userDetails.user_id})
  sqlPool.makeQuery(updateUserProfileQuery, function (error) {
    if (error) {
      Sentry.captureException(error, {updateUserProfileQuery})
      logger('Error: making updateUserProfile Query:', errors.errorWithMessage(error))
      done(errors.internalServer(false), null)
      return
    }
    columnsAndValues = {
      email: userDetails.email
    }
    const updatePrivateFleetUserQuery = query.updateSingle(dbConstants.tables.private_fleet_users, columnsAndValues, {user_id: userDetails.user_id})
    sqlPool.makeQuery(updatePrivateFleetUserQuery, function (error) {
      if (error) {
        Sentry.captureException(error, {updatePrivateFleetUserQuery})
        logger('Error: making update privateFleetUser Query:', errors.errorWithMessage(error))
        done(errors.internalServer(false), null)
        return
      }
      done(null, null)
    })
  })
}

module.exports = {
  getUserData: getUserData,
  getTrips: getTrips,
  updateUserProfile: updateUserProfile,
  getUsers: getUsers,
  updateUserRefund: updateUserRefund
}
