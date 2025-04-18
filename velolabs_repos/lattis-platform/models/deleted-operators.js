'use strict';

const model = require('./model');

const deletedOperators = {
    tableName: 'deleted_operators',

    columns : {
        delete_operator_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        operator_id: 'INT UNSIGNED NOT NULL',
        business_name: 'VARCHAR(64)',
        username: 'VARCHAR(64)',
        password: 'VARCHAR(128)',
        date: 'BIGINT UNSIGNED'
    },

   columnOrder : [
    'delete_operator_id',
    'operator_id',
    'business_name',
    'username',
    'password',
    'date'
]

};

deletedOperators.__proto__ = model;

module.exports = deletedOperators;
