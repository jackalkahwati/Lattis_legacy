const { logger } = require('./error-handler')

const responseCodes = {
  OK: 200,
  Created: 201,
  BadRequest: 400,
  Unauthorized: 401,
  Forbidden: 403,
  NotFound: 404,
  Conflict: 409,
  InternalServer: 500
}

const errorTypes = {
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  AUTHENTICATION_ERROR: 'AUTHENTICATION_ERROR',
  AUTHORIZATION_ERROR: 'AUTHORIZATION_ERROR',
  NOT_FOUND: 'NOT_FOUND',
  CONFLICT: 'CONFLICT',
  INTERNAL_ERROR: 'INTERNAL_ERROR'
}

class AppError extends Error {
  constructor(type, message, details = null) {
    super(message)
    this.type = type
    this.details = details
    Error.captureStackTrace(this, this.constructor)
  }
}

const errors = {
  noError: () => null,
  
  missingParameter: (logError = false) => {
    const error = new AppError(
      errorTypes.VALIDATION_ERROR,
      'Missing required parameter'
    )
    if (logError) logger.error(error)
    return error
  },

  validationError: (message, details = null, logError = false) => {
    const error = new AppError(
      errorTypes.VALIDATION_ERROR,
      message,
      details
    )
    if (logError) logger.error(error)
    return error
  },

  authenticationError: (message = 'Authentication failed', logError = false) => {
    const error = new AppError(
      errorTypes.AUTHENTICATION_ERROR,
      message
    )
    if (logError) logger.error(error)
    return error
  },

  unauthorizedAccess: (logError = false) => {
    const error = new AppError(
      errorTypes.AUTHORIZATION_ERROR,
      'Unauthorized access'
    )
    if (logError) logger.error(error)
    return error
  },

  notFound: (resource = 'Resource', logError = false) => {
    const error = new AppError(
      errorTypes.NOT_FOUND,
      `${resource} not found`
    )
    if (logError) logger.error(error)
    return error
  },

  conflict: (message, logError = false) => {
    const error = new AppError(
      errorTypes.CONFLICT,
      message
    )
    if (logError) logger.error(error)
    return error
  },

  internalServer: (logError = false) => {
    const error = new AppError(
      errorTypes.INTERNAL_ERROR,
      'Internal server error'
    )
    if (logError) logger.error(error)
    return error
  },

  formatErrorForWire: (error) => {
    if (error instanceof AppError) {
      return {
        type: error.type,
        message: error.message,
        ...(error.details && { details: error.details })
      }
    }
    return new AppError(
      errorTypes.INTERNAL_ERROR,
      error.message || 'Internal server error'
    )
  }
}

const jsonResponse = (res, statusCode, error, data) => {
  const response = {
    error: error ? errors.formatErrorForWire(error) : null,
    data: data || null
  }

  return res.status(statusCode).json(response)
}

const statusCodeFromError = (error) => {
  if (!error || !error.type) return responseCodes.InternalServer

  const statusMap = {
    [errorTypes.VALIDATION_ERROR]: responseCodes.BadRequest,
    [errorTypes.AUTHENTICATION_ERROR]: responseCodes.Unauthorized,
    [errorTypes.AUTHORIZATION_ERROR]: responseCodes.Forbidden,
    [errorTypes.NOT_FOUND]: responseCodes.NotFound,
    [errorTypes.CONFLICT]: responseCodes.Conflict,
    [errorTypes.INTERNAL_ERROR]: responseCodes.InternalServer
  }

  return statusMap[error.type] || responseCodes.InternalServer
}

module.exports = {
  responseCodes,
  errorTypes,
  errors,
  jsonResponse,
  statusCodeFromError,
  AppError
}
