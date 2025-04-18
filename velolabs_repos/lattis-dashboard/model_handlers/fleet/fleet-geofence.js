const { logger } = require('../../utils/error-handler')
const { errors } = require('../../utils/response-utils')
const DatabaseUtils = require('../../utils/db-utils')
const { sendCreateGeofenceRequestToGPSService } = require('../../utils/gpsService')

/**
 * Fleet geofence operations
 */
class FleetGeofence {
  /**
   * Create a new geofence
   * @param {Object} geofence - Geofence data
   * @returns {Promise<Object>}
   */
  static async createGeofence(geofence) {
    try {
      // Insert geofence into database
      const [insertedId] = await DatabaseUtils.insert('geofences', {
        ...geofence,
        status: true
      })

      // Send geofence to GPS service
      const geoData = {
        ...geofence,
        geofence_id: insertedId,
        status: true
      }
      await sendCreateGeofenceRequestToGPSService(geoData)

      // Return created geofence
      return await DatabaseUtils.selectWithAnd('geofences', null, {
        geofence_id: insertedId
      }).first()
    } catch (error) {
      logger.error('Error creating geofence:', error)
      throw error
    }
  }

  /**
   * Get fleet geofences
   * @param {number} fleetId - Fleet ID
   * @returns {Promise<Object>}
   */
  static async getGeofences(fleetId) {
    try {
      // Get all geofences for fleet
      const geofences = await DatabaseUtils.selectWithAnd('geofences', null, {
        fleet_id: fleetId
      })

      // Get geofence enabled status
      const enabled = await DatabaseUtils.selectWithAnd(
        'fleet_metadata',
        ['fleet_id', 'geofence_enabled'],
        { fleet_id: fleetId }
      ).first()

      return { geofences, enabled }
    } catch (error) {
      logger.error('Error getting geofences:', error)
      throw error
    }
  }

  /**
   * Update geofence
   * @param {number} geofenceId - Geofence ID
   * @param {Object} update - Update data
   * @returns {Promise<Object>}
   */
  static async updateGeofence(geofenceId, update) {
    try {
      const updated = await DatabaseUtils.update(
        'geofences',
        update,
        { geofence_id: geofenceId }
      )

      if (!updated) {
        throw errors.notFound('Geofence not found')
      }

      return await DatabaseUtils.selectWithAnd('geofences', null, {
        geofence_id: geofenceId
      }).first()
    } catch (error) {
      logger.error('Error updating geofence:', error)
      throw error
    }
  }

  /**
   * Update geofences status
   * @param {number} fleetId - Fleet ID
   * @param {boolean} status - New status
   * @returns {Promise<Object>}
   */
  static async updateGeofencesStatus(fleetId, status) {
    try {
      // Update all geofences status
      const geofencesUpdated = await DatabaseUtils.update(
        'geofences',
        { status },
        { fleet_id: fleetId }
      )

      // Update fleet metadata
      const fleetUpdated = await DatabaseUtils.update(
        'fleet_metadata',
        { geofence_enabled: status },
        { fleet_id: fleetId }
      )

      if (!geofencesUpdated || !fleetUpdated) {
        throw errors.notFound('Fleet not found')
      }

      return await DatabaseUtils.selectWithAnd(
        'fleet_metadata',
        ['fleet_id', 'geofence_enabled'],
        { fleet_id: fleetId }
      ).first()
    } catch (error) {
      logger.error('Error updating geofences status:', error)
      throw error
    }
  }

  /**
   * Delete geofence
   * @param {number} geofenceId - Geofence ID
   * @returns {Promise<Object>}
   */
  static async deleteGeofence(geofenceId) {
    try {
      // Get geofence before deletion
      const geofence = await DatabaseUtils.selectWithAnd('geofences', null, {
        geofence_id: geofenceId
      }).first()

      if (!geofence) {
        throw errors.notFound('Geofence not found')
      }

      // Delete geofence
      await DatabaseUtils.delete('geofences', { geofence_id: geofenceId })

      return geofence
    } catch (error) {
      logger.error('Error deleting geofence:', error)
      throw error
    }
  }
}

module.exports = FleetGeofence
