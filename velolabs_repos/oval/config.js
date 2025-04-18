'use strict'
const _ = require('underscore')
const envConfig = require('./envConfig')
const conversions = require('./utils/conversions')

const requiredEnvParams = [
  'OVAL_PORT',
  'OVAL_MODE'
]

const requiredAWSParams = [
  'OVAL_LOG_PATH',
  'OVAL_TWILIO_ACCOUNT_SID',
  'OVAL_TWILIO_API_TOKEN',
  'OVAL_TWILIO_FROM_PHONE_NUMBER',
  'OVAL_KEY_MASTER_HOST',
  'OVAL_KEY_MASTER_PORT',
  'OVAL_FCM_KEY',
  'LUCY_READ_QUEUE_URL',
  'LUCY_WRITE_QUEUE_URL',
  'LATTIS_SUPPORT_PHONE_NUMBER'
]

const optionalParams = [
  'LATTIS_ANDROID_ELLIPSE_APP_VERSION',
  'LATTIS_ANDROID_LATTIS_APP_VERSION'
]

const envNotSet = []
for (let i = 0; i < requiredAWSParams.length; i++) {
  if (!envConfig(requiredAWSParams[i])) {
    envNotSet.push(requiredAWSParams[i])
  }
}

if (envNotSet.length !== 0) {
  console.log(
    'Error: environment variables have not been properly setup.',
    'The variables:',
    envNotSet,
    'was not found.'
  )
  throw new Error('Oval Environment Variables Not Properly Set')
}

for (let i = 0; i < optionalParams.length; i++) {
  if (!envConfig(optionalParams[i])) {
    console.log(
      'Warning: environment variables have not been properly setup.',
      'The variable:',
      optionalParams[i],
      'was not found.'
    )
  }
}

for (let i = 0; i < requiredEnvParams.length; i++) {
  if (!_.has(process.env, requiredEnvParams[i])) {
    console.log(
      'Error: environment variables have not been properly setup.',
      'The variable:',
      requiredEnvParams[i],
      'was not found.'
    )

    throw new Error('Environment Variables Not Properly Set')
  }
}

for (let i = 0; i < requiredAWSParams.length; i++) {
  if (!envConfig(requiredAWSParams[i])) {
    console.log(
      'Error: environment variables have not been properly setup.',
      'The variable:',
      requiredAWSParams[i],
      'was not found.'
    )

    throw new Error('Environment Variables Not Properly Set')
  }
}

module.exports = {
  appName: 'oval',

  superAdminPassword: envConfig('LATTIS_SUPER_ADMIN_PASSWORD'),

  maxNumberOfSharedLocks: 1,

  // Bike booking expiration from booked time(in seconds)
  bikeBookingExpiration: 900,

  // Search range for searching nearby bikes and parking spots
  lattisSearchRange: 3000,

  lattisNearestRange: 5000,

  lattisSupportPhoneNumber: envConfig('LATTIS_SUPPORT_PHONE_NUMBER'),

  parkingSpotRange: 50,

  lockBatteryLogLimit: 3000,

  android: {
    ellipseVersion: envConfig('LATTIS_ANDROID_ELLIPSE_APP_VERSION'),
    lattisVersion: envConfig('LATTIS_ANDROID_LATTIS_APP_VERSION')
  },

  paths: {
    logPath: envConfig('OVAL_LOG_PATH')
  },

  port: process.env.OVAL_PORT,

  mode: process.env.OVAL_MODE,

  twilio: {
    accountSID: envConfig('OVAL_TWILIO_ACCOUNT_SID'),
    apiToken: envConfig('OVAL_TWILIO_API_TOKEN'),
    fromPhoneNumber: envConfig('OVAL_TWILIO_FROM_PHONE_NUMBER')
  },

  keyMaster: {
    host: envConfig('OVAL_KEY_MASTER_HOST'),
    path: '/api/v1/signed-message',
    port: envConfig('OVAL_KEY_MASTER_PORT')
  },

  firebase: {
    messaging: {
      host: 'https://fcm.googleapis.com',
      path: '/fcm/send',
      key: envConfig('OVAL_FCM_KEY')
    }
  },

  fleets: {
    defaultCurrency: conversions.currencies.USD
  }
}
