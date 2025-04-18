const { errors, responseCodes } = require('@velo-labs/platform')

const moment = require('moment')
const db = require('../../db')
const conversions = require('../../utils/conversions')

async function retrieve ({ id, fleetId }, ctx = { activeOnly: true }) {
  const filters = { pricing_option_id: id }

  if (fleetId) {
    filters.fleet_id = fleetId
  }

  if (ctx.activeOnly) {
    filters.deactivated_at = null
  }

  return (ctx.trx ? ctx.trx('pricing_options') : db.main('pricing_options'))
    .where(filters)
    .first()
}

function durationsBetween ({ pricingOption, start, end }) {
  const pricingDuration = moment.duration(
    pricingOption.duration,
    pricingOption.duration_unit
  )

  // Pricing re-ups. We want the upper bound to re-up the pricing (ceil).
  // If the overflow is less than a minute, ignore (floor).
  // TODO (waiyaki 2021-05-20): grace period?
  return Math.ceil(
    Math.floor(moment.duration(end.diff(start)).asMinutes()) /
      pricingDuration.asMinutes()
  )
}
/**
 * Given a pricing option, a `start` time and an [optional] `end` time, compute the
 * cost of the durations between `end` and `start` as dictated by the cost per duration
 * in the pricing option. The pricing option dictates the length of a single duration.
 *
 * @param {object} params
 * @param {number} params.pricingOptionsId ID of the pricing option to use
 * @param {number} params.start Start time, in milliseconds
 * @param {number} [params.end] End time, in milliseconds. When not provided,
 *                              defaults to current time in Unix milliseconds.
 *
 * @returns {{currency: string, duration: number, amount: number}}
 */
async function getDurationCost ({ pricingOptionId, start, end, duration }) {
  // Time periods
  const pStart = moment.unix(start)
  const pEnd = end ? moment.unix(end) : moment()

  const pricingOption = await retrieve(
    { id: pricingOptionId },
    { activeOnly: false }
  )

  if (!pricingOption) {
    throw errors.customError(
      'Pricing option not found.',
      responseCodes.ResourceNotFound,
      'NotFound'
    )
  }

  const durations = durationsBetween({
    pricingOption,
    start: pStart,
    end: pEnd
  })

  const cost = conversions.roundOffPricing(pricingOption.price * durations)

  return {
    duration,
    currency: pricingOption.price_currency,
    pricing_duration: moment.duration(pEnd.diff(pStart)).asMilliseconds(),
    amount: cost
  }
}

async function list ({ fleet_id: fleetId }) {
  return db
    .main('pricing_options')
    .where({ fleet_id: fleetId, deactivated_at: null })
    .select()
}

module.exports = {
  list,
  retrieve,
  getDurationCost
}
