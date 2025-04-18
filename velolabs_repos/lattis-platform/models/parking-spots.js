'use strict';

const model = require('./model');

const parkingSpots = {
    tableName: 'parking_spots',

    columns : {
        parking_spot_id: 'BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        name : 'VARCHAR(64)',
        description : 'VARCHAR(256)',
        pic : 'VARCHAR(128)',
        type: 'VARCHAR(64)',
        latitude: 'DOUBLE',
        longitude: 'DOUBLE',
        capacity: 'INT UNSIGNED DEFAULT 0',
        parking_area_id: 'BIGINT UNSIGNED DEFAULT NULL',
        operator_id: 'BIGINT UNSIGNED',
        customer_id: 'BIGINT UNSIGNED',
        fleet_id: 'BIGINT UNSIGNED'
    },

    columnOrder : [
        'parking_spot_id',
        'name',
        'description',
        'pic',
        'type',
        'latitude',
        'longitude',
        'capacity',
        'parking_area_id',
        'operator_id',
        'customer_id',
        'fleet_id'
    ]
};

parkingSpots.__proto__ = model;

module.exports = parkingSpots;
