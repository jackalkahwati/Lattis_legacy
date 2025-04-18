'use strict';

const model = require('./model');

const fleetAssociations = {
    tableName: 'fleet_associations',

    columns: {
        fleet_association_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        fleet_id: 'INT UNSIGNED NOT NULL',
        operator_id: 'INT UNSIGNED NOT NULL',
        customer_id: 'INT UNSIGNED NOT NULL',
        acl: 'VARCHAR(32)',
        on_call: 'TINYINT(1)'
    },

    columnOrder: [
        'fleet_association_id',
        'fleet_id',
        'operator_id',
        'customer_id',
        'acl',
        'on_call'
    ]
};

fleetAssociations.__proto__ = model;

module.exports = fleetAssociations;
