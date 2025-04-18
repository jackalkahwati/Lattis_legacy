const DatabaseUtils = require('../utils')
const envConfig = require('../../envConfig')

// Get database parameters from environment or parameter store
const dbParams = {
  DB_HOST: envConfig('LATTIS_MAIN_DB_HOST', 'localhost'),
  DB_PORT: envConfig('LATTIS_MAIN_DB_PORT', '3306'),
  DB_USERNAME: envConfig('LATTIS_MAIN_DB_USERNAME', 'root'),
  DB_PASSWORD: envConfig('LATTIS_MAIN_DB_PASSWORD', ''),
  DB_NAME: envConfig('LATTIS_MAIN_DB_NAME', 'lattis'),
  DB_SSL_CA: envConfig('LATTIS_MAIN_DB_SSL_CA')
}

// Create and validate connection config
const connection = DatabaseUtils.createConnectionConfig(dbParams)

// Get default configuration with our connection
const config = DatabaseUtils.getDefaultKnexConfig(connection)

// Environment specific overrides
const environments = {
  development: {
    ...config,
    debug: true,
    asyncStackTraces: true,
    pool: {
      ...config.pool,
      min: 2,
      max: 5
    }
  },
  test: {
    ...config,
    connection: {
      ...connection,
      database: `${connection.database}_test`
    },
    pool: {
      ...config.pool,
      min: 1,
      max: 3
    }
  },
  production: {
    ...config,
    debug: false,
    asyncStackTraces: false,
    pool: {
      ...config.pool,
      min: 5,
      max: 30
    }
  }
}

// Export environment specific configuration
const environment = process.env.NODE_ENV || 'development'
module.exports = environments[environment] || environments.development
