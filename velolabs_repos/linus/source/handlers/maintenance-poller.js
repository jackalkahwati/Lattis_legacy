'use strict';

const platform = require('@velo-labs/platform');
const Poller = require('./db-poller');
const logger = platform.logger;
const dbConstants = platform.dbConstants;
const _ = require('underscore');



class MaintenancePoller extends Poller {
    constructor(table, interval=5000) {
        super(table, interval);
        this._counter = 0;
        this._maintenanceData = new Set();
        this._finishedPollingCallback = null;
    };

    startPolling(callback=null) {
        if (!!callback) {
            this._finishedPollingCallback = callback;
        }

        this.updateQuery();
        this.poll((error, maintenanceData) => {
            if (error) {
                logger('Error: maintenance poller failed with error:', error);
                return;
            }

            if (maintenanceData && maintenanceData.length > 0) {
                this._handleMaintenaceData(maintenanceData);
            }
        });
    };

    _handleMaintenaceData(maintenanceData) {
        this.updateQuery();
        let removedCounter = 0;
        let addedCounter = 0;
        let newMaintenance = [];
        maintenanceData.forEach((maintenance) => {
            if (this._maintenanceData.has(maintenance.bike_id)) {
                this._maintenanceData.delete(maintenance.bike_id);
                removedCounter += 1;
            } else if (!this._maintenanceData.has(maintenance.bike_id)) {
                this._maintenanceData.add(maintenance.bike_id);
                newMaintenance.push(maintenance);
                addedCounter += 1;
            }
        });

        if (!!this._finishedPollingCallback) {
            this._finishedPollingCallback(newMaintenance);
        }
        this.updateQuery();
        this._counter += 1;
    };


    updateQuery() {
      this._maintenanceMeters = 80000;
      const query = 'SELECT * FROM `' + dbConstants.tables.bikes + '`' +
          ' WHERE `distance` - `maintenance_distance` >= ' + this._maintenanceMeters;
      this.setQueryString(query);
      super.updateQuery();
    };
}

module.exports = MaintenancePoller;

