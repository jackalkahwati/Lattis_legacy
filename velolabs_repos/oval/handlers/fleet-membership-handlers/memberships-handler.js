const {
  errors,
  responseCodes: codes,
  dbConstants
} = require('@velo-labs/platform')

const db = require('../../db')
const dbHelpers = require('../../helpers/db')
const fleetConstants = require('../../constants/fleet-constants')
const subscriptionsHandler = require('./subscriptions-handler')
const helpers = require('./helpers')
const emails = require('./emails')
const { configureSentry } = require('../../utils/configureSentry')

const Sentry = configureSentry()

async function createMembershipSubscription (
  { user, fleetMembership },
  { trx }
) {
  const { periodStart, periodEnd } = helpers.computeSubscriptionPeriods({
    basis: new Date(),
    frequency: fleetMembership.payment_frequency
  })

  const subscription = {
    fleet_membership_id: fleetMembership.fleet_membership_id,
    user_id: user.user_id,
    activation_date: new Date(),
    period_start: periodStart,
    period_end: periodEnd
  }

  const [id] = await trx('membership_subscriptions').insert(subscription)

  const sub = await trx('membership_subscriptions')
    .where('membership_subscription_id', id)
    .first()
    .options({ typeCast: dbHelpers.castDataTypes })

  return sub
}

/**
 * Subscribe a user to the currently active membership plan for the fleet
 * represented by the provided fleet id.
 *
 * @param {object} options
 * @param {object} options.params
 * @param {number} options.params.membership_id ID of membership plan to subscribe to.
 * @param {object} options.user
 * @param {number} options.user.user_id ID of the user subscribing to a membership plan.
 *
 * @returns {object} The created subscription.
 */
