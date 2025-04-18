const model = require('./model');

const notifications = {
    tableName: 'notifications',

    columns: {
        notification_id: 'INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY',
        operator_id: 'INT UNSIGNED NOT NULL',
        customer_id: 'INT UNSIGNED',
        user_id: 'INT UNSIGNED',
        type: 'VARCHAR(64)',
        type_id: 'INT UNSIGNED',
        date: 'BIGINT UNSIGNED',
        rank: 'DOUBLE'
    },

    columnOrder: [
        'notification_id',
        'operator_id',
        'customer_id',
        'user_id',
        'type',
        'type_id',
        'date',
        'rank'
    ]
};

notifications.__proto__ = model;

module.exports = notifications;
