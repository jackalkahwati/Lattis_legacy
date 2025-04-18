import { Request, Response, NextFunction } from 'express';
import logger from '../utils/logger';

export class HttpException extends Error {
  status: number;
  message: string;
  
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
    this.message = message;
  }
}

export const errorMiddleware = (
  error: HttpException,
  req: Request,
  res: Response,
  _next: NextFunction
) => {
  const status = error.status || 500;
  const message = error.message || 'Something went wrong';

  logger.error(`[${req.method}] ${req.path} >> StatusCode: ${status}, Message: ${message}`);

  res.status(status).json({
    status,
    message,
    stack: process.env.NODE_ENV === 'development' ? error.stack : undefined,
  });
};

export const notFoundMiddleware = (
  req: Request,
  _res: Response,
  next: NextFunction
) => {
  const error = new HttpException(404, `Cannot ${req.method} ${req.path}`);
  next(error);
};
