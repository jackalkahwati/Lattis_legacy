/**
 * Fleet-related constants
 */
module.exports = {
  // Access control levels
  acl: {
    admin: 'admin',
    normal_admin: 'normal_admin',
    staff: 'staff',
    superAdmin: 'super_admin',
    supportAdmin: 'support_admin'
  },

  // Card information
  cardinfo: {
    type_card: 'primary'
  },

  // Fleet types
  types: {
    private: 'private',
    public: 'public',
    hybrid: 'hybrid'
  },

  // Fleet status
  status: {
    active: 'active',
    inactive: 'inactive',
    pending: 'pending'
  },

  // Payment settings
  payment: {
    currency: {
      USD: 'USD',
      EUR: 'EUR',
      GBP: 'GBP',
      CAD: 'CAD'
    },
    priceTypes: {
      hourly: 'hourly',
      daily: 'daily',
      weekly: 'weekly',
      monthly: 'monthly'
    },
    depositTypes: {
      fixed: 'fixed',
      percentage: 'percentage'
    }
  },

  // Geofence types
  geofence: {
    types: {
      parking: 'parking',
      operating: 'operating',
      restricted: 'restricted'
    },
    shapes: {
      circle: 'circle',
      polygon: 'polygon'
    }
  },

  // Time settings
  time: {
    formats: {
      display: 'YYYY-MM-DD HH:mm:ss',
      storage: 'X' // Unix timestamp
    },
    defaults: {
      timezone: 'UTC'
    }
  },

  // Database tables
  tables: {
    fleets: 'fleets',
    fleet_metadata: 'fleet_metadata',
    fleet_associations: 'fleet_associations',
    fleet_payment_settings: 'fleet_payment_settings',
    addresses: 'addresses',
    domains: 'domains',
    geofences: 'geofences',
    operator_payment_profiles: 'operator_payment_profiles',
    operator_payment_invoice: 'operator_payment_invoice'
  },

  // Default settings
  defaults: {
    maxTripLength: 24, // hours
    bikeBookingInterval: 15, // minutes
    maxBikeBookingInterval: 60, // minutes
    minBikeBookingInterval: 5, // minutes
    parkingImageRequired: true,
    trackTrips: true,
    emailOnStartTrip: false,
    requirePhoneNumber: true,
    smartDockingStationsEnabled: false
  },

  // Validation
  validation: {
    fleetName: {
      minLength: 3,
      maxLength: 50,
      pattern: /^[a-zA-Z0-9\s-_]+$/
    },
    email: {
      pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    },
    phone: {
      minLength: 10,
      maxLength: 15,
      pattern: /^\+?[\d\s-]+$/
    }
  },

  // Error messages
  errors: {
    fleetNameExists: 'Fleet name already exists',
    invalidFleetName: 'Invalid fleet name. Use only letters, numbers, spaces, hyphens and underscores',
    invalidEmail: 'Invalid email address',
    invalidPhone: 'Invalid phone number',
    invalidTimeZone: 'Invalid timezone',
    invalidCurrency: 'Invalid currency code',
    invalidPriceType: 'Invalid price type',
    invalidDepositType: 'Invalid deposit type',
    invalidGeofenceType: 'Invalid geofence type',
    invalidGeofenceShape: 'Invalid geofence shape'
  }
}
