'use strict'

const platform = require('@velo-labs/platform')
const queryCreator = platform.queryCreator
const sqlPool = platform.sqlPool
const _ = require('underscore')
const errors = platform.errors
const logger = platform.logger
const confirmationCodeTypes = require('../constants/confirmation-code-types')
const dbConstants = platform.dbConstants
const userHandler = require('../handlers/user-handler')
const fleetHandler = require('../handlers/fleet-handler')
const fleetConstant = require('../constants/fleet-constants')
const conversions = require('../utils/conversions')
const db = require('../db')
const {responseCodes} = require('@velo-labs/platform')
const { configureSentry } = require('../utils/configureSentry')

const Sentry = configureSentry()

/**
 * To insert private account
 *
 * @param {Object} user - user identification
 * @param {String} email - Email of the account to be inserted
 * @param {Function} callback
 */
const processPrivateAccount = (user, email, appDetails, callback) => {
  const emailDomain = email.split('@')
  getDomains({domain_name: '@' + emailDomain[1]}, async (error, domains) => {
    if (error) {
      Sentry.captureException(error, { user })
      logger('Error: Failed to process private network. failed to get domain with ',
        errors.errorWithMessage(error))
      callback(errors.customError(
        `An error occurred getting fleet domains`, responseCodes.InternalServer,
        'InternalServer'), null)
      return
    }
    try {
      const privateAccounts = await db.users('private_fleet_users').where({email: email}).where({access: 1}).where({verified: 0})
      // let's determine where to send the email
      if (email) user.email = email
      if (!privateAccounts.length) {
        // we need to return empty list so user gets message from mobile
        logger(`Error: could not add private account for email ${email}`)
        callback(errors.noError(), privateAccounts)
        return
      }

      const privateAccountsWithNoUserId = privateAccounts.filter(account => account.user_id === null)
      // let's add user_ids, so we will be able to confirm emails
      if (privateAccountsWithNoUserId.length) {
        privateAccountsWithNoUserId.forEach(async (account) => {
          await db.users('private_fleet_users').update({user_id: user.user_id}).where({email: account.email})
        })
      }
      // email a confirmation code to user
      userHandler.emailUserConfirmationCode(user.user_id,
        confirmationCodeTypes.lattisAccount,
        user.email, appDetails, (error) => {
          if (error) {
            Sentry.captureException(error, { user })
            logger('Failed to process Lattis account. Sending email failed.', error)
            callback(errors.customError(
              `An error occurred when sending email to user via the email ${user.email}`, responseCodes.InternalServer,
              'InternalServer'), null)
            return
          }
          callback(errors.noError(), privateAccounts)
        })
    } catch (error) {
      Sentry.captureException(error, { user })
      throw errors.customError(
        `Failed to retrieve private account info` || error.message,
        platform.responseCodes.InternalServer,
        'InternalServerError',
        false
      )
    }
  })
}

/**
 * To get all the private accounts
 *
 * @param {Object} accountInfo - Account details to insert
 * @param {Function} callback
 */
const insertPrivateAccounts = (accountInfo, callback) => {
  if (!Array.isArray(accountInfo)) {
    accountInfo = [accountInfo]
  }
  if (accountInfo.length !== 0) {
    _.each(accountInfo, (account, index) => {
      const query = queryCreator.insertSingle(dbConstants.tables.private_fleet_users, _.omit(account, 'private_fleet_user_id'))
      sqlPool.makeQuery(query, (error) => {
        if (error) {
          Sentry.captureException(error, { account })
          logger('Error: could not save lattis account:', account, errors.errorWithMessage(error))
          callback(errors.internalServer(false), null)
          return
        }
        if (index === accountInfo.length - 1) {
          return callback(null)
        }
      })
    })
  } else {
    callback(null)
  }
}

/**
 * To get all the private accounts
 *
 * @param {Object} userInfo - user identification
 * @param {Function} callback
 */
