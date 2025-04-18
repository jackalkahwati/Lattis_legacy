const db = require('../../../db')
const { hasFleetAccess } = require('./fleets')

/**
 * @param {import('../middleware').AuthContext} authContext
 * @param {number} tripId
 */
const hasTripAccess = async (authContext, tripId) => {
  const trip = await db
    .main('trips')
    .where({ trip_id: tripId })
    .select('trip_id', 'fleet_id')
    .first()

  return hasFleetAccess(authContext, (trip || {}).fleet_id)
}

module.exports = {
  hasTripAccess
}
