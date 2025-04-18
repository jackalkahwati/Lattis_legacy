'use strict';

let model = require('./model');

let maintenance = {
    tableName: 'maintenance',

    columns: {
        maintenance_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        status: 'VARCHAR(100)',
        service_start_date: 'BIGINT UNSIGNED',
        service_end_date: 'BIGINT UNSIGNED',
        category: 'VARCHAR(45)',
        user_photo: 'VARCHAR(128)',
        operator_photo: 'VARCHAR(128)',
        lock_id: 'INT UNSIGNED REFERENCES locks (lock_id)',
        bike_id: 'INT UNSIGNED NOT NULL REFERENCES bikes (bike_id)',
        fleet_id: 'INT UNSIGNED NOT NULL REFERENCES fleets (fleet_id)',
        customer_id: 'INT UNSIGNED',
        operator_id: 'INT UNSIGNED NOT NULL'
    },

    columnOrder: [
        'maintenance_id',
        'status',
        'service_start_date',
        'service_end_date',
        'category',
        'user_photo',
        'operator_photo',
        'lock_id',
        'bike_id',
        'fleet_id',
        'customer_id',
        'operator_id'
    ]
};

maintenance.__proto__ = model;

module.exports = maintenance;
