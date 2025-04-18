const util = require('util')
const moment = require('moment')

const { errors, responseCodes: codes } = require('@velo-labs/platform')

const paymentHandler = require('../payment-handler')
const db = require('../../db')
const dbHelpers = require('../../helpers/db')
const conversions = require('../../utils/conversions')
const payments = require('../payments')

const getPrimaryCard = util.promisify(paymentHandler.getPrimaryCard)

function endOfDay (basis) {
  return moment(basis).endOf('day').startOf('second').toDate()
}

function startOfDay (basis) {
  return moment(basis).startOf('day').toDate()
}

/**
 * Given a time basis and a time frequency, determine the start and end period
 * dictated by that time frequency.
 *
 * Computed periods are as of start of and end of corresponding days.
 *
 * @param {object} options
 * @param {Date} options.basis - Determine periods based on this this time
 * @param {string} options.frequency - Time frequency, one of ['monthly', 'weekly']
 *
 * @returns {{ periodStart: Date, periodEnd: Date }}
 */
function computeSubscriptionPeriods ({ basis, frequency }) {
  const start = startOfDay(basis)

  const frequencies = {
    weekly: 'week',
    monthly: 'month',
    yearly: 'year'
  }

  const period = frequencies[frequency]

  if (!period) {
    throw errors.customError(
      `Invalid subscription frequency: ${frequency}. Can only be one of ['weekly', 'monthly', 'yearly'].`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  const end = endOfDay(moment(basis).add(1, period))

  return { periodStart: start, periodEnd: end }
}

/**
 * Retrieve payment settings for a user.
 *
 * @param {object} params
 * @param {object} params.user User object
 * @param {number} params.user.user_id ID of the user
 *
 * @returns {Promise<object>} Promise resolving with the user payment settings.
 */
async function retrieveUserPaymentSettings ({ user }) {
  const [settings] = await getPrimaryCard({ user_id: user.user_id })

  if (!settings) {
    throw errors.customError(
      `Missing credit card for user ${user.user_id}`,
      codes.BadRequest,
      'BadRequest'
    )
  }

  return settings
}

/**
 * Charge a user for a membership plan.
 *
 * @param {object} options
 *
 * @param {object} options.userPaymentSettings
 * @param {string} options.userPaymentSettings.stripe_net_profile_id Stripe customer ID of the user charged.
 * @param {string} options.userPaymentSettings.paymentMethod
 *
 * @param {object} options.fleetMembership
 * @param {string} options.fleetMembership.membership_price_currency Membership plan currency
 * @param {number} options.fleetMembership.membership_price Price of this membership plan. Amount to charge for this membership.
 *
 * @param {object} options.fleetPaymentSettings
 * @param {string} options.fleetPaymentSettings.stripe_account_id Stripe account ID of the fleet the membership belongs to.
 *
 * @returns {Promise<object>} Promise resolving with Stripe charge object.
 */
async function chargeForMembership ({
  userPaymentSettings,
  fleetMembership,
  fleetPaymentSettings
}) {
  const price = Number(fleetMembership.membership_price) + (Number(fleetMembership.tax_sub_total) || 0)
  const charge = await payments.charge({
    amount: Number(price),
    currency: fleetMembership.membership_price_currency,
    card: userPaymentSettings,
    fleetPaymentSettings
  })

  return charge
}

/**
 * Create a subscription payment record in the database.
 *
 * @param {object} options
 *
 * @param {object} options.payment Stripe PaymentIntent or Charge object
 * @param {string} options.payment.id ID of the Stripe payment
 *
 * @param {object} options.fleetMembership Fleet membership plan paid for by this payment.
 * @param {string} options.fleetMembership.membership_price_currency Membership plan currency
 * @param {number} options.fleetMembership.membership_price Price of this membership plan.
 *
 * @param {object} options.userPaymentSettings
 * @param {string} options.userPaymentSettings.stripe_net_profile_id Stripe customer ID of the user charged.
 * @param {string} options.userPaymentSettings.card_id ID of the user's credit card.
 *
 * @param {object} options.membershipSubscription Subscription paid for by this payment
 * @param {number} options.membershipSubscription.membership_subscription_id Subscription ID
 * @param {Date} options.membershipSubscription.period_start Subscription valid from
 * @param {Date} options.membershipSubscription.period_end Subscription valid until
 *
 * @param {object} context
 * @param {object} [context.trx] Knex transaction.
 *
 * @returns {Promise<object>} Promise resolving with the inserted payment record.
 */
async function recordSubscriptionPayment (
  { payment, fleetMembership, userPaymentSettings, membershipSubscription },
  context = {}
) {
  const price = Number(fleetMembership.membership_price) + (Number(fleetMembership.tax_sub_total) || 0)
  const paymentRecord = {
    membership_subscription_id:
      membershipSubscription.membership_subscription_id,
    currency: fleetMembership.membership_price_currency,
    amount: Number(price),
    card_id: userPaymentSettings.card_id,
    stripe_customer_id: userPaymentSettings.stripe_net_profile_id,
    transaction_id: payment.id,
    paid_on: new Date(),
    period_start: membershipSubscription.period_start,
    period_end: membershipSubscription.period_end
  }

  const trx = context.trx || (await db.main.transaction())

  const [id] = await trx('membership_subscription_payments').insert(
    paymentRecord
  )

  if (!context.trx) {
    await trx.commit()
  }

  paymentRecord.membership_subscription_payment_id = id

  return paymentRecord
}

const formatMembership = ({
  addresses: address,
  fleet_memberships: membership,
  fleets: fleet,
  fleet_payment_settings: ps,
  membership_subscription_payments: mp
}) => {
  fleet.address = address
  if (['public', 'private'].includes(fleet.type)) fleet.fleet_payment_settings = {...ps, enable_preauth: !!ps.enable_preauth}
  fleet.skip_parking_image = !!fleet.skip_parking_image
  fleet.parking_area_restriction = !!fleet.parking_area_restriction

  membership.fleet = fleet
  membership.membership_subscription_payments = mp
  return membership
}

const formatSubscription = ({ membership_subscriptions: sub, ...rest }) => {
  const membership = formatMembership(rest)
  const fleetMeta = membership.fleet.fleet_metadata
  const fleetPaymentSettings = {...membership.fleet.fleet_payment_settings, enable_preauth: !!(membership.fleet && membership.fleet.fleet_payment_settings && membership.fleet.fleet_payment_settings.enable_preauth)}
  if (['public', 'private'].includes(membership.fleet.type)) membership.fleet.fleet_payment_settings = fleetPaymentSettings
  membership.fleet = {...membership.fleet, ...fleetMeta}
  delete membership.fleet.fleet_metadata
  sub.fleet_membership = membership

  return sub
}

/**
 * Retrieve all currently active subscriptions for the given user.
 *
 * When a fleet ID is provided, only active subscriptions for that fleet are retrieved.
 *
 * @param {object} options
 * @param {object} [options.params = {}]
 * @param {number} [options.params.fleet_id] Fleet that owns the active plans
 *                                           being retrieved.
 * @param {object} options.user
 * @param {number} options.user.user_id ID of the user owning the active
 *                                      subscriptions being retrieved.
 *
 * @returns {Promise<object>[]} Promise resolving with list of active user subscriptions.
 */
async function userSubscriptions ({ params, user }) {
  const results = await db
    .main('membership_subscriptions')
    .where({
      'membership_subscriptions.user_id': user.user_id
    })
    .andWhere(function () {
      this.where('membership_subscriptions.period_end', '>', new Date())
    })
    .join('fleet_memberships', {
      ...(params.fleet_id
        ? { 'fleet_memberships.fleet_id': params.fleet_id }
        : undefined),
      'fleet_memberships.fleet_membership_id':
        'membership_subscriptions.fleet_membership_id'
    })
    .leftOuterJoin('membership_subscription_payments', 'membership_subscription_payments.membership_subscription_id', 'membership_subscriptions.membership_subscription_id')
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
    .options({ nestTables: true, typeCast: dbHelpers.castDataTypes })
    .select()
    // .groupBy('membership_subscriptions.membership_subscription_id')
  let groupvalues = results.reduce(function (obj, item) {
    obj[item.membership_subscriptions.membership_subscription_id] = obj[item.membership_subscriptions.membership_subscription_id] || []
    obj[item.membership_subscriptions.membership_subscription_id].push(item.membership_subscription_payments)
    return obj
  }, {})
  let result = results.reduce((unique, o) => {
    if (!unique.some(obj => obj.membership_subscriptions.membership_subscription_id === o.membership_subscriptions.membership_subscription_id)) {
      unique.push(o)
    }
    return unique
  }, [])
  result.map((obj) => {
    Object.keys(groupvalues).map(function (key) {
      // eslint-disable-next-line eqeqeq
      if (key == obj.membership_subscriptions.membership_subscription_id) {
        delete obj.membership_subscription_payments
        obj.membership_subscription_payments = groupvalues[key]
      }
    })
  })
  return result.map(formatSubscription)
}

/**
 * Find the active user subscription for a given fleet.
 */
async function activeSubscription ({ userId, fleetId }) {
  const [subscription] = await userSubscriptions({
    params: { fleet_id: fleetId },
    user: { user_id: userId }
  })

  return subscription || null
}

const calculateTaxes = async (fleetId, amount, discount) => {
  const fleetTaxes = await db.main('tax').where({ fleet_id: fleetId, status: 'active' })
  const taxes = []
  let taxTotal = 0
  let totalAmount = Number(amount)
  if (discount) totalAmount = ((100 - discount) / 100) * totalAmount
  if (fleetTaxes.length) {
    for (let tax of fleetTaxes) {
      const amount = (tax.tax_percent / 100 * totalAmount).toFixed(2)
      taxTotal = taxTotal + Number(amount)
      const taxObject = { taxId: tax.tax_id, name: tax.tax_name, percentage: tax.tax_percent, amount: +amount }
      taxes.push(taxObject)
    }
  }
  return { taxes, taxTotal }
}

/**
 * Apply membership discount from a membership subscription to a given amount.
 *
 * @param {object} options
 * @param {number} options.userId
 * @param {number} options.fleetId
 * @param {number} options.amount Amount to apply discount to
 *
 * @returns {Promise<{ discount: number, discountedAmount: number }>}
 */
async function applyMembershipIncentive ({ userId, fleetId, amount }) {
  const subscription = await activeSubscription({ userId, fleetId })

  if (!subscription) {
    // Just calculate tax on trip charge
    const taxInfoWithNoSubscription = await calculateTaxes(fleetId, amount)
    return {
      discount: 0.0,
      discountedAmount: amount,
      taxInfo: taxInfoWithNoSubscription
    }
  }

  const incentive = Number(subscription.fleet_membership.membership_incentive)

  // Free when discount is 100%
  if (incentive === 100) {
    return {
      subscription,
      discount: amount,
      discountedAmount: 0,
      taxInfo: null
    }
  }

  const discount = conversions.roundOffPricing((amount * incentive) / 100)
  const taxInfo = await calculateTaxes(fleetId, amount, incentive)

  return {
    subscription,
    discount,
    discountedAmount: conversions.roundOffPricing(amount - discount),
    taxInfo
  }
}

module.exports = {
  applyMembershipIncentive,
  chargeForMembership,
  endOfDay,
  computeSubscriptionPeriods,
  recordSubscriptionPayment,
  retrieveUserPaymentSettings,
  startOfDay,
  userSubscriptions,
  formatMembership,
  formatSubscription,
  calculateTaxes
}
