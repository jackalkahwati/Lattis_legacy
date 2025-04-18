'use strict'

const platform = require('@velo-labs/platform')
const query = platform.queryCreator
const sqlPool = platform.sqlPool
const dbConstants = platform.dbConstants

/**
 * Used to get maintenance
 *
 * @param {Object} columnsAndValues - To filter maintenance
 * @param done - Callback
 */
const getMaintenance = (columnsAndValues, done) => {
  const getMaintenanceQuery = query.selectWithAnd(dbConstants.tables.maintenance, null, columnsAndValues)
  sqlPool.makeQuery(getMaintenanceQuery, done)
}

/**
 * Used to update maintenance
 *
 * @param {Object} columnsAndValues - to be updated
 * @param {Object} comparisonColumn - target column to be updated
 * @param done - Callback
 */
const updateMaintenance = (columnsAndValues, comparisonColumn, done) => {
  const updateMaintenanceQuery = query.updateSingle(dbConstants.tables.maintenance, columnsAndValues, comparisonColumn)
  sqlPool.makeQuery(updateMaintenanceQuery, done)
}

/**
 * Used to get theft
 *
 * @param {Object} columnsAndValues - To filter theft
 * @param done - Callback
 */
const getTheft = (columnsAndValues, done) => {
  const getTheftQuery = query.selectWithAnd(dbConstants.tables.thefts, null, columnsAndValues)
  sqlPool.makeQuery(getTheftQuery, done)
}

/**
 * Used to update theft
 *
 * @param {Object} columnsAndValues - to be updated
 * @param done - Callback
 */
const insertTheft = (columnsAndValues, done) => {
  const updateMaintenanceQuery = query.insertSingle(dbConstants.tables.thefts, columnsAndValues)
  sqlPool.makeQuery(updateMaintenanceQuery, done)
}

/**
 * Used to update theft
 *
 * @param {Object} columnsAndValues - to be updated
 * @param {Object} comparisonColumn - target column to be updated
 * @param done - Callback
 */
const updateTheft = (columnsAndValues, comparisonColumn, done) => {
  const updateMaintenanceQuery = query.updateSingle(dbConstants.tables.thefts, columnsAndValues, comparisonColumn)
  sqlPool.makeQuery(updateMaintenanceQuery, done)
}

module.exports = {
  updateMaintenance: updateMaintenance,
  getMaintenance: getMaintenance,
  getTheft: getTheft,
  insertTheft: insertTheft,
  updateTheft: updateTheft
}
