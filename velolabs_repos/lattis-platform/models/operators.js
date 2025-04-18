'use strict';

let model = require('./model');
let users = require('./users');
let _ = require('underscore');

let columns = {
    operator_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
    username: 'VARCHAR(64)',
    password: 'CHAR(80)',
    user_state: 'VARCHAR(64)',
    acl: 'VARCHAR(64)',
    reset_password_token: 'VARCHAR(256)',
    reset_password_expires: 'VARCHAR(256)',
    customer_id: 'INT UNSIGNED NOT NULL REFERENCES customers (customer_id)'
}

let columnOrder = [
    'operator_id',
    'username',
    'password',
    'user_state',
    'acl',
    'reset_password_token',
    'reset_password_expires',
    'customer_id'
];


let operator = {};
operator.tableName = 'operators';
operator.columns = _.extend(columns, users.columns);
operator.columnOrder = columnOrder.concat(users.columnOrder);

operator.__proto__ = model;

module.exports = operator;
