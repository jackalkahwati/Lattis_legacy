const _ = require('underscore')
const moment = require('moment')
const { logger } = require('../../utils/error-handler')
const { errors } = require('../../utils/response-utils')
const DatabaseUtils = require('../../utils/db-utils')
const FileUpload = require('../../utils/file-upload')
const fleetConstants = require('../../constants/fleet-constants')
const operatorHandler = require('../operator-handler')

/**
 * Core fleet operations
 */
class FleetCore {
  /**
   * Validate new fleet data
   * @param {Object} fleetData - Fleet data
   * @param {Object} imageData - Fleet image data
   * @param {string} type - Validation type
   * @returns {boolean}
   */
  static validateNewFleet(fleetData, imageData, type) {
    const baseValidation = (
      (fleetData.operator_id || fleetData.customer_id || fleetData.fleet_id) &&
      fleetData.fleet_name &&
      fleetData.email &&
      fleetData.first_name &&
      fleetData.last_name &&
      fleetData.country_code &&
      fleetData.fleet_timezone
    )

    if (!baseValidation) return false

    switch (type) {
      case 'existing_customer':
        return fleetData.image_url && fleetData.city && fleetData.type
      case 'new_customer':
        return (imageData || fleetData.image_url) && fleetData.city && fleetData.type
      case 'edit_fleet_info':
        return fleetData.customer_name && fleetData.phone_number
      default:
        return false
    }
  }

  /**
   * Get fleet list
   * @param {Object} params - Query parameters
   * @returns {Promise<Array>}
   */
  static async getFleetList(params) {
    try {
      // Get base fleet data
      const fleets = await this.getFleetsWithCustomers(params)
      if (!fleets || fleets.length === 0) {
        return fleets
      }

      const fleetIds = _.uniq(_.pluck(fleets, 'fleet_id'))

      // Get fleet metadata
      const metadata = await DatabaseUtils.selectWithAnd(
        fleetConstants.tables.fleet_metadata,
        null,
        { fleet_id: fleetIds }
      )

      // Get fleet payment settings
      const payments = await DatabaseUtils.selectWithAnd(
        fleetConstants.tables.fleet_payment_settings,
        null,
        { fleet_id: fleetIds }
      )

      // Get addresses
      const addresses = await DatabaseUtils.selectWithAnd(
        fleetConstants.tables.addresses,
        null
      )

      // Combine all data
      return fleets.map(fleet => {
        const fleetMetadata = _.findWhere(metadata, { fleet_id: fleet.fleet_id }) || {}
        const fleetPayment = _.findWhere(payments, { fleet_id: fleet.fleet_id }) || {}
        const address = _.findWhere(addresses, { 
          type: 'fleet',
          type_id: fleet.fleet_id
        }) || {}

        return {
          ...fleet,
          ...fleetMetadata,
          ..._.pick(address, 'state', 'country', 'postal_code', 'city'),
          ..._.pick(fleetPayment, 'currency')
        }
      })
    } catch (error) {
      logger.error('Error getting fleet list:', error)
      throw error
    }
  }

  /**
   * Get fleets with customers
   * @param {Object} params - Query parameters
   * @returns {Promise<Array>}
   */
  static async getFleetsWithCustomers(params) {
    try {
      const operator = await operatorHandler.getOperator({ operator_id: params.operator_id })
      
      if (!operator) {
        throw errors.notFound('Operator not found')
      }

      const isSuperUser = operator.acl === fleetConstants.acl.superAdmin || 
                         operator.acl === fleetConstants.acl.supportAdmin

      if (isSuperUser) {
        return await this.getAllFleetsWithCustomers()
      }

      const fleetAssociations = await DatabaseUtils.selectWithAnd(
        fleetConstants.tables.fleet_associations,
        null,
        { operator_id: params.operator_id }
      )

      if (fleetAssociations.length === 0) {
        return []
      }

      const fleetIds = _.uniq(_.pluck(fleetAssociations, 'fleet_id'))
      const fleets = await this.getFleets({ fleet_id: fleetIds })

      if (fleets.length === 0) {
        return []
      }

      const customerIds = _.uniq(_.pluck(fleets, 'customer_id'))
      const customers = await DatabaseUtils.selectWithAnd(
        'customers',
        null,
        { customer_id: customerIds }
      )

      return fleets.map(fleet => {
        const customer = _.findWhere(customers, { customer_id: fleet.customer_id })
        return {
          ...fleet,
          contact_country_code: fleet.country_code,
          ...(customer || {})
        }
      })
    } catch (error) {
      logger.error('Error getting fleets with customers:', error)
      throw error
    }
  }

  /**
   * Get all fleets with customers
   * @returns {Promise<Array>}
   */
  static async getAllFleetsWithCustomers() {
    try {
      return await DatabaseUtils.join(
        fleetConstants.tables.fleets,
        'customers',
        null,
        'customer_id',
        'customer_id'
      )
    } catch (error) {
      logger.error('Error getting all fleets with customers:', error)
      throw error
    }
  }

  /**
   * Get fleets by criteria
   * @param {Object} where - Where conditions
   * @returns {Promise<Array>}
   */
  static async getFleets(where) {
    try {
      const fleets = await DatabaseUtils.selectWithAnd(
        fleetConstants.tables.fleets,
        null,
        where
      )
      return this.formatFleetsForWire(fleets)
    } catch (error) {
      logger.error('Error getting fleets:', error)
      throw error
    }
  }

  /**
   * Format fleet data for response
   * @param {Array|Object} fleets - Fleet data
   * @returns {Array|Object}
   */
  static formatFleetsForWire(fleets) {
    if (!Array.isArray(fleets)) {
      fleets = [fleets]
    }
    return fleets.map(fleet => _.omit(fleet, 'key'))
  }

  /**
   * Update fleet logo
   * @param {Object} fleetDetails - Fleet details
   * @param {Object} imageDetails - Image details
   * @returns {Promise<Object>}
   */
  static async updateFleetLogo(fleetDetails, imageDetails) {
    try {
      const result = await FileUpload.uploadImageFile(
        imageDetails,
        `fleet-logo-${fleetDetails.fleetId}-${moment().unix()}`,
        process.env.AWS_S3_FLEET_LOGOS_BUCKET,
        imageDetails.mimetype
      )

      await DatabaseUtils.update(
        fleetConstants.tables.fleets,
        { logo: result.link },
        { fleet_id: fleetDetails.fleetId }
      )

      return { link: result.link }
    } catch (error) {
      logger.error('Error updating fleet logo:', error)
      throw error
    }
  }
}

module.exports = FleetCore
