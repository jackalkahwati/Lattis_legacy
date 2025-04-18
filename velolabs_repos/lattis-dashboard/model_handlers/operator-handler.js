'use strict'

const _ = require('underscore')
const crypto = require('crypto')
const moment = require('moment')
const { v5: uuidv5 } = require('uuid')
const bcrypt = require('bcrypt')

const { logger } = require('../utils/error-handler')
const { errors } = require('../utils/response-utils')
const db = require('../db')
const config = require('../config')
const mailContent = require('../utils/mail-content')
const operatorConstants = require('../constants/operator-constants')
const fleetHandler = require('./fleet-handler')

const SALT_ROUNDS = 12

/**
 * Hash password using bcrypt
 * @param {string} password
 * @returns {Promise<string>}
 */
const hashPassword = async (password) => {
  return bcrypt.hash(password, SALT_ROUNDS)
}

/**
 * Compare password with hash
 * @param {string} password
 * @param {string} hash
 * @returns {Promise<boolean>}
 */
const comparePassword = async (password, hash) => {
  return bcrypt.compare(password, hash)
}

/**
 * Update operator details
 * @param {Object} columnsToUpdate - Values to update
 * @param {Object} where - Where conditions
 * @returns {Promise}
 */
const updateOperator = async (columnsToUpdate, where) => {
  try {
    await db.users('operators').where(where).update(columnsToUpdate)
    return null
  } catch (error) {
    logger.error('Error updating operator:', error)
    throw errors.internalServer(true)
  }
}

/**
 * Get operator details
 * @param {Object} where - Where conditions
 * @returns {Promise<Object>}
 */
const getOperator = async (where) => {
  try {
    return await db.users('operators').where(where).first()
  } catch (error) {
    logger.error('Error getting operator:', error)
    throw errors.internalServer(true)
  }
}

/**
 * Check operator existence and get fleet associations
 * @param {Object} params - Search parameters
 * @returns {Promise<Object>}
 */
const checkOperator = async (params) => {
  try {
    const where = {}
    if (params.email) where.email = params.email
    if (params.operator_id) where.operator_id = params.operator_id

    const operator = await getOperator(where)
    const fleetAdminAssociations = await fleetHandler.getFleetsWithFleetAssociations(
      'fleet_id',
      'operator_id',
      [params.operator_id],
      true
    )

    if (operator) {
      const fleetAssociations = await fleetHandler.getFleetAssociations({
        operator_id: operator.operator_id
      })

      const fleetIds = _.pluck(fleetAssociations, 'fleet_id')
      const filteredFleets = fleetAdminAssociations.filter(admin => {
        return !admin.fleet_id || !fleetIds.includes(admin.fleet_id)
      })

      return {
        operator: formatOperatorForWire(operator),
        fleets: filteredFleets
      }
    }

    return {
      operator: null,
      fleets: fleetAdminAssociations
    }
  } catch (error) {
    logger.error('Error checking operator:', error)
    throw errors.internalServer(true)
  }
}

/**
 * Create new operator
 * @param {Object} data - Operator data
 * @param {string} url - Reset password URL
 * @returns {Promise}
 */
const createOperator = async (data, url) => {
  try {
    if (data.operator_id) {
      const columns = ['operator_id', 'customer_id', 'acl', 'fleet_id']
      const values = data.fleets.map(fleet => [
        data.operator_id,
        data.customer_id,
        data.access_staff,
        fleet
      ])

      await fleetHandler.insertAssociationForMultipleFleets(columns, values)
      return null
    }

    const existingOperator = await getOperator({ email: data.email })
    if (existingOperator) {
      throw errors.conflict('Email already exists')
    }

    const token = crypto.randomBytes(20).toString('hex')
    const password = crypto.randomBytes(10).toString('hex')
    const hashedPassword = await hashPassword(password)

    const operatorData = {
      first_name: data.first_name,
      last_name: data.last_name,
      phone_number: data.phone_number,
      email: data.email,
      customer_id: data.customer_id,
      username: data.email,
      password: hashedPassword,
      date_created: moment().unix(),
      user_state: operatorConstants.user_state.active,
      reset_password_token: token,
      reset_password_expires: moment().unix() + config.token_expiration_time
    }

    const [operatorId] = await db.users('operators').insert(operatorData)

    const tokenExpirationTime = Math.floor(config.token_expiration_time / 3600)
    const timeUnit = tokenExpirationTime > 1 ? ' hours' : ' hour'
    const mailObject = mailContent.createOperator(token, tokenExpirationTime + timeUnit, url)

    // Send email
    // Note: Implement your own email sending logic here
    // await sendEmail(data.email, mailObject.subject, mailObject.content)

    if (data.fleets) {
      const columns = ['acl', 'fleet_id', 'operator_id', 'customer_id']
      const values = data.fleets.map(fleet => [
        data.access_staff,
        fleet,
        operatorId,
        data.customer_id
      ])

      await fleetHandler.insertAssociationForMultipleFleets(columns, values)
    }

    return null
  } catch (error) {
    logger.error('Error creating operator:', error)
    throw error
  }
}

