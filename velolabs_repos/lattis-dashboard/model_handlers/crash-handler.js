'use strict'

const platform = require('@velo-labs/platform')
const query = platform.queryCreator
const sqlPool = platform.sqlPool
const dbConstants = platform.dbConstants

/**
 * Used to get crash.
 *
 * @param {Object} columnsAndValues - columnsAndValues to fetch crashes.
 * @param {function} done - Callback
 */
const getCrash = (columnsAndValues, done) => {
  const getCrashQuery = query.selectWithAnd(dbConstants.tables.crashes, null, columnsAndValues)
  sqlPool.makeQuery(getCrashQuery, done)
}

module.exports = Object.assign(module.exports, {
  getCrash: getCrash
})
