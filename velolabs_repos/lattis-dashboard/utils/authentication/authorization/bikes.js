const db = require('../../../db')
const { hasFleetAccess } = require('./fleets')

/**
 * @param {import('../middleware').AuthContext} authContext
 * @param {number} bikeId
 */
const hasBikeAccess = async (authContext, bikeId) => {
  const bike = await db
    .main('bikes')
    .where({ bike_id: bikeId })
    .select('bike_id', 'fleet_id')
    .first()

  return hasFleetAccess(authContext, (bike || {}).fleet_id)
}

module.exports = {
  hasBikeAccess
}
