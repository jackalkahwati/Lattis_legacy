'use strict';

const model = require('./model');

const deletedBikes = {
    tableName: 'deleted_bikes',

    columns : {
        delete_bike_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        bike_id: 'INT UNSIGNED NOT NULL',
        type:'VARCHAR(32)',
        date_created: 'BIGINT UNSIGNED',
        lock_id: 'INT UNSIGNED REFERENCES locks (lock_id)',
        date: 'BIGINT UNSIGNED',
    },

    columnOrder : [
        'delete_bike_id',
        'bike_id',
        'type',
        'date_created',
        'lock_id',
        'date'
    ]
};

deletedBikes.__proto__ = model;

module.exports = deletedBikes;
