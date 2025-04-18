import { Router, Request, Response, NextFunction } from 'express';
import { LockService } from '../services/lock.service';
import { validate, lockValidationRules } from '../middleware/validation.middleware';
import { query } from 'express-validator';

const router = Router();
const lockService = new LockService();

// Get all locks or filter by fleet_id
router.get('/', 
    validate([
        query('fleet_id')
            .optional()
            .isInt({ min: 1 })
            .withMessage('Valid fleet ID is required')
    ]),
    async (req: Request, res: Response, next: NextFunction) => {
        try {
            const { fleet_id } = req.query;
            const locks = fleet_id 
                ? await lockService.findByFleet(Number(fleet_id))
                : await lockService.findAll();
            res.json(locks);
        } catch (error) {
            next(error);
        }
    }
);

// Get lock by ID
router.get('/:id',
    validate(lockValidationRules.getById),
    async (req: Request, res: Response, next: NextFunction) => {
        try {
            const lock = await lockService.findOne(Number(req.params.id));
            res.json(lock);
        } catch (error) {
            next(error);
        }
    }
);

// Create new lock
router.post('/',
    validate(lockValidationRules.create),
    async (req: Request, res: Response, next: NextFunction) => {
        try {
            const newLock = await lockService.create(req.body);
            res.status(201).json(newLock);
        } catch (error) {
            next(error);
        }
    }
);

// Update lock
router.put('/:id',
    validate(lockValidationRules.update),
    async (req: Request, res: Response, next: NextFunction) => {
        try {
            const updatedLock = await lockService.update(Number(req.params.id), req.body);
            res.json(updatedLock);
        } catch (error) {
            next(error);
        }
    }
);

// Delete lock
router.delete('/:id',
    validate(lockValidationRules.getById),
    async (req: Request, res: Response, next: NextFunction) => {
        try {
            await lockService.delete(Number(req.params.id));
            res.status(204).send();
        } catch (error) {
            next(error);
        }
    }
);

export const LocksController: Router = router;
