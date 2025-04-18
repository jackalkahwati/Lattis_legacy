'use strict'

const {
  sqlPool,
  queryCreator,
  dbConstants,
  config: platformConfig,
  errors,
  logger,
  responseCodes
} = require('@velo-labs/platform')

const config = require('./../config')
const stripe = require('stripe')(platformConfig.stripe.apiKey)
const _ = require('underscore')
const payments = require('./payments')
const { GATEWAYS } = require('./payments/gateways')
const integrationsHandler = require('./integrations-handler')
const db = require('../db')
const { configureSentry } = require('../utils/configureSentry')

const Sentry = configureSentry()

/*
 * inserts the details of saved cards for users
 * @param {Object} columnsAndValues
 * @param {Function} Callback with params {error, rows}
 */
const insertUserPayments = (columnsAndValues, callback) => {
  const insertUserPaymentQuery = queryCreator.insertSingle(
    dbConstants.tables.user_payment_profiles,
    columnsAndValues
  )
  sqlPool.makeQuery(insertUserPaymentQuery, callback)
}

/*
 * Retrieves the details of saved cards for users
 * @param {Object} columnsToSelect
 * @param {Object} comparisonColumnsAndValues
 * @param {Function} Callback with params {error, rows}
 */
const updateUserPayments = (columnsAndValues, targetColumns, callback) => {
  const updateUserPaymentQuery = queryCreator.updateSingle(
    dbConstants.tables.user_payment_profiles,
    columnsAndValues,
    targetColumns
  )
  sqlPool.makeQuery(updateUserPaymentQuery, callback)
}

const getUserPaymentProfiles = (requestParam, callback) => {
  const comparisonColumnAndValue = {}
  if (requestParam.user_id) {
    comparisonColumnAndValue.user_id = requestParam.user_id
  }
  if (requestParam.is_primary) {
    comparisonColumnAndValue.is_primary = requestParam.is_primary
  }
  const paymentProfileQuery = queryCreator.selectWithAnd(
    dbConstants.tables.user_payment_profiles,
    null,
    comparisonColumnAndValue
  )
  sqlPool.makeQuery(paymentProfileQuery, (error, payments) => {
    if (error) {
      Sentry.captureException(error, { paymentProfileQuery })
      logger(
        'Error: can not get user payment profile with ',
        _.keys(comparisonColumnAndValue),
        ':',
        paymentProfileQuery
      )
      callback(errors.internalServer(false), null)
      return
    }
    callback(null, payments)
  })
}

/*
 * Deleting the card
 *
 * @param {Object} cardDetails - Details of the card
 * @returns {Function} callback - returns with params {error, response}
 */
const deleteCard = (cardDetails, callback) => {
  payments
    .deleteCard(cardDetails)
    .then((res) => callback(null, res))
    .catch(callback)
}

const setupIntent = async (
  { user, payment_gateway: gateway, fleet_id: fleetId },
  appDetails
) => {
  let fleet

  if (gateway === GATEWAYS.mercadopago) {
    if (!fleetId) {
      if (!(appDetails && appDetails.whiteLabel)) {
        throw errors.customError(
          'A fleet id is required to set up Mercado Pago.',
          responseCodes.BadRequest,
          'BadRequest'
        )
      }

      fleet = await db
        .main('fleet_metadata')
        .where({ whitelabel_id: appDetails.whiteLabel.app_id })
        .innerJoin('fleets', 'fleets.fleet_id', 'fleet_metadata.fleet_id')
        .select('fleets.*')
        .first()
    }

    const integration = await integrationsHandler.retrieveIntegration({
      fleet_id: fleet ? fleet.fleet_id : fleetId,
      integration_type: gateway
    })

    if (!integration) {
      throw errors.customError(
        `Fleet is not connected to ${gateway}`,
        responseCodes.BadRequest,
        'BadRequest'
      )
    }

    return {
      publicKey: integration.client_id
    }
  }

  const userPaymentProfile = await db
    .main('user_payment_profiles')
    .where({ user_id: user.user_id, payment_gateway: 'stripe' })
    .select('stripe_net_profile_id')
    .first()

  return stripe.setupIntents.create({
    customer: userPaymentProfile
      ? userPaymentProfile.stripe_net_profile_id
      : undefined
  })
}

/*
 * add cards
 *
 * @param {Object} requestParam - cardDetails of the card
 * @param {Object} user - user to be charged from
 * @returns {Function} callback - returns with params {error, response}
 */
const addCard = (requestParam, user, callback) => {
  payments
    .addCard({ user, params: requestParam })
    .then((card) => {
      callback(null, card)
    })
    .catch((error) => {
      Sentry.captureException(error, { requestParam })
      logger(
        `Error: Sorry cannot add card for user: ${user.email} with error`,
        errors.errorWithMessage(error)
      )

      callback(error)
    })
}

const updateCard = (requestParam, user, callback) => {
  payments
    .updateCard({ user, params: requestParam })
    .then((card) => {
      callback(null, card)
    })
    .catch((error) => {
      Sentry.captureException(error, { requestParam })
      logger(
        `Error: Sorry cannot update card for user: ${user.email} with error`,
        errors.errorWithMessage(error)
      )
      callback(error)
    })
}

const getFleetCurrency = (fleetPaymentSettings) => {
  if (fleetPaymentSettings) {
    return fleetPaymentSettings.currency
  }
  return config.fleets.defaultCurrency
}

const chargeForTrip = (
  { fleetPaymentSettings, amount, paymentProfile },
  context = {}
) => {
  const currency = getFleetCurrency(fleetPaymentSettings)

  return payments.charge(
    { amount, currency, card: paymentProfile, fleetPaymentSettings },
    context
  )
}

const getPrimaryCard = (requestParam, callback) => {
  const comparisonColumnAndValue = {}
  if (_.has(requestParam, 'user_id')) {
    comparisonColumnAndValue.user_id = requestParam.user_id
    comparisonColumnAndValue.is_primary = 1
  }
  const paymentsQuery = queryCreator.selectWithAnd(
    dbConstants.tables.user_payment_profiles,
    null,
    comparisonColumnAndValue
  )
  sqlPool.makeQuery(paymentsQuery, (error, paymentProfiles) => {
    if (error) {
      Sentry.captureException(error, { paymentsQuery })
      logger(
        'Error: making trips with :',
        comparisonColumnAndValue,
        'with query:',
        paymentsQuery
      )
      return callback(errors.internalServer(false), null)
    }
    return callback(null, paymentProfiles)
  })
}

const authorizePayment = async ({ fleetPaymentSettings, amount, userId }) => {
  const paymentProfile = await db
    .main('user_payment_profiles')
    .where({
      user_id: userId,
      is_primary: 1,
      payment_gateway: GATEWAYS.stripe
    })
    .first()

  if (!paymentProfile) {
    throw errors.customError(
      'This user does not have a stripe primary payment source.',
      responseCodes.Conflict,
      'Conflict'
    )
  }

  return chargeForTrip(
    { amount, fleetPaymentSettings, paymentProfile },
    { authorizeOnly: true }
  )
}

module.exports = {
  insertUserPayments: insertUserPayments,
  updateUserPayments: updateUserPayments,
  getUserPaymentProfiles: getUserPaymentProfiles,
  deleteCard: deleteCard,
  addCard: addCard,
  chargeForTrip: chargeForTrip,
  getFleetCurrency: getFleetCurrency,
  setupIntent: setupIntent,
  getPrimaryCard,
  authorizePayment,
  updateCard
}
