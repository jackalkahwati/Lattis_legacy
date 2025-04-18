'use strict'

const platform = require('@velo-labs/platform')
const config = platform.config

/*
 * Related a table name with a database
 */
module.exports = {
  dbNames: {
    operators: config.databaseInfo.users.databaseName,
    customers: config.databaseInfo.users.databaseName,
    users: config.databaseInfo.users.databaseName,
    deleted_users: config.databaseInfo.users.databaseName,
    confirmation_codes: config.databaseInfo.users.databaseName,
    bikes: config.databaseInfo.main.databaseName,
    crashes: config.databaseInfo.main.databaseName,
    thefts: config.databaseInfo.main.databaseName,
    fleets: config.databaseInfo.main.databaseName,
    parking_areas: config.databaseInfo.main.databaseName,
    parking_spots: config.databaseInfo.main.databaseName,
    locks: config.databaseInfo.main.databaseName,
    maintenance: config.databaseInfo.main.databaseName,
    metadata: config.databaseInfo.main.databaseName,
    share: config.databaseInfo.main.databaseName,
    trips: config.databaseInfo.main.databaseName,
    pin_codes: config.databaseInfo.main.databaseName,
    notifications: config.databaseInfo.main.databaseName,
    addresses: config.databaseInfo.main.databaseName,
    booking: config.databaseInfo.main.databaseName,
    user_payment_profiles: config.databaseInfo.main.databaseName
  },

  tables: {
    operators: 'operators',
    customers: 'customers',
    users: 'users',
    deleted_users: 'deleted_users',
    confirmation_codes: 'confirmation_codes',
    bikes: 'bikes',
    crashes: 'crashes',
    thefts: 'thefts',
    fleets: 'fleets',
    parking_areas: 'parking_areas',
    parking_spots: 'parking_spots',
    locks: 'locks',
    maintenance: 'maintenance',
    metadata: 'metadata',
    share: 'share',
    trips: 'trips',
    pin_codes: 'pin_codes',
    notifications: 'notifications',
    addresses: 'addresses',
    booking: 'booking',
    user_payment_profiles: 'user_payment_profiles'
  }
}
