const knex = require('knex')
const { logger } = require('../../utils/error-handler')
const DatabaseUtils = require('../utils')
const config = require('./knexfile')

// Initialize database connection
const db = knex(config)

// Setup connection event handlers
DatabaseUtils.setupConnectionEvents(db)

// Verify connection
async function verifyConnection() {
  try {
    await db.raw('SELECT 1')
    logger.info('Database connection established successfully')
    return true
  } catch (error) {
    const { isFatalError, error: handledError } = DatabaseUtils.handleDatabaseError(error)
    
    if (isFatalError) {
      logger.error('Fatal database connection error:', handledError)
      process.exit(1)
    }
    
    return false
  }
}

// Periodic connection check
if (process.env.NODE_ENV === 'production' || process.env.CHECK_DB === '1') {
  const CHECK_INTERVAL = 30000 // 30 seconds
  setInterval(async () => {
    const isConnected = await verifyConnection()
    if (!isConnected) {
      logger.warn('Database connection check failed')
    }
  }, CHECK_INTERVAL)
} else {
  logger.info('Skipping periodic DB connection checks in development mode')
}

// Export initialized database instance
module.exports = db

// Handle process termination
process.on('SIGINT', async () => {
  try {
    logger.info('Closing database connections...')
    await db.destroy()
    logger.info('Database connections closed')
    process.exit(0)
  } catch (error) {
    logger.error('Error closing database connections:', error)
    process.exit(1)
  }
})

// Handle uncaught errors
process.on('unhandledRejection', (error) => {
  logger.error('Unhandled promise rejection in database:', error)
})

process.on('uncaughtException', (error) => {
  logger.error('Uncaught exception in database:', error)
  process.exit(1)
})
