'use strict'

module.exports = {
  status: {
    created: 'created',
    assigned: 'assigned',
    resolved: 'resolved'
  },
  ticket_status: {
    open: 'open',
    closed: 'closed',
    inprogress: 'in-progress'
  },
  category: {
    reportedTheft: 'reported_theft',
    damageReported: 'damage_reported',
    serviceDue: 'service_due',
    parkingOutsideGeofence: 'parking_outside_geofence',
    issueDetected: 'issue_detected'
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
  table_status: {
    activated: 1,
    deactivated: 2
  },
  user_types: {
    rider: 1,
    operator: 2,
    maintenance: 3
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
