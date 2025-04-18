'use strict';

let model = require('./model');

let booking = {
    tableName: 'booking',

    columns: {
        booking_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        booked_on: 'BIGINT UNSIGNED',
        cancelled_on: 'BIGINT UNSIGNED',
        status: 'VARCHAR(64)',
        bike_id: 'INT UNSIGNED NOT NULL REFERENCES bikes (bike_id)',
        trip_id: 'INT UNSIGNED REFERENCES trips (trip_id)',
        lock_id: 'INT UNSIGNED NOT NULL REFERENCES locks (lock_id)',
        user_id: 'INT UNSIGNED NOT NULL',
        fleet_id: 'INT UNSIGNED REFERENCES fleets (fleet_id)',
        customer_id: 'INT UNSIGNED REFERENCES customers (customer_id)',
        operator_id: 'INT UNSIGNED REFERENCES operators (operator_id)'
    },

    columnOrder: [
        'booking_id',
        'booked_on',
        'cancelled_on',
        'status',
        'bike_id',
        'trip_id',
        'lock_id',
        'user_id',
        'fleet_id',
        'customer_id',
        'operator_id'
    ]
};

booking.__proto__ = model;

module.exports = booking;
