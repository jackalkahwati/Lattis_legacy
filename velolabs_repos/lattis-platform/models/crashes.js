'use strict';

let model = require('./model');

let crashes = {
    tableName: 'crashes',

    columns : {
        crash_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        date: 'BIGINT UNSIGNED',
        message_sent: 'TINYINT(1) DEFAULT 0',
        x_ave: 'DOUBLE',
        y_ave: 'DOUBLE',
        z_ave: 'DOUBLE',
        x_dev: 'DOUBLE',
        y_dev: 'DOUBLE',
        z_dev: 'DOUBLE',
        latitude: 'DOUBLE',
        longitude: 'DOUBLE',
        lock_id: 'INT UNSIGNED REFERENCES locks (lock_id)',
        user_id: 'INT UNSIGNED DEFAULT NULL',
        trip_id: 'INT UNSIGNED DEFAULT NULL',
        operator_id: 'INT UNSIGNED DEFAULT NULL',
        customer_id: 'INT UNSIGNED DEFAULT NULL'
    },

    columnOrder : [
        'crash_id',
        'date',
        'message_sent',
        'x_ave',
        'y_ave',
        'z_ave',
        'x_dev',
        'y_dev',
        'z_dev',
        'latitude',
        'longitude',
        'lock_id',
        'user_id',
        'trip_id',
        'operator_id',
        'customer_id'
    ]
};

crashes.__proto__ = model;

module.exports = crashes;
