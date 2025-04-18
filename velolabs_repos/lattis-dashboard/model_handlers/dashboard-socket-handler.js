'use strict'

const platform = require('@velo-labs/platform')
const SocketHandler = platform.SocketHandler
const socketConstants = require('./../constants/socket-constants')
const _ = require('underscore')
const logger = platform.logger

class DashboardSocketHandler extends SocketHandler {
  constructor (socketServer) {
    super(socketServer, 'operator_id')
  };

  setupSocketServerHooks (socket) {
    logger('setting up dashboard socket server hooks')
    socket.on(socketConstants.operatorClickedNotification, (info) => {
      // whatever needs to happen here, well I guess it should happen here...
    })
  };

  notifyOperators (notifications) {
    let counter = 0
    notifications.forEach((notification) => {
      const operatorId = notification.operator_id.toString()
      if (_.has(this._clients, operatorId)) {
        const socket = this._clients[operatorId]
        socket.emit(socketConstants.operatorNotificationCreated, notification)
        counter += 1
      }
    })

    if (counter > 0) {
      logger('Sending:', counter, 'notifications to operators')
    }
  };
}

module.exports = DashboardSocketHandler
