const DomainManager = require('./domain-manager')
const DomainOperations = require('./domain-operations')
const DomainQuery = require('./domain-query')
const DomainAccess = require('./domain-access')
const UserAccessGrant = require('./user-access-grant')
const UserAccessRevoke = require('./user-access-revoke')

/**
 * Fleet domain operations
 */
class FleetDomain {
  /**
   * Save domain details
   * @param {Object} params - Domain parameters
   * @returns {Promise<void>}
   */
  static async saveDomain(params) {
    const domains = await DomainManager.saveDomain(params)
    await UserAccessGrant.processUserAccess(params.fleet_id, domains)
  }

  /**
   * Delete domain
   * @param {Object} params - Domain parameters
   * @returns {Promise<void>}
   */
  static async deleteDomain(params) {
    const domains = await DomainManager.deleteDomain(params)
    if (domains.length > 0) {
      await UserAccessRevoke.revokeUserAccess(params.fleet_id, domains)
    }
  }

  /**
   * Get domains
   * @param {Object} params - Query parameters
   * @returns {Promise<Array>}
   */
  static async getDomains(params) {
    return DomainManager.getDomains(params)
  }
}

module.exports = {
  FleetDomain,
  DomainManager,
  DomainOperations,
  DomainQuery,
  DomainAccess,
  UserAccessGrant,
  UserAccessRevoke
}
