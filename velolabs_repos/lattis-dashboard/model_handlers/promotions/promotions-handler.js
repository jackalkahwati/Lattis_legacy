const util = require('util')
const crypto = require('crypto')
const { errors, responseCodes: codes } = require('@velo-labs/platform')

const db = require('../../db')

const randomBytes = util.promisify(crypto.randomBytes)

const generatePromoCode = async () => {
  const token = (await randomBytes(5)).toString('hex')

  const code = [token.slice(0, 2), token.slice(2, 6), token.slice(6)]
    .map((s) => s.toUpperCase())
    .join('-')

  return code
}

async function retrieve ({ promotionId }) {
  return db
    .main('promotion')
    .where({ promotion_id: promotionId })
    .first()
    .select()
}

async function create (
  { fleet_id: fleetId, amount, usage, users },
  context = {}
) {
  const trx = context.trx || (await db.main.transaction())

  const activePromos = await trx('promotion')
    .where({ fleet_id: fleetId, deactivated_at: null })
    .limit(5)
    .select('promotion_id')

  if (activePromos.length === 15) {
    throw errors.customError(
      'Fleet cannot have more than 15 active promotions.',
      codes.Conflict,
      'Conflict'
    )
  }

  const promoCode = await generatePromoCode()

  const [promotionId] = await trx('promotion').insert({
    fleet_id: fleetId,
    usage,
    amount,
    promotion_code: promoCode
  })

  if (users && users.length) {
    const promoCodeUsers = users.map((user) => ({
      user_id: user,
      promotion_id: promotionId
    }))

    await trx('promotion_users').insert(promoCodeUsers)
  }

  if (!context.trx) {
    await trx.commit()
  }

  return retrieve({ promotionId })
}

async function deactivate ({ promotion_id: promotionId }) {
  await db
    .main('promotion')
    .where({
      promotion_id: promotionId,
      deactivated_at: null
    })
    .update({ deactivated_at: new Date() })

  const promotion = await retrieve({ promotionId })

  if (promotion.usage === 'multiple_unlimited') {
    // When an unlimited promo code is deactivated, delete all unused redemptions
    await db
      .main('promotion_users')
      .where({ promotion_id: promotion.promotion_id, claimed_at: null })
      .del()
  }

  return promotion
}

async function list ({ fleet_id: fleetId }) {
  return db
    .main('promotion')
    .where({ fleet_id: fleetId, deactivated_at: null })
    .select()
}

function formatPromotion ({ promotion_users: user, fleets: fleet, promotion }) {
  promotion.promotion_users = user
  promotion.fleet = fleet

  return promotion
}

async function userPromotions ({ userId, fleetId }) {
  const promotions = await db
    .main('promotion_users')
    .where({ 'promotion_users.user_id': userId })
    .innerJoin('fleets', 'fleets.fleet_id', db.main.raw('?', [fleetId]))
    .innerJoin('promotion', function () {
      this.on(
        'promotion.promotion_id',
        '=',
        'promotion_users.promotion_id'
      ).andOn('promotion.fleet_id', '=', 'fleets.fleet_id')
    })
    .options({ nestTables: true })
    .select()

  return promotions.map(formatPromotion)
}

module.exports = {
  create,
  deactivate,
  list,
  userPromotions
}