const getPrivateAccount = (userInfo, callback) => {
  let comparisonColumn = (userInfo.verified || userInfo.verified === 0) ? _.pick(userInfo, 'verified') : {}
  if (userInfo.private_fleet_user_id) {
    comparisonColumn.private_fleet_user_id = userInfo.private_fleet_user_id
  }
  if (userInfo.email) {
    comparisonColumn.email = userInfo.email
  }
  if (userInfo.user_id || userInfo.user_id === null) {
    comparisonColumn.user_id = userInfo.user_id
  }
  if (userInfo.fleet_id) {
    comparisonColumn.fleet_id = userInfo.fleet_id
  }
  if (userInfo.access || userInfo.access === 0) {
    comparisonColumn.access = userInfo.access
  }
  const query = queryCreator.selectWithAnd(dbConstants.tables.private_fleet_users, null, comparisonColumn)
  sqlPool.makeQuery(query, (error, accounts) => {
    if (error) {
      Sentry.captureException(error, { userInfo })
      logger(
        'Error: could not process lattis account for:',
        comparisonColumn,
        'failed with error:',
        errors.errorWithMessage(error)
      )
      callback(errors.internalServer(false), null)
      return
    }
    callback(null, accounts)
  })
}

const _updatePrivateAccount = (columnsAndValues, comparisonColumn, callback) => {
  sqlPool.makeQuery(queryCreator.updateSingle(dbConstants.tables.private_fleet_users,
    columnsAndValues, comparisonColumn), callback)
}
/**
 * To update private account details
 *
 * @param {Object} userInfo - account to be formatted
 * @param {Function} callback
 */
const updatePrivateAccount = (userInfo, callback) => {
  _updatePrivateAccount(_.omit(userInfo, 'private_fleet_user_id'),
    _.pick(userInfo, 'private_fleet_user_id'), (error) => {
      if (error) {
        Sentry.captureException(error, { userInfo })
        logger(
          'Error: could not process lattis account for user with user_id:',
          userInfo.user_id,
          'failed with error:',
          errors.errorWithMessage(error)
        )
        callback(errors.internalServer(false))
        return
      }
      callback(null)
    })
}

/**
 * To update multiple private account details at once
 *
 * @param {Object} userInfo - account to be formatted
 * @param {Function} callback
 */
const updatePrivateAccounts = (userInfo, callback) => {
  if (!Array.isArray(userInfo)) {
    userInfo = [userInfo]
  }
  _.each(userInfo, (user, index) => {
    updatePrivateAccount(user, (error) => {
      if (error) {
        Sentry.captureException(error, { user })
        callback(error)
        return
      }
      if (index === userInfo.length - 1) {
        return callback(null)
      }
    })
  })
}

/**
 * To get the all the domains for fleets
 *
 * @param {Object} domainDetail - domains identification
 * @param {Function} callback
 */
const getDomains = (domainDetail, callback) => {
  let comparisonColumn = {}
  if (domainDetail.domain_id) {
    comparisonColumn.domain_id = domainDetail.domain_id
  }
  if (domainDetail.fleet_id) {
    comparisonColumn.fleet_id = domainDetail.fleet_id
  }
  if (domainDetail.domain_name) {
    comparisonColumn.domain_name = domainDetail.domain_name
  }
  const query = queryCreator.selectWithAnd(dbConstants.tables.domains, null, comparisonColumn)
  sqlPool.makeQuery(query, (error, domains) => {
    if (error) {
      Sentry.captureException(error, { domainDetail })
      logger('Error: failed to fetch domain for:', domainDetail, 'with error:', error)
      callback(errors.internalServer(false), null)
      return
    }
    callback(null, domains)
  })
}

/**
 * To delete the private account
 *
 * @param {Object} comparisonColumnAndValue - account details
 * @param {Function} callback
 */
const deletePrivateAccount = (comparisonColumnAndValue, callback) => {
  const query = queryCreator.deleteSingle(dbConstants.tables.private_fleet_users, comparisonColumnAndValue)
  sqlPool.makeQuery(query, (error) => {
    if (error) {
      Sentry.captureException(error, { comparisonColumnAndValue })
      logger('Error: failed to delete private fleet user with', comparisonColumnAndValue, 'with error:', error)
      return callback(errors.internalServer(false))
    }
    callback(null)
  })
}

/**
 * To delete unverified private accounts
 *
 * @param {Object} filterParam - account details
 * @param {Function} callback
 */
