'use strict'

module.exports = {
  status: {
    created: 'created',
    assigned: 'assigned',
    resolved: 'resolved'
  },
  category: {
    reportedTheft: 'reported_theft',
    potentialTheft: 'potential_theft',
    damageReported: 'damage_reported',
    crashDetected: 'crash_detected',
    serviceDue: 'service_due',
    parkingOutsideGeofence: 'parking_outside_geofence',
    other: 'other'
  },
  types: {
    maintenance: 'maintenance',
    crash: 'crash',
    theft: 'theft',
    bike: 'bike',
    trip: 'trip'
  },
  priority: {
    low: 'low',
    high: 'high',
    medium: 'medium'
  },
  log_section: {
    create: 1,
    assign: 2,
    resolve: 3,
    status_update: 4
  },
  log_type: {
    info: 1,
    error: 2,
    warning: 3
  }
}
