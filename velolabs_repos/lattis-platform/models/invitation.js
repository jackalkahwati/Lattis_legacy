'use strict';

let model = require('./model');

let invitations = {
    tableName: 'invitations',

    columns: {
        invitation_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        member_name: 'VARCHAR(256)',
        date_created: 'INT UNSIGNED',
        date_joined: 'INT UNSIGNED',
        email: 'INT UNSIGNED NOT NULL',
        fleet_id: 'INT UNSIGNED REFERENCES fleets (fleet_id)',
        operator_id: 'INT UNSIGNED REFERENCES operator (operator_id)',
        customer_id: 'INT UNSIGNED REFERENCES customer (customer_id)'
    },

    columnOrder: [
        'invitation_id',
        'member_name',
        'date_created',
        'date_joined',
        'email',
        'fleet_id',
        'operator_id',
        'customer_id'
    ]
};

invitations.__proto__ = model;

module.exports = invitations;

