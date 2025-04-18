'use strict'

/*
 * skyfleet's convertions object
 */
module.exports = {

  metersToMiles: function (metres) {
    return metres * 0.000621371192
  },

  metersToKilometers: (meters) => {
    return meters / 1000
  },

  milesToMetres: function (miles) {
    return miles * 1609.344
  },

  millisecondsToMinutes: function (ms) {
    return Math.floor(ms / 60000)
  },

  millisecondsToSeconds: function (ms) {
    return ((ms % 60000) / 1000).toFixed(0)
  },

  secondsToMinutes: function (seconds) {
    seconds = seconds || 0
    var secondsConversion = ((seconds) % 60).toFixed(0)
    var minutes = Math.floor(((seconds / 60) % 60))
    var hours = ((seconds / (60 * 60)))
    hours < 1 ? hours = 0 : hours = hours.toFixed(0)
    return (hours < 10 ? '0' : '') + hours + ':' +
            (minutes < 10 ? '0' : '') + minutes + ':' +
            (secondsConversion < 10 ? '0' : '') + secondsConversion
  },
  secondsToHours: function (seconds) {
    let hours = ((seconds / (60 * 60)) % 24)
    hours < 1 ? hours = 0 : hours = hours.toFixed(0)
    return hours
  },

  daysInMonth: function (month, year) {
    return new Date(year, month, 0).getDate()
  },

  getDaysDiff: function (startTime, endTime) {
    let timeDiff = Math.abs(endTime - startTime)
    return Math.floor(timeDiff / (3600 * 24))
  }
}
