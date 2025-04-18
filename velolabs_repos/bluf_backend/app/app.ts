import 'reflect-metadata';
import express from 'express';
import { createConnection } from 'typeorm';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import * as dotenv from 'dotenv';

import { LocksController, FleetsController, SingleLockController } from './controllers';
import { errorMiddleware, notFoundMiddleware } from './middleware/error.middleware';
import logger from './utils/logger';
import ormconfig from '../ormconfig';

// Load environment variables
dotenv.config();

const app: express.Application = express();
const port = process.env.PORT || 3000;

// Security middleware
app.use(helmet());
app.use(cors({
    origin: process.env.CORS_ORIGIN || '*',
    methods: ['GET', 'POST', 'PUT', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));

// Rate limiting
const limiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100 // limit each IP to 100 requests per windowMs
});
app.use(limiter);

// Body parsing middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Request logging
app.use((req, _res, next) => {
    logger.info(`[${req.method}] ${req.path}`);
    next();
});

// Routes
app.use('/lock', SingleLockController);
app.use('/locks', LocksController);
app.use('/fleets', FleetsController);

// Error handling
app.use(notFoundMiddleware);
app.use(errorMiddleware);

// Database connection and server startup
const startServer = async () => {
    try {
        await createConnection(ormconfig);
        logger.info('Database connected successfully');

        app.listen(port, () => {
            logger.info(`Server running on port ${port}`);
        });
    } catch (error) {
        logger.error('Error starting server:', error);
        process.exit(1);
    }
};

// Handle uncaught exceptions
process.on('uncaughtException', (error) => {
    logger.error('Uncaught Exception:', error);
    process.exit(1);
});

// Handle unhandled promise rejections
process.on('unhandledRejection', (error) => {
    logger.error('Unhandled Rejection:', error);
    process.exit(1);
});

startServer();

export default app;
