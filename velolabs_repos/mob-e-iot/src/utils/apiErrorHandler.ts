import { Request, Response } from 'express';
import logger, { ILogger } from './logger';

export class ApiError extends Error {
  constructor(public statusCode: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

interface ErrorResponse {
  success: boolean;
  message: string;
  stack?: string;
}

export const apiErrorHandler = (
  err: Error,
  req: Request,
  res: Response
) => {
  const response: ErrorResponse = {
    success: false,
    message: err instanceof ApiError ? err.message : 'Internal server error',
  };

  // Add stack trace in development
  if (process.env.NODE_ENV !== 'production') {
    response.stack = err.stack;
  }

  const logContext = {
    path: req.url,
    method: req.method,
    body: req.body,
    query: req.query,
    headers: req.headers,
    timestamp: new Date().toISOString()
  };

  if (err instanceof ApiError) {
    logger.error(`API Error [${err.statusCode}]: ${err.message}`, {
      ...logContext,
      statusCode: err.statusCode,
    });
    
    return res.status(err.statusCode).json(response);
  }

  // Log unexpected errors with full context
  logger.error(`Unexpected Error: ${err.message}`, {
    ...logContext,
    error: err instanceof Error ? {
      name: err.name,
      message: err.message,
      stack: err.stack
    } : err
  });

  return res.status(500).json(response);
};

// Error handling middleware
export const errorMiddleware = (
  err: Error,
  req: Request,
  res: Response,
  next: Function
) => {
  apiErrorHandler(err, req, res);
};
