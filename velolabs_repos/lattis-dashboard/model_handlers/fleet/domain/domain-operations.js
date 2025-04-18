const { logger } = require('../../../utils/error-handler')
const { errors } = require('../../../utils/response-utils')
const DatabaseUtils = require('../../../utils/db-utils')
const DomainQuery = require('./domain-query')

/**
 * Core domain operations
 */
class DomainOperations {
  /**
   * Insert domain
   * @param {Object} params - Domain parameters
   * @param {string} domainName - Domain name
   * @returns {Promise<void>}
   */
  static async insertDomain(params, domainName) {
    try {
      await DatabaseUtils.insert(
        'domains',
        {
          fleet_id: params.fleet_id,
          domain_name: domainName,
          operator_id: params.operator_id,
          customer_id: params.customer_id
        }
      )
    } catch (error) {
      logger.error('Error inserting domain:', error)
      throw error
    }
  }

  /**
   * Delete domain
   * @param {string} domainName - Domain name
   * @param {number} fleetId - Fleet ID
   * @returns {Promise<void>}
   */
  static async deleteDomain(domainName, fleetId) {
    try {
      await DatabaseUtils.delete(
        'domains',
        {
          domain_name: domainName,
          fleet_id: fleetId
        }
      )
    } catch (error) {
      logger.error('Error deleting domain:', error)
      throw error
    }
  }

  /**
   * Validate domain
   * @param {Object} params - Domain parameters
   * @returns {Promise<void>}
   */
  static async validateDomain(params) {
    const domains = await DomainQuery.getDomains(params)
    if (domains && domains.length > 0) {
      throw errors.conflict('Domain name already exists for this fleet')
    }
  }

  /**
   * Normalize domain names
   * @param {string|Array<string>} domainName - Domain name(s)
   * @returns {Array<string>}
   */
  static normalizeDomainNames(domainName) {
    return Array.isArray(domainName) ? domainName : [domainName]
  }
}

module.exports = DomainOperations
