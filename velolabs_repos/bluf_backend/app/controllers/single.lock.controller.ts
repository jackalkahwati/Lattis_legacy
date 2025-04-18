import { Router, Request, Response, NextFunction } from 'express';
import { LockService } from '../services/lock.service';
import { validate, lockValidationRules } from '../middleware/validation.middleware';

const router = Router();
const lockService = new LockService();

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

// Get lock by MAC ID
router.get('/',
    validate(lockValidationRules.getByMacId),
    async (req: Request, res: Response, next: NextFunction) => {
        try {
            const macId = req.query.mac_id as string;
            const lock = await lockService.findByMacId(macId);
            res.json(lock);
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

export const SingleLockController: Router = router;
