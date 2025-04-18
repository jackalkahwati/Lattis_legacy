#!/usr/bin/env node
require('dotenv').config({ path: process.env.NODE_ENV === 'production' ? '.env.prod' : '.env.staging' });

'use strict'

const http = require('http')
const { logger } = require('../utils/error-handler')
const app = require('../app')
const db = require('../db')

// Normalize port
const normalizePort = (val) => {
  const port = parseInt(val, 10)
  if (isNaN(port)) return val
  if (port >= 0) return port
  return false
}

// Get port from environment
const port = normalizePort(process.env.PORT || '3000')
app.set('port', port)

// Create HTTP server
const server = http.createServer(app)

// Handle server errors
const onError = (error) => {
  if (error.syscall !== 'listen') {
    throw error
  }

  const bind = typeof port === 'string' ? `Pipe ${port}` : `Port ${port}`

  switch (error.code) {
    case 'EACCES':
      logger.error(bind + ' requires elevated privileges')
      process.exit(1)
      break
    case 'EADDRINUSE':
      logger.error(bind + ' is already in use')
      process.exit(1)
      break
    default:
      throw error
  }
}

// Server listening handler
const onListening = () => {
  const addr = server.address()
  const bind = typeof addr === 'string' ? 'pipe ' + addr : 'port ' + addr.port
  logger.info('Server listening on ' + bind)
}

// Graceful shutdown handler
const gracefulShutdown = async (signal) => {
  logger.info(`${signal} received. Starting graceful shutdown...`)

  // Create a timeout promise
  const timeout = new Promise((_, reject) => {
    setTimeout(() => {
      reject(new Error('Shutdown timed out'))
    }, 10000) // 10 seconds timeout
  })

  try {
    // Close server first (stop accepting new connections)
    await new Promise((resolve) => {
      server.close(resolve)
    })
    logger.info('Server closed')

    // Close database connections
    await Promise.race([
      Promise.all([
        db.main.destroy(),
        db.users.destroy()
      ]),
      timeout
    ])
    logger.info('Database connections closed')

    // Exit process
    process.exit(0)
  } catch (error) {
    logger.error('Error during shutdown:', error)
    process.exit(1)
  }
}

// Startup sequence
const startServer = async () => {
  try {
    // Verify database connections (skip in dev if DB not available)
    if (process.env.NODE_ENV !== 'development' || process.env.CHECK_DB === '1') {
      logger.info('Verifying database connections...')
      await Promise.all([
        db.main.raw('SELECT 1'),
        db.users.raw('SELECT 1')
      ])
      logger.info('Database connections verified')
    } else {
      logger.info('Skipping DB connectivity check in development mode')
    }

    // Start server
    server.listen(port)
    server.on('error', onError)
    server.on('listening', onListening)

    // Setup shutdown handlers
    process.on('SIGTERM', () => gracefulShutdown('SIGTERM'))
    process.on('SIGINT', () => gracefulShutdown('SIGINT'))

    // Handle uncaught errors
    process.on('unhandledRejection', (error) => {
      logger.error('Unhandled promise rejection:', error)
    })

    process.on('uncaughtException', (error) => {
      logger.error('Uncaught exception:', error)
      gracefulShutdown('UNCAUGHT_EXCEPTION')
    })

  } catch (error) {
    logger.error('Failed to start server:', error)
    process.exit(1)
  }
}

// Start the application
startServer()
