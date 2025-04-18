'use strict'

const platform = require('@velo-labs/platform')
const query = platform.queryCreator
const sqlPool = platform.sqlPool
const dbConstants = platform.dbConstants
const errors = platform.errors
const encryptionHandler = platform.encryptionHandler
const _ = require('underscore')
const userHandler = require('./user-handler')
const fleetHandler = require('./fleet-handler')
const logger = platform.logger
const moment = require('moment')
const { configureSentry } = require('../utils/configureSentry')

const Sentry = configureSentry()

/*
 * Retrieves the details of Operator User
 *
 * @param {Object} columnsToSelect
 * @param {Object} comparisonColumnsAndValues
 * @param {Function} Callback with params {error, rows}
 */
const getOperator = (columnsToSelect, comparisonColumnsAndValues, callback) => {
  const operatorQuery = query.selectWithAnd(
    'operators',
    columnsToSelect,
    comparisonColumnsAndValues
  )
  sqlPool.makeQuery(operatorQuery, callback)
}

/*
 * Retrieves the details of Operator User
 *
 * @param {Object} columnsToSelect
 * @param {Object} comparisonColumnsAndValues
 * @param {Function} Callback with params {error, rows}
 */
const getFleets = (columnsToSelect, comparisonColumnsAndValues, callback) => {
  const operatorAssociationsQuery = query.selectWithAnd(
    dbConstants.tables.fleet_associations,
    null,
    comparisonColumnsAndValues
  )
  sqlPool.makeQuery(operatorAssociationsQuery, callback)
}

/*
 * login
 *
 * @param {Object} columnsToSelect
 * @param {Object} comparisonColumnsAndValues
 * @param {Function} Callback with params {error, rows}
 */
const login = (reqParam, callback) => {
  getOperator(null, { username: reqParam.username }, (error, operator) => {
    if (error) {
      Sentry.captureException(error, { reqParam })
      logger(
        'Error: failed to fetch operators with:',
        reqParam.username,
        'with error:',
        error
      )
      callback(errors.internalServer(false), null)
      return
    }
    if (operator.length === 0) {
      logger('There is no operator with userName', reqParam.username)
      callback(errors.resourceNotFound(false), null)
      return
    }
    if (
      encryptionHandler.verifyPassword(reqParam.password, operator[0].password)
    ) {
      callback(null, {
        operator: _.pick(
          operator[0],
          'operator_id',
          'rest_token',
          'refresh_token'
        )
      })
    } else {
      callback(errors.invalidPassword(false), null)
    }
  })
}

/**
 * This method will validate an incoming REST Token
 *
 * @param {object} requestHeaders
 * @param {Function} callback
 */
const isAuthorizationTokenValid = (requestHeaders, callback) => {
  if (!_.has(requestHeaders, 'authorization')) {
    callback(errors.unauthorizedAccess(false), null, false, false)
    return
  }

  const incomingRestToken = requestHeaders.authorization
  const decryptedToken = userHandler.decryptAuthToken(incomingRestToken)
  logger(decryptedToken)
  logger(decryptedToken.split(userHandler.authorizationTokenDeliminator))
  const parts = decryptedToken.split(userHandler.authorizationTokenDeliminator)
  if (parts.length !== 2) {
    callback(
      new Error('Authorization token is invalid format'),
      null,
      false,
      false
    )
    return
  }

  const email = parts[0]
  getOperator(null, { username: email }, (error, operator) => {
    if (error) {
      Sentry.captureException(error, { requestHeaders })
      logger(
        'Error: failed to check authorization token validity with error:',
        error
      )
      callback(error, null, false, false)
      return
    }

    if (operator.length < 1) {
      logger(
        'Can not validate rest token. There is no operator in database with email',
        email,
        'incoming token:',
        incomingRestToken
      )
      callback(errors.tokenInvalid(false), null, false, false)
      return
    }

    if (operator[0].rest_token !== incomingRestToken) {
      logger(
        'Operator rest token does not match incoming token. User token:',
        operator[0].rest_token,
        'incoming token:',
        incomingRestToken
      )
      callback(null, operator[0], false, false)
      return
    }

    if (
      moment().unix() - parseInt(parts[2]) >
      userHandler.authTokenExpirationTime
    ) {
      // Token has expired.
      logger('Rest token for:', email, 'has expired')
      callback(null, null, false, true)
      return
    }

    logger('rest tokens match')
    callback(null, operator[0], true, false)
  })
}

