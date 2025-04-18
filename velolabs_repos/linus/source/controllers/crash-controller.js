'use strict';

const platform = require('@velo-labs/platform');
const logger = platform.logger;
const dbConstants = platform.dbConstants;
const sqsConstants = platform.sqsConstants;
const config = require('./../config');
const _ = require('underscore');
const CrashPoller = require('./../handlers/crash-poller');
const SQSSender = platform.SQSSender;


class CrashController {
    /**
     * This method initializes a crash controller
     */
    constructor() {
        this._crashPoller = new CrashPoller(dbConstants.tables.crashes);
        this._sqsSender = new SQSSender(
            sqsConstants.lucy.queues.read,
            'crash',
            'crash_id',
            [
                'crash_id',
                'date',
                'message_sent',
                'lock_id',
                'user_id',
                'type',
                'operator_id'
            ],
            500
        );
    };

    /**
     * This method starts the controller.
     */
    start() {
        this._crashPoller.startPolling((crashes) => {
            this._handleCrashData(crashes);
        });

        this._sqsSender.start();
    };

    /**
     * This method parses and handles data that is returned from the database poller.
     *
     * @param {Array} crashes
     * @private
     */
    _handleCrashData(crashes) {
        this._sqsSender.addObjects(crashes);
    };
}

module.exports = CrashController;
