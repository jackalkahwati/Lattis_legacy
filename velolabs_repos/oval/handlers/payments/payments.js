const { logger, errors, responseCodes } = require('@velo-labs/platform')
const config = require('../../config')
const db = require('../../db')
const { retrieveIntegration } = require('../integrations-handler')
const gateways = require('./gateways')
const { configureSentry } = require('../../utils/configureSentry')

const Sentry = configureSentry()

const GATEWAYS = gateways.GATEWAYS

const STATUS = {
  pending: 'pending',
  succeeded: 'succeeded',
  failed: 'failed',
  canceled: 'canceled',
  refunded: 'refunded'
}

function normalizeBrand (brand) {
  const brands = {
    visa: 'Visa',
    debvisa: 'Visa',
    mastercard: 'MasterCard',
    master: 'MasterCard',
    debmaster: 'MasterCard',
    discover: 'Discover',
    amex: 'American Express',
    'american express': 'American Express',
    'american express company': 'American Express'
  }

  return brands[brand.toLowerCase()] || 'Visa'
}

function normalizeCard ({ card, gateway, fleet }) {
  const source = {
    customer_id: card.customer_id,
    id: card.id,
    cc_no: ['x'.repeat(12), card.last4 || card.last_four_digits].join(''),
    exp_month: card.exp_month || card.expiration_month,
    exp_year: card.exp_year || card.expiration_year,
    fingerprint: card.fingerprint,
    brand: normalizeBrand(
      card.brand || (card.payment_method || { id: 'visa' }).id
    ),
    gateway,
    sourceCard: card,
    fleet
  }

  return source
}

function normalizeCharge ({ charge, gateway }) {
  const currency =
    gateway === GATEWAYS.mercadopago ? charge.currency_id : charge.currency

  return {
    ...charge,
    gateway,
    currency: currency.toUpperCase()
  }
}

function determinePaymentGateway (params, fleet) {
  if (fleet && fleet.payment_gateway) {
    return fleet.payment_gateway
  }

  if (params.token) {
    return GATEWAYS.mercadopago
  }

  return (params.payment_gateway || '').toLowerCase() === GATEWAYS.mercadopago
    ? GATEWAYS.mercadopago
    : GATEWAYS.stripe
}

const connectedTo = (gateway) => (params) =>
  determinePaymentGateway(params) === gateway

const isStripeConnect = connectedTo(GATEWAYS.stripe)
const isMercadoPagoConnect = connectedTo(GATEWAYS.mercadopago)

async function determineFleet ({
  fleet_id: fleetId,
  appDetails: { whiteLabel }
}) {
  if (fleetId) {
    return db.main('fleets').where({ fleet_id: fleetId }).first()
  }

  if (whiteLabel) {
    return db
      .main('fleet_metadata')
      .where({ whitelabel_id: whiteLabel.app_id })
      .innerJoin('fleets', 'fleets.fleet_id', 'fleet_metadata.fleet_id')
      .select('fleets.*')
      .first()
  }

  return null
}

/**
 * For us to use payments with MercadoPago in a test environment, we need to
 * specifically configure a test user and a test seller. We can only use these
 * particular users to pay, if we needed to pay without CVV. This function will
 * take the integration and return the configured test users such that for anyone
 * adding a card or paying, the card or charge will actually be performed against
 * the MercadoPago test users on MP's side while appearing as if it happened
 * to the requesting user on our side. Basically, we replace the requester with
 * a known MP test account for a particular integration.
 */
function resolveUser ({ user, integration }) {
  if (!integration) {
    return { user }
  }

  const result = {
    user,
    credentials: {
      publicKey: integration.client_id,
      accessToken: integration.client_secret
    }
  }

  if (config.mode === 'production') {
    return result
  }

  try {
    const meta =
      integration.metadata && integration.metadata.length
        ? JSON.parse(integration.metadata)
        : null

    if (!meta || !meta.test_users) {
      logger('Missing test user configuration', {
        integrations_id: integration.integrations_id
      })

      return result
    }

    result.user = meta.test_users.customer

    return result
  } catch (error) {
    Sentry.captureException(error, { user: result.user.user_id })
    logger(
      'Error resolving user, falling back:',
      { fallback: { user: result.user.user_id } },
      errors.errorWithMessage(error)
    )

    return result
  }
}

