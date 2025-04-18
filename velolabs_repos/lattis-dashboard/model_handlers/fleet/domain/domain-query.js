const { logger } = require('../../../utils/error-handler')
const DatabaseUtils = require('../../../utils/db-utils')
const fleetConstants = require('../../../constants/fleet-constants')

/**
 * Fleet domain query operations
 */
class DomainQuery {
  /**
   * Get domains
   * @param {Object} params - Query parameters
   * @returns {Promise<Array>}
   */
  static async getDomains(params) {
    try {
      const where = {}
      if (params.fleet_id) where.fleet_id = params.fleet_id
      if (params.operator_id) where.operator_id = params.operator_id
      if (params.customer_id) where.customer_id = params.customer_id
      if (params.domain_name) where.domain_name = params.domain_name
      if (params.domain_id) where.domain_id = params.domain_id

      return await DatabaseUtils.selectWithAnd(
        fleetConstants.tables.domains,
        null,
        where
      )
    } catch (error) {
      logger.error('Error getting domains:', error)
      throw error
    }
  }

  /**
   * Get users by domain
   * @param {string} domain - Domain name
   * @returns {Promise<Array>}
   */
  static async getUsersByDomain(domain) {
    try {
      return await DatabaseUtils.selectWithAnd(
        'users',
        null,
        {
          user_type: 'lattis',
          email: DatabaseUtils.raw('LIKE ?', [`%${domain}`])
        }
      )
    } catch (error) {
      logger.error('Error getting users by domain:', error)
      throw error
    }
  }

  /**
   * Get fleet users by domain
   * @param {number} fleetId - Fleet ID
   * @param {string} domain - Domain name
   * @returns {Promise<Array>}
   */
  static async getFleetUsersByDomain(fleetId, domain) {
    try {
      return await DatabaseUtils.selectWithAnd(
        'private_fleet_users',
        null,
        {
          fleet_id: fleetId,
          email: DatabaseUtils.raw('LIKE ?', [`%${domain}`])
        }
      )
    } catch (error) {
      logger.error('Error getting fleet users by domain:', error)
      throw error
    }
  }

  /**
   * Get active fleet users by domain
   * @param {number} fleetId - Fleet ID
   * @param {string} domain - Domain name
   * @returns {Promise<Array>}
   */
  static async getActiveFleetUsersByDomain(fleetId, domain) {
    try {
      return await DatabaseUtils.selectWithAnd(
        'private_fleet_users',
        null,
        {
          fleet_id: fleetId,
          access: 1,
          email: DatabaseUtils.raw('LIKE ?', [`%${domain}`])
        }
      )
    } catch (error) {
      logger.error('Error getting active fleet users by domain:', error)
      throw error
    }
  }
}

module.exports = DomainQuery
