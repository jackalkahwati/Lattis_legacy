'use strict';

const logger = require('@velo-labs/platform').logger;
const config = require('./source/config');
const application = require('./source/application');


if (process.argv.length > 2 && process.argv[process.argv.length - 1] === 'test') {

} else {
    application.run();
}
