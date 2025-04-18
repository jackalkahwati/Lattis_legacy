import winston, { format, Logger, createLogger } from 'winston'

interface LogInfo extends winston.Logform.TransformableInfo {
  timestamp?: string
  stack?: string
  [key: string]: any
}

const logger: Logger = createLogger({
  level: process.env.NODE_ENV === 'development' ? 'debug' : 'info',
  format: format.combine(
    format.timestamp(),
    format.json(),
    format.errors({ stack: true }),
    format.printf((info: LogInfo) => {
      return JSON.stringify({
        timestamp: info.timestamp || new Date().toISOString(),
        level: info.level,
        message: info.message,
        ...(info.stack ? { stack: info.stack } : {}),
        ...Object.entries(info)
          .filter(([key]) => !['timestamp', 'level', 'message', 'stack'].includes(key))
          .reduce((acc, [key, value]) => ({ ...acc, [key]: value }), {}),
      })
    }),
  ),
  transports: [
    new winston.transports.Console({
      format: format.combine(format.colorize(), format.simple()),
    }),
  ],
  exitOnError: false,
})

// Create a stream object with a write function that will be used by Morgan
export const stream = {
  write: (message: string): void => {
    logger.info(message.trim())
  },
}

export { logger }

// Error tracking utility with proper typing
export const trackError = (error: Error, context?: Record<string, unknown>): void => {
  logger.error(error.message, {
    stack: error.stack,
    ...context,
  })
}

// Performance tracking utility with proper typing
export const trackPerformance = (
  label: string,
  timeInMs: number,
  metadata?: Record<string, unknown>,
): void => {
  logger.info(`Performance: ${label}`, {
    duration: timeInMs,
    ...metadata,
  })
}

// Custom error class for application-specific errors
export class AppError extends Error {
  public readonly statusCode: number
  public readonly code?: string

  constructor(message: string, statusCode = 500, code?: string) {
    super(message)
    this.name = 'AppError'
    this.statusCode = statusCode
    this.code = code
    Error.captureStackTrace(this, this.constructor)
    Object.setPrototypeOf(this, AppError.prototype)
  }

  public toJSON(): Record<string, unknown> {
    return {
      name: this.name,
      message: this.message,
      statusCode: this.statusCode,
      ...(this.code && { code: this.code }),
      ...(this.stack && { stack: this.stack }),
    }
  }
}