const getSpecificOperators = (comparisonColumnAndValues, callback) => {
  const getOperatorsQuery = query.selectWithAnd(
    dbConstants.tables.operators,
    null,
    comparisonColumnAndValues
  )
  sqlPool.makeQuery(getOperatorsQuery, callback)
}

const getOperatorsByFleets = (reqParam, callback) => {
  fleetHandler.getFleetAssociations(
    { fleet_id: reqParam.fleet_id },
    (error, fleetAssociations) => {
      if (error) {
        Sentry.captureException(error, { reqParam })
        logger(
          'Error: failed to fetch get fleet Associations with:',
          reqParam.fleet_id,
          'with error:',
          error
        )
        callback(errors.internalServer(false), null)
        return
      }
      const operators = _.uniq(fleetAssociations, (fleetAssociation) => {
        return fleetAssociation.operator_id
      })
      getSpecificOperators(
        { operator_id: _.pluck(operators, 'operator_id') },
        (error, specificOperators) => {
          if (error) {
            Sentry.captureException(error, { reqParam })
            logger(
              'Error: failed to get operators with:',
              _.pluck(operators.operator_id),
              'with error:',
              error
            )
            callback(errors.internalServer(false), null)
            return
          }
          _.each(specificOperators, (operator) => {
            operator.acl = _.findWhere(operators, {
              operator_id: operator.operator_id
            }).acl
          })
          specificOperators = _formatOperators(specificOperators)
          callback(null, specificOperators)
        }
      )
    }
  )
}

const _formatOperators = (operators) => {
  _.each(operators, (operator, index) => {
    operators[index] = _.omit(
      operator,
      'reset_password_token',
      'refresh_token',
      'password',
      'rest_token',
      'reset_password_expires'
    )
  })
  return operators
}

/**
 * This will fetch on-call operator for the given fleet
 *
 * @param {object} fleetId - fleet identification
 * @param {Function} callback
 */
const getOnCallOperator = (fleetId, callback) => {
  fleetHandler.getFleetAssociations(
    { fleet_id: fleetId },
    (error, fleetAssociations) => {
      if (error) {
        Sentry.captureException(error, { fleetId })
        logger(
          'Error: failed to get on-call operators. Error getting fleet association for fleet:',
          fleetId,
          'with error:',
          errors.errorWithMessage(error)
        )
        callback(errors.internalServer(false), null)
        return
      }
      if (fleetAssociations.length !== 0) {
        const onCallFleetOperator = _.findWhere(fleetAssociations, {
          on_call: 1
        })
        if (onCallFleetOperator) {
          getSpecificOperators(
            { operator_id: onCallFleetOperator.operator_id },
            (error, specificOperators) => {
              if (error) {
                Sentry.captureException(error, { fleetId })
                logger(
                  'Error: failed to get operator with:',
                  onCallFleetOperator.operator_id,
                  'with error:',
                  error
                )
                callback(errors.internalServer(false), null)
                return
              }
              callback(errors.noError(), specificOperators[0])
            }
          )
        } else {
          callback(errors.noError(), null)
        }
      } else {
        callback(errors.noError(), null)
      }
    }
  )
}

module.exports = {
  getOperatorsByFleets: getOperatorsByFleets,
  getOperator: getOperator,
  getFleets: getFleets,
  login: login,
  isAuthorizationTokenValid: isAuthorizationTokenValid,
  getOnCallOperator: getOnCallOperator
}
