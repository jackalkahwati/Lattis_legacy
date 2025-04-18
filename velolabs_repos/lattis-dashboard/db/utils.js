const { logger } = require('../utils/error-handler')

/**
 * Database connection configuration utility
 */
class DatabaseUtils {
  static validateConfig(config) {
    const requiredFields = ['host', 'port', 'user', 'database']
    const missingFields = requiredFields.filter(field => !config[field])
    
    if (missingFields.length > 0) {
      throw new Error(`Missing required database configuration fields: ${missingFields.join(', ')}`)
    }

    // Password can be empty string in development
    if (process.env.NODE_ENV !== 'development' && config.password === undefined) {
      throw new Error('Missing required database configuration field: password')
    }

    // Validate port number
    if (isNaN(parseInt(config.port))) {
      throw new Error('Database port must be a number')
    }

    return true
  }

  static getDefaultKnexConfig(connection) {
    return {
      client: 'mysql2',
      version: '5.7',
      connection,
      pool: {
        min: 2,
        max: 10,
        createTimeoutMillis: 30000,
        acquireTimeoutMillis: 30000,
        idleTimeoutMillis: 30000,
        reapIntervalMillis: 1000,
        createRetryIntervalMillis: 100,
        propagateCreateError: false
      },
      acquireConnectionTimeout: 60000,
      migrations: {
        tableName: 'migrations',
        directory: './migrations'
      },
      debug: process.env.NODE_ENV === 'development',
      asyncStackTraces: process.env.NODE_ENV === 'development',
      wrapIdentifier: (value, origImpl) => origImpl(value),
      postProcessResponse: (result) => result,
      log: {
        warn(message) {
          logger.warn(message)
        },
        error(message) {
          logger.error(message)
        },
        deprecate(message) {
          logger.warn(message)
        },
        debug(message) {
          if (process.env.NODE_ENV === 'development') {
            logger.debug(message)
          }
        }
      }
    }
  }

  static handleDatabaseError(error) {
    // Log the error with appropriate level and details
    logger.error('Database error:', {
      code: error.code,
      errno: error.errno,
      sqlState: error.sqlState,
      sqlMessage: error.sqlMessage,
      sql: process.env.NODE_ENV === 'development' ? error.sql : undefined
    })

    // Determine if error is recoverable
    const fatalErrors = ['PROTOCOL_CONNECTION_LOST', 'ER_CON_COUNT_ERROR', 'ECONNREFUSED']
    const isFatalError = fatalErrors.includes(error.code)

    if (isFatalError) {
      logger.error('Fatal database error occurred, connection may need to be re-established')
    }

    return {
      isFatalError,
      error: {
        message: process.env.NODE_ENV === 'production' 
          ? 'A database error occurred' 
          : error.message,
        code: error.code
      }
    }
  }

  static createConnectionConfig(params) {
    // For development, use environment variables if AWS params not available
    const getParam = (key, defaultValue) => {
      if (process.env.NODE_ENV === 'development') {
        return process.env[key] || defaultValue
      }
      return params[key] || defaultValue
    }

    const config = {
      host: getParam('DB_HOST', 'localhost'),
      port: parseInt(getParam('DB_PORT', '3306')),
      user: getParam('DB_USERNAME', 'root'),
      password: getParam('DB_PASSWORD', ''),
      database: getParam('DB_NAME', 'lattis'),
      charset: 'utf8mb4',
      timezone: 'UTC',
      dateStrings: true,
      connectTimeout: 30000,
      ssl: process.env.NODE_ENV === 'production' ? {
        rejectUnauthorized: true,
        ca: getParam('DB_SSL_CA')
      } : undefined
    }

    this.validateConfig(config)
    return config
  }

  static setupConnectionEvents(knex) {
    // Handle connection errors
    knex.on('query-error', (error, query) => {
      logger.error('Query error:', {
        error: error.message,
        query: process.env.NODE_ENV === 'development' ? query : undefined
      })
    })

    // Pool events
    knex.client.pool.on('createSuccess', () => {
      logger.debug('Database connection created successfully')
    })

    knex.client.pool.on('error', (err) => {
      logger.error('Pool error:', err)
    })

    // Graceful shutdown handler
    process.on('SIGTERM', () => {
      logger.info('SIGTERM received, closing database connections...')
      knex.destroy(() => {
        logger.info('Database connections closed')
        process.exit(0)
      })
    })

    return knex
  }
}

module.exports = DatabaseUtils