const deleteUnverifiedAccounts = (filterParam, callback) => {
  let comparisonColumnAndValue = {verified: 0}
  if (filterParam.user_id) {
    comparisonColumnAndValue.user_id = filterParam.user_id
  } else if (filterParam.email) {
    comparisonColumnAndValue.email = filterParam.email
  }
  getPrivateAccount(comparisonColumnAndValue, (error, privateAccount) => {
    if (error) {
      Sentry.captureException(error, { comparisonColumnAndValue })
      callback(error)
      return
    }
    if (privateAccount.length > 0) {
      deletePrivateAccount({private_fleet_user_id: _.pluck(privateAccount, 'private_fleet_user_id')}, (error) => {
        if (error) {
          Sentry.captureException(error, { comparisonColumnAndValue })
          callback(error)
          return
        }
        callback(null)
      })
    } else {
      callback(null)
    }
  })
}

/**
 * To format private fleet content for wire
 *
 * @param {Object} privateAccount - account to be formatted
 * @param {Function} callback
 */
const formatPrivateAccountForWire = (privateAccount, callback) => {
  if (!Array.isArray(privateAccount)) {
    privateAccount = [privateAccount]
  }
  if (privateAccount.length !== 0) {
    fleetHandler.getFleets({fleet_id: _.pluck(privateAccount, 'fleet_id')}, async (error, fleetDetails) => {
      if (error) {
        Sentry.captureException(error, { privateAccount })
        logger('Error: Failed to get fleets with error:', error)
        callback(error, null)
        return
      }
      if (fleetDetails.length === 0) {
        logger('Error: Failed to get private accounts. Unable to fetch fleet with fleet_id:',
          _.pluck(privateAccount, 'fleet_id'))
        callback(errors.internalServer(false), null)
        return
      }

      const addresses = _.groupBy(
        await db.main('addresses')
          .where('type', 'fleet')
          .andWhere(function () {
            this.whereIn('type_id', fleetDetails.map(f => f.fleet_id))
          })
          .select(),
        'type_id'
      )

      fleetHandler.getCustomers({customer_id: _.pluck(fleetDetails, 'customer_id')}, (error, customerDetails) => {
        if (error) {
          Sentry.captureException(error, { privateAccount })
          logger('Error: Failed to get customers with error:', error)
          callback(error, null)
          return
        }
        if (customerDetails.length === 0) {
          logger('Error: Failed to get private accounts. Unable to fetch customer with customer_id:',
            _.pluck(customerDetails, 'customer_id'))
          callback(errors.internalServer(false), null)
          return
        }

        const privateAccountLen = privateAccount.length
        for (let i = 0; i < privateAccountLen; i++) {
          const fleet = _.findWhere(fleetDetails, {fleet_id: privateAccount[i].fleet_id})
          if (fleet) {
            const address = _.first(addresses[fleet.fleet_id])
            const customer = _.findWhere(customerDetails, {customer_id: fleet.customer_id})
            privateAccount[i].fleet_name = fleet.fleet_name
            privateAccount[i].type = fleet.type
            privateAccount[i].logo = fleet.logo
            privateAccount[i].customer_name = customer.customer_name
            privateAccount[i].address = address
          }
        }
        callback(errors.noError(), privateAccount.filter(fleet => ['private', 'private_no_payment'].includes(fleet.type)))
      })
    })
  } else {
    callback(errors.noError(), privateAccount)
  }
}

/**
 * To filter out private fleet content for wire
 *
 * @param {Object} contents - contents to be filtered
 * @param {Object} user - User identification
 * @param {Function} callback
 */
