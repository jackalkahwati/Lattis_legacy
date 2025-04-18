'use strict';

const model = require('./model');

const parkingAreas = {
    tableName: 'parking_areas',

    columns : {
        parking_area_id: 'BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        name : 'VARCHAR(64)',
        type: 'VARCHAR(64)',
        zone: 'VARCHAR(128)',
        geometry: 'VARCHAR(1024)',
        operator_id: 'BIGINT UNSIGNED',
        customer_id: 'BIGINT UNSIGNED',
        fleet_id: 'BIGINT UNSIGNED',
    },

    columnOrder : [
        'parking_area_id',
        'name',
        'type',
        'zone',
        'geometry',
        'operator_id',
        'customer_id',
        'fleet_id'
    ]
};

parkingAreas.__proto__ = model;

module.exports = parkingAreas;
