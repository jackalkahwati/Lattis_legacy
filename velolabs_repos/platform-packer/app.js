'use strict';

const _ = require('underscore');
const exec = require('child_process').exec;
const path = require('path');
const jsonFile = require('jsonfile');
const async = require('async');


const requiredParams = [
    'LATTIS_PLATFORM_TAR_DIR_PATH',
    'LATTIS_PLATFORM_PATH'
];

const basePaths = {
    oval: 'OVAL_BASE_PATH',
    dashboard: 'LATTIS_DASHBOARD_BASE_PATH',
    migrator: 'DB_MIGRATOR_PATH',
    linus: 'LINUS_BASE_PATH'
};

const getAppName = function() {
    if (process.argv.length <= 2) {
        throw new Error('Error: no app name supplied');
    }

    const apps = appNames();
    const appName = process.argv[process.argv.length - 1];
    if (!_.has(apps, appName)) {
        console.log(
            'Incorrect app name supplied. Valid names are:',
            apps,
            _.keys(apps),
            appName,
            'is not valid'
        );
        throw new Error('Incorrect app name supplied');
    }

    return appName;
};

const targetAppPath = function(appName) {
    const apps = appNames();
    let path;
    switch (appName) {
        case apps.oval:
            path = _.has(process.env, basePaths.oval) ? process.env[basePaths.oval]
                : pathError(basePaths.oval);
            break;
        case apps.dashboard:
            path = _.has(process.env, basePaths.dashboard) ? process.env[basePaths.dashboard]
                : pathError(basePaths.dashboard);
            break;
        case apps.migrator:
            path = _.has(process.env, basePaths.migrator) ? process.env[basePaths.migrator]
                : pathError(basePaths.migrator);
            break;
        case apps.linus:
            path = _.has(process.env, basePaths.linus) ? process.env[basePaths.linus]
                : pathError(basePaths.linus);
            break;
        default:
            console.log('The supplied app name is not in:', _.keys(apps));
            throw new Error('Invalid App Name Supplied');
    }

    return path;
};

const pathError = function(path) {
    throw new Error(path + ' path is not set')
};

const appNames = function() {
    const keys = _.keys(basePaths);
    const names = {};
    _.each(keys, (key) => {
        names[key] = key;
    });

    return names;
};

const checkParams = function() {
    _.each(requiredParams, (requiredParam) => {
        if (!_.has(process.env, requiredParam)) {
            console.log('Error: environment vars not set properly. Missing:', requiredParam);
            throw new Error('Environment vars not set properly');
        }
    });

    _.each(_.values(basePaths), (basePath) => {
        if (!_.has(process.env, basePath)) {
            console.log('Warning: environment var:', basePath, 'is not set.');
        }
    });
};

const run = function() {
    checkParams();
    const packageFile = jsonFile.readFileSync(path.join(process.env.LATTIS_PLATFORM_PATH, 'package.json'));
    const version = packageFile.version;
    const packageName = 'velo-labs-platform-' + version + '.tgz';
    const appName = getAppName();
    const targetBasePath = targetAppPath(appName);

    const commands = [
        'cd ' + process.env.LATTIS_PLATFORM_PATH,
        'npm pack',
        'mv ' + packageName + ' ' + process.env.LATTIS_PLATFORM_TAR_DIR_PATH,
        'cd ' + targetBasePath,
        'pwd',
        'npm install --save-dev ' + path.join(process.env.LATTIS_PLATFORM_TAR_DIR_PATH, packageName)
    ];

    const command = commands.join(' && ');
    exec(command, (error, stdout, stderr) => {
        if (error) {
            console.log('output:', stdout);
            console.log('Error: for command:', stderr);
            return;
        }

        console.log(command, 'was made successfully. Output:', stdout);
    });
};

run();
