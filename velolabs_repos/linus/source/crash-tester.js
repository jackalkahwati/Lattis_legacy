'use strict';

const platform = require('@velo-labs/platform');
const _ = require('underscore');
const logger = platform.logger;
const moment = require('moment');
const sqlPool = platform.sqlPool;
const queryCreator = platform.queryCreator;
const async = require('async');
const dbConstants = platform.dbConstants;


class CrashTester {
    constructor() {
        this._crashes = [];
        this._numberOfCrashes = 2000;
    };

    _createCrashes() {
        let crashes = [];
        for (let i=0; i < this._numberOfCrashes; i++) {
            crashes.push({
                date: moment().unix() + Math.floor(Math.random() * 3600),
                message_sent: false,
                x_ave: Math.floor(Math.random() * 1600),
                y_ave: Math.floor(Math.random() * 1600),
                z_ave: Math.floor(Math.random() * 1600),
                x_dev: Math.floor(Math.random() * 1600),
                y_dev: Math.floor(Math.random() * 1600),
                z_dev: Math.floor(Math.random() * 1600),
                latitude: 37.300769,
                longitude: -120.481452,
                lock_id: 1,
                user_id: 1,
                trip_id: 1,
                operator_id: 1
            });
        }

        this._crashes = crashes;
    };

    _saveCrashes() {
        let asyncFunctions = [];
        _.each(this._crashes, (crash, index) => {
            const query = queryCreator.insertSingle(dbConstants.tables.crashes, crash);
            asyncFunctions.push((callback) => {
                sqlPool.makeQuery(query, (error, data) => {
                    if (error) {
                       logger('Failed to insert crash:', crash);
                       callback(error);
                       return;
                    }

                    crash.crash_id = data.insertId;
                    this._crashes[index] = crash;
                    callback();
                });
            });
        });

        async.series(asyncFunctions, (error) => {
            !!error ? logger('Failed to save crashes with error:', error)
                : logger('successfully saved', this._crashes.length, 'crashes');
            sqlPool.destroyPools((error) => {
                if (error) {
                    logger('Failed to destroy sql pools with error:', error);
                }
            });
        });
    };

    run() {
        this._createCrashes();
        this._saveCrashes();
    };
}

const crashTester = new CrashTester();
crashTester.run();
