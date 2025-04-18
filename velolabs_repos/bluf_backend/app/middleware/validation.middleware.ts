import { Request, Response, NextFunction } from 'express';
import { body, param, query, validationResult, ValidationChain } from 'express-validator';
import { HttpException } from './error.middleware';

export const validate = (validations: ValidationChain[]) => {
  return async (req: Request, _res: Response, next: NextFunction) => {
    await Promise.all(validations.map(validation => validation.run(req)));

    const errors = validationResult(req);
    if (errors.isEmpty()) {
      return next();
    }

    const extractedErrors: string[] = [];
    errors.array().map(err => extractedErrors.push(err.msg));

    throw new HttpException(400, extractedErrors.join(', '));
  };
};

export const lockValidationRules = {
  create: [
    body('mac_id')
      .isString()
      .notEmpty()
      .withMessage('MAC ID is required')
      .matches(/^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$/)
      .withMessage('Invalid MAC address format'),
    body('fleet_id')
      .isInt({ min: 1 })
      .withMessage('Valid fleet ID is required'),
    body('name')
      .isString()
      .notEmpty()
      .withMessage('Lock name is required')
      .trim()
      .isLength({ min: 2, max: 100 })
      .withMessage('Lock name must be between 2 and 100 characters')
  ],
  update: [
    param('id')
      .isInt({ min: 1 })
      .withMessage('Valid lock ID is required'),
    body('mac_id')
      .optional()
      .isString()
      .matches(/^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$/)
      .withMessage('Invalid MAC address format'),
    body('fleet_id')
      .optional()
      .isInt({ min: 1 })
      .withMessage('Valid fleet ID is required'),
    body('name')
      .optional()
      .isString()
      .trim()
      .isLength({ min: 2, max: 100 })
      .withMessage('Lock name must be between 2 and 100 characters')
  ],
  getById: [
    param('id')
      .isInt({ min: 1 })
      .withMessage('Valid lock ID is required')
  ],
  getByMacId: [
    query('mac_id')
      .isString()
      .notEmpty()
      .withMessage('MAC ID is required')
      .matches(/^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$/)
      .withMessage('Invalid MAC address format')
  ]
};

export const fleetValidationRules = {
  create: [
    body('fleet_name')
      .isString()
      .notEmpty()
      .withMessage('Fleet name is required')
      .trim()
      .isLength({ min: 2, max: 100 })
      .withMessage('Fleet name must be between 2 and 100 characters')
  ],
  update: [
    param('id')
      .isInt({ min: 1 })
      .withMessage('Valid fleet ID is required'),
    body('fleet_name')
      .optional()
      .isString()
      .trim()
      .isLength({ min: 2, max: 100 })
      .withMessage('Fleet name must be between 2 and 100 characters')
  ],
  getById: [
    param('id')
      .isInt({ min: 1 })
      .withMessage('Valid fleet ID is required')
  ]
};
