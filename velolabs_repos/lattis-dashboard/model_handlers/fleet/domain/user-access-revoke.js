const { logger } = require('../../../utils/error-handler')
const DomainAccess = require('./domain-access')
const DomainQuery = require('./domain-query')

/**
 * User access revocation operations
 */
class UserAccessRevoke {
  /**
   * Revoke user access for domains
   * @param {number} fleetId - Fleet ID
   * @param {Array<Object>} domains - Domains
   * @returns {Promise<void>}
   */
  static async revokeUserAccess(fleetId, domains) {
    try {
      for (const domain of domains) {
        await this.revokeUsersForDomain(fleetId, domain.domain_name)
      }
    } catch (error) {
      logger.error('Error revoking user access:', error)
      throw error
    }
  }

  /**
   * Revoke users for a single domain
   * @param {number} fleetId - Fleet ID
   * @param {string} domain - Domain name
   * @returns {Promise<void>}
   */
  static async revokeUsersForDomain(fleetId, domain) {
    try {
      const users = await DomainQuery.getActiveFleetUsersByDomain(fleetId, domain)
      await this.revokeUsersAccess(users, fleetId)
    } catch (error) {
      logger.error('Error revoking users for domain:', error)
      throw error
    }
  }

  /**
   * Revoke access for multiple users
   * @param {Array<Object>} users - Users
   * @param {number} fleetId - Fleet ID
   * @returns {Promise<void>}
   */
  static async revokeUsersAccess(users, fleetId) {
    for (const user of users) {
      await DomainAccess.revokeAccess(user.email, fleetId)
    }
  }
}

module.exports = UserAccessRevoke
