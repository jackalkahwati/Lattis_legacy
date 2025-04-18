'use strict';

const platform = require('@velo-labs/platform');
const Poller = require('./db-poller');
const logger = platform.logger;
const moment = require('moment');
const dbConstants = platform.dbConstants;
const _ = require('underscore');


class CrashPoller extends Poller {
    constructor(table, interval=5000) {
        super(table, interval);
        this._crashes = new Set();
        this._startTime = 0;
        this._endTime = 0;
        this._finishedPollingCallback = null;
    };

    startPolling(callback=null) {
        if (!!callback) {
            this._finishedPollingCallback = callback;
        }

        this.updateQuery();
        this.poll((error, crashes) => {
            if (error) {
                logger('Error: crash poller failed with error:', error);
                return;
            }

            if (crashes && crashes.length > 0) {
                this._handleCrashData(crashes);
            }
        });
    };

    _handleCrashData(crashes) {
        this.updateQuery();
        let removedCounter = 0;
        let addedCounter = 0;
        let newCrashes = [];
        crashes.forEach((crash) => {
            if (this._crashes.has(crash.crash_id) && this._startTime > crash.date) {
                this._crashes.delete(crash.crash_id);
                removedCounter += 1;
            } else if (!this._crashes.has(crash.crash_id)) {
                this._crashes.add(crash.crash_id);
                newCrashes.push(crash);
                addedCounter += 1;
            }
        });

        if (!!this._finishedPollingCallback) {
            this._finishedPollingCallback(newCrashes);
        }

        this.updateQuery();
    };

    updateQuery() {
        this._startTime = moment().unix();
        this._endTime = (this._startTime + 20*60);
        const query = 'SELECT * FROM `' + dbConstants.tables.crashes + '`' + ' WHERE `date` > '
            + this._startTime.toString() + ' AND ' + '`date` < ' + this._endTime.toString();
        this.setQueryString(query);
        super.updateQuery();
    };
}

module.exports = CrashPoller;
