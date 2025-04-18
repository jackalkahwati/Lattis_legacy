'use strict';

let model = require('./model');

let lock = {
    tableName: 'locks',

    columns: {
        lock_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        user_id: 'INT UNSIGNED',
        mac_id: 'VARCHAR(128) DEFAULT NULL',
        key: 'VARCHAR(256) DEFAULT NULL',
        name: 'VARCHAR(128) DEFAULT NULL',
        status: 'VARCHAR(64)',
        battery_level: 'DOUBLE',
        operator_id: 'INT UNSIGNED',
        customer_id: 'INT UNSIGNED',
        fleet_id: 'INT UNSIGNED REFERENCES fleets (fleet_id)'
    },

    columnOrder: [
        'lock_id',
        'user_id',
        'mac_id',
        'key',
        'name',
        'status',
        'battery_level',
        'operator_id',
        'customer_id',
        'fleet_id'
    ]
};

lock.__proto__ = model;

module.exports = lock;
