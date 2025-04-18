'use strict'

const platform = require('@velo-labs/platform')
const query = platform.queryCreator
const sqlPool = platform.sqlPool
const dbConstants = platform.dbConstants

/**
 * Used to get theft.
 *
 * @param {Object} columnsAndValues - columnsAndValues to fetch thefts.
 * @param {function} done - Callback
 */
const getTheft = (columnsAndValues, done) => {
  const getTheftQuery = query.selectWithAnd(dbConstants.tables.thefts, null, columnsAndValues)
  sqlPool.makeQuery(getTheftQuery, done)
}

module.exports = Object.assign(module.exports, {
  getTheft: getTheft
})
