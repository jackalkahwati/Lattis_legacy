'use strict'

const countryCodeHandler = require('./../handlers/country-code-handler')
const _ = require('underscore')

module.exports = {
  /**
     * This method formats a phone number for a given country code.
     *
     * @param {object} phoneNumberInfo
     * @returns {null || string}
     */
  formatPhoneNumber: function (phoneNumberInfo) {
    if (!_.has(phoneNumberInfo, 'phone_number')) {
      return null
    }

    if (!this.isPhoneNumberFormatted(phoneNumberInfo.phone_number) &&
            _.has(phoneNumberInfo, 'country_code')
    ) {
      if (phoneNumberInfo.country_code) {
        const prefix = countryCodeHandler.getCallingCode(phoneNumberInfo.country_code)
        if (prefix) {
          return prefix + phoneNumberInfo.phone_number
        }
      } else {
        return '+1' + phoneNumberInfo.phone_number
      }
    }

    return phoneNumberInfo.phone_number
  },

  /**
     * This method checks if a phone number has been formatted.
     *
     * @param phoneNumber
     * @returns {boolean}
     */
  isPhoneNumberFormatted: function (phoneNumber) {
    return phoneNumber.includes('+')
  }
}