const filterPrivateFleetContent = (contents, user, callback) => {
  getAccessibleFleets(user, _.pluck(contents, 'fleet_id'), (error, accessibleFleets) => {
    if (error) {
      Sentry.captureException(error, { contents, user })
      return callback(error, null)
    }
    if (!accessibleFleets.length) {
      return callback(errors.noError(), accessibleFleets)
    }
    fleetHandler.getFleetMetaData({fleet_id: accessibleFleets}, (error, fleetMetaData) => {
      if (error) {
        Sentry.captureException(error, { contents, user })
        logger('Error getting fleetMetaData with fleets', accessibleFleets, errors.errorWithMessage(error))
        callback(error, null)
        return
      }
      let fleetsWithinTime = []
      if (fleetMetaData.length === 0) {
        fleetsWithinTime = [...accessibleFleets]
      } else {
        const accessibleFleetsLength = accessibleFleets.length
        for (let i = 0; i < accessibleFleetsLength; i++) {
          const metaData = _.findWhere(fleetMetaData, {fleet_id: accessibleFleets[i]})
          if (metaData) {
            if (metaData.fleet_timezone && metaData.bike_available_from && metaData.bike_available_till) {
              const totalMinutes = conversions.getTotalMinutesForTimeZone(metaData.fleet_timezone)
              if (totalMinutes >= metaData.bike_available_from && totalMinutes <= metaData.bike_available_till) {
                fleetsWithinTime.push(metaData.fleet_id)
              }
            } else {
              fleetsWithinTime.push(metaData.fleet_id)
            }
          } else {
            fleetsWithinTime.push(accessibleFleets[i])
          }
        }
      }
      let finalContent = []
      const fleetsWithinTimeLength = fleetsWithinTime.length
      if (Array.isArray(fleetsWithinTime) || (fleetsWithinTimeLength !== 0)) {
        for (let i = 0, fleetsLen = fleetsWithinTime.length; i < fleetsLen; i++) {
          const eligibleContent = _.filter(contents, {fleet_id: fleetsWithinTime[i]})
          if (eligibleContent.length > 0) {
            finalContent = finalContent.concat(eligibleContent)
          }
        }
        callback(errors.noError(), finalContent)
      } else {
        callback(errors.noError(), finalContent)
      }
    })
  })
}

/**
 * To get accessible private and public fleet for user
 *
 * @param {Object} user - User identification
 * @param {Array} fleets - fleets identification
 * @param {Function} callback
 */
const getAccessibleFleets = (user, fleets, callback) => {
  let accessibleFleets = []
  fleetHandler.getFleets({fleet_id: fleets}, (error, publicFleets) => {
    if (error) {
      Sentry.captureException(error, { user, fleets })
      logger('Error: failed to get public fleets with error:', error)
      callback(error, null)
      return
    }
    publicFleets = publicFleets.filter(pubFleet => ((pubFleet.type === fleetConstant.fleet_type.publicWithPayment) ||
          (pubFleet.type === fleetConstant.fleet_type.publicWithNoPayment)))
    accessibleFleets = _.uniq(_.pluck(publicFleets, 'fleet_id'))
    if (user.user_id) {
      const comparisonColumn = {user_id: user.user_id, verified: 1}
      getPrivateAccount(fleets ? Object.assign(comparisonColumn, {fleet_id: _.uniq(fleets)}) : comparisonColumn,
        (error, privateAccounts) => {
          if (error) {
            Sentry.captureException(error, { user, fleets })
            logger('Error: failed to fetch private accounts for user:', user.user_id, 'with error:', error)
            callback(error, null)
            return
          }
          let validAccessibleFleets = privateAccounts.reduce((acc, curr, index) => {
            if (curr.access === 1) {
              acc.push(curr.fleet_id)
            } else if (curr.access === 0 && accessibleFleets.includes(curr.fleet_id)) {
              let index = accessibleFleets.indexOf(curr.fleet_id)
              accessibleFleets.splice(index, 1)
            }

            return acc
          }, [])
          accessibleFleets = accessibleFleets.concat(validAccessibleFleets)
          callback(errors.noError(), accessibleFleets)
        })
    } else {
      callback(errors.noError(), accessibleFleets)
    }
  })
}

module.exports = Object.assign(module.exports, {
  processPrivateAccount: processPrivateAccount,
  updatePrivateAccount: updatePrivateAccount,
  updatePrivateAccounts: updatePrivateAccounts,
  insertPrivateAccounts: insertPrivateAccounts,
  getPrivateAccount: getPrivateAccount,
  deletePrivateAccount: deletePrivateAccount,
  deleteUnverifiedAccounts: deleteUnverifiedAccounts,
  getDomains: getDomains,
  formatPrivateAccountForWire: formatPrivateAccountForWire,
  filterPrivateFleetContent: filterPrivateFleetContent,
  getAccessibleFleets: getAccessibleFleets
})
