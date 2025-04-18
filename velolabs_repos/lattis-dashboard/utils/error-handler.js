const winston = require('winston')
const { format } = winston
const path = require('path')

// Custom log format
const customFormat = format.combine(
  format.timestamp(),
  format.errors({ stack: true }),
  format.metadata(),
  format.json()
)

// Create logger instance
const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: customFormat,
  defaultMeta: { service: 'lattis-dashboard' },
  transports: [
    // Write all errors to error.log
    new winston.transports.File({
      filename: path.join(__dirname, '../logs/error.log'),
      level: 'error',
      maxsize: 5242880, // 5MB
      maxFiles: 5,
      tailable: true
    }),
    // Write all logs to combined.log
    new winston.transports.File({
      filename: path.join(__dirname, '../logs/combined.log'),
      maxsize: 5242880, // 5MB
      maxFiles: 5,
      tailable: true
    })
  ],
  // Handle exceptions and rejections
  exceptionHandlers: [
    new winston.transports.File({
      filename: path.join(__dirname, '../logs/exceptions.log'),
      maxsize: 5242880, // 5MB
      maxFiles: 5,
      tailable: true
    })
  ],
  rejectionHandlers: [
    new winston.transports.File({
      filename: path.join(__dirname, '../logs/rejections.log'),
      maxsize: 5242880, // 5MB
      maxFiles: 5,
      tailable: true
    })
  ]
})

// Add console transport in development
if (process.env.NODE_ENV !== 'production') {
  logger.add(new winston.transports.Console({
    format: format.combine(
      format.colorize(),
      format.simple()
    )
  }))
}

// Error types
const ErrorTypes = {
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  AUTHENTICATION_ERROR: 'AUTHENTICATION_ERROR',
  AUTHORIZATION_ERROR: 'AUTHORIZATION_ERROR',
  NOT_FOUND: 'NOT_FOUND',
  CONFLICT: 'CONFLICT',
  INTERNAL_ERROR: 'INTERNAL_ERROR',
  EXTERNAL_SERVICE_ERROR: 'EXTERNAL_SERVICE_ERROR'
}

class AppError extends Error {
  constructor(type, message, statusCode, details = null) {
    super(message)
    this.type = type
    this.statusCode = statusCode
    this.details = details
    Error.captureStackTrace(this, this.constructor)
  }

  toJSON() {
    return {
      type: this.type,
      message: this.message,
      ...(this.details && { details: this.details }),
      ...(process.env.NODE_ENV === 'development' && { stack: this.stack })
    }
  }
}

// Error factory functions
const createError = {
  validation: (message, details) => new AppError(ErrorTypes.VALIDATION_ERROR, message, 400, details),
  authentication: (message) => new AppError(ErrorTypes.AUTHENTICATION_ERROR, message, 401),
  authorization: (message) => new AppError(ErrorTypes.AUTHORIZATION_ERROR, message, 403),
  notFound: (message) => new AppError(ErrorTypes.NOT_FOUND, message, 404),
  conflict: (message) => new AppError(ErrorTypes.CONFLICT, message, 409),
  internal: (message, details) => new AppError(ErrorTypes.INTERNAL_ERROR, message, 500, details),
  external: (message, details) => new AppError(ErrorTypes.EXTERNAL_SERVICE_ERROR, message, 502, details)
}

// Error middleware
const errorHandler = (err, req, res, next) => {
  // Log error
  logger.error({
    error: err.message,
    stack: err.stack,
    type: err.type || 'UNKNOWN_ERROR',
    path: req.path,
    method: req.method,
    ip: req.ip,
    userId: req.user?.id
  })

  // If headers already sent, delegate to Express's default error handler
  if (res.headersSent) {
    return next(err)
  }

  // Handle AppError instances
  if (err instanceof AppError) {
    return res.status(err.statusCode).json(err.toJSON())
  }

  // Handle validation errors (e.g., from Joi)
  if (err.isJoi) {
    const validationError = createError.validation(
      'Validation error',
      err.details.map(detail => detail.message)
    )
    return res.status(400).json(validationError.toJSON())
  }

  // Handle unknown errors
  const internalError = createError.internal(
    process.env.NODE_ENV === 'production' 
      ? 'An unexpected error occurred'
      : err.message
  )
  res.status(500).json(internalError.toJSON())
}

// Async handler wrapper
const asyncHandler = (fn) => (req, res, next) => {
  Promise.resolve(fn(req, res, next)).catch(next)
}

module.exports = {
  logger,
  ErrorTypes,
  AppError,
  createError,
  errorHandler,
  asyncHandler
}
