'use strict';

let model = require('./model');
let _ = require('underscore');

let trip = {
    tableName: 'trips',

    columns: {
        trip_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        steps: 'MEDIUMTEXT',
        start_address: 'VARCHAR(100)',
        end_address: 'VARCHAR(100)',
        date_created: 'BIGINT UNSIGNED',
        parking_image : 'VARCHAR(128)',
        rating : 'TINYINT(1)',
        user_id: 'INT UNSIGNED NOT NULL',
        operator_id: 'INT UNSIGNED',
        customer_id : 'INT UNSIGNED',
        lock_id: 'INT UNSIGNED NOT NULL REFERENCES locks (lock_id)',
        bike_id: 'INT UNSIGNED REFERENCES bikes (bike_id)',
        fleet_id:'INT UNSIGNED REFERENCES fleets (fleet_id)'
    },

    columnOrder: [
        'trip_id',
        'steps',
        'start_address',
        'end_address',
        'date_created',
        'parking_image',
        'rating',
        'user_id',
        'operator_id',
        'customer_id',
        'lock_id',
        'bike_id',
        'fleet_id'
    ]
};


trip.__proto__ = model;

module.exports = trip;
