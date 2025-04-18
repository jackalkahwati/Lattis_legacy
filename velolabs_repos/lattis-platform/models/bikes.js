'use strict';

let model = require('./model');

let bike = {
    tableName: 'bikes',

    columns: {
        bike_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        bike_name: 'VARCHAR(128)',
        date_created: 'BIGINT UNSIGNED',
        status: 'VARCHAR(64)',
        bike_battery_level: 'FLOAT',
        current_status: 'VARCHAR(128)',
        maintenance_status: 'VARCHAR(64)',
        distance: 'DOUBLE',
        latitude: 'DOUBLE',
        longitude: 'DOUBLE',
        lock_id: 'INT UNSIGNED REFERENCES locks (lock_id)',
        fleet_id: 'INT UNSIGNED REFERENCES fleets (fleet_id)',
        distance_after_service: 'DOUBLE',
        qr_code_id: 'INT(24)',
        parking_spot_id: 'INT UNSIGNED REFERENCES parking_spots (parking_spot_id)',
        bike_group_id: 'INT UNSIGNED REFERENCES bike_group (bike_group_id)'
    },

    columnOrder: [
        'bike_id',
        'bike_name',
        'date_created',
        'status',
        'bike_battery_level',
        'current_status',
        'maintenance_status',
        'distance',
        'latitude',
        'longitude',
        'lock_id',
        'fleet_id',
        'distance_after_service',
        'qr_code_id',
        'parking_spot_id',
        'bike_group_id'
    ]
};

bike.__proto__ = model;

module.exports = bike;
