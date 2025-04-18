const { logger } = require('../../../utils/error-handler')
const DomainAccess = require('./domain-access')
const DomainQuery = require('./domain-query')

/**
 * User access grant operations
 */
class UserAccessGrant {
  /**
   * Process user access for domains
   * @param {number} fleetId - Fleet ID
   * @param {Array<string>} domains - Domain names
   * @returns {Promise<void>}
   */
  static async processUserAccess(fleetId, domains) {
    try {
      for (const domain of domains) {
        await this.processUsersForDomain(fleetId, domain)
      }
    } catch (error) {
      logger.error('Error processing user access:', error)
      throw error
    }
  }

  /**
   * Process users for a single domain
   * @param {number} fleetId - Fleet ID
   * @param {string} domain - Domain name
   * @returns {Promise<void>}
   */
  static async processUsersForDomain(fleetId, domain) {
    try {
      const users = await DomainQuery.getUsersByDomain(domain)
      const existingUsers = await DomainQuery.getFleetUsersByDomain(fleetId, domain)

      await this.updateExistingUsers(existingUsers)
      await this.addNewUsers(fleetId, users, existingUsers)
    } catch (error) {
      logger.error('Error processing users for domain:', error)
      throw error
    }
  }

  /**
   * Update existing users
   * @param {Array<Object>} existingUsers - Existing users
   * @returns {Promise<void>}
   */
  static async updateExistingUsers(existingUsers) {
    for (const user of existingUsers) {
      await DomainAccess.updateAccess(user.email, {
        access: 1,
        access_mode: 'domain'
      })
    }
  }

  /**
   * Add new users
   * @param {number} fleetId - Fleet ID
   * @param {Array<Object>} users - All users
   * @param {Array<Object>} existingUsers - Existing users
   * @returns {Promise<void>}
   */
  static async addNewUsers(fleetId, users, existingUsers) {
    const existingEmails = existingUsers.map(user => user.email)
    const newUsers = users.filter(user => !existingEmails.includes(user.email))

    for (const user of newUsers) {
      await DomainAccess.grantAccess(user, fleetId)
    }
  }
}

module.exports = UserAccessGrant
