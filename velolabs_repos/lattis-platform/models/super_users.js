'use strict';

let model = require('./model');
let users = require('./users');
let _ = require('underscore');

let columns = {
    superuser_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
    username: 'VARCHAR(64)',
    password: 'CHAR(80)',
    user_state: 'VARCHAR(64)'
};

let columnOrder = [
    'superuser_id',
    'username',
    'password',
    'user_state'
];


let superUser = {};
superUser.tableName = 'super_users';
superUser.columns = _.extend(columns, users.columns);
superUser.columnOrder = columnOrder.concat(users.columnOrder);

superUser.__proto__ = model;

module.exports = superUser;
