'use strict'

const platform = require('@velo-labs/platform')
const queryCreator = platform.queryCreator
const sqlPool = platform.sqlPool
const config = require('./../config')
const _ = require('underscore')
const errors = platform.errors
const encryptionHandler = platform.encryptionHandler
const logger = platform.logger
const responseCodes = platform.responseCodes
const moment = require('moment')

const twilioHandler = require('./twilo-handler')
const phoneNumberHelper = require('./../helpers/phone-number-helper')
const S3Handler = platform.S3Handler
const userTypes = require('./../constants/user-types')
const ConfirmationCodeHandler = require('./confirmation-code-handler')
const confirmationCodeResults = require('./../constants/confirmation-code-results')
const confirmationCodeTypes = require('./../constants/confirmation-code-types')
const dbConstants = platform.dbConstants
const FacebookAuthenticator = require('./../helpers/facebook-authenticator')
const mailHandler = platform.mailHandler
const s3Constants = platform.s3Constants
const userConstants = platform.userConstants
const bookingHandler = require('./booking-handler')
const tripHandler = require('./trip-handler')
const privateAccountHandler = require('./private-account-handler')
const operatorHandler = require('./operator-handler')
const lockHandler = require('./lock-handler')
const bikeHandler = require('./bike-handler')
const hubsHandler = require('./hubs-handler')
const miscConstants = require('./../constants/misc-constants')
const paymentHandler = require('./payment-handler')
const reservationHelpers = require('./reservation-handlers/helpers')
const db = require('./../db')
const util = require('util')
const {configureSentry} = require('../utils/configureSentry')

const Sentry = configureSentry()

/**
 * This property is used to separate the user's name and the expiration date
 * in a decoded (not base64 encoded) authorization token.
 *
 * @type {string}
 * @private
 */
const authorizationTokenDeliminator = '*deliminator*'

/**
 * This property determines how many milli seconds and an authorization token will
 * be valid.
 *
 * @type {number}
 */
const authTokenExpirationTime = 28 * 24 * 3600 * 1000

/**
 * This method checks if all the fields are set on a User object such that the
 * User can be processed.
 *
 * @param {Object} user
 * @returns {boolean}
 */
const isUserValid = (user) => {
  return (
    _.has(user, 'users_id') &&
    _.has(user, 'user_type') &&
    _.has(user, 'reg_id') &&
    _.has(user, 'password') &&
    _.has(user, 'is_signing_up')
  )
}

/**
 * To get user records with given properties.
 *
 * @param {Array} columns
 * @param {Array} values
 * @param {Function} callback
 * @private
 */
const _getUser = (columns, values, callback) => {
  // const query = queryCreator.selectWithOr(dbConstants.tables.users, null, columns, values);
  sqlPool.makeQuery(queryCreator.selectWithOr(dbConstants.tables.users, null, columns, values), callback)
}

/**
 * This method handles the registration/update of a user.
 *
 * @param {Object} incomingUser - The user that the client is sending to the server
 * @param {Function} callback
 */
const processUser = (incomingUser, callback) => {
  // TODO: We'll probably want to throttle this method so that an attacker cannot
  // try a brute force attack.
  logger('processing incoming user', _.omit(incomingUser, 'password'))

  const isSigningUp = incomingUser.is_signing_up
  incomingUser = _updateUserPhoneNumberAndUsersId(incomingUser)
  getUserWithUsersId(incomingUser.users_id, (error, user) => {
    if (error) {
      Sentry.captureException(error, { incomingUser })
      logger(
        'Error: failed to fetch user with id:',
        incomingUser.users_id,
        'with error',
        error,
        'message:',
        errors.errorWithMessage(error)
      )
      callback(errors.internalServer(false), null)
      return
    }

    incomingUser = _.omit(incomingUser, 'is_signing_up')
    if (user) {
      // This is the case where the user is already in the database.
      // Let's try and compare the password of the incoming user with the password
      // of the user in the database.
      if (
        encryptionHandler.verifyPassword(incomingUser.password, user.password)
      ) {
        _verifyUser(incomingUser, user, (isVerified) => {
          if (incomingUser.user_type === 'facebook' && !isVerified) {
            callback(errors.fbAuthenticationFailure(false), null)
            return
          }
          _saveUser(
            _.pick(incomingUser, 'reg_id', 'max_locks'),
            false,
            (error) => {
              if (error) {
                Sentry.captureException(error, { incomingUser })
                callback(errors.internalServer(false), null)
                return
              }

              getUserWithUsersId(user.users_id, (error, user) => {
                if (error) {
                  Sentry.captureException(error, { incomingUser })
                  return
                }
                callback(null, user)
              })
            }
          )
        })
      } else {
        logger('Failed to verify password for:', user.users_id)
        callback(errors.invalidPassword(false), null)
      }
    } else if (isSigningUp) {
      // This case occurs when the user is not in the database.
      logger(
        'user with users id:',
        incomingUser.users_id,
        'is not in the db. Signing up new user.'
      )
      if (_.has(incomingUser, 'phone_number')) {
        incomingUser.phone_number =
          phoneNumberHelper.formatPhoneNumber(incomingUser)
        if (incomingUser.user_type === userTypes.ellipse) {
          incomingUser.users_id = incomingUser.phone_number
        }
      }
      if (!!incomingUser.device_model && !!incomingUser.device_os) {
        incomingUser.device_info =
          incomingUser.device_model + ' ' + incomingUser.device_os
      }
      _verifyUser(incomingUser, null, (isVerified) => {
        if (incomingUser.user_type === userTypes.facebook && !isVerified) {
          logger(
            'Error: could not save user:',
            incomingUser.users_id,
            'FB auth failed'
          )
          callback(errors.fbAuthenticationFailure(false), null)
          return
        }
        const userObj = Object.assign(
          {
            email:
              incomingUser.user_type === userTypes.lattis
                ? incomingUser.users_id
                : incomingUser.email,
            password: encryptionHandler.newPassword(incomingUser.password),
            date_created: moment().unix(),
            verified: isVerified,
            accepted_terms: false,
            rest_token: encryptionHandler.encryptDbValue(
              _newAuthToken(incomingUser.users_id, null)
            ),
            refresh_token: encryptionHandler.encryptDbValue(
              _newRefreshToken(incomingUser.users_id, incomingUser.user_type)
            )
          },
          _.pick(
            incomingUser,
            'title',
            'first_name',
            'last_name',
            'phone_number',
            'users_id',
            'user_type',
            'reg_id',
            'max_locks',
            'country_code',
            'device_info',
            'device_language'
          )
        )
        saveAndReturnUpdatedUser(userObj, true, (error, user) => {
          if (error) {
            Sentry.captureException(error, { incomingUser })
            return
          }
          callback(null, user)
        })
      })
    } else {
      logger('No user in database with users id:', incomingUser.users_id)
      callback(errors.resourceNotFound(false), null)
    }
  })
}

/**
 * Saves the user phone information
 *
 * @param {Object} user
 * @param {Object} deviceMetadata
 * @param {Function} callback
 */
const processUserMetadata = (user, deviceMetadata, callback) => {
  if (!deviceMetadata.device_model && !deviceMetadata.device_os) {
    callback(errors.noError())
    return
  }
  const deviceInfo =
    deviceMetadata.device_model + ' ' + deviceMetadata.device_os
  // check if the previous device is same as current device
  if (!!user.device_info && user.device_info === deviceInfo) {
    _saveUser({ user_id: user.user_id, device_info: deviceInfo, device_language: deviceMetadata.device_language }, false, () => {
      return (error) => {
        if (error) {
          Sentry.captureException(error, { user })
          callback(errors.internalServer(false))
          return
        }
        callback(errors.noError())
      }
    })
    callback(errors.noError())
    return
  }
  _saveUser(
    { user_id: user.user_id, device_info: deviceInfo, device_language: deviceMetadata.device_language },
    false,
    (error) => {
      if (error) {
        Sentry.captureException(error, { user })
        callback(errors.internalServer(false))
        return
      }
      callback(errors.noError())
    }
  )
}

/**
 * Saves the user to the database and, through the callback function, gives the updated
 * user object that can be returned for consumption by the client.
 *
 * @param {Object} user
 * @param {boolean} isNewUser
 * @param {Function} callback
 * @private
 */
