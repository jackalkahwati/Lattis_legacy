'use strict';

const DBPoller = require('./db-poller');
const logger = require('@velo-labs/platform').logger;


class Scheduler {
    constructor(table, interval=undefined) {
        this._poller = new DBPoller(table, interval);
    };

    start() {
        logger('The method `start` should be overridden in the child class implementation');
    }
}

module.exports = Scheduler;
