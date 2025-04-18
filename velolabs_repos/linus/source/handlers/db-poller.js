'use strict';

const platform = require('@velo-labs/platform');
const sqlPool = platform.sqlPool;
const logger = platform.logger;
const dbConstants = platform.dbConstants;


class DBPoller {
    /**
     * This method initializes a database poller object.
     *
     * @param {string} table
     * @param {number || undefined} interval
     */
    constructor(table, interval=5000) {
        this._table = table;
        this._interval = interval;
        this._query = {};
        this._queryString = '';
        this._callback = null;
        this._timer = null;
    };

    /**
     * This method starts the polling of the object.
     *
     * @param {function} callback
     */
    poll(callback) {
        this._callback = callback;
        this._timer = setInterval(this._makeQuery.bind(this), this._interval);
    };

    /**
     * This is a "virtual method" and should be overridden in the child classes.
     */
    startPolling() {
        throw new Error('startPolling must be overridden');
    }

    /**
     * Stops the polling object from polling.
     */
    stopPolling() {
        clearInterval(this._timer);
    };

    /**
     * This method updates the query object.
     */
    updateQuery() {
        this._query[dbConstants.dbNames[this._table]] = this._queryString;
    };

    /**
     * This method sets the Poller's query string property.
     *
     * @param {string} queryString
     */
    setQueryString(queryString) {
        this._queryString = queryString;
    };

    /**
     * This method makes the query into the database to retrieve the rows that have been polled.
     *
     * @private
     */
    _makeQuery() {
        if (!this._query) {
            logger('Could not make polling query in table:', this._table, 'No query set.');
            throw new Error('MissingQuery');
        }

        sqlPool.makeQuery(this._query, (error, results) => {
            if (error) {
                logger('Failed to pool:', this._table, 'for notifications query:', this._query);
                this._callback(error, null);
                return;
            }

            if (!results || results.length === 0) {
                logger('no results for notifications polling with query:', this._query);
                this._callback(error, null);
                return;
            }

            this._callback(null, results);
        });
    };
}

module.exports = DBPoller;