const _saveUser = (user, isNewUser, callback) => {
  let query
  if (isNewUser) {
    logger('inserting new users with users id:', user.users_id)
    if (!user['device_language']) {
      user['language_preference'] = 'en'
      delete user['device_language']
    } else {
      user['language_preference'] = user['device_language']
      delete user['device_language']
    }
    query = queryCreator.insertSingle('users', user)
  } else if (_.has(user, 'user_id')) {
    logger('updating user with user id:', user.user_id)
    const userId = user.user_id
    if (!user['device_language']) {
      user['language_preference'] = 'en'
      delete user['device_language']
    } else {
      user['language_preference'] = user['device_language']
      delete user['device_language']
    }
    query = queryCreator.updateSingle('users', _.omit(user, 'user_id'), {
      user_id: userId
    })
  } else {
    logger('updating user with users id:', user.users_id)
    if (!user['device_language']) {
      user['language_preference'] = 'en'
      delete user['device_language']
    } else {
      user['language_preference'] = user['device_language']
      delete user['device_language']
    }
    query = queryCreator.updateSingle('users', user, {
      users_id: user.users_id
    })
  }

  sqlPool.makeQuery(query, (error) => {
    if (error) {
      Sentry.captureException(error, { user, query })
      logger(
        'Error: saving new user to Users Database with query:',
        query,
        'error:',
        errors.errorWithMessage(error)
      )
      if (error.code === 'ER_DUP_ENTRY') {
        callback(
          errors.customError(
            'Duplicate phone number',
            responseCodes.Unauthorized,
            'Duplicate phone number'
          )
        )
        return
      }
      callback(errors.internalServer(false), null)
    } else {
      logger('successfully saved user:', user.user_id || user.users_id)
      callback(null, user)
    }
  })
}

/**
 * This method updates a user object with a formatted `phone_number` and `users_id`.
 *
 * @param {object} user
 * @param {boolean} needsNewRestToken
 * @returns {object} updated user object
 * @private
 */
const _updateUserPhoneNumberAndUsersId = (user, needsNewRestToken = false) => {
  if (_.has(user, 'country_code')) {
    user.country_code = user.country_code.toLowerCase()
  }

  if (user.phone_number && user.phone_number !== '') {
    user.phone_number = phoneNumberHelper.formatPhoneNumber(user)
  }

  if (
    user.user_type === userTypes.ellipse &&
    user.users_id !== user.phone_number
  ) {
    user.users_id = user.phone_number
  }

  if (needsNewRestToken) {
    user.rest_token = encryptionHandler.encryptDbValue(
      _newAuthToken(user.users_id, null)
    )
  }

  return user
}

/**
 * This method saves a user and then fetches the user and returns the updated user
 * in the callback.
 *
 * @param {object} user
 * @param {boolean} isNewUser
 * @param {function} callback
 */
const saveAndReturnUpdatedUser = (user, isNewUser, callback) => {
  _saveUser(user, isNewUser, (error) => {
    if (error) {
      Sentry.captureException(error, { user, isNewUser })
      logger(
        'Error: could not save and return new user.',
        'Failed with error:',
        error
      )
      callback(errors.internalServer(false), null)
      return
    }

    _.has(user, 'user_id')
      ? getUserWithUserId(user.user_id, callback)
      : getUserWithUsersId(user.users_id, callback)
  })
}

/**
 * This method selects only the properties that should be sent over the wire
 * from a user object.
 *
 * @param {object} user
 * @param {boolean} includeSensitiveData
 * @param {boolean} includeTokens
 * @returns {object} - formatted user
 */
const formatUserForWire = (
  user,
  includeSensitiveData = true,
  includeTokens = false
) => {
  let params = [
    'user_id',
    'user_type',
    'title',
    'first_name',
    'last_name',
    'phone_number',
    'email'
  ]

  if (includeSensitiveData) {
    if (_.has(user, 'verified')) {
      user.verified = !!user.verified
    }

    const extraParams = [
      'users_id',
      'username',
      'country_code',
      'verified',
      'max_locks',
      'phone_number',
      'country_code',
      'email'
    ]

    params = params.concat(extraParams)
  }

  if (includeTokens) {
    const tokenParams = ['rest_token', 'refresh_token']

    params = params.concat(tokenParams)
  }

  return _.pick(user, params)
}

/**
 * This method fetches users with a given Id.
 *
 * @param {Number} userId
 * @param {Function} callback
 */
const getUserWithUserId = (userId, callback) => {
  const query = queryCreator.selectWithAnd('users', null, { user_id: userId })
  sqlPool.makeQuery(query, (error, users) => {
    if (error) {
      Sentry.captureException(error, { userId, query })
      logger('Error: failed to fetch user:', userId, 'with error:', error)
      callback(errors.internalServer(false), null)
      return
    }

    if (users && users.length > 0) {
      callback(null, users[0])
      return
    }

    callback(null, null)
  })
}

/**
 * This method fetches users (should only be one user - if not there is a bug) with
 * a given Id.
 *
 * @param {string} usersId
 * @param {Function} callback
 */
const getUserWithUsersId = (usersId, callback) => {
  const query = queryCreator.selectWithAnd('users', null, {
    users_id: usersId
  })
  sqlPool.makeQuery(query, (error, users) => {
    if (error) {
      Sentry.captureException(error, { usersId, query })
      logger('Error: failed to fetch user:', usersId, 'with error:', error)
      callback(errors.internalServer(false), null)
      return
    }

    if (users && users.length > 0) {
      callback(null, users[0])
      return
    }

    callback(null, null)
  })
}

/**
 * This method fetches a user by phone number.
 *
 * @param {string} phoneNumber
 * @param {string || null} countryCode
 * @param {function} callback
 */
