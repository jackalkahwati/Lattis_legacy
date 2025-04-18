'use strict';

const platform = require('@velo-labs/platform');
const CrashController = require('./controllers/crash-controller');
const MaintenanceController = require('./controllers/maintenance-controller');


module.exports.run = () => {
    // const maintenanceController = new MaintenanceController();
    // maintenanceController.start();

    const crashController = new CrashController();
    crashController.start();
};
