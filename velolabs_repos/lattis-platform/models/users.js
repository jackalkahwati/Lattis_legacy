'use strict';

const model = require('./model');
const _ = require('underscore');

const users = {
    tableName: 'users',

    columns: {
        title: 'VARCHAR(10)',
        first_name: 'VARCHAR(50)',
        last_name: 'VARCHAR(50)',
        phone_number: 'VARCHAR(20)',
        email: 'VARCHAR(128)',
        rest_token: 'VARCHAR(256)',
        refresh_token: 'VARCHAR(256)',
        date_created: 'BIGINT UNSIGNED',
        country_code: 'VARCHAR(10)',
        gender :'VARCHAR(45)',
        date_of_birth: 'BIGINT UNSIGNED',
        address_id: 'BIGINT UNSIGNED DEFAULT NULL'
    },

    columnOrder: [
        'title',
        'first_name',
        'last_name',
        'phone_number',
        'email',
        'rest_token',
        'refresh_token',
        'date_created',
        'country_code',
        'gender',
        'date_of_birth',
        'address_id'
    ]
};

users.__proto__ = model;

module.exports = users;
