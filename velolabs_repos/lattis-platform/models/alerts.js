'use strict';
const model = require('./model');

let alerts = {
    tableName: 'alerts',

    columns:  {
        alert_id : 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        all_tickets: 'TINYINT(1)',
        theft_is_reported: 'TINYINT(1)',
        damage_is_reported: 'TINYINT(1)',
        maintenance_due: 'TINYINT(1)',
        ellipse_battery_low : 'TINYINT(1)',
        bike_is_parked_outside_zone: 'TINYINT(1)',
        bike_is_parked_outside_spot: 'TINYINT(1)',
        operator_id: 'INT UNSIGNED NOT NULL'
    },

    columnOrder: [
        'alert_id',
        'all_tickets',
        'theft_is_reported',
        'damage_is_reported',
        'maintenance_due',
        'ellipse_battery_low',
        'bike_is_parked_outside_zone',
        'bike_is_parked_outside_spot',
        'operator_id'
    ]
};

alerts.__proto__ = model;


module.exports = alerts;
