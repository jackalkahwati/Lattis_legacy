'use strict';

let model = require('./model');
let users = require('./users');
let _ = require('underscore');

let addedColumns = {
    customer_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
    customer_name: 'VARCHAR(50)',
    region: 'VARCHAR(32)'
};

let addedColumnOrder = [
    'customer_id',
    'customer_name',
    'region'
];

let customers = {};

customers.tableName = 'customers';
customers.columns = _.extend(addedColumns, users.columns);
customers.columnOrder = addedColumnOrder.concat(users.columnOrder);

customers.__proto__ = model;

module.exports = customers;
