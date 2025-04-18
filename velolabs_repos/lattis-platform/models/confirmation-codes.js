'use strict';

let model = require('./model');

let confirmationCodes = {
    tableName: 'confirmation_codes',

    columns : {
        confirmation_code_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        date: 'BIGINT UNSIGNED',
        type: 'VARCHAR(32)',
        code: 'VARCHAR(64)',
        action_taken: 'TINYINT(1) DEFAULT 0',
        user_id: 'INT UNSIGNED NOT NULL'
    },

    columnOrder : [
        'confirmation_code_id',
        'date',
        'type',
        'code',
        'action_taken',
        'user_id'
    ]
};

confirmationCodes.__proto__ = model;

module.exports = confirmationCodes;
