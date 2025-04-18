'use strict';

let model = require('./model');

let ellipse = {
    tableName: 'ellipse',

    columns: {
        ellipse_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        ellipse_name: 'VARCHAR(50)',
        public_key_1: 'VARCHAR(64)',
        public_key_2: 'VARCHAR(64)',
        mac_address :  'VARCHAR(256)',
        ellipse_status : 'VARCHAR(45)',
        battery_level: 'FLOAT',
        bike_id :'INT UNSIGNED NOT NULL REFERENCES bikes (bike_id)',
        hub_id : 'INT UNSIGNED NOT NULL REFERENCES hubs (hub_id)',
        admin_id: 'INT UNSIGNED NOT NULL REFERENCES admins (admin_id)',
        customer_id: 'INT UNSIGNED REFERENCES customers (customer_id)',
        fleet_id :'INT UNSIGNED NOT NULL REFERENCES fleets (fleet_id)'
    },
    columnOrder: [
        'ellipse_id',
        'ellipse_name',
        'public_key_1',
        'public_key_2',
        'mac_address',
        'ellipse_status',
        'battery_level',
        'bike_id',
        'hub_id',
        'admin_id',
        'customer_id',
        'fleet_id'
    ]
};

ellipse.__proto__ = model;

module.exports = ellipse;
