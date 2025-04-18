const _ = require('underscore')
const moment = require('moment')
const { CronJob } = require('cron')
const { logger } = require('@velo-labs/platform')

const db = require('../../db')
const helpers = require('./helpers')
const subscriptionHandlers = require('./subscriptions-handler')
const emails = require('./emails')
const { configureSentry } = require('../../utils/configureSentry')

const Sentry = configureSentry()

async function subscriptionsPendingRenewal ({ basis }) {
  const nextStart = helpers.startOfDay(moment(basis))
  const periodEnd = helpers.endOfDay(moment(basis).subtract(1, 'day'))

  const toRenew = await db
    .main('membership_subscriptions')
    .where({
      'membership_subscriptions.period_end': periodEnd,
      'membership_subscriptions.deactivation_date': null
    })
    // Only include subscriptions without next period's payment already on record.
    .where(
      'membership_subscription_payments.membership_subscription_payment_id',
      null
    )
    .join('fleet_memberships', {
      'fleet_memberships.fleet_membership_id':
        'membership_subscriptions.fleet_membership_id'
    })
    .leftOuterJoin('membership_subscription_payments', function () {
      this.on(
        'membership_subscription_payments.membership_subscription_id',
        '=',
        'membership_subscriptions.membership_subscription_id'
      ).andOn(
        'membership_subscription_payments.period_start',
        '=',
        db.main.raw('?', [nextStart])
      )
    })
    .join('fleet_payment_settings', {
      'fleet_payment_settings.fleet_id': 'fleet_memberships.fleet_id'
    })
    .join('fleets', {
      'fleets.fleet_id': 'fleet_memberships.fleet_id'
    })
    .groupBy('membership_subscriptions.membership_subscription_id')
    .options({ nestTables: true })

  return toRenew.reduce(
    (
      subs,
      {
        membership_subscriptions: sub,
        fleet_memberships: membership,
        fleet_payment_settings: paymentSettings,
        fleets: fleet
      }
    ) => {
      sub.fleet_membership = membership
      sub.fleet_payment_settings = paymentSettings
      sub.fleet = fleet

      return subs.concat(sub)
    },
    []
  )
}

async function updateSubscriptionPeriods (
  { basis, subscription, fleetMembership },
  { trx }
) {
  const { periodStart, periodEnd } = helpers.computeSubscriptionPeriods({
    basis,
    frequency: fleetMembership.payment_frequency
  })

  await trx('membership_subscriptions')
    .where({
      membership_subscription_id: subscription.membership_subscription_id,
      deactivation_date: null
    })
    .update({ period_start: periodStart, period_end: periodEnd })

  return {
    ...subscription,
    period_start: periodStart,
    period_end: periodEnd
  }
}

async function renewSubscription ({ basis, subscription }) {
  const {
    fleet,
    fleet_membership: fleetMembership,
    fleet_payment_settings: fleetPaymentSettings
  } = subscription

  const user = { user_id: subscription.user_id }

  const userPaymentSettings = await helpers.retrieveUserPaymentSettings({
    user
  })

  let payment

  try {
    payment = await helpers.chargeForMembership({
      fleetMembership,
      userPaymentSettings,
      fleetPaymentSettings
    })
  } catch (error) {
    Sentry.captureException(error)
    await subscriptionHandlers.unsubscribe(
      {
        user,
        params: { membership_id: fleetMembership.fleet_membership_id }
      },
      {
        sendNotificationEmail: false
      }
    )

    const info = {
      error,
      data: { subscription },
      message: 'Failed to charge for membership. Member has been unsubscribed.'
    }

    throw info
  }

  const sub = await db.main.transaction(async (trx) => {
    const renewedSubscription = await updateSubscriptionPeriods(
      {
        basis,
        subscription,
        fleetMembership
      },
      { trx }
    )

    await helpers.recordSubscriptionPayment(
      {
        payment,
        fleetMembership,
        userPaymentSettings,
        membershipSubscription: renewedSubscription
      },
      { trx }
    )

    return renewedSubscription
  })

  emails.sendSubscriptionEmail({
    user,
    fleet,
    renewal: true,
    fleetMembership,
    subscription: sub,
    paymentDetails: userPaymentSettings
  })

  return sub
}

const reflect = (p) =>
  p.then(
    (result) => ({ result, status: 'fulfilled' }),
    (result) => ({ result, status: 'rejected' })
  )

async function renew ({ basis }) {
  const subscriptions = await subscriptionsPendingRenewal({ basis })

  if (subscriptions.length === 0) {
    logger('No renewable subscriptions found.', { basis })
    return
  }

  logger(`Renewing ${subscriptions.length} subscriptions`, { basis })

  const renewals = await Promise.all(
    subscriptions.map((subscription) =>
      reflect(renewSubscription({ basis, subscription }))
    )
  )

  return _.groupBy(renewals, 'status')
}

async function performRenewals ({ basis }) {
  const renewals = await renew({ basis })

  if (!renewals) {
    return
  }

  if (renewals.fulfilled) {
    logger(`Renewed ${renewals.fulfilled.length} subscriptions`, {
      subscriptions: renewals.fulfilled.map(
        (p) => p.result.membership_subscription_id
      )
    })
  }

  if (renewals.rejected) {
    logger(
      `Failed to renew ${renewals.rejected} subscriptions`,
      renewals.rejected
    )
  }
}

/**
 * Create schedule to renew subscriptions.
 *
 * When `options.start` is `true`, this immediately starts the schedule and also
 * renews every subscription pending renewal as of when this is scheduled.
 *
 * @param {object} [options]
 * @param {boolean} [options.start=true] Where to immediately start the schedule
 *
 * @returns {object} CronJob object
 */
function scheduleRenewals ({ start = true } = {}) {
  // Run 5 seconds after midnight everyday.
  const job = new CronJob('05 00 00 * * *', async function () {
    const basis = new Date()

    logger('Starting renewals:', { basis })

    try {
      await performRenewals({ basis })

      logger('Renewal complete', { basis, nextDate: this.nextDate() })
    } catch (error) {
      Sentry.captureException(error)
      logger('Error performing renewals', error)
    }
  })

  if (start) {
    const nextDate = job.nextDate()

    logger(`Creating member subscriptions renewal schedule:`, {
      start,
      nextDate
    })

    // If running this, `scheduleRenewals` must have just been invoked with
    // `start = true`. With current usage, this happens when the server restarts.
    // Use a `basis` as if this is running as of the previous scheduled time.
    const basis = moment(nextDate).subtract(1, 'day').toDate()

    logger('Starting renewals:', { basis })

    performRenewals({ basis })

    job.start()
  }

  return job
}

module.exports = {
  scheduleRenewals
}
