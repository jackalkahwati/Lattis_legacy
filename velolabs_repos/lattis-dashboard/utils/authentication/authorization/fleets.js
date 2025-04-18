/**
 * @param {import("../middleware").AuthContext} authContext
 * @param {number} fleetId
 *
 * @returns {import("./authorize").AuthorizationResult}
 */
const hasFleetAccess = (authContext, fleetId) => {
  const fleetAssociation = authContext.fleetAssociations[fleetId]

  return {
    authorized: !!fleetAssociation
  }
}

module.exports = {
  hasFleetAccess
}
