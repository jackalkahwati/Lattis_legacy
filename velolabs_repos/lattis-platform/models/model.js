'use strict';

const _ = require('underscore');
const queryHelper = require('./../db/query-helper');


module.exports = {
    tableName: '',

    columns: {},

    columnOrder: [],

    createTableQuery: function () {
        let counter = 0;
        let query = 'CREATE TABLE ' + queryHelper.addDatabaseName(this.tableName) + ' (';
        _.each(this.columnOrder, function (columnName) {
            query += '`' + columnName + '` ' + this.columns[columnName] +
                (counter === this.columnOrder.length - 1 ? ')' : ', ');
            counter++;
        }, this);

        let queryObject = {};
        queryObject[queryHelper.getDatabaseNameForTable(this.tableName)] = query;
        return queryObject;
    }
};
