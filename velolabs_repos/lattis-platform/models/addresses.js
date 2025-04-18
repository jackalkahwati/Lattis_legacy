'use strict';

const model = require('./model');

let addresses = {
    tableName: 'addresses',

    columns:  {
        address_id : 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        city: 'VARCHAR(256)',
        address1: 'VARCHAR(256)',
        address2: 'VARCHAR(256)',
        state: 'VARCHAR(32)',
        postal_code: 'VARCHAR(64)',
        country: 'VARCHAR(32)',
        type: 'VARCHAR(32)',
        type_id: 'INT UNSIGNED NOT NULL'
    },

    columnOrder: [
        'address_id',
        'city',
        'address1',
        'address2',
        'state',
        'postal_code',
        'country',
        'type',
        'type_id'
    ]
};

addresses.__proto__ = model;

module.exports = addresses;
