'use strict'

const express = require('express')
const _ = require('underscore')
const { v5: uuidv5 } = require('uuid')
const { logger } = require('../utils/error-handler')
const {
  responseCodes,
  errors,
  jsonResponse,
  statusCodeFromError
} = require('../utils/response-utils')

const operatorHandler = require('../model_handlers/operator-handler')
const fleetHandler = require('../model_handlers/fleet-handler')
const db = require('../db')
const { generateToken } = require('../utils/auth')

const NAMESPACE = '1b671a64-40d5-491e-99b0-da01ff1f3341'
const router = express.Router()

/**
 * Handle forgot password request
 */
router.post('/forgot-password', async (req, res) => {
  try {
    const { email } = req.body
    if (!email) {
      logger.warn('Missing email parameter in forgot password request')
      return jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    }

    const baseUrl = process.env.MAILER_BASE_URL || `${req.protocol}://${req.get('host')}`
    const resetUrl = `${baseUrl}/#/password-reset`

    await operatorHandler.forgotPassword(email, resetUrl)
    return jsonResponse(res, responseCodes.OK, errors.noError(), null)
  } catch (error) {
    logger.error('Error in forgot password:', error)
    return jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
  }
})

/**
 * Verify password strength and requirements
 */
router.post('/verify-password', async (req, res) => {
  try {
    const { password } = req.body
    if (!password) {
      logger.warn('Missing password parameter in verify password request')
      return jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    }

    const verified = await operatorHandler.verifyPassword(req.body)
    return jsonResponse(res, responseCodes.OK, errors.noError(), verified)
  } catch (error) {
    logger.error('Error in verify password:', error)
    return jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
  }
})

/**
 * Change password with token (forgot password flow)
 */
router.post('/change-password', async (req, res) => {
  try {
    const { password, token } = req.body
    if (!password || !token) {
      logger.warn('Missing required parameters in change password request')
      return jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    }

    const operatorEmail = await operatorHandler.changePassword(req.body)
    return jsonResponse(res, responseCodes.OK, errors.noError(), operatorEmail)
  } catch (error) {
    logger.error('Error in change password:', error)
    return jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
  }
})

/**
 * Change password internally (authenticated flow)
 */
router.post('/change-password-internally', async (req, res) => {
  try {
    const { password, email } = req.body
    if (!password || !email) {
      logger.warn('Missing required parameters in internal change password request')
      return jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    }

    const operatorEmail = await operatorHandler.changePasswordInternally(req.body)
    return jsonResponse(res, responseCodes.OK, errors.noError(), operatorEmail)
  } catch (error) {
    logger.error('Error in internal change password:', error)
    return jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
  }
})

/**
 * Validate reset password token
 */
router.post('/validate-token', async (req, res) => {
  try {
    const { token } = req.body
    if (!token) {
      logger.warn('Missing token parameter in validate token request')
      return jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    }

    await operatorHandler.validateToken(req.body)
    return jsonResponse(res, responseCodes.OK, errors.noError(), null)
  } catch (error) {
    logger.error('Error in validate token:', error)
    return jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
  }
})

/**
 * Operator login
 */
router.post('/login', async (req, res) => {
  try {
    const operator = await new Promise((resolve, reject) => {
      operatorHandler.authenticate(req.body, (err, operator) => {
        if (err) reject(err)
        else resolve(operator)
      })
    })

    if (!operator) {
      logger.warn('Invalid login attempt - operator not found')
      return jsonResponse(res, responseCodes.Unauthorized, errors.authenticationError('Invalid credentials'), null)
    }

    const operatorInfo = {
      operator_id: operator.operator_id,
      operator_name: operator.first_name,
      phone_number: operator.phone_number,
      acl: operator.acl
    }

    const fleetInfo = await new Promise((resolve, reject) => {
      fleetHandler.getFleetAssociations({ operator_id: operator.operator_id }, (error, info) => {
        if (error) reject(error)
        else resolve(info)
      })
    })

    const customerList = await new Promise((resolve, reject) => {
      fleetHandler.getCustomerList({ customer_id: operator.customer_id }, (error, list) => {
        if (error) reject(error)
        else resolve(list)
      })
    })

    fleetInfo.forEach(data => {
      data.on_call = data.on_call === 0
      data.customer_id = customerList[0].customer_id
      data.customer_name = customerList[0].customer_name
    })

    logger.info('Login successful:', { operator_id: operator.operator_id })

    const payload = {
      operator_info: operatorInfo,
      fleet_info: fleetInfo,
      token: generateToken({ sub: operator.operator_id }, { expiresIn: '30d' })
    }

    return jsonResponse(res, responseCodes.OK, errors.noError(), payload)
  } catch (error) {
    logger.error('Error in login:', error)
    return jsonResponse(res, statusCodeFromError(error), errors.formatErrorForWire(error), null)
  }
})

/**
 * Operator logout
 */
router.get('/logout', (req, res) => {
  req.logout()
  logger.info('User logged out successfully')
  return jsonResponse(res, responseCodes.OK, errors.noError(), {})
})

/**
 * Generate API key for third-party access
 */
router.post('/api-key', async (req, res) => {
  try {
    const { issue_to } = req.body
    if (!issue_to) {
      logger.warn('Missing issue_to parameter in API key request')
      return jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), {
        message: 'Please provide issue_to parameter in the body'
      })
    }

    const operator = await new Promise((resolve, reject) => {
      operatorHandler.authenticate(req.body, (err, operator) => {
        if (err) reject(err)
        else resolve(operator)
      })
    })

    if (!operator) {
      logger.warn('Invalid API key request - operator not found')
      return jsonResponse(res, responseCodes.Unauthorized, errors.authenticationError(), null)
    }

    const issuer = operator.email
    const isSuperUser = await db.users('super_users').where({ email: issuer }).first()
    
    if (!isSuperUser) {
      logger.warn(`Unauthorized API key request from non-super user: ${issuer}`)
      return jsonResponse(res, responseCodes.Forbidden, errors.unauthorizedAccess(true), {
        message: 'You cannot perform that action.'
      })
    }

    const existingKey = await db.users('api_keys')
      .where({ issued_to: issue_to, status: 'active' })
      .first()

    if (existingKey) {
      return jsonResponse(res, responseCodes.Conflict, errors.conflict(
        `${issue_to} already has an active key. Revoke it before creating a new one`
      ), null)
    }

    const key = uuidv5(issue_to, NAMESPACE)
    const [createdId] = await db.users('api_keys').insert({ issued_to: issue_to, issuer, key })
    const createdRecord = await db.users('api_keys').where({ api_id: createdId }).first()

    return jsonResponse(res, responseCodes.Created, errors.noError(), {
      message: `API key for ${issue_to} generated successfully`,
      key: createdRecord.key,
      issued_to: createdRecord.issued_to
    })

  } catch (error) {
    logger.error('Error generating API key:', error)
    return jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), {
      message: 'Error generating API key'
    })
  }
})

module.exports = router
