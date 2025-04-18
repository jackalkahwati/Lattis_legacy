const moment = require('moment')
const { logger } = require('../../../utils/error-handler')
const DatabaseUtils = require('../../../utils/db-utils')

/**
 * Core domain access operations
 */
class DomainAccess {
  /**
   * Grant access to user
   * @param {Object} user - User data
   * @param {number} fleetId - Fleet ID
   * @returns {Promise<void>}
   */
  static async grantAccess(user, fleetId) {
    try {
      await DatabaseUtils.insert(
        'private_fleet_users',
        {
          email: user.email,
          user_id: user.user_id,
          fleet_id: fleetId,
          verified: 0,
          access: 1,
          access_mode: 'domain',
          updated_at: moment().unix()
        }
      )
    } catch (error) {
      logger.error('Error granting user access:', error)
      throw error
    }
  }

  /**
   * Update user access
   * @param {string} email - User email
   * @param {Object} update - Update data
   * @returns {Promise<void>}
   */
  static async updateAccess(email, update) {
    try {
      await DatabaseUtils.update(
        'private_fleet_users',
        {
          ...update,
          updated_at: moment().unix()
        },
        { email }
      )
    } catch (error) {
      logger.error('Error updating user access:', error)
      throw error
    }
  }

  /**
   * Revoke user access
   * @param {string} email - User email
   * @param {number} fleetId - Fleet ID
   * @returns {Promise<void>}
   */
  static async revokeAccess(email, fleetId) {
    try {
      await DatabaseUtils.update(
        'private_fleet_users',
        {
          access: 0,
          verified: 0,
          updated_at: moment().unix()
        },
        {
          email,
          fleet_id: fleetId
        }
      )
    } catch (error) {
      logger.error('Error revoking user access:', error)
      throw error
    }
  }
}

module.exports = DomainAccess
