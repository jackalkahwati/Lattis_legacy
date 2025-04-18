'use strict';

let model = require('./model');

let domains = {
    tableName: 'domains',

    columns: {
        domain_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        domain_name: 'VARCHAR(128)',
        fleet_id: 'INT UNSIGNED REFERENCES fleets (fleet_id)',
        operator_id: 'INT UNSIGNED REFERENCES operator (operator_id)',
        customer_id: 'INT UNSIGNED REFERENCES customer (customer_id)'
    },

    columnOrder: [
        'domain_id',
        'domain_name',
        'fleet_id',
        'operator_id',
        'customer_id',
    ]
};

domains.__proto__ = model;

module.exports = domains;


