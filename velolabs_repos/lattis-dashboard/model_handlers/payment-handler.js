'use strict'

const platform = require('@velo-labs/platform')
const request = require('request')
const db = require('../db')

const fleetHandler = require('./fleet-handler')

const platformConfig = platform.config
const logger = platform.logger
const errors = platform.errors
const responseCodes = platform.responseCodes

const stripe = require('stripe')(platformConfig.stripe.apiKey)
const Sentry = require('../utils/configureSentry')

const saveStripeConnectInfo = (stripeInfo, done) => {
  const oAuthRequestBody = {
    client_secret: platformConfig.stripe.apiKey,
    code: stripeInfo.code,
    grant_type: 'authorization_code'
  }
  request({
    url: 'https://connect.stripe.com/oauth/token',
    method: 'POST',
    json: true, // <--Very important!!!
    body: oAuthRequestBody
  }, async (error, response, body) => {
    if (error || (!!body && !!body.error)) {
      Sentry.captureException(error || body.error)
      error = body.error ? body.error + ':' + body.description : error
      logger(`Error: From Stripe, while getting stripe acc id from oauth token for fleet: ${stripeInfo.fleet_id}
        code: ${stripeInfo.code} and error: ${error}`)

      // Disconnect stripe since an error connecting will disconnect any
      // previously authorized connections.
      // Per OAuth v2, this endpoint isn't idempotent. Consuming an authorization code more than once revokes the account connection.
      // Ref - https://stripe.com/docs/connect/oauth-reference
      await db
        .main('fleet_payment_settings')
        .where({
          fleet_id: stripeInfo.fleet_id
        })
        .update({
          stripe_account_id: null,
          payment_gateway: null
        })

      return done(errors.internalServer(false), null)
    }
    if (!body['stripe_user_id']) {
      logger(`Error: There's no stripe_user_id in oauth token verification response`)
      return done(errors.internalServer(false), null)
    }
    fleetHandler.updateFleetProfile(
      {
        stripe_account_id: body['stripe_user_id'],
        payment_gateway: 'stripe'
      },
      { fleet_id: stripeInfo.fleet_id },
      (error) => {
        if (error) {
          Sentry.captureException(error)
          logger(
            'Error: making update fleet query:',
            errors.errorWithMessage(error)
          )
          done(errors.internalServer(false), null)
          return
        }
        done(errors.noError(), null)
      }
    )
  })
}

const connectMercadoPago = async ({ fleet_id: fleetId, privateKey, publicKey }) => {
  const { fleets: fleet, fleet_payment_settings: fleetPaymentSettings } = await db
    .main('fleets')
    .where({ 'fleets.fleet_id': fleetId })
    .leftJoin('fleet_payment_settings', 'fleets.fleet_id', 'fleet_payment_settings.fleet_id')
    .select('fleets.operator_id', 'fleets.fleet_id', 'fleet_payment_settings.fleet_id')
    .first()
    .options({ nestTables: true })

  if (!fleet) {
    logger('Invalid fleet', { fleetId })
    throw errors.customError('Bad request', responseCodes.BadRequest, 'BadRequest')
  }

  if (!fleetPaymentSettings) {
    throw errors.customError(
      'Missing fleet payment settings. Please set up fleet payment profile first',
      responseCodes.BadRequest,
      'BadRequest'
    )
  }

  const mpIntegration = await db
    .main('integrations')
    .where({
      fleet_id: fleetId,
      integration_type: 'mercadopago'
    })
    .first()

  if (mpIntegration) {
    await db.main('integrations')
      .where({
        integrations_id: mpIntegration.integrations_id
      })
      .update({
        client_secret: privateKey,
        client_id: publicKey
      })
  } else {
    const operator = await db
      .users('operators')
      .where({ operator_id: fleet.operator_id })
      .first()
      .select('email')

    if (!operator) {
      logger('Fleet is missing operator', { fleetId })
      throw errors.customError('Bad request', responseCodes.BadRequest, 'BadRequest')
    }

    await db.main('integrations').insert({
      email: operator.email,
      fleet_id: fleetId,
      integration_type: 'mercadopago',
      client_secret: privateKey,
      client_id: publicKey
    })
  }

  await db
    .main('fleet_payment_settings')
    .where({
      fleet_id: fleetId
    })
    .update({ payment_gateway: 'mercadopago' })

  return {
    message: 'Connected successfully'
  }
}

const _handleMissingPaymentIntent = error => {
  if (error.code !== 'resource_missing') {
    return Promise.reject(error)
  }

  return Promise.reject(errors.customError(
    `Payment not found`,
    responseCodes.ResourceNotFound,
    'NotFound',
    false
  ))
}

const _retrievePlatformPaymentIntent = ({ paymentIntentId }) => {
  return stripe.paymentIntents
    .retrieve(paymentIntentId)
    .catch(_handleMissingPaymentIntent)
}

const _retrieveConnectedAccountPaymentIntent = ({ paymentIntentId, connectedAccountId }) => {
  return stripe.paymentIntents
    .retrieve(paymentIntentId, { stripe_account: connectedAccountId })
    .catch(_handleMissingPaymentIntent)
}

const _retrievePaymentIntent = ({ paymentIntentId, connectedAccountId }) => {
  return _retrieveConnectedAccountPaymentIntent({ paymentIntentId, connectedAccountId })
    .catch(e => {
      if (e.code === responseCodes.ResourceNotFound) {
        return _retrievePlatformPaymentIntent({ paymentIntentId })
      }

      return Promise.reject(e)
    })
}

const _computeRefundAmount = ({ amount, stripeCharge }) => {
  if (!amount) {
    return undefined // Stripe refunds entire amount when amount is not provided
  }

  const amountToRefund = amount * 100
  let amountRefundable = stripeCharge.amount

  if (stripeCharge.amount_refunded) {
    amountRefundable -= stripeCharge.amount_refunded
  }

  if (amountToRefund > amountRefundable) {
    throw errors.customError(
      `Maximum amount refundable is ${amountRefundable / 100}`,
      responseCodes.BadRequest,
      null,
      true
    )
  }

  return amountToRefund
}

const refundPaymentIntent = ({ paymentIntentId, connectedAccountId, amount }) => {
  return _retrievePaymentIntent({ paymentIntentId, connectedAccountId })
    .then(paymentIntent => {
      const isDirectCharge = paymentIntent.transfer_data === null
      const stripeCharge = paymentIntent.charges.data[0]

      const refundParams = {
        amount: _computeRefundAmount({ amount, stripeCharge })
      }

      const options = {}

      if (isDirectCharge) {
        refundParams.payment_intent = paymentIntent.id
        options.stripe_account = connectedAccountId
      } else {
        refundParams.charge = stripeCharge.id
        refundParams.reverse_transfer = true
      }

      return isDirectCharge
        ? stripe.refunds.create(refundParams, options)
        : stripe.refunds.create(refundParams)
    })
    .catch(e => {
      if (e.raw) {
        logger('Stripe error: ', e)

        return Promise.reject(errors.customError(
          e.raw.message || `Error retrieving payment: ${e.raw.code || e.raw.type}`,
          e.statusCode || responseCodes.InternalServer,
          null,
          false
        ))
      }

      return Promise.reject(e)
    })
}

module.exports = {
  refundPaymentIntent,
  saveStripeConnectInfo,
  connectMercadoPago
}
