'use strict';

const sqlPool = require('./../db/sql-pool');
const queryCreator = require('./../db/query-creator');
const errors = require('./../errors/lattis-errors');
const dbConstants = require('./../constants/db-constants');


/**
 * This method fetches operators with given parameter
 *
 * @param {string} columnsAndValues
 * @param {Function} callback
 */
const getOperators = (columnsAndValues, callback) => {
    const getUsersQuery = queryCreator.selectWithAnd(dbConstants.tables.operators, null, columnsAndValues);
    sqlPool.makeQuery(getUsersQuery, (error, operators) => {
        if (error) {
            logger('Error: failed to fetch operator with:', columnsAndValues, 'with error:', error);
            callback(errors.internalServer(false), null);
            return;
        }
        if (operators && operators.length > 0) {
            callback(errors.noError(), operators[0]);
            return;
        }
        callback(errors.noError(), null);
    });
};


module.exports = {
    getOperators: getOperators
};
