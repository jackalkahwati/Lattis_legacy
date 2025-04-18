'use strict';

let model = require('./model');

let deletedUsers = {
    tableName: 'deleted_users',

    columns : {
        delete_user_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        user_id: 'INT UNSIGNED NOT NULL',
        password: 'VARCHAR(128)',
        user_type: 'VARCHAR(64)',
        email: 'VARCHAR(64)',
        date: 'BIGINT UNSIGNED',
    },

    columnOrder : [
        'delete_user_id',
        'user_id',
        'password',
        'user_type',
        'email',
        'date'
    ]
};

deletedUsers.__proto__ = model;

module.exports = deletedUsers;
