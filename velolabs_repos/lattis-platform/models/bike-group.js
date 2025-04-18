'use strict';

let model = require('./model');
let _ = require('underscore');

let bikeGroup = {
    tableName: 'bike_group',

    columns: {
        bike_group_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        make:'VARCHAR(64)',
        model:'VARCHAR(64)',
        type:'VARCHAR(45)',
        description:'VARCHAR(1024)',
        date_created: 'BIGINT UNSIGNED',
        pic : 'VARCHAR(128)',
        maintenance_schedule: 'DOUBLE',
        customer_id: 'INT UNSIGNED REFERENCES customers (customer_id)',
        operator_id: 'INT UNSIGNED REFERENCES operators (operator_id)',
        fleet_id: 'INT UNSIGNED REFERENCES fleets (fleet_id)'
    },

    columnOrder: [
        'bike_group_id',
        'make',
        'model',
        'type',
        'description',
        'date_created',
        'pic',
        'maintenance_schedule',
        'customer_id',
        'operator_id',
        'fleet_id'
    ]
};


bikeGroup.__proto__ = model;

module.exports = bikeGroup;
