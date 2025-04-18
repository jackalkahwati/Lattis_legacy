import { Router, Request, Response, NextFunction } from 'express';
import { FleetService } from '../services/fleet.service';
import { validate, fleetValidationRules } from '../middleware/validation.middleware';

const router = Router();
const fleetService = new FleetService();

// Get all fleets
router.get('/', async (_req: Request, res: Response, next: NextFunction) => {
    try {
        const fleets = await fleetService.findAll();
        res.json(fleets);
    } catch (error) {
        next(error);
    }
});

// Get fleet by ID
router.get('/:id',
    validate(fleetValidationRules.getById),
    async (req: Request, res: Response, next: NextFunction) => {
        try {
            const fleet = await fleetService.findOne(Number(req.params.id));
            res.json(fleet);
        } catch (error) {
            next(error);
        }
    }
);

// Create new fleet
router.post('/',
    validate(fleetValidationRules.create),
    async (req: Request, res: Response, next: NextFunction) => {
        try {
            const newFleet = await fleetService.create(req.body);
            res.status(201).json(newFleet);
        } catch (error) {
            next(error);
        }
    }
);

// Update fleet
router.put('/:id',
    validate(fleetValidationRules.update),
    async (req: Request, res: Response, next: NextFunction) => {
        try {
            const updatedFleet = await fleetService.update(Number(req.params.id), req.body);
            res.json(updatedFleet);
        } catch (error) {
            next(error);
        }
    }
);

// Delete fleet
router.delete('/:id',
    validate(fleetValidationRules.getById),
    async (req: Request, res: Response, next: NextFunction) => {
        try {
            await fleetService.delete(Number(req.params.id));
            res.status(204).send();
        } catch (error) {
            next(error);
        }
    }
);

export const FleetsController: Router = router;
