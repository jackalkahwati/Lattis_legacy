const { errors, responseCodes: codes } = require('@velo-labs/platform')

const db = require('../../db')
const dbHelpers = require('../../helpers/db')
const helpers = require('./helpers')
const emails = require('./emails')

/**
 * Retrieve the details of a single subscription.
 *
 * @param {object} options
 * @param {object} options.params
 * @param {number} [options.params.subscription_id] ID of subscription to retrieve.
 * @param {number} [options.params.membership_id] ID of the membership plan subscribed to
 * @param {object} options.user
 * @param {number} options.user.user_id ID of the user subscribed to the plan.
 *
 * @returns {object} The retrieved subscription.
 */
async function retrieve ({ params, user }) {
  if (!(params.membership_id || params.subscription_id)) {
    throw errors.customError(
      'One of subscription_id or membership_id must be provided.',
      codes.BadRequest,
      'BadRequest'
    )
  }

  const query = db.main('membership_subscriptions')

  if (params.subscription_id) {
    query.where({
      'membership_subscriptions.membership_subscription_id': params.subscription_id,
      user_id: user.user_id
    })
  } else {
    query.where({
      'membership_subscriptions.fleet_membership_id': params.membership_id,
      user_id: user.user_id
    })
  }

  query
    .join('fleet_memberships', {
      'fleet_memberships.fleet_membership_id':
        'membership_subscriptions.fleet_membership_id'
    })
    .join('fleets', 'fleets.fleet_id', 'fleet_memberships.fleet_id')
    .join('addresses', function () {
      this.on('addresses.type', '=', db.main.raw('?', ['fleet'])).andOn(
        'addresses.type_id',
        '=',
        'fleets.fleet_id'
      )
    })
    .leftOuterJoin('fleet_payment_settings', 'fleet_payment_settings.fleet_id', 'fleets.fleet_id')
    .options({ nestTables: true, typeCast: dbHelpers.castDataTypes })
    .select()
    .groupBy('membership_subscriptions.membership_subscription_id')

  const [results] = await query

  if (!results) {
    throw errors.customError(
      'Subscription not found',
      codes.ResourceNotFound,
      'NotFound'
    )
  }

  return helpers.formatSubscription(results)
}

/**
 * Unsubscribe a user from a membership plan.
 *
 * @param {object} options
 * @param {object} options.params
 * @param {number} options.params.membership_id ID of the membership plan to unsubscribe from
 * @param {object} options.user
 * @param {number} options.user.user_id ID of the unsubscribing user.
 *
 * @returns {object} The subscription the user has just unsubscribed from.
 */
async function unsubscribe ({ params, user }, { sendNotificationEmail = true } = {}) {
  const results = await db
    .main('membership_subscriptions')
    .where({
      fleet_membership_id: params.membership_id,
      user_id: user.user_id,
      deactivation_date: null
    })
    .update({ deactivation_date: new Date() })

  const subscription = await retrieve({ params, user })

  if (sendNotificationEmail && results === 1) {
    emails.sendSubscriptionCancelledEmail({ subscription, user })
  }

  return subscription
}

module.exports = {
  retrieve,
  unsubscribe,
  list: helpers.userSubscriptions
}
