'use strict';

const _ = require('underscore');
const fs = require('fs');


const requiredParams = [
    'LINUS_LOG_PATH',
    'LUCY_WRITE_QUEUE_URL',
    'LUCY_READ_QUEUE_URL',
    'AWS_ACCESS_KEY_ID',
    'AWS_SECRET_KEY',
    'AWS_REGION'
];

for (let i=0; i < requiredParams.length; i++) {
    if (!_.has(process.env, requiredParams[i])) {
        console.log(
            'Error: environment variables have not been properly setup for Linus.',
            'The variable:',
            requiredParams[i],
            'was not found.'
        );

        throw new Error('Environment Variables Not Properly Set');
    }
}

module.exports = {
    appName: 'linus',

    paths: {
        logPath: process.env.LINUS_LOG_PATH
    },

    aws: {
        keyId: process.env.AWS_ACCESS_KEY_ID,
        key: process.env.AWS_SECRET_KEY,
        region: process.env.AWS_REGION,
        lucyQueues: {
            write: process.env.LUCY_WRITE_QUEUE_URL,
            read: process.env.LUCY_READ_QUEUE_URL
        }
    }
};
