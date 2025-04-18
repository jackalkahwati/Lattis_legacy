const db = require('../db')
const platform = require('@velo-labs/platform')
const errors = platform.errors
const manageFleetAccess = async (properties) => {
  try {
    const fleet = await db.main('fleets')
      .where({ fleet_id: properties.fleetId })
      .first()
    const isPublic = fleet && ['public', 'public_no_payment'].includes(fleet.type)
    if (isPublic) return { access: true }
    const isPrivateFleet = fleet && ['private', 'private_no_payment'].includes(fleet.type)
    if (isPrivateFleet) {
      const hasPrivateFleetAccess = await db.users('private_fleet_users')
        .where({
          user_id: properties.userId,
          fleet_id: properties.fleetId,
          access: 1,
          verified: 1
        }).first()
      if (hasPrivateFleetAccess) return { access: true }
    }
    return { access: false }
  } catch (error) {
    throw errors.customError(
      error.response.data,
      platform.responseCodes.Unauthorized,
      'User access error',
      false
    )
  }
}

module.exports = { manageFleetAccess }
