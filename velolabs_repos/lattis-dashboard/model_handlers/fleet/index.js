const FleetCore = require('./fleet-core')
const FleetPayment = require('./payment')
const FleetGeofence = require('./fleet-geofence')
const { FleetDomain } = require('./domain')

/**
 * Fleet operations
 * Combines all fleet-related functionality
 */
class Fleet {
  // Core operations
  static async getFleetList(params) {
    return FleetCore.getFleetList(params)
  }

  static async getFleetsWithCustomers(params) {
    return FleetCore.getFleetsWithCustomers(params)
  }

  static async validateNewFleet(fleetData, imageData, type) {
    return FleetCore.validateNewFleet(fleetData, imageData, type)
  }

  static async updateFleetLogo(fleetDetails, imageDetails) {
    return FleetCore.updateFleetLogo(fleetDetails, imageDetails)
  }

  // Payment operations
  static async createPaymentProfile(params) {
    return FleetPayment.createPaymentProfile(params)
  }

  static async updateBillingCycle(params) {
    return FleetPayment.updateBillingCycle(params)
  }

  static async getPaymentSettings(where) {
    return FleetPayment.getPaymentSettings(where)
  }

  static async updatePaymentSettings(data, where) {
    return FleetPayment.updatePaymentSettings(data, where)
  }

  // Geofence operations
  static async createGeofence(geofence) {
    return FleetGeofence.createGeofence(geofence)
  }

  static async getGeofences(fleetId) {
    return FleetGeofence.getGeofences(fleetId)
  }

  static async updateGeofence(geofenceId, update) {
    return FleetGeofence.updateGeofence(geofenceId, update)
  }

  static async updateGeofencesStatus(fleetId, status) {
    return FleetGeofence.updateGeofencesStatus(fleetId, status)
  }

  static async deleteGeofence(geofenceId) {
    return FleetGeofence.deleteGeofence(geofenceId)
  }

  // Domain operations
  static async saveDomain(params) {
    return FleetDomain.saveDomain(params)
  }

  static async deleteDomain(params) {
    return FleetDomain.deleteDomain(params)
  }

  static async getDomains(params) {
    return FleetDomain.getDomains(params)
  }
}

module.exports = Fleet
