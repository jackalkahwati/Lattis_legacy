'use strict';

let model = require('./model');

let fleets = {
    tableName: 'fleets',

    columns: {
        fleet_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        fleet_name: 'VARCHAR(256)',
        date_created: 'BIGINT UNSIGNED',
        customer_id: 'INT UNSIGNED',
        operator_id: 'INT UNSIGNED NOT NULL',
        key: 'VARCHAR(256)',
        type: 'VARCHAR(128)',
        logo: 'VARCHAR(256)',
        contact_first_name: 'VARCHAR(128)',
        contact_last_name: 'VARCHAR(128)',
        contact_email: 'VARCHAR(256)',
        contact_phone: 'VARCHAR(20)',
        contract_file: 'VARCHAR(256)',
        address_id: 'INT UNSIGNED REFERENCES addresses (address_id)',
        country_code: 'VARCHAR(10)',
        parking_area_restriction: 'TINYINT(1)'
    },

    columnOrder: [
        'fleet_id',
        'fleet_name',
        'date_created',
        'customer_id',
        'operator_id',
        'key',
        'type',
        'logo',
        'contact_first_name',
        'contact_last_name',
        'contact_email',
        'contact_phone',
        'contract_file',
        'address_id',
        'country_code',
        'parking_area_restriction'
    ]
};

fleets.__proto__ = model;

module.exports = fleets;
