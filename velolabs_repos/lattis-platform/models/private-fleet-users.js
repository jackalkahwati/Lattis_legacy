'use strict';

let model = require('./model');

let privateFleetUsers = {
    tableName: 'private_fleet_users',

    columns: {
        private_fleet_user_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        email: 'VARCHAR(128)',
        user_id: 'BIGINT UNSIGNED NOT NULL',
        fleet_id: 'BIGINT UNSIGNED',
        verified: 'TINYINT(1)'
    },

    columnOrder: [
        'private_fleet_user_id',
        'email',
        'user_id',
        'fleet_id',
        'verified'
    ]
};

privateFleetUsers.__proto__ = model;

module.exports = privateFleetUsers;
