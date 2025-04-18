'use strict';

const config = require('./config.js');

function logger(...args) {
  // Simple wrapper – in real code we might add timestamps or other formatting
  console.log(...args)
}

logger.log = console.log.bind(console)
logger.error = console.error.bind(console)
logger.warn = console.warn.bind(console)
logger.debug = console.debug.bind(console)

const errors = {
  internalServer: (critical) => new Error('Internal Server Error'),
  noError: () => null,
  customError: (msg, code, type, critical) => new Error(msg),
  errorWithMessage: (err) => err.message || String(err),
  resourceNotFound: (critical) => new Error('Resource Not Found'),
};

const responseCodes = {
  BadRequest: 400,
  InternalServer: 500,
  ResourceNotFound: 404,
};

const queryCreator = {
  selectWithAnd: (table, columns, conditions) => `SELECT * FROM ${table}`,
  updateSingle: (table, updates, conditions) => `UPDATE ${table}`,
  insertSingle: (table, values) => `INSERT INTO ${table}`,
  join: (table1, table2, columns, joinColumn, alias, conditions) => `JOIN query between ${table1} and ${table2}`,
  deleteSingle: (table, conditions) => `DELETE FROM ${table}`,
  customQuery: (dbName, query) => query,
};

const sqlPool = {
  makeQuery: (query, callback) => {
    // Dummy implementation: return empty results
    callback(null, []);
  },
};

const tripHelper = {
  getTripDistance: (trip) => 0,
};

const queryHelper = {};

const dbConstants = {};

const mapBoxHandler = {
  getAddressInformationForCoordinate: (coords, done) => {
    done(null, { address: '123 Dummy St', street: 'Dummy Street' });
  },
};

class S3Handler {}

const encryptionHandler = {
  encryptDbValue: (val) => val,
};

module.exports = {
  config,
  logger,
  errors,
  responseCodes,
  queryCreator,
  sqlPool,
  tripHelper,
  queryHelper,
  dbConstants,
  mapBoxHandler,
  S3Handler,
  encryptionHandler,
};
