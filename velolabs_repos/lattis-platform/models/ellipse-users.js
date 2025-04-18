'use strict';

let model = require('./model');
let users = require('./users');
let _ = require('underscore');

let columns = {
    user_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
    users_id: 'VARCHAR(128)',
    username: 'VARCHAR(64)',
    password: 'VARCHAR(128)',
    user_type: 'VARCHAR(64)',
    member_type: 'VARCHAR(64)',
    reg_id: 'VARCHAR(256)',
    verified: 'TINYINT(1)',
    accepted_terms: 'TINYINT(1)',
    max_locks: 'INT'
};

let columnOrder = [
    'user_id',
    'users_id',
    'username',
    'password',
    'user_type',
    'member_type',
    'reg_id',
    'verified',
    'accepted_terms',
    'max_locks'
];

let ellipseUsers = {};
ellipseUsers.tableName = 'users';
ellipseUsers.columns = _.extend(columns, users.columns);
ellipseUsers.columnOrder = columnOrder.concat(users.columnOrder);

ellipseUsers.__proto__ = model;

module.exports = ellipseUsers;
