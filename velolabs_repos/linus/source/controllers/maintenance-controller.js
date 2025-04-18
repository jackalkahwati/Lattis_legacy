'use strict';

const platform = require('@velo-labs/platform');
const logger = platform.logger;
const dbConstants = platform.dbConstants;
const sqsConstants = platform.sqsConstants;
const config = require('./../config');
const _ = require('underscore');
const MaintenancePoller = require('./../handlers/maintenance-poller');
const SQSSender = platform.SQSSender;


class MaintenanceController {
    /**
     * This method initializes a maintenance controller
     */
    constructor() {
        this._maintenancePoller = new MaintenancePoller(dbConstants.tables.bikes);
        this._sqsSender = new SQSSender(
            sqsConstants.lucy.queues.read,
            'bikes',
            'bike_id',
            [
                'bike_id',
                'bike_name',
                'model',
                'status',
                'current_status',
                'maintenance_status',
                'distance',
                'lock_id',
                'bike_id',
                'fleet_id'
            ],
            5000

        );
    };

    /**
     * This method starts the controller.
     */
    start() {
      this._maintenancePoller.startPolling((maintenanceData) => {
          this._handleMaintenanceData(maintenanceData);
      });

      this._sqsSender.start();
    };


    /**
     * This method parses and handles data that is returned from the database poller.
     * @param {Array} maintenanceData
     * @private
     */
    _handleMaintenanceData(maintenanceData) {
        this._sqsSender.addObjects(maintenanceData);
    };
}


module.exports = MaintenanceController;

