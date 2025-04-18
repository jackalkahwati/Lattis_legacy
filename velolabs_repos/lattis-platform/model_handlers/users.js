'use strict';

const sqlPool = require('./../db/sql-pool');
const queryCreator = require('./../db/query-creator');
const errors = require('./../errors/lattis-errors');
const dbConstants = require('./../constants/db-constants');


/**
 * This method fetches users with given parameter
 *
 * @param {string} columnsAndValues
 * @param {Function} callback
 */
const getUsers = (columnsAndValues, callback) => {
    const getUsersQuery = queryCreator.selectWithAnd(dbConstants.tables.users, null, columnsAndValues);
    sqlPool.makeQuery(getUsersQuery, (error, users) => {
        if (error) {
            logger('Error: failed to fetch user with:', columnsAndValues, 'with error:', error);
            callback(errors.internalServer(false), null);
            return;
        }
        if (users && users.length > 0) {
            callback(errors.noError(), users[0]);
            return;
        }
        callback(errors.noError(), null);
    });
};

module.exports = {
    getUsers: getUsers
};
