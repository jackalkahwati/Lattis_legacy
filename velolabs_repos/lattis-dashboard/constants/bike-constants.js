'use strict'

module.exports = {
  types: {
    regular: 'regular',
    electric: 'electric'
  },

  current_status: {
    lockAssigned: 'lock_assigned',
    controllerAssigned: 'controller_assigned',
    lockNotAssigned: 'lock_not_assigned',
    parked: 'parked',
    onTrip: 'on_trip',
    damaged: 'damaged',
    reportedStolen: 'reported_stolen',
    stolen: 'stolen',
    underMaintenance: 'under_maintenance',
    totalLoss: 'total_loss',
    defleeted: 'defleeted',
    reserved: 'reserved',
    collect: 'collect',
    balancing: 'balancing',
    transport: 'transport'
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
    parkingOutsideGeofence: 'parking_outside_geofence'
  },

  grinIoTDefaultSettings: {
    growMaximumSpeed: 200,
    scooterDriveMode: 0,
    scooterBrakeMode: 1,
    heartBeatInterval: 20, // 20 sec
    lockedHeartBeatInterval: 600 // 20 sec
  }
}
