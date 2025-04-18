'use strict';

const model = require('./model');

let thefts = {
    tableName: 'thefts',

    columns : {
        theft_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        date: 'BIGINT UNSIGNED',
        confirmed: 'TINYINT(1) DEFAULT 0',
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
        trip_id: 'INT UNSIGNED DEFAULT NULL'
    },

    columnOrder : [
        'theft_id',
        'date',
        'confirmed',
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
        'trip_id'
    ]
};

thefts.__proto__ = model;

module.exports = thefts;
