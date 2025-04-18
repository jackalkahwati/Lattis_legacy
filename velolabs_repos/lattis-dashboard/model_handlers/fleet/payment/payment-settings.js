const { logger } = require('../../../utils/error-handler')
const { errors } = require('../../../utils/response-utils')
const DatabaseUtils = require('../../../utils/db-utils')
const fleetConstants = require('../../../constants/fleet-constants')

/**
 * Fleet payment settings operations
 */
class PaymentSettings {
  /**
   * Update fleet billing cycle
   * @param {Object} params - Billing parameters
   * @returns {Promise<void>}
   */
  static async updateBillingCycle(params) {
    try {
      if (
        params.enable_preauth &&
        (!params.preauth_amount || Number(params.preauth_amount) === 0)
      ) {
        throw errors.validationError('Pre-authorization amount is required when enabled')
      }

      await DatabaseUtils.update(
        fleetConstants.tables.fleet_payment_settings,
        {
          price_for_membership: params.price_for_membership,
          price_type_value: params.price_type_value,
          price_type: params.price_type,
          usage_surcharge: params.usage_surcharge,
          excess_usage_fees: params.excess_usage_fees,
          excess_usage_type_value: params.excess_usage_type_value,
          excess_usage_type: params.excess_usage_type,
          excess_usage_type_after_value: params.excess_usage_type_after_value,
          excess_usage_type_after_type: params.excess_usage_type_after_type,
          ride_deposit: params.ride_deposit,
          price_for_ride_deposit_type: params.price_for_ride_deposit_type,
          refund_criteria: params.refund_criteria,
          refund_criteria_value: params.refund_criteria_value,
          price_for_penalty_outside_parking: params.price_for_penalty_outside_parking,
          price_for_penalty_outside_parking_below_battery_charge: params.price_for_penalty_outside_parking_below_battery_charge,
          price_for_ride_deposit: params.price_for_ride_deposit,
          price_for_forget_plugin: params.price_for_forget_plugin,
          price_for_bike_unlock: params.price_for_bike_unlock,
          price_for_reservation_late_return: params.price_for_reservation_late_return,
          enable_preauth: params.enable_preauth,
          preauth_amount: params.preauth_amount || 0
        },
        { fleet_id: params.fleet_id }
      )
    } catch (error) {
      logger.error('Error updating billing cycle:', error)
      throw error
    }
  }

  /**
   * Get fleet payment settings
   * @param {Object} where - Query conditions
   * @returns {Promise<Array>}
   */
  static async getSettings(where) {
    try {
      return await DatabaseUtils.selectWithAnd(
        fleetConstants.tables.fleet_payment_settings,
        null,
        where
      )
    } catch (error) {
      logger.error('Error getting payment settings:', error)
      throw error
    }
  }

  /**
   * Update fleet payment settings
   * @param {Object} data - Payment settings
   * @param {Object} where - Query conditions
   * @returns {Promise<void>}
   */
  static async updateSettings(data, where) {
    try {
      await DatabaseUtils.update(
        fleetConstants.tables.fleet_payment_settings,
        data,
        where
      )
    } catch (error) {
      logger.error('Error updating payment settings:', error)
      throw error
    }
  }
}

module.exports = PaymentSettings
