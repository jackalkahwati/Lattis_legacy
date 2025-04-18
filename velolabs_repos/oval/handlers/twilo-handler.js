'use strict'

const config = require('./../config')
const twilioClient = require('twilio')(config.twilio.accountSID, config.twilio.apiToken)

module.exports = {
  /**
     * This method will send an SMS message to a recipient
     *
     * @param {string} toPhoneNumber
     * @param {string} message
     * @param {Function} callback - callback with parameters of (error, returnedData)
     */
  sendSMS: function (toPhoneNumber, message, callback) {
    const params = {
      body: message,
      to: toPhoneNumber,
      from: config.twilio.fromPhoneNumber
    }

    twilioClient.messages.create(params, callback)
  }
}
