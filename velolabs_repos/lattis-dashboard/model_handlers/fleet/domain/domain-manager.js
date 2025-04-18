const { logger } = require('../../../utils/error-handler')
const DomainOperations = require('./domain-operations')
const DomainQuery = require('./domain-query')

/**
 * Domain manager for core domain operations
 */
class DomainManager {
  /**
   * Save domain details
   * @param {Object} params - Domain parameters
   * @returns {Promise<void>}
   */
  static async saveDomain(params) {
    try {
      // Validate domain
      await DomainOperations.validateDomain(params)

      // Get normalized domain names
      const domainNames = DomainOperations.normalizeDomainNames(params.domain_name)

      // Insert domains
      for (const domainName of domainNames) {
        await DomainOperations.insertDomain(params, domainName)
      }

      return domainNames
    } catch (error) {
      logger.error('Error in domain manager - save domain:', error)
      throw error
    }
  }

  /**
   * Delete domain
   * @param {Object} params - Domain parameters
   * @returns {Promise<Array>}
   */
  static async deleteDomain(params) {
    try {
      const domains = await DomainQuery.getDomains(params)
      if (domains.length === 0) {
        return []
      }

      for (const domain of domains) {
        await DomainOperations.deleteDomain(domain.domain_name, params.fleet_id)
      }

      return domains
    } catch (error) {
      logger.error('Error in domain manager - delete domain:', error)
      throw error
    }
  }

  /**
   * Get domains
   * @param {Object} params - Query parameters
   * @returns {Promise<Array>}
   */
  static async getDomains(params) {
    try {
      return await DomainQuery.getDomains(params)
    } catch (error) {
      logger.error('Error in domain manager - get domains:', error)
      throw error
    }
  }
}

module.exports = DomainManager
