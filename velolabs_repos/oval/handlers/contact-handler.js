'use strict'

const logger = require('@velo-labs/platform').logger
const twiloHandler = require('./twilo-handler')
const _ = require('underscore')
const async = require('async')
const countryCodeHandler = require('./country-code-handler')
const {configureSentry} = require('../utils/configureSentry')

const Sentry = configureSentry()

class ContactHandler {
  constructor () {
    this._googleMapsUrl = 'http://www.google.com/maps/place/'
  };

  /**
     * This method sends an emergency text to a user's emergency contact.
     *
     * @param {string} userName
     * @param {Array} contacts
     * @param {object} location - keys latitude & longitude
     * @param {function} callback
     */
  sendEmergencyText (userName, contacts, location, callback) {
    let asyncFunctions = []
    let failedContacts = []
    _.each(contacts, function (contact) {
      const sendText = function (completion) {
        if (_.has(contact, 'country_code') &&
                    contact.country_code &&
                    !contact.phone_number.includes('+')
        ) {
          const prefix = countryCodeHandler.getCallingCode(contact.country_code)
          if (prefix) {
            contact.phone_number = prefix + contact.phone_number
          }
        }

        twiloHandler.sendSMS(contact.phone_number, this._message(userName, location), function (error) {
          if (error) {
            Sentry.captureException(error, {
              user_id: contact.user_id,
              phone_number: contact.phone_number,
              country_code: contact.country_code
            })
            logger('Error: failed to send text to', contact, 'Error:', error)
            failedContacts.push(contact)
          }

          completion()
        })
      }.bind(this)

      asyncFunctions.push(sendText)
    }, this)

    async.parallel(asyncFunctions, () => {
      callback(null, failedContacts.length === 0 ? null : failedContacts)
    })
  };

  /**
     * This method creates the message to be sent to the emergency contact.
     *
     * @param {string || null} userName
     * @param {object} location
     * @returns {string}
     * @private
     */
  _message (userName, location) {
    // TODO: In the future, this text should be localized.
    let text
    if (userName) {
      text = userName + "'s Ellipse bike lock has detected a crash. " +
            'Please contact them asap. Their last know location is: ' + this._locationUrl(location)
    } else {
      text = 'An Ellipse smart bike lock user has listed you as an emergency contact. ' +
                "That user's Ellipse has detected a crash. Their last known location is: " +
                this._locationUrl(location)
    }

    return text
  };

  /**
     * This method creates a url to show the user's location on a map.
     *
     * @param {object} location
     * @returns {string}
     * @private
     */
  _locationUrl (location) {
    return this._googleMapsUrl + location.latitude.toString() + ',' + location.longitude.toString()
  }
}

module.exports = ContactHandler
