'use strict'

const _ = require('underscore')
const socketConstants = require('@velo-labs/platform').socketConstants

const extendedConstants = {
  operatorClickedNotification: 'operatorClickedNotification',
  operatorNotificationCreated: 'operatorNotificationCreated'
}

module.exports = _.extend(socketConstants, extendedConstants)
