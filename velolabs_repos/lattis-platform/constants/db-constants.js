'use strict';

const config = require('./../config');

/*
 * Related a table name with a database
 */
module.exports = {
    dbNames: {
        alerts: config.databaseInfo.main.databaseName,
        addresses: config.databaseInfo.main.databaseName,
        super_users: config.databaseInfo.users.databaseName,
        bikes: config.databaseInfo.main.databaseName,
        booking: config.databaseInfo.main.databaseName,
        confirmation_codes: config.databaseInfo.users.databaseName,
        crashes: config.databaseInfo.main.databaseName,
        customers: config.databaseInfo.users.databaseName,
        deleted_bikes: config.databaseInfo.main.databaseName,
        deleted_operators: config.databaseInfo.users.databaseName,
        deleted_users: config.databaseInfo.users.databaseName,
        electric_bikes: config.databaseInfo.main.databaseName,
        fleet_associations: config.databaseInfo.main.databaseName,
        fleet_metadata: config.databaseInfo.main.databaseName,
        fleets: config.databaseInfo.main.databaseName,
        geofence_circle: config.databaseInfo.main.databaseName,
        geofence_polygon: config.databaseInfo.main.databaseName,
        locks: config.databaseInfo.main.databaseName,
        maintenance: config.databaseInfo.main.databaseName,
        metadata: config.databaseInfo.main.databaseName,
        notifications: config.databaseInfo.main.databaseName,
        operators: config.databaseInfo.users.databaseName,
        operator_payment_profiles:config.databaseInfo.main.databaseName,
        user_payment_profiles:config.databaseInfo.main.databaseName,
        operator_payment_invoice:config.databaseInfo.main.databaseName,
        parking_areas: config.databaseInfo.main.databaseName,
        parking_spots: config.databaseInfo.main.databaseName,
        pin_codes: config.databaseInfo.main.databaseName,
        share: config.databaseInfo.main.databaseName,
        thefts: config.databaseInfo.main.databaseName,
        trips: config.databaseInfo.main.databaseName,
        users: config.databaseInfo.users.databaseName,
        bike_group: config.databaseInfo.main.databaseName,
        tickets: config.databaseInfo.main.databaseName,
        domains: config.databaseInfo.main.databaseName,
        invitations: config.databaseInfo.main.databaseName,
        private_fleet_users: config.databaseInfo.users.databaseName,
        fleet_payment_settings: config.databaseInfo.main.databaseName,
        trip_payment_transactions: config.databaseInfo.main.databaseName,
        user_refunded: config.databaseInfo.main.databaseName,
        sdk: config.databaseInfo.users.databaseName,
        controllers: config.databaseInfo.main.databaseName
    },

    tables: {
        alerts: 'alerts',
        bikes: 'bikes',
        booking: 'booking',
        confirmation_codes: 'confirmation_codes',
        crashes: 'crashes',
        customers: 'customers',
        deleted_bikes: 'deleted_bikes',
        deleted_operators: 'deleted_operators',
        deleted_users: 'deleted_users',
        electric_bikes: 'electric_bikes',
        fleet_associations: 'fleet_associations',
        fleet_metadata: 'fleet_metadata',
        fleets: 'fleets',
        geofence_circle: 'geofence_circle',
        geofence_polygon: 'geofence_polygon',
        locks: 'locks',
        maintenance: 'maintenance',
        metadata: 'metadata',
        operators: 'operators',
        operator_payment_profiles:'operator_payment_profiles',
        operator_payment_invoice:'operator_payment_invoice',
        user_payment_profiles:'user_payment_profiles',
        parking_areas: 'parking_areas',
        parking_spots: 'parking_spots',
        share: 'share',
        thefts: 'thefts',
        trips: 'trips',
        users: 'users',
        pin_codes: 'pin_codes',
        notifications: 'notifications',
        addresses: 'addresses',
        super_users: 'super_users',
        bike_group: 'bike_group',
        tickets: 'tickets',
        domains: 'domains',
        invitations: 'invitations',
        private_fleet_users: 'private_fleet_users',
        fleet_payment_settings:'fleet_payment_settings',
        trip_payment_transactions:'trip_payment_transactions',
        user_refunded:'user_refunded',
        sdk: 'sdk',
        controllers: 'controllers'
    }
};