/**
 * Delete operator
 * @param {Object} params - Delete parameters
 * @returns {Promise}
 */
const deleteOperator = async (params) => {
  try {
    await db.users('fleet_associations')
      .where({
        operator_id: params.operator_id,
        fleet_id: params.fleet_id
      })
      .delete()
  } catch (error) {
    logger.error('Error deleting operator:', error)
    throw errors.internalServer(true)
  }
}

/**
 * Validate operator data
 * @param {Object} data - Operator data
 * @returns {boolean}
 */
const validateOperator = (data) => {
  const isValid = (
    ((data.first_name && data.last_name && data.email) || data.operator_id) &&
    data.access_staff &&
    data.customer_id
  )

  if (data.fleets) {
    return isValid && data.fleets.length > 0
  }

  return isValid
}

/**
 * Handle forgot password request
 * @param {string} email - User email
 * @param {string} url - Reset URL
 * @returns {Promise}
 */
const forgotPassword = async (email, url) => {
  try {
    const operator = await getOperator({ email })
    if (!operator) {
      throw errors.notFound('Operator')
    }

    const token = crypto.randomBytes(20).toString('hex')
    const expires = moment().unix() + config.token_expiration_time

    await updateOperator(
      {
        reset_password_token: token,
        reset_password_expires: expires
      },
      { email }
    )

    const tokenExpirationTime = Math.floor(config.token_expiration_time / 3600)
    const timeUnit = tokenExpirationTime > 1 ? ' hours' : ' hour'
    const mailObject = mailContent.forgotPassword(token, tokenExpirationTime + timeUnit, url)

    // Send email
    // Note: Implement your own email sending logic here
    // await sendEmail(email, mailObject.subject, mailObject.content)

  } catch (error) {
    logger.error('Error in forgot password:', error)
    throw error
  }
}

/**
 * Validate reset password token
 * @param {Object} data - Token data
 * @returns {Promise<Object>}
 */
const validateToken = async (data) => {
  try {
    const operator = await getOperator({ reset_password_token: data.token })
    if (!operator) {
      throw errors.notFound('Invalid token')
    }

    if (operator.reset_password_expires < moment().unix()) {
      throw errors.validationError('Token expired')
    }

    return operator
  } catch (error) {
    logger.error('Error validating token:', error)
    throw error
  }
}

/**
 * Change password with token
 * @param {Object} data - Password data
 * @returns {Promise<Object>}
 */
const changePassword = async (data) => {
  try {
    const operator = await validateToken(data)
    const hashedPassword = await hashPassword(data.password)

    await updateOperator(
      { password: hashedPassword },
      { reset_password_token: data.token }
    )

    return { email: operator.email }
  } catch (error) {
    logger.error('Error changing password:', error)
    throw error
  }
}

/**
 * Change password internally
 * @param {Object} data - Password data
 * @returns {Promise<boolean>}
 */
const changePasswordInternally = async (data) => {
  try {
    const operator = await getOperator({ email: data.email })
    const isValid = await comparePassword(data.oldPassword, operator.password)

    if (!isValid) {
      throw errors.validationError('Invalid current password')
    }

    const hashedPassword = await hashPassword(data.password)
    await updateOperator(
      { password: hashedPassword },
      { email: data.email }
    )

    return true
  } catch (error) {
    logger.error('Error changing password internally:', error)
    throw error
  }
}

/**
 * Authenticate operator
 * @param {Object} credentials - Login credentials
 * @returns {Promise<Object>}
 */
const authenticate = async (credentials) => {
  try {
    const operator = await getOperator({ email: credentials.email })
    if (!operator) {
      throw errors.authenticationError('Invalid credentials')
    }

    const isValid = await comparePassword(credentials.password, operator.password)
    if (!isValid) {
      throw errors.authenticationError('Invalid credentials')
    }

    return operator
  } catch (error) {
    logger.error('Error authenticating:', error)
    throw error
  }
}

/**
 * Format operator data for response
 * @param {Object} operator - Operator data
 * @returns {Object}
 */
const formatOperatorForWire = (operator) => {
  return _.omit(operator,
    'password',
    'rest_token',
    'refresh_token',
    'reset_password_token',
    'reset_password_expires'
  )
}

module.exports = {
  authenticate,
  forgotPassword,
  validateToken,
  changePassword,
  createOperator,
  deleteOperator,
  validateOperator,
  getOperator,
  checkOperator,
  formatOperatorForWire,
  changePasswordInternally
}
