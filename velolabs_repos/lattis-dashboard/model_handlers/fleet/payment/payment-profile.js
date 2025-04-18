const moment = require('moment')
const { logger } = require('../../../utils/error-handler')
const { errors } = require('../../../utils/response-utils')
const DatabaseUtils = require('../../../utils/db-utils')
const fleetConstants = require('../../../constants/fleet-constants')
const stripe = require('stripe')(process.env.STRIPE_API_KEY)

/**
 * Fleet payment profile operations
 */
class PaymentProfile {
  /**
   * Create or update payment profile
   * @param {Object} params - Payment parameters
   * @returns {Promise<void>}
   */
  static async createOrUpdate(params) {
    try {
      const fleet = await this.getFleet(params.fleet_id)
      const customer = await this.createStripeCustomer(fleet)
      const source = await this.addPaymentSource(customer.id, params, fleet)
      const existingProfile = await this.getExistingProfile(params)

      if (existingProfile) {
        await this.updateProfile(existingProfile, customer, source, params)
      } else {
        await this.createProfile(customer, source, params)
      }
    } catch (error) {
      this.handleError(error)
    }
  }

  /**
   * Get fleet details
   * @param {number} fleetId - Fleet ID
   * @returns {Promise<Object>}
   */
  static async getFleet(fleetId) {
    const fleet = await DatabaseUtils.selectWithAnd(
      fleetConstants.tables.fleets,
      null,
      { fleet_id: fleetId }
    ).first()

    if (!fleet) {
      throw errors.notFound('Fleet not found')
    }

    return fleet
  }

  /**
   * Create Stripe customer
   * @param {Object} fleet - Fleet details
   * @returns {Promise<Object>}
   */
  static async createStripeCustomer(fleet) {
    return await stripe.customers.create({
      email: fleet.contact_email
    })
  }

  /**
   * Add payment source to Stripe customer
   * @param {string} customerId - Stripe customer ID
   * @param {Object} params - Payment parameters
   * @param {Object} fleet - Fleet details
   * @returns {Promise<Object>}
   */
  static async addPaymentSource(customerId, params, fleet) {
    return await stripe.customers.createSource(customerId, {
      source: {
        object: 'card',
        exp_month: params.exp_month,
        exp_year: params.exp_year,
        number: params.cc_no,
        cvc: params.cvc,
        name: fleet.fleet_name
      }
    })
  }

  /**
   * Get existing payment profile
   * @param {Object} params - Payment parameters
   * @returns {Promise<Object>}
   */
  static async getExistingProfile(params) {
    return await DatabaseUtils.selectWithAnd(
      'operator_payment_profiles',
      null,
      {
        fleet_id: params.fleet_id,
        operator_id: params.operator_id
      }
    ).first()
  }

  /**
   * Update existing payment profile
   * @param {Object} existingProfile - Existing profile
   * @param {Object} customer - Stripe customer
   * @param {Object} source - Stripe source
   * @param {Object} params - Payment parameters
   * @returns {Promise<void>}
   */
  static async updateProfile(existingProfile, customer, source, params) {
    if (source.fingerprint !== existingProfile.fingerprint) {
      await stripe.customers.deleteCard(
        existingProfile.stripe_profile_id,
        existingProfile.card_id
      )
    }

    await DatabaseUtils.update(
      'operator_payment_profiles',
      this.getProfileData(customer, source, params),
      {
        fleet_id: params.fleet_id,
        operator_id: params.operator_id
      }
    )
  }

  /**
   * Create new payment profile
   * @param {Object} customer - Stripe customer
   * @param {Object} source - Stripe source
   * @param {Object} params - Payment parameters
   * @returns {Promise<void>}
   */
  static async createProfile(customer, source, params) {
    await DatabaseUtils.insert(
      'operator_payment_profiles',
      {
        ...this.getProfileData(customer, source, params),
        created_date: moment().unix()
      }
    )
  }

  /**
   * Get profile data for create/update
   * @param {Object} customer - Stripe customer
   * @param {Object} source - Stripe source
   * @param {Object} params - Payment parameters
   * @returns {Object}
   */
  static getProfileData(customer, source, params) {
    return {
      operator_id: params.operator_id,
      fleet_id: params.fleet_id,
      stripe_profile_id: customer.id,
      cc_no: this.maskCreditCard(params.cc_no),
      exp_month: params.exp_month,
      exp_year: params.exp_year,
      type_card: fleetConstants.cardinfo.type_card,
      card_id: source.id,
      fingerprint: source.fingerprint,
      cc_type: source.brand
    }
  }

  /**
   * Handle errors
   * @param {Error} error - Error object
   */
  static handleError(error) {
    if (error.type === 'StripeCardError') {
      throw errors.validationError('Invalid credit card')
    }
    logger.error('Error managing payment profile:', error)
    throw error
  }

  /**
   * Mask credit card number
   * @param {string} cardNumber - Credit card number
   * @returns {string}
   */
  static maskCreditCard(cardNumber) {
    return cardNumber.replace(/.(?=.{4})/g, 'x')
  }
}

module.exports = PaymentProfile
