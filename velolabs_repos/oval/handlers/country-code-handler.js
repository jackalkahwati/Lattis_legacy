'use strict'

const _ = require('underscore')
const callingCountries = require('country-data').callingCountries

module.exports = {
  /**
     * This object holds a key value pair of the country code and it's calling code.
     */
  _info: null,

  /**
     * This method fills an empty `_info` object or returns the already populated object.
     *
     * @returns {object}
     * @private
     */
  _getInfo: function () {
    if (this._info) {
      return this._info
    }

    this._info = {}
    _.each(callingCountries.all, function (codeInfo) {
      if (codeInfo.countryCallingCodes.length > 0) {
        this._info[codeInfo.alpha2.toLowerCase()] = codeInfo.countryCallingCodes[0]
      }
    }, this)

    return this._info
  },

  /**
     * This method returns a calling code for a given country code. If the country code
     * is not in the `_info` object, null is returned.
     *
     * @param countryCode
     * @returns {string || null}
     */
  getCallingCode (countryCode) {
    const info = this._getInfo()
    const lowerCaseCode = countryCode.toLowerCase()
    if (_.has(info, lowerCaseCode)) {
      return info[lowerCaseCode]
    }

    return null
  }
}
