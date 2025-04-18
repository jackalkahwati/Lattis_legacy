const _ = require('underscore')
const { errors, responseCodes: codes } = require('@velo-labs/platform')

const db = require('../../db')

const list = async (params) => {
  return db
    .main('fleet_memberships')
    .where({ fleet_id: params.fleet_id, deactivation_date: null })
}

const retrieve = async (
  { fleet_membership_id: fleetMembershipId },
  context = {}
) => {
  const [membership] = await (context.trx
    ? context.trx('fleet_memberships')
    : db.main('fleet_memberships')
  )
    .where({ fleet_membership_id: fleetMembershipId })
    .select('*')

  if (!membership) {
    throw errors.customError(
      `Membership id ${fleetMembershipId} not found.`,
      codes.ResourceNotFound,
      'NotFound'
    )
  }

  return membership
}

const calculateTaxes = async (fleetId, amount, discount) => {
  if (discount === 100) return { taxes: null, taxTotal: 0 }
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
  return { taxes, taxTotal: Number(taxTotal.toFixed(2)) }
}

/**
 * Create a fleet membership.{"tax_id": [{"amount": 1.5, "tax_id": 7}, {"amount": 1, "tax_id": 11}], "sub_total": 2.5}
 */
const create = async (params, context = {}) => {
  const trx = context.trx || (await db.main.transaction())
  // params = await taxInfo(params)
  const memberships = await trx('fleet_memberships')
    .where({ fleet_id: params.fleet_id, deactivation_date: null })
    .limit(3)
    .select('fleet_membership_id')

  const taxInfo = await calculateTaxes(params.fleet_id, params.membership_price, params.membership_incentive)

  const membershipData = params

  if (taxInfo.taxTotal) membershipData.tax_sub_total = taxInfo.taxTotal
  if (taxInfo.taxes) membershipData.taxes = JSON.stringify(taxInfo.taxes)
  if (memberships.length === 3) {
    throw errors.customError(
      `Fleet cannot have more than 3 active memberships.`,
      codes.Conflict,
      'Conflict'
    )
  }

  const [membershipId] = await trx('fleet_memberships').insert(membershipData)

  if (!context.trx) {
    await trx.commit()
  }

  return retrieve(
    {
      fleet_membership_id: membershipId
    },
    context
  )
}

/**
 * Update a parameter in an active fleet membership.
 *
 * Updating any field in a fleet membership creates another record of the fleet
 * membership, with the updated field(s).
 *
 * This is because memberships may already have member subscriptions and they
 * should not change from beneath the subscribers.
 *
 * Inactive fleet memberships cannot be (and should not be) updated using this handler.
 */
const update = async ({ fleet_membership_id: fleetMembershipId }, params) => {
  const [membership] = await db
    .main('fleet_memberships')
    .where({
      'fleet_memberships.fleet_membership_id': fleetMembershipId,
      'fleet_memberships.deactivation_date': null
    })
    .select()

  if (!membership) {
    throw errors.customError(
      `Membership id ${fleetMembershipId} not found or is inactive.`,
      codes.ResourceNotFound,
      'NotFound'
    )
  }

  const taxInfo = await calculateTaxes(membership.fleet_id, params.membership_price, params.membership_incentive)

  const current = _.omit(membership, 'fleet_membership_id', 'created_at')

  return db.main.transaction(async (trx) => {
    await trx('fleet_memberships')
      .where('fleet_membership_id', fleetMembershipId)
      .update({
        deactivation_date: new Date(),
        deactivation_reason: 'supersession'
      })

    const membership = { ...current, ...params }
    if (taxInfo.taxTotal) membership.tax_sub_total = taxInfo.taxTotal
    if (taxInfo.taxes) membership.taxes = JSON.stringify(taxInfo.taxes)
    const updated = await create(membership, { trx })
    return updated
  })
}

const activate = async ({ fleet_membership_id: fleetMembershipId }) => {
  const [membership] = await db
    .main('fleet_memberships')
    .where('fleet_membership_id', fleetMembershipId)
    .andWhere(function () {
      this.whereNotNull('deactivation_date')
    })
    .select()

  if (!membership) {
    throw errors.customError(
      `Membership id ${fleetMembershipId} cannot be activated. ` +
        `This can be because it does not exist or has been superseded by another membership on this fleet.`,
      codes.Conflict,
      'Conflict'
    )
  }

  const activeMemberships = await list({ fleet_id: membership.fleet_id })

  if (activeMemberships.length >= 3) {
    throw errors.customError(
      `Fleet cannot have more than 3 active memberships.`,
      codes.Conflict,
      'Conflict'
    )
  }

  await db
    .main('fleet_memberships')
    .where('fleet_membership_id', fleetMembershipId)
    .update({
      deactivation_date: null,
      deactivation_reason: null
    })

  return {
    ...membership,
    deactivation_date: null,
    deactivation_reason: null
  }
}

const deactivate = async ({ fleet_membership_id: fleetMembershipId }) => {
  await db
    .main('fleet_memberships')
    .where({
      fleet_membership_id: fleetMembershipId,
      deactivation_date: null
    })
    .update({
      deactivation_date: new Date(),
      deactivation_reason: 'deactivation'
    })

  return retrieve({ fleet_membership_id: fleetMembershipId })
}

const subscriptions = async ({ fleet_id: fleetId, user_ids: userIds }) => {
  const results = await db
    .main('membership_subscriptions')
    .whereIn('membership_subscriptions.user_id', userIds)
    .join('fleet_memberships', {
      'fleet_memberships.fleet_id': fleetId,
      'fleet_memberships.fleet_membership_id':
        'membership_subscriptions.fleet_membership_id'
    })
    .join('fleets', {'fleets.fleet_id': 'fleet_memberships.fleet_id'})
    .options({ nestTables: true })
    .select()
    .groupBy('membership_subscriptions.membership_subscription_id')

  return results.map(
    ({ membership_subscriptions: sub, fleet_memberships: membership }) => {
      sub.fleet_membership = membership

      return sub
    }
  )
}

module.exports = {
  create,
  update,
  list,
  retrieve,
  activate,
  deactivate,
  subscriptions
}
