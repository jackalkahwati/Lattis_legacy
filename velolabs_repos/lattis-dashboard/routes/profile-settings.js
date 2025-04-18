'use strict'

const platform = require('@velo-labs/platform')
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const errors = platform.errors
const logger = platform.logger
let express = require('express')
let router = express.Router()
let _ = require('underscore')
let profileSettingsHandler = require('./../model_handlers/profile-settings-handler')
const bikeFleetHandler = require('./../model_handlers/bike-fleet-handler')

/**
 * Used to get profiles of operators and customers for customer profile page
 *
 * The request body should include the operatorId or customerId.
 */

router.post('/get-profiles', function (req, res) {
  if (!_.has(req.body, 'customer_id') && !_.has(req.body, 'operator_id')) {
    logger('Parameters did not sent to server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  profileSettingsHandler.getProfiles(req.body, function (error, profile) {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), profile)
  })
})

/**
 * Used to update profiles of the operator and customer in profile page
 *
 * The request body should include email,firstname,lastname,phone_number
 **/
router.post('/update-profile', function (req, res) {
  if (!profileSettingsHandler.validateProfile(req.body)) {
    logger('Some Parameters not passed to server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  profileSettingsHandler.updateProfile(req.body, function (error, status) {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to get tapkey credentials
 *
 **/
router.get('/tap-key-credentials', async function (req, res) {
  try {
    const {Parameters: credentials} = await bikeFleetHandler.getAllParameters(req.query.path, 'tapkey')
    jsonResponse(res, responseCodes.OK, errors.noError(), credentials)
  } catch (error) {
    logger(error)
    jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
  }
})

/**
 * Used to login user to scout iot integration
 *
 * The request body should include the operator's email(username) and password
 */

router.post('/login-scout-user', async (req, res) => {
  try {
    if (!_.has(req.body, 'email') && !_.has(req.body, 'password')) {
      logger('Parameters not sent to server')
      jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
      return
    }
    const response = await profileSettingsHandler.loginUserForScout(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), response)
  } catch (error) {
    logger(`Error adding scout integration: ${error.message}`)
    jsonResponse(res, responseCodes.InternalServer, error.message || errors.internalServer(true), null)
  }
})

router.post('/authorize-geotab-user', async (req, res) => {
  try {
    if (!_.has(req.body, 'username') && !_.has(req.body, 'database') && !_.has(req.body, 'password') && !_.has(req.body, 'servername')) {
      logger('Parameters not sent to server')
      jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
      return
    }
    const response = await profileSettingsHandler.authorizeForGeotab(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), response)
  } catch (error) {
    logger(`Error adding  geotab integration: ${error.message}`)
    jsonResponse(res, responseCodes.InternalServer, error.message || errors.internalServer(true), null)
  }
})

router.post('/register-linka-merchant', async (req, res) => {
  try {
    if (!_.has(req.body, 'apiKey') && !_.has(req.body, 'secretKey')) {
      logger('Parameters not sent to server')
      jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
      return
    }
    const response = await profileSettingsHandler.registerForLinka(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), response)
  } catch (error) {
    logger(`Error adding  linka integration: ${error.message}`)
    jsonResponse(res, responseCodes.InternalServer, error.message || errors.internalServer(true), null)
  }
})

router.post('/register-integration', async (req, res) => {
  try {
    const response = await profileSettingsHandler.registerIntegration(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), response)
  } catch (error) {
    logger(`Error registering integration: ${error.message}`)
    jsonResponse(res, responseCodes.InternalServer, error.message || errors.internalServer(true), null)
  }
})

module.exports = router