async function subscribe ({ params, user }) {
  const [results] = await db
    .main('fleet_memberships')
    .where({
      'fleet_memberships.fleet_membership_id': params.membership_id,
      'fleet_memberships.deactivation_date': null
    })
    .join('fleets', {
      'fleets.fleet_id': 'fleet_memberships.fleet_id'
    })
    .join('fleet_payment_settings', {
      'fleets.fleet_id': 'fleet_payment_settings.fleet_id'
    })
    .options({ nestTables: true, typeCast: dbHelpers.castDataTypes })
    .select(
      'fleets.*',
      'fleet_payment_settings.stripe_account_id',
      'fleet_memberships.*'
    )

  if (!results) {
    throw errors.customError(
      `Membership plan ${params.membership_id} cannot be subscribed to. ` +
        `Please make sure it exists and is active on a fleet.`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  const {
    fleet_memberships: fleetMembership,
    fleet_payment_settings: fleetPaymentSettings,
    fleets: fleet
  } = results

  if (fleetPaymentSettings.payment_gateway === 'mercadopago') {
    // TODO: (waiyaki: 09/03/2020)
    // Cannot support subscriptions via MP since we cannot auto-charge the
    // credit card without the user entering the cvc. MP has a solution for
    // subscriptions, but that's not currently implemented.
    throw errors.customError(
      'Sorry, this fleet does not support membership subscriptions.',
      codes.BadRequest,
      'BadRequest'
    )
  }

  const existingSubscriptions = await subscriptionsHandler.list({
    params: { fleet_id: fleet.fleet_id },
    user
  })

  if (existingSubscriptions.length > 0) {
    throw errors.customError(
      `User ${user.user_id} has an active subscription for fleet ${fleet.fleet_id}`,
      codes.Conflict,
      'Conflict'
    )
  }

  const userPaymentSettings = await helpers.retrieveUserPaymentSettings({
    user
  })

  const payment = await helpers.chargeForMembership({
    fleetMembership,
    userPaymentSettings,
    fleetPaymentSettings
  })

  try {
    const subscription = await db.main.transaction(async (trx) => {
      const membershipSubscription = await createMembershipSubscription(
        { user, fleetMembership },
        { trx }
      )

      await helpers.recordSubscriptionPayment(
        {
          payment,
          fleetMembership,
          userPaymentSettings,
          membershipSubscription
        },
        { trx }
      )

      membershipSubscription.fleet_membership = fleetMembership

      return membershipSubscription
    })

    emails.sendSubscriptionEmail({
      user,
      fleet,
      subscription,
      fleetMembership,
      paymentDetails: userPaymentSettings
    })

    return subscriptionsHandler.retrieve({
      params: { subscription_id: subscription.membership_subscription_id },
      user
    })
  } catch (error) {
    Sentry.captureException(error)
    // TODO: (waiyaki 11/04/2020): Refund payment here?
    throw error
  }
}

/**
 * Retrieve a membership plan.
 *
 * @param {object} options
 * @param {object} options.params
 * @param {number} options.params.membership_id ID of the membership to retrieve
 * @returns {object} The retrieved membership plan.
 */
async function retrieve ({ params }) {
  const [fleetMembership] = await db
    .main('fleet_memberships')
    .where({ 'fleet_memberships.fleet_membership_id': params.membership_id })
    .join('fleets', 'fleets.fleet_id', 'fleet_memberships.fleet_id')
    .join('addresses', function () {
      this.on('addresses.type', '=', db.main.raw('?', ['fleet'])).andOn(
        'addresses.type_id',
        '=',
        'fleets.fleet_id'
      )
    })
    .leftOuterJoin(
      'fleet_payment_settings',
      'fleet_payment_settings.fleet_id',
      'fleets.fleet_id'
    )
    .options({ typeCast: dbHelpers.castDataTypes, nestTables: true })
    .select()
    .groupBy('fleet_memberships.fleet_membership_id')

  if (!fleetMembership) {
    throw errors.customError(
      `Membership plan ${params.membership_id} does not exist.`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  return helpers.formatMembership(fleetMembership)
}

/**
 * Retrieve active memberships available to a user.
 * By default, return all active memberships for all user-accessible fleets.
 * If a `fleet_id` is provided, return just the active memberships in that fleet.
 *
 * @param {object} options
 * @param {object} options.params
 * @param {number} [options.params.fleet_id] ID of the fleet whose memberships to query
 * @param {object} options.user
 * @param {number} options.user.user_id ID of the requesting user
 *
 * @returns {Promise<object[]>} The retrieved memberships
 */
async function list ({ params, user }) {
  const fleetsBuilder = db.main('fleets')

  const accessiblePrivateFleets = await db
    .users(dbConstants.tables.private_fleet_users)
    .where({ user_id: user.user_id, access: 1, verified: 1 })
    .select('fleet_id')

  function restrictAccessible (builder) {
    builder.whereIn('fleets.type', [
      fleetConstants.fleet_type.publicWithPayment,
      fleetConstants.fleet_type.publicWithNoPayment
    ])

    if (accessiblePrivateFleets.length) {
      builder.orWhereIn(
        'fleets.fleet_id',
        accessiblePrivateFleets.map((f) => f.fleet_id)
      )
    }
  }

  if (params.fleet_id) {
    fleetsBuilder
      .where('fleets.fleet_id', params.fleet_id)
      .andWhere(function () {
        restrictAccessible(this)
      })
  } else {
    fleetsBuilder.where(function () {
      restrictAccessible(this)
    })
  }

  fleetsBuilder
    .join('addresses', function () {
      this.on('addresses.type', '=', db.main.raw('?', ['fleet'])).andOn(
        'addresses.type_id',
        '=',
        'fleets.fleet_id'
      )
    })
    .join('fleet_memberships', function () {
      this.on('fleet_memberships.fleet_id', '=', 'fleets.fleet_id').onNull(
        'fleet_memberships.deactivation_date'
      )
    })
    .leftOuterJoin(
      'fleet_payment_settings',
      'fleet_payment_settings.fleet_id',
      'fleets.fleet_id'
    )
    .options({ nestTables: true, typeCast: dbHelpers.castDataTypes })
    .select()
    .groupBy('fleet_memberships.fleet_membership_id')

  const results = await fleetsBuilder

  return results.map(helpers.formatMembership)
}

module.exports = {
  list,
  retrieve,
  subscribe,
  unsubscribe: subscriptionsHandler.unsubscribe
}
