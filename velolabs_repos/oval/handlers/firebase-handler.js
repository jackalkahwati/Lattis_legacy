'use strict'

const platform = require('@velo-labs/platform')
const config = require('./../config')
const logger = platform.logger
const gcm = require('node-gcm')
const errors = platform.errors
const {configureSentry} = require('../utils/configureSentry')

const Sentry = configureSentry()

class FireBaseHandler {
  /**
     * This method initializes FireBase object.
     */
  constructor () {
    this._host = config.firebase.messaging.host
    this._path = config.firebase.messaging.path
    this._key = config.firebase.messaging.key
    this._gcmSender = new gcm.Sender(config.firebase.messaging.key)
  };

  /**
     * This method sends a push notification to the given recipient.
     *
     * @param {string} recipient
     * @param {string} title
     * @param {string} message
     * @param {function} callback
     */
  sendPushNotification (recipient, title, message, callback) {
    const gcmMessage = new gcm.Message({
      notification: {
        title: title,
        body: message
      },
      priority: 'high',
      contentAvailable: true,
      timeToLive: 60 * 60 * 24
    })
    console.log('***************************************', recipient, gcmMessage)
    this._gcmSender.send(gcmMessage, {registrationTokens: [recipient]}, function (error, response) {
      if (error) {
        Sentry.captureException(error, {
          recipient: recipient
        })
        logger('Error: failed to send push notification to:', recipient, 'Error: error')
        callback(errors.internalServer(false))
        return
      }

      logger('Sent push notification successfully to', recipient, 'Response:', response)
      callback(null)
    })
  };
}

module.exports = FireBaseHandler
