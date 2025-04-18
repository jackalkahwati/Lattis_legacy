const PaymentProfile = require('./payment-profile')
const PaymentSettings = require('./payment-settings')

/**
 * Fleet payment operations
 */
class FleetPayment {
  /**
   * Create or update payment profile
   * @param {Object} params - Payment parameters
   * @returns {Promise<void>}
   */
  static async createPaymentProfile(params) {
    return PaymentProfile.createOrUpdate(params)
  }

  /**
   * Update billing cycle
   * @param {Object} params - Billing parameters
   * @returns {Promise<void>}
   */
  static async updateBillingCycle(params) {
    return PaymentSettings.updateBillingCycle(params)
  }

  /**
   * Get payment settings
   * @param {Object} where - Query conditions
   * @returns {Promise<Array>}
   */
  static async getPaymentSettings(where) {
    return PaymentSettings.getSettings(where)
  }

  /**
   * Update payment settings
   * @param {Object} data - Payment settings
   * @param {Object} where - Query conditions
   * @returns {Promise<void>}
   */
  static async updatePaymentSettings(data, where) {
    return PaymentSettings.updateSettings(data, where)
  }
}

module.exports = FleetPayment
