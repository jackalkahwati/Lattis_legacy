'use strict';

let model = require('./model');

let pinCodes = {
    tableName: 'pin_codes',

    columns: {
        pin_code_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        lock_id: 'INT UNSIGNED REFERENCES locks (lock_id)',
        user_id: 'INT UNSIGNED REFERENCES users (user_id)',
        date: 'BIGINT UNSIGNED',
        code: 'VARCHAR(256)'
    },

    columnOrder: [
        'pin_code_id',
        'lock_id',
        'user_id',
        'date',
        'code'
    ]
};

pinCodes.__proto__ = model;

module.exports = pinCodes;