/**
 * Add a card to a customer
 *
 * @param {object} options
 * @param {object} options.user
 * @param {number} options.user.user_id
 * @param {string} options.user.email
 * @param {object} options.params
 * @param {string} [options.params.payment_gateway] - Gateway to add card to. Defaults to 'stripe'
 * @param {number} [options.params.fleet_id] - Fleet to authorize against.
 * @param {string} [options.params.token] - Card token, when gateway is mercadopago
 * @param {object} [options.params.intent] - Setup intent, when gateway is stripe
 *
 */
async function addCard ({ user, params }) {
  const { token } = params

  const fleet = await determineFleet(params)

  if ((token || isMercadoPagoConnect(params)) && !fleet) {
    throw errors.customError(
      '`fleet_id` is required to add a card to Mercado Pago',
      responseCodes.BadRequest,
      'BadRequest'
    )
  }

  const gateway = determinePaymentGateway(params, fleet)

  const userPaymentProfile = await db
    .main('user_payment_profiles')
    .where({ user_id: user.user_id, payment_gateway: gateway })
    .select('stripe_net_profile_id')
    .first()

  if (isMercadoPagoConnect({ payment_gateway: gateway })) {
    const integration = await retrieveIntegration({
      fleet_id: fleet.fleet_id,
      integration_type: gateway
    })

    if (!(integration && integration.client_secret)) {
      logger('Invalid MercadoPago configuration', { fleet: fleet.fleet_id })

      throw errors.customError(
        'Missing MercadoPago configuration',
        responseCodes.Conflict,
        'Conflict'
      )
    }

    const { user: mpUser, credentials = {} } = resolveUser({
      user,
      integration
    })

    return normalizeCard({
      fleet,
      gateway,
      card: await gateways.mercadoPago.addCard(
        { user: mpUser, params, userPaymentProfile },
        { accessToken: credentials.accessToken }
      )
    })
  }

  return normalizeCard({
    fleet,
    gateway,
    card: await gateways.stripe.addCard({
      user,
      params,
      userPaymentProfile
    })
  })
}

/**
 * Charge amount from the card
 *
 * @param {object} options
 * @param {Number} options.amount - amount to be charged
 * @param {String} options.currency - currency the amount is in
 * @param {Object} options.card - Card (user payment profile) to charge from
 * @param {object} options.fleetPaymentSettings Fleet payment settings for fleet
 *                 to create the charge for.
 * @param {object} [context] Additional information to process charge
 * @param {string} [context.cardToken] Card token, required when payment gateway
 *                 is Mercado Pago
 * @param {boolean} [context.authorizeOnly] Create charge now, but capture funds
 *                  later. Only valid when payment gateway is stripe.
 *
 * @returns {object} Gateway-specific charge
 */
async function charge (
  { amount, currency, card, fleetPaymentSettings },
  context = { authorizeOnly: false }
) {
  const gateway = determinePaymentGateway(fleetPaymentSettings)

  if (isMercadoPagoConnect(fleetPaymentSettings) && context.authorizeOnly) {
    throw errors.customError(
      'Capturing is not supported for Mercado Pago connected fleets',
      responseCodes.BadRequest,
      'BadRequest'
    )
  }

  try {
    let integration = {}

    if (isMercadoPagoConnect(fleetPaymentSettings)) {
      integration =
        fleetPaymentSettings.integration ||
        (await retrieveIntegration({
          fleet_id: fleetPaymentSettings.fleet_id,
          integration_type: gateway
        }))

      if (!(integration && integration.client_secret)) {
        logger('Invalid MercadoPago configuration', {
          fleet: fleetPaymentSettings.fleet_id
        })

        throw errors.customError(
          'Missing MercadoPago configuration',
          responseCodes.Conflict,
          'Conflict'
        )
      }
    }

    const appUser = await db
      .users('users')
      .where({ user_id: card.user_id })
      .first()
      .select('email', 'user_id', 'first_name', 'last_name')

    if (isMercadoPagoConnect(fleetPaymentSettings)) {
      const { user, credentials = {} } = resolveUser({
        user: appUser,
        integration
      })

      return normalizeCharge({
        gateway,
        charge: await gateways.mercadoPago.charge(
          { amount, card, user },
          { ...context, ...credentials }
        )
      })
    }

    return normalizeCharge({
      gateway,
      charge: await (context.authorizeOnly
        ? gateways.stripe.authorize
        : gateways.stripe.charge)({
        user: appUser,
        amount,
        currency,
        card,
        fleetPaymentSettings
      })
    })
  } catch (error) {
    Sentry.captureException(error)
    logger(`Error charging ${gateway} card`, errors.errorWithMessage(error))

    throw error
  }
}

