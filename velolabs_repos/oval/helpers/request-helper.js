'use strict'

const platform = require('@velo-labs/platform')
const userHandler = require('./../handlers/user-handler')
const _ = require('underscore')
const errors = platform.errors
const ovalResponse = require('./../utils/oval-response')
const logger = platform.logger
const operatorHandler = require('./../handlers/operator-handler')
const fleetHandler = require('./../handlers/fleet-handler.js')

/*
 *
 * This method will check if the user is valid to pass into the Api.
 *
 * @param {object} req
 * @param {object} res
 * @param {boolean} shouldCheckVerification
 * @param {function} callback
 */
const userAuthorizationValidator = (req, res, shouldCheckVerification, callback) => {
  if (!_.has(req.headers, 'authorization') && !_.has(req.headers, 'api-token')) {
    logger('Failed to authorize user. No authorization token in the request headers.')
    ovalResponse(res, errors.tokenInvalid(true), null)
    callback(errors.tokenInvalid(false), null)
    return
  }
  if (req.header('api-token')) {
    _validateApiAuthorization(req, res, callback)
  } else {
    _validateUserAuthorization(req, res, shouldCheckVerification, callback)
  }
}

/**
 * Ensure that the requester is a valid user to pass to the API.
 *
 * This method is a promise-based wrapper for `userAuthorizationValidator`. Due
 * to an existing quirk in how `userAuthorizationValidator` works, this method
 * never calls `reject` since a response is returned when the user is not valid.
 *
 * @param {Object} req Express request
 * @param {Object} res Express response object
 * @param {boolean} [shouldCheckVerification] Whether or not to ensure the user is verified
 *
 * @returns Promise<Object> Promise eventually resolving with authorized client.
 */
const authorizeUser = (req, res, shouldCheckVerification = true) => {
  return new Promise((resolve, reject) => {
    userAuthorizationValidator(req, res, shouldCheckVerification, (_, user) => {
      resolve(user)
    })
  })
}

const _validateUserAuthorization = (req, res, shouldCheckVerification, callback) => {
  userHandler.isAuthorizationTokenValid(req.headers, (error, user, isValid, hasExpired) => {
    if (error) {
      logger('Error: authenticating authorization token:', error)
      ovalResponse(res, errors.formatErrorForWire(error), null)
      callback(error, user)
      return
    }

    if (!isValid) {
      logger(
        'Failed to authenticate authorization token:',
        req.headers.authorization,
        'Rest token invalid'
      )
      ovalResponse(res, errors.tokenInvalid(true), null)
      callback(errors.tokenInvalid(false), user)
      return
    }

    if (hasExpired) {
      logger(
        'Failed to authenticate authorization token:',
        req.headers.authorization,
        'Rest token has expired'
      )
      ovalResponse(res, errors.tokenInvalid(true), null)
      callback(errors.tokenInvalid(false), user)
      return
    }

    if (!user) {
      logger('Failed to authenticate user:', user.user_id, 'no user in database')
      ovalResponse(res, errors.resourceNotFound(true), null)
      callback(errors.resourceNotFound(false), null)
      return
    }

    if (shouldCheckVerification) {
      if (user.verified) {
        callback(null, user)
      } else {
        logger('user:', user.users_id, 'is not verified')
        ovalResponse(res, errors.unauthorizedAccess(true), null)
        callback(errors.unauthorizedAccess(true), null)
      }
    } else {
      callback(null, user)
    }
  })
}

const _validateApiAuthorization = (req, res, callback) => {
  fleetHandler.validateFleetToken({api_token: req.header('api-token')}, (error, fleet) => {
    if (error) {
      ovalResponse(res, errors.formatErrorForWire(error), null)
      return callback(error, null)
    }
    callback(errors.noError(), fleet)
  })
}

/**
 * This method will check if the operator is valid to pass into the Api.
 *
 * @param {object} req
 * @param {object} res
 * @param {function} callback
 */
const operatorAuthorizationValidator = (req, res, callback) => {
  if (!_.has(req.headers, 'authorization') && !_.has(req.headers, 'api-token')) {
    logger('Failed to authorize user. No authorization token in the request headers.')
    ovalResponse(res, errors.tokenInvalid(true), null)
    callback(errors.tokenInvalid(false), null)
    return
  }
  if (req.header('api-token')) {
    _validateApiAuthorization(req, res, callback)
  } else {
    _validateOperatorAuthorization(req, res, callback)
  }
}

const _validateOperatorAuthorization = (req, res, callback) => {
  operatorHandler.isAuthorizationTokenValid(req.headers, (error, operator, isValid, hasExpired) => {
    if (error) {
      logger('Error: authenticating authorization token:', error)
      ovalResponse(res, errors.formatErrorForWire(error), null)
      callback(error, operator)
      return
    }

    if (!isValid) {
      logger(
        'Failed to authenticate authorization token:',
        req.headers.authorization,
        'Rest token invalid'
      )
      ovalResponse(res, errors.tokenInvalid(true), null)
      callback(errors.tokenInvalid(false), operator)
      return
    }

    if (hasExpired) {
      logger(
        'Failed to authenticate authorization token:',
        req.headers.authorization,
        'Rest token has expired'
      )
      ovalResponse(res, errors.tokenInvalid(true), null)
      callback(errors.tokenInvalid(false), operator)
      return
    }

    if (!operator) {
      logger('Failed to authenticate user:', operator.username, 'no user in database')
      ovalResponse(res, errors.resourceNotFound(true), null)
      callback(errors.resourceNotFound(false), null)
      return
    }
    logger(operator.username, 'has a valid auth token')
    callback(errors.noError(), operator)
  })
}

const hasPassedVerificationCheck = (req, res, user) => {
  if (user.verified) {
    return true
  }

  logger('user:', user.users_id, 'is not verified')
  ovalResponse(res, errors.unauthorizedAccess(true), null)
}

module.exports = {
  authorizeUser,
  userAuthorizationValidator: userAuthorizationValidator,
  hasPassedVerificationCheck: hasPassedVerificationCheck,
  operatorAuthorizationValidator: operatorAuthorizationValidator
}
