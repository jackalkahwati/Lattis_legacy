'use strict';

const model = require('./model');

const tickets = {
    tableName: 'tickets',

    columns : {
        ticket_id: 'BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        name : 'VARCHAR(64)',
        category: 'VARCHAR(64)',
        status: 'VARCHAR(64)',
        assignee: 'BIGINT UNSIGNED',
        date_created: 'INT UNSIGNED',
        date_resolved: 'INT UNSIGNED',
        rider_notes: 'VARCHAR(1024)',
        operator_notes: 'VARCHAR(1024)',
        maintenance_notes: 'VARCHAR(1024)',
        type: 'VARCHAR(32)',
        type_id: 'INT UNSIGNED NOT NULL',
        fleet_id: 'BIGINT UNSIGNED',
        operator_id: 'BIGINT UNSIGNED',
        customer_id: 'BIGINT UNSIGNED'
    },

    columnOrder : [
        'ticket_id',
        'name',
        'category',
        'status',
        'assignee',
        'date_created',
        'date_resolved',
        'rider_notes',
        'operator_notes',
        'maintenance_notes',
        'type',
        'type_id',
        'fleet_id',
        'operator_id',
        'customer_id'
    ]
};

tickets.__proto__ = model;

module.exports = tickets;