const getUserWithPhoneNumber = (phoneNumber, countryCode, callback) => {
  let user = {}
  user.phone_number = phoneNumber
  if (countryCode) {
    user.country_code = countryCode
  }

  phoneNumber = phoneNumberHelper.formatPhoneNumber(user)
  const query = queryCreator.selectWithAnd(dbConstants.tables.users, null, {
    phone_number: phoneNumber
  })
  sqlPool.makeQuery(query, (error, users) => {
    if (error) {
      Sentry.captureException(error, { phoneNumber, countryCode, query })
      logger(
        'Error: failed to fetch user with phone number:',
        phoneNumber,
        'with error:',
        error
      )
      callback(errors.internalServer(false), null)
      return
    }

    if (users && users.length > 0) {
      callback(null, users[0])
      return
    }

    callback(null, null)
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
  const decryptedToken = decryptAuthToken(incomingRestToken)
  logger('decrypted_token:', decryptedToken)
  const parts = decryptedToken.split(authorizationTokenDeliminator)
  if (parts.length !== 2) {
    callback(
      new Error('Authorization token is invalid format'),
      null,
      false,
      false
    )
    return
  }

  const usersId = parts[0]
  getUserWithUsersId(usersId, (error, user) => {
    if (error) {
      Sentry.captureException(error, { usersId, incomingRestToken })
      logger(
        'Error: failed to check authorization token validity with error:',
        error
      )
      callback(error, null, false, false)
      return
    }

    if (!user) {
      logger(
        'Can not validate rest token. There is no user in database with users_id',
        usersId,
        'incoming token:',
        incomingRestToken
      )
      callback(errors.tokenInvalid(false), null, false, false)
      return
    }

    if (user.rest_token !== incomingRestToken) {
      logger(
        'User rest token does not match incoming token. User token:',
        user.rest_token,
        'incoming token:',
        incomingRestToken
      )
      callback(null, user, false, false)
      return
    }

    if (moment().unix() - parseInt(parts[2]) > authTokenExpirationTime) {
      // Token has expired.
      logger('Rest token for:', usersId, 'has expired')
      callback(null, null, false, true)
      return
    }
    callback(null, user, true, false)
  })
}

/**
 * This method returns the unencrypted value of the user's authorization token.
 *
 * @param {string} encodedToken
 * @returns {*|string}
 */
const decryptAuthToken = (encodedToken) => {
  return encryptionHandler.decryptDbValue(encodedToken)
}

/**
 * This method creates a new authorization token with an expiration value included.
 *
 * @param {string} usersId
 * @param {string || null} password
 * @returns {string}
 * @private
 */
const _newAuthToken = (usersId, password) => {
  return (
    usersId +
    authorizationTokenDeliminator +
    (moment().unix() + authTokenExpirationTime).toString() +
    (password ? ':' + password : '')
  )
}

const _newRefreshToken = (usersId, userType) => {
  return encryptionHandler.sha256Hash(
    userType + moment().unix().toString() + usersId
  )
}

/**
 * This method attempts to verify an incoming user.
 *
 * @param {object} incomingUser
 * @param {object} dbUser
 * @param {function} callback
 * @private
 */
const _verifyUser = (incomingUser, dbUser, callback) => {
  if (incomingUser.user_type === userTypes.facebook) {
    _authenticateFBUser(incomingUser, callback)
  } else {
    callback(dbUser ? dbUser.verified : false)
  }
}

/**
 * This method will authenticate a Facebook user against the Facebook API.
 *
 * @param {object} fbUser
 * @param {function} callback
 * @private
 */
const _authenticateFBUser = (fbUser, callback) => {
  const authenticator = new FacebookAuthenticator(fbUser)
  authenticator.authenticate((error, isVerified) => {
    error ? callback(false) : callback(isVerified)
  })
}

/**
 * This method updates and saves a user object with the values of the given properties object
 * if the properties objects keys match the user object keys.
 *
 * @param {object} user
 * @param {object} properties
 * @param {function} callback
 */
const updateUser = (user, properties, callback) => {
  properties = _.omit(
    properties,
    'user_id',
    'user_type',
    'confirmation_code',
    'role_id',
    'verified',
    'accepted_terms',
    'max_locks',
    'rest_token',
    'refresh_token',
    'date_created',
    'users_id',
    'phone_number',
    'country_code',
    'password',
    'username',
    'address_id',
    'reg_id'
  )

  _.each(properties, (propertyValue, propertyKey) => {
    if (_.has(user, propertyKey)) {
      user[propertyKey] = propertyValue
    }
  })

  saveAndReturnUpdatedUser(user, false, callback)
}

/**
 * This method updates the password of an existing user.
 *
 * @param {object} user
 * @param {number || string} code - confirmation code
 * @param {string} newPassword
 * @param {function} callback
 */
const updatePassword = (user, code, newPassword, callback) => {
  if (user.user_type !== userTypes.lattis) {
    _checkConfirmationCode(
      user,
      code,
      confirmationCodeTypes.updatePassword,
      (error) => {
        if (error) {
          Sentry.captureException(error, { user, code })
          logger('Error: failed to update password for user:', user.user_id)
          callback(error)
          return
        }

        user.password = encryptionHandler.newPassword(newPassword)
        _saveUser(user, false, (error) => {
          if (error) {
            Sentry.captureException(error, { user })
            logger(
              'Error: updating password for user:',
              user.user_id,
              'Error:',
              errors.errorWithMessage(error)
            )
            callback(error)
            return
          }

          logger('Updated password for user:', user.user_id)
          callback(null)
        })
      }
    )
  } else {
    user.password = encryptionHandler.newPassword(newPassword)
    _saveUser(user, false, (error) => {
      if (error) {
        Sentry.captureException(error, { user })
        logger(
          'Error: updating password for user:',
          user.user_id,
          'Error:',
          errors.errorWithMessage(error)
        )
        callback(error)
        return
      }

      logger('Updated password for user:', user.user_id)
      callback(null)
    })
  }
}

/**
 * This method allows a forgotten password to be changed.
 *
 * @param {object} user
 * @param {number || string} code - confirmation code
 * @param {string} newPassword
 * @param {function} callback
 */
const forgotPassword = (user, code, newPassword, callback) => {
  _checkConfirmationCode(
    user,
    code,
    confirmationCodeTypes.forgotPassword,
    (error) => {
      if (error) {
        Sentry.captureException(error, { user, code })
        logger(
          'Error: failed to change forgotten password for user:',
          user.user_id
        )
        callback(error)
        return
      }

      user.password = encryptionHandler.newPassword(newPassword)
      _saveUser(user, false, (error) => {
        if (error) {
          Sentry.captureException(error, { user })
          logger(
            'Error: changing forgotten password for user:',
            user.user_id,
            'Error:',
            errors.errorWithMessage(error)
          )
          callback(error)
          return
        }

        logger('Changed forgotten password for user:', user.user_id)
        callback(null)
      })
    }
  )
}

/**
 * This method updates the user phone number if the given confirmation code is valid.
 *
 * @param {object} user
 * @param {string || number} code
 * @param {string} newPhoneNumber
 * @param {string || null} countryCode
 * @param {function} callback
 */
const updatePhoneNumber = (
  user,
  code,
  newPhoneNumber,
  countryCode,
  callback
) => {
  _checkConfirmationCode(
    user,
    code,
    confirmationCodeTypes.updatePhoneNumber,
    (error) => {
      if (error) {
        Sentry.captureException(error, { user, code })
        logger('Error: failed to update phone number for user:', user.user_id)
        callback(error)
        return
      }

      user.phone_number = newPhoneNumber
      if (countryCode) {
        user.country_code = countryCode
      }

      user.phone_number = phoneNumberHelper.formatPhoneNumber(user)
      if (user.user_type === userTypes.ellipse) {
        user.users_id = user.phone_number
      }
      _saveUser(user, false, (error) => {
        if (error) {
          Sentry.captureException(error, { user })
          logger(
            'Error: updating phone number for user:',
            user.user_id,
            'Error:',
            errors.errorWithMessage(error)
          )
          callback(error)
          return
        }

        logger('Updated phone number for user:', user.user_id)
        callback(null)
      })
    }
  )
}

/**
 * This method updates the user email if the given confirmation code is valid.
 *
 * @param {object} user
 * @param {string || number} code
 * @param {string} newEmail
 * @param {function} callback
 */
const updateEmail = (user, code, newEmail, callback) => {
  _checkConfirmationCode(
    user,
    code,
    confirmationCodeTypes.updateEmail,
    (error) => {
      if (error) {
        Sentry.captureException(error, { user, code })
        logger('Error: failed to update Email for user:', user.user_id)
        callback(error)
        return
      }

      user.email = newEmail

      if (user.user_type === userTypes.lattis) {
        user.users_id = user.email
      }
      _saveUser(user, false, (error) => {
        if (error) {
          Sentry.captureException(error, { user })
          logger(
            'Error: updating Email for user:',
            user.user_id,
            'Error:',
            errors.errorWithMessage(error)
          )
          callback(error)
          return
        }

        logger('Updated Email for user:', user.user_id)
        callback(null)
      })
    }
  )
}

/**
 * This method re-authenticates and user and refreshes their rest token.
 * It should only be used by facebook users when they initially sign up/in.
 *
 * @param {number} userId
 * @param {string} password
 * @param {function} callback
 */
const getRestTokens = (userId, password, callback) => {
  getUserWithUserId(userId, (error, user) => {
    if (error) {
      Sentry.captureException(error, { userId })
      logger('Error: updating rest token. Failed to get user with Id:', userId)
      callback(errors.internalServer(false), null)
      return
    }

    if (!user) {
      logger('Error: getting rest token. No user with user id:', userId)
      callback(errors.resourceNotFound(false), null)
      return
    }

    if (!user.verified) {
      logger(
        'Error: getting rest token. user:',
        userId,
        'is not verified user:'
      )
      callback(errors.unverifiedUser(false), null)
      return
    }

    if (!encryptionHandler.verifyPassword(password, user.password)) {
      logger(
        'Could not get rest tokens for:',
        user.user_id,
        'passwords do not match'
      )
      callback(errors.invalidPassword(false), null)
      return
    }

    const tokens = {
      rest_token: user.rest_token,
      refresh_token: user.refresh_token
    }

    callback(null, tokens)
  })
}

/**
 * This method re-authenticates and user and refreshes their rest token.
 *
 * @param {number} userId
 * @param {string} refreshToken
 * @param {function} callback
 */
const refreshRestTokens = (userId, refreshToken, callback) => {
  getUserWithUserId(userId, (error, user) => {
    if (error) {
      Sentry.captureException(error, { userId })
      logger(
        'Error: refreshing refresh token. Failed to get user with Id:',
        userId
      )
      callback(errors.internalServer(false), null)
      return
    }

    if (!user) {
      logger('Error: refreshing refresh token. No user with user id:', userId)
      callback(errors.resourceNotFound(false), null)
      return
    }

    if (!user.verified) {
      logger(
        'Error: refreshing rest token. user:',
        userId,
        'is not verified user:'
      )
      callback(errors.unverifiedUser(false), null)
      return
    }

    if (refreshToken !== user.refresh_token) {
      logger(
        'Error: refreshing refresh token. Refresh token did not match for user:',
        userId
      )
      callback(errors.tokenInvalid(false), null)
      return
    }

    user.rest_token = encryptionHandler.encryptDbValue(
      _newAuthToken(user.users_id, null)
    )
    user.refresh_token = encryptionHandler.encryptDbValue(
      _newRefreshToken(user.users_id, user.user_type)
    )
    _saveUser(user, false, (error) => {
      if (error) {
        Sentry.captureException(error, { user })
        logger(
          'Error: updating rest token. Failed to save user:',
          user.user_id
        )
        callback(errors.internalServer(false), null)
        return
      }

      logger('updated rest token for user:', user.user_id)
      const tokens = {
        rest_token: user.rest_token,
        refresh_token: user.refresh_token
      }

      callback(null, tokens)
    })
  })
}

/**
 * This is to get active booking and active trip for the user
 *
 * @param {Object} user - user identification
 * @param {Function} callback
 */

const getCurrentStatus = (user, callback) => {
  bookingHandler.getActiveBooking(user, async (error, bookingDetails) => {
    if (error) {
      Sentry.captureException(error, { user })
      logger(
        'Error: getting current status for user with user_id:',
        user.user_id,
        'with error:',
        error
      )
      callback(error, null)
      Sentry.captureException(error, { user_id: user.user_id })
      return
    }
    tripHandler.getTrips(user, true, async (error, tripDetails) => {
      if (error) {
        Sentry.captureException(error, { user })
        logger(
          'Error: getting current status for user with user_id:',
          user.user_id,
          'with error:',
          error
        )
        callback(error, null)
        Sentry.captureException(error, tripDetails)
        return
      }
      const now = moment().unix()
      /*
      for (let trip of tripDetails) {
        try {
          if (trip.reservation_id && (!trip.date_endtrip || !trip.end_address)) {
            tripHandler.endTripWithCancelledOrElapsedReservation(trip)
          }
        } catch (error) {
          Sentry.captureException(error, tripDetails)
          throw errors.customError(error.message, responseCodes.Conflict, 'ErrorCancellingScheduledReservation', true)
        }
      }
      */
      tripDetails = _.filter(tripDetails, (trip) => {
        return (
          trip.date_created &&
          !trip.date_endtrip &&
          !trip.end_address &&
          trip.date_created <= now
        )
      })

      let payload = {
        trip: tripDetails.length > 0 ? _.pick(tripDetails[0], 'trip_id') : null,
        active_booking: null,
        support_phone: String(config.lattisSupportPhoneNumber),
        on_call_operator: null
      }

      tripDetails = tripDetails && tripDetails.length && tripDetails[0]
      if (tripDetails) {
        if (!tripDetails.date_endtrip) {
          if (tripDetails['rating_shown'] === 0) {
            payload.rating = { trip_id: tripDetails[0].trip_id }
          }
        }

        const getTripInformation = util.promisify(
          tripHandler.getTripInformation
        )
        try {
          const tripInfo = await getTripInformation({
            userId: user.user_id,
            tripId: tripDetails.trip_id
          })
          if (tripInfo && tripInfo.trip) {
            payload.trip = { ...payload.trip, ...tripInfo.trip }
          }
        } catch (error) {
          Sentry.captureException(error, { tripDetails })
          logger(
            'Missing trip information for user:',
            user.user_id,
            'with error:',
            error
          )
          throw new Error('An error occurred while getting trip information')
        }

        const { bike_id: bikeId, port_id: portId, hub_id: hubId } = tripDetails
        if (bikeId && !payload.bike) {
          const bike = await db
            .main('bikes')
            .where({ bike_id: bikeId })
            .rightJoin('ports', 'bikes.bike_uuid', 'ports.vehicle_uuid')
            .first()
          payload.vehicle = {
            is_docked: !!(bike && bike.vehicle_uuid)
          }
        } else if (portId && !payload.port) {
          const port = await db
            .main('ports')
            .where({ port_id: portId })
            .first()
          const hub = await db
            .main('hubs')
            .where({ uuid: port.hub_uuid })
            .first()
          const hubInfo = await hubsHandler.getHubById(hub.hub_id)
          payload.hub = hubInfo
        } else if (hubId && !payload.hub) {
          const hub = await hubsHandler.getHubById(hubId)
          payload.hub = hub
        }
      }

      let activeFleet
      if (payload.trip) {
        activeFleet = tripDetails.fleet_id
      } else {
        if (bookingDetails) {
          activeFleet = bookingDetails[0].fleet_id
          const activeBooking = _.extend(
            _.pick(
              bookingDetails[0],
              'bike_id',
              'port_id',
              'hub_id',
              'fleet_id',
              'booked_on'
            ),
            {
              till:
                bookingDetails[0].booked_on +
                bookingDetails[0].bike_booking_expiration
            },
            bookingDetails[0]
          )
          payload.active_booking = activeBooking
          const info = await tripHandler.getStandardDeviceInfo({
            bikeId: activeBooking.bike_id,
            portId: activeBooking.port_id,
            hubId: activeBooking.hub_id,
            fleetId: activeBooking.fleet_id
          })
          if (activeBooking.bike_id) {
            payload = { ...payload, ...info }
          } else payload = { ...payload, hub: info }
        }
      }

      let reservation = await reservationHelpers.retrieveCurrentReservation({
        user
      })

      if (!reservation) {
        reservation = await reservationHelpers.retrieveNextReservation({
          userId: user.user_id
        })
      }

      const tripPaymentTransaction =
        reservation && reservation.trip_payment_transaction

      if (
        tripPaymentTransaction &&
        (!tripPaymentTransaction.id || !tripPaymentTransaction.reservation_id)
      ) {
        delete reservation.trip_payment_transaction
      }

      payload.reservation = reservation

      if (activeFleet) {
        operatorHandler.getOnCallOperator(
          activeFleet,
          (error, onCallOperator) => {
            if (error) {
              Sentry.captureException(error, { reservation, activeFleet })
              callback(errors.internalServer(false), null)
              return
            }
            if (onCallOperator) {
              operatorHandler.getOperator(
                null,
                _.pick(onCallOperator, 'operator_id'),
                (error, operators) => {
                  if (error) {
                    Sentry.captureException(error, {
                      reservation,
                      onCallOperator
                    })
                    logger(
                      'Error: Getting current status for user',
                      user.user_id,
                      'Failed to get on-call operator',
                      error
                    )
                    callback(errors.internalServer(false), null)
                    return
                  }
                  payload.on_call_operator =
                    operators.length !== 0
                      ? operators[0].phone_number
                        ? String(operators[0].phone_number)
                        : null
                      : null
                  callback(null, payload)
                }
              )
            } else {
              callback(null, payload)
            }
          }
        )
      } else {
        callback(null, payload)
      }
    })
  })
}

/**
 * This is to get fleet associated with user
 *
 * @param {Object} user - user identification
 * @param {Function} callback
 */
const getFleetInfo = (user, callback) => {
  try {
    bookingHandler.getActiveBooking(user, (error, bookingDetails) => {
      if (error) {
        Sentry.captureException(error, { user })
        logger('Error: getting current status for user with user_id:', user.user_id, 'with error:', error)
        callback(error, null)
        return
      }
      tripHandler.getTrips(user, false, async (error, tripDetails) => {
        if (error) {
          Sentry.captureException(error, { user })
          logger('Error: getting current status for user with user_id:', user.user_id, 'with error:', error)
          callback(error, null)
          return
        }
        let trip
        let fleet
        if (tripDetails && tripDetails.length) {
          trip = tripDetails[0]
          fleet = await db.main('fleets').where({ fleet_id: trip.fleet_id }).first()
        }
        const payload = {
          fleet: _.isObject(fleet) ? fleet : null
        }

        // if we don't have a trip  for user let's try to use bookings
        if (bookingDetails && bookingDetails.length && !trip) {
          const booking = _.extend(_.pick(bookingDetails[0], 'bike_id', 'booked_on'),
            { till: bookingDetails[0].booked_on + bookingDetails[0].bike_booking_expiration })
          payload.fleet = await db.main('fleets').where({ fleet_id: booking.fleet_id }).first()
        }
        callback(null, payload)
      })
    })
  } catch (error) {
    Sentry.captureException(error, { user })
    logger(`Error: Failed to get fleetInfo for ${user}`)
    callback(error, null)
  }
}

/**
 * This method sends an email to the user with a verification code that they will
 * have to enter before they are allowed access to the rest of the API of lattis.
 *
 * @param {string} userId
 * @param {Object} appDetails
 * @param {string} appDetails.userAgent - The name of the app.
 * @param {string} appDetails.contentLanguage - The language used in the app.
 * @param {string} type
 * @param {string} email
 * @param {function} callback
 */
const emailUserConfirmationCode = (userId, type, email, appDetails = null, callback) => {
  getUserWithUserId(userId, (error, user) => {
    if (error) {
      Sentry.captureException(error, { userId })
      logger('Error: failed to email user verification code with error:', error)
      callback(errors.resourceNotFound(false))
      return
    }

    if (!user) {
      logger('Failed to email user verification code. No user in db with user_id', userId)
      callback(errors.resourceNotFound(false))
      return
    }

    if (type === confirmationCodeTypes.lattisAccount) {
      privateAccountHandler.getPrivateAccount({ user_id: user.user_id, verified: 0 }, (error, lattisAccount) => {
        if (error) {
          Sentry.captureException(error, { userId })
          logger('Error: failed to email user verification code with error:', error)
          callback(errors.internalServer(false))
          return
        }
        _sendConfirmationEmail(email || user.email, user, type, appDetails, (error) => {
          if (error) {
            Sentry.captureException(error, { userId })
            logger('Error: failed to email user verification code with error:', error)
            callback(errors.internalServer(false))
            return
          }

          callback(null)
        })
      })
    } else {
      _sendConfirmationEmail(email || user.email, user, type, appDetails, (error) => {
        if (error) {
          Sentry.captureException(error, { userId })
          logger('Error: failed to email user verification code with error:', error)
          callback(errors.internalServer(false))
          return
        }

        callback(null)
      })
    }
  })
}

const determineVerificationText = (appDetails, user) => {
  if (!appDetails) {
    return {
      source: 'Lattis',
      verificationText: user && user.language_preference === 'fr' ? userConstants.verificationTexts.lattisFrenchVerification : userConstants.verificationTexts.lattisVerification
    }
  }

  appDetails.verificationText = user && user.language_preference === 'fr' ? `Votre code de vérification ${appDetails.source} est:` : `Your ${appDetails.source} verification code is: `

  return appDetails
}
/**
 * This method creates a confirmation code object and stores it in the database.
 * It also text the email that is passed in the confirmation code.
 *
 * @param {string} email
 * @param {Object} appDetails
 * @param {string} appDetails.userAgent - The name of the app.
 * @param {string} appDetails.contentLanguage - The language used in the app.
 * @param {object} user
 * @param {string} type - type of confirmation code
 * @param {function} callback
 * @private
 */
const _sendConfirmationEmail = (email, user, type, appDetails, callback) => {
  const confirmationCodeHandler = new ConfirmationCodeHandler(user)
  confirmationCodeHandler.createCode(type, (error, code) => {
    if (error) {
      Sentry.captureException(error, { user })
      callback(error)
      return
    }

    let { source, verificationText } = determineVerificationText(appDetails, user)

    const message = `${verificationText}${code}`
    const textContent = user && user.language_preference === 'fr' ? ` Courriel de vérification de compte ${source}` : `${source} Account Verification Email`
    mailHandler.sendMail([email], textContent, message, false, source, (error) => {
      if (error) {
        Sentry.captureException(error, { user })
        logger(
          'Error: failed to mail verification code to:',
          email,
          'for user:',
          user.user_id,
          'Failed with error:',
          error
        )
        callback(error)
        return
      }

      logger('Mailed verification code', code, 'successfully to:', user.user_id, 'for type', type)
      callback(null)
    })
  })
}

/**
 * This method checks the confirmation code for email sign in.
 *
 * @param {string} userId
 * @param {string || integer} confirmationCode
 * @param {String} accountType
 * @param {function} callback
 */
const checkEmailConfirmationCode = async (userId, confirmationCode, accountType, callback) => {
  const user = await db.users('users').where({user_id: userId}).first()
  if (!user) {
    logger('Failed to check email user verification code. No user in db with user_id', userId)
    callback(errors.customError(
      `No user in the db with that user_id ${userId}`, responseCodes.ResourceNotFound,
      'ResourceNotFound', true), null)
    return
  }
  _checkConfirmationCode(user, confirmationCode, accountType, async (error) => {
    if (error) {
      Sentry.captureException(error, { userId })
      logger('Error: failed to check email confirmation code with error:', error)
      callback(error, null)
      return
    }
    if (accountType === confirmationCodeTypes.lattisSignIn) {
      const invitations = await db.users('private_fleet_users').where({email: user.email, access: 1, user_id: null})
      const privateAccountDetails = []
      const emailDomain = user.users_id.split('@')
      const domainString = `@${emailDomain[1]}`
      const domains = await db.main('domains').where({domain_name: domainString})
      if (domains.length) {
        privateAccountHandler.getPrivateAccount({
          email: user.email
        }, (error, privateAccounts) => {
          if (error) {
            Sentry.captureException(error, { userId })
            logger('Error: could not get private account for user : ',
              user.user_id, errors.errorWithMessage(error))
            callback(errors.customError(
              'Failed to retrieve a private account for user', responseCodes.ResourceNotFound,
              'ResourceNotFound'), null)
            return
          }

          const accounts = []
          _.each(domains, (domain) => {
            if (_.where(privateAccounts, { fleet_id: domain.fleet_id }).length === 0) {
              const fleetInvitation = _.findWhere(invitations, { fleet_id: domain.fleet_id })
              if (!fleetInvitation) {
                accounts.push({
                  user_id: user.user_id,
                  email: user.users_id,
                  fleet_id: domain.fleet_id,
                  verified: 1,
                  access: 1,
                  updated_at: moment().unix(),
                  access_mode: 'domain'
                })
              } else {
                privateAccountDetails.push({
                  private_fleet_user_id: fleetInvitation.private_fleet_user_id,
                  user_id: user.user_id,
                  fleet_id: fleetInvitation.fleet_id,
                  updated_at: moment().unix(),
                  access: 1,
                  verified: 1
                })
              }
            }
          })

          privateAccountHandler.insertPrivateAccounts(accounts, (error) => {
            if (error) {
              Sentry.captureException(error, { userId, accounts })
              logger('Error: Could not save private account.')
              callback(errors.customError(
                `An error occurred when saving private fleet user`, responseCodes.InternalServer,
                'InternalServer'), null)
              return
            }
            if (invitations.length > 0) {
              _.each(invitations, (invitation) => {
                if (_.where(privateAccountDetails, { fleet_id: invitation.fleet_id }).length === 0) {
                  privateAccountDetails.push({
                    private_fleet_user_id: invitation.private_fleet_user_id,
                    user_id: user.user_id,
                    fleet_id: invitation.fleet_id,
                    access: 1,
                    verified: 1,
                    updated_at: moment().unix(),
                    access_mode: 'domain'
                  })
                }
              })
              privateAccountHandler.updatePrivateAccounts(privateAccountDetails, (error) => {
                if (error) {
                  Sentry.captureException(error, {
                    userId,
                    privateAccountDetails
                  })
                  callback(error, null)
                  return
                }
                user.verified = true
                saveAndReturnUpdatedUser(user, false, (error, updatedUser) => {
                  if (error) {
                    Sentry.captureException(error, { user })
                    logger('Error: Could not save user.')
                    callback(errors.customError(
                      `An error occurred when saving private fleet user`, responseCodes.InternalServer,
                      'InternalServer'), null)
                    return
                  }
                  callback(null, updatedUser)
                })
              })
            } else {
              user.verified = true
              saveAndReturnUpdatedUser(user, false, (error, updatedUser) => {
                if (error) {
                  Sentry.captureException(error, { user })
                  logger('Error: Could not save user.')
                  callback(errors.customError(
                    `An error occurred when saving private fleet user`, responseCodes.InternalServer,
                    'InternalServer'), null)
                  return
                }
                callback(null, updatedUser)
              })
            }
          })
        })
      } else {
        // sometimes a private fleet has no associated domains, verify user and return
        user.verified = true
        saveAndReturnUpdatedUser(user, false, (error, updatedUser) => {
          if (error) {
            Sentry.captureException(error, { user })
            logger('Error: Could not save user.')
            callback(errors.customError(
              `An error occurred when saving private fleet user`, responseCodes.InternalServer,
              'InternalServer'), null)
            return
          }
          callback(null, updatedUser)
        })
      }
    } else {
      privateAccountHandler.getPrivateAccount({
        user_id: user.user_id
      }, async (error, lattisAccount) => {
        if (error) {
          Sentry.captureException(error)
          logger('Error: Could not find lattis account.')
          callback(errors.customError(
            'Could not find lattis account', responseCodes.ResourceNotFound,
            'ResourceNotFound'), null)
          return
        }
        try {
          const unverifiedAccounts = _.where(lattisAccount, { verified: 0, access: 1 })
          if (!unverifiedAccounts.length) {
            logger('Error: Unable to verify private account for user:', user.user_id,
              'No private network has been added with this Email.')
            callback(errors.customError(
              'Failed to add private account for user', responseCodes.ResourceNotFound,
              'ResourceNotFound'), null)
            return
          }

          const promises = unverifiedAccounts.map(async account => {
            const user = await db.users('private_fleet_users').update({verified: 1}).where({email: account.email})
            return user
          })
          await Promise.all(promises)
          callback(null, {user_id: user.user_id})
        } catch (error) {
          Sentry.captureException(error)
          throw errors.customError(
            `Failed to update private account`,
            platform.responseCodes.InternalServer,
            'InternalServerError',
            false
          )
        }
      })
    }
  })
}

/**
 * This method sends an sms to the user with a verification code that they will
 * have to enter before they are allowed access to the rest of the API.
 *
 * @param {number} userId
 * @param {string} type
 * @param {function} callback
 */
const textUserVerificationCode = (userId, type, appDetails, callback) => {
  getUserWithUserId(userId, (error, user) => {
    if (error) {
      Sentry.captureException(error, { userId })
      logger('Error: failed to text user verification code with error:', error)
      callback(error)
      return
    }

    if (!user) {
      logger('Failed to text user verification code. No user in db with id', userId)
      callback(errors.resourceNotFound(false))
      return
    }

    if (!user.phone_number) {
      logger('Failed to text user verification code. The user does not have a phone number')
      callback(errors.invalidParameter(false))
      return
    }

    _sendConfirmationCode(user.phone_number, user, type, appDetails, callback)
  })
}

/**
 * This method sends an sms to the user with a verification code that they will
 * have to enter before they can update their phone number
 *
 * @param {number} userId
 * @param {string} phoneNumber
 * @param {string || null} countryCode
 * @param {string} type
 * @param {function} callback
 */
const textUserVerificationCodeForPhoneUpdate = (userId, phoneNumber, countryCode, type, appDetails, callback) => {
  getUserWithUserId(userId, (error, user) => {
    if (error) {
      Sentry.captureException(error, { userId })
      logger('Error: failed to text user verification code for phone update with error:', error)
      callback(error)
      return
    }

    if (!user) {
      logger('Failed to text user verification code for phone update. No user in db with id', userId)
      callback(errors.resourceNotFound(false))
      return
    }

    let phoneNumberObject = { phone_number: phoneNumber }
    if (countryCode) {
      phoneNumberObject.country_code = countryCode
    }
    phoneNumber = phoneNumberHelper.formatPhoneNumber(phoneNumberObject)
    if (user.user_type !== userTypes.lattis) {
      getUserWithPhoneNumber(phoneNumber, countryCode, (error, oldUser) => {
        if (error) {
          Sentry.captureException(error, { userId })
          return callback(errors.internalServer(false), null)
        }
        // if (oldUser && oldUser.user_type === user.user_type) {
        if (oldUser && oldUser.user_id !== user.id) {
          logger('Error:', phoneNumber, 'is already registered for user', oldUser.user_id)
          // callback(errors.duplicateUser(true), null)
          callback({name: 'PhoneNumberExists', code: 409, message: 'The phone number is already assigned by another user'}, null)
          return
        }
        _sendConfirmationCode(phoneNumber, user, type, appDetails, callback)
      })
    } else {
      getUserWithPhoneNumber(phoneNumber, countryCode, (error, oldUser) => {
        if (error) {
          Sentry.captureException(error, { userId })
          return callback(errors.internalServer(false), null)
        }
        if (oldUser && oldUser.user_id !== user.user_id) {
          logger('Error:', phoneNumber, 'is already registered for user', oldUser.user_id)
          callback({name: 'PhoneNumberExists', code: 409, message: 'The phone number is already assigned by another user'}, null)
          return
        }
        _sendConfirmationCode(phoneNumber, user, type, appDetails, callback)
      })
    }
  })
}

/**
 * This method creates a confirmation code object and stores it in the database.
 * It also text the phone number that is passed in the confirmation code.
 *
 * @param {string} phoneNumber
 * @param {object} user
 * @param {string} type - type of confirmation code
 * @param {function} callback
 * @private
 */
const _sendConfirmationCode = (phoneNumber, user, type, appDetails, callback) => {
  const confirmationCodeHandler = new ConfirmationCodeHandler(user)
  confirmationCodeHandler.createCode(type, (error, code) => {
    if (error) {
      Sentry.captureException(error, { userId: user.user_id })
      callback(error)
      return
    }

    const { verificationText } = determineVerificationText(appDetails, user)
    const message = `${verificationText}${code}`

    twilioHandler.sendSMS(phoneNumber, message, (error) => {
      if (error) {
        Sentry.captureException(error, { userId: user.user_id })
        logger(
          'Error: failed to send verification code to:',
          phoneNumber,
          'for user:',
          user.user_id,
          'Failed with error:',
          error
        )
        callback(error)
        return
      }

      logger('Sent verification successfully to:', phoneNumber)
      callback(null)
    })
  })
}

/**
 * This method checks that the confirmation code sent by the user is authentic.
 *
 * @param {number || string} confirmationCode
 * @param {object} user
 * @param {string} type
 * @param {function} callback
 */
const checkUserConfirmationCode = (confirmationCode, user, type, callback) => {
  _checkConfirmationCode(user, confirmationCode, type, (error) => {
    if (error) {
      Sentry.captureException(error, { userId: user.user_id })
      logger('Error: failed to check user confirmation code for user:', user.user_id)
      callback(error, null)
      return
    }

    user.verified = true
    saveAndReturnUpdatedUser(user, false, (error, updatedUser) => {
      if (error) {
        Sentry.captureException(error, { userId: user.user_id })
        logger(
          'Error checking user verification code. Failed with error:',
          errors.errorWithMessage(error)
        )
        callback(error, null)
        return
      }

      callback(null, updatedUser)
    })
  })
}

/**
 * This method retrieves a user with a given `user_id` and saves
 * checks the confirmation code against the database.
 *
 * @param {number} userId
 * @param {string || number} confirmationCode
 * @param {string} type
 * @param {function} callback
 */
const checkConfirmationCode = (userId,
  confirmationCode,
  type,
  callback) => {
  getUserWithUserId(userId, (error, user) => {
    if (error) {
      Sentry.captureException(error, { userId })
      logger('Failed to check confirmation code. error:', error)
      callback(error, null)
      return
    }

    if (!user) {
      logger('Failed to check confirmation code. No user in db with id:', userId)
      callback(errors.resourceNotFound(false), null)
      return
    }

    checkUserConfirmationCode(confirmationCode, user, type, callback)
  })
}

/**
 * This method checks if a confirmation code is the code associated with
 * the user and confirmation code type in the database.
 *
 * @param {object} user
 * @param {string || number} confirmationCode
 * @param {string} type
 * @param {function} callback
 * @private
 */
const _checkConfirmationCode = (user, confirmationCode, type, callback) => {
  const confirmationCodeHandler = new ConfirmationCodeHandler(user)
  confirmationCodeHandler.checkCode(confirmationCode, type, (error, result) => {
    if (error) {
      Sentry.captureException(error, { user })
      logger('Error: failed to check user confirmation code')
      callback(error)
      return
    }

    if (!_.has(confirmationCodeResults, result)) {
      logger('Error: can not check user confirmation code. confirmation code result incorrect')
      callback(errors.internalServer(false))
      return
    }

    if (result === confirmationCodeResults.noCodeInDb) {
      logger('no confirmation code in db for user:', user.user_id, 'of type:', type)
      callback(errors.resourceNotFound(false))
      return
    }

    if (result === confirmationCodeResults.notConfirmed) {
      logger('confirmation code does not match for user:', user.user_id, 'of type:', type)
      callback(errors.confirmationCodeInvalid(false))
      return
    }

    callback(null)
  })
}

/**
 * This method takes a user and returns the full name.
 *
 * @param {object} user
 * @returns {string || null}
 */
const fullName = (user) => {
  let name = ''
  if (user.first_name) {
    name += user.first_name
  }

  if (user.first_name && user.last_name) {
    name += (' ' + user.last_name)
  } else if (user.last_name) {
    name += user.last_name
  }

  return name === '' ? null : name
}

/**
 * This method deletes an account from the database for the given user.
 *
 * @param {object} user
 * @param {function} callback
 */
const deleteAccount = (user, callback) => {
  const deleteQuery = queryCreator.deleteSingle(dbConstants.tables.users, { user_id: user.user_id })
  sqlPool.makeQuery(deleteQuery, (error) => {
    if (error) {
      Sentry.captureException(error, { user })
      logger('Failed to delete user:', user.user_id)
      callback(error)
      return
    }

    const deletedUser = {
      user_id: user.user_id,
      user_type: user.user_type,
      password: user.password,
      email: user.email,
      date: moment().unix()
    }

    const query = queryCreator.insertSingle('deleted_users', deletedUser)
    sqlPool.makeQuery(query, (error) => {
      if (error) {
        Sentry.captureException(error, { user, query, deletedUser })
        logger(
          'Error could not delete user. Failed to move user to deleted user table. Error:',
          errors.errorWithMessage(error)
        )
        return callback(error)
      }

      if (user.user_type === userTypes.lattis) {
        const deleteLattisAccountQuery = queryCreator.deleteSingle(dbConstants.tables.private_fleet_users,
          { user_id: user.user_id })
        sqlPool.makeQuery(deleteLattisAccountQuery, (error) => {
          if (error) {
            Sentry.captureException(error, { user, query, deletedUser })
            logger('Failed to delete lattis account of the user:', user.user_id)
            return callback(error)
          }
          callback(null)
        })
      } else {
        callback(null)
      }
    })
  })
}

/**
 * This method retrieves the current terms and conditions.
 *
 * @param {Object} user
 * @param {function} callback
 */
const getTermsAndConditions = (user, callback) => {
  const options = {
    bucket: s3Constants.termsAndConditions.bucket,
    key: s3Constants.termsAndConditions.file
  }

  if (user.user_type === userTypes.ellipse) {
    options.key = s3Constants.termsAndConditions.file
  } else if (user.user_type === userTypes.lattis) {
    options.key = s3Constants.termsAndConditions.file
  }

  const s3Handler = new S3Handler()
  s3Handler.download(options, (error, termsAndConditions) => {
    if (error) {
      Sentry.captureException(error, { user, options })
      logger('Error: failed to download terms and conditions with error:', error)
      return callback(errors.internalServer(false), null)
    }

    const terms = termsAndConditions.body.toString('utf8')
    callback(null, terms)
  })
}

/**
 * This method takes the appropriate action when the user accepts
 * or denies the terms and conditions in the app. If the user denies
 * the conditions, the user will be deleted from the database.
 *
 * @param {object} user
 * @param {boolean} didAccept
 * @param {function} callback
 */
const acceptTermsAndConditions = (user, didAccept, callback) => {
  const done = (error) => {
    if (error) {
      Sentry.captureException(error, { user })
      logger(
        'Error: failed to update terms and conditions for user:',
        user.user_id,
        'error:',
        errors.errorWithMessage(error)
      )
      return callback(errors.internalServer())
    }

    callback(null)
  }

  if (didAccept) {
    const query = queryCreator.updateSingle('users', { accepted_terms: true }, { user_id: user.user_id })
    sqlPool.makeQuery(query, done)
  } else {
    deleteAccount(user, done)
  }
}

/**
 * This method checks if the user has accepted the terms and conditions.
 *
 * @param {object} user
 * @param {function} callback
 */
const hasUserAcceptedTermsAndConditions = (user, callback) => {
  const query = queryCreator.selectWithAnd('users', ['accepted_terms'], { user_id: user.user_id })
  sqlPool.makeQuery(query, (error, fetchedUsers) => {
    if (error) {
      Sentry.captureException(error, { user })
      logger('Error: failed to check terms and conditions. Error', errors.errorWithMessage(error))
      return callback(errors.internalServer())
    }

    if (!fetchedUsers || fetchedUsers.count === 0) {
      logger('Error: failed to check terms and conditions. No user in db with id:', user.user_id)
      return callback(null, null)
    }

    callback(null, fetchedUsers[0].accepted_terms)
  })
}

/*
 * Retrieves the details of saved cards for users
 * @param {Object} columnsToSelect
 * @param {Object} comparisonColumnsAndValues
 * @param {Function} Callback with params {error, rows}
 */
const getCards = (columnsToSelect, comparisonColumnsAndValues, callback) => {
  const query = queryCreator.selectWithAnd(dbConstants.tables.user_payment_profiles, columnsToSelect, comparisonColumnsAndValues)
  sqlPool.makeQuery(query, callback)
}

/*
 * Retrieves the details of saved cards for users
 * @param {Object} requestParam
 * @param {Function} Callback with params {error, rows}
 */
const setCardPrimary = (requestParam, callback) => {
  let updatePaymentProfile = queryCreator.updateSingle(dbConstants.tables.user_payment_profiles, { 'is_primary': 0 }, { 'user_id': requestParam.user_id })
  sqlPool.makeQuery(updatePaymentProfile, (error) => {
    if (error) {
      Sentry.captureException(error, { requestParam })
      logger('Error: Unable to update user credit cards as non-primary with error: ', error)
      return callback(error, null)
    }
    let updatePaymentProfile = queryCreator.updateSingle(dbConstants.tables.user_payment_profiles, { 'is_primary': 1 }, { 'id': requestParam.id })
    sqlPool.makeQuery(updatePaymentProfile, (error) => {
      if (error) {
        Sentry.captureException(error, { requestParam, updatePaymentProfile })
        logger('Error: Unable to update user credit cards as primary with error: ', error)
        return callback(error, null)
      }
      callback(null, null)
    })
  })
}

/*
 * Delete cards for users
 * @param {Object} requestParam
 * @param {Function} Callback with params {error, rows}
 */
const deleteCard = (requestParam, callback) => {
  getCards(null, { id: requestParam.id }, (error, card) => {
    if (error) {
      Sentry.captureException(error, { requestParam })
      logger('Error: processing card:', error)
      callback(error, null)
      return
    }
    if (card.length > 0) {
      paymentHandler.deleteCard(card[0], (error) => {
        if (error) {
          Sentry.captureException(error, { requestParam })
          logger('Error: could not delete card from payment gateway', error)
          return callback(errors.invalidCreditCard(false), null)
        }
        callback(null, null)
      })
    } else {
      callback(errors.cardNotExist(false), null)
    }
  })
}

const addCards = (requestParam, callback) => {
  getUserWithUserId(requestParam.user_id, (error, user) => {
    if (error) {
      Sentry.captureException(error, { requestParam })
      logger(
        'Error: adding user payment profile. Failed to get user',
        requestParam.user_id,
        errors.errorWithMessage(error)
      )
      callback(error, null)
      return
    }

    if (!user) {
      const error = errors.customError(
        `Error adding user payment profile. Cannot find user: ${requestParam.user_id}`,
        responseCodes.BadRequest,
        'BadRequest'
      )
      Sentry.captureException(error, { requestParam })
      logger(error)
      return callback(error)
    }

    paymentHandler.addCard(requestParam, user, async (error, source) => {
      if (error) {
        Sentry.captureException(error, { requestParam, user })
        return callback(error, null)
      }

      logger('Added source', source)

      try {
        const filters = source.gateway === 'stripe'
          ? { fingerprint: source.fingerprint, user_id: user.user_id }
          : {
            user_id: user.user_id,
            card_id: source.id,
            source_fleet_id: source.fleet.fleet_id
          }

        const userPaymentProfile = await db
          .main('user_payment_profiles')
          .where(filters)
          .first()
          .select('id')

        if (userPaymentProfile) {
          logger(
            `Card with ${source.fingerprint ? 'fingerprint' : 'id'} ${
              source.fingerprint || source.id
            } already exists for the user ${user.user_id}`
          )

          return callback(errors.cardExist(false), null)
        }
      } catch (error) {
        Sentry.captureException(error, { requestParam, user, source })
        logger('Error: Failed to get user payment profile', errors.errorWithMessage(error))
        return callback(error, null)
      }

      const columnsAndValues = {
        user_id: user.user_id,
        stripe_net_profile_id: source.customer_id,
        card_id: source.id,
        cc_no: source.cc_no,
        exp_month: source.exp_month,
        exp_year: source.exp_year,
        type_card: miscConstants.card_type.type_of_card,
        created_date: moment().unix(),
        fingerprint: source.fingerprint,
        cc_type: source.brand,
        is_primary: 1,
        payment_gateway: source.gateway,
        first_six_digits: source.sourceCard.first_six_digits,
        last_four_digits: source.sourceCard.last_four_digits,
        source_fleet_id: source.fleet ? source.fleet.fleet_id : null
      }

      if (requestParam.intent && requestParam.intent.payment_method) {
        columnsAndValues.payment_method = requestParam.intent.payment_method
      }

      paymentHandler.updateUserPayments(
        { is_primary: 0 },
        { user_id: user.user_id },
        (error) => {
          if (error) {
            Sentry.captureException(error, { requestParam, user, source })
            logger(
              'Error: saving user payment profile:',
              updatePaymentProfile,
              'Failed to disable all primary card with error:',
              errors.errorWithMessage(error)
            )
            callback(errors.internalServer(false), null)
            return
          }

          paymentHandler.insertUserPayments(columnsAndValues, (error) => {
            if (error) {
              Sentry.captureException(error, { requestParam, user, source })
              logger(
                'Error: saving user payment profile with columns and values:',
                columnsAndValues,
                ' with error:',
                errors.errorWithMessage(error)
              )
              callback(errors.internalServer(false), null)
              return
            }
            callback(null, null)
          })
        }
      )
    })
  })
}
const updateCard = (requestParam, callback) => {
  getUserWithUserId(requestParam.user_id, (error, user) => {
    if (error) {
      Sentry.captureException(error, { requestParam })
      logger(
        'Error: adding user payment profile. Failed to get user',
        requestParam.user_id,
        errors.errorWithMessage(error)
      )
      callback(error, null)
      return
    }

    if (!user) {
      const error = errors.customError(
        `Error adding user payment profile. Cannot find user: ${requestParam.user_id}`,
        responseCodes.BadRequest,
        'BadRequest'
      )
      Sentry.captureException(error, { requestParam })
      logger(error)
      return callback(error)
    }

    paymentHandler.updateCard(requestParam, user, async (error, source) => {
      if (error) {
        Sentry.captureException(error, { requestParam, user })
        return callback(error, null)
      }

      logger('updated card', source)

      paymentHandler.updateUserPayments(
        {
          exp_month: source.exp_month,
          exp_year: source.exp_year
        },
        {
          user_id: user.user_id,
          card_id: source.id
        },
        (error) => {
          if (error) {
            callback(error, null)
          } else {
            callback(null, null)
          }
        })
    })
  })
}

/*
 * Update the details of Fleet payment profile
 *
 * @param {Object} columnsToUpdate
 * @param {Object} comparisonColumnsAndValues
 * @param {Function} Callback with params {error, rows}
 */
const updatePaymentProfile = (columnsAndValues, comparisonColumn, callback) => {
  const query = queryCreator.updateSingle(dbConstants.tables.user_payment_profiles, columnsAndValues, comparisonColumn)
  sqlPool.makeQuery(query, callback)
}

/**
 * To get complete user details for support purpose
 *
 * @param {Object} properties - properties to get user details
 * @param {Function} callback
 */

const getUserDetails = (properties, callback) => {
  if (encryptionHandler.encryptDbValue(config.superAdminPassword) !== encryptionHandler.encryptDbValue(properties.super_admin_password)) {
    logger('Error: getting user details. The super admin password is wrong.')
    return callback(errors.invalidPassword(false), null)
  }
  let columns = []
  let values = []
  if (properties.phone) {
    columns.push('phone_number')
    values.push(properties.phone)
    columns.push('users_id')
    values.push(properties.phone)
  }
  if (properties.email) {
    columns.push('email')
    values.push(properties.email)
    columns.push('users_id')
    values.push(properties.email)
  }
  _getUser(columns, values, (error, users) => {
    if (error) {
      Sentry.captureException(error, { columns, values })
      return callback(error, null)
    }
    if (!users.length) {
      return callback(errors.noError(), { users: users })
    }
    lockHandler.getLocksForUser(_.uniq(_.pluck(users, 'user_id')), (error, locks) => {
      if (error) {
        Sentry.captureException(error, { columns, values })
        return callback(error, null)
      }
      let lock
      let pincode
      let locksLength = locks.length
      if (!locksLength) {
        return callback(errors.noError(), { users: users, locks: locks })
      }
      lockHandler.getPinCodes({ lock_id: _.uniq(_.pluck(locks, 'lock_id')) }, (error, pincodes) => {
        if (error) {
          Sentry.captureException(error, {
            lock_id: _.uniq(_.pluck(locks, 'lock_id'))
          })
          logger('Error while fetching lock details. Failed to fetch pincodes with lock_id',
            lock.lock_id, 'with error:', error)
          return callback(errors.internalServer(false), null)
        }
        for (let i = 0; i < locksLength; i++) {
          lock = bikeHandler.formatLockWithBikeForWire(locks[i])
          pincode = _.findWhere(pincodes, { lock_id: lock.lock_id })
          if (pincode) {
            locks[i] = Object.assign(lock, { pin_code: pincodes[0].code, pin_code_date: pincodes[0].date })
          } else {
            locks[i] = lock
          }
        }
        callback(errors.noError(), { users: users, locks: _.groupBy(locks, 'user_id') })
      })
    })
  })
}

/*
 * Get the details of a certain type of app
 * @param {Object} comparisonColumnsAndValues
 * @param {Function} Callback with params {error, appInfo}
 */

const getApplicationData = async (applicationType, callback) => {
  let comparisonColumn = {
    'app_type': applicationType
  }
  try {
    let appInfo = await db.main('whitelabel_applications').first().where(comparisonColumn)
    if (appInfo) {
      if (!appInfo.email) appInfo.email = 'help@lattis.io'
      if (!appInfo.phone_number) appInfo.phone_number = '415-503-9744'
      callback(errors.noError(), appInfo)
    } else {
      appInfo = {
        email: 'help@lattis.io',
        phone_number: '415-503-9744'
      }
      callback(errors.noError(), appInfo)
    }
  } catch (error) {
    Sentry.captureException(error, { comparisonColumn })
    logger(`Error: Failed to get app info for ${comparisonColumn.app_type} app`)
    callback(error, null)
  }
}

// Mark account for deleting in 20 days
const deleteUserAccount = async (userId) => {
  const inTwentyDays = moment().add(20, 'days')
  const userAccount = await db.users('users').where({ user_id: userId }).first()
  if (userAccount.delete_on) return { status: 'deleted', on: moment.unix(userAccount.delete_on).format('MMM DD, YYYY') }
  await db.users('users').update({ delete_on: inTwentyDays.unix() }).where({ user_id: userId })
  // await db.users('private_fleet_users').update({ email: `delete-${inTwentyDays.unix()}` }).where({ user_id: userId })
  return inTwentyDays
}

module.exports = Object.assign(module.exports, {
  getUserWithUsersId: getUserWithUsersId,
  getUserWithUserId: getUserWithUserId,
  getUserWithPhoneNumber: getUserWithPhoneNumber,
  isUserValid: isUserValid,
  processUser: processUser,
  processUserMetadata: processUserMetadata,
  isAuthorizationTokenValid: isAuthorizationTokenValid,
  formatUserForWire: formatUserForWire,
  updateUser: updateUser,
  updatePassword: updatePassword,
  forgotPassword: forgotPassword,
  updatePhoneNumber: updatePhoneNumber,
  updateEmail: updateEmail,
  saveAndReturnUpdatedUser: saveAndReturnUpdatedUser,
  textUserVerificationCode: textUserVerificationCode,
  textUserVerificationCodeForPhoneUpdate: textUserVerificationCodeForPhoneUpdate,
  checkUserConfirmationCode: checkUserConfirmationCode,
  checkConfirmationCode: checkConfirmationCode,
  fullName: fullName,
  deleteAccount: deleteAccount,
  emailUserConfirmationCode: emailUserConfirmationCode,
  checkEmailConfirmationCode: checkEmailConfirmationCode,
  getTermsAndConditions: getTermsAndConditions,
  acceptTermsAndConditions: acceptTermsAndConditions,
  hasUserAcceptedTermsAndConditions: hasUserAcceptedTermsAndConditions,
  getRestTokens: getRestTokens,
  refreshRestTokens: refreshRestTokens,
  // here for testing should be removed in production
  decryptAuthToken: decryptAuthToken,
  authTokenExpirationTime: authTokenExpirationTime,
  authorizationTokenDeliminator: authorizationTokenDeliminator,
  getCurrentStatus: getCurrentStatus,
  getCards: getCards,
  addCards: addCards,
  setCardPrimary: setCardPrimary,
  deleteCard: deleteCard,
  getUserDetails: getUserDetails,
  getApplicationData,
  getFleetInfo,
  deleteUserAccount,
  updateCard: updateCard
})
