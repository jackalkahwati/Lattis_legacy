'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const query = platform.queryCreator
const sqlPool = platform.sqlPool
let _ = require('underscore')
let async = require('async')
let Api = require('mg-api-js')
const encryptionHandler = platform.encryptionHandler
const dbConstants = platform.dbConstants
const db = require('../db')
const { logInScoutUser } = require('../utils/scout-api')
const {getParameterStoreValuesByPath} = require('../utils/parameterStore')
const {TapKeyApiClient} = require('../utils/tapkey-api')
const { createOrUpdateGeotabIntegration } = require('../utils/gpsService')
const errors = platform.errors
const Sentry = require('../utils/configureSentry')

const environment = process.env.CURRENT_ENVIRONMENT

/**
 * This methods validates a profile object.
 *
 * @param {Object} requestData
 * @returns {boolean}
 */
let validateProfile = function (requestData) {
  if (_.has(requestData, 'customer_id')) {
    return (
      _.has(requestData, 'customer_id') &&
            _.has(requestData, 'email') &&
            _.has(requestData, 'first_name') &&
            _.has(requestData, 'last_name') &&
            _.has(requestData, 'phone_number')
    )
  } else if (_.has(requestData, 'operator_id')) {
    return (
      _.has(requestData, 'operator_id') &&
            _.has(requestData, 'email') &&
            _.has(requestData, 'first_name') &&
            _.has(requestData, 'last_name') &&
            _.has(requestData, 'phone_number')
    )
  }

  return false
}

/**
 * Used to get Profile Details with Params(requestData, etc)
 * @param {Object} requestData - operatorId or CustomerId to filter
 * @param {Function} done - returns {error, profileInfo}
 * @private
 */
let getProfiles = function (requestData, done) {
  let asyncTasks
  if (_.has(requestData, 'operator_id')) {
    asyncTasks = {
      operator: function (next) {
        getOperator(requestData.operator_id, next)
      }
    }
  } else if (_.has(requestData, 'customer_id')) {
    asyncTasks = {
      customers: function (next) {
        getCustomers(requestData.customer_id, next)
      }
    }
  }
  async.parallel(asyncTasks, function (err, results) {
    if (err) {
      done(err, null)
    } else {
      done(null, results)
    }
  })
}

/**
 *   To get details for operator
 *
 * @param {Number} operatorId - The customer identifier
 * @param {Function} done - Callback function with error, data params
 */
let getOperator = function (operatorId, done) {
  let getOperatorQuery = query.selectWithAnd('operators', [], {operator_id: operatorId})
  sqlPool.makeQuery(getOperatorQuery, function (error, operators) {
    if (error) {
      Sentry.captureException(error, {operatorId})
      logger(error)
      done(error, null)
      return
    }
    done(null, operators)
  })
}

/**
 *   To get Customers for a customer
 *
 * @param {Number} customerId - The customer identifier
 * @param {Function} done - Callback function with error, data params
 */
let getCustomers = function (customerId, done) {
  const getCustomersQuery = query.selectWithAnd('customers', [], {customer_id: customerId})
  sqlPool.makeQuery(getCustomersQuery, function (error, customers) {
    if (error) {
      Sentry.captureException(error, {customerId})
      logger(error)
      done(error, null)
      return
    }

    done(null, customers)
  })
}

/**
 * This method updates profile for an operator or customer.
 *
 * @param {Object} requestData - The operator identifier
 * @param {Function} done - Callback function with error, data params
 */
let updateProfile = function (requestData, done) {
  let table
  let columnsAndValues
  let targetColumnsAndValues

  if (_.has(requestData, 'customer_id')) {
    table = dbConstants.tables.customers
    columnsAndValues = {
      first_name: requestData.first_name,
      last_name: requestData.last_name,
      phone_number: requestData.phone_number,
      email: requestData.email
    }
    targetColumnsAndValues = {customer_id: requestData.customer_id}
  } else if (_.has(requestData, 'operator_id')) {
    table = dbConstants.tables.operators
    columnsAndValues = {
      first_name: requestData.first_name,
      last_name: requestData.last_name,
      phone_number: requestData.phone_number,
      email: requestData.email
    }
    targetColumnsAndValues = {operator_id: requestData.operator_id}
  }
  let updateProfileQuery = query.updateSingle(table, columnsAndValues, targetColumnsAndValues)
  sqlPool.makeQuery(updateProfileQuery, function (error) {
    if (error) {
      Sentry.captureException(error, {requestData})
      logger(error)
      done(error, null)
      return
    }

    done(null, 'updated_successfully')
  })
}

