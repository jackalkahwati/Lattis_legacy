const _ = require('underscore')
const { errors, responseCodes } = require('@velo-labs/platform')

const db = require('../db')
const Sentry = require('../utils/configureSentry')

const MAX_ACTIVE_OPTIONS = 5

async function retrieve (pricingOptionId, context = {}) {
  const qb = context.trx
    ? context.trx('pricing_options')
    : db.main('pricing_options')

  return qb.where({ pricing_option_id: pricingOptionId }).first().select()
}

async function create ({ fleet_id: fleetId, ...data }, context = {}) {
  const trx = context.trx || (await db.main.transaction())

  try {
    const pricingOptions = await trx('pricing_options')
      .where({
        fleet_id: fleetId,
        deactivated_at: null
      })
      .limit(MAX_ACTIVE_OPTIONS)
      .select('pricing_option_id')

    if (pricingOptions.length === 5) {
      throw errors.customError(
        `Fleet cannot have more than ${MAX_ACTIVE_OPTIONS} active pricing options.`,
        responseCodes.Conflict,
        'Conflict'
      )
    }

    let currency = data.currency

    if (!currency) {
      const paymentSettings = await trx('fleet_payment_settings')
        .where({ fleet_id: fleetId })
        .select('currency')
        .first()

      currency = paymentSettings.currency
    }

    const [pricingOptionId] = await trx('pricing_options').insert({
      fleet_id: fleetId,
      price_currency: currency,
      ...data
    })

    if (!context.trx) {
      await trx.commit()
    }

    return retrieve(pricingOptionId, context)
  } catch (error) {
    Sentry.captureException(error, {fleetId})
    if (!context.trx) {
      await trx.rollback()
    }

    throw error
  }
}

async function list ({ fleet_id: fleetId }) {
  return db
    .main('pricing_options')
    .where({ fleet_id: fleetId, deactivated_at: null })
    .select()
}

async function update ({ fleet_id: fleetId, pricing_option_id: id }, params) {
  const option = await db
    .main('pricing_options')
    .where({
      fleet_id: fleetId,
      pricing_option_id: id,
      deactivated_at: null
    })
    .first()
    .select()

  if (!option) {
    throw errors.customError(
      `Pricing option id ${id} is missing or inactive.`,
      responseCodes.ResourceNotFound,
      'NotFound'
    )
  }

  const current = _.omit(option, 'created_at', 'pricing_option_id')

  return db.main.transaction(async (trx) => {
    await trx('pricing_options').where({ pricing_option_id: id }).update({
      deactivated_at: new Date(),
      deactivation_reason: 'supersession'
    })

    const updated = await create({ ...current, ...params }, { trx })

    return updated
  })
}

const activate = async ({
  fleet_id: fleetId,
  pricing_option_id: pricingOptionId
}) => {
  const option = await db
    .main('pricing_options')
    .where({
      fleet_id: fleetId,
      pricing_option_id: pricingOptionId
    })
    .andWhere(function () {
      this.whereNotNull('deactivated_at').andWhere({
        deactivation_reason: 'deactivation'
      })
    })
    .first()
    .select()

  if (!option) {
    throw errors.customError(
      `Pricing option id ${pricingOptionId} cannot be activated.`,
      responseCodes.Conflict,
      'Conflict'
    )
  }

  const activeOptions = await list({ fleet_id: option.fleet_id })

  if (activeOptions.length >= MAX_ACTIVE_OPTIONS) {
    throw errors.customError(
      `Fleet cannot have more than ${MAX_ACTIVE_OPTIONS} active pricing options.`,
      responseCodes.Conflict,
      'Conflict'
    )
  }

  await db
    .main('pricing_options')
    .where('pricing_option_id', pricingOptionId)
    .update({
      deactivated_at: null,
      deactivation_reason: null
    })

  return retrieve(pricingOptionId)
}

const deactivate = async ({
  fleet_id: fleetId,
  pricing_option_id: pricingOptionId
}) => {
  await db
    .main('pricing_options')
    .where({
      fleet_id: fleetId,
      pricing_option_id: pricingOptionId,
      deactivated_at: null
    })
    .update({
      deactivated_at: new Date(),
      deactivation_reason: 'deactivation'
    })

  return retrieve(pricingOptionId)
}

module.exports = {
  create,
  retrieve,
  list,
  update,
  activate,
  deactivate
}