async function deleteCard (userPaymentProfile) {
  try {
    if (isStripeConnect(userPaymentProfile)) {
      const pendingPayment = await db
        .main('trip_payment_transactions')
        .where({ card_id: userPaymentProfile.card_id, status: 'pending' })
        .first()

      if (pendingPayment) {
        logger(
          `Card ${userPaymentProfile.id} has pending payment ${pendingPayment.id}`
        )

        throw errors.customError(
          'Cannot delete a source with a pending payment.',
          responseCodes.Conflict,
          'Conflict'
        )
      }

      await gateways.stripe.deleteSource(userPaymentProfile)
    } else {
      if (userPaymentProfile.source_fleet_id) {
        // Need this fleet id to know which fleet to authorize with.
        const integration = await retrieveIntegration({
          fleet_id: userPaymentProfile.source_fleet_id,
          integration_type: GATEWAYS.mercadopago
        })

        if (!(integration && integration.client_secret)) {
          logger(
            'Invalid MercadoPago configuration. This card will not be deleted from payment gateway',
            {
              fleet: userPaymentProfile.source_fleet_id,
              id: userPaymentProfile.id
            }
          )
        } else {
          const { credentials = {} } = resolveUser({ integration })
          await gateways.mercadoPago.deleteCard(userPaymentProfile, {
            accessToken: credentials.accessToken
          })
        }
      }
    }

    await db
      .main('user_payment_profiles')
      .where({ id: userPaymentProfile.id })
      .delete()
  } catch (error) {
    Sentry.captureException(error, {
      user: userPaymentProfile.id
    })
    logger(
      'Error deleting card',
      { userPaymentProfile: userPaymentProfile.id },
      errors.errorWithMessage(error)
    )

    throw error
  }
}

async function updateCard ({ user, params }) {
  const { token } = params

  const fleet = await determineFleet(params)

  if ((token || isMercadoPagoConnect(params)) && !fleet) {
    throw errors.customError(
      '`fleet_id` is required to update a card to Mercado Pago',
      responseCodes.BadRequest,
      'BadRequest'
    )
  }

  const gateway = determinePaymentGateway(params, fleet)

  const userPaymentProfile = await db
    .main('user_payment_profiles')
    .where({ user_id: user.user_id, payment_gateway: gateway })
    .select('stripe_net_profile_id')
    .first()

  if (isMercadoPagoConnect({ payment_gateway: gateway })) {
    // TODO update card for MercadoPago
  }
  let updateCardRes = await gateways.stripe.updateCard({
    user,
    params,
    userPaymentProfile
  })
  if (updateCardRes && updateCardRes.card) {
    updateCardRes.card.id = params.card_id
    updateCardRes.card.customer_id = updateCardRes.customer
    return normalizeCard({
      fleet,
      gateway,
      card: updateCardRes.card || updateCardRes
    })
  } else {
    Sentry.captureException('error in updating card', {
      user: userPaymentProfile.id
    })
    logger(
      JSON.stringify(updateCardRes) + 'Error updating card',
      { userPaymentProfile: userPaymentProfile.id },
      errors.errorWithMessage('error in updating card')
    )
    throw errors.customError(
      'update card failed',
      responseCodes.BadRequest,
      'BadRequest'
    )
  }
}
async function capture ({ amount, paymentIntentId, fleetPaymentSettings }) {
  if (isMercadoPagoConnect(fleetPaymentSettings)) {
    throw errors.customError(
      'Capturing is not supported for Mercado Pago connected fleets',
      responseCodes.BadRequest,
      'BadRequest'
    )
  }

  const preAuthAmount = Number(fleetPaymentSettings.preauth_amount)

  return normalizeCharge({
    gateway: GATEWAYS.stripe,
    charge: await gateways.stripe.capture({
      amountToCapture: amount > preAuthAmount ? preAuthAmount : amount,
      paymentIntentId,
      fleetPaymentSettings
    })
  })
}

module.exports = {
  gateways,
  STATUS,
  addCard,
  charge,
  capture,
  isStripeConnect,
  isMercadoPagoConnect,
  determinePaymentGateway,
  deleteCard,
  updateCard
}
