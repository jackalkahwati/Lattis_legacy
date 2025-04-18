'use strict'

const moment = require('moment')
require('moment-timezone')

/**
 * To convert time to minutes with given timezone offset
 *
 * @param {String} timezone - Number to be rounded off
 */
const getTotalMinutesForTimeZone = (timezone) => {
  const hours = moment.unix(moment().unix()).tz(timezone).format('H')
  const minutes = moment.unix(moment().unix()).tz(timezone).format('M')
  return (parseInt(hours) * 60) + parseInt(minutes)
}

/**
 * To take ceiling of pricing fraction values
 *
 * @param {Number} price - Number to be rounded off
 */
const roundOffPricing = (price) => {
  const p = (price * 100).toFixed()
  return (Math.ceil(parseFloat(p))) / 100
}

/**
 * Convert hex string to Uint8Array
 *
 * @param {String} hexString - hex string
 */
const hexStringToByteArray = (hexString) => {
  var result = []
  while (hexString.length >= 2) {
    result.push(parseInt(hexString.substring(0, 2), 16))
    hexString = hexString.substring(2, hexString.length)
  }
  return new Uint8Array(result)
}

const currencies = {
  USD: 'USD',
  EUR: 'EUR',
  GBP: 'GBP',
  CAD: 'CAD',
  DKK: 'DKK',
  SEK: 'SEK',
  HUF: 'HUF',
  CLP: 'CLP',
  PEN: 'PEN',
  COP: 'COP',
  BRL: 'BRL'
}

const currencyCodeSymbolMap = {
  EUR: '€',
  USD: '$',
  GBP: '£',
  CAD: 'C$',
  HUF: 'Ft',
  CLP: 'CLP$',
  PEN: 'S/',
  COP: 'COL$',
  BRL: 'R$'
}

const currencyCodeToSymbol = (code) => {
  const currencyCode = (code || '').toUpperCase()

  return currencyCodeSymbolMap[currencyCode] || currencyCode
}

module.exports = {
  getTotalMinutesForTimeZone: getTotalMinutesForTimeZone,
  roundOffPricing: roundOffPricing,
  hexStringToByteArray: hexStringToByteArray,
  currencyCodeToSymbol,
  currencyCodeSymbolMap,
  currencies
}
