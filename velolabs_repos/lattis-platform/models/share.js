'use strict';

let model = require('./model');

let share = {
    tableName: 'share',

    columns : {
        share_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        lock_id: 'INT UNSIGNED NOT NULL REFERENCES locks (lock_id)',
        shared_by_user_id: 'INT UNSIGNED NOT NULL',
        shared_to_user_id: 'INT UNSIGNED',
        shared_by_users_id: 'VARCHAR(64)',
        shared_on: 'BIGINT UNSIGNED',
        share_till: 'BIGINT UNSIGNED',
        sharing_stopped: 'BIGINT UNSIGNED',
        confirmation_code: 'VARCHAR(64)'
    },

    columnOrder : [
        'share_id',
        'lock_id',
        'shared_by_user_id',
        'shared_to_user_id',
        'shared_by_users_id',
        'shared_on',
        'share_till',
        'sharing_stopped',
        'confirmation_code'
    ]
};

share.__proto__ = model;

module.exports = share;