/**
 * Register a user for the scout IoT
 *
 * @param {object } userData
 */
const loginUserForScout = async (userData) => {
  try {
    const login = await logInScoutUser(userData)
    if (login) {
      try {
        const integrationId = await db.main('integrations')
          .insert({
            fleet_id: userData.fleet_id,
            email: userData.email,
            api_key: login.key,
            integration_type: 'scout'
          })
        return await db.main('integrations')
          .where({ integrations_id: integrationId })
          .first()
      } catch (error) {
        Sentry.captureException(error, {userData})
        logger(`Warn: Unable to insert key for fleet ${userData.fleet_id}: ${error}`)
      }
    }
  } catch (error) {
    Sentry.captureException(error, {userData})
    throw errors.customError(
      error.message || `Unable to add scout integration for fleet ${userData.fleet_id}: ${error}`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

/**
 * Register a user for the Geotab integration
 *
 * @param {object } userData
 */
const authorizeForGeotab = async (userData) => {
  const authentication = {
    credentials: {
      userName: userData.username,
      password: userData.password,
      database: userData.database
    },
    path: userData.servername
  }
  const options = {
    fullResponse: true
  }
  const api = new Api(authentication, options)
  try {
    const {data: sessionData} = await api.getSession()
    if (sessionData.result) {
      try {
        const apiKey = encryptionHandler.encryptDbValue(userData.password)
        const integrationExists = await db.main('integrations').where({ fleet_id: userData.fleet_id, integration_type: 'geotab' }).first()
        if (integrationExists) {
          throw errors.customError(
            `The fleet ${userData.fleet_id} already has a geotab integration}`,
            platform.responseCodes.BadRequest,
            'BadRequest',
            false
          )
        }
        const integrationId = await db.main('integrations')
          .insert({
            fleet_id: userData.fleet_id,
            email: userData.username,
            api_key: apiKey,
            session_id: sessionData.result.credentials.sessionId,
            integration_type: 'geotab',
            metadata: JSON.stringify({
              database: sessionData.result.credentials.database,
              server_name: userData.servername
            })
          })
        // Save this info in GPS service
        const integrationInfo = {
          database: userData.database,
          server_name: userData.servername,
          userName: userData.username,
          apiKey,
          sessionId: sessionData.result.credentials.sessionId
        }
        await createOrUpdateGeotabIntegration(integrationInfo, userData.fleet_id, 'geotab')
        return await db.main('integrations')
          .where({ integrations_id: integrationId })
          .first()
      } catch (error) {
        Sentry.captureException(error, {userData})
        logger(`Warn: Unable to insert credentials for fleet ${userData.fleet_id}: ${error}`)
      }
    }
    if (sessionData.error) {
      throw errors.customError(
        `Unable to get sessionID for user ${userData.email}, ${sessionData.error}`,
        platform.responseCodes.BadRequest,
        'BadRequest',
        false
      )
    }
  } catch (error) {
    Sentry.captureException(error, {userData})
    throw errors.customError(
      `Unable to authorize geotab for fleet ${userData.fleet_id}: ${error}`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

/**
 * Register merchant for Linka integration
 *
 * @param {object } userData
 */
const registerForLinka = async (userData) => {
  try {
    const integrationId = await db.main('integrations')
      .insert({
        fleet_id: userData.fleetId,
        integration_type: 'linka',
        metadata: JSON.stringify({
          apikey: userData.apiKey,
          secretKey: userData.secretKey
        })
      })
    return await db.main('integrations')
      .where({ integrations_id: integrationId })
      .first()
  } catch (error) {
    Sentry.captureException(error, {userData})
    logger(`Warn: Unable to insert credentials for fleet ${userData.fleet_id}: ${error}`)
  }
}

const registerIntegration = async (userData) => {
  try {
    let integrationId
    const integrationType = userData.integrationType
    switch (integrationType) {
      case 'duckt': {
        integrationId = await db.main('integrations')
          .insert({
            fleet_id: userData.fleetId,
            email: userData.email,
            api_key: encryptionHandler.encryptDbValue(userData.password),
            integration_type: userData.integrationType || 'duckt'
          })
        break
      }
      case 'tapkey':
        try {
          const { Parameters: tapkeyUrls } = await getParameterStoreValuesByPath('TAP_KEY/URLS')
          const { Parameters: tapkeyKeys } = await getParameterStoreValuesByPath('TAP_KEY/KEYS')
          const urlName = `/env/${environment}/TAP_KEY/URLS`
          const keyName = `/env/${environment}/TAP_KEY/KEYS`
          const passPhrase = tapkeyKeys.find(key => key.Name === `${keyName}/TAP_KEY_PASSPHRASE`)
          const tokenIssuer = tapkeyKeys.find(key => key.Name === `${keyName}/TAP_KEY_TOKEN_ISSUER`)
          const idpClientId = tapkeyKeys.find(url => url.Name === `${keyName}/IDP_CLIENT_ID`)
          const provider = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_ID_PROVIDER`)
          const ownerAccountId = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_OWNER_ACCOUNT_ID`)
          const authCodeClientId = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_AUTH_CODE_CLIENT_ID`)
          const authCodeClientSecret = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_AUTH_CODE_CLIENT_SECRET`)
          const tapKeyPrivateKey = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_PRIVATE_KEY`)

          const grantType = tapkeyUrls.find(url => url.Name === `${urlName}/TAP_KEY_GRANT_TYPE`)
          const loginUrl = tapkeyUrls.find(url => url.Name === `${urlName}/LOGIN_URL`)
          const baseUrl = tapkeyUrls.find(url => url.Name === `${urlName}/BASE_URL`)
          const redirectUri = tapkeyUrls.find(url => url.Name === `${urlName}/REDIRECT_URI`)

          const tapKeyApiClient = new TapKeyApiClient({
            idpClientId: idpClientId.Value,
            grantType: grantType.Value,
            provider: provider.Value,
            loginUrl: loginUrl.Value,
            passPhrase: passPhrase.Value,
            tokenIssuer: tokenIssuer.Value,
            refreshToken: 'refreshToken',
            accessToken: 'accessToken',
            baseUrl: baseUrl.Value,
            ownerAccountId: ownerAccountId.Value,
            authCodeClientId: authCodeClientId.Value,
            authCodeClientSecret: authCodeClientSecret.Value,
            tapKeyPrivateKey: tapKeyPrivateKey.Value,
            redirectUri: redirectUri.Value
          })
          const tapkeyResponse = await tapKeyApiClient.getRefreshToken(userData.code)
          integrationId = await db.main('integrations')
            .insert({
              fleet_id: userData.fleetId,
              integration_type: userData.integrationType || 'tapkey',
              metadata: JSON.stringify({
                accessToken: tapkeyResponse.access_token,
                refreshToken: tapkeyResponse.refresh_token
              })
            })
        } catch (error) {
          Sentry.captureException(error, {userData})
          logger(
            `Error: failed to get tapkey accessToken: ${error.message}`
          )
          throw errors.customError(
            `Failed to fetch accessToken`,
            platform.responseCodes.BadRequest,
            'BadRequest',
            false
          )
        }
        break
      case 'sas':
        try {
          integrationId = await db.main('integrations')
            .insert({
              fleet_id: userData.fleetId,
              integration_type: userData.integrationType || 'sas',
              client_id: userData.clientId,
              client_secret: userData.clientKey,
              metadata: JSON.stringify({
                domainName: userData.domainName
              })
            })
        } catch (error) {
          Sentry.captureException(error, {userData})
          logger(
            `Error: failed to register sas integration : ${error.message}`
          )
          throw errors.customError(
            `Failed to register sas integration`,
            platform.responseCodes.BadRequest,
            'BadRequest',
            false
          )
        }
        break
      case 'ParcelHive':
        try {
          integrationId = await db.main('integrations')
            .insert({
              fleet_id: userData.fleetId,
              integration_type: userData.integrationType || 'ParcelHive',
              metadata: JSON.stringify({
                accessToken: userData.accessToken.trim()
              })
            })
        } catch (error) {
          Sentry.captureException(error, {userData})
          logger(
            `Error: failed to register ParcelHive integration : ${error.message}`
          )
          throw errors.customError(
            `Failed to register ParcelHive integration`,
            platform.responseCodes.BadRequest,
            'BadRequest',
            false
          )
        }
        break
      default: {
        break
      }
    }
    return await db.main('integrations')
      .where({ integrations_id: integrationId })
      .first()
  } catch (error) {
    Sentry.captureException(error, {userData})
    logger(`Warn: Unable to insert credentials for fleet ${userData.fleet_id}: ${error}`)
  }
}

module.exports = {
  validateProfile: validateProfile,
  getProfiles: getProfiles,
  getCustomers: getCustomers,
  updateProfile: updateProfile,
  loginUserForScout: loginUserForScout,
  authorizeForGeotab: authorizeForGeotab,
  getOperator: getOperator,
  registerForLinka,
  registerIntegration
}
