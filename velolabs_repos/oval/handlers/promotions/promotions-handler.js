const {
  logger,
  errors,
  dbConstants,
  responseCodes: codes
} = require('@velo-labs/platform')
const fleetConstants = require('../../constants/fleet-constants')

const db = require('../../db')
const { castDataTypes } = require('../../helpers/db')
const conversions = require('../../utils/conversions')

async function getAccessibleFleets (userId) {
  const accessiblePrivateFleets = await db
    .users(dbConstants.tables.private_fleet_users)
    .where({ user_id: userId, access: 1, verified: 1 })
    .select('fleet_id')

  const publicFleets = await db
    .main('fleets')
    .whereIn('fleets.type', [
      fleetConstants.fleet_type.publicWithPayment,
      fleetConstants.fleet_type.publicWithNoPayment
    ])
    .select('fleet_id')

  return new Set(
    [...accessiblePrivateFleets, ...publicFleets].map(
      ({ fleet_id: fleetId }) => fleetId
    )
  )
}

function formatPromotion ({ promotion, promotion_users: pUser, fleets: fleet }) {
  fleet.skip_parking_image = !!fleet.skip_parking_image
  fleet.parking_area_restriction = !!fleet.parking_area_restriction

  promotion.promotion_users = pUser
  promotion.fleet = fleet

  return promotion
}

async function byPromoCode ({ promoCode, user }) {
  const promotion = await db
    .main('promotion')
    .where({
      'promotion.promotion_code': promoCode,
      'promotion.deactivated_at': null
    })
    .leftOuterJoin('promotion_users', function () {
      this.on(
        'promotion_users.promotion_id',
        '=',
        'promotion.promotion_id'
      ).andOn('promotion_users.user_id', '=', db.main.raw('?', [user.user_id]))
    })
    .innerJoin('fleets', 'fleets.fleet_id', 'promotion.fleet_id')
    .options({ nestTables: true, typeCast: castDataTypes })
    .first()
    .select()

  if (!promotion) {
    return null
  }

  if (!promotion.promotion_users.promotion_users_id) {
    promotion.promotion_users = undefined
  }

  return formatPromotion(promotion)
}

async function redeem ({ promoCode, user }) {
  const promotion = await byPromoCode({
    promoCode,
    user
  })

  if (!promotion) {
    throw errors.customError(
      'Promo code not found',
      codes.ResourceNotFound,
      'NotFound'
    )
  }

  if (promotion.promotion_users) {
    // Already redeemed.
    throw errors.customError(
      'This promo code has already been redeemed for this account',
      codes.BadRequest,
      'BadRequest'
    )
  }

  const accessibleFleets = await getAccessibleFleets(user.user_id)

  if (!accessibleFleets.has(promotion.fleet_id)) {
    // User cannot access this fleet; shouldn't redeem codes in said fleet.
    logger(`Cannot redeem; user cannot access this fleet`, {
      user: user.user_id,
      fleet: promotion.fleet_id
    })

    throw errors.customError(
      'Promo code not found',
      codes.ResourceNotFound,
      'NotFound'
    )
  }

  if (promotion.usage === 'single') {
    const redemptions = await db
      .main('promotion_users')
      .where({
        promotion_id: promotion.promotion_id
      })
      .select('promotion_users_id')

    if (redemptions.length) {
      // Only a single user can use this promo code and they have already
      // redeemed it.
      logger(`Promotion already redeemed`, {
        promotion: promotion.promotion_id,
        redemptions_count: redemptions.length
      })

      throw errors.customError(
        'Invalid promo code',
        codes.BadRequest,
        'BadRequest'
      )
    }
  }

  await db.main('promotion_users').insert({
    promotion_id: promotion.promotion_id,
    user_id: user.user_id
  })

  return byPromoCode({ promoCode, user })
}

async function list ({ user, fleetId, includeClaimed }) {
  const filters = { user_id: user.user_id }

  if (!includeClaimed) {
    filters.claimed_at = null
  }

  const promotions = await db
    .main('promotion_users')
    .where(filters)
    .innerJoin('promotion', function () {
      if (fleetId) {
        this.on(
          'promotion.promotion_id',
          '=',
          'promotion_users.promotion_id'
        ).andOn('promotion.fleet_id', '=', db.main.raw('?', [fleetId]))
      } else {
        this.on('promotion.promotion_id', '=', 'promotion_users.promotion_id')
      }
    })
    .innerJoin('fleets', 'fleets.fleet_id', 'promotion.fleet_id')
    .options({ nestTables: true, typeCast: castDataTypes })
    .groupBy('promotion.promotion_id')
    .select()

  if (promotions.length) {
    return promotions.map(formatPromotion)
  }

  return []
}

async function applyPromotionDiscount ({ userId, fleetId, amount }) {
  // Nothing to apply discount to.
  if (amount === 0) {
    return { discount: 0, discountedAmount: amount }
  }

  const promoDiscounts = await list({
    user: { user_id: userId },
    fleetId,
    includeClaimed: false
  })

  if (!promoDiscounts.length) {
    return { discount: 0, discountedAmount: amount }
  }

  const promotion = promoDiscounts[0]

  logger('Applying promotion discount:', {
    fleetId,
    userId,
    amount,
    promotion: {
      promotion_id: promotion.promotion_id,
      amount: promotion.amount,
      usage: promotion.usage,
      promotion_users: promotion.promotion_users.promotion_users_id
    }
  })

  const incentive = Number(promotion.amount)

  // Free when discount is 100%
  if (incentive === 100) {
    return {
      promotion,
      discount: amount,
      discountedAmount: 0
    }
  }

  const discount = conversions.roundOffPricing((amount * incentive) / 100)

  return {
    promotion,
    discount,
    discountedAmount: conversions.roundOffPricing(amount - discount)
  }
}

module.exports = {
  byPromoCode,
  redeem,
  list,
  applyPromotionDiscount
}
