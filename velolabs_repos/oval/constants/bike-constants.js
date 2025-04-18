'use strict'

module.exports = {
  current_status: {
    lockAssigned: 'lock_assigned',
    lockNotAssigned: 'lock_not_assigned',
    controllerAssigned: 'controller_assigned',
    parked: 'parked',
    collect: 'collect',
    onTrip: 'on_trip',
    damaged: 'damaged',
    reportedStolen: 'reported_stolen',
    stolen: 'stolen',
    underMaintenance: 'under_maintenance',
    totalLoss: 'total_loss',
    defleet: 'defleeted',
    reserved: 'reserved'
  },

  status: {
    active: 'active',
    inactive: 'inactive',
    suspended: 'suspended',
    deleted: 'deleted'
  },

  maintenance_status: {
    shopMaintenance: 'shop_maintenance',
    fieldMaintenance: 'field_maintenance',
    parkedOutsideGeofence: 'parked_outside_geofence'
  },

  grinIoTDefaultSettings: {
    growLED: 1,
    growMaximumSpeed: 20,
    scooterDriveMode: 0,
    scooterBrakeMode: 1,
    heartBeatInterval: 20, // 20 sec
    lockedHeartBeatInterval: 600 // Reduce freq when bike locked
  }
}
